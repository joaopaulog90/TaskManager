package com.taskmanager.api.dto.response;

import java.time.LocalDateTime;

public class RespostaProjeto {

    private Long id;
    private String nome;
    private String descricao;
    private Long idProprietario;
    private String nomeProprietario;
    private LocalDateTime criadoEm;
    private int quantidadeMembros;

    public RespostaProjeto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Long getIdProprietario() {
        return idProprietario;
    }

    public void setIdProprietario(Long idProprietario) {
        this.idProprietario = idProprietario;
    }

    public String getNomeProprietario() {
        return nomeProprietario;
    }

    public void setNomeProprietario(String nomeProprietario) {
        this.nomeProprietario = nomeProprietario;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public int getQuantidadeMembros() {
        return quantidadeMembros;
    }

    public void setQuantidadeMembros(int quantidadeMembros) {
        this.quantidadeMembros = quantidadeMembros;
    }
}
