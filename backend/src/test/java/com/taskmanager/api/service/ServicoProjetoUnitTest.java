package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoMembro;
import com.taskmanager.api.dto.request.RequisicaoProjeto;
import com.taskmanager.api.dto.response.RespostaProjeto;
import com.taskmanager.api.entity.MembroProjeto;
import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.Projeto;
import com.taskmanager.api.entity.Usuario;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoProjetoUnitTest {

    @Mock
    private RepositorioProjeto repositorioProjeto;

    @Mock
    private RepositorioMembroProjeto repositorioMembroProjeto;

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private RepositorioTarefa repositorioTarefa;

    @InjectMocks
    private ServicoProjeto servicoProjeto;

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
        p.setDescricao("Desc");
        p.setProprietario(proprietario);
        return p;
    }

    @Test
    void criar_deveAdicionarProprietarioComoMembro() {
        Usuario proprietario = usuarioFake(1L, "owner@example.com", Perfil.ADMIN);
        when(repositorioUsuario.buscarPorEmail("owner@example.com")).thenReturn(Optional.of(proprietario));

        Projeto projetoSalvo = projetoFake(10L, proprietario);
        when(repositorioProjeto.save(any(Projeto.class))).thenReturn(projetoSalvo);

        MembroProjeto membroSalvo = new MembroProjeto();
        membroSalvo.setProjeto(projetoSalvo);
        membroSalvo.setUsuario(proprietario);
        when(repositorioMembroProjeto.save(any(MembroProjeto.class))).thenReturn(membroSalvo);

        RequisicaoProjeto req = new RequisicaoProjeto();
        req.setNome("Projeto Novo");
        req.setDescricao("Desc");

        servicoProjeto.criar(req, "owner@example.com");

        ArgumentCaptor<MembroProjeto> captor = ArgumentCaptor.forClass(MembroProjeto.class);
        verify(repositorioMembroProjeto).save(captor.capture());
        assertThat(captor.getValue().getUsuario()).isEqualTo(proprietario);
    }

    @Test
    void buscarPorId_deveLancarAccessDenied_quandoUsuarioNaoEhMembro() {
        Usuario proprietario = usuarioFake(1L, "owner@example.com", Perfil.ADMIN);
        Usuario estranho = usuarioFake(2L, "estranho@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, proprietario);

        when(repositorioProjeto.findById(10L)).thenReturn(Optional.of(projeto));
        when(repositorioUsuario.buscarPorEmail("estranho@example.com")).thenReturn(Optional.of(estranho));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 2L)).thenReturn(false);

        assertThatThrownBy(() -> servicoProjeto.buscarPorId(10L, "estranho@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void atualizar_deveLancarAccessDenied_quandoUsuarioEhMember() {
        Usuario proprietario = usuarioFake(1L, "owner@example.com", Perfil.ADMIN);
        Usuario membro = usuarioFake(2L, "membro@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, proprietario);

        when(repositorioProjeto.findById(10L)).thenReturn(Optional.of(projeto));
        when(repositorioUsuario.buscarPorEmail("membro@example.com")).thenReturn(Optional.of(membro));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 2L)).thenReturn(true);

        RequisicaoProjeto req = new RequisicaoProjeto();
        req.setNome("Novo Nome");

        assertThatThrownBy(() -> servicoProjeto.atualizar(10L, req, "membro@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deletar_deveLancarAccessDenied_quandoUsuarioEhMember() {
        Usuario proprietario = usuarioFake(1L, "owner@example.com", Perfil.ADMIN);
        Usuario membro = usuarioFake(2L, "membro@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, proprietario);

        when(repositorioProjeto.findById(10L)).thenReturn(Optional.of(projeto));
        when(repositorioUsuario.buscarPorEmail("membro@example.com")).thenReturn(Optional.of(membro));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> servicoProjeto.deletar(10L, "membro@example.com"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adicionarMembro_deveLancarException_quandoMembroJaExiste() {
        Usuario proprietario = usuarioFake(1L, "owner@example.com", Perfil.ADMIN);
        Usuario candidato = usuarioFake(2L, "candidato@example.com", Perfil.MEMBER);
        Projeto projeto = projetoFake(10L, proprietario);

        when(repositorioProjeto.findById(10L)).thenReturn(Optional.of(projeto));
        when(repositorioUsuario.buscarPorEmail("owner@example.com")).thenReturn(Optional.of(proprietario));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);
        when(repositorioUsuario.findById(2L)).thenReturn(Optional.of(candidato));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 2L)).thenReturn(true);

        RequisicaoMembro req = new RequisicaoMembro();
        req.setIdUsuario(2L);

        assertThatThrownBy(() -> servicoProjeto.adicionarMembro(10L, req, "owner@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("membro");
    }

    @Test
    void removerMembro_deveLancarException_quandoTentaRemoverProprietario() {
        Usuario proprietario = usuarioFake(1L, "owner@example.com", Perfil.ADMIN);
        Projeto projeto = projetoFake(10L, proprietario);

        when(repositorioProjeto.findById(10L)).thenReturn(Optional.of(projeto));
        when(repositorioUsuario.buscarPorEmail("owner@example.com")).thenReturn(Optional.of(proprietario));
        when(repositorioMembroProjeto.existePorIdProjetoEIdUsuario(10L, 1L)).thenReturn(true);

        assertThatThrownBy(() -> servicoProjeto.removerMembro(10L, 1L, "owner@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("owner");
    }
}
