package com.example.Database_TCC.repository;
import com.example.Database_TCC.model.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
public interface HistoricoCheckinRepository extends JpaRepository<HistoricoCheckin,Long> {
    List<HistoricoCheckin> findByUsuarioIdAndDataHoraRegistroGreaterThanEqualAndDataHoraRegistroLessThan(Long id,LocalDateTime inicio,LocalDateTime fim);
    @Query("select h.dataHoraRegistro from HistoricoCheckin h where h.usuario.id=:id and h.pontosGanhos>0 and h.dataHoraRegistro<:limite order by h.dataHoraRegistro desc")
    List<LocalDateTime> datasComPontos(@Param("id") Long usuarioId,@Param("limite") LocalDateTime limite);
    boolean existsByUsuario(Usuario usuario);
    long countByUsuarioAndTipoRefeicaoIgnoreCaseAndStatusPrecisaoStartingWithIgnoreCase(Usuario usuario,String tipo,String precisao);
    long countByUsuarioAndStatusPrecisaoStartingWithIgnoreCase(Usuario usuario,String precisao);
}
