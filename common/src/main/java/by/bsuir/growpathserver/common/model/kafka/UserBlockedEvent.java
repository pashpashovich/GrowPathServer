package by.bsuir.growpathserver.common.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlockedEvent {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String patronymicName;
}
