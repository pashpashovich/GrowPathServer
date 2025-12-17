package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.application.query.GetInternRatingQuery;
import by.bsuir.growpathserver.trainee.application.query.GetRatingsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.Rating;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class RatingServiceImplTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private UserEntity internEntity;
    private UserEntity mentorEntity;
    private AssessmentEntity latestAssessment;
    private AssessmentEntity previousAssessment;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        internEntity = new UserEntity();
        internEntity.setId(10L);
        internEntity.setEmail("intern@example.com");
        internEntity.setName("Intern Name");
        internEntity.setRole(UserRole.INTERN);
        internEntity.setStatus(UserStatus.ACTIVE);

        mentorEntity = new UserEntity();
        mentorEntity.setId(20L);
        mentorEntity.setEmail("mentor@example.com");
        mentorEntity.setName("Mentor Name");
        mentorEntity.setRole(UserRole.MENTOR);
        mentorEntity.setStatus(UserStatus.ACTIVE);

        latestAssessment = new AssessmentEntity();
        latestAssessment.setId(1L);
        latestAssessment.setInternId(10L);
        latestAssessment.setMentorId(20L);
        latestAssessment.setInternshipId(30L);
        latestAssessment.setOverallRating(4.5);
        latestAssessment.setQualityRating(4.0);
        latestAssessment.setSpeedRating(5.0);
        latestAssessment.setCommunicationRating(4.5);
        latestAssessment.setUpdatedAt(now);

        previousAssessment = new AssessmentEntity();
        previousAssessment.setId(2L);
        previousAssessment.setInternId(10L);
        previousAssessment.setMentorId(20L);
        previousAssessment.setInternshipId(30L);
        previousAssessment.setOverallRating(4.0);
        previousAssessment.setUpdatedAt(now.minusDays(30));
    }

    @Test
    void shouldGetInternRatingSuccessfully() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(10L);

        TaskEntity completedTask1 = new TaskEntity();
        completedTask1.setId(1L);
        completedTask1.setAssigneeId(10L);
        completedTask1.setStatus(TaskStatus.COMPLETED);
        completedTask1.setDueDate(now.minusDays(1));
        completedTask1.setCompletedAt(now.minusDays(1));
        completedTask1.setTakenAt(now.minusDays(2));

        TaskEntity completedTask2 = new TaskEntity();
        completedTask2.setId(2L);
        completedTask2.setAssigneeId(10L);
        completedTask2.setStatus(TaskStatus.COMPLETED);
        completedTask2.setDueDate(now.minusDays(1));
        completedTask2.setCompletedAt(now.minusDays(2));
        completedTask2.setTakenAt(now.minusDays(3));

        TaskEntity pendingTask = new TaskEntity();
        pendingTask.setId(3L);
        pendingTask.setAssigneeId(10L);
        pendingTask.setStatus(TaskStatus.PENDING);

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(assessmentRepository.findAll()).thenReturn(List.of(latestAssessment, previousAssessment));
        when(taskRepository.findAll()).thenReturn(List.of(completedTask1, completedTask2, pendingTask));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));

        // When
        Rating result = ratingService.getInternRating(query);

        // Then
        assertNotNull(result);
        assertEquals(10L, result.getInternId());
        assertEquals("Intern Name", result.getInternName());
        assertEquals("Mentor Name", result.getMentorName());
        assertEquals(4.5, result.getOverallRating());
        assertEquals(4.0, result.getQualityRating());
        assertEquals(5.0, result.getSpeedRating());
        assertEquals(4.5, result.getCommunicationRating());
        assertEquals(2, result.getTasksCompleted());
        assertEquals(2, result.getTasksOnTime());
        assertEquals("up", result.getTrend());
        assertEquals(4.0, result.getPreviousRating());
        verify(userRepository).findById(10L);
        verify(assessmentRepository, times(2)).findAll(); // Called twice: for latest and previous assessment
        verify(taskRepository).findAll();
    }

    @Test
    void shouldCalculateTrendAsDown() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(10L);
        latestAssessment.setOverallRating(3.5);

        TaskEntity completedTask = new TaskEntity();
        completedTask.setId(1L);
        completedTask.setAssigneeId(10L);
        completedTask.setStatus(TaskStatus.COMPLETED);
        completedTask.setDueDate(now.minusDays(1));
        completedTask.setCompletedAt(now.minusDays(1));
        completedTask.setTakenAt(now.minusDays(2));

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(assessmentRepository.findAll()).thenReturn(List.of(latestAssessment, previousAssessment));
        when(taskRepository.findAll()).thenReturn(List.of(completedTask));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));

        // When
        Rating result = ratingService.getInternRating(query);

        // Then
        assertNotNull(result);
        assertEquals("down", result.getTrend());
        assertEquals(4.0, result.getPreviousRating());
    }

    @Test
    void shouldCalculateTrendAsStable() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(10L);
        latestAssessment.setOverallRating(4.05);

        TaskEntity completedTask = new TaskEntity();
        completedTask.setId(1L);
        completedTask.setAssigneeId(10L);
        completedTask.setStatus(TaskStatus.COMPLETED);
        completedTask.setDueDate(now.minusDays(1));
        completedTask.setCompletedAt(now.minusDays(1));
        completedTask.setTakenAt(now.minusDays(2));

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(assessmentRepository.findAll()).thenReturn(List.of(latestAssessment, previousAssessment));
        when(taskRepository.findAll()).thenReturn(List.of(completedTask));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));

        // When
        Rating result = ratingService.getInternRating(query);

        // Then
        assertNotNull(result);
        assertEquals("stable", result.getTrend());
    }

    @Test
    void shouldCalculateTasksOnTime() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(10L);

        TaskEntity onTimeTask = new TaskEntity();
        onTimeTask.setId(1L);
        onTimeTask.setAssigneeId(10L);
        onTimeTask.setStatus(TaskStatus.COMPLETED);
        onTimeTask.setDueDate(now);
        onTimeTask.setCompletedAt(now.minusHours(1));
        onTimeTask.setTakenAt(now.minusDays(1));

        TaskEntity lateTask = new TaskEntity();
        lateTask.setId(2L);
        lateTask.setAssigneeId(10L);
        lateTask.setStatus(TaskStatus.COMPLETED);
        lateTask.setDueDate(now.minusDays(1));
        lateTask.setCompletedAt(now);
        lateTask.setTakenAt(now.minusDays(2));

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(assessmentRepository.findAll()).thenReturn(List.of(latestAssessment));
        when(taskRepository.findAll()).thenReturn(List.of(onTimeTask, lateTask));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));

        // When
        Rating result = ratingService.getInternRating(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTasksCompleted());
        assertEquals(1, result.getTasksOnTime());
    }

    @Test
    void shouldCalculateAverageTaskTime() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(10L);

        TaskEntity task1 = new TaskEntity();
        task1.setId(1L);
        task1.setAssigneeId(10L);
        task1.setStatus(TaskStatus.COMPLETED);
        task1.setTakenAt(now.minusHours(48));
        task1.setCompletedAt(now.minusHours(24));

        TaskEntity task2 = new TaskEntity();
        task2.setId(2L);
        task2.setAssigneeId(10L);
        task2.setStatus(TaskStatus.COMPLETED);
        task2.setTakenAt(now.minusHours(72));
        task2.setCompletedAt(now.minusHours(48));

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(assessmentRepository.findAll()).thenReturn(List.of(latestAssessment));
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));

        // When
        Rating result = ratingService.getInternRating(query);

        // Then
        assertNotNull(result);
        assertEquals(24.0, result.getAverageTaskTime(), 0.1);
    }

    @Test
    void shouldThrowExceptionWhenInternNotFound() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> ratingService.getInternRating(query));
        verify(userRepository).findById(999L);
    }

    @Test
    void shouldThrowExceptionWhenNoRatingFound() {
        // Given
        GetInternRatingQuery query = new GetInternRatingQuery(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(assessmentRepository.findAll()).thenReturn(List.of());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> ratingService.getInternRating(query));
        verify(userRepository).findById(10L);
        verify(assessmentRepository).findAll();
    }

    @Test
    void shouldGetRatingsSuccessfully() {
        // Given
        GetRatingsQuery query = new GetRatingsQuery(null, null, null);

        AssessmentEntity assessment1 = new AssessmentEntity();
        assessment1.setId(1L);
        assessment1.setInternId(10L);
        assessment1.setMentorId(20L);
        assessment1.setInternshipId(30L);
        assessment1.setOverallRating(4.5);
        assessment1.setQualityRating(4.0);
        assessment1.setSpeedRating(5.0);
        assessment1.setCommunicationRating(4.5);
        assessment1.setUpdatedAt(now);

        AssessmentEntity assessment2 = new AssessmentEntity();
        assessment2.setId(2L);
        assessment2.setInternId(11L);
        assessment2.setMentorId(20L);
        assessment2.setInternshipId(30L);
        assessment2.setOverallRating(3.5);
        assessment2.setQualityRating(3.0);
        assessment2.setSpeedRating(4.0);
        assessment2.setCommunicationRating(3.5);
        assessment2.setUpdatedAt(now);

        UserEntity intern2 = new UserEntity();
        intern2.setId(11L);
        intern2.setName("Intern Two");
        intern2.setRole(UserRole.INTERN);

        when(assessmentRepository.findAll()).thenReturn(List.of(assessment1, assessment2));
        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(userRepository.findById(11L)).thenReturn(Optional.of(intern2));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of());

        // When
        List<Rating> result = ratingService.getRatings(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(assessmentRepository,
               times(3)).findAll();
    }

    @Test
    void shouldGetRatingsFilteredByInternshipId() {
        // Given
        GetRatingsQuery query = new GetRatingsQuery(30L, null, null);

        AssessmentEntity assessment1 = new AssessmentEntity();
        assessment1.setId(1L);
        assessment1.setInternId(10L);
        assessment1.setMentorId(20L);
        assessment1.setInternshipId(30L);
        assessment1.setOverallRating(4.5);
        assessment1.setQualityRating(4.0);
        assessment1.setSpeedRating(5.0);
        assessment1.setCommunicationRating(4.5);
        assessment1.setUpdatedAt(now);

        AssessmentEntity assessment2 = new AssessmentEntity();
        assessment2.setId(2L);
        assessment2.setInternId(11L);
        assessment2.setMentorId(20L);
        assessment2.setInternshipId(40L);
        assessment2.setOverallRating(3.5);
        assessment2.setQualityRating(3.0);
        assessment2.setSpeedRating(4.0);
        assessment2.setCommunicationRating(3.5);
        assessment2.setUpdatedAt(now);

        when(assessmentRepository.findAll()).thenReturn(List.of(assessment1, assessment2));
        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of());

        // When
        List<Rating> result = ratingService.getRatings(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getInternId());
    }

    @Test
    void shouldSortRatingsByOverallRatingAscending() {
        // Given
        GetRatingsQuery query = new GetRatingsQuery(null, "overallRating", "asc");

        AssessmentEntity assessment1 = new AssessmentEntity();
        assessment1.setId(1L);
        assessment1.setInternId(10L);
        assessment1.setMentorId(20L);
        assessment1.setInternshipId(30L);
        assessment1.setOverallRating(4.5);
        assessment1.setQualityRating(4.0);
        assessment1.setSpeedRating(5.0);
        assessment1.setCommunicationRating(4.5);
        assessment1.setUpdatedAt(now);

        AssessmentEntity assessment2 = new AssessmentEntity();
        assessment2.setId(2L);
        assessment2.setInternId(11L);
        assessment2.setMentorId(20L);
        assessment2.setInternshipId(30L);
        assessment2.setOverallRating(3.5);
        assessment2.setQualityRating(3.0);
        assessment2.setSpeedRating(4.0);
        assessment2.setCommunicationRating(3.5);
        assessment2.setUpdatedAt(now);

        UserEntity intern2 = new UserEntity();
        intern2.setId(11L);
        intern2.setName("Intern Two");
        intern2.setRole(UserRole.INTERN);

        when(assessmentRepository.findAll()).thenReturn(List.of(assessment1, assessment2));
        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity));
        when(userRepository.findById(11L)).thenReturn(Optional.of(intern2));
        when(userRepository.findById(20L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of());

        // When
        List<Rating> result = ratingService.getRatings(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(4.5, result.get(0).getOverallRating());
    }

}
