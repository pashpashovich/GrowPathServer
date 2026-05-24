package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;

@Repository
public interface IprRepository extends JpaRepository<IprEntity, Long>, JpaSpecificationExecutor<IprEntity> {

    @EntityGraph(attributePaths = { "program", "roadmapTemplate", "intern", "mentor" })
    @Query("SELECT i FROM IprEntity i WHERE i.id = :id")
    Optional<IprEntity> findLoadedById(@Param("id") Long id);

    @EntityGraph(attributePaths = { "program", "roadmapTemplate", "intern", "mentor" })
    List<IprEntity> findByMentorId(Long mentorId);

    @EntityGraph(attributePaths = { "program", "roadmapTemplate", "intern", "mentor" })
    List<IprEntity> findByInternId(Long internId);

    @EntityGraph(attributePaths = { "program", "roadmapTemplate", "intern", "mentor", "stages" })
    @Query("SELECT i FROM IprEntity i WHERE i.intern.id = :internId AND i.status = 'ACTIVE'")
    Optional<IprEntity> findActiveByInternId(@Param("internId") Long internId);

    boolean existsByProgram_IdAndIntern_Id(Long programId, Long internId);
}
