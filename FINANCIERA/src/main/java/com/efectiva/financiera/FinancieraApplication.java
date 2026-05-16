package com.efectiva.financiera;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinancieraApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinancieraApplication.class, args);
        System.out.println("¡API de Financiera Efectiva Iniciada Correctamente!");
    }
}