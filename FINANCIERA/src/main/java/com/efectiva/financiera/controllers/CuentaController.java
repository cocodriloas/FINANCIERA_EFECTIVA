package com.efectiva.financiera.controllers;

import com.efectiva.financiera.models.Cuenta;
import com.efectiva.financiera.repositories.CuentaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cuentas")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8080}")

public class CuentaController {

    private final CuentaRepository cuentaRepository;

    public CuentaController(CuentaRepository cuentaRepository) {
        this.cuentaRepository = cuentaRepository;
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<?> getCuentasPorUsuario(@PathVariable String usuarioId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Cuenta> cuentas = cuentaRepository.findByUsuarioId(UUID.fromString(usuarioId));
            if (cuentas.isEmpty()) {
                response.put("success", false);
                response.put("message", "No se encontraron cuentas para este usuario");
                return ResponseEntity.ok(response);
            }
            response.put("success", true);
            response.put("cuentas", cuentas);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}