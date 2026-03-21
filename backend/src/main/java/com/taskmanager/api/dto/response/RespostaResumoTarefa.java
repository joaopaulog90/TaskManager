package com.taskmanager.api.dto.response;

import java.util.Map;

public class RespostaResumoTarefa {

    private Map<String, Long> porStatus;

    private Map<String, Long> porPrioridade;

    public RespostaResumoTarefa() {
    }

    public Map<String, Long> getPorStatus() {
        return porStatus;
    }

    public void setPorStatus(Map<String, Long> porStatus) {
        this.porStatus = porStatus;
    }

    public Map<String, Long> getPorPrioridade() {
        return porPrioridade;
    }

    public void setPorPrioridade(Map<String, Long> porPrioridade) {
        this.porPrioridade = porPrioridade;
    }
}
