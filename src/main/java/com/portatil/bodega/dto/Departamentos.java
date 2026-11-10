package com.portatil.bodega.dto;

import java.util.List;

/** Departamentos de Colombia para el formulario de envío. */
public final class Departamentos {

    public static final List<String> TODOS = List.of(
            "Amazonas", "Antioquia", "Arauca", "Atlántico", "Bogotá D.C.", "Bolívar", "Boyacá",
            "Caldas", "Caquetá", "Casanare", "Cauca", "Cesar", "Chocó", "Córdoba", "Cundinamarca",
            "Guainía", "Guaviare", "Huila", "La Guajira", "Magdalena", "Meta", "Nariño",
            "Norte de Santander", "Putumayo", "Quindío", "Risaralda", "San Andrés y Providencia",
            "Santander", "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada");

    private Departamentos() {
    }
}
