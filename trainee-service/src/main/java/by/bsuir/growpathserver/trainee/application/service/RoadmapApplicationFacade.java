package by.bsuir.growpathserver.trainee.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.ChangeStageStatusRequest;
import by.bsuir.growpathserver.dto.model.CreateRoadmapTemplateRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.RoadmapTemplateListResponse;
import by.bsuir.growpathserver.dto.model.RoadmapTemplateResponse;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateRoadmapTemplateRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapStageEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapLifecycleStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapStageStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.StagePriorityLevel;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.RoadmapMapper;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RoadmapRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RoadmapStageRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoadmapApplicationFacade {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStageRepository roadmapStageRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final InternshipProgramParticipantService internshipProgramParticipantService;
    private final ProgramRoadmapTemplateService programRoadmapTemplateService;
    private final UserRepository userRepository;
    private final CurrentApplicationUserResolver currentUserResolver;
    private final RoadmapMapper roadmapMapper;

    @Transactional(readOnly = true)
    public RoadmapTemplateListResponse getProgramRoadmapTemplates(String programId) {
        long pid = parseLongId(programId, "programId");
        internshipProgramRepository.findById(pid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<RoadmapEntity> all = roadmapRepository.findByProgramId(pid);
        if (isHrOrAdmin()) {
            return toRoadmapTemplateListResponse(all);
        }
        return toRoadmapTemplateListResponse(all.stream().filter(this::canView).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public RoadmapTemplateListResponse listRoadmapTemplates(Long mentorId, Long programId) {
        Long effectiveMentorId = mentorId;
        if (isMentor() && !isHrOrAdmin() && Objects.isNull(effectiveMentorId)) {
            effectiveMentorId = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        }

        Specification<RoadmapEntity> spec = buildRoadmapSpec(programId, effectiveMentorId, null);
        return toRoadmapTemplateListResponse(roadmapRepository.findAll(spec));
    }

    @Transactional(readOnly = true)
    public RoadmapTemplateResponse getRoadmapTemplateById(String templateId) {
        return roadmapMapper.toRoadmapTemplateResponse(requireRoadmapViewable(parseLongId(templateId, "templateId")));
    }

    @Transactional(readOnly = true)
    public StageListResponse getRoadmapTemplateStages(String templateId) {
        long rid = parseLongId(templateId, "templateId");
        requireRoadmapViewable(rid);
        List<RoadmapStageEntity> stages = roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(rid);
        return roadmapMapper.toStageListResponse(stages);
    }

    @Transactional
    public StageResponse createRoadmapTemplateStage(String templateId, CreateStageRequest request) {
        RoadmapEntity roadmap = requireRoadmapEditable(parseLongId(templateId, "templateId"));
        validateStageDateRange(roadmap, request.getStartDate(), request.getEndDate());
        RoadmapStageEntity stage = new RoadmapStageEntity();
        stage.setRoadmap(roadmap);
        stage.setTitle(request.getTitle());
        stage.setDescription(request.getDescription());
        stage.setStartDate(request.getStartDate());
        stage.setEndDate(request.getEndDate());
        if (Objects.nonNull(request.getPriority())) {
            stage.setPriority(StagePriorityLevel.fromString(request.getPriority().getValue()));
        }
        stage.setCheckpoint(Boolean.TRUE.equals(request.getIsCheckpoint()));
        stage.setComments(request.getComments());
        int nextOrder = Objects.nonNull(request.getOrder()) ? request.getOrder() : nextStageOrder(roadmap.getId());
        stage.setStageOrder(nextOrder);
        stage.setStatus(RoadmapStageStatus.PENDING);
        currentUserResolver.resolveCurrentUserDatabaseId().flatMap(userRepository::findById)
                .ifPresent(stage::setCreatedBy);
        RoadmapStageEntity saved = roadmapStageRepository.save(stage);
        return roadmapMapper.toStageResponse(saved);
    }

    @Transactional
    public StageResponse updateRoadmapTemplateStage(String templateId, String stageId, UpdateStageRequest request) {
        long rid = parseLongId(templateId, "templateId");
        long sid = parseLongId(stageId, "stageId");
        RoadmapEntity roadmap = requireRoadmapEditable(rid);
        RoadmapStageEntity stage = roadmapStageRepository.findByIdAndRoadmapId(sid, rid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (Objects.nonNull(request.getTitle())) {
            stage.setTitle(request.getTitle());
        }
        if (Objects.nonNull(request.getDescription())) {
            stage.setDescription(request.getDescription());
        }
        if (Objects.nonNull(request.getStartDate())) {
            stage.setStartDate(request.getStartDate());
        }
        if (Objects.nonNull(request.getEndDate())) {
            stage.setEndDate(request.getEndDate());
        }
        validateStageDateRange(roadmap, stage.getStartDate(), stage.getEndDate());
        if (Objects.nonNull(request.getPriority())) {
            stage.setPriority(StagePriorityLevel.fromString(request.getPriority().getValue()));
        }
        if (Objects.nonNull(request.getIsCheckpoint())) {
            stage.setCheckpoint(request.getIsCheckpoint());
        }
        if (Objects.nonNull(request.getComments())) {
            stage.setComments(request.getComments());
        }
        return roadmapMapper.toStageResponse(roadmapStageRepository.save(stage));
    }

    @Transactional
    public MessageResponse deleteRoadmapTemplateStage(String templateId, String stageId) {
        long rid = parseLongId(templateId, "templateId");
        long sid = parseLongId(stageId, "stageId");
        requireRoadmapEditable(rid);
        RoadmapStageEntity stage = roadmapStageRepository.findByIdAndRoadmapId(sid, rid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        roadmapStageRepository.delete(stage);
        renumberStages(rid);
        MessageResponse m = new MessageResponse();
        m.setMessage("Stage deleted");
        return m;
    }

    @Transactional
    public MessageResponse reorderRoadmapTemplateStages(String templateId, ReorderStagesRequest request) {
        long rid = parseLongId(templateId, "templateId");
        requireRoadmapEditable(rid);
        List<Long> ids = request.getStageIds();
        for (int i = 0; i < ids.size(); i++) {
            Long sid = ids.get(i);
            RoadmapStageEntity st = roadmapStageRepository.findByIdAndRoadmapId(sid, rid)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
            st.setStageOrder(i);
        }
        MessageResponse m = new MessageResponse();
        m.setMessage("Stages reordered");
        return m;
    }

    @Transactional
    public StageResponse changeRoadmapTemplateStageStatus(String templateId, String stageId,
                                                          ChangeStageStatusRequest request) {
        long rid = parseLongId(templateId, "templateId");
        long sid = parseLongId(stageId, "stageId");
        requireRoadmapEditable(rid);
        RoadmapStageEntity stage = roadmapStageRepository.findByIdAndRoadmapId(sid, rid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        stage.setStatus(RoadmapStageStatus.fromString(request.getStatus().getValue()));
        if (Objects.nonNull(request.getComments())) {
            stage.setComments(request.getComments());
        }
        return roadmapMapper.toStageResponse(roadmapStageRepository.save(stage));
    }

    @Transactional
    public RoadmapTemplateResponse createRoadmapTemplate(CreateRoadmapTemplateRequest request) {
        InternshipProgramEntity program = internshipProgramRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Long mentorId = resolveMentorIdForRoadmap(request.getMentorId());
        UserEntity mentor = internshipProgramParticipantService.requireMentorOnProgram(program.getId(), mentorId);
        RoadmapEntity entity;
        if (roadmapRepository.existsByProgram_IdAndMentor_Id(program.getId(), mentorId)) {
            entity = roadmapRepository.findByProgram_IdAndMentor_Id(program.getId(), mentorId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                                                                   "Roadmap template for this mentor already exists"));
            if (Objects.nonNull(request.getTitle())) {
                entity.setTitle(request.getTitle());
            }
            if (Objects.nonNull(request.getDescription())) {
                entity.setDescription(request.getDescription());
            }
            entity = roadmapRepository.save(entity);
        }
        else {
            entity = programRoadmapTemplateService.buildEmptyTemplate(program, mentor);
            entity.setTitle(request.getTitle());
            entity.setDescription(request.getDescription());
            entity = roadmapRepository.save(entity);
        }
        RoadmapEntity saved = entity;
        return roadmapMapper.toRoadmapTemplateResponse(
                roadmapRepository.findWithMentorAndInternsById(saved.getId()).orElse(saved));
    }

    @Transactional
    public RoadmapTemplateResponse updateRoadmapTemplate(String id, UpdateRoadmapTemplateRequest request) {
        RoadmapEntity entity = requireRoadmapEditable(parseLongId(id, "templateId"));
        if (Objects.nonNull(request.getTitle())) {
            entity.setTitle(request.getTitle());
        }
        if (Objects.nonNull(request.getDescription())) {
            entity.setDescription(request.getDescription());
        }
        if (Objects.nonNull(request.getStatus())) {
            RoadmapLifecycleStatus targetStatus = RoadmapLifecycleStatus.fromString(request.getStatus().getValue());
            if (targetStatus == RoadmapLifecycleStatus.ACTIVE) {
                validateRoadmapActivation(entity);
            }
            entity.setStatus(targetStatus);
        }
        if (Objects.nonNull(request.getMentorId())) {
            Long mentorId = resolveMentorIdForRoadmap(request.getMentorId());
            UserEntity mentor = internshipProgramParticipantService.requireMentorOnProgram(
                    entity.getProgram().getId(), mentorId);
            entity.setMentor(mentor);
        }
        roadmapRepository.save(entity);
        return roadmapMapper.toRoadmapTemplateResponse(
                roadmapRepository.findWithMentorAndInternsById(entity.getId()).orElse(entity));
    }

    @Transactional
    public MessageResponse deleteRoadmapTemplate(String id) {
        long rid = parseLongId(id, "templateId");
        requireRoadmapEditable(rid);
        roadmapRepository.deleteById(rid);
        MessageResponse m = new MessageResponse();
        m.setMessage("Roadmap template deleted");
        return m;
    }

    private void renumberStages(long roadmapId) {
        List<RoadmapStageEntity> stages = roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(roadmapId);
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).setStageOrder(i);
        }
    }

    private int nextStageOrder(long roadmapId) {
        return roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(roadmapId).stream()
                .mapToInt(RoadmapStageEntity::getStageOrder)
                .max()
                .orElse(-1) + 1;
    }

    private RoadmapEntity requireRoadmapViewable(long roadmapId) {
        RoadmapEntity r = roadmapRepository.findWithMentorAndInternsById(roadmapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canView(r)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return r;
    }

    private RoadmapEntity requireRoadmapEditable(long roadmapId) {
        RoadmapEntity r = roadmapRepository.findWithMentorAndInternsById(roadmapId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canEdit(r)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return r;
    }

    private boolean canView(RoadmapEntity r) {
        if (isHrOrAdmin()) {
            return true;
        }
        Long uid = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        if (Objects.nonNull(uid)
                && Objects.nonNull(r.getMentor())
                && uid.equals(r.getMentor().getId())) {
            return true;
        }
        return Objects.nonNull(uid)
                && Objects.nonNull(r.getMentor())
                && uid.equals(r.getMentor().getId());
    }

    private boolean canEdit(RoadmapEntity r) {
        if (isHrOrAdmin()) {
            return true;
        }
        if (isIntern()) {
            return false;
        }
        Long uid = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        return Objects.nonNull(uid)
                && Objects.nonNull(r.getMentor())
                && uid.equals(r.getMentor().getId());
    }

    private boolean hasAuthority(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth)) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private boolean isHrOrAdmin() {
        return hasAuthority("HR_MANAGER") || hasAuthority("ADMIN");
    }

    private boolean isMentor() {
        return hasAuthority("MENTOR");
    }

    private boolean isIntern() {
        return hasAuthority("INTERN");
    }

    private static Specification<RoadmapEntity> buildRoadmapSpec(Long programId, Long mentorId, Long internUserId) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> parts = new ArrayList<>();
            if (Objects.nonNull(programId)) {
                parts.add(cb.equal(root.get("program").get("id"), programId));
            }
            if (Objects.nonNull(mentorId)) {
                parts.add(cb.equal(root.get("mentor").get("id"), mentorId));
            }
            if (Objects.nonNull(internUserId)) {
                var join = root.join("interns", JoinType.INNER);
                parts.add(cb.equal(join.get("user").get("id"), internUserId));
            }
            if (parts.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(parts.toArray(Predicate[]::new));
        };
    }

    private RoadmapTemplateListResponse toRoadmapTemplateListResponse(List<RoadmapEntity> entities) {
        List<RoadmapEntity> loaded = new ArrayList<>();
        for (RoadmapEntity entity : entities) {
            loaded.add(roadmapRepository.findWithMentorAndInternsById(entity.getId()).orElse(entity));
        }
        return roadmapMapper.toRoadmapTemplateListResponse(loaded);
    }

    private static long parseLongId(String raw, String label) {
        try {
            return Long.parseLong(raw);
        }
        catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + label);
        }
    }

    private void validateStageDateRange(RoadmapEntity roadmap,
                                        java.time.LocalDate startDate,
                                        java.time.LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stage dates are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stage startDate must be before endDate");
        }
        if (startDate.isBefore(roadmap.getStartDate()) || endDate.isAfter(roadmap.getEndDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Stage dates must be within internship period");
        }
    }

    private Long resolveMentorIdForRoadmap(Long requestedMentorId) {
        Long currentUserId = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        if (isMentor() && !isHrOrAdmin()) {
            if (Objects.isNull(currentUserId)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            if (Objects.nonNull(requestedMentorId) && !Objects.equals(requestedMentorId, currentUserId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                                  "Mentor can only manage their own roadmap template");
            }
            return currentUserId;
        }
        if (Objects.isNull(requestedMentorId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "mentorId is required");
        }
        return requestedMentorId;
    }

    private void validateRoadmapActivation(RoadmapEntity roadmap) {
        if (Objects.isNull(roadmap.getMentor())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor must be assigned before activation");
        }
        if (!internshipProgramParticipantService.isMentorOnProgram(
                roadmap.getProgram().getId(), roadmap.getMentor().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "Mentor must be assigned to the internship program");
        }
        List<RoadmapStageEntity> stages = roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(roadmap.getId());
        if (stages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one stage is required for activation");
        }
        for (RoadmapStageEntity stage : stages) {
            validateStageDateRange(roadmap, stage.getStartDate(), stage.getEndDate());
        }
    }
}
