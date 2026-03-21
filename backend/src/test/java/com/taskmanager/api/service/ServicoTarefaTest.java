package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoAtualizacaoTarefa;
import com.taskmanager.api.dto.request.RequisicaoMembro;
import com.taskmanager.api.dto.request.RequisicaoProjeto;
import com.taskmanager.api.dto.request.RequisicaoTarefa;
import com.taskmanager.api.dto.response.RespostaProjeto;
import com.taskmanager.api.dto.response.RespostaResumoTarefa;
import com.taskmanager.api.dto.response.RespostaTarefa;
import com.taskmanager.api.entity.PapelProjeto;
import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.StatusTarefa;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:taskmanager;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@Transactional
class ServicoTarefaTest {

    @Autowired
    private ServicoTarefa servicoTarefa;

    @Autowired
    private ServicoProjeto servicoProjeto;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario adminUser;
    private Usuario memberUser;
    private Usuario outsiderUser;
    private Long projectId;

    @BeforeEach
    void setUp() {
        adminUser = criarUsuario("Admin", "admin@task.com");
        memberUser = criarUsuario("Member", "member@task.com");
        outsiderUser = criarUsuario("Outsider", "outsider@task.com");

        RespostaProjeto project = servicoProjeto.criar(
                requisicaoProjeto("Projeto Task", null), adminUser.getEmail());
        projectId = project.getId();

        RequisicaoMembro addMember = new RequisicaoMembro();
        addMember.setIdUsuario(memberUser.getId());
        addMember.setPapel(PapelProjeto.MEMBER);
        servicoProjeto.adicionarMembro(projectId, addMember, adminUser.getEmail());
    }

    @Test
    void criarTarefa_statusInicialDeveSerTodo() {
        RequisicaoTarefa req = requisicaoTarefa("Tarefa A", PrioridadeTarefa.LOW, null);

        RespostaTarefa resposta = servicoTarefa.criar(projectId, req, adminUser.getEmail());

        assertThat(resposta.getId()).isNotNull();
        assertThat(resposta.getStatus()).isEqualTo(StatusTarefa.TODO);
        assertThat(resposta.getTitulo()).isEqualTo("Tarefa A");
    }

    @Test
    void criarTarefa_assigneeForaDoProjeto_lancaExcecao() {
        RequisicaoTarefa req = requisicaoTarefa("Tarefa B", PrioridadeTarefa.MEDIUM, outsiderUser.getId());

        assertThatThrownBy(() -> servicoTarefa.criar(projectId, req, adminUser.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("não é membro");
    }

    @Test
    void atualizarTarefa_doneParaTodo_lancaExcecao() {
        RespostaTarefa criada = servicoTarefa.criar(projectId, requisicaoTarefa("Tarefa C", PrioridadeTarefa.LOW, null), adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toInProgress = new RequisicaoAtualizacaoTarefa();
        toInProgress.setStatus(StatusTarefa.IN_PROGRESS);
        servicoTarefa.atualizar(projectId, criada.getId(), toInProgress, adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toDone = new RequisicaoAtualizacaoTarefa();
        toDone.setStatus(StatusTarefa.DONE);
        servicoTarefa.atualizar(projectId, criada.getId(), toDone, adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toTodo = new RequisicaoAtualizacaoTarefa();
        toTodo.setStatus(StatusTarefa.TODO);

        assertThatThrownBy(() -> servicoTarefa.atualizar(projectId, criada.getId(), toTodo, adminUser.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("DONE não pode voltar para TODO");
    }

    @Test
    void atualizarTarefa_criticalDonePorMember_lancaAccessDenied() {
        RespostaTarefa criada = servicoTarefa.criar(projectId,
                requisicaoTarefa("Tarefa Critical", PrioridadeTarefa.CRITICAL, null), adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toDone = new RequisicaoAtualizacaoTarefa();
        toDone.setStatus(StatusTarefa.DONE);

        assertThatThrownBy(() -> servicoTarefa.atualizar(projectId, criada.getId(), toDone, memberUser.getEmail()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void atualizarTarefa_criticalDonePorAdmin_sucesso() {
        RespostaTarefa criada = servicoTarefa.criar(projectId,
                requisicaoTarefa("Tarefa Critical Admin", PrioridadeTarefa.CRITICAL, null), adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toDone = new RequisicaoAtualizacaoTarefa();
        toDone.setStatus(StatusTarefa.DONE);

        RespostaTarefa resultado = servicoTarefa.atualizar(projectId, criada.getId(), toDone, adminUser.getEmail());

        assertThat(resultado.getStatus()).isEqualTo(StatusTarefa.DONE);
    }

    @Test
    void atualizarTarefa_wipLimitAtingido_lancaExcecao() {
        for (int i = 1; i <= 5; i++) {
            RespostaTarefa t = servicoTarefa.criar(projectId,
                    requisicaoTarefa("WIP " + i, PrioridadeTarefa.LOW, memberUser.getId()), adminUser.getEmail());

            RequisicaoAtualizacaoTarefa toIP = new RequisicaoAtualizacaoTarefa();
            toIP.setStatus(StatusTarefa.IN_PROGRESS);
            servicoTarefa.atualizar(projectId, t.getId(), toIP, adminUser.getEmail());
        }

        RespostaTarefa sexta = servicoTarefa.criar(projectId,
                requisicaoTarefa("WIP 6", PrioridadeTarefa.LOW, memberUser.getId()), adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toIP = new RequisicaoAtualizacaoTarefa();
        toIP.setStatus(StatusTarefa.IN_PROGRESS);

        assertThatThrownBy(() -> servicoTarefa.atualizar(projectId, sexta.getId(), toIP, adminUser.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("limite de 5 tarefas IN_PROGRESS");
    }

    @Test
    void buscarTarefas_deveRetornarApenasComTermoNoTituloOuDescricao() {
        servicoTarefa.criar(projectId, requisicaoTarefaComDesc("Implementar login", "auth com JWT", PrioridadeTarefa.HIGH, null), adminUser.getEmail());
        servicoTarefa.criar(projectId, requisicaoTarefaComDesc("Criar endpoint", "api rest", PrioridadeTarefa.MEDIUM, null), adminUser.getEmail());
        servicoTarefa.criar(projectId, requisicaoTarefaComDesc("Corrigir bug", "problema no login", PrioridadeTarefa.LOW, null), adminUser.getEmail());

        List<RespostaTarefa> resultado = servicoTarefa.buscar(projectId, "login", adminUser.getEmail());

        assertThat(resultado).hasSize(2);
        assertThat(resultado).allMatch(t ->
                t.getTitulo().toLowerCase().contains("login") ||
                (t.getDescricao() != null && t.getDescricao().toLowerCase().contains("login")));
    }

    @Test
    void resumo_deveRetornarContadoresPorStatus() {
        RespostaTarefa t1 = servicoTarefa.criar(projectId, requisicaoTarefa("T1", PrioridadeTarefa.LOW, null), adminUser.getEmail());
        RespostaTarefa t2 = servicoTarefa.criar(projectId, requisicaoTarefa("T2", PrioridadeTarefa.LOW, null), adminUser.getEmail());
        RespostaTarefa t3 = servicoTarefa.criar(projectId, requisicaoTarefa("T3", PrioridadeTarefa.LOW, null), adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toIP = new RequisicaoAtualizacaoTarefa();
        toIP.setStatus(StatusTarefa.IN_PROGRESS);
        servicoTarefa.atualizar(projectId, t2.getId(), toIP, adminUser.getEmail());

        RequisicaoAtualizacaoTarefa toDone = new RequisicaoAtualizacaoTarefa();
        toDone.setStatus(StatusTarefa.DONE);
        servicoTarefa.atualizar(projectId, t3.getId(), toDone, adminUser.getEmail());

        RespostaResumoTarefa resumo = servicoTarefa.resumo(projectId, adminUser.getEmail());

        assertThat(resumo.getPorStatus()).containsEntry("TODO", 1L);
        assertThat(resumo.getPorStatus()).containsEntry("IN_PROGRESS", 1L);
        assertThat(resumo.getPorStatus()).containsEntry("DONE", 1L);
    }

    private Usuario criarUsuario(String nome, String email) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("senha123"));
        return repositorioUsuario.save(usuario);
    }

    private RequisicaoProjeto requisicaoProjeto(String nome, String descricao) {
        RequisicaoProjeto req = new RequisicaoProjeto();
        req.setNome(nome);
        req.setDescricao(descricao);
        return req;
    }

    private RequisicaoTarefa requisicaoTarefa(String titulo, PrioridadeTarefa prioridade, Long idResponsavel) {
        RequisicaoTarefa req = new RequisicaoTarefa();
        req.setTitulo(titulo);
        req.setPrioridade(prioridade);
        req.setIdResponsavel(idResponsavel);
        return req;
    }

    private RequisicaoTarefa requisicaoTarefaComDesc(String titulo, String descricao, PrioridadeTarefa prioridade, Long idResponsavel) {
        RequisicaoTarefa req = new RequisicaoTarefa();
        req.setTitulo(titulo);
        req.setDescricao(descricao);
        req.setPrioridade(prioridade);
        req.setIdResponsavel(idResponsavel);
        return req;
    }
}
