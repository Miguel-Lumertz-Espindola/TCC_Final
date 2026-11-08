package com.example.Database_TCC.repository;

import com.example.Database_TCC.model.DietaUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DietaUsuarioRepository extends JpaRepository<DietaUsuario, Long> {
    Optional<DietaUsuario> findByUsuarioId(Long usuarioId);
}
