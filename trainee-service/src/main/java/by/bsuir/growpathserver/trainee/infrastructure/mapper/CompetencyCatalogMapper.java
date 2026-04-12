package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.CompetencyCatalogListResponse;
import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompetencyCatalogMapper {

    CompetencyRef toCompetencyRef(CompetencyEntity entity);

    default CompetencyCatalogListResponse toCatalogListResponse(List<CompetencyEntity> entities) {
        List<Object> data = new ArrayList<>(entities.size());
        for (CompetencyEntity entity : entities) {
            data.add(toCompetencyRef(entity));
        }
        return new CompetencyCatalogListResponse(data);
    }
}
