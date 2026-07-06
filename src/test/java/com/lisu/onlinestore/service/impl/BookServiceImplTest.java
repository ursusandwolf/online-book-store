package com.lisu.onlinestore.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.dto.book.BookDto;
import com.lisu.onlinestore.dto.book.BookDtoWithoutCategoryIds;
import com.lisu.onlinestore.dto.book.CreateBookRequestDto;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.mapper.BookMapper;
import com.lisu.onlinestore.model.Book;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    void create_ShouldReturnMappedDto() {
        CreateBookRequestDto requestDto = createBookRequestDto();
        Book mappedBook = createBook(1L, "Clean Code");
        BookDto expected = createBookDto(1L, "Clean Code");

        when(bookMapper.toEntity(requestDto)).thenReturn(mappedBook);
        when(bookRepository.save(mappedBook)).thenReturn(mappedBook);
        when(bookMapper.toDto(mappedBook)).thenReturn(expected);

        BookDto actual = bookService.create(requestDto);

        assertEquals(expected, actual);
        verify(bookRepository).save(mappedBook);
    }

    @Test
    void findById_ShouldReturnMappedDto() {
        Book book = createBook(2L, "Refactoring");
        BookDto expected = createBookDto(2L, "Refactoring");

        when(bookRepository.findById(2L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto actual = bookService.findById(2L);

        assertEquals(expected, actual);
    }

    @Test
    void findById_ShouldThrowWhenBookDoesNotExist() {
        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> bookService.findById(99L)
        );

        assertEquals("Can't find book by id: 99", exception.getMessage());
    }

    @Test
    void findAll_ShouldMapRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 2);
        Book firstBook = createBook(1L, "Clean Code");
        Book secondBook = createBook(2L, "Effective Java");
        BookDto firstDto = createBookDto(1L, "Clean Code");
        BookDto secondDto = createBookDto(2L, "Effective Java");
        Page<Book> books = new PageImpl<>(List.of(firstBook, secondBook), pageable, 2);

        when(bookRepository.findAll(pageable)).thenReturn(books);
        when(bookMapper.toDto(firstBook)).thenReturn(firstDto);
        when(bookMapper.toDto(secondBook)).thenReturn(secondDto);

        Page<BookDto> actual = bookService.findAll(pageable);

        assertEquals(2, actual.getTotalElements());
        assertEquals(List.of(firstDto, secondDto), actual.getContent());
    }

    @Test
    void findAllByCategoriesId_ShouldMapRepositoryPage() {
        Pageable pageable = PageRequest.of(0, 5);
        Book book = createBook(3L, "Domain-Driven Design");
        BookDtoWithoutCategoryIds expected = new BookDtoWithoutCategoryIds();
        expected.setId(3L);
        expected.setTitle("Domain-Driven Design");
        Page<Book> books = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.findAllByCategoriesId(7L, pageable)).thenReturn(books);
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(expected);

        Page<BookDtoWithoutCategoryIds> actual = bookService.findAllByCategoriesId(7L, pageable);

        assertEquals(1, actual.getTotalElements());
        assertEquals(List.of(expected), actual.getContent());
    }

    @Test
    void update_ShouldApplyChangesAndReturnMappedDto() {
        CreateBookRequestDto requestDto = createBookRequestDto();
        Book book = createBook(4L, "Old title");
        BookDto expected = createBookDto(4L, "Clean Code");

        when(bookRepository.findById(4L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        BookDto actual = bookService.update(4L, requestDto);

        assertEquals(expected, actual);
        verify(bookMapper).updateFromDto(requestDto, book);
        verify(bookRepository).save(book);
    }

    @Test
    void deleteById_ShouldDelegateToRepository() {
        bookService.deleteById(8L);

        verify(bookRepository).deleteById(8L);
    }

    private CreateBookRequestDto createBookRequestDto() {
        return new CreateBookRequestDto(
                "Clean Code",
                "Robert C. Martin",
                "9780132350884",
                new BigDecimal("42.00"),
                "A book about writing cleaner code",
                "cover.png",
                Set.of(1L, 2L)
        );
    }

    private Book createBook(Long id, String title) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn("9780132350884");
        book.setPrice(new BigDecimal("42.00"));
        return book;
    }

    private BookDto createBookDto(Long id, String title) {
        BookDto bookDto = new BookDto();
        bookDto.setId(id);
        bookDto.setTitle(title);
        return bookDto;
    }
}
