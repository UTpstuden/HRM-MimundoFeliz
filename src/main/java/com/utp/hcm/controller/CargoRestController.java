package com.utp.hcm.controller;

import com.utp.hcm.model.Cargo;
import com.utp.hcm.service.CargoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*") // Allow CORS for development
@RestController
@RequestMapping("/api/cargos")
public class CargoRestController {

    @Autowired
    private CargoService cargoService;

    @GetMapping
    public List<Cargo> getAllCargos() {
        return cargoService.findAll();
    }

    @GetMapping("/activos")
    public List<Cargo> getCargosActivos() {
        return cargoService.findActivos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cargo> getCargoById(@PathVariable int id) {
        try {
            return cargoService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createCargo(@RequestBody Cargo cargo) {
        try {
            // Forzar estado activo al crear
            cargo.setEstado(true);
            Cargo savedCargo = cargoService.save(cargo);
            return ResponseEntity.ok(savedCargo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al crear cargo: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCargo(@PathVariable int id, @RequestBody Cargo cargo) {
        try {
            if (!cargoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            cargo.setId(id);
            Cargo updatedCargo = cargoService.save(cargo);
            return ResponseEntity.ok(updatedCargo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar cargo: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(@PathVariable int id, @RequestParam boolean estado) {
        try {
            Cargo cargoActualizado = cargoService.cambiarEstado(id, estado);
            if (cargoActualizado != null) {
                return ResponseEntity.ok(cargoActualizado);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al cambiar estado: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCargo(@PathVariable int id) {
        try {
            if (!cargoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            cargoService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error al eliminar cargo: " + e.getMessage());
        }
    }
}