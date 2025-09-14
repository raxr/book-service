package com.th.ascend.book;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class YearValidator implements ConstraintValidator<ValidYear, String> {
    @Override
    public boolean isValid(String dateString, ConstraintValidatorContext constraintValidatorContext) {
        if (StringUtils.isBlank(dateString)) return true;

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime date = LocalDateTime.parse(dateString, formatter);
            int year = date.getYear();

            int currentYear = LocalDateTime.now().getYear();
            int currentBuddhistYear = currentYear + 543;

            if (year >= 1000 && year <= currentYear) {
                return true;
            }
            return year >= 1543 && year <= currentBuddhistYear;

        } catch (DateTimeParseException e) {
            log.error("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss" ,  e);
            return false;
        }
    }
}
