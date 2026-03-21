package com.taskmanager.api.dto.request;

import com.taskmanager.api.entity.PapelProjeto;
import jakarta.validation.constraints.NotNull;

public class RequisicaoMembro {

    @NotNull(message = "idUsuario é obrigatório")
    private Long idUsuario;

    @NotNull(message = "Papel é obrigatório")
    private PapelProjeto papel;

    public RequisicaoMembro() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public PapelProjeto getPapel() {
        return papel;
    }

    public void setPapel(PapelProjeto papel) {
        this.papel = papel;
    }
}
