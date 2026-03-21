package com.taskmanager.api.dto.response;

public class RespostaAutenticacao {

    private String token;

    private Long idUsuario;

    private String email;

    public RespostaAutenticacao(String token, Long idUsuario, String email) {
        this.token = token;
        this.idUsuario = idUsuario;
        this.email = email;
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
}
