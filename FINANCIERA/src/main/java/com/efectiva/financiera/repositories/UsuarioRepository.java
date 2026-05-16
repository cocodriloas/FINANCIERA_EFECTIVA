package com.efectiva.financiera.repositories;

import com.efectiva.financiera.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {
    boolean existsByNumeroDocumento(String numeroDocumento);
    boolean existsByCorreo(String correo);
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);

    // AGREGA ESTA LÍNEA:
    Optional<Usuario> findByCorreo(String correo);

}