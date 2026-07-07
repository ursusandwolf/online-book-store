package com.lisu.onlinestore.config;

import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
    private static final String DB_IMAGE = "mysql:8";
    private static CustomMySqlContainer mySqlContainer;

    private CustomMySqlContainer() {
        super(DB_IMAGE);
        withDatabaseName("online_book_store_test");
        withUsername("test");
        withPassword("test");
    }

    public static synchronized CustomMySqlContainer getInstance() {
        if (mySqlContainer == null) {
            mySqlContainer = new CustomMySqlContainer();
        }
        return mySqlContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", getUsername());
        System.setProperty("TEST_DB_PASSWORD", getPassword());
    }

    @Override
    public void stop() {
        // Keep container running for test suite
    }
}
