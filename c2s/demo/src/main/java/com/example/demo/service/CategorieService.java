package com.example.demo.service;

import com.example.demo.dto.CategorieWithCountDTO;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.CategorieEntity;
import com.example.demo.repository.CategorieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategorieService {

    @Autowired
    private CategorieRepository repo;

    // ====================== GET ALL ======================
    public List<CategorieWithCountDTO> getAllCategories() {
        return repo.findAllWithCount();
    }

    // ====================== CREATE ======================
    public CategorieEntity createCategorie(CategorieEntity c) {
        if (repo.existsByNom(c.getNom())) {
            throw new IllegalArgumentException("Cette catégorie existe déjà");
        }
        return repo.save(c);
    }

    // ====================== UPDATE ======================
    public CategorieEntity updateCategorie(Long id, CategorieEntity updated) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setNom(updated.getNom());
                    return repo.save(existing);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));
    }

    // ====================== DELETE ======================
    public void deleteCategorie(Long id) {
        CategorieEntity categorie = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie non trouvée"));

        if (categorie.getProduits() != null && !categorie.getProduits().isEmpty()) {
            throw new IllegalStateException(
                    "Impossible de supprimer : cette catégorie est utilisée par " +
                            categorie.getProduits().size() + " produit(s)"
            );
        }

        repo.delete(categorie);
    }
}
