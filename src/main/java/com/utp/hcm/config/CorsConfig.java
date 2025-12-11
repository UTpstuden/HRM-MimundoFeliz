package com.utp.hcm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// Archivo: com.utp.hcm.config.CorsConfig.java (o donde tengas tus configs)

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Aplica la configuración a TODAS las rutas de la API
                .allowedOrigins(
                    "http://localhost:3000", // Para desarrollo local (si usas React/Vue)
                    "https://hrm-mimundofeliz-4.onrender.com" // <--- ¡TU URL DE FRONT-END!
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Permite los métodos que usas
                .allowedHeaders("*") // Permite todos los headers (incluyendo Content-Type, Authorization)
                .allowCredentials(true); // Permite cookies y encabezados de autenticación (JWT)
    }
}