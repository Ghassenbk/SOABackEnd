package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.FournisseurEntity;
import com.example.demo.repository.FournisseurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FournisseurService {

    @Autowired
    private FournisseurRepository repo;

    // ====================== GET ALL ======================
    public List<FournisseurEntity> getAllFournisseurs() {
        return repo.findAll();
    }

    // ====================== CREATE ======================
    public FournisseurEntity createFournisseur(FournisseurEntity f) {
        if (repo.existsByNom(f.getNom())) {
            throw new IllegalArgumentException("Ce fournisseur existe déjà");
        }
        return repo.save(f);
    }

    // ====================== UPDATE ======================
    public FournisseurEntity updateFournisseur(Long id, FournisseurEntity updated) {
        return repo.findById(id).map(existing -> {
            existing.setNom(updated.getNom());
            return repo.save(existing);
        }).orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé"));
    }

    // ====================== DELETE ======================
    public void deleteFournisseur(Long id) {
        FournisseurEntity fournisseur = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé"));

        if (fournisseur.getProduits() != null && !fournisseur.getProduits().isEmpty()) {
            throw new IllegalStateException(
                    "Impossible de supprimer : ce fournisseur est utilisé par " +
                            fournisseur.getProduits().size() + " produit(s)"
            );
        }
        repo.delete(fournisseur);
    }
}
