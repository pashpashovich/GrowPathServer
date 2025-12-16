package by.bsuir.growpathserver.trainee.application.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.query.GetMentorByIdQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorInternsQuery;
import by.bsuir.growpathserver.trainee.application.query.GetMentorsQuery;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.domain.valueobject.UserRole;
import by.bsuir.growpathserver.trainee.infrastructure.repository.AssessmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MentorServiceImpl implements MentorService {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AssessmentRepository assessmentRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<User> getMentors(GetMentorsQuery query) {
        Specification<UserEntity> spec = (root, criteriaQuery, criteriaBuilder) -> {
            Predicate rolePredicate = criteriaBuilder.equal(root.get("role"), UserRole.MENTOR);
            Predicate finalPredicate = rolePredicate;

            if (query.search() != null && !query.search().isBlank()) {
                String searchPattern = "%" + query.search().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), searchPattern);
                Predicate searchPredicate = criteriaBuilder.or(namePredicate, emailPredicate);
                finalPredicate = criteriaBuilder.and(rolePredicate, searchPredicate);
            }

            return finalPredicate;
        };

        int page = query.page() != null && query.page() > 0 ? query.page() - 1 : 0;
        int limit = query.limit() != null && query.limit() > 0 ? query.limit() : 10;
        Pageable pageable = PageRequest.of(page, limit);

        Page<UserEntity> entities = userRepository.findAll(spec, pageable);
        return entities.map(User::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public User getMentorById(GetMentorByIdQuery query) {
        UserEntity entity = userRepository.findById(query.mentorId())
                .orElseThrow(() -> new NoSuchElementException("Mentor not found with id: " + query.mentorId()));

        if (entity.getRole() != UserRole.MENTOR) {
            throw new IllegalArgumentException("User with id " + query.mentorId() + " is not a mentor");
        }

        return User.fromEntity(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getMentorInterns(GetMentorInternsQuery query) {
        UserEntity mentorEntity = userRepository.findById(query.mentorId())
                .orElseThrow(() -> new NoSuchElementException("Mentor not found with id: " + query.mentorId()));

        if (mentorEntity.getRole() != UserRole.MENTOR) {
            throw new IllegalArgumentException("User with id " + query.mentorId() + " is not a mentor");
        }

        Set<Long> internIdsFromTasks = taskRepository.findAll()
                .stream()
                .filter(task -> task.getMentorId().equals(query.mentorId()))
                .filter(task -> task.getAssigneeId() != null)
                .map(TaskEntity::getAssigneeId)
                .collect(Collectors.toSet());

        Set<Long> internIdsFromAssessments = assessmentRepository.findAll()
                .stream()
                .filter(assessment -> assessment.getMentorId().equals(query.mentorId()))
                .map(assessment -> assessment.getInternId())
                .collect(Collectors.toSet());

        Set<Long> allInternIds = internIdsFromTasks;
        allInternIds.addAll(internIdsFromAssessments);

        List<UserEntity> internEntities = userRepository.findAll()
                .stream()
                .filter(user -> user.getRole() == UserRole.INTERN)
                .filter(user -> allInternIds.contains(user.getId()))
                .collect(Collectors.toList());

        return internEntities.stream()
                .map(User::fromEntity)
                .collect(Collectors.toList());
    }
}
