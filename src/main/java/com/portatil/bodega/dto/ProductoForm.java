package com.portatil.bodega.dto;

import com.portatil.bodega.model.Producto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Formulario del panel de administración para crear o editar un producto. */
@Getter @Setter @NoArgsConstructor
public class ProductoForm {

    /** null = producto nuevo. Se llena al editar (solo para mostrar el título). */
    private Long id;

    @NotBlank(message = "Escribe el nombre del producto")
    @Size(max = 200, message = "Máximo 200 caracteres")
    private String nombre;

    @NotNull(message = "Selecciona la marca")
    private Long marcaId;

    @NotNull(message = "Selecciona la categoría")
    private Long categoriaId;

    @NotNull(message = "Escribe el precio")
    @DecimalMin(value = "1", message = "El precio debe ser mayor que cero")
    @Digits(integer = 10, fraction = 2, message = "Precio no válido")
    private BigDecimal precio;

    @NotNull(message = "Escribe el stock (0 si está agotado)")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 1000000, message = "Stock demasiado alto")
    private Integer stock;

    @NotBlank(message = "Escribe la descripción")
    @Size(max = 2000, message = "Máximo 2000 caracteres")
    private String descripcion;

    @Size(max = 2000, message = "Máximo 2000 caracteres")
    private String ingredientes;

    @Size(max = 1000, message = "Máximo 1000 caracteres")
    private String modoUso;

    /** URL de la imagen (https://...) o ruta local (/img/...). Vacío = "imagen pendiente". */
    @Size(max = 500, message = "Máximo 500 caracteres")
    @Pattern(regexp = "^(https?://|/).+", message = "Debe empezar por http://, https:// o /")
    private String urlImagen;

    /** De dónde salió la imagen (catálogo oficial, distribuidor autorizado, etc.). */
    @Size(max = 300, message = "Máximo 300 caracteres")
    private String fuenteImagen;

    @Size(max = 50, message = "Máximo 50 caracteres")
    private String tipoPiel;

    /** Separadas por coma: "labios,mate,larga duración". */
    @Size(max = 300, message = "Máximo 300 caracteres")
    private String etiquetas;

    private boolean masVendido;

    /** Carga el formulario con los datos de un producto existente. */
    public static ProductoForm desde(Producto p) {
        ProductoForm f = new ProductoForm();
        f.setId(p.getId());
        f.setNombre(p.getNombre());
        f.setMarcaId(p.getMarca().getId());
        f.setCategoriaId(p.getCategoria().getId());
        f.setPrecio(p.getPrecio());
        f.setStock(p.getStock());
        f.setDescripcion(p.getDescripcion());
        f.setIngredientes(p.getIngredientes());
        f.setModoUso(p.getModoUso());
        f.setUrlImagen(p.getUrlImagen());
        f.setFuenteImagen(p.getFuenteImagen());
        f.setTipoPiel(p.getTipoPiel());
        f.setEtiquetas(p.getEtiquetas());
        f.setMasVendido(p.isMasVendido());
        return f;
    }
}
