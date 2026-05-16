package com.efectiva.financiera.controllers;

import com.efectiva.financiera.dto.LoginRequestDto;
import com.efectiva.financiera.dto.RegistroRequestDto;
import com.efectiva.financiera.models.Usuario;
import com.efectiva.financiera.services.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequestDto dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            usuarioService.registrarUsuario(dto);
            response.put("success", true);
            response.put("message", "Usuario registrado correctamente");
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            Usuario usuario = usuarioService.login(dto);
            response.put("success", true);
            response.put("data", usuario);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}