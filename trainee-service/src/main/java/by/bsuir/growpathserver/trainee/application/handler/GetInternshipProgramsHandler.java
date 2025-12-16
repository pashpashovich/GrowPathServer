package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternshipProgramsHandler {

    private final InternshipProgramRepository repository;

    public Page<InternshipProgram> handle(GetInternshipProgramsQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<InternshipProgramEntity> spec = (root, criteriaQuery, cb) -> cb.conjunction();

        if (query.status() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("status"), query.status()));
        }

        if (query.search() != null && !query.search().isBlank()) {
            String searchPattern = "%" + query.search().toLowerCase() + "%";
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.or(
                                            criteriaBuilder.like(
                                                    criteriaBuilder.lower(root.get("title")), searchPattern),
                                            criteriaBuilder.like(
                                                    criteriaBuilder.lower(root.get("description")), searchPattern)
                                    ));
        }

        Page<InternshipProgramEntity> entityPage = repository.findAll(spec, pageable);
        return entityPage.map(InternshipProgram::fromEntity);
    }
}
