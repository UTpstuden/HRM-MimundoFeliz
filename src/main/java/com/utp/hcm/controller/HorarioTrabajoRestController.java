package com.utp.hcm.controller;

import com.utp.hcm.model.HorarioTrabajo;
import com.utp.hcm.service.HorarioTrabajoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*") // Allow CORS for development
@RestController
@RequestMapping("/api/horarios")
public class HorarioTrabajoRestController {

    @Autowired
    private HorarioTrabajoService horarioTrabajoService;

    @GetMapping
    public List<HorarioTrabajo> getAllHorarios() {
        return horarioTrabajoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHorarioById(@PathVariable int id) {
        try {
            return horarioTrabajoService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al cargar horario: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createHorario(@RequestBody HorarioTrabajo horarioTrabajo) {
        try {
            HorarioTrabajo savedHorario = horarioTrabajoService.save(horarioTrabajo);
            return ResponseEntity.ok(savedHorario);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al crear horario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHorario(@PathVariable int id, @RequestBody HorarioTrabajo horarioTrabajo) {
        try {
            if (!horarioTrabajoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            horarioTrabajo.setId(id);
            HorarioTrabajo updatedHorario = horarioTrabajoService.save(horarioTrabajo);
            return ResponseEntity.ok(updatedHorario);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar horario: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHorario(@PathVariable int id) {
        try {
            if (!horarioTrabajoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            horarioTrabajoService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al eliminar horario: " + e.getMessage());
        }
    }
}