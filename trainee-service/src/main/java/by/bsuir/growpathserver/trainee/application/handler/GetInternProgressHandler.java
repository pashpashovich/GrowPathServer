package by.bsuir.growpathserver.trainee.application.handler;

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
        try {
            Long userId = Long.parseLong(query.internId());
            GetUserByIdQuery getUserQuery = new GetUserByIdQuery(userId);
            User user = getUserByIdHandler.handle(getUserQuery);
            if (user.getRole() != UserRole.INTERN) {
                throw new IllegalArgumentException("User is not an intern");
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid intern ID format: " + query.internId());
        }

        GetTasksQuery getTasksQuery = GetTasksQuery.builder()
                .assignee(query.internId())
                .build();
        Page<Task> allTasks = getTasksHandler.handle(getTasksQuery);

        long totalTasks = allTasks.getTotalElements();
        long completedTasks = allTasks.getContent().stream()
                .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                .count();
        long inProgressTasks = allTasks.getContent().stream()
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS)
                .count();
        long pendingTasks = allTasks.getContent().stream()
                .filter(task -> task.getStatus() == TaskStatus.PENDING)
                .count();

        double completionRate = totalTasks > 0 ? (double) completedTasks / totalTasks * 100.0 : 0.0;

        InternProgressResponse response = new InternProgressResponse();
        response.setInternId(query.internId());
        response.setTotalTasks((int) totalTasks);
        response.setCompletedTasks((int) completedTasks);
        response.setInProgressTasks((int) inProgressTasks);
        response.setPendingTasks((int) pendingTasks);
        response.setCompletionRate(completionRate);
        response.setAverageTaskTime(0.0); // TODO: Calculate from task completion times
        response.setStagesCompleted(0); // TODO: Calculate from stages
        response.setTotalStages(0); // TODO: Get from internship program

        return response;
    }
}
