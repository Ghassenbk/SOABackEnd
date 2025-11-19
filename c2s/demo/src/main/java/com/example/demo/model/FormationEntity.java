package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.List;


@Entity
@Data
@RequiredArgsConstructor
@Table(name ="formation")
public class FormationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name",unique = true,nullable = false)
    private String name;

    @Column(name = "dateDebut")
    private Date dateDebut;

    @Column(name = "dateFin")
    private Date dateFin;


    @Column(name = "description")
    private String description;

    @Column(name = "prix")
    private float prix;

    @ManyToMany(mappedBy = "formations")
    @JsonIgnore
    private List<User> users;

}
