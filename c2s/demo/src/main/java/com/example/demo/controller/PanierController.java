package com.example.demo.controller;

import com.example.demo.model.PanierEntity;
import com.example.demo.model.PanierSolution;
import com.example.demo.service.PanierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/paniers")
@CrossOrigin(origins = "http://localhost:4200")
public class PanierController {

    @Autowired
    private PanierService panierService;

    // ====================== CRUD PANIERS ======================
    @GetMapping
    public List<PanierEntity> getAllPaniers() {
        return panierService.getAllPaniers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PanierEntity> getPanierById(@PathVariable Long id) {
        return ResponseEntity.ok(panierService.getPanierById(id));
    }

    @PostMapping
    public ResponseEntity<PanierEntity> createPanier() {
        PanierEntity saved = panierService.createPanier();
        return ResponseEntity.status(201).body(saved);
    }

    // ====================== GESTION PRODUITS PANIERS ======================
    @PostMapping("/{panierId}/solutions/{solutionId}")
    public ResponseEntity<String> addSolutionToCart(
            @PathVariable Long panierId,
            @PathVariable Long solutionId
    ) {
        return ResponseEntity.ok(panierService.addSolutionToCart(panierId, solutionId));
    }

    @DeleteMapping("/{panierId}/solutions/{solutionId}")
    public ResponseEntity<String> removeSolutionFromCart(
            @PathVariable Long panierId,
            @PathVariable Long solutionId
    ) {
        return ResponseEntity.ok(panierService.removeSolutionFromCart(panierId, solutionId));
    }

    @DeleteMapping("/{panierId}")
    public ResponseEntity<String> clearCart(@PathVariable Long panierId) {
        return ResponseEntity.ok(panierService.clearCart(panierId));
    }

    @GetMapping("/{panierId}/solutions")
    public ResponseEntity<List<PanierSolution>> getCartItems(@PathVariable Long panierId) {
        return ResponseEntity.ok(panierService.getCartItems(panierId));
    }
}
