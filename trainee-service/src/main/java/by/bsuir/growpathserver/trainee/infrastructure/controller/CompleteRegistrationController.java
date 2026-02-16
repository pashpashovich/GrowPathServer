package by.bsuir.growpathserver.trainee.infrastructure.controller;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.trainee.application.service.RegistrationTokenService;
import by.bsuir.growpathserver.trainee.infrastructure.keycloak.KeycloakAdminClient;
import by.bsuir.growpathserver.trainee.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class CompleteRegistrationController {

    private final RegistrationTokenService registrationTokenService;
    private final UserRepository userRepository;
    private final KeycloakAdminClient keycloakAdminClient;

    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        if (request.token == null || request.token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "token is required"));
        }
        if (request.newPassword == null || request.newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "password must be at least 8 characters"));
        }

        try {
            Long userId = registrationTokenService.validateAndConsumeToken(request.token.trim());
            String email = userRepository.findById(userId)
                    .orElseThrow()
                    .getEmail();

            keycloakAdminClient.setPassword(email, request.newPassword);

            return ResponseEntity.ok(Map.of(
                    "message", "Registration completed. You can now log in with your email and the new password."
            ));
        }
        catch (NoSuchElementException e) {
            log.warn("Invalid or expired registration token");
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(Map.of("error", "Invalid or expired registration link. Please request a new invitation."));
        }
        catch (KeycloakAdminClient.KeycloakAdminException e) {
            log.error("Keycloak error during complete-registration: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Unable to update password. Please try again later."));
        }
    }

    public static class CompleteRegistrationRequest {
        public String token;
        public String newPassword;
    }
}
