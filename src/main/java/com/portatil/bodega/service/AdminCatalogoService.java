package com.portatil.bodega.service;

import com.portatil.bodega.dto.ProductoForm;
import com.portatil.bodega.model.Marca;
import com.portatil.bodega.model.Producto;
import com.portatil.bodega.repository.CategoriaRepository;
import com.portatil.bodega.repository.MarcaRepository;
import com.portatil.bodega.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/** Crear, editar y eliminar productos y marcas desde el panel de administración. */
@Service
@RequiredArgsConstructor
public class AdminCatalogoService {

    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    // ---------- Productos ----------

    @Transactional
    public Producto crearProducto(ProductoForm form) {
        Producto p = new Producto();
        aplicar(p, form);
        return productoRepository.save(p);
    }

    @Transactional
    public Producto actualizarProducto(Long id, ProductoForm form) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        aplicar(p, form);
        return productoRepository.save(p);
    }

    /**
     * Elimina el producto y devuelve su nombre.
     * Si ya tiene pedidos asociados, la base de datos lo impide y se lanza
     * DataIntegrityViolationException (el controlador la convierte en un aviso).
     */
    @Transactional
    public String eliminarProducto(Long id) {
        Producto p = productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        productoRepository.delete(p);
        productoRepository.flush(); // fuerza el error aquí si hay pedidos asociados
        return p.getNombre();
    }

    private void aplicar(Producto p, ProductoForm f) {
        p.setNombre(f.getNombre());
        p.setDescripcion(f.getDescripcion());
        p.setIngredientes(f.getIngredientes());
        p.setModoUso(f.getModoUso());
        p.setPrecio(f.getPrecio().setScale(2, RoundingMode.HALF_UP));
        p.setStock(f.getStock());
        p.setUrlImagen(f.getUrlImagen());
        p.setFuenteImagen(f.getFuenteImagen());
        p.setTipoPiel(f.getTipoPiel());
        p.setEtiquetas(normalizarEtiquetas(f.getEtiquetas()));
        p.setMasVendido(f.isMasVendido());
        p.setFechaActualizacion(LocalDate.now());
        p.setMarca(marcaRepository.findById(f.getMarcaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca no válida")));
        p.setCategoria(categoriaRepository.findById(f.getCategoriaId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría no válida")));
    }

    /** "labios , mate,,larga duración" -> "labios,mate,larga duración" */
    private String normalizarEtiquetas(String etiquetas) {
        if (etiquetas == null || etiquetas.isBlank()) {
            return null;
        }
        return Arrays.stream(etiquetas.split(","))
                .map(String::trim)
                .filter(e -> !e.isEmpty())
                .collect(Collectors.joining(","));
    }

    // ---------- Marcas ----------

    @Transactional
    public Marca crearMarca(String nombre) {
        Marca m = new Marca();
        m.setNombre(nombre.trim());
        return marcaRepository.save(m);
    }

    @Transactional
    public Marca renombrarMarca(Long id, String nombre) {
        Marca m = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
        m.setNombre(nombre.trim());
        return marcaRepository.save(m);
    }

    /** Si la marca tiene productos, la base de datos lo impide (DataIntegrityViolationException). */
    @Transactional
    public String eliminarMarca(Long id) {
        Marca m = marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
        marcaRepository.delete(m);
        marcaRepository.flush();
        return m.getNombre();
    }

    /** true si ya existe otra marca con ese nombre (sin distinguir mayúsculas). */
    public boolean nombreMarcaEnUso(String nombre, Long idActual) {
        return marcaRepository.findAll().stream()
                .anyMatch(m -> m.getNombre().equalsIgnoreCase(nombre.trim()) && !m.getId().equals(idActual));
    }

    public Marca marca(Long id) {
        return marcaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
    }

    /** Cantidad de productos por marca (id de marca -> cantidad). */
    public Map<Long, Long> productosPorMarca() {
        return productoRepository.findAll().stream()
                .collect(Collectors.groupingBy(p -> p.getMarca().getId(), Collectors.counting()));
    }
}
