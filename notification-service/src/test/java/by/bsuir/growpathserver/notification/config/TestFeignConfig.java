package by.bsuir.growpathserver.notification.config;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import by.bsuir.growpathserver.notification.infrastructure.client.TraineeUserClient;
import by.bsuir.growpathserver.notification.infrastructure.client.dto.TraineeUserDto;

@TestConfiguration
@Profile("test")
public class TestFeignConfig {

    @Bean
    public TraineeUserClient traineeUserClient() {
        TraineeUserClient client = mock(TraineeUserClient.class);
        when(client.getUserById(anyLong())).thenAnswer(invocation -> {
            TraineeUserDto user = new TraineeUserDto();
            user.setId(invocation.getArgument(0));
            user.setEmail("test.user@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            return user;
        });
        return client;
    }
}
