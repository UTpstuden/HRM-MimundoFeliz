package com.utp.hcm.controller;

import com.utp.hcm.dto.DashboardBiResponse;
import com.utp.hcm.model.Asistencia;
import com.utp.hcm.model.Incidencia;
import com.utp.hcm.service.AsistenciaService;
import com.utp.hcm.service.IncidenciaService;
import com.utp.hcm.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*") // Allow CORS for development
public class DashboardRestController {

        @Autowired
        private AsistenciaService asistenciaService;

        @Autowired
        private IncidenciaService incidenciaService;
        @Autowired
        private DashboardService dashboardService;

        @Autowired
        private com.utp.hcm.repository.EmpleadoRepository empleadoRepository;
        @Autowired
        private com.utp.hcm.repository.CargoRepository cargoRepository;
        @Autowired
        private com.utp.hcm.repository.DepartamentoRepository departamentoRepository;
        @Autowired
        private com.utp.hcm.repository.HorarioTrabajoRepository horarioTrabajoRepository;

        @GetMapping("/bi")
        public DashboardBiResponse obtenerDatosBi() {
                return dashboardService.obtenerBiData();
        }

        // --- GENERIC STATS ENDPOINTS ---

        @GetMapping("/stats/empleados")
        public ResponseEntity<Long> getTotalEmpleados() {
                return ResponseEntity.ok(empleadoRepository.count());
        }

        @GetMapping("/stats/cargos")
        public ResponseEntity<Long> getTotalCargos() {
                return ResponseEntity.ok(cargoRepository.count());
        }

        @GetMapping("/stats/departamentos")
        public ResponseEntity<Long> getTotalDepartamentos() {
                return ResponseEntity.ok(departamentoRepository.count());
        }

        @GetMapping("/stats/horarios/count")
        public ResponseEntity<Long> getTotalHorarios() {
                return ResponseEntity.ok(horarioTrabajoRepository.count());
        }

        @GetMapping("/stats")
        public ResponseEntity<Map<String, Long>> getOverallStats() {
                Map<String, Long> stats = new HashMap<>();
                stats.put("totalEmpleados", empleadoRepository.count());
                stats.put("totalCargos", cargoRepository.count());
                stats.put("totalDepartamentos", departamentoRepository.count());
                stats.put("totalHorarios", horarioTrabajoRepository.count());
                return ResponseEntity.ok(stats);
        }

        @GetMapping("/stats/asistencia")
        public ResponseEntity<Map<String, Object>> getAsistenciaStats() {
                // 1. Get today's attendance list
                List<Asistencia> asistenciasHoy = asistenciaService.getAsistenciasHoy();

                // 2. Calculate statistics
                long presentesHoy = asistenciasHoy.size();

                long tardanzasHoy = asistenciasHoy.stream()
                                .filter(a -> "TARDANZA".equals(a.getEstadoEntrada()))
                                .count();

                long salidasAnticipadasHoy = asistenciasHoy.stream()
                                .filter(a -> "SALIDA ANTICIPADA".equals(a.getEstadoSalida()))
                                .count();

                // Note: "Absent" can only be calculated with the nightly Scheduled Task.
                // For now, we leave it at 0.
                long ausentesHoy = 0;

                Map<String, Object> response = new HashMap<>();
                response.put("presentes", presentesHoy);
                response.put("tardanzas", tardanzasHoy);
                response.put("salidasAnticipadas", salidasAnticipadasHoy);
                response.put("ausentes", ausentesHoy);
                response.put("asistencias", asistenciasHoy);

                return ResponseEntity.ok(response);
        }

        @GetMapping("/stats/incidencias")
        public ResponseEntity<Map<String, Object>> getIncidenciaStats() {
                List<Incidencia> incidencias = incidenciaService.findAll();

                long total = incidencias.size();

                long pendientes = incidencias.stream()
                                .filter(i -> "PENDIENTE".equals(i.getEstado()))
                                .count();

                long justificadas = incidencias.stream()
                                .filter(i -> "JUSTIFICADA".equals(i.getEstado()))
                                .count();

                long injustificadas = incidencias.stream()
                                .filter(i -> "NO JUSTIFICADO".equals(i.getEstado()))
                                .count();

                Map<String, Object> response = new HashMap<>();
                response.put("total", total);
                response.put("pendientes", pendientes);
                response.put("justificadas", justificadas);
                response.put("injustificadas", injustificadas);
                response.put("incidencias", incidencias);

                return ResponseEntity.ok(response);
        }
}
