package com.utp.hcm.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void enviarCredenciales(String emailPersonal, String emailInstitucional, String passwordPlano) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("tu-correo@gmail.com"); // Debe ser el mismo de application.properties
            message.setTo(emailPersonal);
            message.setSubject("¡Bienvenido a Mi Mundo Feliz! - Tus Credenciales de Acceso");

            String texto = String.format("""
                ¡Hola!
                
                Te damos la bienvenida al equipo de Mi Mundo Feliz.
                
                Hemos creado tu cuenta de acceso a nuestro sistema de gestión.
                
                Tus credenciales son:
                Usuario (Correo Institucional): %s
                Contraseña Temporal: %s
                
                Por favor, guarda esta información en un lugar seguro.
                
                Saludos,
                El equipo de Administración.
                """, emailInstitucional, passwordPlano);

            message.setText(texto);
            mailSender.send(message);

        } catch (Exception e) {
            // Es importante registrar el error, pero NO lanzar una excepción
            // para que la creación del empleado no falle si el correo falla.
            System.err.println("Error al enviar correo de bienvenida a " + emailPersonal + ": " + e.getMessage());
        }
    }
}