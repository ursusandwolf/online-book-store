package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.dto.cart.request.CartItemRequestDto;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequestDto;
import com.lisu.onlinestore.model.User;

public interface ShoppingCartService {
    void createCartForUser(User user);

    CartDto getCart(Long userId);

    CartItemDto addBook(Long userId, CartItemRequestDto request);

    CartItemDto updateQuantity(Long userId, Long cartItemId, UpdateQuantityRequestDto request);

    void removeItem(Long userId, Long cartItemId);

}
