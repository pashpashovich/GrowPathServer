package by.bsuir.growpathserver.notification.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.dto.model.DistributionGroupListResponse;
import by.bsuir.growpathserver.dto.model.DistributionGroupResponse;
import by.bsuir.growpathserver.dto.model.PaginationResponse;
import by.bsuir.growpathserver.notification.domain.aggregate.DistributionGroup;

@Mapper(componentModel = SPRING)
public interface DistributionGroupMapper {

    default DistributionGroupResponse toDistributionGroupResponse(DistributionGroup group) {
        DistributionGroupResponse response = new DistributionGroupResponse();
        response.setId(String.valueOf(group.getId()));
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setRecipientCount(group.getRecipientCount());
        return response;
    }

    default DistributionGroupListResponse toDistributionGroupListResponse(Page<DistributionGroup> page) {
        DistributionGroupListResponse response = new DistributionGroupListResponse();
        response.setData(page.getContent().stream()
                                 .map(this::toDistributionGroupResponse)
                                 .collect(Collectors.toList()));
        PaginationResponse pagination = new PaginationResponse();
        pagination.setPage(page.getNumber() + 1);
        pagination.setLimit(page.getSize());
        pagination.setTotal((int) page.getTotalElements());
        pagination.setTotalPages(page.getTotalPages());
        response.setPagination(pagination);
        return response;
    }
}
