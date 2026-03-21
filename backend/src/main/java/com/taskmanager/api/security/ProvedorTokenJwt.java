package com.taskmanager.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class ProvedorTokenJwt {

    private final SecretKey chaveSecreta;
    private final long expiracaoMs;

    public ProvedorTokenJwt(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms}") long expiracaoMs) {
        this.chaveSecreta = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiracaoMs = expiracaoMs;
    }

    public String gerarToken(String email) {
        Date agora = new Date();
        Date expiracao = new Date(agora.getTime() + expiracaoMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(agora)
                .expiration(expiracao)
                .signWith(chaveSecreta)
                .compact();
    }

    public String extrairEmail(String tokenJwt) {
        return extrairClaims(tokenJwt).getSubject();
    }

    public boolean validarToken(String tokenJwt) {
        try {
            extrairClaims(tokenJwt);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extrairClaims(String tokenJwt) {
        return Jwts.parser()
                .verifyWith(chaveSecreta)
                .build()
                .parseSignedClaims(tokenJwt)
                .getPayload();
    }
}
