package com.taskmanager.api.repository;

import com.taskmanager.api.entity.HistoricoTarefa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepositorioHistoricoTarefa extends JpaRepository<HistoricoTarefa, Long> {

    List<HistoricoTarefa> findByTarefaIdOrderByAlteradoEmAsc(Long idTarefa);

    void deleteByTarefaId(Long idTarefa);
}
