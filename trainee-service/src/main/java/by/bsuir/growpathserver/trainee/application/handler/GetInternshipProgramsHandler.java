package by.bsuir.growpathserver.trainee.application.handler;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.query.GetInternshipProgramsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.ItDirectionEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternshipProgramsHandler {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final InternshipProgramRepository repository;

    @Transactional(readOnly = true)
    public Page<InternshipProgram> handle(GetInternshipProgramsQuery query) {
        int pageIndex = Math.max(0, query.page() != null && query.page() > 0 ? query.page() - 1 : 0);
        int rawLimit = query.limit() != null && query.limit() > 0 ? query.limit() : DEFAULT_PAGE_SIZE;
        int limit = Math.min(rawLimit, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(pageIndex, limit);

        Specification<InternshipProgramEntity> spec = Stream.of(
                        statusSpecification(query),
                        searchSpecification(query),
                        itDirectionSpecification(query),
                        startDateFromSpecification(query),
                        startDateToSpecification(query),
                        maxPlacesMinSpecification(query),
                        maxPlacesMaxSpecification(query),
                        competencySpecification(query)
                )
                .flatMap(Optional::stream)
                .reduce(Specification::and)
                .orElseGet(() -> (root, cq, cb) -> cb.conjunction());

        Page<InternshipProgramEntity> entityPage = repository.findAll(spec, pageable);
        return entityPage.map(InternshipProgram::fromEntity);
    }

    private static Optional<Specification<InternshipProgramEntity>> statusSpecification(GetInternshipProgramsQuery query) {
        return Optional.ofNullable(query.status())
                .map(status -> (Specification<InternshipProgramEntity>) (root, cq, cb) ->
                        cb.equal(root.get("status"), status))
                .or(() -> Boolean.TRUE.equals(query.includeArchived()) ?
                        Optional.empty() :
                        Optional.of((root, cq, cb) ->
                                            cb.notEqual(root.get("status"), InternshipProgramStatus.ARCHIVED)));
    }

    private static Optional<Specification<InternshipProgramEntity>> searchSpecification(GetInternshipProgramsQuery query) {
        return Optional.ofNullable(query.search())
                .filter(s -> !s.isBlank())
                .map(String::trim)
                .map(s -> "%" + s.toLowerCase() + "%")
                .map(pattern -> (Specification<InternshipProgramEntity>) (root, cq, cb) -> cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
    }

    private static Optional<Specification<InternshipProgramEntity>> itDirectionSpecification(
            GetInternshipProgramsQuery query
    ) {
        return Optional.ofNullable(query.itDirection())
                .filter(d -> !d.isBlank())
                .map(String::trim)
                .map(String::toLowerCase)
                .map(dir -> (Specification<InternshipProgramEntity>) (root, cq, cb) -> {
                    Join<InternshipProgramEntity, ItDirectionEntity> join =
                            root.join("itDirection", JoinType.INNER);
                    return cb.equal(cb.lower(join.get("code")), dir);
                });
    }

    private static Optional<Specification<InternshipProgramEntity>> startDateFromSpecification(
            GetInternshipProgramsQuery query
    ) {
        return Optional.ofNullable(query.startDateFrom())
                .map(from -> (Specification<InternshipProgramEntity>) (root, cq, cb) ->
                        cb.greaterThanOrEqualTo(root.get("startDate"), from));
    }

    private static Optional<Specification<InternshipProgramEntity>> startDateToSpecification(
            GetInternshipProgramsQuery query
    ) {
        return Optional.ofNullable(query.startDateTo())
                .map(to -> (Specification<InternshipProgramEntity>) (root, cq, cb) ->
                        cb.lessThanOrEqualTo(root.get("startDate"), to));
    }

    private static Optional<Specification<InternshipProgramEntity>> maxPlacesMinSpecification(
            GetInternshipProgramsQuery query
    ) {
        return Optional.ofNullable(query.maxPlacesMin())
                .map(min -> (Specification<InternshipProgramEntity>) (root, cq, cb) ->
                        cb.greaterThanOrEqualTo(root.get("maxPlaces"), min));
    }

    private static Optional<Specification<InternshipProgramEntity>> maxPlacesMaxSpecification(
            GetInternshipProgramsQuery query
    ) {
        return Optional.ofNullable(query.maxPlacesMax())
                .map(max -> (Specification<InternshipProgramEntity>) (root, cq, cb) ->
                        cb.lessThanOrEqualTo(root.get("maxPlaces"), max));
    }

    private static Optional<Specification<InternshipProgramEntity>> competencySpecification(
            GetInternshipProgramsQuery query
    ) {
        return Optional.ofNullable(query.competencyId())
                .map(competencyId -> (Specification<InternshipProgramEntity>) (root, cq, cb) -> {
                    cq.distinct(true);
                    Join<InternshipProgramEntity, CompetencyEntity> join =
                            root.join("competencies", JoinType.INNER);
                    return cb.equal(join.get("id"), competencyId);
                });
    }
}
