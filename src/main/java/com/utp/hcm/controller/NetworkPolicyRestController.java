package com.utp.hcm.controller;

import com.utp.hcm.model.NetworkPolicy;
import com.utp.hcm.service.NetworkPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/network-policies")
public class NetworkPolicyRestController {

    @Autowired
    private NetworkPolicyService networkPolicyService;

    @GetMapping
    public List<NetworkPolicy> getAllPolicies() {
        return networkPolicyService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> createPolicy(@RequestBody Map<String, String> payload) {
        try {
            String nombre = payload.get("nombre");
            String prefijo = payload.get("prefijo");
            String descripcion = payload.get("descripcion");
            
            networkPolicyService.create(nombre, prefijo, descripcion);
            return ResponseEntity.ok().body(Map.of("message", "Política creada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> updateEstado(@PathVariable Long id, @RequestBody Map<String, Boolean> payload) {
        try {
            Boolean activo = payload.get("activo");
            networkPolicyService.updateEstado(id, activo);
            return ResponseEntity.ok().body(Map.of("message", "Estado actualizado"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePolicy(@PathVariable Long id) {
        try {
            networkPolicyService.delete(id);
            return ResponseEntity.ok().body(Map.of("message", "Política eliminada"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
