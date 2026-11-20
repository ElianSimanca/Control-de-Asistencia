package com.asistencia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            
            // CONFIGURACIÓN DE CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth
                // 1. PERMITIR SIEMPRE LAS PETICIONES PREFLIGHT (OPTIONS)
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 2. Rutas Públicas (Login y ESP32)
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/cards/detect").permitAll()
                .requestMatchers("/api/marcaciones/rfid").permitAll()

                // 3. Frontend React (Lectura y Marcación Manual)
                .requestMatchers(HttpMethod.GET, "/api/marcaciones/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/marcaciones").permitAll()

                // 4. PERMISOS PARA LIMPIAR DATOS (Boton de React)
                .requestMatchers(HttpMethod.DELETE, "/api/marcaciones/limpiar").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/empleados/limpiar").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/cards/limpiar").permitAll()

                // 5. Cualquier otra petición requiere login
                .anyRequest().authenticated()
            )

            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BEAN DE CORS (Para que React pueda conectarse sin problema)
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); 
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
