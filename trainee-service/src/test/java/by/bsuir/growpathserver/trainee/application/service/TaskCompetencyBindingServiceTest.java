package by.bsuir.growpathserver.trainee.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.CompetencyCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskCompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class TaskCompetencyBindingServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private TaskCompetencyRepository taskCompetencyRepository;
    @Mock
    private InternshipProgramRepository internshipProgramRepository;
    @Mock
    private CompetencyRepository competencyRepository;
    @Mock
    private CompetencyCatalogMapper competencyCatalogMapper;

    @InjectMocks
    private TaskCompetencyBindingService service;

    @Test
    void replaceTaskCompetencies_skipsWhenNull() {
        service.replaceTaskCompetencies(1L, 2L, null);
        verify(taskRepository, never()).existsById(any());
    }

    @Test
    void replaceTaskCompetencies_rejectsUnknownCompetency() {
        when(taskRepository.existsById(1L)).thenReturn(true);
        InternshipProgramEntity program = new InternshipProgramEntity();
        CompetencyEntity allowed = new CompetencyEntity();
        allowed.setId(10L);
        program.setCompetencies(Set.of(allowed));
        when(internshipProgramRepository.findWithCollectionsById(2L)).thenReturn(java.util.Optional.of(program));

        assertThatThrownBy(() -> service.replaceTaskCompetencies(1L, 2L, List.of(99L)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
