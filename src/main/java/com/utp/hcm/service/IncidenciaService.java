package com.utp.hcm.service;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Incidencia;
import com.utp.hcm.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class IncidenciaService {

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    /**
     * Obtiene todas las incidencias, ordenadas por fecha.
     */
    @Transactional(readOnly = true)
    public List<Incidencia> findAll() {
        return incidenciaRepository.findAllByOrderByFechaDesc();
    }

    /**
     * Obtiene solo las incidencias que están PENDIENTES.
     */
    @Transactional(readOnly = true)
    public List<Incidencia> findPendientes() {
        return incidenciaRepository.findByEstadoOrderByFechaDesc("PENDIENTE");
    }

    /**
     * Lógica principal para que un admin gestione una incidencia.
     * Cambia el estado (a JUSTIFICADA o NO JUSTIFICADO) y añade una observación.
     */
    @Transactional
    public Incidencia gestionarIncidencia(Long id, String nuevoEstado, String observacion) {

        // ===== CAMBIO DE LÓGICA =====
        if (!"JUSTIFICADA".equals(nuevoEstado) && !"NO JUSTIFICADO".equals(nuevoEstado)) {
            throw new IllegalArgumentException("El estado debe ser 'JUSTIFICADA' o 'NO JUSTIFICADO'.");
        }
        // ===== FIN DE CAMBIO =====

        // Buscar la incidencia
        Incidencia incidencia = incidenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No se encontró la incidencia con ID: " + id));

        // Actualizar los campos
        incidencia.setEstado(nuevoEstado);
        incidencia.setObservacionAdmin(observacion);

        // Guardar y devolver la incidencia actualizada
        return incidenciaRepository.save(incidencia);
    }

    /**
     * Encuentra una incidencia por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<Incidencia> findById(Long id) {
        return incidenciaRepository.findById(id);
    }

    /**
     * NUEVO MÉTODO (para el widget de inicio-empleado):
     * Cuenta el total de incidencias PENDIENTES para un empleado específico.
     */
    @Transactional(readOnly = true)
    public long contarIncidenciasPendientesPorEmpleado(Empleado empleado) {
        return incidenciaRepository.countByEmpleadoAndEstado(empleado, "PENDIENTE");
    }
}