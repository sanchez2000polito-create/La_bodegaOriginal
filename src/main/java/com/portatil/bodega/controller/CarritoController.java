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
@RequestMapping("/carrito")
@RequiredArgsConstructor
public class CarritoController {

    private final CarritoService carrito;

    @GetMapping
    public String ver(Model model) {
        model.addAttribute("resumen", carrito.resumen());
        return "carrito";
    }

    /** Desde la tarjeta, la ficha o guardados: vuelve a la misma página con un aviso. */
    @PostMapping("/agregar")
    public String agregar(@RequestParam Long productoId,
                          @RequestParam(defaultValue = "1") int cantidad,
                          HttpServletRequest request, RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.agregar(productoId, cantidad));
        return Navegacion.volverA(request, "/carrito");
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam Long productoId, @RequestParam int cantidad,
                             RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.actualizar(productoId, cantidad));
        return "redirect:/carrito";
    }

    @PostMapping("/eliminar")
    public String eliminar(@RequestParam Long productoId, RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.quitar(productoId));
        return "redirect:/carrito";
    }

    /** Mueve el producto del carrito a "guardados / comprar después". */
    @PostMapping("/guardar")
    public String guardarParaDespues(@RequestParam Long productoId, RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.guardarParaDespues(productoId));
        return "redirect:/carrito";
    }

    @PostMapping("/vaciar")
    public String vaciar(RedirectAttributes ra) {
        ra.addFlashAttribute("mensaje", carrito.vaciar());
        return "redirect:/carrito";
    }
}
