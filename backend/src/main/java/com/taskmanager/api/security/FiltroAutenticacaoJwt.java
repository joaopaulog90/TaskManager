package com.taskmanager.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class FiltroAutenticacaoJwt extends OncePerRequestFilter {

    private final ProvedorTokenJwt provedorToken;
    private final ServicoDetalhesUsuario servicoDetalhes;

    public FiltroAutenticacaoJwt(ProvedorTokenJwt provedorToken,
                                  ServicoDetalhesUsuario servicoDetalhes) {
        this.provedorToken = provedorToken;
        this.servicoDetalhes = servicoDetalhes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String tokenJwt = extrairTokenDoHeader(request);

        if (StringUtils.hasText(tokenJwt) && provedorToken.validarToken(tokenJwt)) {
            String emailUsuario = provedorToken.extrairEmail(tokenJwt);
            UserDetails detalhesUsuario = servicoDetalhes.loadUserByUsername(emailUsuario);

            UsernamePasswordAuthenticationToken autenticacao = new UsernamePasswordAuthenticationToken(
                    detalhesUsuario, null, detalhesUsuario.getAuthorities()
            );
            autenticacao.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(autenticacao);
        }

        filterChain.doFilter(request, response);
    }

    private String extrairTokenDoHeader(HttpServletRequest request) {
        String headerAutorizacao = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAutorizacao) && headerAutorizacao.startsWith("Bearer ")) {
            return headerAutorizacao.substring(7);
        }
        return null;
    }
}
