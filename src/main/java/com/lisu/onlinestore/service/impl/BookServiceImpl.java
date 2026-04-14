package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.CreateBookRequestDto;
import com.lisu.onlinestore.mapper.BookMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    @Override
    public BookDto create(CreateBookRequestDto dto) {
        Book book = mapper.toBook(dto);
        return mapper.toDto(repository.save(book));
    }

    @Override
    public BookDto findById(Long id) {
        return mapper.toDto(repository.findById(id));
    }

    @Override
    public List<BookDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }
}
