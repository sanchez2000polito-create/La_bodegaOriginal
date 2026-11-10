package com.portatil.bodega.controller;

import com.portatil.bodega.service.CarritoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/guardados")
@RequiredArgsConstructor
public class GuardadosController {

    private final CarritoService carrito;

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("guardados", carrito.guardados());
        return "guardados";
    }

    /** Desde la ficha del producto: guarda y vuelve a la misma página. */
    @PostMapping("/agregar")
    public String agregar(@RequestParam Long productoId, HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.agregarAGuardados(productoId));
        return Navegacion.volverA(request, "/guardados");
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Long productoId, RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.quitarGuardado(productoId));
        return "redirect:/guardados";
    }
}
