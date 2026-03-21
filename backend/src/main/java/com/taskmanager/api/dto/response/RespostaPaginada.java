package com.taskmanager.api.dto.response;

import java.util.List;

public class RespostaPaginada<T> {

    private List<T> conteudo;

    private int pagina;

    private int tamanho;

    private long totalElementos;

    private int totalPaginas;

    public RespostaPaginada() {
    }

    public RespostaPaginada(List<T> conteudo, int pagina, int tamanho, long totalElementos, int totalPaginas) {
        this.conteudo = conteudo;
        this.pagina = pagina;
        this.tamanho = tamanho;
        this.totalElementos = totalElementos;
        this.totalPaginas = totalPaginas;
    }

    public List<T> getConteudo() {
        return conteudo;
    }

    public void setConteudo(List<T> conteudo) {
        this.conteudo = conteudo;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTamanho() {
        return tamanho;
    }

    public void setTamanho(int tamanho) {
        this.tamanho = tamanho;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }
}
