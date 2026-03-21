package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoAtualizacaoTarefa;
import com.taskmanager.api.dto.request.RequisicaoTarefa;
import com.taskmanager.api.dto.response.RespostaPaginada;
import com.taskmanager.api.dto.response.RespostaResumoTarefa;
import com.taskmanager.api.dto.response.RespostaTarefa;
import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.Projeto;
import com.taskmanager.api.entity.StatusTarefa;
import com.taskmanager.api.entity.Tarefa;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.EspecificacaoTarefa;
import com.taskmanager.api.repository.RepositorioMembroProjeto;
import com.taskmanager.api.repository.RepositorioProjeto;
import com.taskmanager.api.repository.RepositorioTarefa;
import com.taskmanager.api.repository.RepositorioUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ServicoTarefa {

    private static final int LIMITE_WIP = 5;

    private final RepositorioTarefa repositorioTarefa;
    private final RepositorioProjeto repositorioProjeto;
    private final RepositorioMembroProjeto repositorioMembroProjeto;
    private final RepositorioUsuario repositorioUsuario;

    public ServicoTarefa(RepositorioTarefa repositorioTarefa,
                          RepositorioProjeto repositorioProjeto,
                          RepositorioMembroProjeto repositorioMembroProjeto,
                          RepositorioUsuario repositorioUsuario) {
        this.repositorioTarefa = repositorioTarefa;
        this.repositorioProjeto = repositorioProjeto;
        this.repositorioMembroProjeto = repositorioMembroProjeto;
        this.repositorioUsuario = repositorioUsuario;
    }

    @CacheEvict(value = "resumo-projeto", key = "#idProjeto")
    public RespostaTarefa criar(Long idProjeto, RequisicaoTarefa requisicao, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        Projeto projeto = buscarProjetoOuLancar(idProjeto);

        exigirMembro(idProjeto, usuarioAtual.getId());

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(requisicao.getTitulo());
        tarefa.setDescricao(requisicao.getDescricao());
        tarefa.setPrioridade(requisicao.getPrioridade());
        tarefa.setProjeto(projeto);
        tarefa.setStatus(StatusTarefa.TODO);
        tarefa.setPrazo(requisicao.getPrazo());

        if (requisicao.getIdResponsavel() != null) {
            Usuario novoResponsavel = buscarUsuarioPorIdOuLancar(requisicao.getIdResponsavel());
            exigirMembroOuLancarNegocio(idProjeto, novoResponsavel.getId(),
                    "Responsável não é membro do projeto");
            tarefa.setResponsavel(novoResponsavel);
        }

        Tarefa salva = repositorioTarefa.save(tarefa);
        return paraResposta(salva);
    }

    @Transactional(readOnly = true)
    public RespostaTarefa buscarPorId(Long idProjeto, Long idTarefa, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        exigirMembro(idProjeto, usuarioAtual.getId());

        Tarefa tarefa = buscarTarefaNoProjetoOuLancar(idProjeto, idTarefa);
        return paraResposta(tarefa);
    }

    @Transactional(readOnly = true)
    public RespostaPaginada<RespostaTarefa> listar(Long idProjeto,
                                                    String emailUsuarioAtual,
                                                    StatusTarefa status,
                                                    PrioridadeTarefa prioridade,
                                                    Long idResponsavel,
                                                    LocalDateTime criadoAPartirDe,
                                                    LocalDateTime criadoAte,
                                                    LocalDateTime prazoAPartirDe,
                                                    LocalDateTime prazoAte,
                                                    Pageable pageable) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        exigirMembro(idProjeto, usuarioAtual.getId());
        buscarProjetoOuLancar(idProjeto);

        Specification<Tarefa> spec = Specification
                .where(EspecificacaoTarefa.doProjeto(idProjeto))
                .and(EspecificacaoTarefa.comStatus(status))
                .and(EspecificacaoTarefa.comPrioridade(prioridade))
                .and(EspecificacaoTarefa.comResponsavel(idResponsavel))
                .and(EspecificacaoTarefa.criadoAPartirDe(criadoAPartirDe))
                .and(EspecificacaoTarefa.criadoAte(criadoAte))
                .and(EspecificacaoTarefa.prazoAPartirDe(prazoAPartirDe))
                .and(EspecificacaoTarefa.prazoAte(prazoAte));

        Page<Tarefa> pagina = repositorioTarefa.findAll(spec, pageable);
        return paraRespostaPaginada(pagina);
    }

    @Transactional(readOnly = true)
    public List<RespostaTarefa> buscar(Long idProjeto, String termo, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        exigirMembro(idProjeto, usuarioAtual.getId());
        buscarProjetoOuLancar(idProjeto);

        return repositorioTarefa.buscarPorTexto(idProjeto, termo)
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @CacheEvict(value = "resumo-projeto", key = "#idProjeto")
    public RespostaTarefa atualizar(Long idProjeto, Long idTarefa, RequisicaoAtualizacaoTarefa requisicao, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        exigirMembro(idProjeto, usuarioAtual.getId());

        Tarefa tarefa = buscarTarefaNoProjetoOuLancar(idProjeto, idTarefa);

        StatusTarefa novoStatus = requisicao.getStatus();
        PrioridadeTarefa novaPrioridade = requisicao.getPrioridade() != null ? requisicao.getPrioridade() : tarefa.getPrioridade();

        if (novoStatus != null
                && tarefa.getStatus() == StatusTarefa.DONE
                && novoStatus == StatusTarefa.TODO) {
            throw new IllegalStateException("Tarefa DONE não pode voltar para TODO");
        }

        PrioridadeTarefa prioridadeEfetiva = requisicao.getPrioridade() != null ? requisicao.getPrioridade() : tarefa.getPrioridade();
        if (novoStatus == StatusTarefa.DONE && prioridadeEfetiva == PrioridadeTarefa.CRITICAL) {
            if (usuarioAtual.getPerfil() != Perfil.ADMIN) {
                throw new AccessDeniedException("Apenas ADMIN pode concluir tarefas CRITICAL");
            }
        }

        Usuario novoResponsavel = tarefa.getResponsavel();
        if (requisicao.isIdResponsavelFornecido()) {
            if (requisicao.getIdResponsavel() == null) {
                novoResponsavel = null;
            } else {
                Usuario candidato = buscarUsuarioPorIdOuLancar(requisicao.getIdResponsavel());
                exigirMembroOuLancarNegocio(idProjeto, candidato.getId(),
                        "Responsável não é membro do projeto");
                novoResponsavel = candidato;
            }
        }

        StatusTarefa statusEfetivo = novoStatus != null ? novoStatus : tarefa.getStatus();
        if (statusEfetivo == StatusTarefa.IN_PROGRESS && novoResponsavel != null) {
            long emAndamento = repositorioTarefa.contarPorIdResponsavelEStatus(novoResponsavel.getId(), StatusTarefa.IN_PROGRESS);
            boolean responsavelMudou = !novoResponsavel.equals(tarefa.getResponsavel());
            boolean statusMudouParaEmAndamento = tarefa.getStatus() != StatusTarefa.IN_PROGRESS
                    && statusEfetivo == StatusTarefa.IN_PROGRESS;
            if ((responsavelMudou || statusMudouParaEmAndamento) && emAndamento >= LIMITE_WIP) {
                throw new IllegalStateException(
                        "Responsável atingiu o limite de 5 tarefas IN_PROGRESS");
            }
        }

        if (requisicao.getTitulo() != null) {
            tarefa.setTitulo(requisicao.getTitulo());
        }
        if (requisicao.getDescricao() != null) {
            tarefa.setDescricao(requisicao.getDescricao());
        }
        if (novoStatus != null) {
            tarefa.setStatus(novoStatus);
        }
        if (requisicao.getPrioridade() != null) {
            tarefa.setPrioridade(requisicao.getPrioridade());
        }
        if (requisicao.getPrazo() != null) {
            tarefa.setPrazo(requisicao.getPrazo());
        }
        tarefa.setResponsavel(novoResponsavel);

        Tarefa salva = repositorioTarefa.save(tarefa);
        return paraResposta(salva);
    }

    @CacheEvict(value = "resumo-projeto", key = "#idProjeto")
    public void deletar(Long idProjeto, Long idTarefa, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        exigirMembro(idProjeto, usuarioAtual.getId());
        exigirAdmin(usuarioAtual);

        Tarefa tarefa = buscarTarefaNoProjetoOuLancar(idProjeto, idTarefa);
        repositorioTarefa.delete(tarefa);
    }

    @Cacheable(value = "resumo-projeto", key = "#idProjeto")
    @Transactional(readOnly = true)
    public RespostaResumoTarefa resumo(Long idProjeto, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);
        exigirMembro(idProjeto, usuarioAtual.getId());
        buscarProjetoOuLancar(idProjeto);

        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (StatusTarefa s : StatusTarefa.values()) porStatus.put(s.name(), 0L);
        for (Object[] row : repositorioTarefa.contarPorIdProjetoAgrupadoPorStatus(idProjeto)) {
            porStatus.put(((StatusTarefa) row[0]).name(), (Long) row[1]);
        }

        Map<String, Long> porPrioridade = new LinkedHashMap<>();
        for (PrioridadeTarefa p : PrioridadeTarefa.values()) porPrioridade.put(p.name(), 0L);
        for (Object[] row : repositorioTarefa.contarPorIdProjetoAgrupadoPorPrioridade(idProjeto)) {
            porPrioridade.put(((PrioridadeTarefa) row[0]).name(), (Long) row[1]);
        }

        RespostaResumoTarefa resposta = new RespostaResumoTarefa();
        resposta.setPorStatus(porStatus);
        resposta.setPorPrioridade(porPrioridade);
        return resposta;
    }

    private void exigirMembro(Long idProjeto, Long idUsuario) {
        if (!repositorioMembroProjeto.existePorIdProjetoEIdUsuario(idProjeto, idUsuario)) {
            throw new AccessDeniedException("Usuário não é membro deste projeto");
        }
    }

    private void exigirMembroOuLancarNegocio(Long idProjeto, Long idUsuario, String mensagem) {
        if (!repositorioMembroProjeto.existePorIdProjetoEIdUsuario(idProjeto, idUsuario)) {
            throw new IllegalStateException(mensagem);
        }
    }

    private void exigirAdmin(Usuario usuario) {
        if (usuario.getPerfil() != Perfil.ADMIN) {
            throw new AccessDeniedException("Apenas ADMIN pode executar esta operação");
        }
    }

    private Projeto buscarProjetoOuLancar(Long idProjeto) {
        return repositorioProjeto.findById(idProjeto)
                .orElseThrow(() -> new EntityNotFoundException("Projeto não encontrado: id=" + idProjeto));
    }

    private Usuario buscarUsuarioPorEmailOuLancar(String email) {
        return repositorioUsuario.buscarPorEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: email=" + email));
    }

    private Usuario buscarUsuarioPorIdOuLancar(Long idUsuario) {
        return repositorioUsuario.findById(idUsuario)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: id=" + idUsuario));
    }

    private Tarefa buscarTarefaNoProjetoOuLancar(Long idProjeto, Long idTarefa) {
        Tarefa tarefa = repositorioTarefa.findById(idTarefa)
                .orElseThrow(() -> new EntityNotFoundException("Tarefa não encontrada: id=" + idTarefa));
        if (!tarefa.getProjeto().getId().equals(idProjeto)) {
            throw new EntityNotFoundException("Tarefa não encontrada no projeto: taskId=" + idTarefa);
        }
        return tarefa;
    }

    private RespostaPaginada<RespostaTarefa> paraRespostaPaginada(Page<Tarefa> pagina) {
        List<RespostaTarefa> conteudo = pagina.getContent().stream().map(this::paraResposta).toList();
        return new RespostaPaginada<>(
                conteudo,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages()
        );
    }

    private RespostaTarefa paraResposta(Tarefa tarefa) {
        RespostaTarefa r = new RespostaTarefa();
        r.setId(tarefa.getId());
        r.setTitulo(tarefa.getTitulo());
        r.setDescricao(tarefa.getDescricao());
        r.setStatus(tarefa.getStatus());
        r.setPrioridade(tarefa.getPrioridade());
        r.setIdProjeto(tarefa.getProjeto().getId());
        r.setCriadoEm(tarefa.getCriadoEm());
        r.setAtualizadoEm(tarefa.getAtualizadoEm());
        r.setPrazo(tarefa.getPrazo());
        if (tarefa.getResponsavel() != null) {
            r.setIdResponsavel(tarefa.getResponsavel().getId());
            r.setNomeResponsavel(tarefa.getResponsavel().getNome());
        }
        return r;
    }
}
