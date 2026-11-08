package com.example.Database_TCC.repository;

import com.example.Database_TCC.model.Usuario;
import com.example.Database_TCC.model.UsuarioConquista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioConquistaRepository extends JpaRepository<UsuarioConquista, Long> {
    // Retorna todas as medalhas que um usuário específico já desbloqueou
    List<UsuarioConquista> findByUsuario(Usuario usuario);

    // Verifica se o usuário já tem uma medalha específica (para não dar a mesma medalha duas vezes)
    boolean existsByUsuarioAndConquista(Usuario usuario, com.example.Database_TCC.model.Conquista conquista);
}