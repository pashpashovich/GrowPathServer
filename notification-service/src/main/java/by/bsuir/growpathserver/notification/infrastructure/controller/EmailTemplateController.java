package by.bsuir.growpathserver.notification.infrastructure.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import by.bsuir.growpathserver.dto.api.EmailTemplatesApi;
import by.bsuir.growpathserver.dto.model.CreateEmailTemplateRequest;
import by.bsuir.growpathserver.dto.model.EmailTemplateAttachmentRequest;
import by.bsuir.growpathserver.dto.model.EmailTemplateListResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateResponse;
import by.bsuir.growpathserver.dto.model.UpdateEmailTemplateRequest;
import by.bsuir.growpathserver.notification.application.command.CreateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.DeleteEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.command.UpdateEmailTemplateCommand;
import by.bsuir.growpathserver.notification.application.handler.CreateEmailTemplateHandler;
import by.bsuir.growpathserver.notification.application.handler.DeleteEmailTemplateHandler;
import by.bsuir.growpathserver.notification.application.handler.GetEmailTemplateByIdHandler;
import by.bsuir.growpathserver.notification.application.handler.GetEmailTemplatesHandler;
import by.bsuir.growpathserver.notification.application.handler.UpdateEmailTemplateHandler;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplateByIdQuery;
import by.bsuir.growpathserver.notification.application.query.GetEmailTemplatesQuery;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import by.bsuir.growpathserver.notification.infrastructure.mapper.EmailTemplateMapper;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateAttachmentRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EmailTemplateController implements EmailTemplatesApi {

    private final CreateEmailTemplateHandler createEmailTemplateHandler;
    private final GetEmailTemplateByIdHandler getEmailTemplateByIdHandler;
    private final GetEmailTemplatesHandler getEmailTemplatesHandler;
    private final UpdateEmailTemplateHandler updateEmailTemplateHandler;
    private final DeleteEmailTemplateHandler deleteEmailTemplateHandler;
    private final EmailTemplateMapper emailTemplateMapper;
    private final EmailTemplateAttachmentRepository emailTemplateAttachmentRepository;

    @Override
    public ResponseEntity<EmailTemplateResponse> createEmailTemplate(CreateEmailTemplateRequest request) {
        try {
            List<CreateEmailTemplateCommand.Attachment> attachments = null;
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                attachments = new ArrayList<>();
                for (EmailTemplateAttachmentRequest attachment : request.getAttachments()) {
                    attachments.add(new CreateEmailTemplateCommand.Attachment(
                            attachment.getName(),
                            attachment.getToken()
                    ));
                }
            }

            CreateEmailTemplateCommand command = CreateEmailTemplateCommand.builder()
                    .name(request.getName())
                    .subject(request.getSubject())
                    .body(request.getBody())
                    .attachments(attachments)
                    .build();

            EmailTemplate template = createEmailTemplateHandler.handle(command);
            EmailTemplateResponse response = emailTemplateMapper.toEmailTemplateResponse(template,
                                                                                         emailTemplateAttachmentRepository);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<Void> deleteEmailTemplate(String id) {
        try {
            Long templateId = Long.parseLong(id);
            DeleteEmailTemplateCommand command = new DeleteEmailTemplateCommand(templateId);
            deleteEmailTemplateHandler.handle(command);
            return ResponseEntity.noContent().build();
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<EmailTemplateResponse> getEmailTemplateById(String id) {
        try {
            Long templateId = Long.parseLong(id);
            GetEmailTemplateByIdQuery query = new GetEmailTemplateByIdQuery(templateId);
            EmailTemplate template = getEmailTemplateByIdHandler.handle(query);
            EmailTemplateResponse response = emailTemplateMapper.toEmailTemplateResponse(template,
                                                                                         emailTemplateAttachmentRepository);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<EmailTemplateListResponse> getEmailTemplates(Integer page, Integer limit) {
        try {
            GetEmailTemplatesQuery query = GetEmailTemplatesQuery.builder()
                    .page(page)
                    .limit(limit)
                    .build();
            Page<EmailTemplate> templatesPage = getEmailTemplatesHandler.handle(query);
            EmailTemplateListResponse response = emailTemplateMapper.toEmailTemplateListResponse(templatesPage,
                                                                                                 emailTemplateAttachmentRepository);
            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public ResponseEntity<EmailTemplateResponse> updateEmailTemplate(String id, UpdateEmailTemplateRequest request) {
        try {
            Long templateId = Long.parseLong(id);
            List<UpdateEmailTemplateCommand.Attachment> attachments = null;
            if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
                attachments = new ArrayList<>();
                for (EmailTemplateAttachmentRequest attachment : request.getAttachments()) {
                    attachments.add(new UpdateEmailTemplateCommand.Attachment(
                            attachment.getName(),
                            attachment.getToken()
                    ));
                }
            }

            UpdateEmailTemplateCommand command = UpdateEmailTemplateCommand.builder()
                    .id(templateId)
                    .name(request.getName())
                    .subject(request.getSubject())
                    .body(request.getBody())
                    .attachments(attachments)
                    .build();

            EmailTemplate template = updateEmailTemplateHandler.handle(command);
            EmailTemplateResponse response = emailTemplateMapper.toEmailTemplateResponse(template,
                                                                                         emailTemplateAttachmentRepository);
            return ResponseEntity.ok(response);
        }
        catch (NumberFormatException e) {
            return ResponseEntity.badRequest().build();
        }
        catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
