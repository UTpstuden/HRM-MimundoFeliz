package com.utp.hcm.controller;

import com.utp.hcm.dto.GestionIncidenciaRequest;
import com.utp.hcm.model.Incidencia;
import com.utp.hcm.service.IncidenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * API REST para que el Administrador gestione las incidencias.
 * Protegida por SecurityConfig bajo "/api/admin/**"
 */
@RestController
@RequestMapping("/api/admin/incidencias")
public class IncidenciaRestController {

    @Autowired
    private IncidenciaService incidenciaService;

    /**
     * Endpoint para listar todas las incidencias (para el admin).
     */
    @GetMapping
    public ResponseEntity<?> getAllIncidencias() {
        try {
            return ResponseEntity.ok(incidenciaService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar incidencias: " + e.getMessage());
        }
    }

    /**
     * Endpoint para listar solo las incidencias PENDIENTES (para notificaciones).
     */
    @GetMapping("/pendientes")
    public ResponseEntity<?> getIncidenciasPendientes() {
        try {
            return ResponseEntity.ok(incidenciaService.findPendientes());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar incidencias pendientes: " + e.getMessage());
        }
    }

    /**
     * Endpoint para que un admin actualice el estado de una incidencia.
     * (Ej. Justificar o Rechazar una tardanza).
     *
     * @param id      El ID de la incidencia a gestionar.
     * @param request El DTO con el nuevo estado y la observación.
     * @return ResponseEntity con la incidencia actualizada o un error.
     */
    @PutMapping("/{id}/gestionar")
    public ResponseEntity<?> gestionarIncidencia(@PathVariable Long id, @RequestBody GestionIncidenciaRequest request) {
        try {
            if (request.getEstado() == null || request.getEstado().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("El campo 'estado' es obligatorio.");
            }

            Incidencia incidenciaActualizada = incidenciaService.gestionarIncidencia(
                    id,
                    request.getEstado(),
                    request.getObservacion());

            return ResponseEntity.ok(incidenciaActualizada);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }
}