package com.taskmanager.api.controller;

import com.taskmanager.api.dto.request.RequisicaoMembro;
import com.taskmanager.api.dto.request.RequisicaoProjeto;
import com.taskmanager.api.dto.response.RespostaMembro;
import com.taskmanager.api.dto.response.RespostaProjeto;
import com.taskmanager.api.service.ServicoProjeto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projetos", description = "CRUD de projetos e membros")
public class ControladorProjeto {

    private final ServicoProjeto servicoProjeto;

    public ControladorProjeto(ServicoProjeto servicoProjeto) {
        this.servicoProjeto = servicoProjeto;
    }

    @PostMapping
    @Operation(summary = "Cria um novo projeto — o criador vira ADMIN automaticamente")
    public ResponseEntity<RespostaProjeto> criar(
            @Valid @RequestBody RequisicaoProjeto requisicao,
            @AuthenticationPrincipal UserDetails userDetails) {
        RespostaProjeto resposta = servicoProjeto.criar(requisicao, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Lista todos os projetos do usuário autenticado")
    public ResponseEntity<List<RespostaProjeto>> listar(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoProjeto.listarDoUsuario(userDetails.getUsername()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Retorna um projeto pelo ID")
    public ResponseEntity<RespostaProjeto> buscarPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoProjeto.buscarPorId(id, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome e descrição do projeto — requer papel ADMIN no projeto")
    public ResponseEntity<RespostaProjeto> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody RequisicaoProjeto requisicao,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoProjeto.atualizar(id, requisicao, userDetails.getUsername()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove o projeto — requer papel ADMIN no projeto")
    public ResponseEntity<Void> deletar(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        servicoProjeto.deletar(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Adiciona um membro ao projeto — requer papel ADMIN no projeto")
    public ResponseEntity<RespostaMembro> adicionarMembro(
            @PathVariable Long id,
            @Valid @RequestBody RequisicaoMembro requisicao,
            @AuthenticationPrincipal UserDetails userDetails) {
        RespostaMembro resposta = servicoProjeto.adicionarMembro(id, requisicao, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "Remove um membro do projeto — requer papel ADMIN no projeto")
    public ResponseEntity<Void> removerMembro(
            @PathVariable Long id,
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        servicoProjeto.removerMembro(id, userId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "Lista os membros do projeto")
    public ResponseEntity<List<RespostaMembro>> listarMembros(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoProjeto.listarMembros(id, userDetails.getUsername()));
    }
}
