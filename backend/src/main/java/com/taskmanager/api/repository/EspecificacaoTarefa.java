package com.taskmanager.api.repository;

import com.taskmanager.api.entity.PrioridadeTarefa;
import com.taskmanager.api.entity.StatusTarefa;
import com.taskmanager.api.entity.Tarefa;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class EspecificacaoTarefa {

    private EspecificacaoTarefa() {}

    public static Specification<Tarefa> doProjeto(Long idProjeto) {
        return (root, query, cb) -> cb.equal(root.get("projeto").get("id"), idProjeto);
    }

    public static Specification<Tarefa> comStatus(StatusTarefa status) {
        return (root, query, cb) -> status == null ? cb.conjunction()
                : cb.equal(root.get("status"), status);
    }

    public static Specification<Tarefa> comPrioridade(PrioridadeTarefa prioridade) {
        return (root, query, cb) -> prioridade == null ? cb.conjunction()
                : cb.equal(root.get("prioridade"), prioridade);
    }

    public static Specification<Tarefa> comResponsavel(Long idResponsavel) {
        return (root, query, cb) -> idResponsavel == null ? cb.conjunction()
                : cb.equal(root.get("responsavel").get("id"), idResponsavel);
    }

    public static Specification<Tarefa> criadoAPartirDe(LocalDateTime de) {
        return (root, query, cb) -> de == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("criadoEm"), de);
    }

    public static Specification<Tarefa> criadoAte(LocalDateTime ate) {
        return (root, query, cb) -> ate == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("criadoEm"), ate);
    }

    public static Specification<Tarefa> prazoAPartirDe(LocalDateTime de) {
        return (root, query, cb) -> de == null ? cb.conjunction()
                : cb.greaterThanOrEqualTo(root.get("prazo"), de);
    }

    public static Specification<Tarefa> prazoAte(LocalDateTime ate) {
        return (root, query, cb) -> ate == null ? cb.conjunction()
                : cb.lessThanOrEqualTo(root.get("prazo"), ate);
    }
}
