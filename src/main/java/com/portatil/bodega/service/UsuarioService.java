package com.portatil.bodega.service;

import com.portatil.bodega.dto.RegistroForm;
import com.portatil.bodega.model.Rol;
import com.portatil.bodega.model.Usuario;
import com.portatil.bodega.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public boolean existeEmail(String email) {
        return usuarioRepository.existsByEmail(normalizar(email));
    }

    /** Crea un cliente nuevo con la contraseña cifrada (BCrypt). */
    @Transactional
    public Usuario registrar(RegistroForm form) {
        return usuarioRepository.save(Usuario.builder()
                .nombre(form.getNombre().trim())
                .email(normalizar(form.getEmail()))
                .password(passwordEncoder.encode(form.getPassword()))
                .rol(Rol.CLIENTE)
                .build());
    }

    public Usuario porEmail(String email) {
        return usuarioRepository.findByEmail(normalizar(email))
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado: " + email));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}
