package com.portatil.bodega.service;

import com.portatil.bodega.dto.CheckoutForm;
import com.portatil.bodega.dto.LineaCarrito;
import com.portatil.bodega.dto.ResumenCarrito;
import com.portatil.bodega.model.ItemPedido;
import com.portatil.bodega.model.Pedido;
import com.portatil.bodega.model.Producto;
import com.portatil.bodega.model.Usuario;
import com.portatil.bodega.repository.PedidoRepository;
import com.portatil.bodega.repository.ProductoRepository;
import com.portatil.bodega.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final CarritoService carritoService;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Convierte el carrito actual en un pedido, todo en una sola transacción:
     * bloquea los productos, verifica el stock, lo descuenta y guarda el pedido.
     * Si algo falla, no se guarda nada (rollback).
     * No cobra nada: el método de pago es solo informativo.
     */
    @Transactional
    public Pedido crearPedido(Usuario usuario, CheckoutForm form) {
        ResumenCarrito resumen = carritoService.resumen();
        if (resumen.getLineas().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }

        List<Long> ids = resumen.getLineas().stream().map(l -> l.getProducto().getId()).toList();
        Map<Long, Producto> bloqueados = productoRepository.findAllByIdForUpdate(ids).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuarioRepository.getReferenceById(usuario.getId()));
        pedido.setFecha(LocalDateTime.now());
        pedido.setNombreDestinatario(form.getNombreDestinatario().trim());
        pedido.setTelefono(form.getTelefono().trim());
        pedido.setDepartamento(form.getDepartamento());
        pedido.setCiudad(form.getCiudad().trim());
        pedido.setDireccion(form.getDireccion().trim());
        pedido.setMetodoPago(form.getMetodoPago());
        pedido.setEstado("PENDIENTE");

        BigDecimal subtotal = BigDecimal.ZERO;
        for (LineaCarrito linea : resumen.getLineas()) {
            Producto p = bloqueados.get(linea.getProducto().getId());
            if (p == null || p.getStock() < linea.getCantidad()) {
                String nombre = linea.getProducto().getNombre();
                throw new StockInsuficienteException(
                        "Ya no hay suficientes unidades de " + nombre + ". Revisa las cantidades de tu carrito.");
            }
            p.setStock(p.getStock() - linea.getCantidad());

            ItemPedido item = new ItemPedido();
            item.setProducto(p);
            item.setNombreProducto(p.getNombre());
            item.setPrecioUnitario(p.getPrecio()); // precio del momento de la compra
            item.setCantidad(linea.getCantidad());
            pedido.agregarItem(item);

            subtotal = subtotal.add(item.getSubtotal());
        }

        pedido.setSubtotal(subtotal);
        pedido.setEnvio(resumen.getEnvio());
        pedido.setTotal(subtotal.add(resumen.getEnvio()));
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<Pedido> misPedidos(Usuario usuario) {
        List<Pedido> pedidos = pedidoRepository.findByUsuarioOrderByFechaDesc(usuario);
        pedidos.forEach(p -> p.getItems().size()); // carga los ítems dentro de la transacción
        return pedidos;
    }

    /** Devuelve el pedido solo si pertenece al usuario o si el usuario es administrador. */
    @Transactional(readOnly = true)
    public Optional<Pedido> buscarVisible(Long id, Usuario usuario) {
        return pedidoRepository.findById(id)
                .filter(p -> p.getUsuario().getId().equals(usuario.getId())
                        || usuario.getRol() == com.portatil.bodega.model.Rol.ADMIN)
                .map(p -> {
                    p.getItems().size();
                    return p;
                });
    }
}
