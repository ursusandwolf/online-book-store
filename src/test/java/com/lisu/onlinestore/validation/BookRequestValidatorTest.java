package com.lisu.onlinestore.validation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lisu.onlinestore.dto.book.CreateBookRequestDto;
import jakarta.validation.Validation;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BookRequestValidatorTest {
    @Test
    void rejectsPricesThatCannotBeStoredWithoutRounding() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = request();
            assertTrue(validator.validate(request).isEmpty());
            request.setPrice(new BigDecimal("10.001"));
            assertFalse(validator.validate(request).isEmpty());
            request.setPrice(new BigDecimal("10000000000.00"));
            assertFalse(validator.validate(request).isEmpty());
        }
    }

    @Test
    void rejectsNullAndNonPositiveCategoryIds() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = request();
            Set<Long> ids = new HashSet<>();
            ids.add(null);
            ids.add(0L);
            request.setCategoryIds(ids);
            assertTrue(factory.getValidator().validate(request).size() >= 2);
        }
    }

    private CreateBookRequestDto request() {
        return new CreateBookRequestDto("Clean Code", "Robert Martin", "9780132350884",
                new BigDecimal("10.00"), null, null, Set.of(1L));
    }
}
