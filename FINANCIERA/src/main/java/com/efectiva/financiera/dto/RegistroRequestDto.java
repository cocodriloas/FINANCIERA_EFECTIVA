package com.efectiva.financiera.dto;

import lombok.Data;

@Data // Esto de Lombok nos ahorra escribir todos los Getters y Setters
public class RegistroRequestDto {
    private String tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String correo;
    private String password;
}