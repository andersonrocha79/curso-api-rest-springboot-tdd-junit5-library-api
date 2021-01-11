package br.com.rochasoft.libraryapi.api.resource;

import br.com.rochasoft.libraryapi.api.dto.BookDTO;
import br.com.rochasoft.libraryapi.api.dto.LoanDTO;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.entity.Loan;
import br.com.rochasoft.libraryapi.model.repository.LoanRepository;
import br.com.rochasoft.libraryapi.service.BookService;
import br.com.rochasoft.libraryapi.service.LoanService;
import br.com.rochasoft.libraryapi.service.impl.LoanServiceImpl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/books")
//@RequiredArgsConstructor
@Api("Book API")
@Slf4j // faz com a compilação da classe receba um objeto de log
public class BookController
{

    private BookService service;
    private LoanService loanService;
    private ModelMapper modelMapper;

    public BookController(BookService service, LoanService lService, ModelMapper modelMapper)
    {
        this.service = service;
        this.loanService = lService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("inclui um livro")
    public BookDTO create(@RequestBody @Valid BookDTO dto)
    {

        // @Slf4j
        log.info("criou um livro para o isbn {}", dto.getIsbn());

        final Book entity = modelMapper.map(dto, Book.class);

        service.save(entity);

        return modelMapper.map(entity, BookDTO.class);

    }

    @GetMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("busca os dados do livro por 'id'")
    public BookDTO get(@PathVariable long id)
    {

        log.info("buscou os dados do livro pelo id {}", id);

        return service
                .getById(id)
                .map(book -> modelMapper.map(book, BookDTO.class))
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiOperation("Exclui o livro por 'id'")
    @ApiResponses({ @ApiResponse(code = 204, message="Livro excluído com sucesso."),
                    @ApiResponse(code = 401, message="Não autorizado.")})
    public void delete(@PathVariable long id)
    {

        log.info("excluiu os dados do livro id {}", id);

        Book book = service
                .getById(id)
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        service.delete(book);
    }

    @PutMapping("{id}")
    @ResponseStatus(HttpStatus.OK)
    @ApiOperation("Atualiza os dados de um livro por 'id'")
    public BookDTO update( @PathVariable long id, BookDTO dto)
    {

        log.info("atualizou os dados do livro id {}", id);

        // busca o livro
        return service
                .getById(id)
                .map(book ->
                {
                    // atualiza os campos do objeto
                    book.setAuthor(dto.getAuthor());
                    book.setTitle(dto.getTitle());

                    // envia a atualização para o banco de dados
                    book = service.update(book);

                    // retorna o objeto 'atualizado'
                    return modelMapper.map(book, BookDTO.class);

                })
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    }

    @GetMapping
    @ApiOperation("Retorna a lista de livros cadastrados")
    public Page<BookDTO> find(BookDTO dto, Pageable pageRequest)
    {

        log.info("retornou a lista de livros {}", dto);

        Book filter = modelMapper.map(dto, Book.class);
        Page<Book> result = service.find(filter, pageRequest);

        List<BookDTO> list =  result.getContent()
                                    .stream() // gera um 'stream'
                                    .map(entity -> modelMapper.map(entity, BookDTO.class)) // faz a conversão para 'bookDTO'
                                    .collect(Collectors.toList()); // pega o resultado e gera uma coleção 'list'

        return new PageImpl<BookDTO> (list, pageRequest, result.getTotalElements());

    }

    // sub recurso de livros (empréstimos do livro passado como parâmetro
    @GetMapping("{id}/loans")
    public Page<LoanDTO> loansByBook(@PathVariable Long id, Pageable pageable)
    {

        log.info("retornou a lista de empréstimos do livro id {}", id);

        Book book = service.getById(id).orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Page<Loan> result =  loanService.getLoansByBook(book, pageable);

        List<LoanDTO> list =  result.getContent()
                             .stream() // gera um 'stream'
                             .map( loan ->
                             {

                                 Book loanBook   = loan.getBook();
                                 BookDTO bookDTO = modelMapper.map(loanBook, BookDTO.class);
                                 LoanDTO loanDTO = modelMapper.map(loan, LoanDTO.class);
                                 loanDTO.setBook(bookDTO);

                                 return loanDTO;

                             }) // faz a conversão para 'loanDTO'
                             .collect(Collectors.toList()); // pega o resultado e gera uma coleção 'list'

        return new PageImpl<LoanDTO> (list, pageable, result.getTotalElements());

    }

}
