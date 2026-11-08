package com.example.Database_TCC.repository;

import com.example.Database_TCC.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // O Spring interpreta o nome do metodo e cria a query SQL em background
    Optional<Usuario> findByEmail(String email);

    // Serializa revisoes de metas do mesmo usuario, inclusive a primeira.
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> buscarParaAtualizarMetas(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.email = :email")
    Optional<Usuario> buscarParaAtualizarGamificacao(@org.springframework.data.repository.query.Param("email") String email);
}
