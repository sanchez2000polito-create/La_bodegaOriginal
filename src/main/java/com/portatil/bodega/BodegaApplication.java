package com.portatil.bodega;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Punto de entrada de la aplicación y raíz del escaneo de componentes. */
@SpringBootApplication
public class BodegaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BodegaApplication.class, args);
    }
}
