package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.BookRequestDto;
import com.lisu.onlinestore.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for book-related operations.
 *
 * <p>This controller exposes book resources over HTTP and returns JSON responses.
 * Because this class is annotated with {@code @RestController}, Spring serializes
 * return values directly into the response body instead of resolving a view.</p>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/books")
public class BookController {

    private final BookService service;

    /**
     * Returns all books.
     *
     * <p>Spring will automatically return HTTP 200 (OK) for a successful GET request.</p>
     *
     * @return list of books
     */
    @GetMapping
    public List<BookDto> getAll() {
        return service.findAll();
    }

    /**
     * Returns a book by its identifier.
     *
     * <p>Spring will automatically return HTTP 200 (OK) if the book is found.
     * If the service throws an exception for a missing entity, it should be handled
     * separately in the service layer or by an exception handler.</p>
     *
     * @param id book identifier
     * @return book data
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public BookDto getBookById(@PathVariable Long id) {
        return service.findById(id);
    }

    /**
     * Creates a new book.
     *
     * <p>The request body is validated via Bean Validation. If validation fails,
     * Spring returns HTTP 400 (Bad Request) automatically.</p>
     *
     * @param bookDto request payload with book data
     * @return created book data
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto createBook(@Valid @RequestBody BookRequestDto bookDto) {
        return service.create(bookDto);
    }
}
