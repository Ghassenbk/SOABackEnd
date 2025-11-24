package com.example.demo.controller;

import com.example.demo.dto.CategorieWithCountDTO;
import com.example.demo.model.CategorieEntity;
import com.example.demo.repository.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategorieController {

    @Autowired
    private CategorieRepository repo;

    @GetMapping
    public List<CategorieWithCountDTO> getAllCategories() {
        return repo.findAllWithCount();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategorieEntity c) {
        if (repo.existsByNom(c.getNom())) {
            return ResponseEntity.badRequest().body("Cette catégorie existe déjà");
        }
        return ResponseEntity.ok(repo.save(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategorieEntity> update(@PathVariable Long id,
                                                  @RequestBody CategorieEntity updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setNom(updated.getNom());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // LA MÉTHODE QUI MANQUAIT → MAINTENANT TOUT MARCHE !
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id)
                .map(categorie -> {
                    if (categorie.getProduits() != null && !categorie.getProduits().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("Impossible de supprimer : cette catégorie est utilisée par " +
                                        categorie.getProduits().size() + " produit(s)");
                    }
                    repo.delete(categorie);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}