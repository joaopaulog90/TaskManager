package com.taskmanager.api;

import com.taskmanager.api.dto.request.RequisicaoAtualizacaoTarefa;
import com.taskmanager.api.dto.request.RequisicaoCadastro;
import com.taskmanager.api.dto.request.RequisicaoLogin;
import com.taskmanager.api.dto.request.RequisicaoMembro;
import com.taskmanager.api.dto.request.RequisicaoProjeto;
import com.taskmanager.api.dto.request.RequisicaoTarefa;
import com.taskmanager.api.dto.request.RequisicaoAlteracaoPerfil;
import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.StatusTarefa;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste E2E do fluxo crítico completo via HTTP real:
 * registro → login → projeto → membro → tarefa → regras de negócio → resumo → histórico
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:e2etest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FluxoCriticoE2ETest {

    @Autowired
    private TestRestTemplate rest;

    private String tokenAdmin;
    private String tokenMember;
    private Long idAdmin;
    private Long idMember;
    private Long idProjeto;
    private Long idTarefa;

    // ─── 1. Registro e login ───

    @Test
    @Order(1)
    void registrarAdmin() {
        RequisicaoCadastro req = new RequisicaoCadastro();
        req.setNome("Admin E2E");
        req.setEmail("admin-e2e@test.com");
        req.setSenha("admin123");

        ResponseEntity<Map> res = rest.postForEntity("/api/auth/register", req, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idAdmin = ((Number) res.getBody().get("id")).longValue();
    }

    @Test
    @Order(2)
    void promoverParaAdmin() {
        // Login com admin seed para promover o usuário
        RequisicaoLogin loginSeed = new RequisicaoLogin();
        loginSeed.setEmail("admin@taskmanager.com");
        loginSeed.setSenha("admin123");
        ResponseEntity<Map> loginRes = rest.postForEntity("/api/auth/login", loginSeed, Map.class);
        String tokenSeed = (String) loginRes.getBody().get("token");

        RequisicaoAlteracaoPerfil perfil = new RequisicaoAlteracaoPerfil();
        perfil.setPerfil(Perfil.ADMIN);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokenSeed);
        headers.set("Content-Type", "application/json");

        ResponseEntity<Map> res = rest.exchange(
                "/api/users/" + idAdmin + "/profile",
                HttpMethod.PATCH, new HttpEntity<>(perfil, headers), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Order(3)
    void loginAdmin() {
        RequisicaoLogin req = new RequisicaoLogin();
        req.setEmail("admin-e2e@test.com");
        req.setSenha("admin123");

        ResponseEntity<Map> res = rest.postForEntity("/api/auth/login", req, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        tokenAdmin = (String) res.getBody().get("token");
        assertThat(tokenAdmin).isNotBlank();
    }

    @Test
    @Order(4)
    void registrarMember() {
        RequisicaoCadastro req = new RequisicaoCadastro();
        req.setNome("Member E2E");
        req.setEmail("member-e2e@test.com");
        req.setSenha("member123");

        ResponseEntity<Map> res = rest.postForEntity("/api/auth/register", req, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idMember = ((Number) res.getBody().get("id")).longValue();
    }

    @Test
    @Order(5)
    void loginMember() {
        RequisicaoLogin req = new RequisicaoLogin();
        req.setEmail("member-e2e@test.com");
        req.setSenha("member123");

        ResponseEntity<Map> res = rest.postForEntity("/api/auth/login", req, Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        tokenMember = (String) res.getBody().get("token");
        assertThat(tokenMember).isNotBlank();
    }

    // ─── 2. Projeto e membros ───

    @Test
    @Order(10)
    void adminCriaProjeto() {
        RequisicaoProjeto req = new RequisicaoProjeto();
        req.setNome("Projeto E2E");
        req.setDescricao("Teste de ponta a ponta");

        ResponseEntity<Map> res = rest.exchange("/api/projects",
                HttpMethod.POST, comAuth(req, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idProjeto = ((Number) res.getBody().get("id")).longValue();
    }

    @Test
    @Order(11)
    void memberNaoPodeCriarProjeto() {
        RequisicaoProjeto req = new RequisicaoProjeto();
        req.setNome("Projeto Proibido");

        ResponseEntity<Map> res = rest.exchange("/api/projects",
                HttpMethod.POST, comAuth(req, tokenMember), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(12)
    void adminAdicionaMembro() {
        RequisicaoMembro req = new RequisicaoMembro();
        req.setIdUsuario(idMember);

        ResponseEntity<Void> res = rest.exchange(
                "/api/projects/" + idProjeto + "/members",
                HttpMethod.POST, comAuth(req, tokenAdmin), Void.class);
        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // ─── 3. Tarefas com regras de negócio ───

    @Test
    @Order(20)
    void adminCriaTarefaCritical() {
        RequisicaoTarefa req = new RequisicaoTarefa();
        req.setTitulo("Tarefa Critical E2E");
        req.setDescricao("Tarefa para testar regra de CRITICAL");
        req.setPrioridade(PrioridadeTarefa.CRITICAL);
        req.setIdResponsavel(idMember);

        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks",
                HttpMethod.POST, comAuth(req, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        idTarefa = ((Number) res.getBody().get("id")).longValue();
        assertThat(res.getBody().get("status")).isEqualTo("TODO");
    }

    @Test
    @Order(21)
    void memberMoveParaInProgress() {
        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.IN_PROGRESS);

        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.PUT, comAuth(req, tokenMember), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("status")).isEqualTo("IN_PROGRESS");
    }

    @Test
    @Order(22)
    void memberNaoPodeConcluirCritical() {
        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.DONE);

        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.PUT, comAuth(req, tokenMember), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(23)
    void adminConcluiCritical() {
        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.DONE);

        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.PUT, comAuth(req, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("status")).isEqualTo("DONE");
    }

    @Test
    @Order(24)
    void doneNaoPodeVoltarParaTodo() {
        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.TODO);

        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.PUT, comAuth(req, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    @Order(25)
    void donePodeVoltarParaInProgress() {
        RequisicaoAtualizacaoTarefa req = new RequisicaoAtualizacaoTarefa();
        req.setStatus(StatusTarefa.IN_PROGRESS);

        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.PUT, comAuth(req, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody().get("status")).isEqualTo("IN_PROGRESS");
    }

    // ─── 4. Resumo e histórico ───

    @Test
    @Order(30)
    void resumoRetornaContadores() {
        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/summary",
                HttpMethod.GET, comAuth(null, tokenAdmin), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).containsKey("porStatus");
        assertThat(res.getBody()).containsKey("porPrioridade");
    }

    @Test
    @Order(31)
    void historicoRegistraAlteracoes() {
        ResponseEntity<List> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa + "/history",
                HttpMethod.GET, comAuth(null, tokenAdmin), List.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(res.getBody()).isNotEmpty();

        // Deve ter registros de criação + mudanças de status
        assertThat(res.getBody().size()).isGreaterThanOrEqualTo(3);
    }

    // ─── 5. Exclusão ───

    @Test
    @Order(40)
    void memberNaoPodeDeletarTarefa() {
        ResponseEntity<Map> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.DELETE, comAuth(null, tokenMember), Map.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Order(41)
    void adminDeletaTarefa() {
        ResponseEntity<Void> res = rest.exchange(
                "/api/projects/" + idProjeto + "/tasks/" + idTarefa,
                HttpMethod.DELETE, comAuth(null, tokenAdmin), Void.class);
        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    // ─── Helper ───

    private <T> HttpEntity<T> comAuth(T body, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set("Content-Type", "application/json");
        return new HttpEntity<>(body, headers);
    }
}
