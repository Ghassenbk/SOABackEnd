package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.PanierEntity;
import com.example.demo.model.ProduitEntity;
import com.example.demo.model.PanierSolution;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/paniers")
@CrossOrigin(origins = "http://localhost:4200") // Pour Angular
public class PanierController {

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private ProduitRepository produitRepository;

    // Récupérer tous les paniers (admin)
    @GetMapping
    public List<PanierEntity> getAllPaniers() {
        return panierRepository.findAll();
    }

    // Récupérer un panier par ID (avec ses items + quantités)
    @GetMapping("/{id}")
    public ResponseEntity<PanierEntity> getPanierById(@PathVariable Long id) {
        PanierEntity panier = panierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé avec l'ID : " + id));
        return ResponseEntity.ok(panier);
    }

    // Créer un nouveau panier vide
    @PostMapping
    public ResponseEntity<PanierEntity> createPanier() {
        PanierEntity panier = new PanierEntity();
        PanierEntity saved = panierRepository.save(panier);
        return ResponseEntity.status(201).body(saved);
    }

    // AJOUTER UN PRODUIT AU PANIER (avec gestion de quantité)
    @PostMapping("/{panierId}/solutions/{solutionId}")
    public ResponseEntity<String> addSolutionToCart(
            @PathVariable Long panierId,
            @PathVariable Long solutionId) {

        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        ProduitEntity solution = produitRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution non trouvée"));

        // Chercher si la solution est déjà dans le panier
        Optional<PanierSolution> existingItem = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId() == solutionId)
                .findFirst();

        if (existingItem.isPresent()) {
            // Augmenter la quantité
            PanierSolution item = existingItem.get();
            item.setQuantite(item.getQuantite() + 1);
        } else {
            // Ajouter un nouveau produit avec quantité = 1
            PanierSolution newItem = new PanierSolution();
            newItem.setPanier(panier);
            newItem.setSolution(solution);
            newItem.setQuantite(1);
            panier.getSolutionItems().add(newItem);
        }

        panierRepository.save(panier);
        return ResponseEntity.ok("Produit ajouté au panier");
    }

    // SUPPRIMER UN PRODUIT DU PANIER (ou diminuer la quantité)
    @DeleteMapping("/{panierId}/solutions/{solutionId}")
    public ResponseEntity<String> removeSolutionFromCart(
            @PathVariable Long panierId,
            @PathVariable Integer solutionId) {

        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        Optional<PanierSolution> itemOpt = panier.getSolutionItems().stream()
                .filter(item ->item.getSolution().getId() == solutionId)
                .findFirst();

        if (itemOpt.isPresent()) {
            PanierSolution item = itemOpt.get();
            if (item.getQuantite() > 1) {
                item.setQuantite(item.getQuantite() - 1);
            } else {
                panier.getSolutionItems().remove(item);
            }
            panierRepository.save(panier);
            return ResponseEntity.ok("Produit retiré du panier");
        }

        return ResponseEntity.status(404).body("Produit non trouvé dans le panier");
    }

    // VIDER COMPLÈTEMENT LE PANIER
    @DeleteMapping("/{panierId}")
    public ResponseEntity<String> clearCart(@PathVariable Long panierId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        panier.getSolutionItems().clear();
        panierRepository.save(panier);
        return ResponseEntity.ok("Panier vidé avec succès");
    }

    // RÉCUPÉRER LE CONTENU DU PANIER (avec quantités)
    @GetMapping("/{panierId}/solutions")
    public ResponseEntity<List<PanierSolution>> getCartItems(@PathVariable Long panierId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        return ResponseEntity.ok(panier.getSolutionItems());
    }
}