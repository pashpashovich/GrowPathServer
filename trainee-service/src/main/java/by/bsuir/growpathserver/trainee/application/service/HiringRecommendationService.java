package by.bsuir.growpathserver.trainee.application.service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.dto.InternProgressDto;
import by.bsuir.growpathserver.trainee.domain.entity.AssessmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.HiringDecisionType;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HiringRecommendationService {

    private static final double HIRE_RATING_THRESHOLD = 4.0;
    private static final double RESERVE_RATING_THRESHOLD = 3.5;
    private static final int HIRE_PROGRESS_THRESHOLD = 80;
    private static final int RESERVE_PROGRESS_THRESHOLD = 60;

    private final IprRepository iprRepository;
    private final InternProgressCalculationService progressCalculationService;
    private final AssessmentRepository assessmentRepository;

    public record Recommendation(HiringDecisionType decision, String reason) {
    }

    @Transactional(readOnly = true)
    public Recommendation recommend(Long internId, Long programId) {
        IprEntity ipr = iprRepository.findByInternId(internId).stream()
                .filter(entity -> Objects.equals(entity.getProgram().getId(), programId))
                .max(Comparator.comparing(IprEntity::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .orElseThrow(() -> new IllegalArgumentException(
                        "Intern has no individual development plan on this internship program"));

        InternProgressDto progress = progressCalculationService.calculateProgress(ipr.getId());
        int progressPercent = progress.getOverallProgress() != null
                ? progress.getOverallProgress().intValue()
                : 0;
        double overallRating = resolveOverallRating(internId, programId);

        if (progressPercent >= HIRE_PROGRESS_THRESHOLD && overallRating >= HIRE_RATING_THRESHOLD) {
            return new Recommendation(
                    HiringDecisionType.RECOMMENDED_FOR_HIRE,
                    "ИПР выполнен на " + progressPercent + "%, средняя оценка "
                            + formatRating(overallRating) + " — рекомендуется к найму");
        }
        if (progressPercent >= RESERVE_PROGRESS_THRESHOLD && overallRating >= RESERVE_RATING_THRESHOLD) {
            return new Recommendation(
                    HiringDecisionType.TALENT_RESERVE,
                    "ИПР выполнен на " + progressPercent + "%, средняя оценка "
                            + formatRating(overallRating) + " — рекомендуется в кадровый резерв");
        }
        if (progressPercent < RESERVE_PROGRESS_THRESHOLD || overallRating < RESERVE_RATING_THRESHOLD) {
            return new Recommendation(
                    HiringDecisionType.ADDITIONAL_ASSESSMENT,
                    "Показатели эффективности (" + progressPercent + "%, оценка "
                            + formatRating(overallRating) + ") недостаточны для однозначного вывода — "
                            + "рекомендуется дополнительная оценка");
        }
        return new Recommendation(
                HiringDecisionType.COMPLETED_WITHOUT_HIRE,
                "Показатели (" + progressPercent + "%, оценка " + formatRating(overallRating)
                        + ") ниже порога для найма или резерва");
    }

    private double resolveOverallRating(Long internId, Long programId) {
        List<AssessmentEntity> assessments = assessmentRepository.findByInternIdOrderByUpdatedAtAsc(internId);
        return assessments.stream()
                .filter(assessment -> Objects.equals(assessment.getInternshipId(), programId))
                .map(AssessmentEntity::getOverallRating)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private static String formatRating(double rating) {
        return String.format("%.1f", rating);
    }
}
