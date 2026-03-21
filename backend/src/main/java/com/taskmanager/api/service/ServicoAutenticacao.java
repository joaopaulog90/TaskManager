package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoCadastro;
import com.taskmanager.api.dto.request.RequisicaoLogin;
import com.taskmanager.api.dto.response.RespostaAutenticacao;
import com.taskmanager.api.dto.response.RespostaUsuario;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioUsuario;
import com.taskmanager.api.security.ProvedorTokenJwt;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoAutenticacao {

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;
    private final ProvedorTokenJwt provedorToken;
    private final AuthenticationManager authenticationManager;

    public ServicoAutenticacao(RepositorioUsuario repositorioUsuario,
                                PasswordEncoder passwordEncoder,
                                ProvedorTokenJwt provedorToken,
                                AuthenticationManager authenticationManager) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
        this.provedorToken = provedorToken;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public RespostaUsuario cadastrar(RequisicaoCadastro requisicao) {
        if (repositorioUsuario.existePorEmail(requisicao.getEmail())) {
            throw new IllegalStateException("Email já cadastrado: " + requisicao.getEmail());
        }

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.getNome());
        usuario.setEmail(requisicao.getEmail());
        usuario.setSenha(passwordEncoder.encode(requisicao.getSenha()));

        Usuario salvo = repositorioUsuario.save(usuario);
        return new RespostaUsuario(salvo);
    }

    public RespostaAutenticacao autenticar(RequisicaoLogin requisicao) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requisicao.getEmail(), requisicao.getSenha())
        );

        String emailUsuarioAtual = autenticacao.getName();
        Usuario usuarioAtual = repositorioUsuario.buscarPorEmail(emailUsuarioAtual)
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado após autenticação"));

        String tokenJwt = provedorToken.gerarToken(emailUsuarioAtual);
        return new RespostaAutenticacao(tokenJwt, usuarioAtual.getId(), usuarioAtual.getEmail(), usuarioAtual.getPerfil());
    }
}
