package by.bsuir.growpathserver.trainee.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import by.bsuir.growpathserver.trainee.application.query.GetMentorByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorInternsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class MentorServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @InjectMocks
    private MentorServiceImpl mentorService;

    private UserEntity mentorEntity;
    private UserEntity internEntity1;
    private UserEntity internEntity2;

    @BeforeEach
    void setUp() {
        mentorEntity = new UserEntity();
        mentorEntity.setId(1L);
        mentorEntity.setEmail("mentor@example.com");
        mentorEntity.setName("Mentor Name");
        mentorEntity.setRole(UserRole.MENTOR);
        mentorEntity.setStatus(UserStatus.ACTIVE);

        internEntity1 = new UserEntity();
        internEntity1.setId(10L);
        internEntity1.setEmail("intern1@example.com");
        internEntity1.setName("Intern One");
        internEntity1.setRole(UserRole.INTERN);
        internEntity1.setStatus(UserStatus.ACTIVE);

        internEntity2 = new UserEntity();
        internEntity2.setId(11L);
        internEntity2.setEmail("intern2@example.com");
        internEntity2.setName("Intern Two");
        internEntity2.setRole(UserRole.INTERN);
        internEntity2.setStatus(UserStatus.ACTIVE);
    }

    @Test
    void shouldGetMentorsSuccessfully() {
        // Given
        GetMentorsQuery query = new GetMentorsQuery(1, 10, null);
        Page<UserEntity> mentorPage = new PageImpl<>(List.of(mentorEntity));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mentorPage);

        // When
        Page<User> result = mentorService.getMentors(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("Mentor Name", result.getContent().get(0).getName());
        assertEquals(UserRole.MENTOR, result.getContent().get(0).getRole());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldGetMentorsWithSearch() {
        // Given
        GetMentorsQuery query = new GetMentorsQuery(1, 10, "mentor");
        Page<UserEntity> mentorPage = new PageImpl<>(List.of(mentorEntity));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mentorPage);

        // When
        Page<User> result = mentorService.getMentors(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(userRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    void shouldGetMentorsWithDefaultPagination() {
        // Given
        GetMentorsQuery query = new GetMentorsQuery(null, null, null);
        Page<UserEntity> mentorPage = new PageImpl<>(List.of(mentorEntity));

        when(userRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(mentorPage);

        // When
        Page<User> result = mentorService.getMentors(query);

        // Then
        assertNotNull(result);
        verify(userRepository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    void shouldGetMentorByIdSuccessfully() {
        // Given
        GetMentorByIdQuery query = new GetMentorByIdQuery(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentorEntity));

        // When
        User result = mentorService.getMentorById(query);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Mentor Name", result.getName());
        assertEquals(UserRole.MENTOR, result.getRole());
        verify(userRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenMentorNotFound() {
        // Given
        GetMentorByIdQuery query = new GetMentorByIdQuery(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> mentorService.getMentorById(query));
        verify(userRepository).findById(999L);
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotMentor() {
        // Given
        GetMentorByIdQuery query = new GetMentorByIdQuery(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> mentorService.getMentorById(query));
        verify(userRepository).findById(10L);
    }

    @Test
    void shouldGetMentorInternsSuccessfully() {
        // Given
        GetMentorInternsQuery query = new GetMentorInternsQuery(1L);

        TaskEntity task1 = new TaskEntity();
        task1.setId(1L);
        task1.setMentorId(1L);
        task1.setAssigneeId(10L);

        TaskEntity task2 = new TaskEntity();
        task2.setId(2L);
        task2.setMentorId(1L);
        task2.setAssigneeId(11L);

        AssessmentEntity assessment = new AssessmentEntity();
        assessment.setId(1L);
        assessment.setMentorId(1L);
        assessment.setInternId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of(task1, task2));
        when(assessmentRepository.findAll()).thenReturn(List.of(assessment));
        when(userRepository.findAll()).thenReturn(List.of(internEntity1, internEntity2));

        // When
        List<User> result = mentorService.getMentorInterns(query);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        Set<Long> internIds = result.stream().map(User::getId).collect(Collectors.toSet());
        assertEquals(Set.of(10L, 11L), internIds);
        verify(userRepository).findById(1L);
        verify(taskRepository).findAll();
        verify(assessmentRepository).findAll();
        verify(userRepository).findAll();
    }

    @Test
    void shouldGetMentorInternsFromTasksOnly() {
        // Given
        GetMentorInternsQuery query = new GetMentorInternsQuery(1L);

        TaskEntity task1 = new TaskEntity();
        task1.setId(1L);
        task1.setMentorId(1L);
        task1.setAssigneeId(10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of(task1));
        when(assessmentRepository.findAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(internEntity1, internEntity2));

        // When
        List<User> result = mentorService.getMentorInterns(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        verify(userRepository).findById(1L);
        verify(taskRepository).findAll();
        verify(assessmentRepository).findAll();
        verify(userRepository).findAll();
    }

    @Test
    void shouldGetMentorInternsFromAssessmentsOnly() {
        // Given
        GetMentorInternsQuery query = new GetMentorInternsQuery(1L);

        AssessmentEntity assessment = new AssessmentEntity();
        assessment.setId(1L);
        assessment.setMentorId(1L);
        assessment.setInternId(11L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of());
        when(assessmentRepository.findAll()).thenReturn(List.of(assessment));
        when(userRepository.findAll()).thenReturn(List.of(internEntity1, internEntity2));

        // When
        List<User> result = mentorService.getMentorInterns(query);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(11L, result.get(0).getId());
        verify(userRepository).findById(1L);
        verify(taskRepository).findAll();
        verify(assessmentRepository).findAll();
        verify(userRepository).findAll();
    }

    @Test
    void shouldThrowExceptionWhenMentorNotFoundForInterns() {
        // Given
        GetMentorInternsQuery query = new GetMentorInternsQuery(999L);

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NoSuchElementException.class, () -> mentorService.getMentorInterns(query));
        verify(userRepository).findById(999L);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void shouldThrowExceptionWhenUserIsNotMentorForInterns() {
        // Given
        GetMentorInternsQuery query = new GetMentorInternsQuery(10L);

        when(userRepository.findById(10L)).thenReturn(Optional.of(internEntity1));

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> mentorService.getMentorInterns(query));
        verify(userRepository).findById(10L);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenMentorHasNoInterns() {
        // Given
        GetMentorInternsQuery query = new GetMentorInternsQuery(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(mentorEntity));
        when(taskRepository.findAll()).thenReturn(List.of());
        when(assessmentRepository.findAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of(internEntity1, internEntity2));

        // When
        List<User> result = mentorService.getMentorInterns(query);

        // Then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository).findById(1L);
        verify(taskRepository).findAll();
        verify(assessmentRepository).findAll();
        verify(userRepository).findAll();
    }
}

