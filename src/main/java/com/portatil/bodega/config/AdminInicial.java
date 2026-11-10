package com.portatil.bodega.config;

import com.portatil.bodega.model.Rol;
import com.portatil.bodega.model.Usuario;
import com.portatil.bodega.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea el usuario administrador al arrancar, si no existe.
 * Se lee de application-dev.properties (desarrollo) o de las variables ADMIN_EMAIL y
 * ADMIN_PASSWORD (producción). Si el correo o la clave están vacíos, no crea nada.
 * Se hace por código (y no en Flyway) para que la clave se cifre con BCrypt.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${tienda.admin.nombre:Administrador}")
    private String nombre;

    @Value("${tienda.admin.email:}")
    private String email;

    @Value("${tienda.admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            log.info("No se configuró administrador inicial (tienda.admin.email / tienda.admin.password).");
            return;
        }
        String correo = email.trim().toLowerCase();
        if (usuarioRepository.existsByEmail(correo)) {
            return;
        }
        usuarioRepository.save(Usuario.builder()
                .nombre(nombre)
                .email(correo)
                .password(passwordEncoder.encode(password))
                .rol(Rol.ADMIN)
                .build());
        log.info("Administrador inicial creado: {}", correo);
    }
}
