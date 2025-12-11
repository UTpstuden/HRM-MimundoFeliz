package com.utp.hcm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

import com.utp.hcm.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private com.utp.hcm.security.CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean para el AuthenticationManager necesario para JWT
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                // IF_REQUIRED: Crear sesiones solo cuando sea necesario (login tradicional)
                // Las APIs con JWT seguirán siendo stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        // URLs públicas - API REST para autenticación
                        .requestMatchers("/api/auth/**", "/api/recuperar").permitAll()

                        // URLs públicas - Vistas tradicionales y SPA
                        .requestMatchers("/", "/index.html", "/*.js", "/*.css", "/*.ico", "/*.png", "/*.jpg",
                                "/assets/**", "/login", "/recuperar", "/css/**", "/js/**", "/images/**", "/media/**")
                        .permitAll()
                        .requestMatchers("/app/**").permitAll()

                        // URLs de Administrador
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")

                        // URLs de Empleado
                        .requestMatchers(
                                "/empleado/**",
                                "/api/empleado/**",
                                "/api/empleados/**",
                                "/api/nominas/**",
                                "/api/asistencias/**",
                                "/api/cargos/**",
                                "/api/departamentos/**",
                                "/api/horarios/**",
                                "/api/tipos-contrato/**",
                                "/api/tipos-pension/**",
                                "/api/dashboard/stats/asistencia")
                        .hasAnyRole("EMPLEADO", "ADMIN")

                        // Dashboard general (algunos endpoints para empleados)
                        .requestMatchers("/api/dashboard/**").hasRole("ADMIN")

                        // Cualquier otra petición debe estar autenticada
                        .anyRequest().authenticated())
                // Mantener el form login para las vistas tradicionales
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customAuthenticationSuccessHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                // Agregar el filtro JWT antes del filtro de autenticación de usuario/contraseña
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // Manejo de excepciones para No Autorizado (401)
                .exceptionHandling(e -> e.authenticationEntryPoint((request, response, authException) -> {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: No autorizado");
                }));

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
