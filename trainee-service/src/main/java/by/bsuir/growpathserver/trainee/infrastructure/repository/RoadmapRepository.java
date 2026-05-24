package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.RoadmapEntity;

@Repository
public interface RoadmapRepository extends JpaRepository<RoadmapEntity, Long>, JpaSpecificationExecutor<RoadmapEntity> {

    @EntityGraph(attributePaths = { "mentor", "interns", "program" })
    @Query("SELECT r FROM RoadmapEntity r WHERE r.id = :id")
    Optional<RoadmapEntity> findWithMentorAndInternsById(@Param("id") Long id);

    List<RoadmapEntity> findByProgramId(Long programId);

    boolean existsByProgram_IdAndMentor_Id(Long programId, Long mentorId);

    Optional<RoadmapEntity> findByProgram_IdAndMentor_Id(Long programId, Long mentorId);

    List<RoadmapEntity> findByMentorId(Long mentorId);

    @Query("SELECT DISTINCT r FROM RoadmapEntity r JOIN r.interns i WHERE i.user.id = :userId")
    List<RoadmapEntity> findByInternUserId(@Param("userId") Long userId);
}
