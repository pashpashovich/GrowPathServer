package by.bsuir.growpathserver.common.model.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskCompletedEvent {
    private String taskName;
    private String email;
}
