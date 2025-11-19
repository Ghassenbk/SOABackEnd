 package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

 @Entity
@Data
@RequiredArgsConstructor
@Table(name = "paiement")
public class PaiementEntity {

     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Long id;

     @Column(name = "card", nullable = false)
     private String card; // Should contain values like "PayPal", "MasterCard", "Visa"

     @Column(name = "total_payed", nullable = false)
     private Double totalPayed;

     @ManyToOne(cascade = CascadeType.PERSIST)
     @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)
     private User user;

     @ManyToOne
     @JoinColumn(name = "panier_id", referencedColumnName = "id", nullable = true)
     private PanierEntity panier;

     @ManyToMany
     @JoinTable(
             name = "paiement_solution",
             joinColumns = @JoinColumn(name = "paiement_id"),
             inverseJoinColumns = @JoinColumn(name = "solution_id")
     )
     private List<SolutionEntity> solutionsBought;

      @Column(name = "solution_names")
      @ElementCollection
      private List<String> solutionNames = new ArrayList<>();




}
