package com.lisu.onlinestore.dto.category;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryDto {

    @NotBlank(message = "Category name cannot be blank")
    private String name;

    private String description;
}
