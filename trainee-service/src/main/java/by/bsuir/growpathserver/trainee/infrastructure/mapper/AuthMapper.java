package by.bsuir.growpathserver.trainee.infrastructure.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import by.bsuir.growpathserver.dto.model.CompleteRegistrationRequest;
import by.bsuir.growpathserver.dto.model.ForgetPasswordRequest;
import by.bsuir.growpathserver.dto.model.ResetPasswordRequest;
import by.bsuir.growpathserver.trainee.application.command.CompleteRegistrationCommand;
import by.bsuir.growpathserver.trainee.application.command.ForgetPasswordCommand;
import by.bsuir.growpathserver.trainee.application.command.ResetPasswordCommand;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AuthMapper {

    CompleteRegistrationCommand toCommand(CompleteRegistrationRequest request);

    ForgetPasswordCommand toForgetPasswordCommand(ForgetPasswordRequest request);

    ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest request);
}
