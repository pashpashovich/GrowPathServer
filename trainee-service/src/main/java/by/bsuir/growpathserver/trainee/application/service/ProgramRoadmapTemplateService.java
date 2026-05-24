package by.bsuir.growpathserver.trainee.application.service;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapLifecycleStatus;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RoadmapRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RoadmapStageRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProgramRoadmapTemplateService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStageRepository roadmapStageRepository;

    @Transactional
    public RoadmapEntity createEmptyTemplateForMentor(InternshipProgramEntity program, UserEntity mentor) {
        if (roadmapRepository.existsByProgram_IdAndMentor_Id(program.getId(), mentor.getId())) {
            return roadmapRepository.findByProgram_IdAndMentor_Id(program.getId(), mentor.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                                                                   "Roadmap template already exists for mentor"));
        }
        RoadmapEntity entity = buildEmptyTemplate(program, mentor);
        return roadmapRepository.save(entity);
    }

    @Transactional
    public void removeEmptyTemplateForMentor(Long programId, Long mentorUserId) {
        roadmapRepository.findByProgram_IdAndMentor_Id(programId, mentorUserId).ifPresent(roadmap -> {
            if (roadmap.getStatus() != RoadmapLifecycleStatus.DRAFT) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                  "Cannot remove mentor: roadmap template is not in draft status");
            }
            if (CollectionUtils.isNotEmpty(
                    roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(roadmap.getId()))) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                                                  "Cannot remove mentor: roadmap template already has stages");
            }
            roadmapRepository.delete(roadmap);
        });
    }

    public RoadmapEntity buildEmptyTemplate(InternshipProgramEntity program, UserEntity mentor) {
        RoadmapEntity entity = new RoadmapEntity();
        entity.setProgram(program);
        entity.setTitle(defaultTemplateTitle(program));
        entity.setDescription(null);
        entity.setStartDate(program.getStartDate());
        entity.setEndDate(program.getStartDate().plusDays(Math.max(1, program.getDuration()) - 1L));
        entity.setStatus(RoadmapLifecycleStatus.DRAFT);
        entity.setMentor(mentor);
        return entity;
    }

    private String defaultTemplateTitle(InternshipProgramEntity program) {
        return program.getTitle();
    }
}
