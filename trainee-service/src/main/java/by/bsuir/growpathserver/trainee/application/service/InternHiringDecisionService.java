package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.dto.model.HiringDecisionResponse;
import by.bsuir.growpathserver.dto.model.RecordHiringDecisionRequest;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.application.service.HiringRecommendationService.Recommendation;
import by.bsuir.growpathserver.trainee.domain.entity.InternHiringDecisionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.HiringDecisionType;
import by.bsuir.growpathserver.trainee.domain.valueobject.InternProfileStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.ProgramParticipantRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternHiringDecisionMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternHiringDecisionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramParticipantRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternHiringDecisionService {

    private final InternHiringDecisionRepository hiringDecisionRepository;
    private final UserRepository userRepository;
    private final InternshipProgramRepository programRepository;
    private final InternshipProgramParticipantRepository participantRepository;
    private final IprRepository iprRepository;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final InternHiringDecisionMapper hiringDecisionMapper;
    private final HiringRecommendationService hiringRecommendationService;
    private final InternHiringDecisionNotificationService notificationService;

    @Transactional
    public HiringDecisionResponse recordDecision(Long internId, RecordHiringDecisionRequest request) {
        Long programId = Objects.requireNonNull(request.getProgramId(), "programId is required");
        HiringDecisionType decision = HiringDecisionType.fromApiValue(request.getDecision().getValue());

        UserEntity intern = requireIntern(internId);
        InternshipProgramEntity program = programRepository.findById(programId)
                .orElseThrow(() -> new NoSuchElementException("Internship program not found: " + programId));

        verifyInternOnProgram(internId, programId);
        if (!iprRepository.existsByProgram_IdAndIntern_Id(programId, internId)) {
            throw new IllegalArgumentException(
                    "Intern has no individual development plan on this internship program");
        }

        UserEntity decidedBy = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .flatMap(userRepository::findById)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        String comment = StringUtils.trimToNull(request.getComment());
        InternHiringDecisionEntity entity = hiringDecisionRepository
                .findByIntern_IdAndProgram_Id(internId, programId)
                .orElseGet(InternHiringDecisionEntity::new);

        if (Objects.isNull(entity.getId())) {
            entity.setIntern(intern);
            entity.setProgram(program);
            entity.setDecidedAt(LocalDateTime.now());
        }
        entity.setDecision(decision);
        entity.setComment(comment);
        entity.setDecidedBy(decidedBy);
        entity.setUpdatedAt(LocalDateTime.now());

        applyInternProfileStatus(intern, decision);

        InternHiringDecisionEntity saved = hiringDecisionRepository.save(entity);
        notificationService.notifyDecisionRecorded(saved);

        Recommendation recommendation = hiringRecommendationService.recommend(internId, programId);
        return hiringDecisionMapper.toView(saved, recommendation, resolveInternProfileStatus(intern));
    }

    @Transactional(readOnly = true)
    public HiringDecisionResponse getDecisionView(Long internId, Long programId) {
        UserEntity intern = requireIntern(internId);
        programRepository.findById(programId)
                .orElseThrow(() -> new NoSuchElementException("Internship program not found: " + programId));
        verifyInternOnProgram(internId, programId);
        if (!iprRepository.existsByProgram_IdAndIntern_Id(programId, internId)) {
            throw new IllegalArgumentException(
                    "Intern has no individual development plan on this internship program");
        }

        Recommendation recommendation = hiringRecommendationService.recommend(internId, programId);
        InternProfileStatus internStatus = resolveInternProfileStatus(intern);

        return hiringDecisionRepository.findByIntern_IdAndProgram_Id(internId, programId)
                .map(entity -> hiringDecisionMapper.toView(entity, recommendation, internStatus))
                .orElseGet(() -> hiringDecisionMapper.toPreview(
                        internId,
                        programId,
                        programRepository.findById(programId).orElseThrow(),
                        recommendation,
                        internStatus));
    }

    private void applyInternProfileStatus(UserEntity intern, HiringDecisionType decision) {
        intern.setInternProfileStatus(decision.toInternProfileStatus());
        userRepository.save(intern);
    }

    private InternProfileStatus resolveInternProfileStatus(UserEntity intern) {
        if (Objects.nonNull(intern.getInternProfileStatus())) {
            return intern.getInternProfileStatus();
        }
        return InternProfileStatus.ACTIVE;
    }

    private UserEntity requireIntern(Long internId) {
        UserEntity user = userRepository.findById(internId)
                .orElseThrow(() -> new NoSuchElementException("Intern not found: " + internId));
        if (user.getRole() != UserRole.INTERN) {
            throw new NoSuchElementException("User is not an intern: " + internId);
        }
        return user;
    }

    private void verifyInternOnProgram(Long internId, Long programId) {
        if (!participantRepository.existsByProgramIdAndUserIdAndRole(
                programId, internId, ProgramParticipantRole.INTERN)) {
            throw new IllegalArgumentException("Intern is not assigned to this internship program");
        }
    }
}
