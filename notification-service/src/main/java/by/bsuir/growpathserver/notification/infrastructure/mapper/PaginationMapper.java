package by.bsuir.growpathserver.notification.infrastructure.mapper;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import by.bsuir.growpathserver.dto.model.PaginationResponse;

@Mapper(componentModel = SPRING)
public interface PaginationMapper {

    @Mapping(target = "page", expression = "java(page.getNumber() + 1)")
    @Mapping(target = "limit", source = "size")
    @Mapping(target = "total", expression = "java((int) page.getTotalElements())")
    @Mapping(target = "totalPages", source = "totalPages")
    PaginationResponse toPaginationResponse(Page<?> page);
}
