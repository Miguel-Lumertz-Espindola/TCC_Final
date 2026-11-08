package com.example.Database_TCC.repository;

import com.example.Database_TCC.model.StatusGamificacao;
import com.example.Database_TCC.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
      public interface StatusGamificacaoRepository extends JpaRepository<StatusGamificacao, Long> {
    // Retorna a lista decrescente baseada na pontuação
    List<StatusGamificacao> findTop50ByOrderByPontuacaoTotalDesc();
    Optional<StatusGamificacao> findByUsuario(Usuario usuario);
}