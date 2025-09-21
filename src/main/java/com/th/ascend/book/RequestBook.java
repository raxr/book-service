package com.th.ascend.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.apache.commons.lang3.StringUtils;

public record RequestBook(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Author is required")
        String author,

        @NotNull(message = "Published date is required")
        @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date format must be yyyy-MM-dd")
        String publishedDate,

        @Pattern(regexp = "CE|BE", message = "Calendar must be CE or BE")
        String calendar
) {
    public RequestBook {
        calendar = StringUtils.isNotBlank(calendar) ? calendar : "CE";
    }
}
