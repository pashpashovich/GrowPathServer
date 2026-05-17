package by.bsuir.growpathserver.notification.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.dto.model.MailingListResponse;
import by.bsuir.growpathserver.dto.model.MailingResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.notification.domain.aggregate.Mailing;

@Mapper(componentModel = SPRING)
public interface MailingMapper {

    default MailingResponse toMailingResponse(Mailing mailing) {
        MailingResponse response = new MailingResponse();
        response.setId(String.valueOf(mailing.getId()));
        response.setName(mailing.getName());
        response.setType(MailingResponse.TypeEnum.fromValue(mailing.getType().toApiValue()));
        response.setStatus(MailingResponse.StatusEnum.fromValue(mailing.getStatus().toApiValue()));
        response.setEmailTemplateId(String.valueOf(mailing.getEmailTemplateId()));
        response.setExecuteAt(mailing.getExecuteAt());
        if (mailing.getDistributionGroupIds() != null) {
            response.setDistributionGroupIds(mailing.getDistributionGroupIds().stream()
                                                     .map(String::valueOf)
                                                     .collect(Collectors.toList()));
        }
        response.setCreatedAt(mailing.getCreatedAt());
        response.setUpdatedAt(mailing.getUpdatedAt());
        return response;
    }

    default MailingListResponse toMailingListResponse(Page<Mailing> page) {
        MailingListResponse response = new MailingListResponse();
        response.setData(page.getContent().stream().map(this::toMailingResponse).collect(Collectors.toList()));
        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(page.getNumber() + 1);
        pagination.setLimit(page.getSize());
        pagination.setTotal((int) page.getTotalElements());
        pagination.setTotalPages(page.getTotalPages());
        response.setPagination(pagination);
        return response;
    }
}
