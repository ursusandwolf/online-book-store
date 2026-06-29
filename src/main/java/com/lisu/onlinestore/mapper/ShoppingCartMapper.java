package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class ShoppingCartMapper {
    @Autowired
    protected CartItemMapper cartItemMapper;

    public abstract CartDto toDto(ShoppingCart shoppingCart);

    @AfterMapping
    protected void setUserIdAndItems(@MappingTarget CartDto cartDto, ShoppingCart shoppingCart) {
        if (shoppingCart.getUser() != null) {
            cartDto.setUserId(shoppingCart.getUser().getId());
        }
        if (shoppingCart.getItems() != null) {
            cartDto.setCartItems(shoppingCart.getItems().stream()
                    .map(cartItemMapper::toDto)
                    .collect(Collectors.toList()));
        }
    }
}
