package com.example.demo.repository;

import com.example.demo.dto.CategorieWithCountDTO;
import com.example.demo.model.CategorieEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategorieRepository extends JpaRepository<CategorieEntity, Long> {
    boolean existsByNom(String nom);
    @Query("SELECT new com.example.demo.dto.CategorieWithCountDTO(c.id, c.nom, COUNT(p.id)) " +
            "FROM CategorieEntity c LEFT JOIN c.produits p " +
            "GROUP BY c.id, c.nom " +
            "ORDER BY c.nom")
    List<CategorieWithCountDTO> findAllWithCount();

}

