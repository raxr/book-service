package com.th.ascend.book;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookMapper {
    @Mapping(target = "publishedDate", dateFormat = "yyyy-MM-dd HH:mm:ss")
    BookEntity toEntity(RequestBook requestBook);

    List<ResponseBook> toResponseList(Page<BookEntity> bookEntity);

    ResponseBook toResponse(BookEntity bookEntity);
}