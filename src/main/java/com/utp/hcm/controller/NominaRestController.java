package com.utp.hcm.controller;

import com.utp.hcm.model.Nomina;
import com.utp.hcm.service.NominaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@RestController
@RequestMapping("/api/nominas")
public class NominaRestController {

    @Autowired
    private NominaService nominaService;

    @GetMapping("/empleado/{empleadoId}")
    public ResponseEntity<List<Nomina>> getNominasByEmpleado(@PathVariable Integer empleadoId) {
        List<Nomina> nominas = nominaService.findByEmpleadoId(empleadoId);
        List<Nomina> actualizadas = nominas.stream()
                .map(n -> nominaService.actualizarNomina(n.getId()))
                .toList();
        return ResponseEntity.ok(actualizadas);
    }

    @GetMapping
    public ResponseEntity<List<Nomina>> getAllNominas() {
        return ResponseEntity.ok(nominaService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nomina> getNominaById(@PathVariable Long id) {
        return nominaService.findById(id)
                .map(n -> ResponseEntity.ok(nominaService.actualizarNomina(n.getId())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/generar-todas/{empleadoId}")
    public ResponseEntity<?> generarTodasNominasEmpleado(@PathVariable Integer empleadoId) {
        try {
            List<Nomina> nominasGeneradas = nominaService.generarTodasNominasEmpleado(empleadoId);
            return ResponseEntity.ok(nominasGeneradas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/actualizar")
    public ResponseEntity<?> actualizarNomina(@PathVariable Long id) {
        try {
            Nomina nominaActualizada = nominaService.actualizarNomina(id);
            return ResponseEntity.ok(nominaActualizada);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar nómina: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNomina(@PathVariable Long id) {
        try {
            nominaService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar nómina: " + e.getMessage());
        }
    }
}
