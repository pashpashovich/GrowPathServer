package by.bsuir.growpathserver.notification.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.dto.model.EmailTemplateAttachmentResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateListResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.notification.application.service.EmailTemplateAttachmentStorageService;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateAttachmentEntity;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateAttachmentRepository;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {

    default EmailTemplateResponse toEmailTemplateResponse(EmailTemplate template,
                                                          EmailTemplateAttachmentRepository attachmentRepository,
                                                          EmailTemplateAttachmentStorageService attachmentStorageService) {
        EmailTemplateResponse response = new EmailTemplateResponse();
        response.setId(String.valueOf(template.getId()));
        response.setName(template.getName());
        response.setSubject(template.getSubject());
        response.setBody(template.getBody());

        List<EmailTemplateAttachmentEntity> attachments = attachmentRepository.findByEmailTemplateId(template.getId());
        response.setAttachments(attachments.stream()
                                        .map(attachment -> toAttachmentResponse(attachment, attachmentStorageService))
                                        .collect(Collectors.toList()));

        return response;
    }

    default EmailTemplateAttachmentResponse toAttachmentResponse(
            EmailTemplateAttachmentEntity attachment,
            EmailTemplateAttachmentStorageService attachmentStorageService) {
        EmailTemplateAttachmentResponse attachmentResponse = new EmailTemplateAttachmentResponse();
        attachmentResponse.setId(String.valueOf(attachment.getId()));
        attachmentResponse.setName(attachment.getName());
        attachmentResponse.setToken(attachment.getToken());
        attachmentResponse.setDownloadUrl(attachmentStorageService.createPresignedDownloadUrl(attachment.getToken()));
        return attachmentResponse;
    }

    default EmailTemplateListResponse toEmailTemplateListResponse(
            Page<EmailTemplate> templatesPage,
            EmailTemplateAttachmentRepository attachmentRepository,
            EmailTemplateAttachmentStorageService attachmentStorageService) {
        EmailTemplateListResponse response = new EmailTemplateListResponse();
        response.setData(templatesPage.getContent().stream()
                                 .map(template -> toEmailTemplateResponse(template, attachmentRepository,
                                                                          attachmentStorageService))
                                 .collect(Collectors.toList()));

        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(templatesPage.getNumber() + 1);
        pagination.setLimit(templatesPage.getSize());
        pagination.setTotal((int) templatesPage.getTotalElements());
        pagination.setTotalPages(templatesPage.getTotalPages());
        response.setPagination(pagination);

        return response;
    }
}
