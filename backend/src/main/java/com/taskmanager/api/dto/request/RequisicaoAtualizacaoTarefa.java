package com.taskmanager.api.dto.request;

import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.StatusTarefa;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class RequisicaoAtualizacaoTarefa {

    @Size(min = 1, max = 255, message = "Título deve ter entre 1 e 255 caracteres")
    private String titulo;

    @Size(max = 5000, message = "Descrição deve ter no máximo 5000 caracteres")
    private String descricao;

    private StatusTarefa status;

    private PrioridadeTarefa prioridade;

    private Long idResponsavel;

    private boolean idResponsavelFornecido = false;

    private LocalDateTime prazo;

    public RequisicaoAtualizacaoTarefa() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusTarefa getStatus() {
        return status;
    }

    public void setStatus(StatusTarefa status) {
        this.status = status;
    }

    public PrioridadeTarefa getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeTarefa prioridade) {
        this.prioridade = prioridade;
    }

    public Long getIdResponsavel() {
        return idResponsavel;
    }

    public void setIdResponsavel(Long idResponsavel) {
        this.idResponsavel = idResponsavel;
        this.idResponsavelFornecido = true;
    }

    public boolean isIdResponsavelFornecido() {
        return idResponsavelFornecido;
    }

    public LocalDateTime getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDateTime prazo) {
        this.prazo = prazo;
    }
}
