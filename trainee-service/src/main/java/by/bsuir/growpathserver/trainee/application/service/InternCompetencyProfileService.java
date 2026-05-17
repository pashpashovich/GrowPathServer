package by.bsuir.growpathserver.trainee.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.domain.entity.TaskCompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskCompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternCompetencyProfileService {

    private static final double DEFAULT_AVERAGE_RATING = 3.0;
    private static final double MAX_ACHIEVED_LEVEL = 5.0;

    private final TaskRepository taskRepository;
    private final TaskCompetencyRepository taskCompetencyRepository;

    @Transactional(readOnly = true)
    public Map<Long, Double> buildNormalizedProfile(Long internId) {
        List<TaskEntity> completedTasks = taskRepository.findByAssigneeIdAndStatus(internId, TaskStatus.COMPLETED);
        if (completedTasks.isEmpty()) {
            return Map.of();
        }

        double averageRating = completedTasks.stream()
                .filter(task -> task.getRating() != null)
                .mapToInt(TaskEntity::getRating)
                .average()
                .orElse(DEFAULT_AVERAGE_RATING);

        Map<Long, Long> competencyFrequency = new HashMap<>();
        Map<Long, Long> totalCompetencyTasks = new HashMap<>();

        for (TaskEntity task : completedTasks) {
            for (TaskCompetencyEntity taskCompetency : taskCompetencyRepository.findByTaskId(task.getId())) {
                Long competencyId = taskCompetency.getCompetency().getId();
                competencyFrequency.merge(competencyId, 1L, Long::sum);
            }
        }

        for (TaskEntity task : taskRepository.findByAssigneeId(internId)) {
            for (TaskCompetencyEntity taskCompetency : taskCompetencyRepository.findByTaskId(task.getId())) {
                Long competencyId = taskCompetency.getCompetency().getId();
                totalCompetencyTasks.merge(competencyId, 1L, Long::sum);
            }
        }

        Map<Long, Double> profile = new HashMap<>();
        for (Map.Entry<Long, Long> entry : competencyFrequency.entrySet()) {
            Long competencyId = entry.getKey();
            long completed = entry.getValue();
            long total = totalCompetencyTasks.getOrDefault(competencyId, 1L);
            double completionRate = (double) completed / total;
            double normalizedLevel = (completionRate * averageRating) / MAX_ACHIEVED_LEVEL;
            profile.put(competencyId, normalizedLevel);
        }
        return profile;
    }

    public double toAchievedLevelOutOfFive(Map<Long, Double> normalizedProfile, Long competencyId) {
        double normalized = normalizedProfile.getOrDefault(competencyId, 0.0);
        return Math.round(normalized * MAX_ACHIEVED_LEVEL * 10.0) / 10.0;
    }
}
