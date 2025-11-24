package com.example.demo.dto;


public record PanierItemDTO(
        int quantite,
        SolutionDTO solution
) {
    public record SolutionDTO(
            Long id,
            String name,
            String description,
            Double prix
    ) {}
}