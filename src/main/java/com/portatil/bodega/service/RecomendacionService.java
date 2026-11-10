package com.portatil.bodega.service;

import com.portatil.bodega.model.Producto;
import com.portatil.bodega.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Calcula "Productos similares" y "También te puede gustar".
 * Funciona con puntajes simples (categoría, tipo de piel, precio, marca y etiquetas).
 * Con un catálogo pequeño o mediano es suficiente; si crece mucho, se mueve a consultas SQL.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecomendacionService {

    private static final int MAXIMO = 4;
    private static final String TODO_TIPO = "Todo tipo";

    private final ProductoRepository productoRepository;

    /** Misma categoría; prioriza mismo tipo de piel, precio parecido y marcas alternativas. */
    public List<Producto> similares(Producto base) {
        Long id = base.getId();
        Long categoriaId = base.getCategoria().getId();

        Specification<Producto> spec = (root, query, cb) -> cb.and(
                cb.notEqual(root.get("id"), id),
                cb.equal(root.get("categoria").get("id"), categoriaId));

        return productoRepository.findAll(spec).stream()
                .sorted(Comparator.comparingInt((Producto p) -> puntosSimilar(base, p)).reversed()
                        .thenComparing(Producto::getNombre))
                .limit(MAXIMO)
                .toList();
    }

    /** Cualquier categoría; prioriza la misma marca, tipo de piel, precio y etiquetas en común. */
    public List<Producto> tambienTeGustara(Producto base, List<Producto> yaMostrados) {
        Long id = base.getId();
        Set<Long> excluidos = yaMostrados.stream().map(Producto::getId).collect(Collectors.toCollection(HashSet::new));
        excluidos.add(id);

        return productoRepository.findAll().stream()
                .filter(p -> !excluidos.contains(p.getId()))
                .sorted(Comparator.comparingInt((Producto p) -> puntosGusto(base, p)).reversed()
                        .thenComparing(Producto::getNombre))
                .limit(MAXIMO)
                .toList();
    }

    // ---------- Puntajes ----------

    private int puntosSimilar(Producto base, Producto otro) {
        int puntos = 0;
        if (mismoTipoPiel(base, otro)) {
            puntos += 3;
        } else if (esTodoTipo(base) || esTodoTipo(otro)) {
            puntos += 1;
        }
        if (precioCercano(base, otro, 0.40)) {
            puntos += 2;
        }
        if (!base.getMarca().getId().equals(otro.getMarca().getId())) {
            puntos += 1; // marca alternativa
        }
        return puntos + 2 * etiquetasEnComun(base, otro);
    }

    private int puntosGusto(Producto base, Producto otro) {
        int puntos = 0;
        if (base.getMarca().getId().equals(otro.getMarca().getId())) {
            puntos += 3;
        }
        if (mismoTipoPiel(base, otro)) {
            puntos += 2;
        }
        if (precioCercano(base, otro, 0.30)) {
            puntos += 2;
        }
        if (otro.isMasVendido()) {
            puntos += 1;
        }
        return puntos + etiquetasEnComun(base, otro);
    }

    private boolean mismoTipoPiel(Producto a, Producto b) {
        return a.getTipoPiel() != null && a.getTipoPiel().equalsIgnoreCase(b.getTipoPiel());
    }

    private boolean esTodoTipo(Producto p) {
        return TODO_TIPO.equalsIgnoreCase(p.getTipoPiel());
    }

    /** true si el precio de "otro" está dentro de ±tolerancia del precio de "base". */
    private boolean precioCercano(Producto base, Producto otro, double tolerancia) {
        BigDecimal minimo = base.getPrecio().multiply(BigDecimal.valueOf(1 - tolerancia));
        BigDecimal maximo = base.getPrecio().multiply(BigDecimal.valueOf(1 + tolerancia));
        return otro.getPrecio().compareTo(minimo) >= 0 && otro.getPrecio().compareTo(maximo) <= 0;
    }

    private int etiquetasEnComun(Producto a, Producto b) {
        Set<String> etiquetasA = a.getEtiquetasLista().stream()
                .map(String::toLowerCase).collect(Collectors.toSet());
        return (int) b.getEtiquetasLista().stream()
                .map(String::toLowerCase).filter(etiquetasA::contains).count();
    }
}
