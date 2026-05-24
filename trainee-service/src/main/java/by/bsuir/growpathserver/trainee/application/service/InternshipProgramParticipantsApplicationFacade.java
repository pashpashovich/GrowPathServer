package by.bsuir.growpathserver.trainee.application.service;

import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.AssignProgramInternRequest;
import by.bsuir.growpathserver.dto.model.AssignProgramMentorRequest;
import by.bsuir.growpathserver.dto.model.ProgramParticipantListResponse;
import by.bsuir.growpathserver.dto.model.ProgramParticipantResponse;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramParticipantEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.ProgramParticipantMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipProgramParticipantsApplicationFacade {

    private final InternshipProgramParticipantService participantService;
    private final ProgramParticipantMapper programParticipantMapper;
    private final MentorQueryScopeResolver mentorQueryScopeResolver;

    public ProgramParticipantListResponse listProgramMentors(String programId) {
        return programParticipantMapper.toProgramParticipantListResponse(
                participantService.listMentors(parseProgramId(programId)));
    }

    public ProgramParticipantListResponse listProgramInterns(String programId, Long mentorId) {
        Long effectiveMentorId = mentorQueryScopeResolver.resolveOptionalMentorFilter(mentorId);
        return programParticipantMapper.toProgramParticipantListResponse(
                participantService.listInterns(parseProgramId(programId), effectiveMentorId));
    }

    public ProgramParticipantResponse assignProgramMentor(String programId, AssignProgramMentorRequest request) {
        InternshipProgramParticipantEntity entity = participantService.assignMentor(
                parseProgramId(programId), request.getUserId());
        return programParticipantMapper.toProgramParticipantResponse(entity);
    }

    public void unassignProgramMentor(String programId, String userId) {
        participantService.unassignMentor(parseProgramId(programId), Long.parseLong(userId));
    }

    public ProgramParticipantResponse assignProgramIntern(String programId, AssignProgramInternRequest request) {
        InternshipProgramParticipantEntity entity = participantService.assignIntern(
                parseProgramId(programId), request.getUserId(), request.getMentorId());
        return programParticipantMapper.toProgramParticipantResponse(entity);
    }

    public void unassignProgramIntern(String programId, String userId) {
        participantService.unassignIntern(parseProgramId(programId), Long.parseLong(userId));
    }

    private static long parseProgramId(String programId) {
        return Long.parseLong(programId);
    }
}
