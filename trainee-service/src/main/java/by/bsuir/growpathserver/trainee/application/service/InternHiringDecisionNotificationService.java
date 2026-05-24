package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.common.model.kafka.InternHiringDecisionRecordedEvent;
import by.bsuir.growpathserver.trainee.domain.entity.InternHiringDecisionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.HiringDecisionType;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternHiringDecisionNotificationService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserRepository userRepository;
    private final IprRepository iprRepository;

    @Value("${kafka.topic.hiring-decision-recorded:HIRING_DECISION_RECORDED}")
    private String topicHiringDecisionRecorded;

    @Async
    public void notifyDecisionRecorded(InternHiringDecisionEntity decision) {
        UserEntity intern = decision.getIntern();
        HiringDecisionType decisionType = decision.getDecision();
        Long programId = decision.getProgram().getId();

        List<String> hrEmails = userRepository.findByRole(UserRole.HR).stream()
                .filter(user -> user.getStatus() == UserStatus.ACTIVE)
                .map(UserEntity::getEmail)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        String mentorEmail = null;
        if (decisionType == HiringDecisionType.ADDITIONAL_ASSESSMENT) {
            mentorEmail = iprRepository.findByInternId(intern.getId()).stream()
                    .filter(ipr -> Objects.equals(ipr.getProgram().getId(), programId))
                    .map(IprEntity::getMentor)
                    .filter(Objects::nonNull)
                    .map(UserEntity::getEmail)
                    .findFirst()
                    .orElse(null);
        }

        InternHiringDecisionRecordedEvent event = new InternHiringDecisionRecordedEvent(
                intern.getId(),
                intern.getEmail(),
                formatName(intern),
                programId,
                decision.getProgram().getTitle(),
                decisionType.toApiValue(),
                decisionType.toDisplayLabel(),
                hrEmails,
                mentorEmail);

        kafkaTemplate.send(topicHiringDecisionRecorded, intern.getId().toString(), event)
                .whenComplete((result, throwable) -> {
                    if (Objects.nonNull(throwable)) {
                        log.error("Failed to publish hiring decision notification for internId={}",
                                intern.getId(), throwable);
                    }
                });
    }

    private static String formatName(UserEntity user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
