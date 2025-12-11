package com.utp.hcm.service;

import com.utp.hcm.model.Asistencia;
import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Incidencia;
import com.utp.hcm.repository.AsistenciaRepository;
import com.utp.hcm.repository.EmpleadoRepository;
import com.utp.hcm.repository.IncidenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class TareaProgramadaService {

    private static final Logger log = LoggerFactory.getLogger(TareaProgramadaService.class);

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    // Importamos el AsistenciaService para re-usar su lógica
    @Autowired
    private AsistenciaService asistenciaService;

    /**
     * Tarea programada para registrar FALTAS (Ausencias)a.
     * Se ejecuta todos los días a las 23:50 (11:50 PM).
     */
    @Scheduled(cron = "0 50 23 * * ?")
    @Transactional
    public void registrarFaltas() {
        LocalDate hoy = LocalDate.now();
        DayOfWeek diaDeHoy = hoy.getDayOfWeek();
        log.info("--- Iniciando Tarea Programada: Registrar Faltas para el {} ---", hoy);

        List<Empleado> empleados = empleadoRepository.findAll();
        int faltasRegistradas = 0;

        for (Empleado empleado : empleados) {
            // 1. Verificar si el empleado tenía que trabajar hoy
            boolean debeAsistirHoy = false;
            if (empleado.getHorarioTrabajo() != null && empleado.getHorarioTrabajo().getDetalles() != null) {
                debeAsistirHoy = empleado.getHorarioTrabajo().getDetalles().stream()
                        .anyMatch(detalle -> detalle.getDiaDeSemana().equals(diaDeHoy));
            }

            if (debeAsistirHoy) {
                // 2. Si debía asistir, verificar si marcó asistencia
                Optional<Asistencia> asistenciaHoy = asistenciaRepository.findByEmpleadoAndFecha(empleado, hoy);

                if (asistenciaHoy.isEmpty()) {
                    // 3. ¡FALTA DETECTADA!

                    // 4. Verificar que no hayamos creado ya una incidencia de FALTA
                    boolean yaTieneFalta = incidenciaRepository.existsByEmpleadoAndFechaAndTipo(empleado, hoy, "FALTA");

                    if (!yaTieneFalta) {
                        // 5. Crear la incidencia de tipo "FALTA"
                        log.info("FALTA detectada para: {} {}", empleado.getNombre(), empleado.getApellido());

                        // Re-utilizamos el método de AsistenciaService
                        asistenciaService.crearIncidencia(empleado, hoy, "FALTA");
                        faltasRegistradas++;
                    }
                }
            }
        }
        log.info("--- Tarea Programada Finalizada: {} Faltas registradas ---", faltasRegistradas);
    }
}