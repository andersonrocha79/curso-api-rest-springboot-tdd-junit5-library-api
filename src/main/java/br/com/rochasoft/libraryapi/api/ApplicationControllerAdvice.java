package br.com.rochasoft.libraryapi.api;

import br.com.rochasoft.libraryapi.api.exception.ApiErros;
import br.com.rochasoft.libraryapi.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice // indica que esta classe terá funções globais para todas as apis
public class ApplicationControllerAdvice
{

    // este método será executado sempre que esta exception for gerada no 'create' (por causa da diretiva @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleValidationExceptions(MethodArgumentNotValidException ex)
    {

        BindingResult bindingResult = ex.getBindingResult();

        return new ApiErros(bindingResult);

    }

    // este método será executado sempre que falhar validação de dados no create (por causa da diretiva @Valid)
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErros handleBusinessException(BusinessException ex)
    {
        return new ApiErros(ex);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity handleResponseStatusException(ResponseStatusException ex)
    {
        return new ResponseEntity(new ApiErros(ex), ex.getStatus());
    }

}
