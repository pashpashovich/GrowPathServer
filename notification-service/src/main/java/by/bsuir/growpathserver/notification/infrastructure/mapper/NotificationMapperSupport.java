package by.bsuir.growpathserver.notification.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import java.util.Objects;

import org.mapstruct.Mapper;
import org.mapstruct.Named;

import by.bsuir.growpathserver.dto.model.MailingHistoryResponse;
import by.bsuir.growpathserver.notification.domain.valueobject.MailingType;

@Mapper(componentModel = SPRING)
public interface NotificationMapperSupport {

    @Named("toStringId")
    default String toStringId(Long id) {
        return Objects.toString(id, null);
    }

    @Named("toMailingHistoryType")
    default MailingHistoryResponse.TypeEnum toMailingHistoryType(MailingType mailingType) {
        return MailingHistoryResponse.TypeEnum.fromValue(mailingType.toApiValue());
    }
}
