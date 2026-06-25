package com.lisu.onlinestore.service;

import com.lisu.onlinestore.dto.cart.request.AddCartItemRequest;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequest;

/**
 * Service for validating cart-related DTOs.
 * Handles request validation before processing.
 */
public interface CartRequestValidator {
    /**
     * Validate add cart item request.
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateAddRequest(AddCartItemRequest request);

    /**
     * Validate update quantity request.
     * @param request the request to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validateUpdateRequest(UpdateQuantityRequest request);

    /**
     * Validate quantity value.
     * @param quantity the quantity to validate
     * @throws IllegalArgumentException if invalid
     */
    void validateQuantity(Integer quantity);
}
