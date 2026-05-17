package by.bsuir.growpathserver.trainee.application.handler;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.application.dto.RatingProfileDto;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetRatingProfileQuery;
import by.bsuir.growpathserver.trainee.application.service.RatingService;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetRatingProfileHandler {

    private final RatingService ratingService;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final UserRepository userRepository;

    public RatingProfileDto handle(GetRatingProfileQuery query) {
        Long currentUserId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));

        if (!currentUserId.equals(query.internId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        UserEntity user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!UserRole.INTERN.equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        return ratingService.getRatingProfile(query);
    }
}
