package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dto.book.BookDto;
import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.book.CreateBookRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.BookMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        Book book = mapper.toEntity(dto);
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
    public Page<BookDto> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(mapper::toDto);
    }

    @Override
    public Page<BookDtoWithoutCategoryIds> findAllByCategoriesId(Long id, Pageable pageable) {
        return repository.findAllByCategoriesId(id, pageable)
                .map(mapper::toDtoWithoutCategories);
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto dto) {
        Book book = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + id)
        );
        mapper.updateFromDto(dto, book);
        return mapper.toDto(repository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
