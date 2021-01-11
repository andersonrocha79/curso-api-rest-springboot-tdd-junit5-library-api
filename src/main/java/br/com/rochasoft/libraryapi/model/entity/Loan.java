package br.com.rochasoft.libraryapi.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity     // diz que esta classe é uma entidade do banco de dados
@Table(name = "tbLoan")  // Tabela no banco
public class Loan
{

    @Id  // indica que é a chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY) // indica que o valor será gerado automaticamente
    @Column
    private long id;

    @Column(length = 100, nullable = false)
    @NotNull
    private String customer;

    @Column(name = "customer_email")
    private String customerEmail;

    @JoinColumn(name = "id_book")
    @ManyToOne          // muitos empréstimos para o mesmo livro
    private Book book;

    @Column
    private LocalDate loanDate;

    @Column
    private Boolean returned;

}
