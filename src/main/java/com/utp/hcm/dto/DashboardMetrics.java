package com.utp.hcm.dto;

public record DashboardMetrics(
        long totalEmpleados,
        long empleadosConAsistenciaHoy,
        long empleadosSinAsistenciaHoy,
        double porcentajeAsistencia,
        long incidenciasPendientes,
        long incidenciasJustificadas,
        long incidenciasNoJustificadas,
        long totalIncidencias
) {}
