package br.com.rochasoft.libraryapi.service;

import java.util.List;

// servidor de email > https://mailtrap.io/
// logado com a conta do github
public interface EmailService
{

    void sendMails(String assunto, String mensagem, List<String> mailsList);

}
