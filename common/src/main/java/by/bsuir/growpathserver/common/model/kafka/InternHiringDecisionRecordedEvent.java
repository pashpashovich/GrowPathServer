package by.bsuir.growpathserver.common.model.kafka;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InternHiringDecisionRecordedEvent {
    private Long internId;
    private String internEmail;
    private String internName;
    private Long programId;
    private String programTitle;
    private String decision;
    private String decisionLabel;
    private List<String> hrManagerEmails;
    private String mentorEmail;
}
