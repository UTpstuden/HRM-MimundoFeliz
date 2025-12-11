package com.utp.hcm.controller;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.hcm.service.RecuperarService;

@RestController
@RequestMapping("/api/recuperar")
@CrossOrigin(origins = "*")
public class RecuperarRestController {

    @Autowired
    private RecuperarService recuperarService;

    @PostMapping
    public ResponseEntity<Map<String, String>> recuperarPassword(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        boolean exito = recuperarService.recuperarPassword(email);

        if (exito) {
            return ResponseEntity.ok(Collections.singletonMap("mensaje",
                    "Se ha enviado una nueva contraseña a tu correo institucional."));
        } else {
            // In a production environment, you might want to return the same message to
            // prevent enumeration,
            // but for this internal tool, specificity helps.
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "El correo ingresado no está registrado en el sistema."));
        }
    }
}
