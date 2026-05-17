package by.bsuir.growpathserver.notification.infrastructure.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import by.bsuir.growpathserver.notification.application.service.MailingExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailingExecutionScheduler {

    private final MailingExecutionService mailingExecutionService;

    @Scheduled(cron = "${app.mailing.scheduler-cron:0 * * * * *}")
    public void run() {
        try {
            mailingExecutionService.processDueMailings();
        }
        catch (Exception e) {
            log.error("Mailing scheduler failed: {}", e.getMessage(), e);
        }
    }
}
