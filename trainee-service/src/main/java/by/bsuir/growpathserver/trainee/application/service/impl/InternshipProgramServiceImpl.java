package by.bsuir.growpathserver.trainee.application.service.impl;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.CreateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.DeleteInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.command.UpdateInternshipProgramCommand;
import by.bsuir.growpathserver.trainee.application.service.InternshipProgramService;
import by.bsuir.growpathserver.trainee.domain.aggregate.InternshipProgram;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.exception.DuplicateInternshipProgramTitleException;
import by.bsuir.growpathserver.trainee.domain.validator.InternshipProgramValidator;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.InternshipProgramEntityMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.CompetencyRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipProgramServiceImpl implements InternshipProgramService {

    private final InternshipProgramRepository repository;
    private final CompetencyRepository competencyRepository;
    private final InternshipProgramEntityMapper internshipProgramEntityMapper;

    @Transactional
    @Override
    public InternshipProgram createInternshipProgram(CreateInternshipProgramCommand command) {
        InternshipProgramValidator.validateDurationMonths(command.duration());
        String title = Objects.isNull(command.title()) ? "" : command.title().trim();
        if (title.isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (repository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateInternshipProgramTitleException();
        }

        InternshipProgramEntity entity = internshipProgramEntityMapper.toNewEntity(command, title, competencyRepository);
        InternshipProgramEntity savedEntity = repository.save(entity);
        return InternshipProgram.fromEntity(savedEntity);
    }

    @Transactional
    @Override
    public InternshipProgram updateInternshipProgram(UpdateInternshipProgramCommand command) {
        InternshipProgramEntity entity = repository.findWithCollectionsById(command.id())
                .orElseThrow(() -> new NoSuchElementException("Internship program not found with id: " + command.id()));

        boolean structural = internshipProgramEntityMapper.isStructuralChange(command, entity);
        InternshipProgramValidator.assertUpdateAllowedAfterStart(
                entity.getStartDate(),
                entity.getStatus(),
                LocalDate.now(),
                command.status(),
                structural
        );

        if (Objects.nonNull(command.title())) {
            String trimmed = command.title().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Title is required");
            }
            if (repository.existsByTitleIgnoreCaseAndIdNot(trimmed, command.id())) {
                throw new DuplicateInternshipProgramTitleException();
            }
        }

        internshipProgramEntityMapper.applyUpdate(command, entity, competencyRepository);

        InternshipProgramEntity savedEntity = repository.save(entity);
        return InternshipProgram.fromEntity(savedEntity);
    }

    @Transactional
    @Override
    public void deleteInternshipProgram(DeleteInternshipProgramCommand command) {
        if (!repository.existsById(command.id())) {
            throw new NoSuchElementException("Internship program not found with id: " + command.id());
        }
        repository.deleteById(command.id());
    }

    @Transactional(readOnly = true)
    @Override
    public InternshipProgram getInternshipProgramById(Long id) {
        InternshipProgramEntity entity = repository.findWithCollectionsById(id)
                .orElseThrow(() -> new NoSuchElementException("Internship program not found with id: " + id));
        return InternshipProgram.fromEntity(entity);
    }
}
