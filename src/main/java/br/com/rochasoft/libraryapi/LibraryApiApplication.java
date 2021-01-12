package br.com.rochasoft.libraryapi;

import br.com.rochasoft.libraryapi.service.EmailService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;
import java.util.List;

// https://docs.spring.io/spring-integration/docs/current/reference/html/index.html
// https://swagger.io/
// monitoramento > localhost:8080/actuator
// arquivo de log > http://localhost:8080/actuator/logfile

// gerando o pacote e executando
// mvnw clean package
// java -jar library-api-0.0.2-SNAPSHOT.jar
// http://localhost:8080/swagger-ui.html

@SpringBootApplication
@EnableScheduling			// habilita o agendamento de tarefas
public class LibraryApiApplication extends SpringBootServletInitializer
{

	/*

	// este código habilita o teste para envio de email

	@Autowired
	private EmailService emailService;

	@Bean
	// executa sempre que subir a aplicação todos os métodos do commandline runner
	public CommandLineRunner runner()
	{
		return args ->
		{
			List<String> emails = Arrays.asList("library-api-c34aa0@inbox.mailtrap.io");
			// https://mailtrap.io/ (login com git)
			emailService.sendMails("teste", "testando serviço de emails", emails);
			System.out.printf("email enviado para mailtrap.io");
		};
	}

    */

	@Bean
	public ModelMapper modelMapper()
	{
		return new ModelMapper();
	}

	public static void main(String[] args)
	{
		SpringApplication.run(LibraryApiApplication.class, args);
	}

}
