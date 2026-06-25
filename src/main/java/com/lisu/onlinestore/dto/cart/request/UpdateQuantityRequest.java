package com.lisu.onlinestore.dto.cart.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuantityRequest {
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be greater than 0")
    private Integer quantity;
}
