package by.bsuir.growpathserver.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import by.bsuir.growpathserver.notification.config.TestFeignConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestFeignConfig.class)
class NotificationServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
