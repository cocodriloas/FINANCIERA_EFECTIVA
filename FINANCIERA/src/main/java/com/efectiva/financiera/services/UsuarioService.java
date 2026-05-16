package com.efectiva.financiera.services;

import com.efectiva.financiera.dto.LoginRequestDto;
import com.efectiva.financiera.dto.RegistroRequestDto;
import com.efectiva.financiera.models.Usuario;

public interface UsuarioService {
    Usuario registrarUsuario(RegistroRequestDto dto);
    Usuario login(LoginRequestDto dto);
}