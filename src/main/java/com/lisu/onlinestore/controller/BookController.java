package com.lisu.onlinestore.controller;

import com.lisu.onlinestore.dto.BookRequestDto;
import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.service.BookService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    public List<BookDto> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public BookDto createBook(@Valid @RequestBody BookRequestDto bookDto) {
        return service.create(bookDto);
    }
}
