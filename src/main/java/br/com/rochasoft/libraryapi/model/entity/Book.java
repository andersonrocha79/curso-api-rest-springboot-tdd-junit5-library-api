package br.com.rochasoft.libraryapi.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity // indica que esta classe é uma entidade JPA
@Table(name = "tbBook")  // Tabela no banco
public class Book
{

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // auto incremento da chave
    private long   id;

    // @Column(name = "title") pode definir os parâmetros na diretiva se for necessário
    @Column
    private String title;

    @Column
    private String author;

    @Column
    private String isbn;

    @OneToMany( mappedBy = "book", fetch = FetchType.LAZY) // EAGER-busca todos automaticamente // o livro tem o relacionamento de um para muitos com a entidade 'loans'
    private List<Loan> loans;


}
