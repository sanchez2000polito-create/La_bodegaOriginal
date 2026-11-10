package com.portatil.bodega.controller;

import com.portatil.bodega.service.CatalogoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class InicioController {

    private final CatalogoService catalogo;

    @GetMapping("/")
    public String inicio(Model model) {
        model.addAttribute("masVendidos", catalogo.masVendidos());
        model.addAttribute("novedades", catalogo.novedades());
        model.addAttribute("marcas", catalogo.marcas());
        model.addAttribute("categorias", catalogo.categorias());
        return "inicio";
    }
}
