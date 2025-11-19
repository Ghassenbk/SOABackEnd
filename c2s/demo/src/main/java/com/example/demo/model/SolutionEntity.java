package com.example.demo.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;


@Entity
@Data
@RequiredArgsConstructor
@Table(name ="solution")
public class SolutionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name",unique = true,nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "devis")
    private String devis;

    @Column(name = "prix")
    private float prix;

    @Lob
    @Column(name = "image",length = 1000000)
    private String image;

    @ManyToMany(mappedBy = "solutions")
    @JsonIgnore
    private List<PanierEntity> paniers;

    @ManyToMany(mappedBy = "solutionsBought")
    @JsonIgnore
    private List<PaiementEntity> paiements;


}
