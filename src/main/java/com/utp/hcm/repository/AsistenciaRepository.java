package com.utp.hcm.repository;

import com.utp.hcm.model.Asistencia;
import com.utp.hcm.model.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AsistenciaRepository extends JpaRepository<Asistencia, Long> {

    /**
     * Busca un registro de asistencia específico para un empleado en una fecha concreta.
     */
    Optional<Asistencia> findByEmpleadoAndFecha(Empleado empleado, LocalDate fecha);

    /**
     * Busca todos los registros de asistencia de una fecha específica.
     */
    List<Asistencia> findByFecha(LocalDate fecha);

    /**
     * Busca todos los registros de un empleado (para su historial).
     */
    List<Asistencia> findByEmpleadoOrderByFechaDesc(Empleado empleado);

    /**
     * Elimina todos los registros de asistencia por ID de empleado.
     */
    @Modifying
    @Transactional
    void deleteAllByEmpleadoId(int empleadoId);

    /**
     * MÉTODO CORREGIDO (Soluciona Error 1):
     * Busca todas las asistencias de un empleado en un rango de fechas.
     * Reemplaza todos los 'count...Between' anteriores.
     * Usaremos esto para filtrar manualmente en el AsistenciaService.
     */
    List<Asistencia> findByEmpleadoAndFechaBetween(Empleado empleado, LocalDate fechaInicio, LocalDate fechaFin);

    long countByFecha(LocalDate fecha);

    List<Asistencia> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}
