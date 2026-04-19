package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.ProgramGoalDefinitionEntity;

@Repository
public interface ProgramGoalDefinitionRepository extends JpaRepository<ProgramGoalDefinitionEntity, Long> {

    List<ProgramGoalDefinitionEntity> findAllByOrderByTitleAsc();

    long countByIdIn(Collection<Long> ids);

    @Query("""
            select case when count(e) > 0 then true else false end
            from ProgramGoalDefinitionEntity e
            where lower(trim(e.title)) = lower(trim(:title))
              and ((e.description is null and :desc is null)
                   or (e.description is not null and :desc is not null and e.description = :desc))
              and (:excludeId is null or e.id <> :excludeId)
            """)
    boolean existsDuplicateTitleAndDescription(@Param("title") String title,
                                               @Param("desc") String description,
                                               @Param("excludeId") Long excludeId);
}
