package com.utp.hcm.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.utp.hcm.dto.LoginRequest;
import com.utp.hcm.dto.LoginResponse;
import com.utp.hcm.security.JwtUtil;
import com.utp.hcm.service.CustomUserDetailsService;

import jakarta.validation.Valid;

/**
 * Controlador REST para manejar la autenticación con JWT
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*") // Permitir CORS para desarrollo
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.utp.hcm.repository.UsuarioRepository usuarioRepository;

    /**
     * Endpoint para iniciar sesión y obtener un token JWT
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Autenticar al usuario
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getCorreoInstitucional(),
                            loginRequest.getPassword()));

            // Cargar los detalles del usuario
            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    loginRequest.getCorreoInstitucional());

            // Generar el token JWT
            String token = jwtUtil.generateToken(userDetails);

            // Extraer el rol del usuario
            String rol = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(","));

            // Obtener usuario completo para datos adicionales
            com.utp.hcm.model.Usuario usuario = usuarioRepository
                    .findByCorreoInstitucional(loginRequest.getCorreoInstitucional())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Crear y retornar la respuesta
            LoginResponse response = new LoginResponse(
                    token,
                    userDetails.getUsername(),
                    rol,
                    usuario.getEmpleado(),
                    usuario.getIdUsuario());

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            // Credenciales inválidas
            System.err.println("DEBUG: BadCredentials for " + loginRequest.getCorreoInstitucional());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("mensaje", "Correo o contraseña incorrectos");
            errorResponse.put("error", "CREDENCIALES_INVALIDAS");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            // Error general
            Map<String, String> error = new HashMap<>();
            error.put("mensaje", "Error al procesar la solicitud");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Endpoint para validar si un token es válido
     * GET /api/auth/validate
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtUtil.extractUsername(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.validateToken(token, userDetails)) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("valido", true);
                    response.put("usuario", username);
                    response.put("roles", jwtUtil.extractRoles(token));
                    return ResponseEntity.ok(response);
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("valido", false);
            response.put("mensaje", "Token inválido o expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("valido", false);
            response.put("mensaje", "Error al validar el token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
