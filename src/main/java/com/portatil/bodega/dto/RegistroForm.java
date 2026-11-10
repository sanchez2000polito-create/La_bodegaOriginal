package com.portatil.bodega.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class RegistroForm {

    @NotBlank(message = "Escribe tu nombre")
    @Size(max = 120, message = "El nombre es demasiado largo")
    private String nombre;

    @NotBlank(message = "Escribe tu correo")
    @Email(message = "El correo no es válido")
    @Size(max = 160, message = "El correo es demasiado largo")
    private String email;

    @NotBlank(message = "Escribe una contraseña")
    @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
    private String password;

    @NotBlank(message = "Repite la contraseña")
    private String confirmar;
}
