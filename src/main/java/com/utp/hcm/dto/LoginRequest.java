package com.utp.hcm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir las credenciales de login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    
    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "Debe ser un correo válido")
    private String correoInstitucional;
    
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
