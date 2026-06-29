package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.dto.cart.request.AddCartItemRequest;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequest;
import com.lisu.onlinestore.model.User;

public interface ShoppingCartService {
    void createCartForUser(User user);

    CartDto getCart(Long userId);

    CartItemDto addBook(Long userId, AddCartItemRequest request);

    CartItemDto updateQuantity(Long userId, Long cartItemId, UpdateQuantityRequest request);

    void removeItem(Long userId, Long cartItemId);

}
