package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.PaiementRepository;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private UserRepository userRepository;

    // ====================== ADMIN : Tous les paiements détaillés ======================
    public List<Map<String, Object>> getAllPaiementsDetailed() {
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

        return result;
    }

    // ====================== PAIEMENT CLIENT ======================
    public Map<String, Object> processPaiement(Long userId, String card) {
        if (card == null || card.trim().isEmpty()) {
            throw new IllegalArgumentException("Méthode de paiement requise.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        PanierEntity panier = user.getPanier();
        if (panier == null || panier.getSolutionItems().isEmpty()) {
            throw new IllegalArgumentException("Votre panier est vide.");
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

        List<PaiementItem> boughtItems = new ArrayList<>();
        for (PanierSolution ps : panier.getSolutionItems()) {
            PaiementItem item = new PaiementItem();
            item.setSolution(ps.getSolution());
            item.setQuantite(ps.getQuantite());
            item.setPrixUnitaire(ps.getSolution().getPrix());
            boughtItems.add(item);
        }
        paiement.setItems(boughtItems);

        paiement.getSolutionNames().addAll(
                panier.getSolutionItems().stream()
                        .map(ps -> ps.getSolution().getName() + " (x" + ps.getQuantite() + ")")
                        .toList()
        );

        PaiementEntity saved = paiementRepository.save(paiement);

        // Vider le panier
        panier.getSolutionItems().clear();
        panierRepository.save(panier);

        return Map.of(
                "message", "Paiement réussi !",
                "total", total,
                "paiementId", saved.getId()
        );
    }

    // ====================== AUTRES ======================
    public List<PaiementEntity> getAllPaiements() {
        return paiementRepository.findAll();
    }

    public void deletePaiement(Long id) {
        PaiementEntity paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        paiement.getItems().clear();
        paiementRepository.save(paiement);
        paiementRepository.delete(paiement);
    }

    public void deleteAllPaiements() {
        paiementRepository.deleteAll();
    }
}
