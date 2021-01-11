package br.com.rochasoft.libraryapi.service;

import br.com.rochasoft.libraryapi.api.dto.LoanFilterDTO;
import br.com.rochasoft.libraryapi.exception.BusinessException;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.entity.Loan;
import br.com.rochasoft.libraryapi.model.repository.LoanRepository;
import br.com.rochasoft.libraryapi.service.impl.LoanServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class LoanServiceTest
{

    LoanService service;

    @MockBean
    LoanRepository repository;

    @BeforeEach
    public void setUp()
    {
        this.service = new LoanServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um empréstimo")
    public void saveLoanTest()
    {

        Book book = Book.builder().id(11).build();

        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                            .book(book)
                            .customer(customer)
                            .loanDate(LocalDate.now())
                            .build();

        Loan savedLoan = Loan.builder().id(11).book(book).loanDate(LocalDate.now()).customer(customer).build();

        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(false);
        Mockito.when(repository.save(savingLoan)).thenReturn(savedLoan);

        Loan loan = service.save(savingLoan);

        Assertions.assertThat(loan.getId()).isEqualTo(savedLoan.getId());
        Assertions.assertThat(loan.getBook().getId()).isEqualTo(savedLoan.getBook().getId());
        Assertions.assertThat(loan.getCustomer()).isEqualTo(savedLoan.getCustomer());
        Assertions.assertThat(loan.getLoanDate()).isEqualTo(savedLoan.getLoanDate());

    }

    @Test
    @DisplayName("Deve gerar erro ao tentar salvar um empréstimo de livro já emprestado")
    public void loanedBooksaveTest()
    {

        Book book = Book.builder().id(11).build();

        String customer = "Fulano";

        Loan savingLoan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        // faz a simulação para retornar 'true' quando executar o método 'existsByBookAndNotReturned'
        Mockito.when(repository.existsByBookAndNotReturned(book)).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(savingLoan));

        Assertions.assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Book already loaned");

        Mockito.verify(repository, Mockito.never()).save(savingLoan);

    }

    @Test
    @DisplayName("Deve obter as informações de empréstimo pelo id")
    public void getLoanDetailsTest()
    {

        // cenário
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);

        // simula que foi na base e trouxe o empréstimo do livro como resultado
        Mockito.when(repository.findById(id)).thenReturn(Optional.of(loan));

        // execução
        Optional<Loan> result = service.getById(id);

        // verificação
        Assertions.assertThat(result.isPresent()).isTrue();
        Assertions.assertThat(result.get().getId()).isEqualTo(id);
        Assertions.assertThat(result.get().getCustomer()).isEqualTo(loan.getCustomer());
        Assertions.assertThat(result.get().getBook()).isEqualTo(loan.getBook());
        Assertions.assertThat(result.get().getLoanDate()).isEqualTo(loan.getLoanDate());

        Mockito.verify(repository).findById(id);

    }

    @Test
    @DisplayName("Deve atualizar um empréstimo")
    public void updateLoanTest()
    {

        // cenário
        Long id = 1l;
        Loan loan = createLoan();
        loan.setId(id);
        loan.setReturned(true);

        Mockito.when(repository.save(loan)).thenReturn(loan);

        Loan updatedLoan = service.update(loan);

        Assertions.assertThat(updatedLoan.getReturned()).isTrue();
        Mockito.verify(repository).save(loan);


    }

    public static Loan createLoan()
    {

        Book book = Book.builder().id(11).build();

        String customer = "Fulano";

        Loan loan = Loan.builder()
                .book(book)
                .customer(customer)
                .loanDate(LocalDate.now())
                .build();

        return loan;

    }

    @Test
    @DisplayName("Deve filtrar empréstimos pelas propriedades")
    public void findLoanTest()
    {

        // cenário
        LoanFilterDTO loanFilterDTO = LoanFilterDTO.builder().customer("Fulano").isbn("321").build();
        Loan loan = createLoan();
        loan.setId(1);

        PageRequest pageRequest = PageRequest.of(0, 10);

        List<Loan> lista = Arrays.asList(loan);
        Page<Loan> page = new PageImpl<Loan>(lista, pageRequest, 1);

        Mockito.when(repository.findByBookIsbnOrCustomer(Mockito.anyString(), Mockito.anyString(), Mockito.any(PageRequest.class))).thenReturn(page);

        // execução
        Page<Loan> results = service.find(loanFilterDTO, pageRequest);

        // verificações
        Assertions.assertThat(results.getTotalElements()).isEqualTo(1);
        Assertions.assertThat(results.getContent()).isEqualTo(lista);
        Assertions.assertThat(results.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(results.getPageable().getPageSize()).isEqualTo(10);

    }


}
