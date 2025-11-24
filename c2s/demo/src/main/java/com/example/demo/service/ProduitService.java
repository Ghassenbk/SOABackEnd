package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.CategorieEntity;
import com.example.demo.model.FournisseurEntity;
import com.example.demo.model.PaiementEntity;
import com.example.demo.model.ProduitEntity;
import com.example.demo.repository.CategorieRepository;
import com.example.demo.repository.FournisseurRepository;
import com.example.demo.repository.PaiementRepository;
import com.example.demo.repository.ProduitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class ProduitService {

    @Autowired private PaiementRepository paiementRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private CategorieRepository categorieRepository;
    @Autowired private FournisseurRepository fournisseurRepository;

    private static final String IMAGE_DIR = "src/main/resources/static/images/";
    private static final String PDF_DIR = "src/main/resources/static/pdfs/";

    static {
        try {
            Files.createDirectories(Paths.get(IMAGE_DIR));
            Files.createDirectories(Paths.get(PDF_DIR));
        } catch (IOException e) {
            System.err.println("Impossible de créer les dossiers statiques: " + e.getMessage());
        }
    }

    // ====================== CRUD ======================
    public List<ProduitEntity> getAllSolutions() {
        return produitRepository.findAllWithCategorieAndFournisseur();
    }

    public ProduitEntity getSolutionById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));
    }

    public ProduitEntity createSolution(
            String name,
            String description,
            Float prix,
            MultipartFile devis,
            MultipartFile image,
            Long categorieId,
            Long fournisseurId
    ) throws IOException {

        if (image.isEmpty() || !image.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image invalide");
        }
        if (devis.isEmpty() || !devis.getContentType().equals("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF requis");
        }

        String imageExt = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
        String pdfExt = devis.getOriginalFilename().substring(devis.getOriginalFilename().lastIndexOf("."));
        String imageFilename = "solution_" + System.currentTimeMillis() + imageExt;
        String pdfFilename = "devis_" + System.currentTimeMillis() + pdfExt;

        Files.copy(image.getInputStream(), Paths.get(IMAGE_DIR + imageFilename), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(devis.getInputStream(), Paths.get(PDF_DIR + pdfFilename), StandardCopyOption.REPLACE_EXISTING);

        CategorieEntity categorie = null;
        if (categorieId != null && categorieId > 0) {
            categorie = categorieRepository.findById(categorieId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie invalide"));
        }

        FournisseurEntity fournisseur = null;
        if (fournisseurId != null && fournisseurId > 0) {
            fournisseur = fournisseurRepository.findById(fournisseurId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fournisseur invalide"));
        }

        ProduitEntity solution = new ProduitEntity();
        solution.setName(name);
        solution.setDescription(description);
        solution.setPrix(prix);
        solution.setImage("/images/" + imageFilename);
        solution.setDevis("/pdfs/" + pdfFilename);
        solution.setCategorie(categorie);
        solution.setFournisseur(fournisseur);

        return produitRepository.save(solution);
    }

    public ProduitEntity updateSolution(
            Long id,
            String name,
            String description,
            Float prix,
            MultipartFile devis,
            MultipartFile image,
            Long categorieId,
            Long fournisseurId
    ) throws IOException {

        ProduitEntity existing = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution non trouvée"));

        if (name != null && !name.isBlank()) {
            if (!name.equals(existing.getName()) && produitRepository.existsByName(name)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce nom existe déjà");
            }
            existing.setName(name);
        }
        if (description != null && !description.isBlank()) existing.setDescription(description);
        if (prix != null && prix > 0) existing.setPrix(prix);

        if (categorieId != null) {
            if (categorieId == 0) {
                existing.setCategorie(null);
            } else {
                CategorieEntity cat = categorieRepository.findById(categorieId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie invalide"));
                existing.setCategorie(cat);
            }
        }

        if (fournisseurId != null) {
            if (fournisseurId == 0) {
                existing.setFournisseur(null);
            } else {
                FournisseurEntity four = fournisseurRepository.findById(fournisseurId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fournisseur invalide"));
                existing.setFournisseur(four);
            }
        }

        if (image != null && !image.isEmpty()) {
            String ext = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
            String filename = "solution_" + System.currentTimeMillis() + ext;
            Files.copy(image.getInputStream(), Paths.get(IMAGE_DIR + filename), StandardCopyOption.REPLACE_EXISTING);
            existing.setImage("/images/" + filename);
        }

        if (devis != null && !devis.isEmpty()) {
            String ext = devis.getOriginalFilename().substring(devis.getOriginalFilename().lastIndexOf("."));
            String filename = "devis_" + System.currentTimeMillis() + ext;
            Files.copy(devis.getInputStream(), Paths.get(PDF_DIR + filename), StandardCopyOption.REPLACE_EXISTING);
            existing.setDevis("/pdfs/" + filename);
        }

        return produitRepository.save(existing);
    }

    public void deleteSolution(Long id) {
        ProduitEntity solution = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));

        // Remove from paiements
        paiementRepository.findAll().forEach(paiement -> {
            paiement.getItems().removeIf(item ->
                    item.getSolution() != null && item.getSolution().getId()==id
            );
            paiementRepository.save(paiement);
        });

        produitRepository.deleteById(id);
    }

    public void deleteAllSolutions() {
        produitRepository.deleteAll();
    }
}
