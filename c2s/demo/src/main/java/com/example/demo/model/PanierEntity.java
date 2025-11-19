package com.example.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Entity
@Data
@RequiredArgsConstructor
@Table(name ="panier")
public class PanierEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @JoinTable(
            name = "panier_solution",
            joinColumns = @JoinColumn(name = "panier_id"),
            inverseJoinColumns = @JoinColumn(name = "solution_id")
    )
    private List<SolutionEntity> solutions;


    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<PaiementEntity> paiements;



}
