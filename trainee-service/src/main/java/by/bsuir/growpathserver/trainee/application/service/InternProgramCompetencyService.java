package by.bsuir.growpathserver.trainee.application.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternProgramCompetencyService {

    private final UserRepository userRepository;
    private final IprRepository iprRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final CompetencyCatalogMapper competencyCatalogMapper;

    @Transactional(readOnly = true)
    public InternProgramCompetenciesResponse getProgramCompetencies(Long internId, Long programId) {
        UserEntity intern = userRepository.findById(internId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Intern not found"));
        if (intern.getRole() != UserRole.INTERN) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Intern not found");
        }

        Long resolvedProgramId = resolveProgramId(internId, programId);
        InternshipProgramEntity program = internshipProgramRepository.findWithCollectionsById(resolvedProgramId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Internship program not found"));

        List<Object> competencies = new ArrayList<>();
        program.getCompetencies().stream()
                .sorted(Comparator.comparing(CompetencyEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(competencyCatalogMapper::toCompetencyRef)
                .forEach(competencies::add);

        InternProgramCompetenciesResponse response = new InternProgramCompetenciesResponse();
        response.setInternId(internId);
        response.setProgramId(resolvedProgramId);
        response.setProgramTitle(program.getTitle());
        response.setCompetencies(competencies);
        return response;
    }

    private Long resolveProgramId(Long internId, Long programId) {
        if (Objects.nonNull(programId)) {
            if (!iprRepository.existsByProgram_IdAndIntern_Id(programId, internId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                  "Intern is not enrolled in the specified internship program");
            }
            return programId;
        }
        return resolveIpr(internId).getProgram().getId();
    }

    private IprEntity resolveIpr(Long internId) {
        return iprRepository.findActiveByInternId(internId)
                .or(() -> iprRepository.findByInternId(internId).stream()
                        .max(Comparator.comparing(IprEntity::getEndDate)
                                     .thenComparing(IprEntity::getId)))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                                                               "Intern has no individual development plan"));
    }
}
