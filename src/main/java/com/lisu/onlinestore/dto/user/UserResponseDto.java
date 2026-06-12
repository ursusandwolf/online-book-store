package com.lisu.onlinestore.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserResponseDto {
    @Schema(example = "1")
    private Long id;

    @Schema(example = "john.doe@example.com")
    private String email;

    @Schema(example = "John")
    private String firstName;

    @Schema(example = "Doe")
    private String lastName;

    @Schema(example = "123 Main St, City, Country")
    private String shippingAddress;
}
