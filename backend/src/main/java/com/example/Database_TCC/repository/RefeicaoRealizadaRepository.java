package com.example.Database_TCC.repository;
import com.example.Database_TCC.model.RefeicaoRealizada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
public interface RefeicaoRealizadaRepository extends JpaRepository<RefeicaoRealizada, Long> {
    @Query("select r from RefeicaoRealizada r where r.usuario.id = :usuarioId and r.dataHoraConsumo >= :inicio and r.dataHoraConsumo < :fim order by r.dataHoraConsumo desc, r.id desc")
    List<RefeicaoRealizada> buscarPorDia(@Param("usuarioId") Long usuarioId,
            @Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    Optional<RefeicaoRealizada> findByIdAndUsuarioId(Long id, Long usuarioId);
}
