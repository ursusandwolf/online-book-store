package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.dto.cart.request.AddCartItemRequest;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequest;

public interface ShoppingCartService {
    /**
     * Get the shopping cart for the specified user.
     * @param userId the ID of the user
     * @return the cart with all items
     */
    CartDto getCart(Long userId);

    /**
     * Add a book to the shopping cart.
     * @param userId the ID of the user
     * @param request the add book request containing bookId and quantity
     * @return the added or updated cart item
     */
    CartItemDto addBook(Long userId, AddCartItemRequest request);

    /**
     * Update the quantity of a cart item.
     * @param userId the ID of the user
     * @param cartItemId the ID of the cart item
     * @param request the update request containing new quantity
     * @return the updated cart item
     */
    CartItemDto updateQuantity(Long userId, Long cartItemId, UpdateQuantityRequest request);

    /**
     * Remove an item from the shopping cart.
     * @param userId the ID of the user
     * @param cartItemId the ID of the cart item to remove
     */
    void removeItem(Long userId, Long cartItemId);
}
