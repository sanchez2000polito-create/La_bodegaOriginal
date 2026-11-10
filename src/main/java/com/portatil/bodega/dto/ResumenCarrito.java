package com.portatil.bodega.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

/** Todo lo que necesita la página del carrito (y luego el checkout). */
@Getter
@AllArgsConstructor
public class ResumenCarrito {

    private final List<LineaCarrito> lineas;
    private final int cantidadTotal;
    private final BigDecimal subtotal;
    private final BigDecimal envio;
    private final BigDecimal total;
    private final boolean envioGratis;
    /** Cuánto falta para llegar al envío gratis (0 si ya lo tiene o el carrito está vacío). */
    private final BigDecimal faltaParaEnvioGratis;
}
