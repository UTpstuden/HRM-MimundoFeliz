package com.utp.hcm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para recibir la solicitud de gestión de una incidencia.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GestionIncidenciaRequest {

    /**
     * El nuevo estado (ej. "JUSTIFICADA" o "RECHAZADA").
     */
    private String estado;

    /**
     * La observación o nota del administrador.
     */
    private String observacion;
}