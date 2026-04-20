package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.RoadmapStageEntity;

@Repository
public interface RoadmapStageRepository extends JpaRepository<RoadmapStageEntity, Long> {

    Optional<RoadmapStageEntity> findByIdAndRoadmapId(Long id, Long roadmapId);

    List<RoadmapStageEntity> findByRoadmapIdOrderByStageOrderAsc(Long roadmapId);
}
