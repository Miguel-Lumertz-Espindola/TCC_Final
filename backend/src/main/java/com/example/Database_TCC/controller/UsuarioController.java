package com.example.Database_TCC.controller;

import com.example.Database_TCC.model.Usuario;
import com.example.Database_TCC.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;


    @Autowired
    private com.example.Database_TCC.service.CadastroService cadastro;

    @PostMapping("/cadastrar")
    public ResponseEntity<String> cadastrar(@RequestBody com.example.Database_TCC.dto.CadastroRequest dados) {
        try {
            cadastro.cadastrar(dados);
            return ResponseEntity.ok("Usuário, mascote e sugestão alimentar cadastrados com sucesso!");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getReason());
        }
    }

    @GetMapping("/dieta")
    public com.example.Database_TCC.dto.DietaSugeridaDTO consultarDieta(@RequestParam("email") String email) {
        return cadastro.consultar(email);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Usuario loginUsuario) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(loginUsuario.getEmail());

        if (usuario.isPresent() && usuario.get().getSenha().equals(loginUsuario.getSenha())) {
            return ResponseEntity.ok("Login efetuado com sucesso!");
        }

        return ResponseEntity.status(401).body("E-mail ou senha incorretos");
    }
}
