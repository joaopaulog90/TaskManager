package com.taskmanager.api.dto.response;

import com.taskmanager.api.entity.Perfil;

import java.time.LocalDateTime;

public class RespostaMembro {

    private Long idUsuario;
    private String nomeUsuario;
    private String emailUsuario;
    private Perfil perfil;
    private LocalDateTime entradoEm;

    public RespostaMembro() {
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public LocalDateTime getEntradoEm() {
        return entradoEm;
    }

    public void setEntradoEm(LocalDateTime entradoEm) {
        this.entradoEm = entradoEm;
    }
}
