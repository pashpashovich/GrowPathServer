package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {
    Optional<TaskEntity> findById(Long id);

    @Query("SELECT COALESCE(MAX(t.sortOrder), 0) FROM TaskEntity t WHERE t.assigneeId = :assigneeId AND t.status = :status")
    Long findMaxSortOrderByAssigneeIdAndStatus(@Param("assigneeId") Long assigneeId,
                                               @Param("status") TaskStatus status);
}
