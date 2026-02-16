package by.bsuir.growpathserver.common.model;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {
    private String eventType;
    private LocalDateTime timestamp;
    private Map<String, Object> data;
    private String userId;
}
