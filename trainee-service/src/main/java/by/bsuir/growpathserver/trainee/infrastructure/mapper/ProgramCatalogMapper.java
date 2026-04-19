package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.ItDirectionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ItDirectionRef;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoalDefinitionRef;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.RequirementDefinitionRef;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionCatalogListResponse;
import by.bsuir.growpathserver.dto.model.SelectionStageDefinitionRef;
import by.bsuir.growpathserver.trainee.domain.entity.ItDirectionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.ProgramGoalDefinitionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RequirementDefinitionEntity;
import by.bsuir.growpathserver.trainee.domain.entity.SelectionStageDefinitionEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProgramCatalogMapper {

    ItDirectionRef toItDirectionRef(ItDirectionEntity entity);

    RequirementDefinitionRef toRequirementDefinitionRef(RequirementDefinitionEntity entity);

    ProgramGoalDefinitionRef toProgramGoalDefinitionRef(ProgramGoalDefinitionEntity entity);

    @Mapping(target = "isActive", source = "active")
    SelectionStageDefinitionRef toSelectionStageDefinitionRef(SelectionStageDefinitionEntity entity);

    default ItDirectionCatalogListResponse toItDirectionCatalogListResponse(List<ItDirectionEntity> entities) {
        List<Object> data = new ArrayList<>(entities.size());
        for (ItDirectionEntity entity : entities) {
            data.add(toItDirectionRef(entity));
        }
        return new ItDirectionCatalogListResponse(data);
    }

    default RequirementDefinitionCatalogListResponse toRequirementDefinitionCatalogListResponse(
            List<RequirementDefinitionEntity> entities
    ) {
        List<Object> data = new ArrayList<>(entities.size());
        for (RequirementDefinitionEntity entity : entities) {
            data.add(toRequirementDefinitionRef(entity));
        }
        return new RequirementDefinitionCatalogListResponse(data);
    }

    default ProgramGoalDefinitionCatalogListResponse toProgramGoalDefinitionCatalogListResponse(
            List<ProgramGoalDefinitionEntity> entities
    ) {
        List<Object> data = new ArrayList<>(entities.size());
        for (ProgramGoalDefinitionEntity entity : entities) {
            data.add(toProgramGoalDefinitionRef(entity));
        }
        return new ProgramGoalDefinitionCatalogListResponse(data);
    }

    default SelectionStageDefinitionCatalogListResponse toSelectionStageDefinitionCatalogListResponse(
            List<SelectionStageDefinitionEntity> entities
    ) {
        List<Object> data = new ArrayList<>(entities.size());
        for (SelectionStageDefinitionEntity entity : entities) {
            data.add(toSelectionStageDefinitionRef(entity));
        }
        return new SelectionStageDefinitionCatalogListResponse(data);
    }
}
