package br.com.rochasoft.libraryapi.model.repository;

import br.com.rochasoft.libraryapi.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>
{

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

}
