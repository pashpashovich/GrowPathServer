package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramParticipantEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.ProgramParticipantRole;

public interface InternshipProgramParticipantRepository
        extends JpaRepository<InternshipProgramParticipantEntity, Long> {

    @EntityGraph(attributePaths = { "user", "mentor", "program" })
    List<InternshipProgramParticipantEntity> findByProgramIdAndRole(Long programId, ProgramParticipantRole role);

    @EntityGraph(attributePaths = { "user", "mentor", "program" })
    List<InternshipProgramParticipantEntity> findByProgramIdAndRoleAndMentor_Id(
            Long programId,
            ProgramParticipantRole role,
            Long mentorId);

    @EntityGraph(attributePaths = { "user", "mentor", "program" })
    Optional<InternshipProgramParticipantEntity> findByProgramIdAndUserId(Long programId, Long userId);

    boolean existsByProgramIdAndUserId(Long programId, Long userId);

    long countByProgramIdAndRole(Long programId, ProgramParticipantRole role);

    long countByProgramIdAndRoleAndMentorId(Long programId, ProgramParticipantRole role, Long mentorId);

    boolean existsByProgramIdAndUserIdAndRole(Long programId, Long userId, ProgramParticipantRole role);

    @EntityGraph(attributePaths = { "program", "user" })
    List<InternshipProgramParticipantEntity> findByUserIdAndRole(Long userId, ProgramParticipantRole role);
}
