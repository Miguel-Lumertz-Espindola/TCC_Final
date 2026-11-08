package com.example.Database_TCC.repository;

import com.example.Database_TCC.model.Conquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConquistaRepository extends JpaRepository<Conquista, Long> {
    // ATUALIZADO AQUI: findByNome
    Conquista findByNome(String nome);
}