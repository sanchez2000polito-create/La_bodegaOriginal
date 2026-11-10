package com.portatil.bodega.repository;

import com.portatil.bodega.model.Producto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

// JpaSpecificationExecutor se usa en el catálogo para los filtros combinados.
public interface ProductoRepository
        extends JpaRepository<Producto, Long>, JpaSpecificationExecutor<Producto> {

    List<Producto> findByMasVendidoTrue();

    List<Producto> findTop8ByOrderByFechaActualizacionDescIdDesc();

    @Query("select distinct p.tipoPiel from Producto p where p.tipoPiel is not null order by p.tipoPiel")
    List<String> findTiposPiel();

    /**
     * Trae los productos bloqueándolos hasta el fin de la transacción (SELECT ... FOR UPDATE).
     * Se usa al confirmar un pedido para que dos clientes no compren la misma última unidad.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids order by p.id")
    List<Producto> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
