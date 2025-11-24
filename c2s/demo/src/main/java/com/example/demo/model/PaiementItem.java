// src/main/java/com/example/demo/model/PaiementItem.java
package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "paiement_item")
@Data
public class PaiementItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solution_id", nullable = false)
    private ProduitEntity solution;

    @Column(nullable = false)
    private int quantite;

    @Column(nullable = false)
    private double prixUnitaire; // prix au moment de l'achat (important !)
}