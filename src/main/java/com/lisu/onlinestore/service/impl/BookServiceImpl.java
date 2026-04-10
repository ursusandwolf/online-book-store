package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.BookRequestDto;
import com.lisu.onlinestore.mapper.BookMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository repository;
    private final BookMapper mapper;

    @Override
    public BookDto save(Book book) {
        return mapper.toDto(repository.save(book));
    }

    @Override
    public BookDto update(Long id, BookRequestDto dto) {
        Book existing = repository.findById(id);
        mapper.updateEntityFromDto(dto, existing);
        Book saved = repository.save(existing);
        return mapper.toDto(saved);
    }

    @Override
    public BookDto findById(Long id) {
        return mapper.toDto(repository.findById(id));
    }

    @Override
    public List<BookDto> findAll() {
        List<BookDto> result = new ArrayList<>();
        for (Book book : repository.findAll()) {
            result.add(mapper.toDto(book));
        }
        return result;
    }

    @Override
    public void delete(Long id) {
        //not implemented yet
    }
}
