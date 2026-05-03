package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.TaskCompetencyEntity;

@Repository
public interface TaskCompetencyRepository extends JpaRepository<TaskCompetencyEntity, Long> {
    List<TaskCompetencyEntity> findAllByTaskId(Long taskId);

    List<TaskCompetencyEntity> findByTaskId(Long taskId);
}
