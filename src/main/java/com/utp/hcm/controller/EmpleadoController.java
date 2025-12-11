package com.utp.hcm.controller;

import com.utp.hcm.service.IncidenciaService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.utp.hcm.repository.EmpleadoRepository;
import com.utp.hcm.repository.UsuarioRepository;
import com.utp.hcm.model.Usuario;
import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Cargo; // <--- IMPORT NECESARIO
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.utp.hcm.model.Asistencia; // <-- IMPORTAR
import com.utp.hcm.service.AsistenciaService; // <-- IMPORTAR
import java.util.Optional; // <-- IMPORTAR

@Controller
public class EmpleadoController {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private IncidenciaService incidenciaService;

    @Autowired
    private AsistenciaService asistenciaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/empleado/perfil-empleado")
    public String perfilEmpleado(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        Usuario usuario = usuarioRepository
                .findByCorreoInstitucionalConEmpleado(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Empleado empleado = usuario.getEmpleado();
        Cargo cargo = empleado.getCargo();

        model.addAttribute("empleado", empleado);
        model.addAttribute("cargo", cargo);

        return "perfil-empleado";
    }

    @GetMapping("/empleado/asistencia-empleado")
    public String asistenciaEmpleado(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        Usuario usuario = usuarioRepository
                .findByCorreoInstitucionalConEmpleado(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Empleado empleado = usuario.getEmpleado();

        model.addAttribute("empleado", empleado);

        return "asistencia-empleado";
    }

    @GetMapping("/empleado/inicio")
    public String empleadoInicio(Model model) {

        // --- 1. Lógica de Empleado (Ya la tenías) ---
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();
        Usuario usuario = usuarioRepository
                .findByCorreoInstitucionalConEmpleado(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Empleado empleado = usuario.getEmpleado();
        model.addAttribute("empleado", empleado);


        // --- 2. Lógica para Widgets (Actualizada) ---

        // Widget 1: Estado de Hoy (Lógica existente)
        Optional<Asistencia> asistenciaHoy = asistenciaService.getEstadoAsistenciaHoy();
        model.addAttribute("estadoAsistencia", asistenciaHoy);

        // Widget 2: Asistencia del Mes (Nueva lógica)
        long diasTrabajados = asistenciaService.contarAsistenciasDelMesActual();
        model.addAttribute("diasTrabajados", diasTrabajados);

        // Widget 3: Incidencias (Ahora dinámico)
        long incidenciasPendientes = incidenciaService.contarIncidenciasPendientesPorEmpleado(empleado);
        model.addAttribute("incidencias", incidenciasPendientes);

        return "inicio-empleado";
    }

    @PostMapping("/empleado/subir-foto")
    public String subirFoto(@RequestParam("id") Integer id,
                            @RequestParam("foto") MultipartFile foto) throws IOException {

        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        empleado.setFoto(foto.getBytes());
        empleadoRepository.save(empleado);

        return "redirect:/empleado/perfil-empleado";

    }



    @GetMapping("/empleado/foto/{id}")
    public ResponseEntity<byte[]> mostrarFoto(@PathVariable int id) {
        Empleado e = empleadoRepository.findById(id).orElse(null);

        if (e == null || e.getFoto() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(e.getFoto());
    }
    @PostMapping("/usuario/cambiar-password")
    public String cambiarPassword(@RequestParam String actual,
                              @RequestParam String nueva,
                              @RequestParam String confirmada) {

    if(!nueva.equals(confirmada)){
        return "redirect:/empleado/perfil-empleado?error=nomatch";
    }

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String correo = auth.getName();

    Usuario usuario = usuarioRepository.findByCorreoInstitucional(correo)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    if(!passwordEncoder.matches(actual, usuario.getPassword())){
        return "redirect:/empleado/perfil-empleado?error=wrongpass";

    }

    usuario.setPassword(passwordEncoder.encode(nueva));
    usuarioRepository.save(usuario);

    return "redirect:/empleado/perfil-empleado?success=clave";
}


}
