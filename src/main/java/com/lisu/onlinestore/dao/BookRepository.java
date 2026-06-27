package com.lisu.onlinestore.dao;

import com.lisu.onlinestore.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findAllByCategoriesId(Long categoryId, Pageable pageable);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Book b
            SET b.stock = b.stock - :quantity,
                b.version = b.version + 1
            WHERE b.id = :bookId
                AND b.version = :version
                AND b.stock >= :quantity
            """)
    int reserveStock(@Param("bookId") Long bookId,
                     @Param("version") int version,
                     @Param("quantity") int quantity);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE Book b
            SET b.stock = b.stock + :quantity,
                b.version = b.version + 1
            WHERE b.id = :bookId
                AND b.version = :version
            """)
    int releaseStock(@Param("bookId") Long bookId,
                     @Param("version") int version,
                     @Param("quantity") int quantity);
}
