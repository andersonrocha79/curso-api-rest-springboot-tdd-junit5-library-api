package br.com.rochasoft.libraryapi.service.impl;

import br.com.rochasoft.libraryapi.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService
{

    private final JavaMailSender javaMailSender;

    @Value("${application.mail.default-remetente}")
    private String remetente;

    @Override
    public void sendMails(String assunto, String mensagem, List<String> mailsList)
    {

        // converte a lista de emails em array de string
        String[] listaEmails = mailsList.toArray(new String[mailsList.size()]);

        SimpleMailMessage mailMessage = new SimpleMailMessage();

        // remetente
        mailMessage.setFrom(remetente);

        // assunto
        mailMessage.setSubject(assunto);

        // mensagem
        mailMessage.setText(mensagem);

        // lista de emails
        mailMessage.setTo(listaEmails);

        // envia a mensagem
        javaMailSender.send(mailMessage);

    }

}
