package by.bsuir.growpathserver.trainee.application.handler;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.command.UpdateUserCommand;
import by.bsuir.growpathserver.trainee.domain.aggregate.User;
import by.bsuir.growpathserver.trainee.domain.entity.DepartmentEntity;
import by.bsuir.growpathserver.trainee.domain.entity.UserEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.DepartmentRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UpdateUserHandler {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional
    public User handle(UpdateUserCommand command) {
        UserEntity entity = userRepository.findById(command.userId())
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + command.userId()));

        command.email()
                .filter(StringUtils::isNotBlank)
                .ifPresent(email -> {
                    if (!email.equals(entity.getEmail()) && userRepository.existsByEmail(email)) {
                        throw new IllegalArgumentException("User with email " + email + " already exists");
                    }
                    entity.setEmail(email);
                });

        Optional<String> firstOpt = command.firstName().filter(StringUtils::isNotBlank);
        Optional<String> lastOpt = command.lastName().filter(StringUtils::isNotBlank);
        if (firstOpt.isPresent() != lastOpt.isPresent()) {
            throw new IllegalArgumentException("firstName and lastName must both be provided to update the name");
        }
        firstOpt.ifPresent(first -> entity.setFirstName(first.trim()));
        lastOpt.ifPresent(last -> entity.setLastName(last.trim()));

        command.patronymicName().ifPresent(p -> {
            if (StringUtils.isBlank(p)) {
                entity.setPatronymicName(null);
            }
            else {
                entity.setPatronymicName(p.trim());
            }
        });

        command.role().ifPresent(entity::setRole);

        command.departmentId().ifPresent(deptId -> {
            DepartmentEntity department = departmentRepository.findById(deptId)
                    .orElseThrow(() -> new NoSuchElementException("Department not found with id: " + deptId));
            entity.setDepartmentId(deptId);
        });

        UserEntity savedEntity = userRepository.save(entity);
        return User.fromEntity(savedEntity);
    }
}
