package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.dto.model.InternListResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.trainee.application.query.GetInternsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetUsersQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternMapper;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetInternsHandler {

    private final GetUsersHandler getUsersHandler;
    private final InternMapper internMapper;

    public InternListResponse handle(GetInternsQuery query) {
        GetUsersQuery getUsersQuery = GetUsersQuery.builder()
                .page(query.page())
                .limit(query.limit())
                .search(query.search())
                .role(UserRole.INTERN)
                .status(query.status() != null ? UserStatus.fromString(query.status()) : null)
                .build();

        Page<User> usersPage = getUsersHandler.handle(getUsersQuery);

        InternListResponse response = new InternListResponse();
        response.setData(usersPage.getContent().stream()
                                 .map(internMapper::toInternResponse)
                                 .toList());

        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(usersPage.getNumber() + 1);
        pagination.setLimit(usersPage.getSize());
        pagination.setTotal((int) usersPage.getTotalElements());
        pagination.setTotalPages(usersPage.getTotalPages());
        response.setPagination(pagination);

        return response;
    }
}
