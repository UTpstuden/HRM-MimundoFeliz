package com.utp.hcm.service;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.utp.hcm.model.Empleado;
import com.utp.hcm.model.Usuario;
import com.utp.hcm.repository.EmpleadoRepository;
import com.utp.hcm.repository.UsuarioRepository;

@Service
public class RecuperarService {

    @Autowired
    private EmpleadoRepository empleadoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Metodo principal de recuperacion de contrasena
    public boolean recuperarPassword(String correoPersonal) {
        // 1. Buscar al empleado por su correo personal
        Optional<Empleado> optEmpleado = empleadoRepository.findByCorreo(correoPersonal);
        if (optEmpleado.isEmpty()) {
            return false; // No se encontro el empleado
        }

        Empleado empleado = optEmpleado.get();

        // 2. Buscar el usuario asociado al empleado
        Usuario usuario = usuarioRepository.findByEmpleado(empleado).orElse(null);
        if (usuario == null) {
            return false; // El empleado no tiene usuario vinculado
        }

        // 3. Generar nueva contrasena temporal
        String nuevaPassword = generarPasswordTemporal();

        // 4. Encriptar y guardar la nueva contrasena
        System.out.println(
                "DEBUG: Generada password temporal para " + usuario.getCorreoInstitucional() + ": " + nuevaPassword);
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        // 5. Enviar correo con las nuevas credenciales al correo personal
        enviarCorreoCredenciales(
                empleado.getCorreo(), // correo personal
                empleado.getNombre() + " " + empleado.getApellido(),
                usuario.getCorreoInstitucional(), // usuario institucional
                nuevaPassword);

        return true;
    }

    // Genera una contrasena temporal segura
    private String generarPasswordTemporal() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return sb.toString();
    }

    // Envio del correo con las credenciales
    private void enviarCorreoCredenciales(String to, String nombre, String usuario, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Recuperación de contraseña - Mi Mundo Feliz");

        String texto = String.format(
                "Hola %s,\n\n" +
                        "Hemos recibido una solicitud para restablecer tu contraseña.\n\n" +
                        "Tus nuevas credenciales de acceso son:\n\n" +
                        "Usuario: %s\n" +
                        "Contraseña temporal: %s\n\n" +
                        "Por favor, cambia tu contraseña al iniciar sesión.\n\n" +
                        "Saludos cordiales,\n" +
                        "El equipo de Mi Mundo Feliz.",
                nombre, usuario, password);

        message.setText(texto);
        mailSender.send(message);
    }
}
