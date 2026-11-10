package com.portatil.bodega.service;

import com.portatil.bodega.config.CarritoSesion;
import com.portatil.bodega.dto.LineaCarrito;
import com.portatil.bodega.dto.ResumenCarrito;
import com.portatil.bodega.model.Producto;
import com.portatil.bodega.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Lógica del carrito y de la lista de guardados.
 * Regla: un producto está en UNA sola lista a la vez (carrito o guardados).
 * Los métodos que cambian algo devuelven un mensaje corto para mostrar al cliente.
 */
@Service
@RequiredArgsConstructor
public class CarritoService {

    private final CarritoSesion sesion;
    private final ProductoRepository productoRepository;

    // Se pueden cambiar en application.properties (tienda.envio.costo, tienda.envio.gratis-desde).
    @Value("${tienda.envio.costo:12900}")
    private BigDecimal costoEnvio;

    @Value("${tienda.envio.gratis-desde:150000}")
    private BigDecimal envioGratisDesde;

    // ---------- Carrito ----------

    public String agregar(Long productoId, int cantidad) {
        Producto p = buscar(productoId);
        if (p.getStock() <= 0) {
            return p.getNombre() + " está agotado.";
        }
        sesion.getGuardados().remove(productoId);

        int actual = sesion.getCarrito().getOrDefault(productoId, 0);
        int deseada = actual + Math.max(1, cantidad);
        int nueva = Math.min(deseada, p.getStock());

        if (nueva == actual) {
            return "Ya tienes en el carrito todas las unidades disponibles de " + p.getNombre() + ".";
        }
        sesion.getCarrito().put(productoId, nueva);
        if (nueva < deseada) {
            return "Solo hay " + p.getStock() + " unidades disponibles de " + p.getNombre() + ".";
        }
        return "Agregado al carrito: " + p.getNombre();
    }

    public String actualizar(Long productoId, int cantidad) {
        if (!sesion.getCarrito().containsKey(productoId)) {
            return "Ese producto no está en tu carrito.";
        }
        if (cantidad <= 0) {
            return quitar(productoId);
        }
        Producto p = buscar(productoId);
        if (p.getStock() <= 0) {
            sesion.getCarrito().remove(productoId);
            return p.getNombre() + " se agotó y lo quitamos del carrito.";
        }
        int nueva = Math.min(cantidad, p.getStock());
        sesion.getCarrito().put(productoId, nueva);
        return nueva < cantidad
                ? "Solo hay " + p.getStock() + " unidades disponibles de " + p.getNombre() + "."
                : "Cantidad actualizada.";
    }

    public String quitar(Long productoId) {
        sesion.getCarrito().remove(productoId);
        return "Producto eliminado del carrito.";
    }

    public String vaciar() {
        sesion.getCarrito().clear();
        return "Carrito vaciado.";
    }

    // ---------- Guardados ----------

    /** Mueve un producto del carrito a "comprar después". */
    public String guardarParaDespues(Long productoId) {
        Producto p = buscar(productoId);
        sesion.getCarrito().remove(productoId);
        sesion.getGuardados().add(productoId);
        return "Guardado para después: " + p.getNombre();
    }

    /** Guarda un producto (por ejemplo desde su ficha). Si estaba en el carrito, sale de ahí. */
    public String agregarAGuardados(Long productoId) {
        return guardarParaDespues(productoId);
    }

    public String quitarGuardado(Long productoId) {
        sesion.getGuardados().remove(productoId);
        return "Producto eliminado de guardados.";
    }

    // ---------- Consultas ----------

    public int cantidadCarrito() {
        return sesion.getCarrito().values().stream().mapToInt(Integer::intValue).sum();
    }

    public int cantidadGuardados() {
        return sesion.getGuardados().size();
    }

    public List<Producto> guardados() {
        Set<Long> ids = sesion.getGuardados();
        if (ids.isEmpty()) {
            return List.of();
        }
        Map<Long, Producto> productos = productoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        ids.removeIf(id -> !productos.containsKey(id)); // por si el admin borró un producto
        return ids.stream().map(productos::get).toList();
    }

    public ResumenCarrito resumen() {
        Map<Long, Integer> carrito = sesion.getCarrito();
        List<LineaCarrito> lineas = new ArrayList<>();

        if (!carrito.isEmpty()) {
            Map<Long, Producto> productos = productoRepository.findAllById(carrito.keySet()).stream()
                    .collect(Collectors.toMap(Producto::getId, Function.identity()));
            carrito.keySet().removeIf(id -> !productos.containsKey(id));
            for (Map.Entry<Long, Integer> e : carrito.entrySet()) {
                lineas.add(new LineaCarrito(productos.get(e.getKey()), e.getValue()));
            }
        }

        BigDecimal subtotal = lineas.stream()
                .map(LineaCarrito::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean hayProductos = !lineas.isEmpty();
        boolean envioGratis = hayProductos && subtotal.compareTo(envioGratisDesde) >= 0;
        BigDecimal envio = (!hayProductos || envioGratis) ? BigDecimal.ZERO : costoEnvio;
        BigDecimal falta = (hayProductos && !envioGratis) ? envioGratisDesde.subtract(subtotal) : BigDecimal.ZERO;

        return new ResumenCarrito(lineas, cantidadCarrito(), subtotal, envio, subtotal.add(envio), envioGratis, falta);
    }

    private Producto buscar(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }
}
