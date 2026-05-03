package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;

import by.bsuir.growpathserver.trainee.application.dto.TaskRecommendationDto;

public interface TaskRecommendationService {
    List<TaskRecommendationDto> getRecommendedTasks(Long internId, int limit);
}
