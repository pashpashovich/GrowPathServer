package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;

@Repository
public interface CompetencyRepository extends JpaRepository<CompetencyEntity, Long> {

    List<CompetencyEntity> findAllByOrderByNameAsc();

    long countByIdIn(Collection<Long> ids);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
