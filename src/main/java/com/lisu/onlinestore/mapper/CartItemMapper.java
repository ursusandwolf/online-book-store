package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.model.cart.CartItem;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CartItemMapper {
    CartItemDto toDto(CartItem cartItem);

    @AfterMapping
    default void setBookDetails(@MappingTarget CartItemDto cartItemDto, CartItem cartItem) {
        if (cartItem.getBook() != null) {
            cartItemDto.setBookId(cartItem.getBook().getId());
            cartItemDto.setBookTitle(cartItem.getBook().getTitle());
        }
    }
}
