package by.bsuir.growpathserver.notification.application.service;

import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.notification.domain.entity.MailingEntity;
import by.bsuir.growpathserver.notification.domain.entity.MailingScheduleEntity;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingStatus;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;
import by.bsuir.growpathserver.notification.infrastructure.repository.MailingHistoryRepository;
import by.bsuir.growpathserver.notification.infrastructure.repository.MailingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailingExecutionService {

    private final MailingRepository mailingRepository;
    private final MailingHistoryRepository mailingHistoryRepository;
    private final MailingDispatchService mailingDispatchService;

    @Transactional
    public void processDueMailings() {
        LocalDateTime now = LocalDateTime.now();

        for (MailingEntity mailing : mailingRepository.findDueScheduledMailings(
                MailingStatus.SCHEDULED, MailingType.SCHEDULED, now)) {
            try {
                mailingDispatchService.dispatch(mailing.getId());
            }
            catch (Exception e) {
                log.error("Failed scheduled mailing {}: {}", mailing.getId(), e.getMessage());
            }
        }

        LocalTime currentTime = now.toLocalTime().withSecond(0).withNano(0);
        var currentDay = now.getDayOfWeek();

        for (MailingEntity mailing : mailingRepository.findActiveRecurringMailings(
                MailingStatus.SCHEDULED, MailingType.RECURRING)) {
            if (mailingHistoryRepository.existsByMailingIdAndSentAtAfter(
                    mailing.getId(), now.minusMinutes(1))) {
                continue;
            }
            boolean due = mailing.getSchedules().stream()
                    .anyMatch(schedule -> matches(schedule, currentDay, currentTime));
            if (!due) {
                continue;
            }
            try {
                mailingDispatchService.dispatch(mailing.getId());
            }
            catch (Exception e) {
                log.error("Failed recurring mailing {}: {}", mailing.getId(), e.getMessage());
            }
        }
    }

    private boolean matches(MailingScheduleEntity schedule,
                            java.time.DayOfWeek currentDay,
                            LocalTime currentTime) {
        return schedule.getWeekDay().toDayOfWeek() == currentDay
                && schedule.getExecuteTime().getHour() == currentTime.getHour()
                && schedule.getExecuteTime().getMinute() == currentTime.getMinute();
    }
}
