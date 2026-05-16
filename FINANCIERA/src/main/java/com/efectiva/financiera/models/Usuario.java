package com.efectiva.financiera.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Data // Magia de Lombok: nos genera automáticamente los Getters, Setters y constructores invisibles
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tipo_documento", nullable = false)
    private String tipoDocumento;

    @Column(name = "numero_documento", nullable = false, unique = true)
    private String numeroDocumento;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "password", nullable = false)
    private String password;
}