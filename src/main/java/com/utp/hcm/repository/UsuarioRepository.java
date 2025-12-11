package com.utp.hcm.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // <-- NECESITAS ESTE IMPORT
import org.springframework.stereotype.Repository;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Usuario;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // ... (Métodos existentes) ...
    boolean existsByEmpleadoId(int idEmpleado);

    boolean existsByCorreoInstitucional(String correoInstitucional);

    Optional<Usuario> findByEmpleado(Empleado empleado);

    Optional<Usuario> findByCorreoInstitucional(String correoInstitucional);

    Optional<Usuario> findByEmpleadoId(int idEmpleado);

    // 🚀 MÉTODO CLAVE PARA CARGAR EL PERFIL COMPLETO:
    /**
     * Busca el Usuario y carga inmediatamente su entidad Empleado asociada.
     * Esto previene LazyInitializationException y errores de 'null' en el
     * controlador.
     */
    @Query("SELECT u FROM Usuario u JOIN FETCH u.empleado e WHERE u.correoInstitucional = :correoInstitucional")
    Optional<Usuario> findByCorreoInstitucionalConEmpleado(String correoInstitucional);
}