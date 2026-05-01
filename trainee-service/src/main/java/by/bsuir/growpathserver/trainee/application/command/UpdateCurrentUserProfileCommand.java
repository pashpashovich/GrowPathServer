package by.bsuir.growpathserver.trainee.application.command;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateCurrentUserProfileCommand {
    private final String firstName;
    private final String lastName;
    private final String patronymicName;
    private final String phoneNumber;
}
