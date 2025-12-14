package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetTasksHandler {

    private final TaskRepository taskRepository;

    public Page<Task> handle(GetTasksQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<TaskEntity> spec = (root, criteriaQuery, cb) -> cb.conjunction();

        if (query.status() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("status"), query.status()));
        }

        if (query.assignee() != null && !query.assignee().isBlank()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("assigneeId"), query.assignee()));
        }

        if (query.priority() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("priority"), query.priority()));
        }

        if (query.internshipId() != null && !query.internshipId().isBlank()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("internshipId"), query.internshipId()));
        }

        if (query.mentorId() != null && !query.mentorId().isBlank()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("mentorId"), query.mentorId()));
        }

        Page<TaskEntity> entityPage = taskRepository.findAll(spec, pageable);
        return entityPage.map(Task::fromEntity);
    }
}
