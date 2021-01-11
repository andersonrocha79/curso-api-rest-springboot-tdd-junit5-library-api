package br.com.rochasoft.libraryapi.api.resource;

import br.com.rochasoft.libraryapi.api.dto.BookDTO;
import br.com.rochasoft.libraryapi.exception.BusinessException;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.service.BookService;
import br.com.rochasoft.libraryapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest(controllers = BookController.class)
@AutoConfigureMockMvc
public class BookControllerTest
{

    static String BOOK_API = "/api/books";

    @Autowired
    MockMvc mvc;

    @MockBean
    BookService service;

    @MockBean
    LoanService loanService;

    @Test
    @DisplayName("Deve criar um livro com sucesso.")
    public void createBookTest() throws Exception
    {

        BookDTO dto = createBook();

        Book savedBook = Book.builder()
                             .id(100)
                             .author("Arthur")
                             .title("As aventuras")
                             .isbn("001")
                             .build();

        BDDMockito.given(service.save(Mockito.any(Book.class))).willReturn(savedBook);

        String json = new ObjectMapper().writeValueAsString(dto);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                                                .post(BOOK_API)
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .accept(MediaType.APPLICATION_JSON)
                                                .content(json);

        mvc.perform(request)
           .andExpect(status().isCreated())
           .andExpect(jsonPath("id").value(dto.getId()))
           .andExpect(jsonPath("title").value(dto.getTitle()))
           .andExpect(jsonPath("author").value(dto.getAuthor()))
           .andExpect(jsonPath("isbn").value(dto.getIsbn()));

    }

    private BookDTO createBook()
    {
        return BookDTO.builder()
                .author("Arthur")
                .title("As aventuras")
                .isbn("001")
                .build();
    }

    @Test
    @DisplayName("Deve gerar erro ao tentar criar um livro com dados incompletos.")
    public void createInvalidBookTest() throws Exception
    {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
           .andExpect( status().isBadRequest())
           .andExpect( jsonPath("erros", Matchers.hasSize(3)));
    }

    @Test
    @DisplayName("Deve gerar erro ao tentar criar um livro com ISBN já utilizado")
    public void createBookWithDuplicatedIsbn() throws Exception
    {

        BookDTO dto = createBook();

        String json = new ObjectMapper().writeValueAsString(dto);

        String mensagemErro = "Isbn já cadastrado";

        // simulação de geração do erro 'isbn já cadastrado' quando o 'service.save' for executado
        BDDMockito.given(service.save(Mockito.any(Book.class))).willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest())
                .andExpect( jsonPath("erros", Matchers.hasSize(1)))
                .andExpect( jsonPath("erros[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve obter informações de um livro")
    public void getBookDetailsTest() throws Exception
    {

        // cenário (given)
        long id = 11;

        Book bookDto = Book
                        .builder()
                        .id(id)
                        .author("Arthur")
                        .title("As aventuras")
                        .isbn("001")
                        .build();

        Book book = Book.builder()
                            .id(bookDto.getId())
                            .title(bookDto.getTitle())
                            .author(bookDto.getAuthor())
                            .isbn(bookDto.getIsbn()).build();

        BDDMockito.given(service.getById(id)).willReturn(Optional.of(book));

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(bookDto.getId()))
                .andExpect(jsonPath("title").value(bookDto.getTitle()))
                .andExpect(jsonPath("author").value(bookDto.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDto.getIsbn()));

    }

    @Test
    @DisplayName("Deve retornar 'resource not found' quando o livro procurado não existir")
    public void bookNotFoundTest() throws Exception
    {

        // cenário
        // sempre que o serviço executar 'getById' irá retornar 'empty'
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat("/" + 1))
                .accept(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform(request).andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve deletar um livro")
    public void deleteBookTest() throws Exception
    {

        // cenário
        // se executar a função 'service.getbyid' irá retornar um book com id '11'
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.of(Book.builder().id(11).build()));

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 11))
                .accept(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform( request )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Deve retornar 'resource not found' quando tentar retornar um livro inexistente")
    public void deleteInexistentBookTest() throws Exception
    {

        // cenário
        // se executar a função 'service.getbyid' não irá retornar um livro
        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(BOOK_API.concat("/" + 11))
                .accept(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform( request )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() throws Exception
    {
        // cenário
        // se executar a função 'service.getbyid' irá retornar um book com id '11'

        long id = 11;
        String json = new ObjectMapper().writeValueAsString(createBook());

        Book bookDto = Book
                .builder()
                .id(id)
                .author("Arthur")
                .title("As aventuras")
                .isbn("001")
                .build();

        Book updateBook = Book.builder().id(id).title("algum titulo").author("algum ator").build();

        // quando executar 'service.getByid' retorna o livro atual
        BDDMockito.given(service.getById(id)).willReturn(Optional.of(updateBook));

        // quando executar 'service.update', retorna o livro atual
        BDDMockito.given(service.update(updateBook)).willReturn(bookDto);

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + id))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform( request )
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(bookDto.getId()))
                .andExpect(jsonPath("title").value(bookDto.getTitle()))
                .andExpect(jsonPath("author").value(bookDto.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDto.getIsbn()));
    }

    @Test
    @DisplayName("Deve retornar 'resource not found' quando tentar atualizar um livro inexistente")
    public void updateInexistentBookTest() throws Exception
    {

        // cenário
        // se executar a função 'service.getbyid' irá retornar um book com id '11'

        String json = new ObjectMapper().writeValueAsString(createBook());

        BDDMockito.given(service.getById(Mockito.anyLong())).willReturn(Optional.empty());

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(BOOK_API.concat("/" + 1))
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform( request )
                .andExpect(status().isNotFound());

    }

    @Test
    @DisplayName("Deve filtrar os livros")
    public void findBooksTest() throws Exception
    {

        long id = 11;

        Book book = Book.builder()
                .id(id)
                .title(createBook().getTitle())
                .author(createBook().getAuthor())
                .isbn(createBook().getIsbn())
                .build();

        BDDMockito.given( service.find(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1));


        // /api/books?
        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                                            book.getTitle(),
                                            book.getAuthor());

        // execução (when)
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(BOOK_API.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        // verificação
        mvc.perform( request )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content", Matchers.hasSize(1)))
                .andExpect(jsonPath("totalElements").value(1))
                .andExpect(jsonPath("pageable.pageSize").value(100))
                .andExpect(jsonPath("pageable.pageNumber").value(0));
    }

}
