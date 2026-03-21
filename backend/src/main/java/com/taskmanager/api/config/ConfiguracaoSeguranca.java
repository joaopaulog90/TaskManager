package com.taskmanager.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.api.security.FiltroAutenticacaoJwt;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.net.URI;
import java.util.List;

@Configuration
@EnableWebSecurity
public class ConfiguracaoSeguranca {

    private final FiltroAutenticacaoJwt filtroAutenticacaoJwt;
    private final UserDetailsService servicoDetalhes;
    private final ObjectMapper mapeadorObjeto;

    public ConfiguracaoSeguranca(FiltroAutenticacaoJwt filtroAutenticacaoJwt,
                                  UserDetailsService servicoDetalhes,
                                  ObjectMapper mapeadorObjeto) {
        this.filtroAutenticacaoJwt = filtroAutenticacaoJwt;
        this.servicoDetalhes = servicoDetalhes;
        this.mapeadorObjeto = mapeadorObjeto;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((requisicao, resposta, excecaoAutenticacao) -> {
                    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                            HttpStatus.UNAUTHORIZED, "Token ausente ou inválido");
                    problem.setType(URI.create("https://api.taskmanager.com/errors/unauthorized"));
                    problem.setTitle("Não autorizado");
                    problem.setInstance(URI.create(requisicao.getRequestURI()));
                    resposta.setStatus(401);
                    resposta.setContentType("application/problem+json");
                    resposta.getWriter().write(mapeadorObjeto.writeValueAsString(problem));
                })
                .accessDeniedHandler((requisicao, resposta, excecaoAcessoNegado) -> {
                    ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                            HttpStatus.FORBIDDEN, "Acesso negado");
                    problem.setType(URI.create("https://api.taskmanager.com/errors/forbidden"));
                    problem.setTitle("Acesso negado");
                    problem.setInstance(URI.create(requisicao.getRequestURI()));
                    resposta.setStatus(403);
                    resposta.setContentType("application/problem+json");
                    resposta.getWriter().write(mapeadorObjeto.writeValueAsString(problem));
                })
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))
            .addFilterBefore(filtroAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(servicoDetalhes);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
