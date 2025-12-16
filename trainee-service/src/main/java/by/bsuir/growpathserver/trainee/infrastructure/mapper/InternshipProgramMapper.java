package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.dto.model.InternshipProgramResponse;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;

@Mapper(componentModel = "spring")
public interface InternshipProgramMapper {

    @Mapping(target = "id", expression = "java(String.valueOf(program.getId()))")
    @Mapping(target = "createdBy", expression = "java(program.getCreatedBy() != null ? String.valueOf(program.getCreatedBy()) : null)")
    @Mapping(target = "requirements", ignore = true)
    @Mapping(target = "goals", ignore = true)
    @Mapping(target = "competencies", ignore = true)
    @Mapping(target = "selectionStages", ignore = true)
    @Mapping(target = "internships", ignore = true)
    InternshipProgramResponse toInternshipProgramResponse(InternshipProgram program, @Context ObjectMapper objectMapper);

    @AfterMapping
    default void mapJsonFields(InternshipProgram program, @MappingTarget InternshipProgramResponse response, @Context ObjectMapper objectMapper) {
        response.setRequirements(parseStringList(program.getRequirements(), objectMapper));
        response.setGoals(parseGoals(program.getGoals(), objectMapper));
        response.setCompetencies(parseStringList(program.getCompetencies(), objectMapper));
        response.setSelectionStages(parseSelectionStages(program.getSelectionStages(), objectMapper));
        response.setInternships(parseStringList(program.getInternships(), objectMapper));
    }

    default java.util.List<String> parseStringList(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<java.util.List<String>>() {});
        } catch (Exception e) {
            return null;
        }
    }

    default java.util.List<Object> parseGoals(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            java.util.List<by.bsuir.growpathserver.dto.model.ProgramGoal> goals = 
                    objectMapper.readValue(json, new TypeReference<java.util.List<by.bsuir.growpathserver.dto.model.ProgramGoal>>() {});
            return goals != null ? new java.util.ArrayList<>(goals) : null;
        } catch (Exception e) {
            return null;
        }
    }

    default java.util.List<Object> parseSelectionStages(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            java.util.List<by.bsuir.growpathserver.dto.model.SelectionStage> stages = 
                    objectMapper.readValue(json, new TypeReference<java.util.List<by.bsuir.growpathserver.dto.model.SelectionStage>>() {});
            return stages != null ? new java.util.ArrayList<>(stages) : null;
        } catch (Exception e) {
            return null;
        }
    }
}
