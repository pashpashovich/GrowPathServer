package by.bsuir.growpathserver.trainee.application.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.application.dto.RatingProfileDto;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.query.GetRatingProfileQuery;
import by.bsuir.growpathserver.trainee.application.service.RatingService;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class GetRatingProfileHandlerTest {

    @Mock
    private RatingService ratingService;

    @Mock
    private CurrentApplicationUserResolver currentApplicationUserResolver;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetRatingProfileHandler handler;

    @Test
    void handle_returnsProfileForIntern() {
        when(currentApplicationUserResolver.resolveCurrentUserDatabaseId()).thenReturn(Optional.of(7L));
        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setRole(UserRole.INTERN);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        RatingProfileDto expected = RatingProfileDto.builder()
                .internId(7L)
                .internName("Test Intern")
                .hasAssessment(false)
                .build();
        when(ratingService.getRatingProfile(any(GetRatingProfileQuery.class))).thenReturn(expected);

        RatingProfileDto result = handler.handle(new GetRatingProfileQuery(7L, 12, 5));

        assertEquals(expected, result);
        verify(ratingService).getRatingProfile(new GetRatingProfileQuery(7L, 12, 5));
    }

    @Test
    void handle_forbiddenForMentor() {
        when(currentApplicationUserResolver.resolveCurrentUserDatabaseId()).thenReturn(Optional.of(7L));
        UserEntity user = new UserEntity();
        user.setId(7L);
        user.setRole(UserRole.MENTOR);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class,
                     () -> handler.handle(new GetRatingProfileQuery(7L, 12, 5)));
    }
}
