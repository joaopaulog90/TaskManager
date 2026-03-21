package com.taskmanager.api.dto.request;

import com.taskmanager.api.entity.Perfil;
import jakarta.validation.constraints.NotNull;

public class RequisicaoAlteracaoPerfil {

    @NotNull(message = "Perfil é obrigatório")
    private Perfil perfil;

    public RequisicaoAlteracaoPerfil() {
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
