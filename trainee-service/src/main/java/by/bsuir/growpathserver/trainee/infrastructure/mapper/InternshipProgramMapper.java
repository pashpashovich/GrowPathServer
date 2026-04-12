package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.dto.model.ProgramGoal;
import by.bsuir.growpathserver.dto.model.SelectionStage;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgramGoalItem;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgramStageItem;
import by.bsuir.growpathserver.trainee.domain.aggregate.ProgramCompetency;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InternshipProgramMapper {

    @Mapping(target = "requirements", ignore = true)
    @Mapping(target = "goals", ignore = true)
    @Mapping(target = "competencyRefs", ignore = true)
    @Mapping(target = "selectionStages", ignore = true)
    @Mapping(target = "internships", ignore = true)
    @Mapping(target = "status", ignore = true)
    InternshipProgramResponse toInternshipProgramResponse(InternshipProgram program,
                                                          @Context ObjectMapper objectMapper);

    @AfterMapping
    default void mapJsonFields(InternshipProgram program, @MappingTarget InternshipProgramResponse response,
                               @Context ObjectMapper objectMapper) {
        response.setStatus(InternshipProgramResponse.StatusEnum.fromValue(program.getStatus().getValue()));
        response.setRequirements(program.getRequirements().isEmpty() ? null : new ArrayList<>(program.getRequirements()));
        response.setGoals(toProgramGoalPayload(program.getGoals()));
        response.setCompetencyRefs(toCompetencyRefPayload(program));
        response.setSelectionStages(toSelectionStagePayload(program.getSelectionStages()));
        response.setInternships(parseStringList(program.getInternships(), objectMapper));
    }

    private static List<Object> toCompetencyRefPayload(InternshipProgram program) {
        List<Object> refs = new ArrayList<>();
        for (ProgramCompetency c : program.getCompetencyRefs()) {
            refs.add(new CompetencyRef(c.id(), c.name()));
        }
        return refs;
    }

    private static List<Object> toProgramGoalPayload(List<InternshipProgramGoalItem> goals) {
        if (Objects.isNull(goals) || goals.isEmpty()) {
            return null;
        }
        List<Object> out = new ArrayList<>();
        for (InternshipProgramGoalItem g : goals) {
            ProgramGoal dto = new ProgramGoal();
            dto.setId(g.id());
            dto.setTitle(g.title());
            dto.setDescription(g.description());
            out.add(dto);
        }
        return out;
    }

    private static List<Object> toSelectionStagePayload(List<InternshipProgramStageItem> stages) {
        if (Objects.isNull(stages) || stages.isEmpty()) {
            return null;
        }
        List<Object> out = new ArrayList<>();
        for (InternshipProgramStageItem s : stages) {
            SelectionStage dto = new SelectionStage();
            dto.setId(s.id());
            dto.setName(s.name());
            dto.setDescription(s.description());
            dto.setOrder(s.order());
            dto.setIsActive(s.active());
            out.add(dto);
        }
        return out;
    }

    default List<String> parseStringList(String json, ObjectMapper objectMapper) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        }
        catch (Exception e) {
            return null;
        }
    }
}
