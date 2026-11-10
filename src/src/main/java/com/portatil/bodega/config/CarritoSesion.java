package com.portatil.bodega.config;

import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Estado del cliente guardado en su sesión HTTP (una instancia por visitante).
 * Solo guarda IDs y cantidades, no entidades JPA, para que sea liviano y serializable.
 */
@Component
@SessionScope
@Getter
public class CarritoSesion implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Carrito: id de producto -> cantidad. Mantiene el orden en que se agregó. */
    private final Map<Long, Integer> carrito = new LinkedHashMap<>();

    /** "Guardados / comprar después": ids de producto. */
    private final Set<Long> guardados = new LinkedHashSet<>();
}
