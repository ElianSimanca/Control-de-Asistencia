package com.asistencia.config;

import com.asistencia.entity.Usuario;
import com.asistencia.repository.UsuarioRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner crearUsuarioInicial(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (usuarioRepository.count() == 0) {

                Usuario usuario = new Usuario();

                usuario.setUsername("admin");

                usuario.setPassword(
                        passwordEncoder.encode("admin123")
                );

                usuario.setActivo(true);

                usuarioRepository.save(usuario);

                System.out.println(
                        "Usuario administrador creado: admin"
                );
            }
        };
    }
}