package by.bsuir.growpathserver.trainee.infrastructure.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.AuthApi;
import by.bsuir.growpathserver.dto.model.CompleteRegistrationRequest;
import by.bsuir.growpathserver.dto.model.ForgetPasswordRequest;
import by.bsuir.growpathserver.dto.model.MessageResponse;
import by.bsuir.growpathserver.dto.model.ResetPasswordRequest;
import by.bsuir.growpathserver.trainee.application.handler.CompleteRegistrationHandler;
import by.bsuir.growpathserver.trainee.application.handler.ForgetPasswordHandler;
import by.bsuir.growpathserver.trainee.application.handler.ResetPasswordHandler;
import by.bsuir.growpathserver.trainee.infrastructure.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuthController extends BaseController implements AuthApi {

    private final CompleteRegistrationHandler completeRegistrationHandler;
    private final ForgetPasswordHandler forgetPasswordHandler;
    private final ResetPasswordHandler resetPasswordHandler;
    private final AuthMapper authMapper;

    @Override
    public ResponseEntity<MessageResponse> completeRegistration(CompleteRegistrationRequest completeRegistrationRequest) {
        completeRegistrationHandler.handle(authMapper.toCommand(completeRegistrationRequest));
        return ResponseEntity.ok(new MessageResponse().message("Registration completed"));
    }

    @Override
    public ResponseEntity<Void> forgotPassword(ForgetPasswordRequest forgetPasswordRequest) {
        forgetPasswordHandler.handle(authMapper.toForgetPasswordCommand(forgetPasswordRequest));
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> resetPassword(ResetPasswordRequest resetPasswordRequest) {
        resetPasswordHandler.handle(authMapper.toResetPasswordCommand(resetPasswordRequest));
        return ResponseEntity.noContent().build();
    }
}
