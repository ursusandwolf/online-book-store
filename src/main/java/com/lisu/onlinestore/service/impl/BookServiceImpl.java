package com.lisu.onlinestore.service.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dao.CategoryRepository;
import com.lisu.onlinestore.dto.book.BookDto;
import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.book.CreateBookRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.BookMapper;
import com.lisu.onlinestore.model.Book;
import com.lisu.onlinestore.model.Category;
import com.lisu.onlinestore.service.BookService;
import java.util.Set;
import java.util.stream.Collectors;
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
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto create(CreateBookRequestDto dto) {
        Book book = mapper.toEntity(dto);
        book.setCategories(resolveCategories(dto.getCategoryIds()));
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
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Can't find category by id: " + id);
        }
        return repository.findAllByCategoriesId(id, pageable)
                .map(mapper::toDtoWithoutCategories);
    }

    @Override
    public BookDto update(Long id, CreateBookRequestDto dto) {
        Book book = repository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Can't find book by id: " + id)
        );
        mapper.updateFromDto(dto, book);
        book.setCategories(resolveCategories(dto.getCategoryIds()));
        return mapper.toDto(repository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Can't find book by id: " + id);
        }
        repository.deleteById(id);
    }

    private Set<Category> resolveCategories(Set<Long> ids) {
        return ids.stream().map(id -> categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Can't find category by id: " + id)))
                .collect(Collectors.toSet());
    }
}
