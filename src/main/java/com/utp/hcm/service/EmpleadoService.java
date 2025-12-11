package com.utp.hcm.service;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Usuario;
import com.utp.hcm.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmpleadoService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmpleadoRepository empleadoRepository;
    @Autowired
    private CargoRepository cargoRepository;
    @Autowired
    private DepartamentoRepository departamentoRepository;
    @Autowired
    private HorarioTrabajoRepository horarioTrabajoRepository;
    @Autowired
    private TipoContratoRepository tipoContratoRepository;
    @Autowired
    private TipoPensionRepository tipoPensionRepository;
    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private AsistenciaRepository asistenciaRepository;

    @Autowired
    private NominaRepository nominaRepository; // <-- 1. AÑADE ESTA LÍNEA

    public List<Empleado> findAll() {
        return empleadoRepository.findAll();
    }

    public Optional<Empleado> findById(int id) {
        return empleadoRepository.findById(id);
    }

    // Tu método save() COMPLETO (no necesita cambios)
    @Transactional
    public Empleado save(Empleado empleado) {
        // ... (Tu lógica de guardar, crear usuario, y enviar correo va aquí)
        // ... (Esto ya está correcto en tu código)
        // --- VALIDACIONES EXISTENTES ---
        if (empleado.getCorreo() == null || empleado.getCorreo().trim().isEmpty()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        if (empleado.getSueldoBase() == null || empleado.getSueldoBase() <= 0) {
            throw new IllegalArgumentException("El sueldo base debe ser mayor a 0");
        }

        boolean esNuevoEmpleado = (empleado.getId() == 0);

        // Validar que el correo sea único (excepto para el mismo empleado en
        // actualización)
        if (esNuevoEmpleado) {
            if (empleadoRepository.existsByCorreo(empleado.getCorreo())) {
                throw new IllegalArgumentException("El correo electrónico personal ya está registrado");
            }
        } else { // Actualización
            Optional<Empleado> empleadoExistente = empleadoRepository.findByCorreo(empleado.getCorreo());
            if (empleadoExistente.isPresent() && empleadoExistente.get().getId() != empleado.getId()) {
                throw new IllegalArgumentException("El correo electrónico ya está registrado por otro empleado");
            }
        }
        // --- FIN VALIDACIONES EXISTENTES ---

        // Cargar entidades relacionadas (código existente)
        if (empleado.getCargo() != null && empleado.getCargo().getId() != 0) {
            empleado.setCargo(cargoRepository.findById(empleado.getCargo().getId()).orElse(null));
        }
        if (empleado.getDepartamento() != null && empleado.getDepartamento().getId() != 0) {
            empleado.setDepartamento(departamentoRepository.findById(empleado.getDepartamento().getId()).orElse(null));
        }
        if (empleado.getHorarioTrabajo() != null && empleado.getHorarioTrabajo().getId() != 0) {
            empleado.setHorarioTrabajo(
                    horarioTrabajoRepository.findById(empleado.getHorarioTrabajo().getId()).orElse(null));
        }
        if (empleado.getTipoContrato() != null && empleado.getTipoContrato().getId() != 0) {
            empleado.setTipoContrato(tipoContratoRepository.findById(empleado.getTipoContrato().getId()).orElse(null));
        }
        if (empleado.getTipoPension() != null && empleado.getTipoPension().getId() != 0) {
            empleado.setTipoPension(tipoPensionRepository.findById(empleado.getTipoPension().getId()).orElse(null));
        }

        // 1. GUARDAR EL EMPLEADO PRIMERO
        Empleado empleadoGuardado = empleadoRepository.save(empleado);

        // 2. SI ES NUEVO, CREAR USUARIO Y ENVIAR CORREO
        if (esNuevoEmpleado) {
            // Generar Correo Institucional
            String emailInstitucional = generarEmailInstitucional(empleadoGuardado.getNombre(),
                    empleadoGuardado.getApellido());

            // Generar Contraseña Aleatoria
            String passwordPlano = UUID.randomUUID().toString().substring(0, 10);
            String passwordHasheada = passwordEncoder.encode(passwordPlano);

            // Crear y Guardar el Usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setEmpleado(empleadoGuardado);
            nuevoUsuario.setCorreoInstitucional(emailInstitucional);
            nuevoUsuario.setPassword(passwordHasheada);
            nuevoUsuario.setRol("ROLE_EMPLEADO"); // Asignar Rol con prefijo correcto

            usuarioRepository.save(nuevoUsuario);

            // Enviar el correo (al correo personal)
            emailService.enviarCredenciales(
                    empleadoGuardado.getCorreo(), // Destino
                    emailInstitucional, // Credencial 1
                    passwordPlano // Credencial 2
            );
        }

        return empleadoGuardado;
    }

    @Transactional
    public void updateFoto(Integer id, org.springframework.web.multipart.MultipartFile file)
            throws java.io.IOException {
        Empleado empleado = empleadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empleado no encontrado"));

        if (file != null && !file.isEmpty()) {
            empleado.setFoto(file.getBytes());
            empleadoRepository.save(empleado);
        }
    }

    // Tu método utilitario (no necesita cambios)
    private String generarEmailInstitucional(String nombre, String apellido) {
        // ... (Tu lógica de generar email va aquí)
        // ... (Esto ya está correcto en tu código)
        String nombreLimpio = nombre.toLowerCase().split(" ")[0].replaceAll("[^a-z]", "");
        String apellidoLimpio = apellido.toLowerCase().split(" ")[0].replaceAll("[^a-z]", "");

        String emailBase = nombreLimpio + "." + apellidoLimpio;
        String dominio = "@mimundofeliz.edu.pe";

        if (usuarioRepository.existsByCorreoInstitucional(emailBase + dominio)) {
            int i = 1;
            while (usuarioRepository.existsByCorreoInstitucional(emailBase + i + dominio)) {
                i++;
            }
            return emailBase + i + dominio;
        }
        return emailBase + dominio;
    }

    // --- REEMPLAZA TU MÉTODO deleteById POR ESTE ---
    @Transactional
    public void deleteById(int id) {

        // 0. Verificar si el empleado existe
        if (!empleadoRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró el empleado con ID: " + id);
        }

        // 1. ELIMINAR INCIDENCIAS (Hijo #1)
        incidenciaRepository.deleteAllByEmpleadoId(id);

        // 2. ELIMINAR ASISTENCIAS (Hijo #2)
        asistenciaRepository.deleteAllByEmpleadoId(id);

        // 3. ELIMINAR NÓMINAS (Hijo #3)
        nominaRepository.deleteAllByEmpleadoId(id);

        // 4. ELIMINAR USUARIO (Hijo #4)
        // (Esta lógica ya la tenías)
        Optional<Usuario> usuarioAsociado = usuarioRepository.findByEmpleadoId(id);
        usuarioAsociado.ifPresent(usuario -> {
            usuarioRepository.delete(usuario);
        });

        // 5. ELIMINAR EMPLEADO (Padre)
        empleadoRepository.deleteById(id);
    }
}