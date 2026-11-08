package com.example.Database_TCC.controller;
import com.example.Database_TCC.dto.*;
import com.example.Database_TCC.service.MetasService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;
@RestController
@RequestMapping("/api/metas")
public class MetasController {
    private final MetasService service;
    public MetasController(MetasService service){this.service=service;}
    @GetMapping public MetasConsultaDTO consultar(@RequestParam("email") String email,
            @RequestParam(value="data",required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate data) {
        return service.consultar(email,data);
    }
    @GetMapping("/historico") public List<MetasDTO> historico(@RequestParam("email") String email) { return service.historico(email); }
    @PostMapping public MetasDTO salvar(@RequestParam("email") String email,@RequestBody MetasRequest dados) { return service.salvar(email,dados); }
    @ExceptionHandler(ResponseStatusException.class) public ResponseEntity<String> erro(ResponseStatusException erro) {
        return ResponseEntity.status(erro.getStatusCode()).body(erro.getReason());
    }
}
