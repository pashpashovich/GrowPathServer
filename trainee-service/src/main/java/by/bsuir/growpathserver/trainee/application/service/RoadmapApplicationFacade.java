package by.bsuir.growpathserver.trainee.application.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import by.bsuir.growpathserver.dto.model.ChangeStageStatusRequest;
import by.bsuir.growpathserver.dto.model.CreateRoadmapRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.RoadmapListResponse;
import by.bsuir.growpathserver.dto.model.RoadmapResponse;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateRoadmapRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapInternEntity;
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
    private final UserRepository userRepository;
    private final CurrentApplicationUserResolver currentUserResolver;
    private final RoadmapMapper roadmapMapper;

    @Transactional(readOnly = true)
    public RoadmapListResponse getProgramInternships(String programId) {
        long pid = parseLongId(programId, "programId");
        internshipProgramRepository.findById(pid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        List<RoadmapEntity> all = roadmapRepository.findByProgramId(pid);
        if (isHrOrAdmin()) {
            return toRoadmapListResponse(all);
        }
        return toRoadmapListResponse(all.stream().filter(this::canView).collect(Collectors.toList()));
    }

    @Transactional(readOnly = true)
    public RoadmapListResponse listInternships(Long mentorId, Long internId, Long programId) {
        if (isIntern() && !isHrOrAdmin()) {
            String kc = currentUserResolver.resolveCurrentKeycloakSubject().orElse(null);
            if (StringUtils.isBlank(kc)) {
                RoadmapListResponse empty = new RoadmapListResponse();
                empty.setData(new ArrayList<>());
                return empty;
            }
            return toRoadmapListResponse(roadmapRepository.findByInternKeycloakUserId(kc));
        }

        Long effectiveMentorId = mentorId;
        if (isMentor() && !isHrOrAdmin() && Objects.isNull(effectiveMentorId)) {
            effectiveMentorId = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        }

        String internKeycloak = null;
        if (Objects.nonNull(internId)) {
            internKeycloak = userRepository.findById(internId)
                    .map(UserEntity::getKeycloakUserId)
                    .orElse(null);
        }

        Specification<RoadmapEntity> spec = buildRoadmapSpec(programId, effectiveMentorId, internKeycloak);
        return toRoadmapListResponse(roadmapRepository.findAll(spec));
    }

    @Transactional(readOnly = true)
    public RoadmapListResponse listMyInternships() {
        Long uid = currentUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        String sub = currentUserResolver.resolveCurrentKeycloakSubject().orElse(null);

        Set<Long> seen = new LinkedHashSet<>();
        List<RoadmapEntity> out = new ArrayList<>();
        for (RoadmapEntity r : roadmapRepository.findByMentorId(uid)) {
            if (seen.add(r.getId())) {
                out.add(r);
            }
        }
        if (StringUtils.isNotBlank(sub)) {
            for (RoadmapEntity r : roadmapRepository.findByInternKeycloakUserId(sub)) {
                if (seen.add(r.getId())) {
                    out.add(r);
                }
            }
        }
        return toRoadmapListResponse(out);
    }

    @Transactional(readOnly = true)
    public RoadmapResponse getInternshipById(String internshipId) {
        return roadmapMapper.toRoadmapResponse(requireRoadmapViewable(parseLongId(internshipId, "internshipId")));
    }

    @Transactional(readOnly = true)
    public StageListResponse getInternshipStages(String internshipId) {
        long rid = parseLongId(internshipId, "internshipId");
        requireRoadmapViewable(rid);
        List<RoadmapStageEntity> stages = roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(rid);
        return roadmapMapper.toStageListResponse(stages);
    }

    @Transactional
    public StageResponse createInternshipStage(String internshipId, CreateStageRequest request) {
        RoadmapEntity roadmap = requireRoadmapEditable(parseLongId(internshipId, "internshipId"));
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
        currentUserResolver.resolveCurrentUserDatabaseId().ifPresent(uid ->
                                                                             userRepository.findById(uid)
                                                                                     .ifPresent(stage::setCreatedBy));
        RoadmapStageEntity saved = roadmapStageRepository.save(stage);
        return roadmapMapper.toStageResponse(saved);
    }

    @Transactional
    public StageResponse updateInternshipStage(String internshipId, String stageId, UpdateStageRequest request) {
        long rid = parseLongId(internshipId, "internshipId");
        long sid = parseLongId(stageId, "stageId");
        requireRoadmapEditable(rid);
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
    public MessageResponse deleteInternshipStage(String internshipId, String stageId) {
        long rid = parseLongId(internshipId, "internshipId");
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
    public MessageResponse reorderInternshipStages(String internshipId, ReorderStagesRequest request) {
        long rid = parseLongId(internshipId, "internshipId");
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
    public StageResponse changeInternshipStageStatus(String internshipId, String stageId,
                                                     ChangeStageStatusRequest request) {
        long rid = parseLongId(internshipId, "internshipId");
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
    public RoadmapResponse createInternship(CreateRoadmapRequest request) {
        InternshipProgramEntity program = internshipProgramRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        RoadmapEntity entity = new RoadmapEntity();
        entity.setProgram(program);
        entity.setTitle(request.getTitle());
        entity.setDescription(request.getDescription());
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setStatus(RoadmapLifecycleStatus.DRAFT);
        if (Objects.nonNull(request.getMentorId())) {
            UserEntity mentor = userRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
            entity.setMentor(mentor);
        }
        RoadmapEntity saved = roadmapRepository.save(entity);
        replaceInterns(saved, request.getInternIds());
        saved = roadmapRepository.save(saved);
        return roadmapMapper.toRoadmapResponse(
                roadmapRepository.findWithMentorAndInternsById(saved.getId()).orElse(saved));
    }

    @Transactional
    public RoadmapResponse updateInternship(String id, UpdateRoadmapRequest request) {
        RoadmapEntity entity = requireRoadmapEditable(parseLongId(id, "internshipId"));
        if (Objects.nonNull(request.getTitle())) {
            entity.setTitle(request.getTitle());
        }
        if (Objects.nonNull(request.getDescription())) {
            entity.setDescription(request.getDescription());
        }
        if (Objects.nonNull(request.getStartDate())) {
            entity.setStartDate(request.getStartDate());
        }
        if (Objects.nonNull(request.getEndDate())) {
            entity.setEndDate(request.getEndDate());
        }
        if (Objects.nonNull(request.getStatus())) {
            entity.setStatus(RoadmapLifecycleStatus.fromString(request.getStatus().getValue()));
        }
        if (Objects.nonNull(request.getMentorId())) {
            UserEntity mentor = userRepository.findById(request.getMentorId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
            entity.setMentor(mentor);
        }
        if (Objects.nonNull(request.getInternIds())) {
            replaceInterns(entity, request.getInternIds());
        }
        roadmapRepository.save(entity);
        return roadmapMapper.toRoadmapResponse(
                roadmapRepository.findWithMentorAndInternsById(entity.getId()).orElse(entity));
    }

    @Transactional
    public MessageResponse deleteInternship(String id) {
        long rid = parseLongId(id, "internshipId");
        requireRoadmapEditable(rid);
        roadmapRepository.deleteById(rid);
        MessageResponse m = new MessageResponse();
        m.setMessage("Internship deleted");
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
        String sub = currentUserResolver.resolveCurrentKeycloakSubject().orElse(null);
        if (Objects.nonNull(uid)
                && Objects.nonNull(r.getMentor())
                && uid.equals(r.getMentor().getId())) {
            return true;
        }
        if (StringUtils.isNotBlank(sub)) {
            return r.getInterns().stream().anyMatch(i -> sub.equals(i.getKeycloakUserId()));
        }
        return false;
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

    private static Specification<RoadmapEntity> buildRoadmapSpec(Long programId, Long mentorId, String internKeycloak) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> parts = new ArrayList<>();
            if (Objects.nonNull(programId)) {
                parts.add(cb.equal(root.get("program").get("id"), programId));
            }
            if (Objects.nonNull(mentorId)) {
                parts.add(cb.equal(root.get("mentor").get("id"), mentorId));
            }
            if (StringUtils.isNotBlank(internKeycloak)) {
                var join = root.join("interns", JoinType.INNER);
                parts.add(cb.equal(join.get("keycloakUserId"), internKeycloak));
            }
            if (parts.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(parts.toArray(Predicate[]::new));
        };
    }

    private void replaceInterns(RoadmapEntity roadmap, List<String> internIds) {
        roadmap.getInterns().clear();
        if (Objects.isNull(internIds)) {
            return;
        }
        for (String kid : internIds) {
            if (StringUtils.isBlank(kid)) {
                continue;
            }
            RoadmapInternEntity ri = new RoadmapInternEntity();
            ri.setRoadmap(roadmap);
            ri.setKeycloakUserId(kid.trim());
            roadmap.getInterns().add(ri);
        }
    }

    private RoadmapListResponse toRoadmapListResponse(List<RoadmapEntity> entities) {
        List<RoadmapEntity> loaded = new ArrayList<>();
        for (RoadmapEntity entity : entities) {
            loaded.add(roadmapRepository.findWithMentorAndInternsById(entity.getId()).orElse(entity));
        }
        return roadmapMapper.toRoadmapListResponse(loaded);
    }

    private static long parseLongId(String raw, String label) {
        try {
            return Long.parseLong(raw);
        }
        catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + label);
        }
    }
}
