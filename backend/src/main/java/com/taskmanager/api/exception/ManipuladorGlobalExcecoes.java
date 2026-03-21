package com.taskmanager.api.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ManipuladorGlobalExcecoes {

    private static final Logger log = LoggerFactory.getLogger(ManipuladorGlobalExcecoes.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidacao(MethodArgumentNotValidException excecao) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setType(URI.create("https://api.taskmanager.com/errors/validation"));
        problem.setTitle("Erro de validação");

        List<Map<String, String>> erros = excecao.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> Map.of(
                        "campo", fieldError.getField(),
                        "mensagem", mensagemOuPadrao(fieldError)
                ))
                .collect(Collectors.toList());

        problem.setProperty("erros", erros);
        return problem;
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ProblemDetail handleUsernameNotFound(UsernameNotFoundException excecao) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setType(URI.create("https://api.taskmanager.com/errors/unauthorized"));
        problem.setTitle("Não autorizado");
        problem.setDetail(excecao.getMessage());
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException excecao) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problem.setType(URI.create("https://api.taskmanager.com/errors/unauthorized"));
        problem.setTitle("Credenciais inválidas");
        problem.setDetail("Email ou senha incorretos");
        return problem;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException excecao) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problem.setType(URI.create("https://api.taskmanager.com/errors/forbidden"));
        problem.setTitle("Acesso negado");
        problem.setDetail(excecao.getMessage());
        return problem;
    }

    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ProblemDetail handleNotFound(RuntimeException excecao) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setType(URI.create("https://api.taskmanager.com/errors/not-found"));
        problem.setTitle("Recurso não encontrado");
        problem.setDetail(excecao.getMessage());
        return problem;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleRegraDeNegocio(IllegalStateException excecao) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        problem.setType(URI.create("https://api.taskmanager.com/errors/business-rule"));
        problem.setTitle("Regra de negócio violada");
        problem.setDetail(excecao.getMessage());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenerico(Exception excecao) {
        log.error("Erro interno não tratado", excecao);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setType(URI.create("https://api.taskmanager.com/errors/internal"));
        problem.setTitle("Erro interno");
        problem.setDetail("Ocorreu um erro inesperado. Tente novamente mais tarde.");
        return problem;
    }

    private String mensagemOuPadrao(FieldError fieldError) {
        String mensagem = fieldError.getDefaultMessage();
        return (mensagem != null) ? mensagem : "Campo inválido";
    }
}
