package com.example.Database_TCC.repository;
import com.example.Database_TCC.model.MetasUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.*;
public interface MetasUsuarioRepository extends JpaRepository<MetasUsuario,Long> {
    Optional<MetasUsuario> findFirstByUsuarioIdAndInicioVigenciaBeforeOrderByInicioVigenciaDescIdDesc(Long usuarioId, LocalDateTime limite);
    Optional<MetasUsuario> findFirstByUsuarioIdOrderByInicioVigenciaDescIdDesc(Long usuarioId);
    List<MetasUsuario> findTop50ByUsuarioIdOrderByInicioVigenciaDescIdDesc(Long usuarioId);
}
