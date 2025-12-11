package com.utp.hcm.controller;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.TipoContrato;
import com.utp.hcm.model.TipoPension;
import com.utp.hcm.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException; // <-- 1. IMPORTA ESTO
import org.springframework.http.HttpStatus; // <-- 2. IMPORTA ESTO
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "*") // Allow CORS for development
@RestController
@RequestMapping("/api/empleados")
public class EmpleadoRestController {

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    private DepartamentoService departamentoService;

    @Autowired
    private HorarioTrabajoService horarioTrabajoService;

    @Autowired
    private TipoContratoService tipoContratoService;

    @Autowired
    private TipoPensionService tipoPensionService;

    @GetMapping
    public List<Empleado> getAllEmpleados() {
        return empleadoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEmpleadoById(@PathVariable int id) {
        try {
            return empleadoService.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al cargar empleado: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createEmpleado(@RequestBody Empleado empleado) {
        try {
            // Validar que las entidades relacionadas existan
            if (!cargoService.findById(empleado.getCargo().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Cargo no encontrado");
            }
            if (!departamentoService.findById(empleado.getDepartamento().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Departamento no encontrado");
            }
            if (!horarioTrabajoService.findById(empleado.getHorarioTrabajo().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Horario no encontrado");
            }
            if (!tipoContratoService.findById(empleado.getTipoContrato().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Tipo de contrato no encontrado");
            }
            if (empleado.getTipoPension() != null &&
                    !tipoPensionService.findById(empleado.getTipoPension().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Tipo de pensión no encontrado");
            }

            Empleado savedEmpleado = empleadoService.save(empleado);
            return ResponseEntity.ok(savedEmpleado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al crear empleado: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEmpleado(@PathVariable int id, @RequestBody Empleado empleado) {
        try {
            if (!empleadoService.findById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            // Validar que las entidades relacionadas existan
            if (!cargoService.findById(empleado.getCargo().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Cargo no encontrado");
            }
            if (!departamentoService.findById(empleado.getDepartamento().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Departamento no encontrado");
            }
            if (!horarioTrabajoService.findById(empleado.getHorarioTrabajo().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Horario no encontrado");
            }
            if (!tipoContratoService.findById(empleado.getTipoContrato().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Tipo de contrato no encontrado");
            }
            if (empleado.getTipoPension() != null &&
                    !tipoPensionService.findById(empleado.getTipoPension().getId()).isPresent()) {
                return ResponseEntity.badRequest().body("Tipo de pensión no encontrado");
            }

            empleado.setId(id);
            Empleado updatedEmpleado = empleadoService.save(empleado);
            return ResponseEntity.ok(updatedEmpleado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body("Error al actualizar empleado: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> uploadFoto(@PathVariable int id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            empleadoService.updateFoto(id, file);
            return ResponseEntity.ok().body(java.util.Map.of("message", "Foto actualizada correctamente"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al subir foto: " + e.getMessage());
        }
    }

    // --- 3. REEMPLAZA TU MÉTODO deleteEmpleado POR ESTE ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEmpleado(@PathVariable int id) {

        // Verificamos si existe antes de intentar borrar
        if (!empleadoService.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Empleado no encontrado con id: " + id);
        }

        try {
            // Esto llamará al método modificado en EmpleadoService
            // que borra nóminas primero.
            empleadoService.deleteById(id);

            // Si todo va bien, devolvemos 200 OK con un mensaje
            return ResponseEntity.ok()
                    .body(java.util.Map.of("message", "Empleado con id " + id + " eliminado correctamente"));

        } catch (DataIntegrityViolationException e) {
            // Esto captura el error de Foreign Key si OTRA tabla
            // (que no sea 'nomina') impide el borrado.
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT) // Error 409
                    .body("No se puede eliminar el empleado. Aún tiene otros datos relacionados: " + e.getMessage());
        } catch (Exception e) {
            // Captura para cualquier otro error inesperado
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar empleado: " + e.getMessage());
        }
    }

    @GetMapping("/tipos-contrato")
    public List<TipoContrato> getTiposContrato() {
        return tipoContratoService.findAll();
    }

    @GetMapping("/tipos-pension")
    public List<TipoPension> getTiposPension() {
        return tipoPensionService.findAll();
    }
}