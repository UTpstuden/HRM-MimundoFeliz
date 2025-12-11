package com.utp.hcm.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.hcm.service.RecuperarService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class PasswordRecoveryRestController {

    @Autowired
    private RecuperarService recuperarService;

    @PostMapping("/recuperar")
    public ResponseEntity<?> recuperarPassword(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "El email es requerido"));
        }

        boolean exito = recuperarService.recuperarPassword(email);

        if (exito) {
            return ResponseEntity.ok(Collections.singletonMap("mensaje",
                    "Se ha enviado una nueva contraseña a tu correo institucional."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "El correo ingresado no está registrado en el sistema."));
        }
    }
}
