package com.portatil.bodega.controller;

import com.portatil.bodega.dto.RegistroForm;
import com.portatil.bodega.model.Usuario;
import com.portatil.bodega.service.PedidoService;
import com.portatil.bodega.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;

/** Login, registro y "Mis pedidos". El POST de /login y /logout los maneja Spring Security. */
@Controller
@RequiredArgsConstructor
public class CuentaController {

    private final UsuarioService usuarios;
    private final PedidoService pedidos;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("registroForm", new RegistroForm());
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("registroForm") RegistroForm form, BindingResult result) {
        if (!result.hasFieldErrors("password") && !result.hasFieldErrors("confirmar")
                && !form.getPassword().equals(form.getConfirmar())) {
            result.rejectValue("confirmar", "noCoincide", "Las contraseñas no coinciden");
        }
        if (!result.hasFieldErrors("email") && usuarios.existeEmail(form.getEmail())) {
            result.rejectValue("email", "duplicado", "Ya existe una cuenta con ese correo");
        }
        if (result.hasErrors()) {
            return "registro";
        }
        usuarios.registrar(form);
        return "redirect:/login?registrado";
    }

    @GetMapping("/mis-pedidos")
    public String misPedidos(Principal principal, Model model) {
        Usuario usuario = usuarios.porEmail(principal.getName());
        model.addAttribute("pedidos", pedidos.misPedidos(usuario));
        return "mis-pedidos";
    }
}
