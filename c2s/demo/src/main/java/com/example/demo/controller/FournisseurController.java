package com.example.demo.controller;

import com.example.demo.model.FournisseurEntity;
import com.example.demo.repository.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurRepository repo;

    @GetMapping
    public List<FournisseurEntity> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody FournisseurEntity f) {
        if (repo.existsByNom(f.getNom())) {
            return ResponseEntity.badRequest()
                    .body("Ce fournisseur existe déjà");
        }
        return ResponseEntity.ok(repo.save(f));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FournisseurEntity> update(@PathVariable Long id,
                                                    @Valid @RequestBody FournisseurEntity updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setNom(updated.getNom());
                    return ResponseEntity.ok(repo.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        return repo.findById(id)
                .map(fournisseur -> {
                    if (fournisseur.getProduits() != null && !fournisseur.getProduits().isEmpty()) {
                        return ResponseEntity.badRequest()
                                .body("Impossible de supprimer : ce fournisseur est utilisé par " +
                                        fournisseur.getProduits().size() + " produit(s)");
                    }
                    repo.delete(fournisseur);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}