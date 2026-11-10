package com.portatil.bodega.dto;

import com.portatil.bodega.model.Producto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/** Una fila del carrito: producto + cantidad. */
@Getter
@AllArgsConstructor
public class LineaCarrito {

    private final Producto producto;
    private final int cantidad;

    public BigDecimal getSubtotal() {
        return producto.getPrecio().multiply(BigDecimal.valueOf(cantidad));
    }
}
