package com.efectiva.financiera.controllers;

import com.efectiva.financiera.dto.LoginRequestDto;
import com.efectiva.financiera.dto.RegistroRequestDto;
import com.efectiva.financiera.models.Usuario;
import com.efectiva.financiera.models.Asesor;
import com.efectiva.financiera.models.Cuenta;
import com.efectiva.financiera.services.UsuarioService;
import com.efectiva.financiera.repositories.UsuarioRepository;
import com.efectiva.financiera.repositories.AsesorRepository;
import com.efectiva.financiera.repositories.CuentaRepository;
import com.efectiva.financiera.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "${FRONTEND_URL:http://localhost:8080}")

public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final AsesorRepository asesorRepository;
    private final CuentaRepository cuentaRepository;
    private final JwtUtil jwtUtil;

    // SEGURIDAD: PREVENCIÓN DE FUERZA BRUTA
    private static final Map<String, Integer> intentosFallidos = new HashMap<>();
    private static final int MAX_INTENTOS = 3;

    public UsuarioController(UsuarioService usuarioService,
                             UsuarioRepository usuarioRepository,
                             AsesorRepository asesorRepository,
                             CuentaRepository cuentaRepository,
                             JwtUtil jwtUtil) {
        this.usuarioService = usuarioService;
        this.usuarioRepository = usuarioRepository;
        this.asesorRepository = asesorRepository;
        this.cuentaRepository = cuentaRepository;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequestDto dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            usuarioService.registrarUsuario(dto);
            Optional<Usuario> nuevoUsuario = usuarioRepository.findByCorreo(dto.getCorreo());

            if (nuevoUsuario.isPresent()) {
                Cuenta nuevaCuenta = new Cuenta();
                nuevaCuenta.setUsuarioId(nuevoUsuario.get().getId());

                Random random = new Random();
                int numeroAleatorio = 1000000 + random.nextInt(9000000);
                nuevaCuenta.setNumeroCuenta("193-" + numeroAleatorio);

                nuevaCuenta.setTipoCuenta("Ahorro Digital");
                nuevaCuenta.setMoneda("PEN");
                nuevaCuenta.setSaldo(BigDecimal.ZERO);
                nuevaCuenta.setEstado("ACTIVA");

                cuentaRepository.save(nuevaCuenta);
            }

            response.put("success", true);
            response.put("message", "Usuario y Cuenta creados correctamente");
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

        // SEGURIDAD: Verificar si la cuenta está bloqueada por Fuerza Bruta
        if (intentosFallidos.getOrDefault(dto.getCorreo(), 0) >= MAX_INTENTOS) {
            response.put("success", false);
            response.put("message", "Seguridad: Cuenta bloqueada temporalmente por múltiples intentos fallidos.");
            return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
        }

        try {
            // 1. Intentar loguear como CLIENTE
            try {
                Usuario usuario = usuarioService.login(dto);
                String token = jwtUtil.generarToken(usuario.getCorreo(), "CLIENTE");

                intentosFallidos.remove(dto.getCorreo()); // Resetea los intentos si es exitoso

                response.put("success", true);
                response.put("data", usuario);
                response.put("tipo", "CLIENTE");
                response.put("token", token);
                response.put("redirect", "dashboard.html");
                return ResponseEntity.ok(response);
            } catch (RuntimeException e) {
                if (e.getMessage().equals("Contraseña incorrecta")) {
                    throw e; // Pasa al catch final para sumar el intento
                }
            }

            // 2. Intentar loguear como PERSONAL INTERNO
            Optional<Asesor> asesorOpt = asesorRepository.findByCorreo(dto.getCorreo());
            if (asesorOpt.isPresent()) {
                Asesor asesor = asesorOpt.get();
                if (asesor.getPassword().equals(dto.getPassword())) {
                    String token = jwtUtil.generarToken(asesor.getCorreo(), asesor.getRol());

                    intentosFallidos.remove(dto.getCorreo()); // Resetea los intentos si es exitoso

                    response.put("success", true);
                    response.put("data", asesor);
                    response.put("tipo", asesor.getRol());
                    response.put("token", token);

                    if (asesor.getRol().equals("GERENTE") || asesor.getRol().equals("ADMINISTRADOR")) {
                        response.put("redirect", "admin_reportes.html");
                    } else {
                        response.put("redirect", "admin_bandeja.html");
                    }
                    return ResponseEntity.ok(response);
                } else {
                    throw new RuntimeException("Contraseña incorrecta");
                }
            }

            throw new RuntimeException("Usuario no encontrado");

        } catch (RuntimeException e) {
            // SEGURIDAD: Sumar intento fallido por Fuerza Bruta
            intentosFallidos.put(dto.getCorreo(), intentosFallidos.getOrDefault(dto.getCorreo(), 0) + 1);

            response.put("success", false);
            response.put("message", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }
    }
}