package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.RoadmapListResponse;
import by.bsuir.growpathserver.dto.model.RoadmapResponse;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapInternEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapStageEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoadmapMapper {

    @Mapping(target = "programId", source = "program.id")
    @Mapping(target = "mentorId", source = "mentor.id")
    @Mapping(target = "internIds", expression = "java(mapInternIds(entity))")
    @Mapping(target = "status", expression = "java(toRoadmapStatus(entity))")
    RoadmapResponse toRoadmapResponse(RoadmapEntity entity);

    @Mapping(target = "roadmapId", source = "roadmap.id")
    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "isCheckpoint", source = "checkpoint")
    @Mapping(target = "order", source = "stageOrder")
    @Mapping(target = "status", expression = "java(toStageStatus(stage))")
    @Mapping(target = "priority", expression = "java(toStagePriority(stage))")
    StageResponse toStageResponse(RoadmapStageEntity stage);

    default RoadmapListResponse toRoadmapListResponse(List<RoadmapEntity> entities) {
        RoadmapListResponse response = new RoadmapListResponse();
        List<Object> rows = new ArrayList<>();
        for (RoadmapEntity entity : entities) {
            rows.add(toRoadmapResponse(entity));
        }
        response.setData(rows);
        return response;
    }

    default StageListResponse toStageListResponse(List<RoadmapStageEntity> stages) {
        StageListResponse response = new StageListResponse();
        List<Object> rows = new ArrayList<>();
        for (RoadmapStageEntity stage : stages) {
            rows.add(toStageResponse(stage));
        }
        response.setData(rows);
        return response;
    }

    default List<String> mapInternIds(RoadmapEntity entity) {
        return entity.getInterns().stream()
                .map(RoadmapInternEntity::getUser)
                .filter(Objects::nonNull)
                .map(UserEntity::getKeycloakUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    default RoadmapResponse.StatusEnum toRoadmapStatus(RoadmapEntity entity) {
        return RoadmapResponse.StatusEnum.fromValue(entity.getStatus().getValue());
    }

    default StageResponse.StatusEnum toStageStatus(RoadmapStageEntity stage) {
        return StageResponse.StatusEnum.fromValue(stage.getStatus().getValue());
    }

    default StageResponse.PriorityEnum toStagePriority(RoadmapStageEntity stage) {
        if (Objects.isNull(stage.getPriority())) {
            return null;
        }
        return StageResponse.PriorityEnum.fromValue(stage.getPriority().getValue());
    }
}
