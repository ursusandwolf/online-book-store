package com.lisu.onlinestore.dto.user;

import com.lisu.onlinestore.validation.FieldMatch;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@FieldMatch(
        first = "password",
        second = "repeatPassword",
        message = "Passwords do not match"
)
public class UserRegistrationRequestDto {
    @NotBlank
    @Email
    @Schema(example = "user@example.com", description = "User's email address")
    private String email;

    @NotBlank
    @Length(min = 8, max = 35)
    @Schema(example = "password123", description = "User's password")
    private String password;

    @NotBlank
    @Length(min = 8, max = 35)
    @Schema(example = "password123", description = "Repeat user's password for verification")
    private String repeatPassword;

    @NotBlank
    @Schema(example = "John", description = "User's first name")
    private String firstName;

    @NotBlank
    @Schema(example = "Doe", description = "User's last name")
    private String lastName;

    @Schema(example = "123 Main St, New York, NY", description = "User's shipping address")
    private String shippingAddress;
}
