package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDate;
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
import by.bsuir.growpathserver.dto.model.CreateIprRequest;
import by.bsuir.growpathserver.dto.model.CreateStageRequest;
import by.bsuir.growpathserver.dto.model.InternProgressResponse;
import by.bsuir.growpathserver.dto.model.InternProgressResponseStageProgressInner;
import by.bsuir.growpathserver.dto.model.IprListResponse;
import by.bsuir.growpathserver.dto.model.IprResponse;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ReorderStagesRequest;
import by.bsuir.growpathserver.dto.model.StageListResponse;
import by.bsuir.growpathserver.dto.model.StageResponse;
import by.bsuir.growpathserver.dto.model.UpdateIprRequest;
import by.bsuir.growpathserver.dto.model.UpdateStageRequest;
import by.bsuir.growpathserver.trainee.application.dto.InternProgressDto;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprStageEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapEntity;
import by.bsuir.growpathserver.trainee.domain.entity.RoadmapStageEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapLifecycleStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.RoadmapStageStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.StagePriorityLevel;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprStageRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RoadmapRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.RoadmapStageRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IprApplicationFacade {

    private final IprRepository iprRepository;
    private final IprStageRepository iprStageRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapStageRepository roadmapStageRepository;
    private final UserRepository userRepository;
    private final CurrentApplicationUserResolver currentUserResolver;
    private final InternProgressCalculationService progressCalculationService;

    @Transactional(readOnly = true)
    public IprListResponse listIprs(Long mentorId, Long internId, Long programId, Long templateId) {
        if (isIntern() && !isHrOrAdmin()) {
            Long currentInternId = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
            if (Objects.isNull(currentInternId)) {
                return emptyIprListResponse();
            }
            List<IprEntity> visible = iprRepository.findByInternId(currentInternId).stream()
                    .filter(this::isActiveForIntern)
                    .collect(Collectors.toList());
            return toIprListResponse(visible);
        }

        Long effectiveMentorId = mentorId;
        if (isMentor() && !isHrOrAdmin() && Objects.isNull(effectiveMentorId)) {
            effectiveMentorId = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        }
        Specification<IprEntity> spec = buildIprSpec(programId, effectiveMentorId, internId, templateId);
        List<IprEntity> listed = iprRepository.findAll(spec).stream().filter(this::canView)
                .collect(Collectors.toList());
        return toIprListResponse(listed);
    }

    @Transactional(readOnly = true)
    public IprResponse getMyIpr() {
        if (!isIntern()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        Long internId = currentUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        IprEntity ipr = iprRepository.findActiveByInternId(internId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active IPR not found"));
        return toIprResponse(ipr);
    }

    @Transactional(readOnly = true)
    public IprResponse getIprById(String iprId) {
        return toIprResponse(requireIprViewable(parseLongId(iprId, "iprId")));
    }

    @Transactional
    public IprResponse createIpr(CreateIprRequest request) {
        if (Objects.isNull(request.getProgramId())
                || Objects.isNull(request.getTemplateId())
                || Objects.isNull(request.getInternId())
                || Objects.isNull(request.getStartDate())
                || Objects.isNull(request.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                              "programId, templateId, internId, startDate, endDate are required");
        }
        InternshipProgramEntity program = internshipProgramRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Program not found"));
        RoadmapEntity template = roadmapRepository.findWithMentorAndInternsById(request.getTemplateId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roadmap template not found"));
        UserEntity intern = userRepository.findById(request.getInternId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intern not found"));
        if (!Objects.equals(template.getProgram().getId(), program.getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template does not belong to program");
        }

        IprEntity ipr = new IprEntity();
        ipr.setProgram(program);
        ipr.setRoadmapTemplate(template);
        ipr.setIntern(intern);
        ipr.setTitle(Objects.nonNull(request.getTitle()) ? request.getTitle() : template.getTitle());
        ipr.setDescription(
                Objects.nonNull(request.getDescription()) ? request.getDescription() : template.getDescription());
        ipr.setStartDate(request.getStartDate());
        ipr.setEndDate(request.getEndDate());
        ipr.setStatus(RoadmapLifecycleStatus.DRAFT);
        if (Objects.nonNull(request.getMentorId())) {
            ipr.setMentor(userRepository.findById(request.getMentorId())
                                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                                                 "Mentor not found")));
        }
        else {
            ipr.setMentor(template.getMentor());
        }
        validateIprDateRange(ipr.getStartDate(), ipr.getEndDate());
        IprEntity saved = iprRepository.save(ipr);
        copyTemplateStages(saved, template);
        return toIprResponse(iprRepository.findLoadedById(saved.getId()).orElse(saved));
    }

    @Transactional
    public IprResponse updateIpr(String iprId, UpdateIprRequest request) {
        IprEntity ipr = requireIprEditable(parseLongId(iprId, "iprId"));
        if (Objects.nonNull(request.getTitle())) {
            ipr.setTitle(request.getTitle());
        }
        if (Objects.nonNull(request.getDescription())) {
            ipr.setDescription(request.getDescription());
        }
        if (Objects.nonNull(request.getStartDate())) {
            ipr.setStartDate(request.getStartDate());
        }
        if (Objects.nonNull(request.getEndDate())) {
            ipr.setEndDate(request.getEndDate());
        }
        validateIprDateRange(ipr.getStartDate(), ipr.getEndDate());
        if (Objects.nonNull(request.getMentorId())) {
            ipr.setMentor(userRepository.findById(request.getMentorId())
                                  .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                                                                 "Mentor not found")));
        }
        if (Objects.nonNull(request.getStatus())) {
            RoadmapLifecycleStatus target = RoadmapLifecycleStatus.fromString(String.valueOf(request.getStatus()));
            if (target == RoadmapLifecycleStatus.ACTIVE) {
                validateIprActivation(ipr);
            }
            ipr.setStatus(target);
        }
        return toIprResponse(iprRepository.save(ipr));
    }

    @Transactional
    public MessageResponse deleteIpr(String iprId) {
        long id = parseLongId(iprId, "iprId");
        requireIprEditable(id);
        iprRepository.deleteById(id);
        MessageResponse response = new MessageResponse();
        response.setMessage("IPR deleted");
        return response;
    }

    @Transactional(readOnly = true)
    public StageListResponse getIprStages(String iprId) {
        long id = parseLongId(iprId, "iprId");
        requireIprViewable(id);
        return toStageListResponse(iprStageRepository.findByIprIdOrderByStageOrderAsc(id));
    }

    @Transactional
    public StageResponse createIprStage(String iprId, CreateStageRequest request) {
        IprEntity ipr = requireIprEditable(parseLongId(iprId, "iprId"));
        validateStageDateRange(ipr, request.getStartDate(), request.getEndDate());
        IprStageEntity stage = new IprStageEntity();
        stage.setIpr(ipr);
        stage.setTitle(request.getTitle());
        stage.setDescription(request.getDescription());
        stage.setStartDate(request.getStartDate());
        stage.setEndDate(request.getEndDate());
        stage.setStatus(RoadmapStageStatus.PENDING);
        if (Objects.nonNull(request.getPriority())) {
            stage.setPriority(StagePriorityLevel.fromString(request.getPriority().getValue()));
        }
        stage.setCheckpoint(Boolean.TRUE.equals(request.getIsCheckpoint()));
        stage.setComments(request.getComments());
        int nextOrder = Objects.nonNull(request.getOrder()) ? request.getOrder() : nextStageOrder(ipr.getId());
        stage.setStageOrder(nextOrder);
        currentUserResolver.resolveCurrentUserDatabaseId().flatMap(userRepository::findById)
                .ifPresent(stage::setCreatedBy);
        return toStageResponse(iprStageRepository.save(stage));
    }

    @Transactional
    public StageResponse updateIprStage(String iprId, String stageId, UpdateStageRequest request) {
        IprEntity ipr = requireIprEditable(parseLongId(iprId, "iprId"));
        IprStageEntity stage = iprStageRepository.findByIdAndIprId(parseLongId(stageId, "stageId"), ipr.getId())
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
        validateStageDateRange(ipr, stage.getStartDate(), stage.getEndDate());
        if (Objects.nonNull(request.getPriority())) {
            stage.setPriority(StagePriorityLevel.fromString(request.getPriority().getValue()));
        }
        if (Objects.nonNull(request.getIsCheckpoint())) {
            stage.setCheckpoint(request.getIsCheckpoint());
        }
        if (Objects.nonNull(request.getComments())) {
            stage.setComments(request.getComments());
        }
        return toStageResponse(iprStageRepository.save(stage));
    }

    @Transactional
    public MessageResponse deleteIprStage(String iprId, String stageId) {
        IprEntity ipr = requireIprEditable(parseLongId(iprId, "iprId"));
        IprStageEntity stage = iprStageRepository.findByIdAndIprId(parseLongId(stageId, "stageId"), ipr.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        iprStageRepository.delete(stage);
        renumberStages(ipr.getId());
        MessageResponse response = new MessageResponse();
        response.setMessage("Stage deleted");
        return response;
    }

    @Transactional
    public MessageResponse reorderIprStages(String iprId, ReorderStagesRequest request) {
        IprEntity ipr = requireIprEditable(parseLongId(iprId, "iprId"));
        List<Long> ids = request.getStageIds();
        for (int i = 0; i < ids.size(); i++) {
            IprStageEntity stage = iprStageRepository.findByIdAndIprId(ids.get(i), ipr.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST));
            stage.setStageOrder(i);
        }
        MessageResponse response = new MessageResponse();
        response.setMessage("Stages reordered");
        return response;
    }

    @Transactional
    public StageResponse changeIprStageStatus(String iprId, String stageId, ChangeStageStatusRequest request) {
        IprEntity ipr = requireIprEditable(parseLongId(iprId, "iprId"));
        IprStageEntity stage = iprStageRepository.findByIdAndIprId(parseLongId(stageId, "stageId"), ipr.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        stage.setStatus(RoadmapStageStatus.fromString(request.getStatus().getValue()));
        if (Objects.nonNull(request.getComments())) {
            stage.setComments(request.getComments());
        }
        return toStageResponse(iprStageRepository.save(stage));
    }

    private void copyTemplateStages(IprEntity ipr, RoadmapEntity template) {
        List<RoadmapStageEntity> templateStages = roadmapStageRepository.findByRoadmapIdOrderByStageOrderAsc(
                template.getId());
        for (RoadmapStageEntity source : templateStages) {
            IprStageEntity stage = new IprStageEntity();
            stage.setIpr(ipr);
            stage.setTemplateStage(source);
            stage.setTitle(source.getTitle());
            stage.setDescription(source.getDescription());
            stage.setStartDate(source.getStartDate());
            stage.setEndDate(source.getEndDate());
            stage.setStatus(RoadmapStageStatus.PENDING);
            stage.setPriority(source.getPriority());
            stage.setCheckpoint(source.isCheckpoint());
            stage.setComments(source.getComments());
            stage.setStageOrder(source.getStageOrder());
            validateStageDateRange(ipr, stage.getStartDate(), stage.getEndDate());
            iprStageRepository.save(stage);
        }
    }

    private IprEntity requireIprViewable(long iprId) {
        IprEntity ipr = iprRepository.findLoadedById(iprId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canView(ipr)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ipr;
    }

    private IprEntity requireIprEditable(long iprId) {
        IprEntity ipr = iprRepository.findLoadedById(iprId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!canEdit(ipr)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        return ipr;
    }

    private boolean canView(IprEntity ipr) {
        if (isHrOrAdmin()) {
            return true;
        }
        Long uid = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        if (Objects.isNull(uid)) {
            return false;
        }
        if (Objects.nonNull(ipr.getMentor()) && uid.equals(ipr.getMentor().getId())) {
            return true;
        }
        if (Objects.nonNull(ipr.getIntern()) && uid.equals(ipr.getIntern().getId())) {
            return isActiveForIntern(ipr);
        }
        return false;
    }

    private boolean canEdit(IprEntity ipr) {
        if (isHrOrAdmin()) {
            return true;
        }
        if (isIntern()) {
            return false;
        }
        Long uid = currentUserResolver.resolveCurrentUserDatabaseId().orElse(null);
        return Objects.nonNull(uid) && Objects.nonNull(ipr.getMentor()) && uid.equals(ipr.getMentor().getId());
    }

    private boolean isActiveForIntern(IprEntity ipr) {
        return ipr.getStatus() == RoadmapLifecycleStatus.ACTIVE;
    }

    private void validateIprDateRange(LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR dates are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "IPR startDate must be before endDate");
        }
    }

    private void validateStageDateRange(IprEntity ipr, LocalDate startDate, LocalDate endDate) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stage dates are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stage startDate must be before endDate");
        }
        if (startDate.isBefore(ipr.getStartDate()) || endDate.isAfter(ipr.getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Stage dates must be within IPR period");
        }
    }

    private void validateIprActivation(IprEntity ipr) {
        if (Objects.isNull(ipr.getMentor())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mentor must be assigned before activation");
        }
        if (Objects.isNull(ipr.getIntern())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Intern must be assigned before activation");
        }
        List<IprStageEntity> stages = iprStageRepository.findByIprIdOrderByStageOrderAsc(ipr.getId());
        if (stages.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one stage is required for activation");
        }
        for (IprStageEntity stage : stages) {
            validateStageDateRange(ipr, stage.getStartDate(), stage.getEndDate());
        }
    }

    private int nextStageOrder(long iprId) {
        return iprStageRepository.findByIprIdOrderByStageOrderAsc(iprId).stream()
                .mapToInt(IprStageEntity::getStageOrder)
                .max()
                .orElse(-1) + 1;
    }

    private void renumberStages(long iprId) {
        List<IprStageEntity> stages = iprStageRepository.findByIprIdOrderByStageOrderAsc(iprId);
        for (int i = 0; i < stages.size(); i++) {
            stages.get(i).setStageOrder(i);
        }
    }

    private boolean hasAuthority(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth)) {
            return false;
        }
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
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

    private static Specification<IprEntity> buildIprSpec(Long programId,
                                                         Long mentorId,
                                                         Long internId,
                                                         Long templateId) {
        return (root, query, cb) -> {
            query.distinct(true);
            List<Predicate> parts = new ArrayList<>();
            if (Objects.nonNull(programId)) {
                parts.add(cb.equal(root.get("program").get("id"), programId));
            }
            if (Objects.nonNull(mentorId)) {
                parts.add(cb.equal(root.get("mentor").get("id"), mentorId));
            }
            if (Objects.nonNull(internId)) {
                parts.add(cb.equal(root.get("intern").get("id"), internId));
            }
            if (Objects.nonNull(templateId)) {
                parts.add(cb.equal(root.get("roadmapTemplate").get("id"), templateId));
            }
            if (parts.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(parts.toArray(Predicate[]::new));
        };
    }

    private IprResponse toIprResponse(IprEntity ipr) {
        IprResponse response = new IprResponse();
        response.setId(ipr.getId());
        response.setProgramId(ipr.getProgram().getId());
        response.setTemplateId(ipr.getRoadmapTemplate().getId());
        response.setTitle(ipr.getTitle());
        response.setDescription(ipr.getDescription());
        response.setStartDate(ipr.getStartDate());
        response.setEndDate(ipr.getEndDate());
        response.setStatus(IprResponse.StatusEnum.fromValue(ipr.getStatus().getValue()));
        response.setMentorId(Objects.nonNull(ipr.getMentor()) ? ipr.getMentor().getId() : null);
        response.setInternId(ipr.getIntern().getId());
        response.setCreatedAt(ipr.getCreatedAt());
        response.setUpdatedAt(ipr.getUpdatedAt());
        return response;
    }

    private IprListResponse toIprListResponse(List<IprEntity> iprs) {
        IprListResponse response = new IprListResponse();
        List<Object> data = new ArrayList<>();
        for (IprEntity ipr : iprs) {
            data.add(toIprResponse(ipr));
        }
        response.setData(data);
        return response;
    }

    private StageResponse toStageResponse(IprStageEntity stage) {
        StageResponse response = new StageResponse();
        response.setId(stage.getId());
        response.setRoadmapId(stage.getIpr().getId());
        response.setTitle(stage.getTitle());
        response.setDescription(stage.getDescription());
        response.setStartDate(stage.getStartDate());
        response.setEndDate(stage.getEndDate());
        response.setStatus(StageResponse.StatusEnum.fromValue(stage.getStatus().getValue()));
        response.setPriority(Objects.nonNull(stage.getPriority())
                                     ? StageResponse.PriorityEnum.fromValue(stage.getPriority().getValue())
                                     : null);
        response.setIsCheckpoint(stage.isCheckpoint());
        response.setComments(stage.getComments());
        response.setOrder(stage.getStageOrder());
        response.setCreatedBy(Objects.nonNull(stage.getCreatedBy()) ? stage.getCreatedBy().getId() : null);
        response.setCreatedAt(stage.getCreatedAt());
        response.setUpdatedAt(stage.getUpdatedAt());
        return response;
    }

    private StageListResponse toStageListResponse(List<IprStageEntity> stages) {
        StageListResponse response = new StageListResponse();
        List<Object> data = new ArrayList<>();
        for (IprStageEntity stage : stages) {
            data.add(toStageResponse(stage));
        }
        response.setData(data);
        return response;
    }

    private static long parseLongId(String raw, String label) {
        try {
            return Long.parseLong(raw);
        }
        catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid " + label);
        }
    }

    private IprListResponse emptyIprListResponse() {
        IprListResponse response = new IprListResponse();
        response.setData(new ArrayList<>());
        return response;
    }

    @Transactional(readOnly = true)
    public InternProgressResponse calculateIprProgress(String iprId) {
        long id = parseLongId(iprId, "iprId");
        requireIprViewable(id);

        InternProgressDto progressDto = progressCalculationService.calculateProgress(id);

        InternProgressResponse response = new InternProgressResponse();
        response.setIprId(progressDto.getIprId());
        response.setInternId(progressDto.getInternId());
        response.setOverallProgress(progressDto.getOverallProgress());
        response.setCompletedTasks(progressDto.getCompletedTasks());
        response.setTotalTasks(progressDto.getTotalTasks());
        response.setCompletedStages(progressDto.getCompletedStages());
        response.setTotalStages(progressDto.getTotalStages());
        response.setStatus(InternProgressResponse.StatusEnum.fromValue(progressDto.getStatus().name()));
        response.setEstimatedCompletionDate(progressDto.getEstimatedCompletionDate());
        response.setPlannedEndDate(progressDto.getPlannedEndDate());
        response.setAverageTaskRating(progressDto.getAverageTaskRating());

        List<InternProgressResponseStageProgressInner> stageProgressItems = progressDto.getStageProgress().stream()
                .map(this::toStageProgressItem)
                .collect(Collectors.toList());
        response.setStageProgress(stageProgressItems);

        return response;
    }

    private InternProgressResponseStageProgressInner toStageProgressItem(InternProgressDto.StageProgressDto dto) {
        InternProgressResponseStageProgressInner item = new InternProgressResponseStageProgressInner();
        item.setStageId(dto.getStageId());
        item.setStageTitle(dto.getStageTitle());
        item.setProgressPercentage(dto.getProgressPercentage());
        item.setCompletedTasks(dto.getCompletedTasks());
        item.setTotalTasks(dto.getTotalTasks());
        item.setStartDate(dto.getStartDate());
        item.setEndDate(dto.getEndDate());
        item.setStatus(dto.getStatus());
        return item;
    }
}
