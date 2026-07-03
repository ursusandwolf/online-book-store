package com.lisu.onlinestore.dto.order.request;

import com.lisu.onlinestore.model.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderStatusRequestDto {
    @NotNull(message = "Status cannot be null")
    private OrderStatus status;
}
