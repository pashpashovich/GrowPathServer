package by.bsuir.growpathserver.notification.infrastructure.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;

import by.bsuir.growpathserver.dto.model.EmailTemplateAttachmentResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateListResponse;
import by.bsuir.growpathserver.dto.model.EmailTemplateResponse;
import by.bsuir.growpathserver.notification.domain.aggregate.EmailTemplate;
import by.bsuir.growpathserver.notification.domain.entity.EmailTemplateAttachmentEntity;
import by.bsuir.growpathserver.notification.infrastructure.repository.EmailTemplateAttachmentRepository;

@Mapper(componentModel = "spring")
public interface EmailTemplateMapper {

    default EmailTemplateResponse toEmailTemplateResponse(EmailTemplate template,
                                                          EmailTemplateAttachmentRepository attachmentRepository) {
        EmailTemplateResponse response = new EmailTemplateResponse();
        response.setId(String.valueOf(template.getId()));
        response.setName(template.getName());
        response.setSubject(template.getSubject());
        response.setBody(template.getBody());

        List<EmailTemplateAttachmentEntity> attachments = attachmentRepository.findByEmailTemplateId(template.getId());
        response.setAttachments(attachments.stream()
                                        .map(attachment -> {
                                            EmailTemplateAttachmentResponse attachmentResponse = new EmailTemplateAttachmentResponse();
                                            attachmentResponse.setId(String.valueOf(attachment.getId()));
                                            attachmentResponse.setName(attachment.getName());
                                            attachmentResponse.setToken(attachment.getToken());
                                            return attachmentResponse;
                                        })
                                        .collect(Collectors.toList()));

        return response;
    }

    default EmailTemplateListResponse toEmailTemplateListResponse(
            org.springframework.data.domain.Page<EmailTemplate> templatesPage,
            EmailTemplateAttachmentRepository attachmentRepository) {
        EmailTemplateListResponse response = new EmailTemplateListResponse();
        response.setData(templatesPage.getContent().stream()
                                 .map(template -> toEmailTemplateResponse(template, attachmentRepository))
                                 .collect(Collectors.toList()));

        by.bsuir.growpathserver.dto.model.PaginationResponse pagination = new by.bsuir.growpathserver.dto.model.PaginationResponse();
        pagination.setPage(templatesPage.getNumber() + 1);
        pagination.setLimit(templatesPage.getSize());
        pagination.setTotal((int) templatesPage.getTotalElements());
        pagination.setTotalPages(templatesPage.getTotalPages());
        response.setPagination(pagination);

        return response;
    }
}
