package com.utp.hcm.controller;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Usuario;
import com.utp.hcm.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // <-- AÑADIR ESTA IMPORTACIÓN
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public List<Usuario> getAllUsuarios() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getUsuarioById(@PathVariable int id) {
        return usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint especial para obtener empleados sin cuenta.
     */
    @GetMapping("/empleados-sin-cuenta")
    public List<Empleado> getEmpleadosSinUsuario() {
        return usuarioService.findEmpleadosSinUsuario();
    }

    @PostMapping
    public ResponseEntity<?> createUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.save(usuario);
            return ResponseEntity.ok(nuevoUsuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al crear usuario: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUsuario(@PathVariable int id, @RequestBody Usuario usuario) {
        try {
            if (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0) {
                usuario.setIdUsuario(id);
            }
            if (id != usuario.getIdUsuario()) {
                return ResponseEntity.badRequest().body("ID del path y del body no coinciden.");
            }

            Usuario usuarioActualizado = usuarioService.save(usuario);
            return ResponseEntity.ok(usuarioActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar usuario: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable int id, @RequestBody java.util.Map<String, String> payload) {
        try {
            String newPassword = payload.get("newPassword");
            if (newPassword == null || newPassword.isBlank()) {
                return ResponseEntity.badRequest().body("La nueva contraseña es obligatoria");
            }
            usuarioService.updatePassword(id, newPassword);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al actualizar contraseña: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUsuario(@PathVariable int id) {
        try {
            usuarioService.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            // --- ESTA ES LA LÍNEA CORREGIDA ---
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            // Captura de violaciones de integridad (ej. si el usuario no se puede borrar
            // por otras dependencias)
            return ResponseEntity.status(500)
                    .body("Error al eliminar el usuario. Verifique que no tenga registros asociados.");
        }
    }
}