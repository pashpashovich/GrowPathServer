package by.bsuir.growpathserver.trainee.application.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprStageEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprStageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskIprStageBindingService {

    private final IprStageRepository iprStageRepository;

    public Long resolveStageId(Long iprStageId, Long stageId) {
        if (Objects.nonNull(iprStageId)) {
            return iprStageId;
        }
        return stageId;
    }

    @Transactional(readOnly = true)
    public void validateStageBinding(Long stageId, Long iprId, Long programId, Long assigneeId) {
        if (Objects.isNull(stageId)) {
            return;
        }
        IprStageEntity stage = iprStageRepository.findLoadedById(stageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR stage not found"));
        IprEntity ipr = stage.getIpr();
        if (Objects.isNull(ipr)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR stage is not linked to an IPR");
        }
        if (Objects.nonNull(iprId) && !Objects.equals(ipr.getId(), iprId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stage does not belong to the specified IPR");
        }
        if (Objects.nonNull(programId)
                && Objects.nonNull(ipr.getProgram())
                && !Objects.equals(ipr.getProgram().getId(), programId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Stage does not belong to the specified internship program");
        }
        if (Objects.nonNull(assigneeId)
                && Objects.nonNull(ipr.getIntern())
                && !Objects.equals(ipr.getIntern().getId(), assigneeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Stage does not belong to the specified intern");
        }
    }
}
