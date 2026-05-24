package by.bsuir.growpathserver.notification.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.dto.model.MailingHistoryListResponse;
import by.bsuir.growpathserver.dto.model.MailingHistoryResponse;
import by.bsuir.growpathserver.notification.domain.entity.MailingHistoryEntity;

@Mapper(componentModel = SPRING, uses = { PaginationMapper.class, NotificationMapperSupport.class })
public interface MailingHistoryMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "toStringId")
    @Mapping(target = "mailingId", source = "mailingId", qualifiedByName = "toStringId")
    @Mapping(target = "type", source = "mailingType", qualifiedByName = "toMailingHistoryType")
    MailingHistoryResponse toMailingHistoryResponse(MailingHistoryEntity entity);

    @Mapping(target = "data", source = "content")
    @Mapping(target = "pagination", source = ".")
    MailingHistoryListResponse toMailingHistoryListResponse(Page<MailingHistoryEntity> page);
}
