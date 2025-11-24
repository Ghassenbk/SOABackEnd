package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.PaiementRepository;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/paiements")
@CrossOrigin(origins = "http://localhost:4200")
public class PaiementController {

    @Autowired private PaiementRepository paiementRepository;
    @Autowired private PanierRepository panierRepository;
    @Autowired private UserRepository userRepository;

    // ADMIN : Tous les paiements avec détails
    @GetMapping("/all-detailed")
    public ResponseEntity<List<Map<String, Object>>> getAllPaiementsDetailed() {
        List<PaiementEntity> paiements = paiementRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (PaiementEntity p : paiements) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("userName", p.getUser().getName());
            item.put("userEmail", p.getUser().getEmail());
            item.put("totalPayed", p.getTotalPayed());
            item.put("date", p.getDate());
            item.put("card", p.getCard());

            // Liste des solutions avec quantité
            List<String> solutionDetails = new ArrayList<>();
            for (PaiementItem pi : p.getItems()) {
                solutionDetails.add(
                        pi.getSolution().getName() +
                                " (x" + pi.getQuantite() +
                                ") - " + (pi.getQuantite() * pi.getPrixUnitaire()) + " DT"
                );
            }
            item.put("solutionNames", solutionDetails);

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    // PAIEMENT CLIENT
    @PostMapping("/pay/{userId}")
    public ResponseEntity<?> processPaiement(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {

        String card = request.get("card");
        if (card == null || card.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Méthode de paiement requise.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        PanierEntity panier = user.getPanier();
        if (panier == null || panier.getSolutionItems().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Votre panier est vide.");
        }

        double total = panier.getSolutionItems().stream()
                .mapToDouble(ps -> ps.getQuantite() * ps.getSolution().getPrix())
                .sum();

        PaiementEntity paiement = new PaiementEntity();
        paiement.setUser(user);
        paiement.setPanier(panier);
        paiement.setCard(card);
        paiement.setTotalPayed(total);
        paiement.setDate(new Date());

        // Copie correcte avec PaiementItem
        List<PaiementItem> boughtItems = new ArrayList<>();
        for (PanierSolution ps : panier.getSolutionItems()) {
            PaiementItem item = new PaiementItem();
            item.setSolution(ps.getSolution());
            item.setQuantite(ps.getQuantite());
            item.setPrixUnitaire(ps.getSolution().getPrix()); // Prix figé
            boughtItems.add(item);
        }
        paiement.setItems(boughtItems);  // CHANGÉ ICI : setItems()

        // Noms pour affichage rapide
        paiement.getSolutionNames().addAll(
                panier.getSolutionItems().stream()
                        .map(ps -> ps.getSolution().getName() + " (x" + ps.getQuantite() + ")")
                        .toList()
        );

        PaiementEntity saved = paiementRepository.save(paiement);

        // Vider le panier
        panier.getSolutionItems().clear();
        panierRepository.save(panier);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Paiement réussi !",
                        "total", total,
                        "paiementId", saved.getId()
                ));
    }

    @GetMapping("/all")
    public List<PaiementEntity> getAllPaiements() {
        return paiementRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(@PathVariable Long id) {
        PaiementEntity paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        paiement.getItems().clear();  // CHANGÉ ICI : getItems().clear()
        paiementRepository.save(paiement);
        paiementRepository.delete(paiement);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllPaiements() {
        paiementRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}