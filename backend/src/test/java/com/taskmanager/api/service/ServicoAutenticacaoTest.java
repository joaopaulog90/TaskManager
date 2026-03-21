package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoCadastro;
import com.taskmanager.api.dto.request.RequisicaoLogin;
import com.taskmanager.api.dto.response.RespostaAutenticacao;
import com.taskmanager.api.dto.response.RespostaUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

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
class ServicoAutenticacaoTest {

    @Autowired
    private ServicoAutenticacao servicoAutenticacao;

    @Test
    void cadastrar_deveRetornarUsuarioCriado() {
        RequisicaoCadastro requisicao = new RequisicaoCadastro();
        requisicao.setNome("João Silva");
        requisicao.setEmail("joao@exemplo.com");
        requisicao.setSenha("senha123");

        RespostaUsuario resposta = servicoAutenticacao.cadastrar(requisicao);

        assertThat(resposta.getId()).isNotNull();
        assertThat(resposta.getNome()).isEqualTo("João Silva");
        assertThat(resposta.getEmail()).isEqualTo("joao@exemplo.com");
    }

    @Test
    void cadastrar_deveLancarExcecaoSeEmailDuplicado() {
        RequisicaoCadastro requisicao = new RequisicaoCadastro();
        requisicao.setNome("João Silva");
        requisicao.setEmail("duplicado@exemplo.com");
        requisicao.setSenha("senha123");

        servicoAutenticacao.cadastrar(requisicao);

        assertThatThrownBy(() -> servicoAutenticacao.cadastrar(requisicao))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email já cadastrado");
    }

    @Test
    void autenticar_deveRetornarTokenValido() {
        RequisicaoCadastro registro = new RequisicaoCadastro();
        registro.setNome("Maria Souza");
        registro.setEmail("maria@exemplo.com");
        registro.setSenha("senha456");
        servicoAutenticacao.cadastrar(registro);

        RequisicaoLogin login = new RequisicaoLogin();
        login.setEmail("maria@exemplo.com");
        login.setSenha("senha456");

        RespostaAutenticacao resposta = servicoAutenticacao.autenticar(login);

        assertThat(resposta.getToken()).isNotBlank();
        assertThat(resposta.getEmail()).isEqualTo("maria@exemplo.com");
        assertThat(resposta.getIdUsuario()).isNotNull();
    }
}
