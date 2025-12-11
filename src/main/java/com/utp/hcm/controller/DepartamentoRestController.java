package com.utp.hcm.controller;

import com.utp.hcm.model.Departamento;
import com.utp.hcm.service.DepartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*") // Allow CORS for development
@RestController
@RequestMapping("/api/departamentos")
public class DepartamentoRestController {

    @Autowired
    private DepartamentoService departamentoService;

    @GetMapping
    public List<Departamento> getAllDepartamentos() {
        return departamentoService.findAll();
    }

    @GetMapping("/activos")
    public List<Departamento> getDepartamentosActivos() {
        return departamentoService.findActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departamento> getDepartamentoById(@PathVariable int id) {
        try {
            return departamentoService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createDepartamento(@RequestBody Departamento departamento) {
        try {
            // Forzar estado activo al crear
            departamento.setEstado(true);
            Departamento savedDepto = departamentoService.save(departamento);
            return ResponseEntity.ok(savedDepto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear departamento: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateDepartamento(@PathVariable int id, @RequestBody Departamento departamento) {
        try {
            if (!departamentoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            departamento.setId(id);
            Departamento updatedDepto = departamentoService.save(departamento);
            return ResponseEntity.ok(updatedDepto);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar departamento: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable int id, @RequestParam boolean estado) {
        try {
            Departamento departamentoActualizado = departamentoService.cambiarEstado(id, estado);
            if (departamentoActualizado != null) {
                return ResponseEntity.ok(departamentoActualizado);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al cambiar estado: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDepartamento(@PathVariable int id) {
        try {
            if (!departamentoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            departamentoService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al eliminar departamento: " + e.getMessage());
        }
    }
}