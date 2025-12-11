package com.utp.hcm.service;

import com.utp.hcm.dto.*;
import com.utp.hcm.model.Empleado;
import com.utp.hcm.repository.AsistenciaRepository;
import com.utp.hcm.repository.EmpleadoRepository;
import com.utp.hcm.repository.IncidenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    public DashboardMetrics obtenerMetricas() {
        long totalEmpleados = empleadoRepository.count();
        LocalDate hoy = LocalDate.now();
        long empleadosConAsistencia = asistenciaRepository.countByFecha(hoy);
        long empleadosSinAsistencia = Math.max(totalEmpleados - empleadosConAsistencia, 0);
        double porcentajeAsistencia = totalEmpleados == 0
                ? 0
                : (double) empleadosConAsistencia * 100 / totalEmpleados;

        long incidenciasPendientes = incidenciaRepository.countByEstado("PENDIENTE");
        long incidenciasJustificadas = incidenciaRepository.countByEstado("JUSTIFICADA");
        long incidenciasNoJustificadas = incidenciaRepository.countByEstado("NO JUSTIFICADO");
        long totalIncidencias = incidenciaRepository.count();

        return new DashboardMetrics(
                totalEmpleados,
                empleadosConAsistencia,
                empleadosSinAsistencia,
                porcentajeAsistencia,
                incidenciasPendientes,
                incidenciasJustificadas,
                incidenciasNoJustificadas,
                totalIncidencias
        );
    }

    public List<CargoResumen> obtenerComparativaPorCargo() {
        List<Object[]> datos = empleadoRepository.countEmpleadosPorCargo();
        long total = datos.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        List<CargoResumen> resumen = new ArrayList<>();
        for (Object[] row : datos) {
            String nombre = row[0] != null ? row[0].toString() : "Sin cargo";
            long cantidad = ((Number) row[1]).longValue();
            double porcentaje = total == 0 ? 0 : (double) cantidad * 100 / total;
            resumen.add(new CargoResumen(nombre, cantidad, porcentaje));
        }
        return resumen;
    }

    public DashboardBiResponse obtenerBiData() {
        return new DashboardBiResponse(
                mapToChartPoints(obtenerComparativaPorCargo()),
                asistenciaUltimosDias(10),
                incidenciasPorEstado(),
                composicionPorPension(),
                dispersionSueldoTardanza(),
                histogramaSueldos(),
                prediccionAsistencia(7, 3)
        );
    }

    private List<ChartPoint> mapToChartPoints(List<CargoResumen> resumenes) {
        List<ChartPoint> points = new ArrayList<>();
        for (CargoResumen resumen : resumenes) {
            points.add(new ChartPoint(resumen.nombre(), resumen.cantidad()));
        }
        return points;
    }

    private List<TimeSeriesPoint> asistenciaUltimosDias(int dias) {
        List<TimeSeriesPoint> puntos = new ArrayList<>();
        LocalDate hoy = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = dias - 1; i >= 0; i--) {
            LocalDate fecha = hoy.minusDays(i);
            double valor = asistenciaRepository.countByFecha(fecha);
            puntos.add(new TimeSeriesPoint(fecha.format(formatter), valor, false));
        }
        return puntos;
    }

    private List<ChartPoint> incidenciasPorEstado() {
        return List.of(
                new ChartPoint("Pendientes", incidenciaRepository.countByEstado("PENDIENTE")),
                new ChartPoint("Justificadas", incidenciaRepository.countByEstado("JUSTIFICADA")),
                new ChartPoint("No justificadas", incidenciaRepository.countByEstado("NO JUSTIFICADO"))
        );
    }

    private List<ChartPoint> composicionPorPension() {
        List<Object[]> datos = empleadoRepository.countEmpleadosPorPension();
        List<ChartPoint> puntos = new ArrayList<>();
        for (Object[] row : datos) {
            String label = row[0] != null ? row[0].toString() : "Sin régimen";
            long cantidad = ((Number) row[1]).longValue();
            puntos.add(new ChartPoint(label, cantidad));
        }
        return puntos;
    }

    private List<ScatterPoint> dispersionSueldoTardanza() {
        List<ScatterPoint> puntos = new ArrayList<>();
        LocalDate inicio = LocalDate.now().minusDays(30);
        LocalDate fin = LocalDate.now();

        for (Empleado empleado : empleadoRepository.findAll()) {
            double sueldo = empleado.getSueldoBase() != null ? empleado.getSueldoBase() : 0;
            int minutos = incidenciaRepository.findByEmpleadoAndTipoAndFechaBetween(
                            empleado, "TARDANZA", inicio, fin)
                    .stream()
                    .mapToInt(i -> i.getMinutosIncidencia() == null ? 0 : i.getMinutosIncidencia())
                    .sum();
            double horas = minutos / 60.0;
            puntos.add(new ScatterPoint(sueldo, horas, empleado.getNombre()));
        }
        return puntos;
    }

    private List<ChartPoint> histogramaSueldos() {
        Map<String, Integer> buckets = new LinkedHashMap<>();
        buckets.put("<=2500", 0);
        buckets.put("2501-3000", 0);
        buckets.put("3001-3500", 0);
        buckets.put("3501-4000", 0);
        buckets.put(">4000", 0);

        for (Empleado empleado : empleadoRepository.findAll()) {
            double sueldo = empleado.getSueldoBase() == null ? 0 : empleado.getSueldoBase();
            if (sueldo <= 2500) buckets.computeIfPresent("<=2500", (k, v) -> v + 1);
            else if (sueldo <= 3000) buckets.computeIfPresent("2501-3000", (k, v) -> v + 1);
            else if (sueldo <= 3500) buckets.computeIfPresent("3001-3500", (k, v) -> v + 1);
            else if (sueldo <= 4000) buckets.computeIfPresent("3501-4000", (k, v) -> v + 1);
            else buckets.computeIfPresent(">4000", (k, v) -> v + 1);
        }

        List<ChartPoint> puntos = new ArrayList<>();
        buckets.forEach((label, value) -> puntos.add(new ChartPoint(label, value)));
        return puntos;
    }

    private List<TimeSeriesPoint> prediccionAsistencia(int diasHistoricos, int diasFuturos) {
        List<TimeSeriesPoint> historicos = asistenciaUltimosDias(diasHistoricos);
        List<TimeSeriesPoint> resultado = new ArrayList<>(historicos);
        if (historicos.isEmpty()) return resultado;

        double diferenciaPromedio = 0;
        for (int i = 1; i < historicos.size(); i++) {
            diferenciaPromedio += historicos.get(i).value() - historicos.get(i - 1).value();
        }
        diferenciaPromedio = diferenciaPromedio / Math.max(1, historicos.size() - 1);

        double ultimaMedicion = historicos.get(historicos.size() - 1).value();
        LocalDate fechaBase = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (int i = 1; i <= diasFuturos; i++) {
            ultimaMedicion = Math.max(0, ultimaMedicion + diferenciaPromedio);
            resultado.add(new TimeSeriesPoint(
                    fechaBase.plusDays(i).format(formatter),
                    ultimaMedicion,
                    true
            ));
        }

        return resultado;
    }
}
