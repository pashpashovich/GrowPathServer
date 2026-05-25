package by.bsuir.growpathserver.trainee.application.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprStageEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprStageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssessmentIprStageBindingService {

    private final IprStageRepository iprStageRepository;
    private final AssessmentRepository assessmentRepository;

    public record ResolvedStageBinding(Long iprId, Long internshipId, String iprStageTitle) {
    }

    @Transactional(readOnly = true)
    public ResolvedStageBinding resolveRequired(Long iprStageId, Long internId, Long internshipId) {
        ResolvedStageBinding binding = validateStage(iprStageId, internId, internshipId);
        if (assessmentRepository.existsByIprStageId(iprStageId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                                              "Assessment already exists for this IPR stage");
        }
        return binding;
    }

    @Transactional(readOnly = true)
    public ResolvedStageBinding validateStage(Long iprStageId, Long internId, Long internshipId) {
        Objects.requireNonNull(iprStageId, "iprStageId is required");

        IprStageEntity stage = iprStageRepository.findLoadedById(iprStageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR stage not found"));

        IprEntity ipr = stage.getIpr();
        if (Objects.isNull(ipr)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR stage is not linked to an IPR");
        }
        if (Objects.nonNull(internId)
                && Objects.nonNull(ipr.getIntern())
                && !Objects.equals(ipr.getIntern().getId(), internId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "IPR stage does not belong to the specified intern");
        }
        Long programId = ipr.getProgram() != null ? ipr.getProgram().getId() : null;
        if (Objects.isNull(programId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR is not linked to an internship program");
        }
        if (Objects.nonNull(internshipId) && !Objects.equals(programId, internshipId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "IPR stage does not belong to the specified internship program");
        }

        return new ResolvedStageBinding(ipr.getId(), programId, stage.getTitle());
    }
}
