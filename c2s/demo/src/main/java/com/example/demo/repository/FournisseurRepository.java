package com.example.demo.repository;

import com.example.demo.model.FournisseurEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FournisseurRepository extends JpaRepository<FournisseurEntity, Long> {

    boolean existsByNom(String nom);

}




