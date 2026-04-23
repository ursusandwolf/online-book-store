package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dto.BookDto;
import com.lisu.onlinestore.dto.CreateBookRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.BookMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
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
        Book book = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + id)
        );
        return mapper.toDto(book);
    }

    @Override
    public List<BookDto> findAll() {
        return repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto dto) {
        Book book = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + id)
        );
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setPrice(dto.getPrice());
        book.setDescription(dto.getDescription());
        book.setCoverImage(dto.getCoverImage());
        return mapper.toDto(repository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
