package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;

@Repository
public interface InternshipProgramRepository
        extends JpaRepository<InternshipProgramEntity, Long>, JpaSpecificationExecutor<InternshipProgramEntity> {

    boolean existsByTitleIgnoreCase(String title);

    boolean existsByTitleIgnoreCaseAndIdNot(String title, Long id);

    @EntityGraph(attributePaths = {
            "competencies",
            "requirementItems",
            "goalItems",
            "selectionStageItems"
    })
    Optional<InternshipProgramEntity> findWithCollectionsById(Long id);

    @EntityGraph(attributePaths = {
            "competencies",
            "requirementItems",
            "goalItems",
            "selectionStageItems"
    })
    Page<InternshipProgramEntity> findAll(Specification<InternshipProgramEntity> spec, Pageable pageable);
}
