package com.taskmanager.api.repository;

import com.taskmanager.api.entity.StatusTarefa;
import com.taskmanager.api.entity.Tarefa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RepositorioTarefa extends JpaRepository<Tarefa, Long>, JpaSpecificationExecutor<Tarefa> {

    @Query("SELECT t FROM Tarefa t WHERE t.projeto.id = :idProjeto")
    List<Tarefa> buscarPorIdProjeto(@Param("idProjeto") Long idProjeto);

    @Query("SELECT COUNT(t) FROM Tarefa t WHERE t.responsavel.id = :idResponsavel AND t.status = :status")
    long contarPorIdResponsavelEStatus(@Param("idResponsavel") Long idResponsavel, @Param("status") StatusTarefa status);

    @Query("SELECT t FROM Tarefa t WHERE t.projeto.id = :idProjeto AND " +
           "(LOWER(t.titulo) LIKE LOWER(CONCAT('%',:termo,'%')) OR " +
           "LOWER(t.descricao) LIKE LOWER(CONCAT('%',:termo,'%')))")
    List<Tarefa> buscarPorTexto(@Param("idProjeto") Long idProjeto, @Param("termo") String termo);

    @Query("SELECT t.status, COUNT(t) FROM Tarefa t WHERE t.projeto.id = :idProjeto GROUP BY t.status")
    List<Object[]> contarPorIdProjetoAgrupadoPorStatus(@Param("idProjeto") Long idProjeto);

    @Query("SELECT t.prioridade, COUNT(t) FROM Tarefa t WHERE t.projeto.id = :idProjeto GROUP BY t.prioridade")
    List<Object[]> contarPorIdProjetoAgrupadoPorPrioridade(@Param("idProjeto") Long idProjeto);

    @Modifying
    @Query("DELETE FROM Tarefa t WHERE t.projeto.id = :idProjeto")
    void deletarPorIdProjeto(@Param("idProjeto") Long idProjeto);
}
