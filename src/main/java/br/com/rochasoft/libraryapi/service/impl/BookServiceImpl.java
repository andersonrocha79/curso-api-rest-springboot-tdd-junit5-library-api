package br.com.rochasoft.libraryapi.service.impl;

import br.com.rochasoft.libraryapi.exception.BusinessException;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.repository.BookRepository;
import br.com.rochasoft.libraryapi.service.BookService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BookServiceImpl implements BookService
{

    private BookRepository repository;

    public BookServiceImpl(BookRepository repository)
    {
        this.repository = repository;
    }

    @Override
    public Book save(Book book)
    {

        // verifica se o isbn já está cadastrado
        if (repository.existsByIsbn(book.getIsbn()))
        {
            throw new BusinessException("Isbn já cadastrado");
        }

        return repository.save(book);

    }

    @Override
    public Optional<Book> getById(long id)
    {
        return this.repository.findById(id);
    }

    @Override
    public void delete(Book book)
    {

        if (book == null || book.getId() <= 0 )
        {
            throw new IllegalArgumentException("O id do livro deve ser informado");
        }

        this.repository.delete(book);

    }

    @Override
    public Book update(Book book)
    {

        if (book == null || book.getId() <= 0 )
        {
            throw new IllegalArgumentException("O id do livro deve ser informado");
        }

        return this.repository.save(book);

    }

    @Override
    public Page<Book> find(Book filter, Pageable pageRequest)
    {
        Example<Book> example = Example.of(filter, ExampleMatcher
                                                    .matching()
                                                    .withIgnoreCase()
                                                    .withIgnoreNullValues()
                                                    .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING));

        return repository.findAll(example, pageRequest);
    }

    @Override
    public Optional<Book> getBookByIsbn(String isbn)
    {
        return repository.findByIsbn(isbn);
    }

}
