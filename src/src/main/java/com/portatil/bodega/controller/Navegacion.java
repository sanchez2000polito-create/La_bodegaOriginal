package com.portatil.bodega.controller;

import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;

/** Utilidad para volver a la página donde estaba el cliente después de un POST. */
final class Navegacion {

    private Navegacion() {
    }

    /**
     * Devuelve "redirect:/ruta-donde-estaba". Solo acepta el Referer si es de este mismo sitio;
     * si no hay Referer (o es de otro sitio), usa la ruta por defecto.
     */
    static String volverA(HttpServletRequest request, String porDefecto) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                if (request.getServerName().equalsIgnoreCase(uri.getHost())) {
                    String ruta = uri.getRawPath();
                    String contexto = request.getContextPath();
                    if (ruta == null || ruta.isEmpty()) {
                        ruta = "/";
                    }
                    if (!contexto.isEmpty() && ruta.startsWith(contexto)) {
                        ruta = ruta.substring(contexto.length());
                    }
                    if (ruta.isEmpty()) {
                        ruta = "/";
                    }
                    String consulta = uri.getRawQuery() != null ? "?" + uri.getRawQuery() : "";
                    return "redirect:" + ruta + consulta;
                }
            } catch (IllegalArgumentException ignorado) {
                // Referer mal formado: se usa la ruta por defecto.
            }
        }
        return "redirect:" + porDefecto;
    }
}
