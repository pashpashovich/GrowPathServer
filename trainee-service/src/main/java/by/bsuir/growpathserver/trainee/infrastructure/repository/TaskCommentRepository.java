package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.TaskCommentEntity;

@Repository
public interface TaskCommentRepository extends JpaRepository<TaskCommentEntity, Long> {
    List<TaskCommentEntity> findAllByTaskIdOrderByCreatedAtAsc(Long taskId);
}
