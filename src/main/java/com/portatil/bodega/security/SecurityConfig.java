package com.portatil.bodega.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * Configuración real de seguridad (reemplaza a la temporal de la etapa 2).
 * - Público: tienda, catálogo, fichas, carrito, guardados, login y registro.
 * - Con sesión iniciada: checkout y "Mis pedidos".
 * - Solo ADMIN: todo lo que empiece por /admin (panel de la etapa 6).
 *
 * Compatible con Spring Security 6.5 y 7 (Spring Boot 3.5 y 4).
 */
@Configuration
public class SecurityConfig {

    /** Las contraseñas se guardan siempre cifradas con BCrypt. */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder ruta = PathPatternRequestMatcher.withDefaults();

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(ruta.matcher("/admin/**")).hasRole("ADMIN")
                        .requestMatchers(
                                ruta.matcher("/checkout/**"),
                                ruta.matcher("/mis-pedidos/**")).authenticated()
                        .anyRequest().permitAll())
                .formLogin(form -> form
                        .loginPage("/login")
                        // false = si el cliente venía de /checkout, vuelve allí después de ingresar
                        .defaultSuccessUrl("/", false)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?salio")
                        .permitAll());
        return http.build();
    }
}
