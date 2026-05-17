package by.bsuir.growpathserver.trainee.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskCompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskCompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
class InternCompetencyProfileServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskCompetencyRepository taskCompetencyRepository;

    @InjectMocks
    private InternCompetencyProfileService service;

    @Test
    void buildNormalizedProfile_returnsEmptyWhenNoCompletedTasks() {
        when(taskRepository.findByAssigneeIdAndStatus(1L, TaskStatus.COMPLETED)).thenReturn(List.of());

        Map<Long, Double> profile = service.buildNormalizedProfile(1L);

        assertThat(profile).isEmpty();
    }

    @Test
    void toAchievedLevelOutOfFive_scalesNormalizedValue() {
        assertThat(service.toAchievedLevelOutOfFive(Map.of(10L, 0.6), 10L)).isEqualTo(3.0);
        assertThat(service.toAchievedLevelOutOfFive(Map.of(), 10L)).isEqualTo(0.0);
    }

    @Test
    void buildNormalizedProfile_calculatesLevelFromCompletedTasks() {
        TaskEntity completed = new TaskEntity();
        completed.setId(100L);
        completed.setRating(5);
        completed.setStatus(TaskStatus.COMPLETED);

        CompetencyEntity competency = new CompetencyEntity();
        competency.setId(7L);
        competency.setName("Java");

        TaskCompetencyEntity link = new TaskCompetencyEntity();
        link.setCompetency(competency);
        link.setTask(completed);

        when(taskRepository.findByAssigneeIdAndStatus(1L, TaskStatus.COMPLETED)).thenReturn(List.of(completed));
        when(taskRepository.findByAssigneeId(1L)).thenReturn(List.of(completed));
        when(taskCompetencyRepository.findByTaskId(100L)).thenReturn(List.of(link));

        Map<Long, Double> profile = service.buildNormalizedProfile(1L);

        assertThat(profile).containsKey(7L);
        assertThat(service.toAchievedLevelOutOfFive(profile, 7L)).isEqualTo(5.0);
    }
}
