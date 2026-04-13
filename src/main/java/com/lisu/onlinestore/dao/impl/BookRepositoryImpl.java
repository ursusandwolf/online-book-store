package com.lisu.onlinestore.dao.impl;

import com.lisu.onlinestore.dao.BookRepository;
import com.lisu.onlinestore.exception.DataProcessingException;
import com.lisu.onlinestore.exception.EntityNotFoundException;
import com.lisu.onlinestore.model.Book;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepositoryImpl implements BookRepository {
    private final SessionFactory sessionFactory;

    @Autowired
    public BookRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public Book save(Book book) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = sessionFactory.openSession();
            transaction = session.beginTransaction();
            session.persist(book);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't save book: " + book, e);

        } finally {
            if (session != null) {
                session.close();
            }
        }
        return book;
    }

    @Override
    public Book findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(Book.class, id);
        } catch (Exception e) {
            throw new EntityNotFoundException("Can't find book by id: " + id, e);
        }
    }

    @Override
    public List<Book> findAll() {
        try (Session session = sessionFactory.openSession()) {
            Query<Book> fromBook = session.createQuery("from Book", Book.class);
            return fromBook.getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Cant read from books", e);
        }
    }
}
