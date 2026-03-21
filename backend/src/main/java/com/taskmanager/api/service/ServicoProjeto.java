package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoMembro;
import com.taskmanager.api.dto.request.RequisicaoProjeto;
import com.taskmanager.api.dto.response.RespostaMembro;
import com.taskmanager.api.dto.response.RespostaProjeto;
import com.taskmanager.api.entity.MembroProjeto;
import com.taskmanager.api.entity.PapelProjeto;
import com.taskmanager.api.entity.Projeto;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioMembroProjeto;
import com.taskmanager.api.repository.RepositorioProjeto;
import com.taskmanager.api.repository.RepositorioTarefa;
import com.taskmanager.api.repository.RepositorioUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicoProjeto {

    private final RepositorioProjeto repositorioProjeto;
    private final RepositorioMembroProjeto repositorioMembroProjeto;
    private final RepositorioUsuario repositorioUsuario;
    private final RepositorioTarefa repositorioTarefa;

    public ServicoProjeto(RepositorioProjeto repositorioProjeto,
                           RepositorioMembroProjeto repositorioMembroProjeto,
                           RepositorioUsuario repositorioUsuario,
                           RepositorioTarefa repositorioTarefa) {
        this.repositorioProjeto = repositorioProjeto;
        this.repositorioMembroProjeto = repositorioMembroProjeto;
        this.repositorioUsuario = repositorioUsuario;
        this.repositorioTarefa = repositorioTarefa;
    }

    public RespostaProjeto criar(RequisicaoProjeto requisicao, String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        Projeto projeto = new Projeto();
        projeto.setNome(requisicao.getNome());
        projeto.setDescricao(requisicao.getDescricao());
        projeto.setProprietario(usuarioAtual);

        Projeto salvo = repositorioProjeto.save(projeto);

        MembroProjeto membroProprietario = new MembroProjeto();
        membroProprietario.setProjeto(salvo);
        membroProprietario.setUsuario(usuarioAtual);
        membroProprietario.setPapel(PapelProjeto.ADMIN);
        repositorioMembroProjeto.save(membroProprietario);

        return paraRespostaProjeto(salvo, 1);
    }

    @Transactional(readOnly = true)
    public RespostaProjeto buscarPorId(Long idProjeto, String emailUsuarioAtual) {
        Projeto projeto = buscarProjetoOuLancar(idProjeto);
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        exigirMembro(projeto.getId(), usuarioAtual.getId());

        int totalMembros = repositorioMembroProjeto.buscarPorIdProjeto(idProjeto).size();
        return paraRespostaProjeto(projeto, totalMembros);
    }

    @Transactional(readOnly = true)
    public List<RespostaProjeto> listarDoUsuario(String emailUsuarioAtual) {
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        List<MembroProjeto> vinculos = repositorioMembroProjeto.buscarPorIdUsuario(usuarioAtual.getId());

        if (vinculos.isEmpty()) {
            return List.of();
        }

        List<Long> idsProjeto = vinculos.stream()
                .map(pm -> pm.getProjeto().getId())
                .toList();

        Map<Long, Long> totalMembrosPorIdProjeto = repositorioMembroProjeto.contarPorIdsProjeto(idsProjeto).stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (Long) row[1]
                ));

        return vinculos.stream()
                .map(pm -> {
                    Projeto projeto = pm.getProjeto();
                    long totalMembros = totalMembrosPorIdProjeto.getOrDefault(projeto.getId(), 0L);
                    return paraRespostaProjeto(projeto, (int) totalMembros);
                })
                .toList();
    }

    public RespostaProjeto atualizar(Long idProjeto, RequisicaoProjeto requisicao, String emailUsuarioAtual) {
        Projeto projeto = buscarProjetoOuLancar(idProjeto);
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        exigirAdmin(projeto.getId(), usuarioAtual.getId());

        projeto.setNome(requisicao.getNome());
        projeto.setDescricao(requisicao.getDescricao());

        Projeto salvo = repositorioProjeto.save(projeto);
        int totalMembros = repositorioMembroProjeto.buscarPorIdProjeto(idProjeto).size();
        return paraRespostaProjeto(salvo, totalMembros);
    }

    public void deletar(Long idProjeto, String emailUsuarioAtual) {
        Projeto projeto = buscarProjetoOuLancar(idProjeto);
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        exigirAdmin(projeto.getId(), usuarioAtual.getId());

        repositorioTarefa.deletarPorIdProjeto(idProjeto);
        repositorioMembroProjeto.buscarPorIdProjeto(idProjeto)
                .forEach(repositorioMembroProjeto::delete);
        repositorioProjeto.delete(projeto);
    }

    public RespostaMembro adicionarMembro(Long idProjeto, RequisicaoMembro requisicao, String emailUsuarioAtual) {
        Projeto projeto = buscarProjetoOuLancar(idProjeto);
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        exigirAdmin(projeto.getId(), usuarioAtual.getId());

        Usuario novoMembro = repositorioUsuario.findById(requisicao.getIdUsuario())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não encontrado: id=" + requisicao.getIdUsuario()));

        if (repositorioMembroProjeto.existePorIdProjetoEIdUsuario(idProjeto, novoMembro.getId())) {
            throw new IllegalStateException("Usuário já é membro deste projeto");
        }

        MembroProjeto membro = new MembroProjeto();
        membro.setProjeto(projeto);
        membro.setUsuario(novoMembro);
        membro.setPapel(requisicao.getPapel());
        MembroProjeto salvo = repositorioMembroProjeto.save(membro);

        return paraRespostaMembro(salvo);
    }

    public void removerMembro(Long idProjeto, Long idUsuario, String emailUsuarioAtual) {
        Projeto projeto = buscarProjetoOuLancar(idProjeto);
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        exigirAdmin(projeto.getId(), usuarioAtual.getId());

        if (projeto.getProprietario().getId().equals(idUsuario)) {
            throw new IllegalStateException("Não é possível remover o owner do projeto");
        }

        MembroProjeto vinculo = repositorioMembroProjeto
                .buscarPorIdProjetoEIdUsuario(idProjeto, idUsuario)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuário não é membro deste projeto: userId=" + idUsuario));

        repositorioMembroProjeto.delete(vinculo);
    }

    @Transactional(readOnly = true)
    public List<RespostaMembro> listarMembros(Long idProjeto, String emailUsuarioAtual) {
        Projeto projeto = buscarProjetoOuLancar(idProjeto);
        Usuario usuarioAtual = buscarUsuarioPorEmailOuLancar(emailUsuarioAtual);

        exigirMembro(projeto.getId(), usuarioAtual.getId());

        return repositorioMembroProjeto.buscarPorIdProjeto(idProjeto).stream()
                .map(this::paraRespostaMembro)
                .toList();
    }

    private void exigirMembro(Long idProjeto, Long idUsuario) {
        if (!repositorioMembroProjeto.existePorIdProjetoEIdUsuario(idProjeto, idUsuario)) {
            throw new AccessDeniedException("Usuário não é membro deste projeto");
        }
    }

    private void exigirAdmin(Long idProjeto, Long idUsuario) {
        boolean ehAdmin = repositorioMembroProjeto
                .existePorIdProjetoIdUsuarioEPapel(idProjeto, idUsuario, PapelProjeto.ADMIN);
        if (!ehAdmin) {
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

    private RespostaProjeto paraRespostaProjeto(Projeto projeto, int totalMembros) {
        RespostaProjeto resposta = new RespostaProjeto();
        resposta.setId(projeto.getId());
        resposta.setNome(projeto.getNome());
        resposta.setDescricao(projeto.getDescricao());
        resposta.setIdProprietario(projeto.getProprietario().getId());
        resposta.setNomeProprietario(projeto.getProprietario().getNome());
        resposta.setCriadoEm(projeto.getCriadoEm());
        resposta.setQuantidadeMembros(totalMembros);
        return resposta;
    }

    private RespostaMembro paraRespostaMembro(MembroProjeto membro) {
        RespostaMembro resposta = new RespostaMembro();
        resposta.setIdUsuario(membro.getUsuario().getId());
        resposta.setNomeUsuario(membro.getUsuario().getNome());
        resposta.setEmailUsuario(membro.getUsuario().getEmail());
        resposta.setPapel(membro.getPapel());
        resposta.setEntradoEm(membro.getEntradoEm());
        return resposta;
    }
}
