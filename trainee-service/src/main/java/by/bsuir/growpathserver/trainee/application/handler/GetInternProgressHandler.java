package by.bsuir.growpathserver.trainee.application.handler;

import java.util.ArrayList;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.InternProgressResponse;
import by.bsuir.growpathserver.trainee.application.query.GetInternProgressQuery;
import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUserByIdQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternProgressHandler {

    private final GetUserByIdHandler getUserByIdHandler;
    private final GetTasksHandler getTasksHandler;

    public InternProgressResponse handle(GetInternProgressQuery query) {
        final Long userId;
        try {
            userId = Long.parseLong(query.internId());
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid intern ID format: " + query.internId());
        }

        GetUserByIdQuery getUserQuery = new GetUserByIdQuery(userId);
        User user = getUserByIdHandler.handle(getUserQuery);
        if (user.getRole() != UserRole.INTERN) {
            throw new IllegalArgumentException("User is not an intern");
        }

        GetTasksQuery getTasksQuery = GetTasksQuery.builder()
                .assignee(query.internId())
                .build();
        Page<Task> allTasks = getTasksHandler.handle(getTasksQuery);

        long totalTasks = allTasks.getTotalElements();
        long completedTasks = allTasks.getContent().stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();

        double overallProgress = totalTasks > 0 ? (double) completedTasks / totalTasks * 100.0 : 0.0;

        InternProgressResponse response = new InternProgressResponse();
        response.setIprId(null);
        response.setInternId(userId);
        response.setTotalTasks((int) totalTasks);
        response.setCompletedTasks((int) completedTasks);
        response.setOverallProgress(overallProgress);
        response.setCompletedStages(0);
        response.setTotalStages(0);
        response.setStatus(InternProgressResponse.StatusEnum.ON_TRACK);
        response.setEstimatedCompletionDate(null);
        response.setPlannedEndDate(null);
        response.setStageProgress(new ArrayList<>());
        response.setAverageTaskRating(0.0);

        return response;
    }
}
