package com.utp.hcm.service;

import com.utp.hcm.model.*;
import com.utp.hcm.repository.AsistenciaRepository;
import com.utp.hcm.repository.IncidenciaRepository;
import com.utp.hcm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class AsistenciaService {

    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private IncidenciaRepository incidenciaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NetworkPolicyService networkPolicyService;

    /**
     * Lógica principal para marcar la entrada de un empleado - CON REGISTRO DE
     * MINUTOS
     */
    @Transactional
    public Asistencia marcarEntrada(String ipAddress) {
        // 1. Obtener el empleado que está actualmente logueado
        Empleado empleado = getEmpleadoAutenticado();

        LocalDate fechaHoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        validarIpPermitida(ipAddress, "entrada");

        // 2. Verificar si el empleado ya marcó su entrada hoy
        Optional<Asistencia> registroExistente = asistenciaRepository.findByEmpleadoAndFecha(empleado, fechaHoy);
        if (registroExistente.isPresent()) {
            throw new RuntimeException("Error: Ya has marcado tu entrada el día de hoy.");
        }

        // 3. Obtener el horario que le corresponde al empleado para HOY
        Optional<HorarioDetalle> horarioHoy = getHorarioParaHoy(empleado);
        if (horarioHoy.isEmpty()) {
            throw new RuntimeException("Error: No tienes un horario de trabajo programado para hoy.");
        }

        // 4. Determinar si es "PUNTUAL" o "TARDANZA" y calcular minutos exactos
        HorarioDetalle detalleHorario = horarioHoy.get();
        LocalTime horaEntradaProgramada = detalleHorario.getHoraEntrada();

        String estadoEntrada;
        Integer minutosTardanza = 0;

        if (horaActual.isAfter(horaEntradaProgramada)) {
            estadoEntrada = "TARDANZA";
            // CALCULAR MINUTOS EXACTOS DE TARDANZA
            minutosTardanza = (int) Duration.between(horaEntradaProgramada, horaActual).toMinutes();
        } else {
            estadoEntrada = "PUNTUAL";
        }

        // 5. Crear y guardar el nuevo registro de asistencia
        Asistencia nuevaAsistencia = Asistencia.builder()
                .empleado(empleado)
                .fecha(fechaHoy)
                .horaEntrada(horaActual)
                .ipEntrada(ipAddress)
                .estadoEntrada(estadoEntrada)
                .build();

        // Guardamos la asistencia primero para que tenga un ID
        Asistencia asistenciaGuardada = asistenciaRepository.save(nuevaAsistencia);

        // 6. CREAR INCIDENCIA AUTOMÁTICA SI HAY TARDANZA
        if ("TARDANZA".equals(estadoEntrada) && minutosTardanza > 0) {
            crearIncidencia(asistenciaGuardada, "TARDANZA", minutosTardanza);
        }

        return asistenciaGuardada;
    }

    /**
     * Lógica principal para marcar la salida de un empleado - CON REGISTRO DE
     * MINUTOS
     */
    @Transactional
    public Asistencia marcarSalida(String ipAddress) {
        // 1. Obtener el empleado logueado
        Empleado empleado = getEmpleadoAutenticado();

        LocalDate fechaHoy = LocalDate.now();
        LocalTime horaActual = LocalTime.now();

        validarIpPermitida(ipAddress, "salida");

        // 2. Verificar que haya marcado su ENTRADA primero
        Asistencia registro = asistenciaRepository.findByEmpleadoAndFecha(empleado, fechaHoy)
                .orElseThrow(() -> new RuntimeException("Error: Debes marcar tu entrada antes de marcar la salida."));

        // 3. Verificar que no haya marcado su salida previamente
        if (registro.getHoraSalida() != null) {
            throw new RuntimeException("Error: Ya has marcado tu salida el día de hoy.");
        }

        // 4. Determinar el estado de la salida y calcular minutos de salida anticipada
        String estadoSalida = "SALIDA REGISTRADA";
        Integer minutosSalidaAnticipada = 0;
        Optional<HorarioDetalle> horarioHoy = getHorarioParaHoy(empleado);

        if (horarioHoy.isPresent()) {
            LocalTime horaSalidaProgramada = horarioHoy.get().getHoraSalida();
            if (horaActual.isBefore(horaSalidaProgramada)) {
                estadoSalida = "SALIDA ANTICIPADA";
                // CALCULAR MINUTOS EXACTOS DE SALIDA ANTICIPADA
                minutosSalidaAnticipada = (int) Duration.between(horaActual, horaSalidaProgramada).toMinutes();
            }
        }

        // 5. Actualizar el registro existente con la hora de salida
        registro.setHoraSalida(horaActual);
        registro.setEstadoSalida(estadoSalida);
        registro.setIpSalida(ipAddress);

        Asistencia asistenciaGuardada = asistenciaRepository.save(registro);

        // 6. CREAR INCIDENCIA AUTOMÁTICA SI HAY SALIDA ANTICIPADA
        if ("SALIDA ANTICIPADA".equals(estadoSalida) && minutosSalidaAnticipada > 0) {
            // Verificar que no exista ya una incidencia de este tipo
            if (!incidenciaRepository.existsByAsistenciaAndTipo(asistenciaGuardada, "SALIDA ANTICIPADA")) {
                crearIncidencia(asistenciaGuardada, "SALIDA ANTICIPADA", minutosSalidaAnticipada);
            }
        }

        return asistenciaGuardada;
    }

    // --- MÉTODOS PARA EL ADMIN ---

    @Transactional(readOnly = true)
    public List<Asistencia> getAsistenciasHoy() {
        return asistenciaRepository.findByFecha(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public Optional<Asistencia> getEstadoAsistenciaHoy() {
        try {
            Empleado empleado = getEmpleadoAutenticado();
            return asistenciaRepository.findByEmpleadoAndFecha(empleado, LocalDate.now());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Obtiene el historial de asistencia del empleado logueado
     */
    @Transactional(readOnly = true)
    public List<Asistencia> getHistorialAsistenciaEmpleadoLogueado() {
        Empleado empleado = getEmpleadoAutenticado();
        return asistenciaRepository.findByEmpleadoOrderByFechaDesc(empleado);
    }

    // --- MÉTODOS PRIVADOS DE AYUDA ---

    private Empleado getEmpleadoAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            throw new SecurityException("No se encontró un usuario autenticado.");
        }

        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreoInstitucionalConEmpleado(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + correo));

        if (usuario.getEmpleado() == null) {
            throw new RuntimeException("Esta cuenta de usuario no está vinculada a un empleado.");
        }
        return usuario.getEmpleado();
    }

    private Optional<HorarioDetalle> getHorarioParaHoy(Empleado empleado) {
        DayOfWeek diaDeHoy = LocalDate.now().getDayOfWeek();
        HorarioTrabajo horario = empleado.getHorarioTrabajo();

        if (horario == null || horario.getDetalles() == null || horario.getDetalles().isEmpty()) {
            return Optional.empty();
        }

        return horario.getDetalles().stream()
                .filter(detalle -> detalle.getDiaDeSemana().equals(diaDeHoy))
                .findFirst();
    }

    /**
     * Método para crear incidencias automáticamente con minutos
     */
    public void crearIncidencia(Asistencia asistencia, String tipo, Integer minutos) {
        Incidencia incidencia = Incidencia.builder()
                .empleado(asistencia.getEmpleado())
                .asistencia(asistencia)
                .fecha(asistencia.getFecha())
                .tipo(tipo)
                .estado("PENDIENTE") // Inicialmente pendiente de justificación
                .minutosIncidencia(minutos) // Registrar minutos exactos
                .build();

        incidenciaRepository.save(incidencia);
    }

    /**
     * Método para crear incidencias de FALTA (sin minutos, es el día completo)
     */
    public void crearIncidencia(Empleado empleado, LocalDate fecha, String tipo) {
        Incidencia incidencia = Incidencia.builder()
                .empleado(empleado)
                .asistencia(null)
                .fecha(fecha)
                .tipo(tipo)
                .estado("PENDIENTE")
                .minutosIncidencia(null) // FALTA no tiene minutos
                .build();

        incidenciaRepository.save(incidencia);
    }

    /**
     * Cuenta los días trabajados en el mes actual
     */
    @Transactional(readOnly = true)
    public long contarAsistenciasDelMesActual() {
        try {
            Empleado empleado = getEmpleadoAutenticado();
            LocalDate hoy = LocalDate.now();
            LocalDate inicioDeMes = hoy.withDayOfMonth(1);
            LocalDate finDeMes = hoy.withDayOfMonth(hoy.lengthOfMonth());

            long totalAsistencias = asistenciaRepository.findByEmpleadoAndFechaBetween(
                    empleado, inicioDeMes, finDeMes).size();

            long faltasJustificadas = incidenciaRepository.countByEmpleadoAndTipoAndEstadoAndFechaBetween(
                    empleado, "FALTA", "JUSTIFICADA", inicioDeMes, finDeMes);

            return totalAsistencias + faltasJustificadas;

        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void validarIpPermitida(String ipAddress, String tipoMarcacion) {
        if (ipAddress == null || ipAddress.isBlank()) {
            throw new RuntimeException(
                    "No se pudo detectar tu dirección IP. Verifica que estés conectado a la red del colegio.");
        }

        // LOCALHOST BYPASS DESACTIVADO - Solo se permiten IPs registradas en el
        // Dashboard Admin
        // Las IPs permitidas deben estar configuradas en "Configuración > Redes" del
        // dashboard
        // Para activar bypass en desarrollo local, descomentar las siguientes líneas:
        // if ("127.0.0.1".equals(ipAddress) || "0:0:0:0:0:0:0:1".equals(ipAddress) ||
        // "::1".equals(ipAddress)) {
        // System.out.println("DEBUG: Localhost bypass activado para IP: " + ipAddress);
        // return;
        // }

        System.out.println("DEBUG: Validando IP del cliente: " + ipAddress);

        List<String> allowedPrefixes = networkPolicyService.getActivePrefixes();
        System.out.println("DEBUG: Prefijos activos permitidos: " + allowedPrefixes);

        if (allowedPrefixes.isEmpty()) {
            throw new RuntimeException(
                    "No existen redes autorizadas configuradas para registrar asistencia. Contacta al administrador.");
        }

        boolean ipAutorizada = allowedPrefixes.stream().anyMatch(prefix -> {
            String cleanPrefix = prefix.trim();

            System.out.println("DEBUG: Comparando IP '" + ipAddress + "' con política '" + cleanPrefix + "'");

            // Si tiene /XX al final (CIDR notation), extraer solo la parte de IP
            String ipPart = cleanPrefix;
            if (cleanPrefix.contains("/")) {
                ipPart = cleanPrefix.split("/")[0];
                System.out.println("DEBUG: Extraída IP de CIDR: '" + ipPart + "'");
            }

            // 1. Coincidencia EXACTA de IP completa
            if (ipAddress.equals(ipPart)) {
                System.out.println("DEBUG: ✅ Coincidencia EXACTA encontrada!");
                return true;
            }

            // 2. Coincidencia por PREFIJO (ej: 192.168.1 coincide con 192.168.1.115)
            String prefixToCheck = ipPart;
            if (!prefixToCheck.endsWith(".")) {
                prefixToCheck += ".";
            }
            if (ipAddress.startsWith(prefixToCheck)) {
                System.out.println("DEBUG: ✅ Coincidencia por PREFIJO encontrada!");
                return true;
            }

            // 3. Coincidencia inversa: si el prefijo registrado empieza igual que la IP
            // Esto permite que "192.168.1" coincida con IP "192.168.1.115"
            String ipPrefix = ipAddress;
            if (!ipPrefix.endsWith(".")) {
                // Obtener los primeros 3 octetos de la IP del cliente
                String[] octets = ipAddress.split("\\.");
                if (octets.length >= 3) {
                    ipPrefix = octets[0] + "." + octets[1] + "." + octets[2];
                    if (ipPart.startsWith(ipPrefix) || ipPart.equals(ipPrefix)) {
                        System.out.println("DEBUG: ✅ Coincidencia por subred encontrada!");
                        return true;
                    }
                }
            }

            System.out.println("DEBUG: ❌ Sin coincidencia");
            return false;
        });

        if (!ipAutorizada) {
            throw new RuntimeException(
                    "Solo puedes marcar tu " + tipoMarcacion + " desde la red autorizada del colegio. (IP detectada: "
                            + ipAddress + ")");
        }
    }

    /**
     * Valida si una IP está dentro de un rango CIDR
     * Ejemplo: validarCIDR("10.249.211.50", "10.249.211.0/24") -> true
     */
    private boolean validarCIDR(String ipAddress, String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato CIDR inválido: " + cidr);
        }

        String networkAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        if (prefixLength < 0 || prefixLength > 32) {
            throw new IllegalArgumentException("Longitud de prefijo inválida: " + prefixLength);
        }

        long ipLong = ipToLong(ipAddress);
        long networkLong = ipToLong(networkAddress);

        // Crear máscara de red
        // /24 = 11111111.11111111.11111111.00000000
        long mask = (0xFFFFFFFFL << (32 - prefixLength)) & 0xFFFFFFFFL;

        // Verificar si la IP está en la misma red
        boolean enRango = (ipLong & mask) == (networkLong & mask);

        System.out.println("DEBUG CIDR: IP=" + ipAddress + ", Red=" + cidr + ", Resultado=" + enRango);
        return enRango;
    }

    /**
     * Convierte una dirección IP (String) a número long de 32 bits
     * Ejemplo: "192.168.1.1" -> 3232235777
     */
    private long ipToLong(String ipAddress) {
        String[] octets = ipAddress.split("\\.");
        if (octets.length != 4) {
            throw new IllegalArgumentException("Formato de IP inválido: " + ipAddress);
        }

        long result = 0;
        for (int i = 0; i < 4; i++) {
            int octet = Integer.parseInt(octets[i]);
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Octeto inválido en IP " + ipAddress + ": " + octet);
            }
            result = (result << 8) | octet;
        }

        return result;
    }

    /**
     * Verifica si una IP está autorizada y retorna información para el frontend.
     * 
     * @param ipAddress IP a verificar
     * @return Map con: ip, autorizada (boolean), redNombre (String)
     */
    public java.util.Map<String, Object> verificarIpAutorizada(String ipAddress) {
        java.util.Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("ip", ipAddress);

        List<com.utp.hcm.model.NetworkPolicy> policies = networkPolicyService.findAllActive();

        for (com.utp.hcm.model.NetworkPolicy policy : policies) {
            String cleanPrefix = policy.getPrefijo().trim();

            // Si tiene /XX al final (CIDR notation), extraer solo la parte de IP
            String ipPart = cleanPrefix;
            if (cleanPrefix.contains("/")) {
                ipPart = cleanPrefix.split("/")[0];
            }

            // 1. Coincidencia EXACTA
            if (ipAddress.equals(ipPart)) {
                resultado.put("autorizada", true);
                resultado.put("redNombre", policy.getDescripcion() != null && !policy.getDescripcion().isEmpty()
                        ? policy.getDescripcion()
                        : policy.getNombre());
                return resultado;
            }

            // 2. Coincidencia por PREFIJO
            String prefixToCheck = ipPart;
            if (!prefixToCheck.endsWith(".")) {
                prefixToCheck += ".";
            }
            if (ipAddress.startsWith(prefixToCheck)) {
                resultado.put("autorizada", true);
                resultado.put("redNombre", policy.getDescripcion() != null && !policy.getDescripcion().isEmpty()
                        ? policy.getDescripcion()
                        : policy.getNombre());
                return resultado;
            }

            // 3. Coincidencia por subred (primeros 3 octetos)
            String[] octets = ipAddress.split("\\.");
            if (octets.length >= 3) {
                String ipPrefix = octets[0] + "." + octets[1] + "." + octets[2];
                if (ipPart.startsWith(ipPrefix) || ipPart.equals(ipPrefix)) {
                    resultado.put("autorizada", true);
                    resultado.put("redNombre", policy.getDescripcion() != null && !policy.getDescripcion().isEmpty()
                            ? policy.getDescripcion()
                            : policy.getNombre());
                    return resultado;
                }
            }
        }

        // No encontró coincidencia
        resultado.put("autorizada", false);
        resultado.put("redNombre", "Red no autorizada");
        return resultado;
    }
}
