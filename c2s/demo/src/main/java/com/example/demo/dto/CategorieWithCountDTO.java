// src/main/java/com/example/demo/dto/CategorieWithCountDTO.java
package com.example.demo.dto;

import lombok.Data;

@Data
public class CategorieWithCountDTO {
    private Long id;
    private String nom;
    private long nombreProduits;

    public CategorieWithCountDTO(Long id, String nom, long nombreProduits) {
        this.id = id;
        this.nom = nom;
        this.nombreProduits = nombreProduits;
    }
}