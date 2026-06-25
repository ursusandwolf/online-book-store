package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dto.cart.request.AddCartItemRequest;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequest;
import com.lisu.onlinestore.service.CartRequestValidator;
import org.springframework.stereotype.Service;

@Service
public class CartRequestValidatorImpl implements CartRequestValidator {

    @Override
    public void validateAddRequest(AddCartItemRequest request) {
        validateQuantity(request.getQuantity());
        if (request.getBookId() == null) {
            throw new IllegalArgumentException("Book ID cannot be null");
        }
    }

    @Override
    public void validateUpdateRequest(UpdateQuantityRequest request) {
        validateQuantity(request.getQuantity());
    }

    @Override
    public void validateQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
}
