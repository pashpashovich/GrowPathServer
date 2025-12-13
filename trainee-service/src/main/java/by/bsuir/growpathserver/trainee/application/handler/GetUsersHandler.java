package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.trainee.application.query.GetUsersQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetUsersHandler {

    private final UserRepository userRepository;

    public Page<User> handle(GetUsersQuery query) {
        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Specification<UserEntity> spec = (root, criteriaQuery, cb) -> cb.conjunction();

        if (query.role() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("role"), query.role()));
        }

        if (query.status() != null) {
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.equal(root.get("status"), query.status()));
        }

        if (query.search() != null && !query.search().isBlank()) {
            String searchPattern = "%" + query.search().toLowerCase() + "%";
            spec = spec.and((root, criteriaQuery, criteriaBuilder) ->
                                    criteriaBuilder.or(
                                            criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                                                                 searchPattern),
                                            criteriaBuilder.like(criteriaBuilder.lower(root.get("email")),
                                                                 searchPattern)
                                    ));
        }

        Page<UserEntity> entityPage = userRepository.findAll(spec, pageable);
        return entityPage.map(User::fromEntity);
    }
}
