package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.PanierEntity;
import com.example.demo.model.PanierSolution;
import com.example.demo.model.ProduitEntity;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PanierService {

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private ProduitRepository produitRepository;

    // ====================== CRUD PANIERS ======================

    public List<PanierEntity> getAllPaniers() {
        return panierRepository.findAll();
    }

    public PanierEntity getPanierById(Long id) {
        return panierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé avec l'ID : " + id));
    }

    public PanierEntity createPanier() {
        PanierEntity panier = new PanierEntity();
        return panierRepository.save(panier);
    }

    // ====================== GESTION PRODUITS PANIERS ======================

    public String addSolutionToCart(Long panierId, Long solutionId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        ProduitEntity solution = produitRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution non trouvée"));

        Optional<PanierSolution> existingItem = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId()== solutionId)
                .findFirst();

        if (existingItem.isPresent()) {
            PanierSolution item = existingItem.get();
            item.setQuantite(item.getQuantite() + 1);
        } else {
            PanierSolution newItem = new PanierSolution();
            newItem.setPanier(panier);
            newItem.setSolution(solution);
            newItem.setQuantite(1);
            panier.getSolutionItems().add(newItem);
        }

        panierRepository.save(panier);
        return "Produit ajouté au panier";
    }

    public String removeSolutionFromCart(Long panierId, Long solutionId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        Optional<PanierSolution> itemOpt = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId()== solutionId)
                .findFirst();

        if (itemOpt.isPresent()) {
            PanierSolution item = itemOpt.get();
            if (item.getQuantite() > 1) {
                item.setQuantite(item.getQuantite() - 1);
            } else {
                panier.getSolutionItems().remove(item);
            }
            panierRepository.save(panier);
            return "Produit retiré du panier";
        }

        throw new ResourceNotFoundException("Produit non trouvé dans le panier");
    }

    public String clearCart(Long panierId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        panier.getSolutionItems().clear();
        panierRepository.save(panier);
        return "Panier vidé avec succès";
    }

    public List<PanierSolution> getCartItems(Long panierId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new ResourceNotFoundException("Panier non trouvé"));

        return panier.getSolutionItems();
    }
}
