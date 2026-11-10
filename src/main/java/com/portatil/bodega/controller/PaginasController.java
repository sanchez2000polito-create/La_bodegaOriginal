package com.portatil.bodega.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;

/** Páginas informativas: contacto, envíos y devoluciones, términos y condiciones. */
@Controller
public class PaginasController {

    // Los mismos valores del carrito (application-*.properties), para que el texto no se desactualice.
    @Value("${tienda.envio.costo:12900}")
    private BigDecimal costoEnvio;

    @Value("${tienda.envio.gratis-desde:150000}")
    private BigDecimal envioGratisDesde;

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }

    @GetMapping("/envios")
    public String envios(Model model) {
        model.addAttribute("costoEnvio", costoEnvio);
        model.addAttribute("envioGratisDesde", envioGratisDesde);
        return "envios";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }
}
