package com.portatil.bodega.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Datos de envío y método de pago del checkout (validados con Bean Validation). */
@Getter @Setter @NoArgsConstructor
public class CheckoutForm {

    @NotBlank(message = "Escribe el nombre de quien recibe")
    @Size(max = 120, message = "El nombre es demasiado largo")
    private String nombreDestinatario;

    @NotBlank(message = "Escribe un teléfono de contacto")
    @Pattern(regexp = "^[0-9+ ]{7,20}$", message = "Usa solo números (de 7 a 20 dígitos)")
    private String telefono;

    @NotBlank(message = "Selecciona el departamento")
    private String departamento;

    @NotBlank(message = "Escribe la ciudad o municipio")
    @Size(max = 80, message = "La ciudad es demasiado larga")
    private String ciudad;

    @NotBlank(message = "Escribe la dirección")
    @Size(min = 6, max = 200, message = "La dirección debe tener entre 6 y 200 caracteres")
    private String direccion;

    /** Solo estructura: no se hace ningún cobro real. */
    @NotBlank(message = "Selecciona un método de pago")
    @Pattern(regexp = "PSE|TARJETA|CONTRA_ENTREGA", message = "Método de pago no válido")
    private String metodoPago;
}
