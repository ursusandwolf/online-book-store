package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.model.cart.CartItem;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShoppingCartMapper {

    CartDto toDto(ShoppingCart shoppingCart);

    CartItemDto toItemDto(CartItem cartItem);

    @AfterMapping
    default void setUserIdAndItems(@MappingTarget CartDto cartDto, ShoppingCart shoppingCart) {
        if (shoppingCart.getUser() != null) {
            cartDto.setUserId(shoppingCart.getUser().getId());
        }
        if (shoppingCart.getItems() != null) {
            cartDto.setItems(shoppingCart.getItems().stream()
                    .map(this::toItemDto)
                    .collect(Collectors.toList()));
        }
    }

    @AfterMapping
    default void setBookIdAndTitle(@MappingTarget CartItemDto cartItemDto, CartItem cartItem) {
        if (cartItem.getBook() != null) {
            cartItemDto.setBookId(cartItem.getBook().getId());
            cartItemDto.setBookTitle(cartItem.getBook().getTitle());
        }
    }
}
