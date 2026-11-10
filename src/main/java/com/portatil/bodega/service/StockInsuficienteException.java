package com.portatil.bodega.service;

/** Se lanza cuando al confirmar el pedido ya no hay unidades suficientes de un producto. */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
