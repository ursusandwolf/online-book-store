package com.lisu.onlinestore.support;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
    private static final String DB_IMAGE = "mysql:8";
    private static CustomMySqlContainer mysqlContainer;

    private CustomMySqlContainer() {
        super(DB_IMAGE);
        withDatabaseName("online_book_store_test");
        withUsername("test");
        withPassword("test");
    }

    public static synchronized CustomMySqlContainer getInstance() {
        if (mysqlContainer == null) {
            mysqlContainer = new CustomMySqlContainer();
        }
        return mysqlContainer;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
    }
}
