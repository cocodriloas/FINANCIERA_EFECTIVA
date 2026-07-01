package com.efectiva.financiera.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Dejamos pasar las peticiones previas de los navegadores (CORS)
        if (request.getMethod().equals("OPTIONS")) {
            return true;
        }

        // Buscamos el Token en la cabecera de la petición
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Acceso denegado: Token no proporcionado o formato invalido");
            return false;
        }

        String token = authHeader.substring(7); // Quitamos la palabra "Bearer "

        try {
            // Validamos que la "Pulsera VIP" sea real y no esté vencida
            String correo = jwtUtil.extraerCorreo(token);
            if (correo != null && jwtUtil.validarToken(token, correo)) {
                // Extraer los datos del token y pasarlos a la request
                String rol = jwtUtil.extraerRol(token);
                String idUsuario = jwtUtil.extraerId(token); // Extraemos el ID

                request.setAttribute("rolUsuario", rol);
                request.setAttribute("correoUsuario", correo);
                request.setAttribute("usuarioId", idUsuario); // Guardamos el ID
                return true; // ¡El portero deja pasar la petición!
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Acceso denegado: Token expirado o alterado");
            return false;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        return false;
    }
}