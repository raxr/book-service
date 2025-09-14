package com.th.ascend.book;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class BuddhistDateDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext context)
            throws IOException {
        String dateStr = p.getText();
        try {
            LocalDateTime parsedDate = LocalDateTime.parse(dateStr, FORMATTER);
            int year = parsedDate.getYear();
            if (year > 2200) {
                return parsedDate.minusYears(543);
            } else {
                return parsedDate;
            }
        } catch (DateTimeParseException e) {
            log.error("Invalid date format. Expected: yyyy-MM-dd HH:mm:ss" ,  e);
            throw new JsonParseException(p, "Invalid date format. Expected: yyyy-MM-dd HH:mm:ss");
        }
    }
}
