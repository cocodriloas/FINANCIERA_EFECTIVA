package com.efectiva.financiera.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Table(name = "asesores")
@Data
public class Asesor {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String nombres;
    private String correo;
    private String password;
    private String rol;

    @Column(name = "agencia_id")
    private UUID agenciaId;
}