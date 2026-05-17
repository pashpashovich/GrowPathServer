package by.bsuir.growpathserver.notification.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.dto.model.RecipientListResponse;
import by.bsuir.growpathserver.dto.model.RecipientResponse;
import by.bsuir.growpathserver.notification.domain.aggregate.Recipient;

@Mapper(componentModel = SPRING)
public interface RecipientMapper {

    default RecipientResponse toRecipientResponse(Recipient recipient) {
        RecipientResponse response = new RecipientResponse();
        response.setId(String.valueOf(recipient.getId()));
        response.setEmail(recipient.getEmail());
        response.setFullName(recipient.getFullName());
        if (recipient.getUserId() != null) {
            response.setUserId(String.valueOf(recipient.getUserId()));
        }
        response.setType(RecipientResponse.TypeEnum.fromValue(recipient.getType().toApiValue()));
        if (recipient.getDistributionGroupIds() != null) {
            response.setDistributionGroupIds(recipient.getDistributionGroupIds().stream()
                                                     .map(String::valueOf)
                                                     .collect(Collectors.toList()));
        }
        return response;
    }

    default RecipientListResponse toRecipientListResponse(Page<Recipient> page) {
        RecipientListResponse response = new RecipientListResponse();
        response.setData(page.getContent().stream().map(this::toRecipientResponse).collect(Collectors.toList()));
        response.setPagination(buildPagination(page));
        return response;
    }

    default RecipientListResponse toRecipientListResponse(List<Recipient> recipients) {
        RecipientListResponse response = new RecipientListResponse();
        response.setData(recipients.stream().map(this::toRecipientResponse).collect(Collectors.toList()));
        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(1);
        pagination.setLimit(recipients.size());
        pagination.setTotal(recipients.size());
        pagination.setTotalPages(1);
        response.setPagination(pagination);
        return response;
    }

    private PaginationResponse buildPagination(Page<Recipient> page) {
        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(page.getNumber() + 1);
        pagination.setLimit(page.getSize());
        pagination.setTotal((int) page.getTotalElements());
        pagination.setTotalPages(page.getTotalPages());
        return pagination;
    }
}
