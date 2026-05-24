package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramParticipantEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.ProgramParticipantRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramParticipantRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipProgramParticipantService {

    private final InternshipProgramRepository internshipProgramRepository;
    private final InternshipProgramParticipantRepository participantRepository;
    private final ProgramRoadmapTemplateService programRoadmapTemplateService;
    private final UserRepository userRepository;
    private final IprRepository iprRepository;

    @Transactional(readOnly = true)
    public List<InternshipProgramParticipantEntity> listMentors(Long programId) {
        requireProgram(programId);
        return participantRepository.findByProgramIdAndRole(programId, ProgramParticipantRole.MENTOR);
    }

    @Transactional(readOnly = true)
    public List<InternshipProgramParticipantEntity> listInterns(Long programId, Long mentorId) {
        requireProgram(programId);
        if (Objects.nonNull(mentorId)) {
            if (!isMentorOnProgram(programId, mentorId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                  "Mentor is not assigned to this internship program");
            }
            return participantRepository.findByProgramIdAndRoleAndMentor_Id(
                    programId, ProgramParticipantRole.INTERN, mentorId);
        }
        return participantRepository.findByProgramIdAndRole(programId, ProgramParticipantRole.INTERN);
    }

    @Transactional
    public InternshipProgramParticipantEntity assignMentor(Long programId, Long userId) {
        InternshipProgramEntity program = requireProgram(programId);
        UserEntity user = requireUser(userId);
        if (user.getRole() != UserRole.MENTOR) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not a mentor");
        }
        if (participantRepository.existsByProgramIdAndUserId(programId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already assigned to the program");
        }
        InternshipProgramParticipantEntity participant = new InternshipProgramParticipantEntity();
        participant.setProgram(program);
        participant.setUser(user);
        participant.setRole(ProgramParticipantRole.MENTOR);
        InternshipProgramParticipantEntity saved = participantRepository.save(participant);
        programRoadmapTemplateService.createEmptyTemplateForMentor(program, user);
        return saved;
    }

    @Transactional
    public void unassignMentor(Long programId, Long userId) {
        requireProgram(programId);
        InternshipProgramParticipantEntity participant = requireParticipant(programId, userId,
                                                                            ProgramParticipantRole.MENTOR);
        long supervisedInterns = participantRepository.countByProgramIdAndRoleAndMentorId(
                programId, ProgramParticipantRole.INTERN, userId);
        if (supervisedInterns > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              "Cannot remove mentor while interns are assigned to them on this program");
        }
        programRoadmapTemplateService.removeEmptyTemplateForMentor(programId, userId);
        participantRepository.delete(participant);
    }

    @Transactional
    public InternshipProgramParticipantEntity assignIntern(Long programId, Long userId, Long mentorId) {
        InternshipProgramEntity program = requireProgram(programId);
        UserEntity intern = requireUser(userId);
        if (intern.getRole() != UserRole.INTERN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is not an intern");
        }
        if (participantRepository.existsByProgramIdAndUserId(programId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Intern is already assigned to the program");
        }
        long internCount = participantRepository.countByProgramIdAndRole(programId, ProgramParticipantRole.INTERN);
        if (internCount >= program.getMaxPlaces()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program has no available intern places");
        }
        UserEntity mentor = requireMentorOnProgram(programId, mentorId);

        InternshipProgramParticipantEntity participant = new InternshipProgramParticipantEntity();
        participant.setProgram(program);
        participant.setUser(intern);
        participant.setRole(ProgramParticipantRole.INTERN);
        participant.setMentor(mentor);
        return participantRepository.save(participant);
    }

    @Transactional
    public void unassignIntern(Long programId, Long userId) {
        requireProgram(programId);
        InternshipProgramParticipantEntity participant = requireParticipant(programId, userId,
                                                                            ProgramParticipantRole.INTERN);
        if (iprRepository.existsByProgram_IdAndIntern_Id(programId, userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              "Cannot remove intern from program while an IPR exists");
        }
        participantRepository.delete(participant);
    }

    @Transactional(readOnly = true)
    public UserEntity requireMentorOnProgram(Long programId, Long mentorUserId) {
        if (!participantRepository.existsByProgramIdAndUserIdAndRole(
                programId, mentorUserId, ProgramParticipantRole.MENTOR)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Mentor is not assigned to this internship program");
        }
        return userRepository.findById(mentorUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor not found"));
    }

    @Transactional(readOnly = true)
    public void requireInternOnProgramWithMentor(Long programId, Long internUserId, Long mentorUserId) {
        InternshipProgramParticipantEntity internParticipant = participantRepository
                .findByProgramIdAndUserId(programId, internUserId)
                .filter(p -> p.getRole() == ProgramParticipantRole.INTERN)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                               "Intern is not assigned to this internship program"));
        if (Objects.isNull(internParticipant.getMentor())
                || !Objects.equals(internParticipant.getMentor().getId(), mentorUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Intern is not supervised by the selected mentor on this program");
        }
    }

    @Transactional(readOnly = true)
    public boolean isMentorOnProgram(Long programId, Long mentorUserId) {
        return participantRepository.existsByProgramIdAndUserIdAndRole(
                programId, mentorUserId, ProgramParticipantRole.MENTOR);
    }

    private InternshipProgramEntity requireProgram(Long programId) {
        return internshipProgramRepository.findById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Program not found"));
    }

    private UserEntity requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "User not found"));
    }

    private InternshipProgramParticipantEntity requireParticipant(Long programId,
                                                                  Long userId,
                                                                  ProgramParticipantRole role) {
        return participantRepository.findByProgramIdAndUserId(programId, userId)
                .filter(p -> p.getRole() == role)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Participant not found"));
    }
}
