package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.SelectionStageDefinitionEntity;

@Repository
public interface SelectionStageDefinitionRepository extends JpaRepository<SelectionStageDefinitionEntity, Long> {

    List<SelectionStageDefinitionEntity> findAllByOrderByNameAsc();

    long countByIdIn(Collection<Long> ids);
}
