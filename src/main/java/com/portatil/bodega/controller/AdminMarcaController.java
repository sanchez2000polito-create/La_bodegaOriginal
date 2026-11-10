package com.portatil.bodega.controller;

import com.portatil.bodega.dto.MarcaForm;
import com.portatil.bodega.model.Marca;
import com.portatil.bodega.service.AdminCatalogoService;
import com.portatil.bodega.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Panel de administración: marcas. */
@Controller
@RequestMapping("/admin/marcas")
@RequiredArgsConstructor
public class AdminMarcaController {

    private final CatalogoService catalogo;
    private final AdminCatalogoService admin;

    @InitBinder
    public void limpiarTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("marcas", catalogo.marcas());
        model.addAttribute("conteo", admin.productosPorMarca());
        return "admin/marcas";
    }

    @GetMapping("/nueva")
    public String nueva(Model model) {
        model.addAttribute("marcaForm", new MarcaForm());
        model.addAttribute("accion", "/admin/marcas");
        return "admin/marca-form";
    }

    @PostMapping
    public String crear(@Valid @ModelAttribute("marcaForm") MarcaForm form, BindingResult result,
                        Model model, RedirectAttributes ra) {
        if (!result.hasFieldErrors("nombre") && admin.nombreMarcaEnUso(form.getNombre(), null)) {
            result.rejectValue("nombre", "duplicada", "Ya existe una marca con ese nombre");
        }
        if (result.hasErrors()) {
            model.addAttribute("accion", "/admin/marcas");
            return "admin/marca-form";
        }
        Marca m = admin.crearMarca(form.getNombre());
        ra.addFlashAttribute("mensaje", "Marca creada: " + m.getNombre());
        return "redirect:/admin/marcas";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Marca m = admin.marca(id);
        MarcaForm form = new MarcaForm();
        form.setId(m.getId());
        form.setNombre(m.getNombre());
        model.addAttribute("marcaForm", form);
        model.addAttribute("accion", "/admin/marcas/" + id);
        return "admin/marca-form";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id, @Valid @ModelAttribute("marcaForm") MarcaForm form,
                             BindingResult result, Model model, RedirectAttributes ra) {
        form.setId(id);
        if (!result.hasFieldErrors("nombre") && admin.nombreMarcaEnUso(form.getNombre(), id)) {
            result.rejectValue("nombre", "duplicada", "Ya existe una marca con ese nombre");
        }
        if (result.hasErrors()) {
            model.addAttribute("accion", "/admin/marcas/" + id);
            return "admin/marca-form";
        }
        Marca m = admin.renombrarMarca(id, form.getNombre());
        ra.addFlashAttribute("mensaje", "Marca actualizada: " + m.getNombre());
        return "redirect:/admin/marcas";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String nombre = admin.eliminarMarca(id);
            ra.addFlashAttribute("mensaje", "Marca eliminada: " + nombre);
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("mensaje",
                    "No se puede eliminar: la marca todavía tiene productos. Elimínalos o cámbiales la marca primero.");
        }
        return "redirect:/admin/marcas";
    }
}
