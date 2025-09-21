package com.th.ascend.book;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDate.class, Year.class})
public interface BookMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "publishedDate", expression = "java(parseAndValidateDate(requestBook.publishedDate(), requestBook.calendar()))")
    BookEntity toEntity(RequestBook requestBook);

    ResponseBook toResponse(BookEntity entity);

    List<ResponseBook> toResponseList(Page<BookEntity> bookEntity);


    default LocalDate parseAndValidateDate(String dateString, String calendar) {

        var calendarSystem = StringUtils.isNotBlank(calendar) ? calendar : "CE";

        var parts = dateString.split("-");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid date format. Expected: yyyy-MM-dd");
        }

        var year = Integer.parseInt(parts[0]);
        var month = Integer.parseInt(parts[1]);
        var day = Integer.parseInt(parts[2]);

        if (calendarSystem.equals("BE")) {
            return parseBuddhistDate(year, month, day);
        } else {
            return parseChristianDate(year, month, day);
        }
    }

    default LocalDate parseBuddhistDate(int beYear, int month, int day) {
        var currentYear = LocalDate.now().getYear();
        var minBEYear = 1543;
        var maxBEYear = currentYear + 543;

        if (beYear < minBEYear || beYear > maxBEYear) {
            throw new IllegalArgumentException(
                    "Buddhist year %d is out of valid range (%d-%d)"
                            .formatted(beYear, minBEYear, maxBEYear)
            );
        }

        var ceYear = beYear - 543;

        if (month == 2 && day == 29 && !Year.isLeap(ceYear)) {
            throw new IllegalArgumentException(
                    "Buddhist year %d (CE: %d) is not a leap year. February 29 doesn't exist."
                            .formatted(beYear, ceYear)
            );
        }

        return LocalDate.of(ceYear, month, day);
    }

    default LocalDate parseChristianDate(int year, int month, int day) {
        var currentYear = LocalDate.now().getYear();
        var minCEYear = 1000;

        if (year < minCEYear || year > currentYear) {
            throw new IllegalArgumentException(
                    "Christian year %d is out of valid range (%d-%d)"
                            .formatted(year, minCEYear, currentYear)
            );
        }

        try {
            return LocalDate.of(year, month, day);
        } catch (DateTimeException e) {
            if (month == 2 && day == 29) {
                throw new IllegalArgumentException(
                        "Christian year %d is not a leap year. February 29 doesn't exist."
                                .formatted(year)
                );
            }
            throw new IllegalArgumentException("Invalid date: " + e.getMessage());
        }
    }

}