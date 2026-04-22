package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.TaskArtifactEntity;

@Repository
public interface TaskArtifactRepository extends JpaRepository<TaskArtifactEntity, Long> {
    List<TaskArtifactEntity> findAllByTaskId(Long taskId);
}
