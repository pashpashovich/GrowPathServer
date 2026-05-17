package by.bsuir.growpathserver.trainee.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.InternProgressDto;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.ArtifactLink;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.AssessmentRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.CompetencyRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.CompletedTaskRow;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.InternInfo;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.IprInfo;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.ProgramInfo;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.ProgressInfo;
import by.bsuir.growpathserver.trainee.application.dto.report.InternshipResultReportData.RatingInfo;
import by.bsuir.growpathserver.trainee.application.exception.InternIprNotFoundForReportException;
import by.bsuir.growpathserver.trainee.application.exception.InternNotFoundForReportException;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.CompetencyEntity;
import by.bsuir.growpathserver.trainee.domain.entity.InternshipProgramEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.TaskStatus;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.InternshipProgramRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskArtifactRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipResultReportAggregationService {

    public static final String REPORT_ID = "GP-RPT-1";

    private final UserRepository userRepository;
    private final IprRepository iprRepository;
    private final InternshipProgramRepository internshipProgramRepository;
    private final InternProgressCalculationService progressCalculationService;
    private final InternCompetencyProfileService competencyProfileService;
    private final AssessmentRepository assessmentRepository;
    private final TaskRepository taskRepository;
    private final TaskArtifactRepository taskArtifactRepository;
    private final CurrentApplicationUserResolver currentApplicationUserResolver;

    @Transactional(readOnly = true)
    public InternshipResultReportData aggregate(Long internId) {
        UserEntity internEntity = userRepository.findById(internId)
                .orElseThrow(() -> new InternNotFoundForReportException(internId));
        if (internEntity.getRole() != UserRole.INTERN) {
            throw new InternNotFoundForReportException(internId);
        }

        IprEntity ipr = resolveIpr(internId);
        InternshipProgramEntity program = internshipProgramRepository.findWithCollectionsById(ipr.getProgram().getId())
                .orElse(ipr.getProgram());

        InternProgressDto progress = progressCalculationService.calculateProgress(ipr.getId());
        Map<Long, Double> competencyProfile = competencyProfileService.buildNormalizedProfile(internId);

        List<CompetencyRow> competencyRows = program.getCompetencies().stream()
                .sorted(Comparator.comparing(CompetencyEntity::getName, String.CASE_INSENSITIVE_ORDER))
                .map(competency -> new CompetencyRow(
                        competency.getName(),
                        competencyProfileService.toAchievedLevelOutOfFive(competencyProfile, competency.getId())
                ))
                .toList();

        List<CompletedTaskRow> completedTasks = buildCompletedTasks(internId);
        List<AssessmentRow> assessments = buildAssessments(internId);

        RatingInfo ratingInfo = buildRatingInfo(assessments, ipr);

        String generatedByName = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .flatMap(userRepository::findById)
                .map(user -> User.fromEntity(user).getDisplayName())
                .orElse("Система");

        LocalDate programEnd = program.getStartDate().plusMonths(program.getDuration());

        return new InternshipResultReportData(
                REPORT_ID,
                LocalDateTime.now(),
                generatedByName,
                new InternInfo(internId, User.fromEntity(internEntity).getDisplayName()),
                new ProgramInfo(
                        program.getId(),
                        program.getTitle(),
                        program.getItDirection() != null ? program.getItDirection().getDisplayName() : "—",
                        program.getStartDate(),
                        programEnd
                ),
                new IprInfo(
                        ipr.getId(),
                        ipr.getTitle(),
                        ipr.getStartDate(),
                        ipr.getEndDate(),
                        ipr.getStatus().getValue()
                ),
                new ProgressInfo(
                        progress.getOverallProgress() != null ? progress.getOverallProgress() : 0.0,
                        progress.getAverageTaskRating() != null ? progress.getAverageTaskRating() : 0.0,
                        progress.getCompletedTasks() != null ? progress.getCompletedTasks() : 0,
                        progress.getTotalTasks() != null ? progress.getTotalTasks() : 0
                ),
                ratingInfo,
                competencyRows,
                completedTasks,
                assessments
        );
    }

    private IprEntity resolveIpr(Long internId) {
        return iprRepository.findActiveByInternId(internId)
                .or(() -> iprRepository.findByInternId(internId).stream()
                        .max(Comparator.comparing(IprEntity::getEndDate)
                                     .thenComparing(IprEntity::getId)))
                .orElseThrow(() -> new InternIprNotFoundForReportException(internId));
    }

    private List<CompletedTaskRow> buildCompletedTasks(Long internId) {
        return taskRepository.findByAssigneeIdAndStatus(internId, TaskStatus.COMPLETED).stream()
                .sorted(Comparator.comparing(TaskEntity::getCompletedAt,
                                             Comparator.nullsLast(Comparator.reverseOrder()))
                                .thenComparing(TaskEntity::getId, Comparator.reverseOrder()))
                .map(task -> {
                    List<ArtifactLink> artifacts = taskArtifactRepository.findAllByTaskId(task.getId()).stream()
                            .map(artifact -> new ArtifactLink(artifact.getName(), artifact.getUrl()))
                            .toList();
                    return new CompletedTaskRow(
                            task.getTitle(),
                            task.getCompletedAt(),
                            task.getRating(),
                            task.getReviewComment(),
                            artifacts
                    );
                })
                .toList();
    }

    private List<AssessmentRow> buildAssessments(Long internId) {
        List<AssessmentRow> rows = new ArrayList<>();
        for (AssessmentEntity assessment : assessmentRepository.findByInternIdOrderByUpdatedAtAsc(internId)) {
            String mentorName = userRepository.findById(assessment.getMentorId())
                    .map(entity -> User.fromEntity(entity).getDisplayName())
                    .orElse("—");
            rows.add(new AssessmentRow(
                    assessment.getCreatedAt(),
                    assessment.getOverallRating(),
                    assessment.getQualityRating(),
                    assessment.getSpeedRating(),
                    assessment.getCommunicationRating(),
                    assessment.getComment(),
                    mentorName
            ));
        }
        return rows;
    }

    private RatingInfo buildRatingInfo(List<AssessmentRow> assessments, IprEntity ipr) {
        AssessmentRow latest = assessments.isEmpty() ? null : assessments.get(assessments.size() - 1);
        String mentorName = ipr.getMentor() != null ? User.fromEntity(ipr.getMentor()).getDisplayName() : null;
        if (latest != null && latest.mentorName() != null) {
            mentorName = latest.mentorName();
        }
        Double overall = latest != null ? latest.overallRating() : null;
        return new RatingInfo(overall, mentorName);
    }
}
