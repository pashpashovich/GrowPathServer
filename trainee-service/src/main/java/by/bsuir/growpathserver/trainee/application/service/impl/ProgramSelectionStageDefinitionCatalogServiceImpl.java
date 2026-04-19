package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.CreateSelectionStageDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateSelectionStageDefinitionRequest;
import by.bsuir.growpathserver.trainee.application.service.ProgramSelectionStageDefinitionCatalogService;
import by.bsuir.growpathserver.trainee.domain.entity.SelectionStageDefinitionEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.ProgramCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.SelectionStageDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.web.CatalogPathIds;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramSelectionStageDefinitionCatalogServiceImpl
        implements ProgramSelectionStageDefinitionCatalogService {

    private final SelectionStageDefinitionRepository selectionStageDefinitionRepository;
    private final ProgramCatalogMapper programCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public SelectionStageDefinitionCatalogListResponse list() {
        return programCatalogMapper.toSelectionStageDefinitionCatalogListResponse(
                selectionStageDefinitionRepository.findAllByOrderByNameAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public SelectionStageDefinitionRef getById(String id) {
        long lid = CatalogPathIds.parseLong(id);
        SelectionStageDefinitionEntity entity = selectionStageDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return programCatalogMapper.toSelectionStageDefinitionRef(entity);
    }

    @Override
    @Transactional
    public SelectionStageDefinitionRef create(CreateSelectionStageDefinitionRequest request) {
        String name = requireNonBlank(request.getName(), "name");
        String description = normalizeDescription(request.getDescription());
        if (request.getIsActive() == null) {
            throw new IllegalArgumentException("isActive");
        }
        boolean active = request.getIsActive();
        if (selectionStageDefinitionRepository.existsDuplicateNameDescriptionAndActive(name, description, active,
                                                                                       null)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        SelectionStageDefinitionEntity e = new SelectionStageDefinitionEntity();
        e.setName(name);
        e.setDescription(description);
        e.setActive(active);
        return programCatalogMapper.toSelectionStageDefinitionRef(selectionStageDefinitionRepository.save(e));
    }

    @Override
    @Transactional
    public SelectionStageDefinitionRef update(String id, UpdateSelectionStageDefinitionRequest request) {
        long lid = CatalogPathIds.parseLong(id);
        SelectionStageDefinitionEntity entity = selectionStageDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newName = request.getName() != null ? requireNonBlank(request.getName(), "name") : entity.getName();
        String newDescription = request.getDescription() != null
                ? normalizeDescription(request.getDescription())
                : entity.getDescription();
        boolean newActive =
                request.getIsActive() != null ? Boolean.TRUE.equals(request.getIsActive()) : entity.isActive();
        if (selectionStageDefinitionRepository.existsDuplicateNameDescriptionAndActive(newName, newDescription,
                                                                                       newActive, lid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        entity.setName(newName);
        entity.setDescription(newDescription);
        entity.setActive(newActive);
        return programCatalogMapper.toSelectionStageDefinitionRef(selectionStageDefinitionRepository.save(entity));
    }

    @Override
    @Transactional
    public MessageResponse delete(String id) {
        long lid = CatalogPathIds.parseLong(id);
        SelectionStageDefinitionEntity entity = selectionStageDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        selectionStageDefinitionRepository.delete(entity);
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
