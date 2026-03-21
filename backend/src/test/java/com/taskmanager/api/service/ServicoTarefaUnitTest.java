package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoAtualizacaoTarefa;
import com.taskmanager.api.dto.request.RequisicaoTarefa;
import com.taskmanager.api.dto.response.RespostaTarefa;
import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.Projeto;
import com.taskmanager.api.entity.StatusTarefa;
import com.taskmanager.api.entity.Tarefa;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioHistoricoTarefa;
import com.taskmanager.api.repository.RepositorioMembroProjeto;
import com.taskmanager.api.repository.RepositorioProjeto;
import com.taskmanager.api.repository.RepositorioTarefa;
import com.taskmanager.api.repository.RepositorioUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoTarefaUnitTest {

    @Mock
    private RepositorioTarefa repositorioTarefa;

    @Mock
    private RepositorioProjeto repositorioProjeto;

    @Mock
    private RepositorioMembroProjeto repositorioMembroProjeto;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioHistoricoTarefa repositorioHistoricoTarefa;

    @InjectMocks
    private ServicoTarefa servicoTarefa;

    private Usuario usuarioFake(Long id, String email, Perfil perfil) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setEmail(email);
        u.setNome("User " + id);
        u.setSenha("hashed");
        u.setPerfil(perfil);
        return u;
    }

    private Projeto projetoFake(Long id, Usuario proprietario) {
        Projeto p = new Projeto();
        p.setId(id);
        p.setNome("Projeto " + id);
        p.setProprietario(proprietario);
        return p;
    }

    private Tarefa tarefaFake(Long id, Projeto projeto, StatusTarefa status, PrioridadeTarefa prioridade) {
        Tarefa t = new Tarefa();
        t.setId(id);
        t.setTitulo("Tarefa " + id);
        t.setStatus(status);
        t.setPrioridade(prioridade);
        t.setProjeto(projeto);
        t.setCriadoEm(LocalDateTime.now());
        t.setAtualizadoEm(LocalDateTime.now());
        return t;
    }

    @Test
    void criar_deveDefinirStatusTodo_sempre() {
        Usuario criador = usuarioFake(1L, "user@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, criador);

        when(repositorioUsuario.buscarPorEmail("user@example.com")).thenReturn(Optional.of(criador));
        when(repositorioProjeto.findById(10L)).thenReturn(Optional.of(projeto));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);

        Tarefa tarefaSalva = tarefaFake(100L, projeto, StatusTarefa.TODO, PrioridadeTarefa.LOW);
        when(repositorioTarefa.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        RequisicaoTarefa req = new RequisicaoTarefa();
        req.setTitulo("Nova Tarefa");
        req.setPrioridade(PrioridadeTarefa.LOW);

        servicoTarefa.criar(10L, req, "user@example.com");

        ArgumentCaptor<Tarefa> captor = ArgumentCaptor.forClass(Tarefa.class);
        verify(repositorioTarefa).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StatusTarefa.TODO);
    }

    @Test
    void atualizar_deveLancarException_quandoDoneVoltaParaTodo() {
        Usuario usuario = usuarioFake(1L, "user@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, usuario);
        Tarefa tarefa = tarefaFake(100L, projeto, StatusTarefa.DONE, PrioridadeTarefa.LOW);

        when(repositorioUsuario.buscarPorEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);
        when(repositorioTarefa.findById(100L)).thenReturn(Optional.of(tarefa));

        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.TODO);

        assertThatThrownBy(() -> servicoTarefa.atualizar(10L, 100L, req, "user@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("TODO");
    }

    @Test
    void atualizar_deveLancarAccessDenied_quandoMembreTentaFecharCritical() {
        Usuario usuario = usuarioFake(1L, "membro@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, usuario);
        Tarefa tarefa = tarefaFake(100L, projeto, StatusTarefa.IN_PROGRESS, PrioridadeTarefa.CRITICAL);

        when(repositorioUsuario.buscarPorEmail("membro@example.com")).thenReturn(Optional.of(usuario));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);
        when(repositorioTarefa.findById(100L)).thenReturn(Optional.of(tarefa));

        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.DONE);

        assertThatThrownBy(() -> servicoTarefa.atualizar(10L, 100L, req, "membro@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void atualizar_devePermitirAdmin_fecharCritical() {
        Usuario admin = usuarioFake(1L, "admin@example.com", Perfil.ADMIN);
        Projeto projeto = projetoFake(10L, admin);
        Tarefa tarefa = tarefaFake(100L, projeto, StatusTarefa.IN_PROGRESS, PrioridadeTarefa.CRITICAL);

        when(repositorioUsuario.buscarPorEmail("admin@example.com")).thenReturn(Optional.of(admin));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);
        when(repositorioTarefa.findById(100L)).thenReturn(Optional.of(tarefa));

        Tarefa tarefaSalva = tarefaFake(100L, projeto, StatusTarefa.DONE, PrioridadeTarefa.CRITICAL);
        when(repositorioTarefa.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.DONE);

        servicoTarefa.atualizar(10L, 100L, req, "admin@example.com");

        verify(repositorioTarefa).save(any(Tarefa.class));
    }

    @Test
    void atualizar_deveLancarException_quandoWipLimitAtingido() {
        Usuario usuario = usuarioFake(1L, "user@example.com", Perfil.MEMBER);
        Usuario responsavel = usuarioFake(2L, "assignee@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, usuario);

        Tarefa tarefa = tarefaFake(100L, projeto, StatusTarefa.TODO, PrioridadeTarefa.MEDIUM);

        when(repositorioUsuario.buscarPorEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);
        when(repositorioTarefa.findById(100L)).thenReturn(Optional.of(tarefa));
        when(repositorioUsuario.findById(2L)).thenReturn(Optional.of(responsavel));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 2L)).thenReturn(true);
        when(repositorioTarefa.contarPorIdResponsavelEStatus(2L, StatusTarefa.IN_PROGRESS)).thenReturn(5L);

        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.IN_PROGRESS);
        req.setIdResponsavel(2L);

        assertThatThrownBy(() -> servicoTarefa.atualizar(10L, 100L, req, "user@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("5");
    }

    @Test
    void atualizar_devePermitir_quandoWipAbaixoDoLimite() {
        Usuario usuario = usuarioFake(1L, "user@example.com", Perfil.MEMBER);
        Usuario responsavel = usuarioFake(2L, "assignee@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, usuario);

        Tarefa tarefa = tarefaFake(100L, projeto, StatusTarefa.TODO, PrioridadeTarefa.MEDIUM);

        when(repositorioUsuario.buscarPorEmail("user@example.com")).thenReturn(Optional.of(usuario));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);
        when(repositorioTarefa.findById(100L)).thenReturn(Optional.of(tarefa));
        when(repositorioUsuario.findById(2L)).thenReturn(Optional.of(responsavel));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 2L)).thenReturn(true);
        when(repositorioTarefa.contarPorIdResponsavelEStatus(2L, StatusTarefa.IN_PROGRESS)).thenReturn(4L);

        Tarefa tarefaSalva = tarefaFake(100L, projeto, StatusTarefa.IN_PROGRESS, PrioridadeTarefa.MEDIUM);
        when(repositorioTarefa.save(any(Tarefa.class))).thenReturn(tarefaSalva);

        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.IN_PROGRESS);
        req.setIdResponsavel(2L);

        servicoTarefa.atualizar(10L, 100L, req, "user@example.com");

        verify(repositorioTarefa).save(any(Tarefa.class));
    }
}
