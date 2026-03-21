package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoCadastro;
import com.taskmanager.api.dto.request.RequisicaoLogin;
import com.taskmanager.api.dto.response.RespostaAutenticacao;
import com.taskmanager.api.dto.response.RespostaUsuario;
import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioUsuario;
import com.taskmanager.api.security.ProvedorTokenJwt;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoAutenticacaoUnitTest {

    @Mock
    private RepositorioUsuario repositorioUsuario;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ProvedorTokenJwt provedorToken;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private ServicoAutenticacao servicoAutenticacao;

    @Test
    void cadastrar_deveRetornarRespostaUsuario_quandoDadosValidos() {
        RequisicaoCadastro requisicao = new RequisicaoCadastro();
        requisicao.setNome("Ana Lima");
        requisicao.setEmail("ana@example.com");
        requisicao.setSenha("senha123");

        when(repositorioUsuario.existePorEmail("ana@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");

        Usuario salvo = new Usuario();
        salvo.setId(1L);
        salvo.setNome("Ana Lima");
        salvo.setEmail("ana@example.com");
        salvo.setSenha("hashed");
        salvo.setPerfil(Perfil.MEMBER);
        when(repositorioUsuario.save(any(Usuario.class))).thenReturn(salvo);

        RespostaUsuario resposta = servicoAutenticacao.cadastrar(requisicao);

        assertThat(resposta.getNome()).isEqualTo("Ana Lima");
        assertThat(resposta.getEmail()).isEqualTo("ana@example.com");
        assertThat(resposta.getId()).isEqualTo(1L);
    }

    @Test
    void cadastrar_deveLancarException_quandoEmailJaExiste() {
        RequisicaoCadastro requisicao = new RequisicaoCadastro();
        requisicao.setNome("Ana Lima");
        requisicao.setEmail("ana@example.com");
        requisicao.setSenha("senha123");

        when(repositorioUsuario.existePorEmail("ana@example.com")).thenReturn(true);

        assertThatThrownBy(() -> servicoAutenticacao.cadastrar(requisicao))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ana@example.com");
    }

    @Test
    void autenticar_deveRetornarRespostaAutenticacao_quandoCredenciaisValidas() {
        RequisicaoLogin requisicao = new RequisicaoLogin();
        requisicao.setEmail("ana@example.com");
        requisicao.setSenha("senha123");

        Authentication autenticacaoMock = mock(Authentication.class);
        when(autenticacaoMock.getName()).thenReturn("ana@example.com");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(autenticacaoMock);

        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setEmail("ana@example.com");
        usuario.setNome("Ana Lima");
        usuario.setPerfil(Perfil.MEMBER);
        when(repositorioUsuario.buscarPorEmail("ana@example.com")).thenReturn(Optional.of(usuario));

        when(provedorToken.gerarToken("ana@example.com")).thenReturn("token-fake");

        RespostaAutenticacao resposta = servicoAutenticacao.autenticar(requisicao);

        assertThat(resposta.getToken()).isEqualTo("token-fake");
        assertThat(resposta.getEmail()).isEqualTo("ana@example.com");
        assertThat(resposta.getIdUsuario()).isEqualTo(42L);
        assertThat(resposta.getPerfil()).isEqualTo(Perfil.MEMBER);
    }

    @Test
    void autenticar_deveLancarException_quandoAutenticacaoFalha() {
        RequisicaoLogin requisicao = new RequisicaoLogin();
        requisicao.setEmail("ana@example.com");
        requisicao.setSenha("senha-errada");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas"));

        assertThatThrownBy(() -> servicoAutenticacao.autenticar(requisicao))
                .isInstanceOf(org.springframework.security.authentication.BadCredentialsException.class);
    }
}
