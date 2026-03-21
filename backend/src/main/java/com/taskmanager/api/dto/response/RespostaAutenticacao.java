package com.taskmanager.api.dto.response;

import com.taskmanager.api.entity.Perfil;

public class RespostaAutenticacao {

    private String token;
    private Long idUsuario;
    private String email;
    private Perfil perfil;

    public RespostaAutenticacao(String token, Long idUsuario, String email, Perfil perfil) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.email = email;
        this.perfil = perfil;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }
}
