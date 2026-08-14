package com.asistencia.controller;

import com.asistencia.entity.Usuario;
import com.asistencia.repository.UsuarioRepository;
import com.asistencia.security.JwtService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByUsername(request.getUsername())
                .orElse(null);

        if (usuario == null ||
                !usuario.isActivo() ||
                !passwordEncoder.matches(
                        request.getPassword(),
                        usuario.getPassword())) {

            return ResponseEntity
                    .status(401)
                    .body("Usuario o contraseña incorrectos");
        }

        String token = jwtService.generarToken(
                usuario.getUsername()
        );

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }

    public static class LoginRequest {

        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {

        private String token;

        public LoginResponse(String token) {
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }
}