package br.com.rochasoft.libraryapi.service;

import br.com.rochasoft.libraryapi.model.entity.Loan;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService
{

    // segundo, minuto, hora, dia, mes, ano
    // http://www.cronmaker.com/
    private static final String CRON_LATE_LOANS = "0 0 13 1/1 * ?"; // executa todos os dias as 13:00

    private final LoanService   loanService;
    private final EmailService  emailService;

    // busca a mensagem no arquivo 'resource' application.properties
    @Value("${application.mail.lateloans.message}")
    private String mensagem;

    @Value("${application.mail.lateloans.subject}")
    private String assunto;

    @Scheduled(cron = CRON_LATE_LOANS)
    public void sendEmailToLateLoans()
    {

        // busca os empréstimos atrasados
        List<Loan> allLateLoans = loanService.getAllLateLoans();

        // cria uma lista de emails com o campo 'email' da lista de 'loans'
        List<String> mailsList = allLateLoans.stream().map(loan -> loan.getCustomerEmail()).collect(Collectors.toList());

        // envia emails para os clientes com emprestimos atrasados
        emailService.sendMails(assunto, mensagem, mailsList);

    }

}
