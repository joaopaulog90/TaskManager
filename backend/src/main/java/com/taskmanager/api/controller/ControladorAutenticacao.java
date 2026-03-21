package com.taskmanager.api.controller;

import com.taskmanager.api.dto.request.RequisicaoCadastro;
import com.taskmanager.api.dto.request.RequisicaoLogin;
import com.taskmanager.api.dto.response.RespostaAutenticacao;
import com.taskmanager.api.dto.response.RespostaUsuario;
import com.taskmanager.api.service.ServicoAutenticacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticação e registro")
public class ControladorAutenticacao {

    private final ServicoAutenticacao servicoAutenticacao;

    public ControladorAutenticacao(ServicoAutenticacao servicoAutenticacao) {
        this.servicoAutenticacao = servicoAutenticacao;
    }

    @PostMapping("/register")
    @Operation(summary = "Registra um novo usuário")
    public ResponseEntity<RespostaUsuario> cadastrar(@Valid @RequestBody RequisicaoCadastro requisicao) {
        RespostaUsuario resposta = servicoAutenticacao.cadastrar(requisicao);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/login")
    @Operation(summary = "Autentica o usuário e retorna o token JWT")
    public ResponseEntity<RespostaAutenticacao> autenticar(@Valid @RequestBody RequisicaoLogin requisicao) {
        RespostaAutenticacao resposta = servicoAutenticacao.autenticar(requisicao);
        return ResponseEntity.ok(resposta);
    }
}
