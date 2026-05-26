package by.bsuir.growpathserver.trainee.application.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.CompetencyRef;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskCompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.CompetencyCatalogMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskCompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCompetencyBindingService {

    private final TaskRepository taskRepository;
    private final TaskCompetencyRepository taskCompetencyRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final CompetencyRepository competencyRepository;
    private final CompetencyCatalogMapper competencyCatalogMapper;

    @Transactional(readOnly = true)
    public List<CompetencyRef> getCompetencyRefsForTask(Long taskId) {
        return taskCompetencyRepository.findByTaskId(taskId).stream()
                .map(TaskCompetencyEntity::getCompetency)
                .filter(Objects::nonNull)
                .map(competencyCatalogMapper::toCompetencyRef)
                .toList();
    }

    @Transactional
    public void replaceTaskCompetencies(Long taskId, Long programId, List<Long> competencyIds) {
        if (Objects.isNull(competencyIds)) {
            return;
        }
        if (!taskRepository.existsById(taskId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }

        if (competencyIds.stream().anyMatch(Objects::isNull)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Competency id must not be null");
        }
        Set<Long> distinctIds = new HashSet<>(competencyIds);
        if (distinctIds.size() != competencyIds.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate competency ids are not allowed");
        }

        Set<Long> allowedIds = loadProgramCompetencyIds(programId);
        for (Long competencyId : distinctIds) {
            if (!allowedIds.contains(competencyId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                  "Competency " + competencyId
                                                          + " is not part of the internship program");
            }
        }

        List<TaskCompetencyEntity> existing = taskCompetencyRepository.findByTaskId(taskId);
        if (CollectionUtils.isNotEmpty(existing)) {
            taskCompetencyRepository.deleteAll(existing);
        }

        if (distinctIds.isEmpty()) {
            return;
        }

        TaskEntity taskRef = taskRepository.getReferenceById(taskId);
        List<TaskCompetencyEntity> links = new ArrayList<>(distinctIds.size());
        for (Long competencyId : distinctIds) {
            TaskCompetencyEntity link = new TaskCompetencyEntity();
            link.setTask(taskRef);
            link.setCompetency(competencyRepository.getReferenceById(competencyId));
            links.add(link);
        }
        taskCompetencyRepository.saveAll(links);
    }

    private Set<Long> loadProgramCompetencyIds(Long programId) {
        InternshipProgramEntity program = internshipProgramRepository.findWithCollectionsById(programId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Internship program not found"));
        return program.getCompetencies().stream()
                .map(CompetencyEntity::getId)
                .collect(Collectors.toSet());
    }
}
