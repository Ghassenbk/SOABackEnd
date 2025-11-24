// src/main/java/com/example/demo/model/PanierSolution.java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "panier_solution")
@Data
@IdClass(PanierSolutionId.class)
public class PanierSolution {

    @Id
    @ManyToOne
    @JoinColumn(name = "panier_id")
    private PanierEntity panier;

    @Id
    @ManyToOne
    @JoinColumn(name = "solution_id")
    private ProduitEntity solution;

    @Column(nullable = false)
    private int quantite = 1;
}