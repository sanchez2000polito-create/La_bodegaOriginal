package com.portatil.bodega.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MarcaForm {

    /** null = marca nueva. */
    private Long id;

    @NotBlank(message = "Escribe el nombre de la marca")
    @Size(max = 100, message = "Máximo 100 caracteres")
    private String nombre;
}
