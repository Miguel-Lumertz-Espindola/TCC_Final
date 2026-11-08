package com.example.Database_TCC.controller;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.service.DiarioAlimentarService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/refeicoes")
public class DiarioAlimentarController {
    private final DiarioAlimentarService diario;
    public DiarioAlimentarController(DiarioAlimentarService diario) { this.diario = diario; }
    @GetMapping
    public List<RefeicaoDTO> listar(@RequestParam("email") String email,
            @RequestParam("data") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return diario.listar(email, data);
    }
    @PostMapping
    public ResponseEntity<RefeicaoDTO> registrar(@RequestParam("email") String email, @RequestBody RefeicaoRequest dados) {
        return ResponseEntity.status(HttpStatus.CREATED).body(diario.registrar(email, dados));
    }
    @PutMapping("/{id}")
    public RefeicaoDTO editar(@RequestParam("email") String email, @PathVariable("id") Long id, @RequestBody RefeicaoRequest dados) {
        return diario.editar(email, id, dados);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@RequestParam("email") String email, @PathVariable("id") Long id) {
        diario.excluir(email, id); return ResponseEntity.noContent().build();
    }
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> erro(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(erro.getReason());
    }
}
