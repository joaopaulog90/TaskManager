package com.taskmanager.api.config;

import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioUsuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InicializadorAdminSeed implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InicializadorAdminSeed.class);

    private final RepositorioUsuario repositorioUsuario;
    private final PasswordEncoder passwordEncoder;
    private final PropriedadesSeedAdmin propriedades;

    public InicializadorAdminSeed(RepositorioUsuario repositorioUsuario,
                                   PasswordEncoder passwordEncoder,
                                   PropriedadesSeedAdmin propriedades) {
        this.repositorioUsuario = repositorioUsuario;
        this.passwordEncoder = passwordEncoder;
        this.propriedades = propriedades;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repositorioUsuario.existeAdmin()) {
            log.info("Admin seed ignorado: já existe ao menos um usuário ADMIN no banco.");
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(propriedades.getNome());
        admin.setEmail(propriedades.getEmail());
        admin.setSenha(passwordEncoder.encode(propriedades.getSenha()));
        admin.setPerfil(Perfil.ADMIN);

        repositorioUsuario.save(admin);
        log.info("Admin seed criado com email: {}", propriedades.getEmail());
    }
}
