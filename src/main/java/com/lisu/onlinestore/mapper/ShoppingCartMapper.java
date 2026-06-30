package com.lisu.onlinestore.mapper;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.model.cart.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = CartItemMapper.class,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShoppingCartMapper {

    @Mapping(source = "user.id", target = "userId")
    CartDto toDto(ShoppingCart shoppingCart);
}
