package com.lisu.onlinestore.dao;

import com.lisu.onlinestore.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
