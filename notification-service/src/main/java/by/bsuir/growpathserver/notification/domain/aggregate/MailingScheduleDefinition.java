package by.bsuir.growpathserver.notification.domain.aggregate;

import java.time.LocalTime;

import by.bsuir.growpathserver.notification.domain.valueobject.WeekDay;

public record MailingScheduleDefinition(WeekDay weekDay, LocalTime executeTime) {
}
