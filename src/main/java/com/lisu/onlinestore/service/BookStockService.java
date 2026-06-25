package com.lisu.onlinestore.service;

import com.lisu.onlinestore.model.Book;

/**
 * Service for managing book stock operations.
 * Handles stock validation and updates.
 */
public interface BookStockService {
    /**
     * Validate that requested quantity is available in stock.
     * @param book the book to validate
     * @param requestedQuantity the quantity to validate
     * @throws IllegalArgumentException if stock is insufficient
     */
    void validateStockAvailable(Book book, int requestedQuantity);

    /**
     * Decrease book stock by the specified quantity.
     * @param book the book to decrease stock for
     * @param quantity the quantity to decrease
     */
    void decreaseStock(Book book, int quantity);

    /**
     * Increase book stock by the specified quantity.
     * @param book the book to increase stock for
     * @param quantity the quantity to increase
     */
    void increaseStock(Book book, int quantity);
}
