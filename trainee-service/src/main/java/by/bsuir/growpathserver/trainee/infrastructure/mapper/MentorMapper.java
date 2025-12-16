package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import by.bsuir.growpathserver.dto.model.MentorInternsResponse;
import by.bsuir.growpathserver.dto.model.MentorInternsResponseDataInner;
import by.bsuir.growpathserver.dto.model.MentorResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;

@Mapper(componentModel = "spring")
public interface MentorMapper {

    default MentorResponse toMentorResponse(User user,
                                            TaskRepository taskRepository,
                                            AssessmentRepository assessmentRepository) {
        MentorResponse response = new MentorResponse();
        response.setId(String.valueOf(user.getId()));
        response.setUserId(String.valueOf(user.getId()));
        response.setName(user.getName());
        response.setEmail(user.getEmail().value());
        response.setDepartment(null);
        response.setCreatedAt(user.getCreatedAt());

        long totalInterns = assessmentRepository.findAll().stream()
                .filter(assessment -> assessment.getMentorId().equals(user.getId()))
                .map(assessment -> assessment.getInternId())
                .distinct()
                .count();
        response.setTotalInterns((int) totalInterns);

        long activeTasks = taskRepository.findAll().stream()
                .filter(task -> task.getMentorId().equals(user.getId()))
                .filter(task -> task.getStatus() == TaskStatus.IN_PROGRESS || task.getStatus() == TaskStatus.SUBMITTED)
                .count();
        response.setActiveTasks((int) activeTasks);

        return response;
    }

    default MentorInternsResponse toMentorInternsResponse(List<User> interns, TaskRepository taskRepository) {
        MentorInternsResponse response = new MentorInternsResponse();
        response.setData(interns.stream()
                                 .map(intern -> {
                                     MentorInternsResponseDataInner internData = new MentorInternsResponseDataInner();
                                     internData.setId(String.valueOf(intern.getId()));
                                     internData.setName(intern.getName());
                                     internData.setEmail(intern.getEmail().value());
                                     internData.setDepartment(null);
                                     internData.setPosition(null);
                                     internData.setStatus(convertUserStatusToInternStatus(intern.getStatus()));

                                     long totalTasks = taskRepository.findAll().stream()
                                             .filter(task -> task.getAssigneeId() != null && task.getAssigneeId()
                                                     .equals(intern.getId()))
                                             .count();
                                     internData.setTotalTasks((int) totalTasks);

                                     long tasksCompleted = taskRepository.findAll().stream()
                                             .filter(task -> task.getAssigneeId() != null && task.getAssigneeId()
                                                     .equals(intern.getId()))
                                             .filter(task -> task.getStatus() == TaskStatus.COMPLETED)
                                             .count();
                                     internData.setTasksCompleted((int) tasksCompleted);

                                     double avgRating = taskRepository.findAll().stream()
                                             .filter(task -> task.getAssigneeId() != null && task.getAssigneeId()
                                                     .equals(intern.getId()))
                                             .filter(task -> task.getRating() != null)
                                             .mapToDouble(task -> task.getRating())
                                             .average()
                                             .orElse(0.0);
                                     internData.setRating(avgRating);

                                     return internData;
                                 })
                                 .collect(Collectors.toList()));
        return response;
    }

    default MentorInternsResponseDataInner.StatusEnum convertUserStatusToInternStatus(by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus status) {
        if (status == null) {
            return MentorInternsResponseDataInner.StatusEnum.ACTIVE;
        }
        return switch (status) {
            case ACTIVE -> MentorInternsResponseDataInner.StatusEnum.ACTIVE;
            case BLOCKED, PENDING -> MentorInternsResponseDataInner.StatusEnum.PAUSED;
        };
    }
}
