package com.taskmanager.api.dto.request;

import jakarta.validation.constraints.NotNull;

public class RequisicaoMembro {

    @NotNull(message = "idUsuario é obrigatório")
    private Long idUsuario;

    public RequisicaoMembro() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
}
