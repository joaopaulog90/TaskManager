package com.taskmanager.api.controller;

import com.taskmanager.api.dto.request.RequisicaoAlteracaoPerfil;
import com.taskmanager.api.dto.response.RespostaUsuario;
import com.taskmanager.api.repository.RepositorioUsuario;
import com.taskmanager.api.service.ServicoUsuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Consulta e administração de usuários")
public class ControladorUsuario {

    private final RepositorioUsuario repositorioUsuario;
    private final ServicoUsuario servicoUsuario;

    public ControladorUsuario(RepositorioUsuario repositorioUsuario,
                               ServicoUsuario servicoUsuario) {
        this.repositorioUsuario = repositorioUsuario;
        this.servicoUsuario = servicoUsuario;
    }

    @GetMapping
    @Operation(summary = "Lista todos os usuários (apenas ADMIN)")
    public ResponseEntity<List<RespostaUsuario>> listarTodos(
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        List<RespostaUsuario> lista = servicoUsuario.listarTodos(usuarioAutenticado.getUsername());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca usuário por email exato")
    public ResponseEntity<RespostaUsuario> buscarPorEmail(@RequestParam String email) {
        RespostaUsuario resposta = repositorioUsuario.buscarPorEmail(email)
                .map(RespostaUsuario::new)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + email));
        return ResponseEntity.ok(resposta);
    }

    @PatchMapping("/{id}/profile")
    @Operation(summary = "Altera o perfil de um usuário (apenas ADMIN)")
    public ResponseEntity<RespostaUsuario> alterarPerfil(
            @PathVariable Long id,
            @Valid @RequestBody RequisicaoAlteracaoPerfil requisicao,
            @AuthenticationPrincipal UserDetails usuarioAutenticado) {
        RespostaUsuario resposta = servicoUsuario.alterarPerfil(
                usuarioAutenticado.getUsername(), id, requisicao);
        return ResponseEntity.ok(resposta);
    }
}
