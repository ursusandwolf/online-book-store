package com.lisu.onlinestore.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO class
 *
 * <p>Represents incoming data for creating or updating a book.
 * Validation is intentionally omitted as it is out of scope of this task.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequestDto {

    private String title;

    private String author;

    private String isbn;

    private BigDecimal price;

    private String description;

    private String coverImage;
}
