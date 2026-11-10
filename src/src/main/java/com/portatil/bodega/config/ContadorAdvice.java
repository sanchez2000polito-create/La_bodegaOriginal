package com.portatil.bodega.config;

import com.portatil.bodega.service.CarritoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Pone los contadores del encabezado (carrito y guardados) en todas las páginas. */
@ControllerAdvice
@RequiredArgsConstructor
public class ContadorAdvice {

    private final CarritoService carritoService;

    @ModelAttribute("carritoCantidad")
    public int carritoCantidad() {
        return carritoService.cantidadCarrito();
    }

    @ModelAttribute("guardadosCantidad")
    public int guardadosCantidad() {
        return carritoService.cantidadGuardados();
    }
}
