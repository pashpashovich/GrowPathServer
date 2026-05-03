package by.bsuir.growpathserver.trainee.application.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.InternProgressDto;
import by.bsuir.growpathserver.trainee.application.service.InternProgressCalculationService;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprStageEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapStageStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class InternProgressCalculationServiceImpl implements InternProgressCalculationService {

    private static final double PROGRESS_THRESHOLD = 10.0;
    private static final double MIN_VELOCITY = 0.1;

    private final IprRepository iprRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public InternProgressDto calculateProgress(Long iprId) {
        log.info("Calculate ipr progress with id: {}", iprId);

        IprEntity ipr = iprRepository.findById(iprId)
                .orElseThrow(() -> new IllegalArgumentException("ИПР с id " + iprId + " не найден"));

        List<IprStageEntity> stages = ipr.getStages();
        log.debug("Loaded stages: {}", stages.size());

        List<TaskEntity> allTasks = taskRepository.findByInternshipId(ipr.getProgram().getId());
        log.debug("Loaded tasks: {}", allTasks.size());

        List<InternProgressDto.StageProgressDto> stageProgressList = new ArrayList<>();
        int totalCompletedTasks = 0;
        int totalTasks = 0;

        for (IprStageEntity stage : stages) {
            List<TaskEntity> stageTasks = allTasks.stream()
                    .filter(task -> stage.getId().equals(task.getStageId()))
                    .collect(Collectors.toList());

            int stageTaskCount = stageTasks.size();
            int stageCompletedCount = (int) stageTasks.stream()
                    .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                    .count();

            double stageProgress = stageTaskCount > 0
                    ? (stageCompletedCount * 100.0) / stageTaskCount
                    : 0.0;

            InternProgressDto.StageProgressDto stageProgressDto = InternProgressDto.StageProgressDto.builder()
                    .stageId(stage.getId())
                    .stageTitle(stage.getTitle())
                    .progressPercentage(stageProgress)
                    .completedTasks(stageCompletedCount)
                    .totalTasks(stageTaskCount)
                    .startDate(stage.getStartDate())
                    .endDate(stage.getEndDate())
                    .status(stage.getStatus().name())
                    .build();

            stageProgressList.add(stageProgressDto);

            totalTasks += stageTaskCount;
            totalCompletedTasks += stageCompletedCount;
        }

        double overallProgress = totalTasks > 0
                ? (totalCompletedTasks * 100.0) / totalTasks
                : 0.0;

        int completedStages = (int) stages.stream()
                .filter(stage -> RoadmapStageStatus.COMPLETED.equals(stage.getStatus()))
                .count();

        log.debug("Overall progress: {}%, completed tasks: {}/{}", overallProgress, totalCompletedTasks, totalTasks);

        Double averageRating = allTasks.stream()
                .filter(task -> TaskStatus.COMPLETED.equals(task.getStatus()))
                .filter(task -> task.getRating() != null)
                .mapToInt(TaskEntity::getRating)
                .average()
                .orElse(0.0);

        InternProgressDto.ProgressStatus progressStatus = determineProgressStatus(
                ipr.getStartDate(),
                ipr.getEndDate(),
                overallProgress
        );

        LocalDate estimatedCompletionDate = estimateCompletionDate(
                ipr.getStartDate(),
                totalCompletedTasks,
                totalTasks
        );

        InternProgressDto result = InternProgressDto.builder()
                .iprId(ipr.getId())
                .internId(ipr.getIntern().getId())
                .overallProgress(overallProgress)
                .completedTasks(totalCompletedTasks)
                .totalTasks(totalTasks)
                .completedStages(completedStages)
                .totalStages(stages.size())
                .status(progressStatus)
                .estimatedCompletionDate(estimatedCompletionDate)
                .plannedEndDate(ipr.getEndDate())
                .stageProgress(stageProgressList)
                .averageTaskRating(averageRating)
                .build();

        log.info("Progress calculation completed. Status: {}, Progress: {}%", progressStatus, overallProgress);

        return result;
    }

    private InternProgressDto.ProgressStatus determineProgressStatus(
            LocalDate startDate,
            LocalDate endDate,
            double actualProgress
    ) {
        LocalDate today = LocalDate.now();

        if (today.isBefore(startDate)) {
            return InternProgressDto.ProgressStatus.ON_TRACK;
        }

        if (today.isAfter(endDate)) {
            return actualProgress >= 100.0
                    ? InternProgressDto.ProgressStatus.ON_TRACK
                    : InternProgressDto.ProgressStatus.BEHIND_SCHEDULE;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        long elapsedDays = ChronoUnit.DAYS.between(startDate, today);

        double expectedProgress = totalDays > 0
                ? (elapsedDays * 100.0) / totalDays
                : 0.0;

        log.debug("Expected progress: {}%, Actual: {}%", expectedProgress, actualProgress);

        if (actualProgress >= expectedProgress + PROGRESS_THRESHOLD) {
            return InternProgressDto.ProgressStatus.AHEAD_OF_SCHEDULE;
        }
        else if (actualProgress < expectedProgress - PROGRESS_THRESHOLD) {
            return InternProgressDto.ProgressStatus.BEHIND_SCHEDULE;
        }
        else {
            return InternProgressDto.ProgressStatus.ON_TRACK;
        }
    }

    private LocalDate estimateCompletionDate(
            LocalDate startDate,
            int completedTasks,
            int totalTasks
    ) {
        LocalDate today = LocalDate.now();

        if (completedTasks >= totalTasks) {
            return today;
        }

        long elapsedDays = ChronoUnit.DAYS.between(startDate, today);
        if (elapsedDays <= 0) {
            return today;
        }

        double velocity = (double) completedTasks / elapsedDays;

        if (velocity < MIN_VELOCITY) {
            velocity = MIN_VELOCITY;
        }

        int remainingTasks = totalTasks - completedTasks;
        long remainingDays = (long) Math.ceil(remainingTasks / velocity);

        LocalDate estimatedDate = today.plusDays(remainingDays);

        log.debug("Estimated completion: {} (velocity: {} tasks/day)", estimatedDate, velocity);

        return estimatedDate;
    }
}
