package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetAssessmentsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Assessment;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetAssessmentsHandler {

    private final AssessmentRepository assessmentRepository;

    public Page<Assessment> handle(GetAssessmentsQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<AssessmentEntity> spec = (root, criteriaQuery, cb) -> cb.conjunction();

        if (query.internId() != null && !query.internId().isBlank()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("internId"), query.internId()));
        }

        if (query.mentorId() != null && !query.mentorId().isBlank()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("mentorId"), query.mentorId()));
        }

        if (query.internshipId() != null && !query.internshipId().isBlank()) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("internshipId"), query.internshipId()));
        }

        Page<AssessmentEntity> entityPage = assessmentRepository.findAll(spec, pageable);
        return entityPage.map(Assessment::fromEntity);
    }
}
