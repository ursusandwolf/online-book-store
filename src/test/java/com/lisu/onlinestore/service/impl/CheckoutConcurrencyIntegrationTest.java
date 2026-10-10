package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.lisu.onlinestore.dto.cart.request.CartItemRequestDto;
import com.lisu.onlinestore.dto.order.request.CreateOrderRequestDto;
import com.lisu.onlinestore.service.OrderService;
import com.lisu.onlinestore.service.ShoppingCartService;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;

@SpringBootTest
@Sql(scripts = {
    "classpath:database/delete/delete-all-orders.sql",
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql",
    "classpath:database/delete/delete-books-categories-table.sql",
    "classpath:database/delete/delete-all-books.sql",
    "classpath:database/create/add-default-users.sql",
    "classpath:database/create/add-default-books.sql",
    "classpath:database/create/add-default-shopping-cart.sql"
})
@Sql(scripts = {
    "classpath:database/delete/delete-all-orders.sql",
    "classpath:database/delete/delete-all-shopping-carts.sql",
    "classpath:database/delete/delete-all-users.sql",
    "classpath:database/delete/delete-all-books.sql"
}, executionPhase = ExecutionPhase.AFTER_TEST_METHOD)
class CheckoutConcurrencyIntegrationTest {
    @Autowired
    private OrderService orderService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void concurrentCheckoutCreatesOnlyOneOrder() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        Callable<Boolean> checkout = () -> {
            start.await(10, TimeUnit.SECONDS);
            try {
                orderService.placeOrder(2L, new CreateOrderRequestDto("Kyiv, Book Street, 10"));
                return true;
            } catch (IllegalArgumentException ex) {
                assertEquals("Shopping cart is empty", ex.getMessage());
                return false;
            }
        };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(checkout);
            var second = executor.submit(checkout);
            start.countDown();
            int successful = (first.get(30, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(30, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successful);
        }
        assertEquals(1, jdbc.queryForObject(
                "SELECT COUNT(*) FROM orders WHERE user_id = 2", Integer.class));
        assertEquals(0, jdbc.queryForObject(
                "SELECT COUNT(*) FROM cart_items WHERE shopping_cart_id = 2", Integer.class));
    }

    @Test
    void concurrentAddsDoNotLoseQuantities() throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        Callable<Void> add = () -> {
            start.await(10, TimeUnit.SECONDS);
            shoppingCartService.addBook(2L, new CartItemRequestDto(3L, 1));
            return null;
        };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(add);
            var second = executor.submit(add);
            start.countDown();
            first.get(30, TimeUnit.SECONDS);
            second.get(30, TimeUnit.SECONDS);
        }
        assertEquals(5, jdbc.queryForObject(
                "SELECT quantity FROM cart_items WHERE shopping_cart_id = 2 AND book_id = 3",
                Integer.class));
    }
}
