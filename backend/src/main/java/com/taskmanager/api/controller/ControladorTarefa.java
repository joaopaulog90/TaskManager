package com.taskmanager.api.controller;

import com.taskmanager.api.dto.request.RequisicaoAtualizacaoTarefa;
import com.taskmanager.api.dto.request.RequisicaoTarefa;
import com.taskmanager.api.dto.response.RespostaHistoricoTarefa;
import com.taskmanager.api.dto.response.RespostaPaginada;
import com.taskmanager.api.dto.response.RespostaResumoTarefa;
import com.taskmanager.api.dto.response.RespostaTarefa;
import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.StatusTarefa;
import com.taskmanager.api.service.ServicoTarefa;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
@Tag(name = "Tarefas", description = "CRUD de tarefas com filtros e relatório")
public class ControladorTarefa {

    private final ServicoTarefa servicoTarefa;

    public ControladorTarefa(ServicoTarefa servicoTarefa) {
        this.servicoTarefa = servicoTarefa;
    }

    @PostMapping
    @Operation(summary = "Cria uma tarefa no projeto")
    public ResponseEntity<RespostaTarefa> criar(
            @PathVariable Long projectId,
            @Valid @RequestBody RequisicaoTarefa requisicao,
            @AuthenticationPrincipal UserDetails userDetails) {
        RespostaTarefa resposta = servicoTarefa.criar(projectId, requisicao, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @GetMapping
    @Operation(summary = "Lista tarefas do projeto com filtros opcionais por status, prioridade, responsável e datas")
    public ResponseEntity<RespostaPaginada<RespostaTarefa>> listar(
            @PathVariable Long projectId,
            @RequestParam(required = false) StatusTarefa status,
            @RequestParam(value = "priority", required = false) PrioridadeTarefa prioridade,
            @RequestParam(value = "assigneeId", required = false) Long idResponsavel,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAtTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime deadlineTo,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        RespostaPaginada<RespostaTarefa> resposta = servicoTarefa.listar(
                projectId, userDetails.getUsername(), status, prioridade, idResponsavel,
                createdAtFrom, createdAtTo, deadlineFrom, deadlineTo, pageable);
        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca tarefas por texto no título ou descrição")
    public ResponseEntity<List<RespostaTarefa>> buscar(
            @PathVariable Long projectId,
            @RequestParam("q") String termo,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoTarefa.buscar(projectId, termo, userDetails.getUsername()));
    }

    @GetMapping("/summary")
    @Operation(summary = "Retorna contagem de tarefas agrupadas por status e por prioridade")
    public ResponseEntity<RespostaResumoTarefa> resumo(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoTarefa.resumo(projectId, userDetails.getUsername()));
    }

    @GetMapping("/{taskId}")
    @Operation(summary = "Retorna uma tarefa pelo ID")
    public ResponseEntity<RespostaTarefa> buscarPorId(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoTarefa.buscarPorId(projectId, taskId, userDetails.getUsername()));
    }

    @PutMapping("/{taskId}")
    @Operation(summary = "Atualiza campos da tarefa — aplica regras de transição de status e restrições de prioridade CRITICAL")
    public ResponseEntity<RespostaTarefa> atualizar(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @RequestBody RequisicaoAtualizacaoTarefa requisicao,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoTarefa.atualizar(projectId, taskId, requisicao, userDetails.getUsername()));
    }

    @GetMapping("/{taskId}/history")
    @Operation(summary = "Retorna o histórico de alterações de uma tarefa")
    public ResponseEntity<List<RespostaHistoricoTarefa>> historico(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(servicoTarefa.buscarHistorico(projectId, taskId, userDetails.getUsername()));
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Remove uma tarefa do projeto")
    public ResponseEntity<Void> deletar(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        servicoTarefa.deletar(projectId, taskId, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
