package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.SelectionStageDefinitionEntity;

@Repository
public interface SelectionStageDefinitionRepository extends JpaRepository<SelectionStageDefinitionEntity, Long> {

    List<SelectionStageDefinitionEntity> findAllByOrderByNameAsc();

    long countByIdIn(Collection<Long> ids);

    @Query("""
            select case when count(e) > 0 then true else false end
            from SelectionStageDefinitionEntity e
            where trim(e.name) = trim(:name)
              and ((e.description is null and :desc is null)
                   or (e.description is not null and :desc is not null and e.description = :desc))
              and e.active = :active
              and (:excludeId is null or e.id <> :excludeId)
            """)
    boolean existsDuplicateNameDescriptionAndActive(@Param("name") String name,
                                                    @Param("desc") String description,
                                                    @Param("active") boolean active,
                                                    @Param("excludeId") Long excludeId);
}
