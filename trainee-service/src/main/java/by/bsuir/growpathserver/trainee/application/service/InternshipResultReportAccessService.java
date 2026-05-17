package by.bsuir.growpathserver.trainee.application.service;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.bsuir.growpathserver.trainee.application.exception.InternshipResultReportAccessDeniedException;
import by.bsuir.growpathserver.trainee.application.port.CurrentApplicationUserResolver;
import by.bsuir.growpathserver.trainee.domain.entity.IprEntity;
import by.bsuir.growpathserver.trainee.domain.entity.TaskEntity;
import by.bsuir.growpathserver.trainee.infrastructure.repository.IprRepository;
import by.bsuir.growpathserver.trainee.infrastructure.repository.TaskRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InternshipResultReportAccessService {

    private final CurrentApplicationUserResolver currentApplicationUserResolver;
    private final IprRepository iprRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public void verifyCanViewReport(Long internId) {
        Long currentUserId = currentApplicationUserResolver.resolveCurrentUserDatabaseId()
                .orElseThrow(InternshipResultReportAccessDeniedException::new);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new InternshipResultReportAccessDeniedException();
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        if (hasAnyRole(authorities, "ADMIN", "HR_MANAGER", "DEPARTMENT_HEAD")) {
            return;
        }
        if (hasAnyRole(authorities, "MENTOR") && isMentorOfIntern(currentUserId, internId)) {
            return;
        }
        throw new InternshipResultReportAccessDeniedException();
    }

    private boolean isMentorOfIntern(Long mentorId, Long internId) {
        List<IprEntity> iprs = iprRepository.findByInternId(internId);
        boolean mentorOnIpr = iprs.stream()
                .anyMatch(ipr -> ipr.getMentor() != null && mentorId.equals(ipr.getMentor().getId()));
        if (mentorOnIpr) {
            return true;
        }
        return taskRepository.findByAssigneeId(internId).stream()
                .map(TaskEntity::getMentorId)
                .anyMatch(mentorId::equals);
    }

    private boolean hasAnyRole(Collection<? extends GrantedAuthority> authorities, String... roles) {
        for (String role : roles) {
            String roleName = "ROLE_" + role;
            for (GrantedAuthority authority : authorities) {
                if (roleName.equals(authority.getAuthority())) {
                    return true;
                }
            }
        }
        return false;
    }
}
