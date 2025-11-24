package com.example.demo.controller;

import com.example.demo.model.ProduitEntity;
import com.example.demo.service.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/solutions")
@CrossOrigin(origins = "http://localhost:4200")
public class ProduitController {

    @Autowired
    private ProduitService produitService;

    // ====================== CRUD ======================
    @GetMapping
    public List<ProduitEntity> getAllSolutions() {
        return produitService.getAllSolutions();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitEntity> getSolutionById(@PathVariable Long id) {
        return ResponseEntity.ok(produitService.getSolutionById(id));
    }

    @PostMapping
    public ResponseEntity<ProduitEntity> createSolution(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("prix") Float prix,
            @RequestParam("devis") MultipartFile devis,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "categorieId", required = false) Long categorieId,
            @RequestParam(value = "fournisseurId", required = false) Long fournisseurId
    ) throws IOException {
        ProduitEntity saved = produitService.createSolution(
                name, description, prix, devis, image, categorieId, fournisseurId
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProduitEntity> updateSolution(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "prix", required = false) Float prix,
            @RequestParam(value = "devis", required = false) MultipartFile devis,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "categorieId", required = false) Long categorieId,
            @RequestParam(value = "fournisseurId", required = false) Long fournisseurId
    ) throws IOException {
        return ResponseEntity.ok(
                produitService.updateSolution(id, name, description, prix, devis, image, categorieId, fournisseurId)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolution(@PathVariable Long id) {
        produitService.deleteSolution(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllSolutions() {
        produitService.deleteAllSolutions();
        return ResponseEntity.noContent().build();
    }
}
