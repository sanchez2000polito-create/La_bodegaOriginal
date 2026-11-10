package com.portatil.bodega.service;

import com.portatil.bodega.dto.FiltroCatalogo;
import com.portatil.bodega.model.Categoria;
import com.portatil.bodega.model.Marca;
import com.portatil.bodega.model.Producto;
import com.portatil.bodega.repository.CategoriaRepository;
import com.portatil.bodega.repository.MarcaRepository;
import com.portatil.bodega.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CatalogoService {

    private final ProductoRepository productoRepository;
    private final MarcaRepository marcaRepository;
    private final CategoriaRepository categoriaRepository;

    /** Búsqueda combinada: cada filtro presente se suma con AND. */
    public List<Producto> buscar(FiltroCatalogo f) {
        Specification<Producto> spec = (root, query, cb) -> cb.conjunction();

        if (StringUtils.hasText(f.getQ())) {
            String patron = "%" + f.getQ().trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("nombre")), patron),
                    cb.like(cb.lower(root.join("marca").get("nombre")), patron)));
        }
        if (f.getMarcaId() != null) {
            Long marcaId = f.getMarcaId();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("marca").get("id"), marcaId));
        }
        if (f.getCategoriaId() != null) {
            Long categoriaId = f.getCategoriaId();
            spec = spec.and((root, query, cb) -> cb.equal(root.get("categoria").get("id"), categoriaId));
        }
        if (f.getPrecioMin() != null) {
            var min = f.getPrecioMin();
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("precio"), min));
        }
        if (f.getPrecioMax() != null) {
            var max = f.getPrecioMax();
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("precio"), max));
        }
        if (StringUtils.hasText(f.getTipoPiel())) {
            String tipo = f.getTipoPiel();
            // Los productos "Todo tipo" sirven para cualquier tipo de piel.
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("tipoPiel"), tipo),
                    cb.equal(root.get("tipoPiel"), "Todo tipo")));
        }

        return productoRepository.findAll(spec, ordenar(f.getOrden()));
    }

    private Sort ordenar(String orden) {
        if (orden == null) {
            orden = "";
        }
        return switch (orden) {
            case "precio_asc" -> Sort.by(Sort.Order.asc("precio"));
            case "precio_desc" -> Sort.by(Sort.Order.desc("precio"));
            case "novedades" -> Sort.by(Sort.Order.desc("fechaActualizacion"), Sort.Order.desc("id"));
            case "nombre" -> Sort.by(Sort.Order.asc("nombre"));
            default -> Sort.by(Sort.Order.desc("masVendido"), Sort.Order.asc("nombre"));
        };
    }

    public List<Producto> masVendidos() {
        return productoRepository.findByMasVendidoTrue().stream().limit(8).toList();
    }

    public List<Producto> novedades() {
        return productoRepository.findTop8ByOrderByFechaActualizacionDescIdDesc();
    }

    public List<Marca> marcas() {
        return marcaRepository.findAllByOrderByNombreAsc();
    }

    public List<Categoria> categorias() {
        return categoriaRepository.findAllByOrderByNombreAsc();
    }

    public List<String> tiposPiel() {
        return productoRepository.findTiposPiel();
    }

    /** Se usa en la etapa 3 (ficha de producto). */
    public Optional<Producto> obtener(Long id) {
        return productoRepository.findById(id);
    }
}
