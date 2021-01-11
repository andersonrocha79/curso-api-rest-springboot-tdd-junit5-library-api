package br.com.rochasoft.libraryapi.api.exception;

import br.com.rochasoft.libraryapi.exception.BusinessException;
import org.springframework.validation.BindingResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErros
{

    private List<String> erros;

    public ApiErros(BindingResult bindingResult)
    {
        // preenche a lista de erros
        this.erros = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.erros.add(error.getDefaultMessage()));
    }

    public ApiErros(BusinessException ex)
    {
        // preenche a lista de erros
        this.erros = Arrays.asList(ex.getMessage());
    }

    public ApiErros(ResponseStatusException ex)
    {
        // preenche a lista de erros
        this.erros = Arrays.asList(ex.getReason());
    }

    public List<String> getErros()
    {
        return erros;
    }


}
