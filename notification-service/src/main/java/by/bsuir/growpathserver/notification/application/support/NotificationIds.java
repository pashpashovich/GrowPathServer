package by.bsuir.growpathserver.notification.application.support;

import by.bsuir.growpathserver.notification.application.exception.InvalidNotificationIdException;

public final class NotificationIds {

    private NotificationIds() {
    }

    public static Long parseRequired(String rawId, String fieldName) {
        if (rawId == null || rawId.isBlank()) {
            throw new InvalidNotificationIdException(fieldName);
        }
        try {
            return Long.parseLong(rawId);
        }
        catch (NumberFormatException ex) {
            throw new InvalidNotificationIdException(fieldName);
        }
    }

    public static Long parseOptional(String rawId, String fieldName) {
        if (rawId == null || rawId.isBlank()) {
            return null;
        }
        return parseRequired(rawId, fieldName);
    }
}
