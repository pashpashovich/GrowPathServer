package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.RequirementDefinitionEntity;

@Repository
public interface RequirementDefinitionRepository extends JpaRepository<RequirementDefinitionEntity, Long> {

    List<RequirementDefinitionEntity> findAllByOrderByRequirementTextAsc();

    long countByIdIn(Collection<Long> ids);
}
