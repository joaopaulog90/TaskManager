package com.taskmanager.api.repository;

import com.taskmanager.api.entity.MembroProjeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositorioMembroProjeto extends JpaRepository<MembroProjeto, Long> {

    @Query("SELECT pm FROM MembroProjeto pm WHERE pm.projeto.id = :idProjeto AND pm.usuario.id = :idUsuario")
    Optional<MembroProjeto> buscarPorIdProjetoEIdUsuario(@Param("idProjeto") Long idProjeto, @Param("idUsuario") Long idUsuario);

    @Query("SELECT pm FROM MembroProjeto pm WHERE pm.projeto.id = :idProjeto")
    List<MembroProjeto> buscarPorIdProjeto(@Param("idProjeto") Long idProjeto);

    @Query("SELECT pm FROM MembroProjeto pm WHERE pm.usuario.id = :idUsuario")
    List<MembroProjeto> buscarPorIdUsuario(@Param("idUsuario") Long idUsuario);

    @Query("SELECT COUNT(pm) > 0 FROM MembroProjeto pm WHERE pm.projeto.id = :idProjeto AND pm.usuario.id = :idUsuario")
    boolean existePorIdProjetoEIdUsuario(@Param("idProjeto") Long idProjeto, @Param("idUsuario") Long idUsuario);

    @Modifying
    @Query("DELETE FROM MembroProjeto pm WHERE pm.projeto.id = :idProjeto AND pm.usuario.id = :idUsuario")
    void deletarPorIdProjetoEIdUsuario(@Param("idProjeto") Long idProjeto, @Param("idUsuario") Long idUsuario);

    @Query("SELECT pm.projeto.id, COUNT(pm) FROM MembroProjeto pm WHERE pm.projeto.id IN :idsProjeto GROUP BY pm.projeto.id")
    List<Object[]> contarPorIdsProjeto(@Param("idsProjeto") List<Long> idsProjeto);
}
