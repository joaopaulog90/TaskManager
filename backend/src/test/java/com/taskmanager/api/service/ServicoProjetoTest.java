package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoMembro;
import com.taskmanager.api.dto.request.RequisicaoProjeto;
import com.taskmanager.api.dto.response.RespostaMembro;
import com.taskmanager.api.dto.response.RespostaProjeto;
import com.taskmanager.api.entity.Perfil;
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
class ServicoProjetoTest {

    @Autowired
    private ServicoProjeto servicoProjeto;

    @Autowired
    private RepositorioUsuario repositorioUsuario;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Usuario adminUser;
    private Usuario memberUser;
    private Usuario outsiderUser;

    @BeforeEach
    void setUp() {
        adminUser = criarUsuario("Admin Teste", "admin@projeto.com", Perfil.ADMIN);
        memberUser = criarUsuario("Membro Teste", "membro@projeto.com", Perfil.MEMBER);
        outsiderUser = criarUsuario("Fora do Projeto", "fora@projeto.com", Perfil.MEMBER);
    }

    @Test
    void criarProjeto_ownerDeveSerAdicionadoComoMembro() {
        RequisicaoProjeto req = requisicaoProjeto("Projeto Alpha", "Descricao");

        RespostaProjeto resposta = servicoProjeto.criar(req, adminUser.getEmail());

        assertThat(resposta.getId()).isNotNull();
        assertThat(resposta.getNome()).isEqualTo("Projeto Alpha");
        assertThat(resposta.getIdProprietario()).isEqualTo(adminUser.getId());
        assertThat(resposta.getQuantidadeMembros()).isEqualTo(1);

        List<RespostaMembro> membros = servicoProjeto.listarMembros(resposta.getId(), adminUser.getEmail());
        assertThat(membros).hasSize(1);
        assertThat(membros.get(0).getPerfil()).isEqualTo(Perfil.ADMIN);
        assertThat(membros.get(0).getIdUsuario()).isEqualTo(adminUser.getId());
    }

    @Test
    void listarProjetos_usuarioVeApenasOsSeusProjetos() {
        RespostaProjeto projetoA = servicoProjeto.criar(requisicaoProjeto("Projeto A", null), adminUser.getEmail());
        RespostaProjeto projetoB = servicoProjeto.criar(requisicaoProjeto("Projeto B", null), adminUser.getEmail());

        RequisicaoMembro addMember = new RequisicaoMembro();
        addMember.setIdUsuario(memberUser.getId());
        servicoProjeto.adicionarMembro(projetoB.getId(), addMember, adminUser.getEmail());

        List<RespostaProjeto> projetosAdmin = servicoProjeto.listarDoUsuario(adminUser.getEmail());
        List<RespostaProjeto> projetosMembro = servicoProjeto.listarDoUsuario(memberUser.getEmail());

        assertThat(projetosAdmin).hasSize(2);
        assertThat(projetosMembro).hasSize(1);
        assertThat(projetosMembro.get(0).getNome()).isEqualTo("Projeto B");
    }

    @Test
    void criarProjeto_memberNaoPodeCriar() {
        assertThatThrownBy(() ->
                servicoProjeto.criar(requisicaoProjeto("Projeto Proibido", null), memberUser.getEmail()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void atualizarProjeto_memberNaoPodeAtualizar() {
        RespostaProjeto projeto = servicoProjeto.criar(requisicaoProjeto("Projeto Restrito", null), adminUser.getEmail());

        RequisicaoMembro addMember = new RequisicaoMembro();
        addMember.setIdUsuario(memberUser.getId());
        servicoProjeto.adicionarMembro(projeto.getId(), addMember, adminUser.getEmail());

        assertThatThrownBy(() ->
                servicoProjeto.atualizar(projeto.getId(), requisicaoProjeto("Novo Nome", null), memberUser.getEmail()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adicionarMembro_memberNaoPodeAdicionarOutroMembro() {
        RespostaProjeto projeto = servicoProjeto.criar(requisicaoProjeto("Projeto B", null), adminUser.getEmail());

        RequisicaoMembro addMember = new RequisicaoMembro();
        addMember.setIdUsuario(memberUser.getId());
        servicoProjeto.adicionarMembro(projeto.getId(), addMember, adminUser.getEmail());

        RequisicaoMembro tentativa = new RequisicaoMembro();
        tentativa.setIdUsuario(outsiderUser.getId());

        assertThatThrownBy(() ->
                servicoProjeto.adicionarMembro(projeto.getId(), tentativa, memberUser.getEmail()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void adicionarMembro_membroDuplicadoLancaExcecao() {
        RespostaProjeto projeto = servicoProjeto.criar(requisicaoProjeto("Projeto C", null), adminUser.getEmail());

        RequisicaoMembro addMember = new RequisicaoMembro();
        addMember.setIdUsuario(memberUser.getId());
        servicoProjeto.adicionarMembro(projeto.getId(), addMember, adminUser.getEmail());

        assertThatThrownBy(() ->
                servicoProjeto.adicionarMembro(projeto.getId(), addMember, adminUser.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("já é membro");
    }

    @Test
    void removerMembro_naoEPossivelRemoverOwner() {
        RespostaProjeto projeto = servicoProjeto.criar(requisicaoProjeto("Projeto D", null), adminUser.getEmail());

        assertThatThrownBy(() ->
                servicoProjeto.removerMembro(projeto.getId(), adminUser.getId(), adminUser.getEmail()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("owner");
    }

    private Usuario criarUsuario(String nome, String email, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode("senha123"));
        usuario.setPerfil(perfil);
        return repositorioUsuario.save(usuario);
    }

    private RequisicaoProjeto requisicaoProjeto(String nome, String descricao) {
        RequisicaoProjeto req = new RequisicaoProjeto();
        req.setNome(nome);
        req.setDescricao(descricao);
        return req;
    }
}
