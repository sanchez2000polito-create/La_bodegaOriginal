package com.portatil.bodega.controller;

import com.portatil.bodega.dto.CheckoutForm;
import com.portatil.bodega.dto.Departamentos;
import com.portatil.bodega.dto.ResumenCarrito;
import com.portatil.bodega.model.Pedido;
import com.portatil.bodega.model.Usuario;
import com.portatil.bodega.service.CarritoService;
import com.portatil.bodega.service.PedidoService;
import com.portatil.bodega.service.StockInsuficienteException;
import com.portatil.bodega.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Todo /checkout/** exige sesión iniciada (ver SecurityConfig). */
@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CarritoService carrito;
    private final UsuarioService usuarios;
    private final PedidoService pedidos;

    @GetMapping
    public String formulario(Principal principal, Model model, RedirectAttributes ra) {
        ResumenCarrito resumen = carrito.resumen();
        if (resumen.getLineas().isEmpty()) {
            ra.addFlashAttribute("mensaje", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }
        Usuario usuario = usuarios.porEmail(principal.getName());

        CheckoutForm form = new CheckoutForm();
        form.setNombreDestinatario(usuario.getNombre());
        form.setMetodoPago("PSE");

        model.addAttribute("checkoutForm", form);
        cargarDatos(model, resumen);
        return "checkout";
    }

    @PostMapping
    public String confirmar(@Valid @ModelAttribute("checkoutForm") CheckoutForm form, BindingResult result,
                            Principal principal, Model model, RedirectAttributes ra) {
        ResumenCarrito resumen = carrito.resumen();
        if (resumen.getLineas().isEmpty()) {
            ra.addFlashAttribute("mensaje", "Tu carrito está vacío.");
            return "redirect:/carrito";
        }
        if (!result.hasFieldErrors("departamento") && !Departamentos.TODOS.contains(form.getDepartamento())) {
            result.rejectValue("departamento", "invalido", "Selecciona un departamento de la lista");
        }
        if (result.hasErrors()) {
            cargarDatos(model, resumen);
            return "checkout";
        }

        try {
            Pedido pedido = pedidos.crearPedido(usuarios.porEmail(principal.getName()), form);
            carrito.vaciar();
            return "redirect:/checkout/confirmacion/" + pedido.getId();
        } catch (StockInsuficienteException e) {
            ra.addFlashAttribute("mensaje", e.getMessage());
            return "redirect:/carrito";
        }
    }

    @GetMapping("/confirmacion/{id}")
    public String confirmacion(@PathVariable Long id, Principal principal, Model model) {
        Usuario usuario = usuarios.porEmail(principal.getName());
        // Si el pedido no es suyo se responde 404 (no se revela que existe).
        Pedido pedido = pedidos.buscarVisible(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        model.addAttribute("pedido", pedido);
        return "confirmacion";
    }

    private void cargarDatos(Model model, ResumenCarrito resumen) {
        model.addAttribute("resumen", resumen);
        model.addAttribute("departamentos", Departamentos.TODOS);
    }
}
