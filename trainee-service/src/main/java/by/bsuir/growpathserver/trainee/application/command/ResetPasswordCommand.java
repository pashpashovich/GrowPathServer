package by.bsuir.growpathserver.trainee.application.command;

public record ResetPasswordCommand(String token, String newPassword) {
}
