package br.com.rochasoft.libraryapi.model.repository;

import br.com.rochasoft.libraryapi.model.entity.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest                            // indica que irá fazer testes com 'jpa' (cria banco dados em memória para executar os testes) (h2 database)
public class BookRepositoryTest
{

    @Autowired
    TestEntityManager entityManager;

    @Autowired
    BookRepository repository;

    @Test
    @DisplayName("Deve retornar true porque o isbn já existe na base")
    public void returnTrueWhenIsbnExists()
    {

        // cenário
        String isbn = "123";

        // grava um livro
        Book book = createNewBook(isbn);
        entityManager.persist(book);

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação (tem que retornar verdadeiro)
        Assertions.assertThat(exists).isTrue();

    }

    public static Book createNewBook(String isbn)
    {
        return Book.builder().title("Aventuras").author("Fulano").isbn(isbn).build();
    }

    @Test
    @DisplayName("Deve retornar falso porque o isbn não existe na base")
    public void returnFalseWhenIsbnDoesntExists()
    {

        // cenário
        String isbn = "123";

        // execução
        boolean exists = repository.existsByIsbn(isbn);

        // verificação (tem que retornar falso)
        Assertions.assertThat(exists).isFalse();

    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void findByIdTest()
    {
        // cenário
        Book book = createNewBook("123");
        entityManager.persist(book);

        // execução
        Optional<Book> foundBook = repository.findById(book.getId());

        // verificação
        Assertions.assertThat(foundBook.isPresent()).isTrue();
        Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());

    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest()
    {

        Book book = createNewBook("123");
        Book savedBook = repository.save(book);

        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getId()).isGreaterThan(0);

    }

    @Test
    @DisplayName("Deve excluir um livro")
    public void deleteBookTest()
    {

        // grava um livro
        Book book = createNewBook("123");
        book = entityManager.persist(book);

        Book foundBook = entityManager.find(Book.class, book.getId());

        repository.delete(foundBook);

        Book deletedBook = entityManager.find(Book.class, book.getId());

        Assertions.assertThat(deletedBook).isNull();

    }
}
