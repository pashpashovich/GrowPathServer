package by.bsuir.growpathserver.notification.application.service;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import by.bsuir.growpathserver.dto.model.CreateMailingRequest;
import by.bsuir.growpathserver.dto.model.CreateMailingRequestSchedule;
import by.bsuir.growpathserver.dto.model.MailingListResponse;
import by.bsuir.growpathserver.dto.model.MailingResponse;
import by.bsuir.growpathserver.dto.model.UpdateMailingRequest;
import by.bsuir.growpathserver.notification.application.command.CreateMailingCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteMailingCommand;
import by.bsuir.growpathserver.notification.application.command.SendMailingCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateMailingCommand;
import by.bsuir.growpathserver.notification.application.exception.InvalidMailingScheduleException;
import by.bsuir.growpathserver.notification.application.exception.UnknownEnumerationValueException;
import by.bsuir.growpathserver.notification.application.handler.CreateMailingHandler;
import by.bsuir.growpathserver.notification.application.handler.DeleteMailingHandler;
import by.bsuir.growpathserver.notification.application.handler.GetMailingByIdHandler;
import by.bsuir.growpathserver.notification.application.handler.GetMailingsHandler;
import by.bsuir.growpathserver.notification.application.handler.SendMailingHandler;
import by.bsuir.growpathserver.notification.application.handler.UpdateMailingHandler;
import by.bsuir.growpathserver.notification.application.query.GetMailingByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetMailingsQuery;
import by.bsuir.growpathserver.notification.application.support.NotificationIds;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;
import by.bsuir.growpathserver.notification.domain.valueobject.WeekDay;
import by.bsuir.growpathserver.notification.infrastructure.mapper.MailingMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MailingApplicationFacade {

    private final CreateMailingHandler createMailingHandler;
    private final GetMailingByIdHandler getMailingByIdHandler;
    private final GetMailingsHandler getMailingsHandler;
    private final UpdateMailingHandler updateMailingHandler;
    private final DeleteMailingHandler deleteMailingHandler;
    private final SendMailingHandler sendMailingHandler;
    private final MailingMapper mailingMapper;

    public MailingResponse createMailing(CreateMailingRequest request) {
        Mailing mailing = createMailingHandler.handle(buildCreateCommand(request));
        return mailingMapper.toMailingResponse(mailing);
    }

    public void deleteMailing(String id) {
        deleteMailingHandler.handle(new DeleteMailingCommand(NotificationIds.parseRequired(id, "id")));
    }

    public MailingResponse getMailingById(String id) {
        Mailing mailing = getMailingByIdHandler.handle(
                new GetMailingByIdQuery(NotificationIds.parseRequired(id, "id")));
        return mailingMapper.toMailingResponse(mailing);
    }

    public MailingListResponse getMailings(Integer page, Integer limit, String status, String type) {
        Page<Mailing> mailings = getMailingsHandler.handle(GetMailingsQuery.builder()
                                                                   .page(page)
                                                                   .limit(limit)
                                                                   .status(status)
                                                                   .type(type)
                                                                   .build());
        return mailingMapper.toMailingListResponse(mailings);
    }

    public void sendMailing(String id) {
        sendMailingHandler.handle(new SendMailingCommand(NotificationIds.parseRequired(id, "id")));
    }

    public MailingResponse updateMailing(String id, UpdateMailingRequest request) {
        Mailing mailing = updateMailingHandler.handle(UpdateMailingCommand.builder()
                                                              .id(NotificationIds.parseRequired(id, "id"))
                                                              .name(request.getName())
                                                              .type(request.getType() != null ?
                                                                            request.getType().getValue() :
                                                                            null)
                                                              .emailTemplateId(NotificationIds.parseOptional(
                                                                      request.getEmailTemplateId(), "emailTemplateId"))
                                                              .executeAt(request.getExecuteAt())
                                                              .distributionGroupIds(
                                                                      parseGroupIds(request.getDistributionGroupIds()))
                                                              .build());
        return mailingMapper.toMailingResponse(mailing);
    }

    private CreateMailingCommand buildCreateCommand(CreateMailingRequest request) {
        WeekDay weekDay = null;
        LocalTime executeTime = null;
        CreateMailingRequestSchedule schedule = request.getSchedule();
        if (schedule != null) {
            if (schedule.getWeekDay() != null) {
                weekDay = parseWeekDay(schedule.getWeekDay().getValue());
            }
            if (schedule.getExecuteTime() != null) {
                executeTime = parseExecuteTime(schedule.getExecuteTime());
            }
        }

        return CreateMailingCommand.builder()
                .name(request.getName())
                .type(request.getType().getValue())
                .emailTemplateId(NotificationIds.parseRequired(request.getEmailTemplateId(), "emailTemplateId"))
                .executeAt(request.getExecuteAt())
                .distributionGroupIds(parseGroupIds(request.getDistributionGroupIds()))
                .weekDay(weekDay)
                .executeTime(executeTime)
                .build();
    }

    private List<Long> parseGroupIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return ids.stream()
                .map(id -> NotificationIds.parseRequired(id, "distributionGroupId"))
                .toList();
    }

    private WeekDay parseWeekDay(String value) {
        try {
            return WeekDay.fromApiValue(value);
        }
        catch (IllegalArgumentException ex) {
            throw new UnknownEnumerationValueException("weekDay", value);
        }
    }

    private LocalTime parseExecuteTime(String executeTime) {
        try {
            return LocalTime.parse(executeTime);
        }
        catch (DateTimeParseException ex) {
            throw new InvalidMailingScheduleException(executeTime);
        }
    }
}
