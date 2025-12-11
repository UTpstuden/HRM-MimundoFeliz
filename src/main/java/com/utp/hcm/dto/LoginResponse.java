package com.utp.hcm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.utp.hcm.model.Empleado;

/**
 * DTO para enviar la respuesta del login con el token JWT
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo = "Bearer";
    private String correoInstitucional;
    private String rol;
    private String mensaje;

    private Integer idUsuario;
    private Empleado empleado;

    public LoginResponse(String token, String correoInstitucional, String rol, Empleado empleado, Integer idUsuario) {
        this.token = token;
        this.correoInstitucional = correoInstitucional;
        this.rol = rol;
        this.empleado = empleado;
        this.idUsuario = idUsuario;
        this.mensaje = "Login exitoso";
    }
}
