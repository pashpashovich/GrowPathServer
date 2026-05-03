package by.bsuir.growpathserver.trainee.infrastructure.repository;

import java.time.LocalDateTime;
import java.util.List;
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

    // Statistics queries
    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.assigneeId = :assigneeId AND t.status = :status")
    Long countByAssigneeIdAndStatus(@Param("assigneeId") Long assigneeId, @Param("status") TaskStatus status);

    @Query("SELECT COUNT(t) FROM TaskEntity t WHERE t.assigneeId = :assigneeId AND t.status = 'COMPLETED'")
    Long countCompletedTasksByAssigneeId(@Param("assigneeId") Long assigneeId);

    @Query("SELECT COALESCE(AVG(t.rating), 0.0) FROM TaskEntity t WHERE t.assigneeId = :assigneeId AND t.rating IS NOT NULL")
    Double getAverageRatingByAssigneeId(@Param("assigneeId") Long assigneeId);

    @Query("SELECT t FROM TaskEntity t WHERE t.assigneeId = :assigneeId")
    List<TaskEntity> findByAssigneeId(@Param("assigneeId") Long assigneeId);

    @Query("SELECT t FROM TaskEntity t WHERE t.mentorId = :mentorId")
    List<TaskEntity> findByMentorId(@Param("mentorId") Long mentorId);

    @Query("SELECT t FROM TaskEntity t WHERE t.dueDate BETWEEN :startDate AND :endDate " +
           "AND t.status NOT IN ('COMPLETED', 'CANCELLED')")
    List<TaskEntity> findUpcomingDeadlines(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TaskEntity t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<TaskEntity> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM TaskEntity t WHERE t.internshipId = :internshipId")
    List<TaskEntity> findByInternshipId(@Param("internshipId") Long internshipId);

    @Query("SELECT t FROM TaskEntity t WHERE t.assigneeId = :assigneeId AND t.status = :status")
    List<TaskEntity> findByAssigneeIdAndStatus(@Param("assigneeId") Long assigneeId, @Param("status") TaskStatus status);
}
