package br.com.rochasoft.libraryapi.api.resource;

import br.com.rochasoft.libraryapi.api.dto.BookDTO;
import br.com.rochasoft.libraryapi.api.dto.LoanDTO;
import br.com.rochasoft.libraryapi.api.dto.LoanFilterDTO;
import br.com.rochasoft.libraryapi.api.dto.ReturnedLoanDTO;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.entity.Loan;
import br.com.rochasoft.libraryapi.service.BookService;
import br.com.rochasoft.libraryapi.service.LoanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Api("Loan API")
public class LoanController
{

    private final LoanService service;
    private final BookService bookService;
    private final ModelMapper modelMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation("inclui um novo empréstimo de um livro")
    public long create(@RequestBody LoanDTO dto)
    {

        Book book = bookService
                .getBookByIsbn(dto.getIsbn())
                .orElseThrow( () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book not found for passed isbn"));

        Loan entity = Loan.builder()
                .book(book)
                .customer(dto.getCustomer())
                .loanDate(LocalDate.now())
                .build();

        entity = service.save(entity);

        return entity.getId();

    }

    @PatchMapping("{id}")
    @ApiOperation("Registra a devolução de um livro emprestado")
    public void returnBook(@PathVariable Long id, @RequestBody ReturnedLoanDTO dto)
    {

        Loan loan = service.getById(id)
                           .orElseThrow( () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found for passed isbn"));
        loan.setReturned(dto.isReturned());

        service.update(loan);

    }

    @GetMapping
    @ApiOperation("Retorna o histórico de empréstimos de um livro")
    public Page<LoanDTO> find(LoanFilterDTO dto, Pageable pageRequest)
    {

        Page<Loan> result = service.find(dto, pageRequest);

        List<LoanDTO> list =  result
                .getContent()
                .stream() // gera um 'stream'
                .map(entity ->
                {
                    Book book = entity.getBook();
                    BookDTO bookDto = modelMapper.map(book, BookDTO.class);
                    LoanDTO loanDto = modelMapper.map(entity, LoanDTO.class);
                    loanDto.setBook(bookDto);
                    return loanDto;
                }) // faz a conversão para 'LoanDTO', embutindo também o book convertido
                .collect(Collectors.toList()); // pega o resultado e gera uma coleção 'list'

        return new PageImpl<LoanDTO>(list, pageRequest, result.getTotalElements());

        // porque temos o BookDTO e o Book
        // O Book representa a entidade ou tabela do banco de dados com todos os campos
        // O BookDTO representa o json que será enviado ao cliente, e pode ter apenas campos que o cliente pode ver
        // Se for Usuario por exemplo, o 'Usuario' teria o campo 'senha', mas o UsuarioDTO poderia não ter este campos

    }

}
