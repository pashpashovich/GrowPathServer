package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import by.bsuir.growpathserver.trainee.domain.entity.InternHiringDecisionEntity;

public interface InternHiringDecisionRepository extends JpaRepository<InternHiringDecisionEntity, Long> {

    @EntityGraph(attributePaths = { "intern", "program", "decidedBy" })
    Optional<InternHiringDecisionEntity> findByIntern_IdAndProgram_Id(Long internId, Long programId);

    @EntityGraph(attributePaths = { "intern", "program" })
    List<InternHiringDecisionEntity> findByDecidedAtBetween(LocalDateTime from, LocalDateTime to);
}
