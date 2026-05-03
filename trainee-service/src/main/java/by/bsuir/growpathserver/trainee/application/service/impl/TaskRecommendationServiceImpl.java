package by.bsuir.growpathserver.trainee.application.service.impl;

import by.bsuir.growpathserver.trainee.application.dto.TaskRecommendationDto;
import by.bsuir.growpathserver.trainee.application.service.TaskRecommendationService;
import by.bsuir.growpathserver.trainee.domain.entity.*;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskPriority;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация алгоритма формирования персонализированных рекомендаций по задачам.
 *
 * АЛГОРИТМ 2: ФОРМИРОВАНИЕ ПЕРСОНАЛИЗИРОВАННЫХ РЕКОМЕНДАЦИЙ ПО ЗАДАЧАМ
 *
 * Описание алгоритма по шагам:
 *
 * Шаг 1: Получение данных стажера
 *   - Загрузить информацию о стажере из базы данных
 *   - Проверить существование стажера
 *   - Получить активный ИПР стажера
 *
 * Шаг 2: Анализ истории выполнения задач
 *   2.1. Получить все завершенные задачи стажера
 *   2.2. Извлечь компетенции из завершенных задач
 *   2.3. Рассчитать уровень владения каждой компетенцией:
 *        competencyLevel = (completedTasksWithCompetency / totalTasksWithCompetency) * avgRating
 *   2.4. Создать профиль компетенций стажера
 *
 * Шаг 3: Получение доступных задач
 *   3.1. Загрузить все задачи из ИПР стажера
 *   3.2. Отфильтровать задачи со статусом PENDING или IN_PROGRESS
 *   3.3. Исключить задачи, уже назначенные другим стажерам
 *   3.4. Получить компетенции для каждой задачи
 *
 * Шаг 4: Расчет оценки релевантности для каждой задачи
 *   Для каждой доступной задачи:
 *   4.1. Рассчитать соответствие компетенций (0-40 баллов):
 *        - Для каждой компетенции задачи проверить уровень владения стажера
 *        - competencyScore = sum(internCompetencyLevels) / taskCompetenciesCount * 40
 *   4.2. Рассчитать приоритетный балл (0-30 баллов):
 *        - HIGH → 30 баллов
 *        - MEDIUM → 20 баллов
 *        - LOW → 10 баллов
 *   4.3. Рассчитать срочность (0-20 баллов):
 *        - Если дедлайн < 3 дней → 20 баллов
 *        - Если дедлайн < 7 дней → 15 баллов
 *        - Если дедлайн < 14 дней → 10 баллов
 *        - Иначе → 5 баллов
 *   4.4. Рассчитать балл последовательности (0-10 баллов):
 *        - Если задача из текущего активного этапа → 10 баллов
 *        - Если задача из следующего этапа → 5 баллов
 *        - Иначе → 0 баллов
 *   4.5. Итоговая оценка релевантности:
 *        relevanceScore = competencyScore + priorityScore + urgencyScore + sequenceScore
 *
 * Шаг 5: Определение сложности задачи
 *   5.1. Подсчитать количество компетенций задачи
 *   5.2. Определить уровень сложности (1-5):
 *        - 1 компетенция → уровень 1
 *        - 2 компетенции → уровень 2
 *        - 3 компетенции → уровень 3
 *        - 4 компетенции → уровень 4
 *        - 5+ компетенций → уровень 5
 *
 * Шаг 6: Формирование причины рекомендации
 *   6.1. Определить основной фактор рекомендации (наибольший балл)
 *   6.2. Сформировать текстовое обоснование:
 *        - Высокое соответствие компетенций
 *        - Высокий приоритет
 *        - Приближающийся дедлайн
 *        - Следующая задача в последовательности
 *
 * Шаг 7: Сортировка и ограничение результатов
 *   7.1. Отсортировать задачи по убыванию relevanceScore
 *   7.2. Ограничить список до заданного лимита
 *   7.3. Для каждой задачи создать TaskRecommendationDto
 *
 * Шаг 8: Формирование результата
 *   8.1. Создать список объектов TaskRecommendationDto
 *   8.2. Заполнить все поля для каждой рекомендации
 *   8.3. Вернуть отсортированный список
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskRecommendationServiceImpl implements TaskRecommendationService {

    // Весовые коэффициенты для расчета релевантности
    private static final double COMPETENCY_WEIGHT = 40.0;
    private static final double PRIORITY_WEIGHT = 30.0;
    private static final double URGENCY_WEIGHT = 20.0;
    private static final double SEQUENCE_WEIGHT = 10.0;

    // Пороги срочности в днях
    private static final long URGENT_THRESHOLD = 3;
    private static final long HIGH_URGENCY_THRESHOLD = 7;
    private static final long MEDIUM_URGENCY_THRESHOLD = 14;

    private final UserRepository userRepository;
    private final IprRepository iprRepository;
    private final TaskRepository taskRepository;
    private final AssessmentRepository assessmentRepository;
    private final TaskCompetencyRepository taskCompetencyRepository;

    @Override
    @Transactional(readOnly = true)
    public List<TaskRecommendationDto> getRecommendedTasks(Long internId, int limit) {
        log.info("Starting task recommendations generation for intern id: {}", internId);

        // Шаг 1: Получение данных стажера
        UserEntity intern = userRepository.findById(internId)
                .orElseThrow(() -> new IllegalArgumentException("Intern with id " + internId + " not found"));

        IprEntity activeIpr = iprRepository.findActiveByInternId(internId)
                .orElseThrow(() -> new IllegalArgumentException("Active IPR for intern not found"));

        log.debug("Found active IPR with id: {}", activeIpr.getId());

        // Шаг 2: Анализ истории выполнения задач
        Map<Long, Double> internCompetencyProfile = buildInternCompetencyProfile(internId);
        log.debug("Built competency profile: {} competencies", internCompetencyProfile.size());

        // Шаг 3: Получение доступных задач
        List<TaskEntity> availableTasks = getAvailableTasks(activeIpr.getProgram().getId(), internId);
        log.debug("Found available tasks: {}", availableTasks.size());

        // Шаг 4-7: Расчет релевантности и формирование рекомендаций
        List<TaskRecommendationDto> recommendations = availableTasks.stream()
                .map(task -> buildRecommendation(task, internCompetencyProfile, activeIpr))
                .sorted(Comparator.comparing(TaskRecommendationDto::getRelevanceScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        log.info("Generated recommendations: {}", recommendations.size());

        return recommendations;
    }

    /**
     * Строит профиль компетенций стажера на основе истории выполнения задач.
     *
     * @param internId идентификатор стажера
     * @return карта: competencyId -> уровень владения (0-1)
     */
    private Map<Long, Double> buildInternCompetencyProfile(Long internId) {
        // Получаем все завершенные задачи стажера
        List<TaskEntity> completedTasks = taskRepository.findByAssigneeIdAndStatus(
                internId,
                TaskStatus.COMPLETED
        );

        if (completedTasks.isEmpty()) {
            return new HashMap<>();
        }

        // Получаем средний рейтинг стажера
        Double averageRating = completedTasks.stream()
                .filter(task -> task.getRating() != null)
                .mapToInt(TaskEntity::getRating)
                .average()
                .orElse(3.0); // Средний рейтинг по умолчанию

        // Подсчитываем частоту использования каждой компетенции
        Map<Long, Long> competencyFrequency = new HashMap<>();
        Map<Long, Long> totalCompetencyTasks = new HashMap<>();

        for (TaskEntity task : completedTasks) {
            List<TaskCompetencyEntity> taskCompetencies = taskCompetencyRepository.findByTaskId(task.getId());
            for (TaskCompetencyEntity tc : taskCompetencies) {
                Long competencyId = tc.getCompetency().getId();
                competencyFrequency.merge(competencyId, 1L, Long::sum);
            }
        }

        // Получаем общее количество задач с каждой компетенцией
        List<TaskEntity> allTasks = taskRepository.findByAssigneeId(internId);
        for (TaskEntity task : allTasks) {
            List<TaskCompetencyEntity> taskCompetencies = taskCompetencyRepository.findByTaskId(task.getId());
            for (TaskCompetencyEntity tc : taskCompetencies) {
                Long competencyId = tc.getCompetency().getId();
                totalCompetencyTasks.merge(competencyId, 1L, Long::sum);
            }
        }

        // Рассчитываем уровень владения каждой компетенцией
        Map<Long, Double> competencyProfile = new HashMap<>();
        for (Map.Entry<Long, Long> entry : competencyFrequency.entrySet()) {
            Long competencyId = entry.getKey();
            Long completed = entry.getValue();
            Long total = totalCompetencyTasks.getOrDefault(competencyId, 1L);

            double completionRate = (double) completed / total;
            double competencyLevel = (completionRate * averageRating) / 5.0; // Нормализация к [0, 1]

            competencyProfile.put(competencyId, competencyLevel);
        }

        return competencyProfile;
    }

    /**
     * Получает список доступных задач для рекомендации.
     *
     * @param programId идентификатор программы стажировки
     * @param internId идентификатор стажера
     * @return список доступных задач
     */
    private List<TaskEntity> getAvailableTasks(Long programId, Long internId) {
        List<TaskEntity> allTasks = taskRepository.findByInternshipId(programId);

        return allTasks.stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING || task.getStatus() == TaskStatus.IN_PROGRESS)
                .filter(task -> task.getAssigneeId() == null || task.getAssigneeId().equals(internId))
                .collect(Collectors.toList());
    }

    /**
     * Формирует рекомендацию для задачи.
     *
     * @param task задача
     * @param internCompetencyProfile профиль компетенций стажера
     * @param activeIpr активный ИПР
     * @return объект рекомендации
     */
    private TaskRecommendationDto buildRecommendation(
            TaskEntity task,
            Map<Long, Double> internCompetencyProfile,
            IprEntity activeIpr
    ) {
        // Получаем компетенции задачи
        List<TaskCompetencyEntity> taskCompetencies = taskCompetencyRepository.findByTaskId(task.getId());
        List<String> competencyNames = taskCompetencies.stream()
                .map(tc -> tc.getCompetency().getName())
                .collect(Collectors.toList());

        // Шаг 4.1: Расчет соответствия компетенций
        double competencyScore = calculateCompetencyScore(taskCompetencies, internCompetencyProfile);

        // Шаг 4.2: Расчет приоритетного балла
        double priorityScore = calculatePriorityScore(task.getPriority());

        // Шаг 4.3: Расчет срочности
        double urgencyScore = calculateUrgencyScore(task.getDueDate());
        long daysUntilDue = task.getDueDate() != null
                ? ChronoUnit.DAYS.between(LocalDateTime.now(), task.getDueDate())
                : Long.MAX_VALUE;

        // Шаг 4.4: Расчет балла последовательности
        double sequenceScore = calculateSequenceScore(task, activeIpr);

        // Шаг 4.5: Итоговая оценка релевантности
        double relevanceScore = competencyScore + priorityScore + urgencyScore + sequenceScore;

        // Шаг 5: Определение сложности задачи
        int difficultyLevel = calculateDifficultyLevel(taskCompetencies.size());

        // Шаг 6: Формирование причины рекомендации
        String recommendationReason = generateRecommendationReason(
                competencyScore,
                priorityScore,
                urgencyScore,
                sequenceScore
        );

        // Получаем информацию об этапе
        IprStageEntity stage = activeIpr.getStages().stream()
                .filter(s -> s.getId().equals(task.getStageId()))
                .findFirst()
                .orElse(null);

        return TaskRecommendationDto.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .relevanceScore(relevanceScore)
                .recommendationReason(recommendationReason)
                .difficultyLevel(difficultyLevel)
                .estimatedHours(estimateTaskHours(difficultyLevel))
                .relatedCompetencies(competencyNames)
                .stageId(stage != null ? stage.getId() : null)
                .stageTitle(stage != null ? stage.getTitle() : null)
                .daysUntilDue(daysUntilDue != Long.MAX_VALUE ? daysUntilDue : null)
                .build();
    }

    /**
     * Рассчитывает балл соответствия компетенций (0-40).
     */
    private double calculateCompetencyScore(
            List<TaskCompetencyEntity> taskCompetencies,
            Map<Long, Double> internProfile
    ) {
        if (taskCompetencies.isEmpty()) {
            return COMPETENCY_WEIGHT * 0.5; // Средний балл для задач без компетенций
        }

        double totalLevel = taskCompetencies.stream()
                .mapToDouble(tc -> internProfile.getOrDefault(tc.getCompetency().getId(), 0.0))
                .sum();

        double averageLevel = totalLevel / taskCompetencies.size();
        return averageLevel * COMPETENCY_WEIGHT;
    }

    /**
     * Рассчитывает приоритетный балл (0-30).
     */
    private double calculatePriorityScore(TaskPriority priority) {
        return switch (priority) {
            case HIGH -> PRIORITY_WEIGHT;
            case MEDIUM -> PRIORITY_WEIGHT * 0.67;
            case LOW -> PRIORITY_WEIGHT * 0.33;
        };
    }

    /**
     * Рассчитывает балл срочности (0-20).
     */
    private double calculateUrgencyScore(LocalDateTime dueDate) {
        if (dueDate == null) {
            return URGENCY_WEIGHT * 0.25;
        }

        long daysUntilDue = ChronoUnit.DAYS.between(LocalDateTime.now(), dueDate);

        if (daysUntilDue < 0) {
            return URGENCY_WEIGHT; // Просроченная задача - максимальная срочность
        } else if (daysUntilDue <= URGENT_THRESHOLD) {
            return URGENCY_WEIGHT;
        } else if (daysUntilDue <= HIGH_URGENCY_THRESHOLD) {
            return URGENCY_WEIGHT * 0.75;
        } else if (daysUntilDue <= MEDIUM_URGENCY_THRESHOLD) {
            return URGENCY_WEIGHT * 0.5;
        } else {
            return URGENCY_WEIGHT * 0.25;
        }
    }

    /**
     * Рассчитывает балл последовательности (0-10).
     */
    private double calculateSequenceScore(TaskEntity task, IprEntity ipr) {
        if (task.getStageId() == null) {
            return 0.0;
        }

        // Находим текущий активный этап
        Optional<IprStageEntity> currentStage = ipr.getStages().stream()
                .filter(stage -> stage.getStatus().name().equals("IN_PROGRESS"))
                .findFirst();

        if (currentStage.isPresent() && currentStage.get().getId().equals(task.getStageId())) {
            return SEQUENCE_WEIGHT; // Задача из текущего этапа
        }

        // Проверяем, является ли это следующим этапом
        Optional<IprStageEntity> nextStage = ipr.getStages().stream()
                .filter(stage -> stage.getStageOrder() > currentStage.map(IprStageEntity::getStageOrder).orElse(-1))
                .min(Comparator.comparing(IprStageEntity::getStageOrder));

        if (nextStage.isPresent() && nextStage.get().getId().equals(task.getStageId())) {
            return SEQUENCE_WEIGHT * 0.5; // Задача из следующего этапа
        }

        return 0.0;
    }

    /**
     * Определяет уровень сложности задачи (1-5).
     */
    private int calculateDifficultyLevel(int competencyCount) {
        return Math.min(Math.max(competencyCount, 1), 5);
    }

    /**
     * Оценивает время выполнения задачи в часах.
     */
    private int estimateTaskHours(int difficultyLevel) {
        return switch (difficultyLevel) {
            case 1 -> 4;
            case 2 -> 8;
            case 3 -> 16;
            case 4 -> 24;
            case 5 -> 40;
            default -> 8;
        };
    }

    /**
     * Генерирует текстовое обоснование рекомендации.
     */
    private String generateRecommendationReason(
            double competencyScore,
            double priorityScore,
            double urgencyScore,
            double sequenceScore
    ) {
        double maxScore = Math.max(Math.max(competencyScore, priorityScore),
                Math.max(urgencyScore, sequenceScore));

        if (maxScore == urgencyScore && urgencyScore >= URGENCY_WEIGHT * 0.75) {
            return "Приближающийся дедлайн требует немедленного внимания";
        } else if (maxScore == priorityScore && priorityScore >= PRIORITY_WEIGHT * 0.67) {
            return "Высокий приоритет задачи в рамках программы стажировки";
        } else if (maxScore == competencyScore && competencyScore >= COMPETENCY_WEIGHT * 0.7) {
            return "Отличное соответствие вашим текущим компетенциям";
        } else if (maxScore == sequenceScore) {
            return "Следующая задача в последовательности обучения";
        } else if (competencyScore >= COMPETENCY_WEIGHT * 0.3 && competencyScore < COMPETENCY_WEIGHT * 0.7) {
            return "Возможность развития новых компетенций";
        } else {
            return "Рекомендуется для расширения профессионального опыта";
        }
    }
}
