package com.efectiva.financiera.services;

import com.efectiva.financiera.dto.LoginRequestDto;
import com.efectiva.financiera.dto.RegistroRequestDto;
import com.efectiva.financiera.models.Usuario;
import com.efectiva.financiera.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario registrarUsuario(RegistroRequestDto dto) {
        if (usuarioRepository.existsByNumeroDocumento(dto.getNumeroDocumento())) {
            throw new RuntimeException("Ya existe un usuario con ese número de documento");
        }
        if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("Ya existe un usuario con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setTipoDocumento(dto.getTipoDocumento());
        usuario.setNumeroDocumento(dto.getNumeroDocumento());
        usuario.setNombres(dto.getNombres());
        usuario.setCorreo(dto.getCorreo());
        usuario.setPassword(dto.getPassword());
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario login(LoginRequestDto dto) {
        Usuario usuario = usuarioRepository.findByCorreo(dto.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getPassword().equals(dto.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return usuario;
    }
}