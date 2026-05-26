package by.bsuir.growpathserver.trainee.application.handler;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetTasksQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Task;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.spec.TaskQuerySpecifications;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetTasksHandler {

    private final TaskRepository taskRepository;

    public Page<Task> handle(GetTasksQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit,
                                           Sort.by(Sort.Order.asc("sortOrder"), Sort.Order.asc("id")));

        Specification<TaskEntity> spec = (root, criteriaQuery, cb) -> cb.conjunction();

        if (query.status() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("status"), query.status()));
        }

        if (query.assignee() != null && !query.assignee().isBlank()) {
            try {
                Long assigneeId = Long.parseLong(query.assignee());
                spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                        criteriaBuilder.equal(root.get("assigneeId"), assigneeId));
            }
            catch (NumberFormatException e) {
            }
        }

        if (query.priority() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("priority"), query.priority()));
        }

        if (StringUtils.isNotBlank(query.internshipId())) {
            try {
                Long programId = Long.parseLong(query.internshipId().trim());
                spec = spec.and(TaskQuerySpecifications.belongsToInternshipProgram(programId));
            }
            catch (NumberFormatException e) {
            }
        }

        if (query.iprId() != null) {
            spec = spec.and(TaskQuerySpecifications.belongsToIpr(query.iprId()));
        }

        if (query.mentorId() != null && !query.mentorId().isBlank()) {
            try {
                Long mentorId = Long.parseLong(query.mentorId());
                spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                        criteriaBuilder.equal(root.get("mentorId"), mentorId));
            }
            catch (NumberFormatException e) {
            }
        }

        Page<TaskEntity> entityPage = taskRepository.findAll(spec, pageable);
        return entityPage.map(Task::fromEntity);
    }
}
