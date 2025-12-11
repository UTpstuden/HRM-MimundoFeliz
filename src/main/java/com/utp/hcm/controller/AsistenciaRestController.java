package com.utp.hcm.controller;

import com.utp.hcm.model.Asistencia;
import com.utp.hcm.service.AsistenciaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping; // <-- IMPORTAR
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List; // <-- IMPORTAR
import java.util.Optional; // <-- IMPORTAR

/**
 * API REST segura para que los empleados registren su asistencia.
 * Protegida por Spring Security bajo la ruta "/api/empleado/**" (solo
 * ROLE_EMPLEADO).
 */
@RestController
@RequestMapping("/api/empleado/asistencia")
public class AsistenciaRestController {

    @Autowired
    private AsistenciaService asistenciaService;

    /**
     * Endpoint para que el empleado autenticado obtenga su estado de asistencia del
     * día.
     */
    @GetMapping("/estado-hoy")
    public ResponseEntity<Asistencia> getEstadoAsistenciaHoy() {
        Optional<Asistencia> asistenciaHoy = asistenciaService.getEstadoAsistenciaHoy();

        return asistenciaHoy.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    /**
     * ====================================================================
     * MÉTODO NUEVO: Verificar si IP actual está autorizada
     * ====================================================================
     * Endpoint para verificar si la IP del cliente está en las redes autorizadas.
     */
    @GetMapping("/verificar-ip")
    public ResponseEntity<?> verificarIp(HttpServletRequest request) {
        try {
            String clientIp = obtenerIpCliente(request);
            var resultado = asistenciaService.verificarIpAutorizada(clientIp);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(java.util.Map.of(
                    "ip", "desconocida",
                    "autorizada", false,
                    "redNombre", "Error: " + e.getMessage()));
        }
    }

    /**
     * ====================================================================
     * MÉTODO NUEVO (Paso 2: Historial de Empleado)
     * ====================================================================
     * Endpoint para que el empleado obtenga su historial completo de asistencias.
     * 
     * @return ResponseEntity con la List<Asistencia> o un error.
     */
    @GetMapping("/historial")
    public ResponseEntity<?> getHistorialAsistencia() {
        try {
            List<Asistencia> historial = asistenciaService.getHistorialAsistenciaEmpleadoLogueado();
            return ResponseEntity.ok(historial);

        } catch (Exception e) {
            // Capturar cualquier error (ej. si el usuario no es un empleado)
            return ResponseEntity.status(500).body("Error al obtener el historial: " + e.getMessage());
        }
    }

    /**
     * Endpoint para que el empleado autenticado marque su hora de entrada.
     */
    @PostMapping("/marcar-entrada")
    public ResponseEntity<?> marcarEntrada(HttpServletRequest request) {
        try {
            Asistencia asistencia = asistenciaService.marcarEntrada(obtenerIpCliente(request));
            return ResponseEntity.ok(asistencia);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Endpoint para que el empleado autenticado marque su hora de salida.
     */
    @PostMapping("/marcar-salida")
    public ResponseEntity<?> marcarSalida(HttpServletRequest request) {
        try {
            Asistencia asistencia = asistenciaService.marcarSalida(obtenerIpCliente(request));
            return ResponseEntity.ok(asistencia);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error interno del servidor: " + e.getMessage());
        }
    }

    private String obtenerIpCliente(HttpServletRequest request) {
        // 1. Primero intentar X-Forwarded-For (para proxies/load balancers)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            String detectedIp = normalizarIp(ip.split(",")[0].trim());
            System.out.println("DEBUG: IP detected via X-Forwarded-For: " + detectedIp);
            return detectedIp;
        }

        // 2. Obtener IP remota
        String remoteAddr = normalizarIp(request.getRemoteAddr());
        System.out.println("DEBUG: IP detected via RemoteAddr: " + remoteAddr);

        // 3. Si es localhost (desarrollo), usar la IP real de la máquina
        if (remoteAddr.equals("127.0.0.1") ||
                remoteAddr.equals("0:0:0:0:0:0:0:1") ||
                remoteAddr.equals("::1")) {

            String localIp = detectarIpLocal();
            if (localIp != null) {
                System.out.println("DEBUG: Localhost detectado, usando IP local de red: " + localIp);
                return localIp;
            }
        }

        return remoteAddr;
    }

    private String normalizarIp(String ip) {
        return ip;
    }

    private String detectarIpLocal() {
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ignored) {
        }
        return null;
    }
}
