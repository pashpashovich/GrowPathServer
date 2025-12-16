package by.bsuir.growpathserver.trainee.application.service;

import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InternshipProgramServiceImpl implements InternshipProgramService {

    private final InternshipProgramRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public InternshipProgram createInternshipProgram(CreateInternshipProgramCommand command) {
        try {
            String requirementsJson = command.requirements() != null ?
                    objectMapper.writeValueAsString(command.requirements()) : null;
            String goalsJson = command.goals() != null ?
                    objectMapper.writeValueAsString(command.goals()) : null;
            String competenciesJson = command.competencies() != null ?
                    objectMapper.writeValueAsString(command.competencies()) : null;
            String selectionStagesJson = command.selectionStages() != null ?
                    objectMapper.writeValueAsString(command.selectionStages()) : null;

            InternshipProgram program = InternshipProgram.create(
                    command.title(),
                    command.description(),
                    command.startDate(),
                    command.duration(),
                    command.maxPlaces(),
                    requirementsJson,
                    goalsJson,
                    competenciesJson,
                    selectionStagesJson,
                    command.status(),
                    command.createdBy()
            );

            InternshipProgramEntity entity = program.toEntity();
            InternshipProgramEntity savedEntity = repository.save(entity);
            return InternshipProgram.fromEntity(savedEntity);
        }
        catch (JsonProcessingException e) {
            log.error("Error serializing JSON for internship program", e);
            throw new IllegalArgumentException("Invalid JSON data", e);
        }
    }

    @Override
    @Transactional
    public InternshipProgram updateInternshipProgram(UpdateInternshipProgramCommand command) {
        InternshipProgramEntity entity = repository.findById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Internship program not found with id: " + command.id()));

        if (command.title() != null) {
            entity.setTitle(command.title());
        }
        if (command.description() != null) {
            entity.setDescription(command.description());
        }
        if (command.startDate() != null) {
            entity.setStartDate(command.startDate());
        }
        if (command.duration() != null) {
            entity.setDuration(command.duration());
        }
        if (command.maxPlaces() != null) {
            entity.setMaxPlaces(command.maxPlaces());
        }
        if (command.requirements() != null) {
            try {
                entity.setRequirements(objectMapper.writeValueAsString(command.requirements()));
            }
            catch (JsonProcessingException e) {
                log.error("Error serializing requirements", e);
                throw new IllegalArgumentException("Invalid requirements data", e);
            }
        }
        if (command.goals() != null) {
            try {
                entity.setGoals(objectMapper.writeValueAsString(command.goals()));
            }
            catch (JsonProcessingException e) {
                log.error("Error serializing goals", e);
                throw new IllegalArgumentException("Invalid goals data", e);
            }
        }
        if (command.competencies() != null) {
            try {
                entity.setCompetencies(objectMapper.writeValueAsString(command.competencies()));
            }
            catch (JsonProcessingException e) {
                log.error("Error serializing competencies", e);
                throw new IllegalArgumentException("Invalid competencies data", e);
            }
        }
        if (command.selectionStages() != null) {
            try {
                entity.setSelectionStages(objectMapper.writeValueAsString(command.selectionStages()));
            }
            catch (JsonProcessingException e) {
                log.error("Error serializing selection stages", e);
                throw new IllegalArgumentException("Invalid selection stages data", e);
            }
        }
        if (command.status() != null) {
            entity.setStatus(command.status());
        }

        InternshipProgramEntity savedEntity = repository.save(entity);
        return InternshipProgram.fromEntity(savedEntity);
    }

    @Override
    @Transactional
    public void deleteInternshipProgram(DeleteInternshipProgramCommand command) {
        if (!repository.existsById(command.id())) {
            throw new NoSuchElementException("Internship program not found with id: " + command.id());
        }
        repository.deleteById(command.id());
    }

    @Override
    @Transactional(readOnly = true)
    public InternshipProgram getInternshipProgramById(Long id) {
        InternshipProgramEntity entity = repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Internship program not found with id: " + id));
        return InternshipProgram.fromEntity(entity);
    }
}
