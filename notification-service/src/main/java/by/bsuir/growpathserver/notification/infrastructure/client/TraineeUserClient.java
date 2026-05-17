package by.bsuir.growpathserver.notification.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import by.bsuir.growpathserver.notification.infrastructure.client.config.TraineeFeignClientConfig;
import by.bsuir.growpathserver.notification.infrastructure.client.dto.TraineeUserDto;

@FeignClient(
        name = "trainee-user-client",
        url = "${app.trainee-service.url}",
        configuration = TraineeFeignClientConfig.class
)
public interface TraineeUserClient {

    @GetMapping("/api/v1/users/{id}")
    TraineeUserDto getUserById(@PathVariable("id") Long id);
}
