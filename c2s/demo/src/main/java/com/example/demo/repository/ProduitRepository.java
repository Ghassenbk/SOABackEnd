package com.example.demo.repository;


import com.example.demo.model.ProduitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ProduitRepository extends JpaRepository<ProduitEntity, Long> {
    boolean existsByName(String name);

    @Query("SELECT p FROM ProduitEntity p LEFT JOIN FETCH p.categorie LEFT JOIN FETCH p.fournisseur")
    List<ProduitEntity> findAllWithCategorieAndFournisseur();
}