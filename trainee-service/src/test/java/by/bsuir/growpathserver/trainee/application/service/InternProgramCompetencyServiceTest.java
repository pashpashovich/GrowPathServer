package by.bsuir.growpathserver.trainee.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.dto.model.InternProgramCompetenciesResponse;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.CompetencyCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class InternProgramCompetencyServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private IprRepository iprRepository;
    @Mock
    private InternshipProgramRepository internshipProgramRepository;
    @Mock
    private CompetencyCatalogMapper competencyCatalogMapper;

    @InjectMocks
    private InternProgramCompetencyService service;

    @Test
    void getProgramCompetencies_usesExplicitProgramId() {
        UserEntity intern = new UserEntity();
        intern.setId(5L);
        intern.setRole(UserRole.INTERN);
        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(intern));
        when(iprRepository.existsByProgram_IdAndIntern_Id(3L, 5L)).thenReturn(true);

        InternshipProgramEntity program = new InternshipProgramEntity();
        program.setId(3L);
        program.setTitle("Backend");
        CompetencyEntity competency = new CompetencyEntity();
        competency.setId(1L);
        competency.setName("Java");
        program.setCompetencies(Set.of(competency));
        when(internshipProgramRepository.findWithCollectionsById(3L)).thenReturn(java.util.Optional.of(program));

        CompetencyRef ref = new CompetencyRef();
        ref.setId(1L);
        ref.setName("Java");
        when(competencyCatalogMapper.toCompetencyRef(competency)).thenReturn(ref);

        InternProgramCompetenciesResponse response = service.getProgramCompetencies(5L, 3L);

        assertThat(response.getProgramId()).isEqualTo(3L);
        assertThat(response.getProgramTitle()).isEqualTo("Backend");
        assertThat(response.getCompetencies()).hasSize(1);
    }

    @Test
    void getProgramCompetencies_resolvesActiveIprWhenProgramIdOmitted() {
        UserEntity intern = new UserEntity();
        intern.setId(5L);
        intern.setRole(UserRole.INTERN);
        when(userRepository.findById(5L)).thenReturn(java.util.Optional.of(intern));

        InternshipProgramEntity program = new InternshipProgramEntity();
        program.setId(7L);
        program.setTitle("DevOps");
        program.setCompetencies(Set.of());

        IprEntity ipr = new IprEntity();
        ipr.setProgram(program);
        when(iprRepository.findActiveByInternId(5L)).thenReturn(java.util.Optional.of(ipr));
        when(internshipProgramRepository.findWithCollectionsById(7L)).thenReturn(java.util.Optional.of(program));

        InternProgramCompetenciesResponse response = service.getProgramCompetencies(5L, null);

        assertThat(response.getProgramId()).isEqualTo(7L);
    }
}
