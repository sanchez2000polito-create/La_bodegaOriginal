package com.portatil.bodega.controller;

import com.portatil.bodega.dto.FiltroCatalogo;
import com.portatil.bodega.dto.ProductoForm;
import com.portatil.bodega.model.Producto;
import com.portatil.bodega.service.AdminCatalogoService;
import com.portatil.bodega.service.CatalogoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Panel de administración: productos. Todo /admin/** exige rol ADMIN (ver SecurityConfig). */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductoController {

    private final CatalogoService catalogo;
    private final AdminCatalogoService admin;

    /** Quita espacios y convierte los textos vacíos en null (así "sin imagen" funciona bien). */
    @InitBinder
    public void limpiarTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String inicio() {
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos")
    public String listar(@RequestParam(required = false) String q,
                         @RequestParam(defaultValue = "false") boolean sinImagen,
                         Model model) {
        FiltroCatalogo filtro = new FiltroCatalogo();
        filtro.setQ(q);
        filtro.setOrden("nombre");

        List<Producto> productos = catalogo.buscar(filtro);
        if (sinImagen) {
            productos = productos.stream().filter(Producto::isImagenPendiente).toList();
        }
        long pendientes = catalogo.buscar(new FiltroCatalogo()).stream()
                .filter(Producto::isImagenPendiente).count();

        model.addAttribute("productos", productos);
        model.addAttribute("q", q);
        model.addAttribute("sinImagen", sinImagen);
        model.addAttribute("pendientes", pendientes);
        return "admin/productos";
    }

    @GetMapping("/productos/nuevo")
    public String nuevo(Model model) {
        ProductoForm form = new ProductoForm();
        form.setStock(0);
        model.addAttribute("productoForm", form);
        cargarCombos(model, "/admin/productos");
        return "admin/producto-form";
    }

    @PostMapping("/productos")
    public String crear(@Valid @ModelAttribute("productoForm") ProductoForm form, BindingResult result,
                        Model model, RedirectAttributes ra) {
        validarFuenteImagen(form, result);
        if (result.hasErrors()) {
            cargarCombos(model, "/admin/productos");
            return "admin/producto-form";
        }
        Producto p = admin.crearProducto(form);
        ra.addFlashAttribute("mensaje", "Producto creado: " + p.getNombre());
        return "redirect:/admin/productos";
    }

    @GetMapping("/productos/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Producto p = catalogo.obtener(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        model.addAttribute("productoForm", ProductoForm.desde(p));
        cargarCombos(model, "/admin/productos/" + id);
        return "admin/producto-form";
    }

    @PostMapping("/productos/{id}")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("productoForm") ProductoForm form, BindingResult result,
                             Model model, RedirectAttributes ra) {
        form.setId(id);
        validarFuenteImagen(form, result);
        if (result.hasErrors()) {
            cargarCombos(model, "/admin/productos/" + id);
            return "admin/producto-form";
        }
        Producto p = admin.actualizarProducto(id, form);
        ra.addFlashAttribute("mensaje", "Producto actualizado: " + p.getNombre());
        return "redirect:/admin/productos";
    }

    @PostMapping("/productos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String nombre = admin.eliminarProducto(id);
            ra.addFlashAttribute("mensaje", "Producto eliminado: " + nombre);
        } catch (DataIntegrityViolationException e) {
            ra.addFlashAttribute("mensaje",
                    "No se puede eliminar: el producto ya tiene pedidos. Ponle stock 0 para dejar de venderlo.");
        }
        return "redirect:/admin/productos";
    }

    /** Si hay imagen, hay que indicar su fuente (de dónde salió). */
    private void validarFuenteImagen(ProductoForm form, BindingResult result) {
        if (form.getUrlImagen() != null && form.getFuenteImagen() == null
                && !result.hasFieldErrors("urlImagen")) {
            result.rejectValue("fuenteImagen", "requerida",
                    "Indica de dónde salió la imagen (catálogo oficial, distribuidor autorizado, etc.)");
        }
    }

    private void cargarCombos(Model model, String accion) {
        model.addAttribute("marcas", catalogo.marcas());
        model.addAttribute("categorias", catalogo.categorias());
        model.addAttribute("tiposPiel", catalogo.tiposPiel());
        model.addAttribute("accion", accion);
    }
}
