package com.portatil.bodega.repository;

import com.portatil.bodega.model.Pedido;
import com.portatil.bodega.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioOrderByFechaDesc(Usuario usuario);
}
