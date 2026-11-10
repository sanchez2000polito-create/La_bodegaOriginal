package com.portatil.bodega.controller;

import com.portatil.bodega.dto.FiltroCatalogo;
import com.portatil.bodega.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService catalogo;

    @GetMapping("/catalogo")
    public String catalogo(@ModelAttribute("filtro") FiltroCatalogo filtro, Model model) {
        model.addAttribute("productos", catalogo.buscar(filtro));
        model.addAttribute("marcas", catalogo.marcas());
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("tiposPiel", catalogo.tiposPiel());
        return "catalogo";
    }
}
