package com.utp.hcm.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.utp.hcm.model.*;
import com.utp.hcm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NominaService {

    @Autowired
    private NominaRepository nominaRepository;

    @Autowired
    private EmpleadoService empleadoService;

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    private static final String LOGO_URL = "https://scontent.flim26-1.fna.fbcdn.net/v/t39.30808-1/470699705_586228447487821_631917957837173743_n.jpg?stp=dst-jpg_s480x480_tt6&_nc_cat=102&ccb=1-7&_nc_sid=2d3e12&_nc_eui2=AeFJ2jd9xrfGGUe4bpviQ_49j6KxAqptB4yPorECqm0HjFhykOEjGOpTqMlVeqY-XhW5upZ4WEZCqsCUAHW6ZWTx&_nc_ohc=bPpGB73iL2YQ7kNvwFD5V6q&_nc_oc=Adnd3utjg1aA3-uamvC8YnxY0htYItF-_ctyxA8XAJOILeLLQSy3yeb-CooLTOPWhAg&_nc_zt=24&_nc_ht=scontent.flim26-1.fna&_nc_gid=Ec8_8a80xX0Ayae73tgF-g&oh=00_AfjUeGyHmm2HOeSXkZjNn6rXIWTrKT4k4Ju3QEm0uRXGuQ&oe=6922F1E6";
    private static final BaseColor COLOR_AZUL = new BaseColor(5, 53, 110);
    private static final BaseColor COLOR_DORADO = new BaseColor(244, 180, 0);
    private static final BaseColor COLOR_GRIS = new BaseColor(245, 247, 250);

    public List<Nomina> findByEmpleadoId(Integer empleadoId) {
        return nominaRepository.findByEmpleadoId(empleadoId);
    }

    public List<Nomina> findAll() {
        return nominaRepository.findAllWithDetails();
    }

    public Optional<Nomina> findById(Long id) {
        return nominaRepository.findByIdWithDetails(id);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!nominaRepository.existsById(id)) {
            throw new IllegalArgumentException("Nómina no encontrada para eliminar.");
        }
        nominaRepository.deleteById(id);
    }

    public Nomina save(Nomina nomina) {
        return nominaRepository.save(nomina);
    }

    // ============================================================
    // GENERAR TODAS LAS NÓMINAS DE UN EMPLEADO
    // ============================================================
    @Transactional
    public List<Nomina> generarTodasNominasEmpleado(Integer empleadoId) {
        Optional<Empleado> empleadoOpt = empleadoService.findById(empleadoId);
        if (empleadoOpt.isEmpty()) {
            throw new IllegalArgumentException("Empleado no encontrado");
        }

        Empleado empleado = empleadoOpt.get();
        List<Nomina> nominasGeneradas = new ArrayList<>();

        LocalDate fechaContratacion = empleado.getFechaContratacion();
        if (fechaContratacion == null) {
            throw new IllegalArgumentException("El empleado no tiene fecha de contratación");
        }

        LocalDate fechaActual = LocalDate.now();
        YearMonth mesInicio = YearMonth.from(fechaContratacion);
        YearMonth mesFin = YearMonth.from(fechaActual);

        if (mesInicio.isAfter(mesFin)) {
            // En lugar de lanzar error, simplemente retornamos lista vacía
            // throw new IllegalArgumentException("La fecha de contratación es futura, no se
            // pueden generar nóminas");
            return new ArrayList<>();
        }

        int nominasCreadas = 0;
        YearMonth mesActual = mesInicio;

        while (!mesActual.isAfter(mesFin)) {
            String mesPeriodo = mesActual.toString();

            // PRIMERO: Verificar si ya existe una nómina para este periodo
            Optional<Nomina> nominaExistente = nominaRepository.findByEmpleadoId(empleado.getId())
                    .stream()
                    .filter(n -> mesPeriodo.equals(n.getMesPeriodo()))
                    .findFirst();

            if (nominaExistente.isPresent()) {
                // Si ya existe, simplemente saltamos este mes
                mesActual = mesActual.plusMonths(1);
                continue;
            }

            // SOLO crear la nómina si no existe
            Nomina nomina = generarNominaParaPeriodo(empleado, mesActual);
            nominasGeneradas.add(nominaRepository.save(nomina));
            nominasCreadas++;
            mesActual = mesActual.plusMonths(1);
        }

        if (nominasCreadas == 0) {
            // Check if it was because all exist or because range was invalid?
            // If loop ran but no nominas created because all exist:
            throw new IllegalStateException("Nóminas ya generadas para este periodo");
        }

        return nominasGeneradas;
    }

    // ============================================================
    // GENERAR NÓMINA PARA UN PERIODO
    // ============================================================
    private Nomina generarNominaParaPeriodo(Empleado empleado, YearMonth mesPeriodo) {
        Nomina nomina = new Nomina();
        nomina.setEmpleado(empleado);
        nomina.setMesPeriodo(mesPeriodo.toString());
        nomina.setFechaGeneracion(mesPeriodo.atEndOfMonth());

        calcularMontosNomina(nomina, empleado, mesPeriodo);

        return nomina;
    }

    private void calcularMontosNomina(Nomina nomina, Empleado empleado, YearMonth mesPeriodo) {
        // 1. SUELDO BASE COMPLETO (contratual)
        Double sueldoBaseCompleto = empleado.getSueldoBase() == null ? 0.0 : empleado.getSueldoBase();

        // 2. SUELDO BASE PROPORCIONAL (si fue contratado en el mes)
        Double sueldoBaseProporcional = calcularSueldoProporcional(empleado, mesPeriodo, sueldoBaseCompleto);

        // 3. DETERMINAR HORAS DIARIAS SEGÚN HORARIO
        double horasDiarias = 8.0; // Default
        if (empleado.getHorarioTrabajo() != null &&
                empleado.getHorarioTrabajo().getDetalles() != null &&
                !empleado.getHorarioTrabajo().getDetalles().isEmpty()) {
            try {
                // Usamos el primer detalle como referencia (asumiendo horario regular)
                HorarioDetalle detalle = empleado.getHorarioTrabajo().getDetalles().get(0);

                java.time.LocalTime entrada = detalle.getHoraEntrada();
                java.time.LocalTime salida = detalle.getHoraSalida();

                if (entrada != null && salida != null) {
                    long minutos = ChronoUnit.MINUTES.between(entrada, salida);
                    horasDiarias = minutos / 60.0;
                    if (horasDiarias <= 0)
                        horasDiarias += 24;
                    if (horasDiarias > 6)
                        horasDiarias -= 1.0; // Refrigerio
                }
            } catch (Exception e) {
                System.err.println("Error calculando horas diarias: " + e.getMessage());
            }
        }

        // 4. VALORES DE REFERENCIA
        double valorDia = sueldoBaseCompleto / 30.0;
        double valorHora = valorDia / horasDiarias;

        // 5. DIAS TRABAJADOS Y FALTAS
        int diasTrabajados = calcularDiasTrabajados(empleado, mesPeriodo);
        int faltasInjustificadas = calcularDiasFalta(empleado, mesPeriodo);
        int minutosTardanza = calcularMinutosTardanzaNoJustificada(empleado, mesPeriodo);
        int minutosSalidaAnticipada = calcularMinutosSalidaAnticipadaNoJustificada(empleado, mesPeriodo);

        // 6. CONVERTIR MINUTOS A HORAS
        double horasTardanza = minutosTardanza / 60.0;
        double horasSalidaAnticipada = minutosSalidaAnticipada / 60.0;
        double horasTotalesIncidencias = horasTardanza + horasSalidaAnticipada;

        // 7. DESCUENTOS por incidencias NO JUSTIFICADAS
        double descuentoFaltas = faltasInjustificadas * valorDia;
        double descuentoTardanza = horasTardanza * valorHora;
        double descuentoSalidaAnticipada = horasSalidaAnticipada * valorHora;
        double descuentoIncidencias = descuentoFaltas + descuentoTardanza + descuentoSalidaAnticipada;

        // 8. VERIFICAR TIPO DE CONTRATO (Locación vs Planilla)
        boolean esLocacion = false;
        if (empleado.getTipoContrato() != null &&
                empleado.getTipoContrato().getNombre() != null &&
                empleado.getTipoContrato().getNombre().toLowerCase().contains("locaci")) {
            esLocacion = true;
        }

        // 9. DESCUENTOS DE PENSION (Solo si no es Locación)
        double descuentoAFP = 0.0;
        double descuentoONP = 0.0;

        if (!esLocacion && empleado.getTipoPension() != null && empleado.getTipoPension().getNombre() != null) {
            String tipo = empleado.getTipoPension().getNombre().trim().toUpperCase();
            if (tipo.contains("AFP")) {
                descuentoAFP = sueldoBaseProporcional * 0.12;
            } else if (tipo.contains("ONP")) {
                descuentoONP = sueldoBaseProporcional * 0.13;
            }
        }

        // 10. ASIGNACION FAMILIAR (Solo si no es Locación)
        double asignacionFamiliar = 0.0;
        if (!esLocacion && Boolean.TRUE.equals(empleado.getTieneHijosMenores())) {
            asignacionFamiliar = calcularAsignacionFamiliarProporcional(empleado, mesPeriodo, 102.50);
        }

        // 11. TOTAL NETO
        double totalNeto = sueldoBaseProporcional + asignacionFamiliar - descuentoAFP - descuentoONP
                - descuentoIncidencias;
        if (totalNeto < 0)
            totalNeto = 0.0;

        // 12. SETEAR VALORES
        nomina.setSueldoBase(sueldoBaseProporcional);
        nomina.setValorDia(valorDia);
        nomina.setValorHora(valorHora);
        nomina.setDiasTrabajados(diasTrabajados);
        nomina.setDiasFalta(faltasInjustificadas);
        nomina.setMinutosTardanza(minutosTardanza);
        nomina.setHorasTardanza(horasTardanza);
        nomina.setHorasIncidencias(horasTotalesIncidencias);
        nomina.setDescuentoTardanza(descuentoTardanza);
        nomina.setDescuentoFaltas(descuentoFaltas);
        nomina.setDescuentoIncidencias(descuentoIncidencias);
        nomina.setDescuentoAFP(descuentoAFP);
        nomina.setDescuentoONP(descuentoONP);
        nomina.setAsignacionFamiliar(asignacionFamiliar);
        nomina.setTotalNeto(totalNeto);
    }

    // ============================================================
    // MÉTODOS DE CÁLCULO
    // ============================================================

    private Double calcularSueldoProporcional(Empleado empleado, YearMonth mesPeriodo, Double sueldoMensual) {
        LocalDate fechaIngreso = empleado.getFechaContratacion();
        LocalDate inicioMes = mesPeriodo.atDay(1);
        LocalDate finMes = mesPeriodo.atEndOfMonth();

        if (fechaIngreso == null) {
            return sueldoMensual;
        }

        if (fechaIngreso.getMonth() == mesPeriodo.getMonth() &&
                fechaIngreso.getYear() == mesPeriodo.getYear()) {

            long diasTrabajados = ChronoUnit.DAYS.between(fechaIngreso, finMes.plusDays(1));
            return (sueldoMensual / 30.0) * diasTrabajados;
        }

        return sueldoMensual;
    }

    private Double calcularAsignacionFamiliarProporcional(Empleado empleado, YearMonth mesPeriodo,
            Double asignacionCompleta) {
        LocalDate fechaIngreso = empleado.getFechaContratacion();
        LocalDate finMes = mesPeriodo.atEndOfMonth();

        if (fechaIngreso == null)
            return asignacionCompleta;

        if (fechaIngreso.getMonth() == mesPeriodo.getMonth() &&
                fechaIngreso.getYear() == mesPeriodo.getYear()) {

            long diasTrabajados = ChronoUnit.DAYS.between(fechaIngreso, finMes.plusDays(1));
            return (asignacionCompleta / 30.0) * diasTrabajados;
        }

        return asignacionCompleta;
    }

    private int calcularMinutosTardanzaNoJustificada(Empleado empleado, YearMonth mesPeriodo) {
        LocalDate inicioMes = mesPeriodo.atDay(1);
        LocalDate finMes = mesPeriodo.atEndOfMonth();
        LocalDate fechaIngreso = empleado.getFechaContratacion();

        LocalDate inicio = (fechaIngreso != null && fechaIngreso.isAfter(inicioMes)) ? fechaIngreso : inicioMes;

        List<Incidencia> tardanzas = incidenciaRepository.findByEmpleadoAndTipoAndEstadoAndFechaBetween(
                empleado, "TARDANZA", "NO JUSTIFICADO", inicio, finMes);

        return tardanzas.stream()
                .mapToInt(i -> i.getMinutosIncidencia() == null ? 0 : i.getMinutosIncidencia())
                .sum();
    }

    private int calcularMinutosSalidaAnticipadaNoJustificada(Empleado empleado, YearMonth mesPeriodo) {
        LocalDate inicioMes = mesPeriodo.atDay(1);
        LocalDate finMes = mesPeriodo.atEndOfMonth();
        LocalDate fechaIngreso = empleado.getFechaContratacion();

        LocalDate inicio = (fechaIngreso != null && fechaIngreso.isAfter(inicioMes)) ? fechaIngreso : inicioMes;

        List<Incidencia> salidasAnticipadas = incidenciaRepository.findByEmpleadoAndTipoAndEstadoAndFechaBetween(
                empleado, "SALIDA ANTICIPADA", "NO JUSTIFICADO", inicio, finMes);

        return salidasAnticipadas.stream()
                .mapToInt(i -> i.getMinutosIncidencia() == null ? 0 : i.getMinutosIncidencia())
                .sum();
    }

    private int calcularDiasTrabajados(Empleado empleado, YearMonth mesPeriodo) {
        LocalDate inicioMes = mesPeriodo.atDay(1);
        LocalDate finMes = mesPeriodo.atEndOfMonth();
        LocalDate fechaIngreso = empleado.getFechaContratacion();

        LocalDate inicio = (fechaIngreso != null && fechaIngreso.isAfter(inicioMes)) ? fechaIngreso : inicioMes;

        return asistenciaRepository.findByEmpleadoAndFechaBetween(empleado, inicio, finMes).size();
    }

    private int calcularDiasFalta(Empleado empleado, YearMonth mesPeriodo) {
        LocalDate inicioMes = mesPeriodo.atDay(1);
        LocalDate finMes = mesPeriodo.atEndOfMonth();
        LocalDate fechaIngreso = empleado.getFechaContratacion();

        LocalDate inicio = (fechaIngreso != null && fechaIngreso.isAfter(inicioMes)) ? fechaIngreso : inicioMes;

        return (int) incidenciaRepository.countByEmpleadoAndTipoAndEstadoAndFechaBetween(
                empleado, "FALTA", "NO JUSTIFICADO", inicio, finMes);
    }

    public boolean existeNominaParaPeriodo(Integer empleadoId, String mesPeriodo) {
        return nominaRepository.findByEmpleadoId(empleadoId)
                .stream()
                .anyMatch(n -> mesPeriodo.equals(n.getMesPeriodo()));
    }

    public String generarNominasParaTodos() {
        List<Empleado> empleados = empleadoService.findAll();
        int totalNominasGeneradas = 0;
        int empleadosProcesados = 0;

        for (Empleado empleado : empleados) {
            try {
                if (empleado.getTipoContrato() != null &&
                        !"Contratista".equalsIgnoreCase(empleado.getTipoContrato().getNombre())) {

                    List<Nomina> nominas = generarTodasNominasEmpleado(empleado.getId());
                    totalNominasGeneradas += nominas.size();
                    empleadosProcesados++;
                }
            } catch (Exception e) {
                System.err.println("Error generando nóminas para empleado " + empleado.getId() + ": " + e.getMessage());
            }
        }

        return String.format("Se generaron %d nóminas para %d empleados",
                totalNominasGeneradas, empleadosProcesados);
    }

    @Transactional
    public Nomina actualizarNomina(Long id) {

        Nomina nomina = nominaRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Nómina no encontrada"));

        Empleado empleado = nomina.getEmpleado();
        YearMonth mesPeriodo = YearMonth.parse(nomina.getMesPeriodo());

        // Recalcular valores igual que en generarNominaParaPeriodo,
        // pero SIN crear nueva entidad ni asignar ID nuevo.
        Double sueldoBaseCompleto = empleado.getSueldoBase();
        Double sueldoBaseProporcional = calcularSueldoProporcional(empleado, mesPeriodo, sueldoBaseCompleto);
        double valorDia = sueldoBaseCompleto / 30.0;
        double valorHora = valorDia / 8.0;

        int diasTrabajados = calcularDiasTrabajados(empleado, mesPeriodo);
        int faltas = calcularDiasFalta(empleado, mesPeriodo);
        int minutosTardanza = calcularMinutosTardanzaNoJustificada(empleado, mesPeriodo);
        int minutosSalida = calcularMinutosSalidaAnticipadaNoJustificada(empleado, mesPeriodo);

        double horasTardanza = minutosTardanza / 60.0;
        double horasSalida = minutosSalida / 60.0;

        double descuentoFaltas = faltas * valorDia;
        double descuentoTardanza = horasTardanza * valorHora;
        double descuentoSalida = horasSalida * valorHora;

        double descuentoAFP = 0.0;
        double descuentoONP = 0.0;
        if (empleado.getTipoPension() != null) {
            String tipo = empleado.getTipoPension().getNombre().toUpperCase();
            if (tipo.equals("AFP"))
                descuentoAFP = sueldoBaseProporcional * 0.10;
            if (tipo.equals("ONP"))
                descuentoONP = sueldoBaseProporcional * 0.13;
        }

        double asignacion = (empleado.getTieneHijosMenores() != null && empleado.getTieneHijosMenores())
                ? calcularAsignacionFamiliarProporcional(empleado, mesPeriodo, 102.50)
                : 0.0;

        double totalNeto = sueldoBaseProporcional + asignacion - descuentoAFP - descuentoONP -
                (descuentoFaltas + descuentoTardanza + descuentoSalida);

        // --- ACTUALIZAR CAMPOS DE LA NOMINA ---
        nomina.setSueldoBase(sueldoBaseProporcional);
        nomina.setValorDia(valorDia);
        nomina.setValorHora(valorHora);
        nomina.setDiasTrabajados(diasTrabajados);
        nomina.setDiasFalta(faltas);
        nomina.setMinutosTardanza(minutosTardanza);
        nomina.setHorasTardanza(horasTardanza);
        nomina.setHorasIncidencias(horasTardanza + horasSalida);
        nomina.setDescuentoTardanza(descuentoTardanza);
        nomina.setDescuentoFaltas(descuentoFaltas);
        nomina.setDescuentoIncidencias(descuentoFaltas + descuentoTardanza + descuentoSalida);
        nomina.setDescuentoAFP(descuentoAFP);
        nomina.setDescuentoONP(descuentoONP);
        nomina.setAsignacionFamiliar(asignacion);
        nomina.setTotalNeto(Math.max(0, totalNeto));

        return nominaRepository.save(nomina);
    }

    public byte[] generarVoucherPdf(Long nominaId) {
        Nomina nomina = nominaRepository.findByIdWithDetails(nominaId)
                .orElseThrow(() -> new IllegalArgumentException("La nómina solicitada no existe."));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 48, 36);
            PdfWriter.getInstance(document, baos);
            document.open();

            agregarCabecera(document, nomina);
            agregarResumenEmpleado(document, nomina);
            agregarDetalleMontos(document, nomina);
            agregarNotas(document);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("No fue posible generar el voucher en PDF", e);
        }
    }

    private void agregarCabecera(Document document, Nomina nomina) throws Exception {
        PdfPTable headerTable = new PdfPTable(new float[] { 2, 5 });
        headerTable.setWidthPercentage(100);

        PdfPCell imgCell;
        try {
            Image logo = Image.getInstance(new URL(LOGO_URL));
            logo.scaleToFit(85, 85);
            imgCell = new PdfPCell(logo, false);
        } catch (Exception e) {
            imgCell = new PdfPCell(new Phrase("Colegio Mi Mundo Feliz", getFont(14, Font.BOLD, COLOR_AZUL)));
        }
        imgCell.setBorder(Rectangle.NO_BORDER);
        imgCell.setRowspan(2);
        imgCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(imgCell);

        PdfPCell titleCell = new PdfPCell(
                new Phrase("Mi Mundo Feliz - Gestión del Capital Humano", getFont(14, Font.BOLD, COLOR_AZUL)));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(titleCell);

        PdfPCell subtitle = new PdfPCell(new Phrase("Voucher de Pago - " + nomina.getMesPeriodo(),
                getFont(12, Font.NORMAL, BaseColor.DARK_GRAY)));
        subtitle.setBorder(Rectangle.NO_BORDER);
        subtitle.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(subtitle);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        LineSeparator separator = new LineSeparator();
        separator.setLineColor(COLOR_AZUL);
        document.add(new Chunk(separator));
        document.add(Chunk.NEWLINE);
    }

    private void agregarResumenEmpleado(Document document, Nomina nomina) throws DocumentException {
        Empleado empleado = nomina.getEmpleado();
        PdfPTable infoTable = new PdfPTable(new float[] { 2, 3 });
        infoTable.setWidthPercentage(100);

        agregarFilaInfo(infoTable, "Colaborador", empleado.getNombre() + " " + empleado.getApellido());
        agregarFilaInfo(infoTable, "Documento", empleado.getDni());
        agregarFilaInfo(infoTable, "Cargo",
                empleado.getCargo() != null ? empleado.getCargo().getNombreCargo() : "No asignado");
        agregarFilaInfo(infoTable, "Departamento",
                empleado.getDepartamento() != null ? empleado.getDepartamento().getNombreDepartamento()
                        : "No asignado");
        agregarFilaInfo(infoTable, "Tipo de contrato",
                empleado.getTipoContrato() != null ? empleado.getTipoContrato().getNombre() : "No asignado");
        agregarFilaInfo(infoTable, "Horas de inasistencias",
                String.format("%.2f horas", Optional.ofNullable(nomina.getHorasIncidencias()).orElse(0.0)));

        document.add(infoTable);
        document.add(Chunk.NEWLINE);
    }

    private void agregarDetalleMontos(Document document, Nomina nomina) throws DocumentException {
        PdfPTable table = new PdfPTable(new float[] { 3, 1.2f });
        table.setWidthPercentage(100);

        table.addCell(encabezado("Concepto"));
        table.addCell(encabezado("Monto (S/.)"));

        agregarFilaMonto(table, "Sueldo base del periodo", nomina.getSueldoBase());
        agregarFilaMonto(table, "Asignación familiar", nomina.getAsignacionFamiliar());
        agregarFilaMonto(table, "Descuento AFP", nomina.getDescuentoAFP());
        agregarFilaMonto(table, "Descuento ONP", nomina.getDescuentoONP());
        agregarFilaMonto(table, "Descuento por incidencias", nomina.getDescuentoIncidencias());

        PdfPCell totalLabel = new PdfPCell(
                new Phrase("Total neto a depositar", getFont(12, Font.BOLD, BaseColor.WHITE)));
        totalLabel.setBackgroundColor(COLOR_AZUL);
        totalLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabel.setPadding(8f);
        table.addCell(totalLabel);

        PdfPCell totalValue = new PdfPCell(
                new Phrase(String.format("S/ %.2f", nomina.getTotalNeto()), getFont(12, Font.BOLD, BaseColor.WHITE)));
        totalValue.setBackgroundColor(COLOR_DORADO);
        totalValue.setHorizontalAlignment(Element.ALIGN_CENTER);
        totalValue.setPadding(8f);
        table.addCell(totalValue);

        document.add(table);
        document.add(Chunk.NEWLINE);
    }

    private void agregarNotas(Document document) throws DocumentException {
        Paragraph nota = new Paragraph("Este comprobante respalda el pago correspondiente al periodo indicado. "
                + "Ante cualquier duda, comunícate con el área administrativa del colegio.",
                getFont(9, Font.ITALIC, BaseColor.DARK_GRAY));
        nota.setSpacingBefore(12f);
        document.add(nota);
    }

    private void agregarFilaInfo(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, getFont(10, Font.BOLD, COLOR_AZUL)));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(6f);
        labelCell.setBackgroundColor(COLOR_GRIS);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, getFont(10, Font.NORMAL, BaseColor.BLACK)));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(6f);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private PdfPCell encabezado(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, getFont(11, Font.BOLD, BaseColor.WHITE)));
        cell.setBackgroundColor(COLOR_AZUL);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8f);
        return cell;
    }

    private void agregarFilaMonto(PdfPTable table, String concepto, Double monto) {
        PdfPCell conceptoCell = new PdfPCell(new Phrase(concepto, getFont(10, Font.NORMAL, BaseColor.BLACK)));
        conceptoCell.setPadding(6f);
        conceptoCell.setBorderColor(COLOR_GRIS);

        PdfPCell montoCell = new PdfPCell(new Phrase(String.format("S/ %.2f", Optional.ofNullable(monto).orElse(0.0)),
                getFont(10, Font.NORMAL, BaseColor.BLACK)));
        montoCell.setPadding(6f);
        montoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        montoCell.setBorderColor(COLOR_GRIS);

        table.addCell(conceptoCell);
        table.addCell(montoCell);
    }

    private Font getFont(float size, int style, BaseColor color) {
        Font font = new Font(Font.FontFamily.HELVETICA, size, style);
        font.setColor(color);
        return font;
    }
}
