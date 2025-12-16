package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;

@Repository
public interface AssessmentRepository
        extends JpaRepository<AssessmentEntity, Long>, JpaSpecificationExecutor<AssessmentEntity> {
    Optional<AssessmentEntity> findById(Long id);
}
