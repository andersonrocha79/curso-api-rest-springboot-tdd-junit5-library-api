package br.com.rochasoft.libraryapi.model.repository;

import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.entity.Loan;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.List;

import static br.com.rochasoft.libraryapi.model.repository.BookRepositoryTest.createNewBook;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@DataJpaTest    // indica que vai fazer teste de integração com banco em memória
public class LoanRepositoryTest
{

    @Autowired
    private LoanRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Deve verificar se existe empréstimo não devolvido para o livro")
    public void existsByBookAndNotReturnedTest()
    {

        // cenário

        // cria um livro
        Loan loan = createAndPersistLoan(LocalDate.now());
        Book book = loan.getBook();

        // execução
        boolean exists = repository.existsByBookAndNotReturned(book);

        // verificação
        Assertions.assertThat(exists).isTrue();

    }

    @Test
    @DisplayName("Deve buscar empréstimo pelo isbn do livro ou customer")
    public void findByBookIsbnOrCustomerTest()
    {

        // cenário
        // cria um livro e um empréstimo com este livro
        Loan loan = createAndPersistLoan(LocalDate.now());

        // seleciona o único registro que foi incluído
        Page<Loan> result = repository.findByBookIsbnOrCustomer("123", "Fulano", PageRequest.of(0, 10));

        // verificaçõs
        Assertions.assertThat(result.getContent()).hasSize(1);
        Assertions.assertThat(result.getContent()).contains(loan);
        Assertions.assertThat(result.getPageable().getPageSize()).isEqualTo(10);
        Assertions.assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        Assertions.assertThat(result.getTotalElements()).isEqualTo(1);

    }

    @Test
    @DisplayName("Deve obter empréstimos cuja data de empréstimo for menor ou igual a 3 dias atrás e não retornado")
    public void findByLoanDateLessThanAndNotReturnedTest()
    {

        // cenário
        // inclui um empréstimo já 'atrasado'
        Loan loan = createAndPersistLoan(LocalDate.now().minusDays(5));

        // execução
        // busca os emprestimos vencidos
        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        // verificação
        Assertions.assertThat(result).hasSize(1);

    }

    @Test
    @DisplayName("Deve obter a lista vazia de empréstimos atrasados porque todos os empréstimos realizados estão em dia")
    public void notFindByLoanDateLessThanAndNotReturnedTest()
    {

        // cenário
        // inclui um empréstimo com data atual
        Loan loan1 = createAndPersistLoan(LocalDate.now());
        Loan loan2 = createAndPersistLoan(LocalDate.now().minusDays(1));
        Loan loan3 = createAndPersistLoan(LocalDate.now().minusDays(2));
        Loan loan4 = createAndPersistLoan(LocalDate.now().minusDays(3));

        // execução
        // busca os emprestimos vencidos
        List<Loan> result = repository.findByLoanDateLessThanAndNotReturned(LocalDate.now().minusDays(4));

        // verificação
        Assertions.assertThat(result).isEmpty();

    }

    public Loan createAndPersistLoan(LocalDate loanDate)
    {

        // cria um livro
        Book book = createNewBook("123");
        entityManager.persist(book);

        // registra o empréstimo do livro
        Loan loan = Loan.builder().book(book).customer("Fulano").loanDate(loanDate).build();
        entityManager.persist(loan);

        return loan;

    }

}
