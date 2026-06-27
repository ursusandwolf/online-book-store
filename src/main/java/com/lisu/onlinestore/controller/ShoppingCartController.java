package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.cart.CartDto;
import com.lisu.onlinestore.dto.cart.CartItemDto;
import com.lisu.onlinestore.dto.cart.request.AddCartItemRequest;
import com.lisu.onlinestore.dto.cart.request.UpdateQuantityRequest;
import com.lisu.onlinestore.model.User;
import com.lisu.onlinestore.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
@PreAuthorize("hasRole('USER')")
@Tag(name = "Shopping Cart", description = "Operations with the user's shopping cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping
    @Operation(summary = "Get the current user's shopping cart")
    @ApiResponse(responseCode = "200", description = "Shopping cart retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Shopping cart not found")
    public CartDto getCart(@AuthenticationPrincipal User user) {
        return shoppingCartService.getCart(user.getId());
    }

    @PostMapping
    @Operation(summary = "Add a book to the shopping cart")
    @ApiResponse(responseCode = "200", description = "Book added to cart successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Book or shopping cart not found")
    public CartItemDto addBook(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid AddCartItemRequest request) {
        return shoppingCartService.addBook(user.getId(), request);
    }

    @PutMapping("/items/{cartItemId}")
    @Operation(summary = "Update quantity of a book in the shopping cart")
    @ApiResponse(responseCode = "200", description = "Cart item quantity updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    public CartItemDto updateQuantity(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId,
            @RequestBody @Valid UpdateQuantityRequest request) {
        return shoppingCartService.updateQuantity(user.getId(), cartItemId, request);
    }

    @DeleteMapping("/items/{cartItemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a book from the shopping cart")
    @ApiResponse(responseCode = "204", description = "Cart item removed successfully")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    public void removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartItemId) {
        shoppingCartService.removeItem(user.getId(), cartItemId);
    }
}
