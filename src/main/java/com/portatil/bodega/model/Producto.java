package com.portatil.bodega.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "producto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nombre;

    @Column(length = 2000)
    private String descripcion;

    @Column(length = 2000)
    private String ingredientes;

    @Column(name = "modo_uso", length = 1000)
    private String modoUso;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Integer stock;

    /** URL de la imagen. Si está vacía, la tienda muestra "imagen pendiente". */
    @Column(name = "url_imagen", length = 500)
    private String urlImagen;

    /** De dónde salió la imagen (catálogo oficial, distribuidor, etc.). */
    @Column(name = "fuente_imagen", length = 300)
    private String fuenteImagen;

    @Column(name = "tipo_piel", length = 50)
    private String tipoPiel;

    /** Etiquetas separadas por coma: "labios,mate,larga duración". */
    @Column(length = 300)
    private String etiquetas;

    @Column(name = "mas_vendido", nullable = false)
    private boolean masVendido;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDate fechaActualizacion;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "marca_id")
    private Marca marca;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;

    /** Para las plantillas: true si no hay imagen cargada. */
    @Transient
    public boolean isImagenPendiente() {
        return urlImagen == null || urlImagen.isBlank();
    }

    /** Para las plantillas: las etiquetas como lista. */
    @Transient
    public List<String> getEtiquetasLista() {
        if (etiquetas == null || etiquetas.isBlank()) {
            return List.of();
        }
        return Arrays.stream(etiquetas.split(",")).map(String::trim).toList();
    }
}
