package com.utp.hcm.service;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Usuario;
import com.utp.hcm.repository.EmpleadoRepository;
import com.utp.hcm.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpleadoRepository empleadoRepository;

    public List<Usuario> findAll() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> findById(int id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Obtiene la lista de empleados que aún no tienen una cuenta de usuario.
     */
    public List<Empleado> findEmpleadosSinUsuario() {
        return empleadoRepository.findEmpleadosSinUsuario();
    }

    @Transactional
    public Usuario save(Usuario usuario) {
        // 1. Validar y cargar el empleado completo
        if (usuario.getEmpleado() == null || usuario.getEmpleado().getId() == 0) {
            throw new IllegalArgumentException("El empleado es obligatorio.");
        }
        Empleado empleado = empleadoRepository.findById(usuario.getEmpleado().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Empleado no encontrado con ID: " + usuario.getEmpleado().getId()));

        // 2. Sincronizar datos del empleado al usuario
        usuario.setEmpleado(empleado);
        usuario.setCorreoInstitucional(empleado.getCorreo());

        // 3. Validar Rol
        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            throw new IllegalArgumentException("El rol es obligatorio.");
        }

        // 4. Lógica de Creación vs Actualización
        if (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0) {
            // --- CREACIÓN ---
            // 4a. Verificar que el empleado no tenga ya un usuario
            if (usuarioRepository.existsByEmpleadoId(empleado.getId())) {
                throw new IllegalArgumentException("Este empleado ya tiene una cuenta de usuario.");
            }
            // 4b. Omitir contraseña (como acordamos)
            usuario.setPassword(null);
            usuario.setIdUsuario(null);
        } else {
            // --- ACTUALIZACIÓN ---
            // 4a. Rescatar la contraseña existente para no sobreescribirla
            Usuario usuarioExistente = usuarioRepository.findById(usuario.getIdUsuario())
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado para actualizar."));

            // 4b. No permitimos cambiar el empleado en una actualización, solo el ROL.
            if (usuarioExistente.getEmpleado().getId() != empleado.getId()) {
                throw new IllegalArgumentException("No se puede cambiar el empleado de una cuenta existente.");
            }

            usuario.setPassword(usuarioExistente.getPassword()); // Preservar la contraseña
        }

        return usuarioRepository.save(usuario);
    }

    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Transactional
    public void changePassword(Integer usuarioId, String oldDescrypted, String newEncrypted) {
        throw new UnsupportedOperationException("Use la sobrecarga con password encoder");
    }

    // Método que se usará
    @Transactional
    public void updatePassword(Integer usuarioId, String rawNewPassword) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        usuario.setPassword(passwordEncoder.encode(rawNewPassword));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void deleteById(int id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado para eliminar.");
        }
        usuarioRepository.deleteById(id);
    }
}