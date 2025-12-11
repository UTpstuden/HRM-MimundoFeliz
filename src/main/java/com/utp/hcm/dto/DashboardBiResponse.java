package com.utp.hcm.dto;

import java.util.List;

public record DashboardBiResponse(
        List<ChartPoint> empleadosPorCargo,
        List<TimeSeriesPoint> asistenciaUltimosDias,
        List<ChartPoint> incidenciasPorEstado,
        List<ChartPoint> composicionPension,
        List<ScatterPoint> dispersionSueldoTardanza,
        List<ChartPoint> histogramaSueldos,
        List<TimeSeriesPoint> prediccionAsistencia
) {}
