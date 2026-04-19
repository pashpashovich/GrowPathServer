package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.CreateRequirementDefinitionRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionRef;
import by.bsuir.growpathserver.dto.model.UpdateRequirementDefinitionRequest;
import by.bsuir.growpathserver.trainee.application.service.ProgramRequirementDefinitionCatalogService;
import by.bsuir.growpathserver.trainee.domain.entity.RequirementDefinitionEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.ProgramCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RequirementDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.web.CatalogPathIds;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramRequirementDefinitionCatalogServiceImpl implements ProgramRequirementDefinitionCatalogService {

    private final RequirementDefinitionRepository requirementDefinitionRepository;
    private final ProgramCatalogMapper programCatalogMapper;

    @Override
    @Transactional(readOnly = true)
    public RequirementDefinitionCatalogListResponse list() {
        return programCatalogMapper.toRequirementDefinitionCatalogListResponse(
                requirementDefinitionRepository.findAllByOrderByRequirementTextAsc());
    }

    @Override
    @Transactional(readOnly = true)
    public RequirementDefinitionRef getById(String id) {
        long lid = CatalogPathIds.parseLong(id);
        RequirementDefinitionEntity entity = requirementDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return programCatalogMapper.toRequirementDefinitionRef(entity);
    }

    @Override
    @Transactional
    public RequirementDefinitionRef create(CreateRequirementDefinitionRequest request) {
        String text = normalizeRequirementText(request.getRequirementText());
        if (requirementDefinitionRepository.existsByRequirementText(text)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        RequirementDefinitionEntity e = new RequirementDefinitionEntity();
        e.setRequirementText(text);
        return programCatalogMapper.toRequirementDefinitionRef(requirementDefinitionRepository.save(e));
    }

    @Override
    @Transactional
    public RequirementDefinitionRef update(String id, UpdateRequirementDefinitionRequest request) {
        long lid = CatalogPathIds.parseLong(id);
        RequirementDefinitionEntity entity = requirementDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        String newText = request.getRequirementText() != null
                ? normalizeRequirementText(request.getRequirementText())
                : entity.getRequirementText();
        if (!newText.equals(entity.getRequirementText())
                && requirementDefinitionRepository.existsByRequirementTextAndIdNot(newText, lid)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT);
        }
        entity.setRequirementText(newText);
        return programCatalogMapper.toRequirementDefinitionRef(requirementDefinitionRepository.save(entity));
    }

    @Override
    @Transactional
    public MessageResponse delete(String id) {
        long lid = CatalogPathIds.parseLong(id);
        RequirementDefinitionEntity entity = requirementDefinitionRepository.findById(lid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        requirementDefinitionRepository.delete(entity);
        MessageResponse msg = new MessageResponse();
        msg.setMessage("Deleted");
        return msg;
    }

    private static String normalizeRequirementText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("requirementText");
        }
        return text.trim();
    }
}
