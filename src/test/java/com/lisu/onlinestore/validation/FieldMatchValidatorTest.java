package com.lisu.onlinestore.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FieldMatchValidatorTest {
    private FieldMatchValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FieldMatchValidator();
        validator.initialize(
                RegistrationPayload.class.getAnnotation(FieldMatch.class)
        );
    }

    @Test
    void isValid_ShouldReturnTrueWhenFieldsMatch() {
        RegistrationPayload payload = new RegistrationPayload("password123", "password123");

        assertTrue(validator.isValid(payload, null));
    }

    @Test
    void isValid_ShouldReturnTrueWhenBothFieldsAreNull() {
        RegistrationPayload payload = new RegistrationPayload(null, null);

        assertTrue(validator.isValid(payload, null));
    }

    @Test
    void isValid_ShouldReturnFalseWhenFieldsDoNotMatch() {
        RegistrationPayload payload = new RegistrationPayload("password123", "different123");

        assertFalse(validator.isValid(payload, null));
    }

    @Test
    void isValid_ShouldReturnFalseWhenOnlyOneFieldIsNull() {
        RegistrationPayload payload = new RegistrationPayload("password123", null);

        assertFalse(validator.isValid(payload, null));
    }

    @FieldMatch(first = "password", second = "repeatPassword", message = "Passwords do not match")
    private static class RegistrationPayload {
        private final String password;
        private final String repeatPassword;

        private RegistrationPayload(String password, String repeatPassword) {
            this.password = password;
            this.repeatPassword = repeatPassword;
        }

        public String getPassword() {
            return password;
        }

        public String getRepeatPassword() {
            return repeatPassword;
        }
    }
}
