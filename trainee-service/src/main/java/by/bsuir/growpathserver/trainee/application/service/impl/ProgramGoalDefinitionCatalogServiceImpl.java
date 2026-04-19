package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.CreateProgramGoalDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateProgramGoalDefinitionRequest;
import by.bsuir.growpathserver.trainee.application.service.ProgramGoalDefinitionCatalogService;
import by.bsuir.growpathserver.trainee.domain.entity.ProgramGoalDefinitionEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.ProgramCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ProgramGoalDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.web.CatalogPathIds;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramGoalDefinitionCatalogServiceImpl implements ProgramGoalDefinitionCatalogService {

    private final ProgramGoalDefinitionRepository programGoalDefinitionRepository;
    private final ProgramCatalogMapper programCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public ProgramGoalDefinitionCatalogListResponse list() {
        return programCatalogMapper.toProgramGoalDefinitionCatalogListResponse(
                programGoalDefinitionRepository.findAllByOrderByTitleAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramGoalDefinitionRef getById(String id) {
        long lid = CatalogPathIds.parseLong(id);
        ProgramGoalDefinitionEntity entity = programGoalDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return programCatalogMapper.toProgramGoalDefinitionRef(entity);
    }

    @Override
    @Transactional
    public ProgramGoalDefinitionRef create(CreateProgramGoalDefinitionRequest request) {
        String title = requireNonBlank(request.getTitle(), "title");
        String description = normalizeDescription(request.getDescription());
        if (programGoalDefinitionRepository.existsDuplicateTitleAndDescription(title, description, null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        ProgramGoalDefinitionEntity e = new ProgramGoalDefinitionEntity();
        e.setTitle(title);
        e.setDescription(description);
        return programCatalogMapper.toProgramGoalDefinitionRef(programGoalDefinitionRepository.save(e));
    }

    @Override
    @Transactional
    public ProgramGoalDefinitionRef update(String id, UpdateProgramGoalDefinitionRequest request) {
        long lid = CatalogPathIds.parseLong(id);
        ProgramGoalDefinitionEntity entity = programGoalDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newTitle = request.getTitle() != null ? requireNonBlank(request.getTitle(), "title") : entity.getTitle();
        String newDescription = request.getDescription() != null
                ? normalizeDescription(request.getDescription())
                : entity.getDescription();
        if (programGoalDefinitionRepository.existsDuplicateTitleAndDescription(newTitle, newDescription, lid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        entity.setTitle(newTitle);
        entity.setDescription(newDescription);
        return programCatalogMapper.toProgramGoalDefinitionRef(programGoalDefinitionRepository.save(entity));
    }

    @Override
    @Transactional
    public MessageResponse delete(String id) {
        long lid = CatalogPathIds.parseLong(id);
        ProgramGoalDefinitionEntity entity = programGoalDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        programGoalDefinitionRepository.delete(entity);
        MessageResponse msg = new MessageResponse();
        msg.setMessage("Deleted");
        return msg;
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field);
        }
        return value.trim();
    }

    private static String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }
}
