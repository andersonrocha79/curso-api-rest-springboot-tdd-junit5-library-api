package br.com.rochasoft.libraryapi.service.impl;

import br.com.rochasoft.libraryapi.api.dto.LoanFilterDTO;
import br.com.rochasoft.libraryapi.exception.BusinessException;
import br.com.rochasoft.libraryapi.model.entity.Book;
import br.com.rochasoft.libraryapi.model.entity.Loan;
import br.com.rochasoft.libraryapi.model.repository.LoanRepository;
import br.com.rochasoft.libraryapi.service.LoanService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class LoanServiceImpl implements LoanService
{

    private LoanRepository repository;

    public LoanServiceImpl(LoanRepository repository)
    {

        this.repository = repository;
    }

    @Override
    public Loan save(Loan loan)
    {
        if (repository.existsByBookAndNotReturned(loan.getBook()))
        {
            throw new BusinessException("Book already loaned");
        }

        return repository.save(loan);
    }

    @Override
    public Optional<Loan> getById(Long id)
    {
        return repository.findById(id);
    }

    @Override
    public Loan update(Loan loan)
    {
        return repository.save(loan);
    }

    @Override
    public Page<Loan> find(LoanFilterDTO filter, Pageable pageable)
    {
        return repository.findByBookIsbnOrCustomer(filter.getIsbn(), filter.getCustomer(), pageable);
    }

    @Override
    public Page<Loan> getLoansByBook(Book book, Pageable pageable)
    {
        // se passar o 'pageable' como segundo parametro
        // de um 'querymethod', vai entender que a consulta é paginada
        return repository.findByBook(book, pageable);
    }

    @Override
    public List<Loan> getAllLateLoans()
    {

        // número de dias para o livro ser considerado atrasado
        final Integer loanDays = 4;

        // calcula uma data com 3 dias atrás
        LocalDate threeDaysAgo = LocalDate.now().minusDays(loanDays);

        // busca os livros atrasados
        return repository.findByLoanDateLessThanAndNotReturned(threeDaysAgo);

    }

}
