package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "paiement")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaiementEntity {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(name = "card", nullable = false)
     private String card;

     @Column(name = "total_payed", nullable = false)
     private Double totalPayed;

     @Column(name = "date", nullable = false)
     @Temporal(TemporalType.TIMESTAMP)
     private Date date = new Date();

     @ManyToOne
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

     @ManyToOne
     @JoinColumn(name = "panier_id")
     private PanierEntity panier;

     // CORRIGÉ : on utilise PaiementItem maintenant
     @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
     @JoinColumn(name = "paiement_id")  // ← UNIQUEMENT CETTE LIGNE, pas de @JoinTable !
     @JsonIgnoreProperties("paiement")
     private List<PaiementItem> items = new ArrayList<>();

     @ElementCollection
     private List<String> solutionNames = new ArrayList<>();
}