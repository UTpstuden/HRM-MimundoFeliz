package com.utp.hcm.repository;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Incidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import com.utp.hcm.model.Asistencia;
import java.util.List;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {

    /**
     * Busca incidencias por su estado.
     */
    List<Incidencia> findByEstadoOrderByFechaDesc(String estado);

    /**
     * Busca todas las incidencias ordenadas por fecha.
     */
    List<Incidencia> findAllByOrderByFechaDesc();

    /**
     * Verifica si ya existe una incidencia para un registro de asistencia específico.
     */
    boolean existsByAsistenciaId(Long asistenciaId);

    /**
     * Verifica si ya existe una incidencia de un tipo específico para un empleado en una fecha específica.
     */
    boolean existsByEmpleadoAndFechaAndTipo(Empleado empleado, LocalDate fecha, String tipo);

    /**
     * NUEVO MÉTODO: Elimina todas las incidencias por ID de empleado.
     * Necesario para el borrado en cascada.
     */
    @Modifying
    @Transactional
    void deleteAllByEmpleadoId(int empleadoId);

    /**
     * Cuenta las incidencias de un empleado por TIPO, ESTADO y rango de fechas.
     * Lo usaremos para contar las "FALTAS JUSTIFICADAS" del mes.
     */
    long countByEmpleadoAndTipoAndEstadoAndFechaBetween(
            Empleado empleado,
            String tipo,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
    /**
     * NUEVO MÉTODO (Corrección de Bug):
     * Verifica si ya existe una incidencia de un TIPO específico para un registro de ASISTENCIA específico.
     */
    boolean existsByAsistenciaAndTipo(Asistencia asistencia, String tipo);

    /**
     * NUEVO MÉTODO (para el widget de inicio-empleado):
     * Cuenta las incidencias de un empleado por su estado.
     */
    long countByEmpleadoAndEstado(Empleado empleado, String estado);
    List<Incidencia> findByEmpleadoAndTipoAndEstadoAndFechaBetween(
            Empleado empleado,
            String tipo,
            String estado,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );

    long countByEstado(String estado);

    List<Incidencia> findByEmpleadoAndTipoAndFechaBetween(
            Empleado empleado,
            String tipo,
            LocalDate fechaInicio,
            LocalDate fechaFin
    );
}
