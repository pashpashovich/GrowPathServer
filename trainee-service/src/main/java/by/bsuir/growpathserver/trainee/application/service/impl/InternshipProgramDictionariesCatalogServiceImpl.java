package by.bsuir.growpathserver.trainee.application.service.impl;

import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramDictionariesCatalogService;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.ProgramCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ItDirectionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.ProgramGoalDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RequirementDefinitionRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.SelectionStageDefinitionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipProgramDictionariesCatalogServiceImpl implements InternshipProgramDictionariesCatalogService {

    private final ItDirectionRepository itDirectionRepository;
    private final RequirementDefinitionRepository requirementDefinitionRepository;
    private final ProgramGoalDefinitionRepository programGoalDefinitionRepository;
    private final SelectionStageDefinitionRepository selectionStageDefinitionRepository;
    private final ProgramCatalogMapper programCatalogMapper;

    @Override
    public ItDirectionCatalogListResponse listItDirections() {
        return programCatalogMapper.toItDirectionCatalogListResponse(itDirectionRepository.findAllByOrderByCodeAsc());
    }

    @Override
    public RequirementDefinitionCatalogListResponse listRequirementDefinitions() {
        return programCatalogMapper.toRequirementDefinitionCatalogListResponse(
                requirementDefinitionRepository.findAllByOrderByRequirementTextAsc());
    }

    @Override
    public ProgramGoalDefinitionCatalogListResponse listProgramGoalDefinitions() {
        return programCatalogMapper.toProgramGoalDefinitionCatalogListResponse(
                programGoalDefinitionRepository.findAllByOrderByTitleAsc());
    }

    @Override
    public SelectionStageDefinitionCatalogListResponse listSelectionStageDefinitions() {
        return programCatalogMapper.toSelectionStageDefinitionCatalogListResponse(
                selectionStageDefinitionRepository.findAllByOrderByNameAsc());
    }
}
