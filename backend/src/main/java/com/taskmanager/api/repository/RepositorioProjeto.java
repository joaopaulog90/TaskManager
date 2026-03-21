package com.taskmanager.api.repository;

import com.taskmanager.api.entity.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositorioProjeto extends JpaRepository<Projeto, Long> {
}
