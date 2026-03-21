package com.taskmanager.api.service;

import com.taskmanager.api.dto.request.RequisicaoAlteracaoPerfil;
import com.taskmanager.api.dto.response.RespostaUsuario;
import com.taskmanager.api.entity.Perfil;
import com.taskmanager.api.entity.Usuario;
import com.taskmanager.api.repository.RepositorioUsuario;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoUsuario {

    private final RepositorioUsuario repositorioUsuario;

    public ServicoUsuario(RepositorioUsuario repositorioUsuario) {
        this.repositorioUsuario = repositorioUsuario;
    }

    @Transactional(readOnly = true)
    public java.util.List<RespostaUsuario> listarTodos(String emailRequisitante) {
        Usuario requisitante = repositorioUsuario.buscarPorEmail(emailRequisitante)
                .orElseThrow(() -> new EntityNotFoundException("Usuário requisitante não encontrado"));

        if (requisitante.getPerfil() != Perfil.ADMIN) {
            throw new AccessDeniedException("Apenas ADMINs podem listar usuários");
        }

        return repositorioUsuario.findAll().stream()
                .map(RespostaUsuario::new)
                .toList();
    }

    @Transactional
    public RespostaUsuario alterarPerfil(String emailRequisitante, Long idAlvo, RequisicaoAlteracaoPerfil requisicao) {
        Usuario requisitante = repositorioUsuario.buscarPorEmail(emailRequisitante)
                .orElseThrow(() -> new EntityNotFoundException("Usuário requisitante não encontrado"));

        if (requisitante.getPerfil() != Perfil.ADMIN) {
            throw new AccessDeniedException("Apenas ADMINs podem alterar perfis de usuários");
        }

        Usuario alvo = repositorioUsuario.findById(idAlvo)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + idAlvo));

        alvo.setPerfil(requisicao.getPerfil());
        Usuario atualizado = repositorioUsuario.save(alvo);
        return new RespostaUsuario(atualizado);
    }
}
