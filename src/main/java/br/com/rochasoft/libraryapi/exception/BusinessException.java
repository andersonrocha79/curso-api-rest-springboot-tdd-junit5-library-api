package br.com.rochasoft.libraryapi.exception;

public class BusinessException extends RuntimeException
{

    public BusinessException(String s)
    {
        super(s);
    }
}
