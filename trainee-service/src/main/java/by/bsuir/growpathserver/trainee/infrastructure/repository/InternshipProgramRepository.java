package by.bsuir.growpathserver.trainee.infrastructure.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;

@Repository
public interface InternshipProgramRepository
        extends JpaRepository<InternshipProgramEntity, Long>, JpaSpecificationExecutor<InternshipProgramEntity> {
}
