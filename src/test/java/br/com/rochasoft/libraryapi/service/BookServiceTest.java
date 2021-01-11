package br.com.rochasoft.libraryapi.service;

import br.com.rochasoft.libraryapi.exception.BusinessException;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.repository.BookRepository;
import br.com.rochasoft.libraryapi.service.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest
{

    BookService     service;

    @MockBean
    BookRepository  repository;

    @BeforeEach
    public void setup()
    {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro")
    public void saveBookTest()
    {

        // cenário
        Book book = createValidBook();

        // coloca uma regra para este cenário
        // retorna 'false' ao executar a função 'repository.existsByIsbn'
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(false);

        // simula a utilização do método 'save' do 'repository'
        Mockito.when(repository.save(book)).thenReturn(Book.builder().id(11).isbn("123").title("As Aventuras").author("Fulano").build());

        // execução
        Book savedBook = service.save(book);

        // verificação
        Assertions.assertThat(savedBook.getId()).isNotNull();
        Assertions.assertThat(savedBook.getIsbn()).isEqualTo("123");
        Assertions.assertThat(savedBook.getTitle()).isEqualTo("As Aventuras");
        Assertions.assertThat(savedBook.getAuthor()).isEqualTo("Fulano");

    }

    private Book createValidBook()
    {
        return Book.builder().isbn("123").author("Fulano").title("As Aventuras").build();
    }

    @Test
    @DisplayName("Deve gerar erro ao tentar registrar um livro com ISBN duplicado")
    public void shouldNotSaveABookWithDuplicatedISBN()
    {

        // cenário
        Book book = createValidBook();

        // coloca uma regra para este cenário
        // retorna 'true' ao executar a função 'repository.existsByIsbn'
        Mockito.when(repository.existsByIsbn(Mockito.anyString())).thenReturn(true);

        // execução
        Throwable exception = Assertions.catchThrowable( () -> service.save(book));

        // verifica se o erro foi gerado (isbn já cadastrado)
        Assertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já cadastrado");

        // verifica se nunca irá executar o método salvar com o parametro 'book'
        Mockito.verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve obter um livro por id")
    public void getByIdTest()
    {

        // cenário
        long id = 11;
        Book book = createValidBook();
        book.setId(11);

        Mockito.when(repository.findById(id)).thenReturn(Optional.of(book));

        // excecução
        Optional<Book> foundBook = service.getById(id);

        // verificações
        Assertions.assertThat(foundBook.isPresent()).isTrue();
        Assertions.assertThat(foundBook.get().getId()).isEqualTo(id);
        Assertions.assertThat(foundBook.get().getAuthor()).isEqualTo(book.getAuthor());
        Assertions.assertThat(foundBook.get().getTitle()).isEqualTo(book.getTitle());
        Assertions.assertThat(foundBook.get().getIsbn()).isEqualTo(book.getIsbn());

    }

    @Test
    @DisplayName("Deve retornar vazio ao obter  um livro por id inexistente")
    public void bookNotFoundByIdTest()
    {

        // cenário
        long id = 11;

        // retorna 'vazio' quando executar a função 'repository.findById'
        Mockito.when(repository.findById(id)).thenReturn(Optional.empty());

        // excecução
        Optional<Book> book = service.getById(id);

        // verificações
        Assertions.assertThat(book.isPresent()).isFalse();

    }

    @Test
    @DisplayName("Deve excluir um livro")
    public void deleteBookTest()
    {

        // cenário
        Book book = Book.builder().id(11).build();

        // atualizar
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> service.delete(book));

        // verificação
        Mockito.verify(repository, times(1)).delete(book);

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar excluir um livro inexistente")
    public void deleteInvalidBookTest()
    {

        // cenário
        Book book = new Book();

        // atualizar
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.delete(book));

        // verificação
        Mockito.verify(repository, Mockito.never()).delete(book);

    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest()
    {

        // cenário
        long id = 11;
        // livro a atualizar
        Book updatingBook = Book.builder().id(id).build();
        // livro atualizado
        Book updatedBook = createValidBook();
        updatedBook.setId(id);
        Mockito.when(repository.save(updatingBook)).thenReturn(updatedBook);

        // excecução
        Book book = service.update(updatingBook);

        // verificações
        Assertions.assertThat(book.getId()).isEqualTo(updatedBook.getId());
        Assertions.assertThat(book.getIsbn()).isEqualTo(updatedBook.getIsbn());
        Assertions.assertThat(book.getTitle()).isEqualTo(updatedBook.getTitle());
        Assertions.assertThat(book.getAuthor()).isEqualTo(updatedBook.getAuthor());

    }

    @Test
    @DisplayName("Deve retornar erro ao tentar atualizar um livro inexistente")
    public void updateInvalidBookTest()
    {

        // cenário
        Book book = new Book();

        // atualizar
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class, () -> service.update(book));

        // verificação
        Mockito.verify(repository, Mockito.never()).save(book);

    }

    @Test
    @DisplayName("Deve filtrar os livros pelas propriedades")
    public void findBookTest()
    {

        // cenário
        Book book = createValidBook();

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);

        Mockito.when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class))).thenReturn(page);

        // execução
        Page<Book> results = service.find(book, pageRequest);

        // verificações
        Assertions.assertThat(results.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(results.getContent()).isEqualTo(lista);
        Assertions.assertThat(results.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(results.getPageable().getPageSize()).isEqualTo(10);

    }

    @Test
    @DisplayName("Deve obter um livro pelo isbn")
    public void getBookByIsbn()
    {

        String isbn = "1230";

        // simula que o serviço irá retornar o livro
        Mockito.when(repository.findByIsbn(isbn)).thenReturn(Optional.of(Book.builder().id(11).isbn("1230").build()));

        Optional<Book> book = service.getBookByIsbn(isbn);

        Assertions.assertThat(book.isPresent()).isTrue();
        Assertions.assertThat(book.get().getId()).isEqualTo(11);
        Assertions.assertThat(book.get().getIsbn()).isEqualTo(isbn);
        Mockito.verify(repository, times(1)).findByIsbn(isbn);


    }

}
