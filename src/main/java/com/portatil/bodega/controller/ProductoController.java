package com.portatil.bodega.controller;

import com.portatil.bodega.model.Producto;
import com.portatil.bodega.service.CatalogoService;
import com.portatil.bodega.service.RecomendacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductoController {

    private final CatalogoService catalogo;
    private final RecomendacionService recomendaciones;

    @GetMapping("/producto/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Producto producto = catalogo.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

        List<Producto> similares = recomendaciones.similares(producto);
        List<Producto> tambien = recomendaciones.tambienTeGustara(producto, similares);

        model.addAttribute("producto", producto);
        model.addAttribute("similares", similares);
        model.addAttribute("tambien", tambien);
        return "producto";
    }
}
