package by.bsuir.growpathserver.common.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationCreatedEvent {
    private String applicationId;
    private String email;
}
