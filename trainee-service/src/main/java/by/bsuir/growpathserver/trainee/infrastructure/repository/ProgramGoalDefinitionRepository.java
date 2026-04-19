package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.ProgramGoalDefinitionEntity;

@Repository
public interface ProgramGoalDefinitionRepository extends JpaRepository<ProgramGoalDefinitionEntity, Long> {

    List<ProgramGoalDefinitionEntity> findAllByOrderByTitleAsc();

    long countByIdIn(Collection<Long> ids);
}
