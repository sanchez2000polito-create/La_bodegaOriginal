package com.portatil.bodega.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Parámetros GET del catálogo: /catalogo?q=labial&marcaId=3&orden=precio_asc
 * Los campos vacíos llegan como null (números) o "" (texto).
 */
@Getter @Setter @NoArgsConstructor
public class FiltroCatalogo {

    /** Texto de búsqueda (nombre del producto o de la marca). */
    private String q;
    private Long marcaId;
    private Long categoriaId;
    private BigDecimal precioMin;
    private BigDecimal precioMax;
    private String tipoPiel;

    /** precio_asc, precio_desc, novedades, nombre. Vacío = más vendidos primero. */
    private String orden;
}
