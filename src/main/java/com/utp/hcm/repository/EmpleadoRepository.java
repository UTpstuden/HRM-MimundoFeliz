package com.utp.hcm.repository;

import com.utp.hcm.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

public interface EmpleadoRepository extends JpaRepository<Empleado, Integer> {

    // Método existente para DNI
    boolean existsByDni(String dni);

    // NUEVO MÉTODO: Verificar si existe un empleado con el correo
    boolean existsByCorreo(String correo);

    // NUEVO MÉTODO: Buscar empleado por correo
    Optional<Empleado> findByCorreo(String correo);

    // Método existente para DNI (si lo tienes)
    Optional<Empleado> findByDni(String dni);

    /**
     * NUEVO MÉTODO: Busca empleados que no están vinculados a ningún Usuario.
     * Se usará para llenar el <select> al crear un nuevo usuario.
     */
    @Query("SELECT e FROM Empleado e WHERE e.id NOT IN (SELECT u.empleado.id FROM Usuario u)")
    List<Empleado> findEmpleadosSinUsuario();

    @Query("SELECT e.cargo.nombreCargo, COUNT(e) FROM Empleado e GROUP BY e.cargo.nombreCargo")
    List<Object[]> countEmpleadosPorCargo();

    @Query("SELECT COALESCE(e.tipoPension.nombre, 'Sin régimen'), COUNT(e) FROM Empleado e GROUP BY e.tipoPension.nombre")
    List<Object[]> countEmpleadosPorPension();
}
