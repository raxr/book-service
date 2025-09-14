package com.th.ascend.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RequestBook(
        @NotBlank
        String title,

        @NotBlank
        String author,

        String publisher,

        @NotNull(message = "Published date is required")
        @ValidYear(message = "Year must be between 1000-current (CE) or 1543-current+543 (BE)")
        String publishedDate
) {
}
