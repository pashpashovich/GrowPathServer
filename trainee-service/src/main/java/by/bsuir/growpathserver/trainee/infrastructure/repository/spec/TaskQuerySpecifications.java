package by.bsuir.growpathserver.trainee.infrastructure.repository.spec;

import org.springframework.data.jpa.domain.Specification;

import by.bsuir.growpathserver.trainee.domain.entity.IprStageEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import jakarta.persistence.criteria.Subquery;

public final class TaskQuerySpecifications {

    private TaskQuerySpecifications() {
    }

    public static Specification<TaskEntity> belongsToInternshipProgram(Long programId) {
        return (root, query, cb) -> {
            var directMatch = cb.equal(root.get("internshipId"), programId);

            Subquery<Long> stageSubquery = query.subquery(Long.class);
            var stageRoot = stageSubquery.from(IprStageEntity.class);
            var iprJoin = stageRoot.join("ipr");
            stageSubquery.select(stageRoot.get("id"))
                    .where(cb.equal(iprJoin.get("program").get("id"), programId));

            var viaStage = root.get("stageId").in(stageSubquery);
            return cb.or(directMatch, viaStage);
        };
    }

    public static Specification<TaskEntity> belongsToIpr(Long iprId) {
        return (root, query, cb) -> {
            Subquery<Long> stageSubquery = query.subquery(Long.class);
            var stageRoot = stageSubquery.from(IprStageEntity.class);
            stageSubquery.select(stageRoot.get("id"))
                    .where(cb.equal(stageRoot.get("ipr").get("id"), iprId));

            return root.get("stageId").in(stageSubquery);
        };
    }
}
