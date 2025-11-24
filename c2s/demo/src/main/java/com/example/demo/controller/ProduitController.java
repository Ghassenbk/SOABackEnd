package com.example.demo.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/solutions")
public class ProduitController {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private ProduitRepository produitRepository;

    @Autowired
    private CategorieRepository categorieRepository;

    @Autowired
    private FournisseurRepository fournisseurRepository;

    // FICHIERS STATIQUES ACCESSIBLES
    private static final String IMAGE_DIR = "src/main/resources/static/images/";
    private static final String PDF_DIR = "src/main/resources/static/pdfs/";

    // Crée les dossiers au démarrage si absents
    static {
        try {
            Files.createDirectories(Paths.get(IMAGE_DIR));
            Files.createDirectories(Paths.get(PDF_DIR));
        } catch (IOException e) {
            System.err.println("Impossible de créer les dossiers statiques: " + e.getMessage());
        }
    }

    @GetMapping
    public List<ProduitEntity> getAllSolutions() {
        return produitRepository.findAllWithCategorieAndFournisseur();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProduitEntity> getSolutionById(@PathVariable Long id) {
        ProduitEntity solution = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));
        return ResponseEntity.ok(solution);
    }

    @PostMapping
    public ResponseEntity<ProduitEntity> createSolution(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("prix") float prix,
            @RequestParam("devis") MultipartFile devis,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "categorieId", required = false) Long categorieId,
            @RequestParam(value = "fournisseurId", required = false) Long fournisseurId) throws IOException {

        // Validation
        if (image.isEmpty() || !image.getContentType().startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image invalide");
        }
        if (devis.isEmpty() || !devis.getContentType().equals("application/pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF requis");
        }

        // Génération noms uniques
        String imageExt = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
        String pdfExt = devis.getOriginalFilename().substring(devis.getOriginalFilename().lastIndexOf("."));
        String imageFilename = "solution_" + System.currentTimeMillis() + imageExt;
        String pdfFilename = "devis_" + System.currentTimeMillis() + pdfExt;

        // Sauvegarde fichiers
        Files.copy(image.getInputStream(), Paths.get(IMAGE_DIR + imageFilename), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(devis.getInputStream(), Paths.get(PDF_DIR + pdfFilename), StandardCopyOption.REPLACE_EXISTING);

        // Récupération entités liées
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

        // Création solution
        ProduitEntity solution = new ProduitEntity();
        solution.setName(name);
        solution.setDescription(description);
        solution.setPrix(prix);
        solution.setImage("/images/" + imageFilename);
        solution.setDevis("/pdfs/" + pdfFilename);
        solution.setCategorie(categorie);
        solution.setFournisseur(fournisseur);

        ProduitEntity saved = produitRepository.save(solution);
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
            @RequestParam(value = "fournisseurId", required = false) Long fournisseurId) throws IOException {

        return produitRepository.findById(id).map(existing -> {
            if (name != null && !name.isBlank()) {
                if (!name.equals(existing.getName()) && produitRepository.existsByName(name)) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ce nom existe déjà");
                }
                existing.setName(name);
            }
            if (description != null && !description.isBlank()) existing.setDescription(description);
            if (prix != null && prix > 0) existing.setPrix(prix);

            // Mise à jour catégorie
            if (categorieId != null) {
                if (categorieId == 0) {
                    existing.setCategorie(null);
                } else {
                    CategorieEntity cat = categorieRepository.findById(categorieId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Catégorie invalide"));
                    existing.setCategorie(cat);
                }
            }

            // Mise à jour fournisseur
            if (fournisseurId != null) {
                if (fournisseurId == 0) {
                    existing.setFournisseur(null);
                } else {
                    FournisseurEntity four = fournisseurRepository.findById(fournisseurId)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fournisseur invalide"));
                    existing.setFournisseur(four);
                }
            }

            // Image
            if (image != null && !image.isEmpty()) {
                String ext = image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf("."));
                String filename = "solution_" + System.currentTimeMillis() + ext;
                try {
                    Files.copy(image.getInputStream(), Paths.get(IMAGE_DIR + filename), StandardCopyOption.REPLACE_EXISTING);
                    existing.setImage("/images/" + filename);
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur sauvegarde image");
                }
            }

            // PDF
            if (devis != null && !devis.isEmpty()) {
                String ext = devis.getOriginalFilename().substring(devis.getOriginalFilename().lastIndexOf("."));
                String filename = "devis_" + System.currentTimeMillis() + ext;
                try {
                    Files.copy(devis.getInputStream(), Paths.get(PDF_DIR + filename), StandardCopyOption.REPLACE_EXISTING);
                    existing.setDevis("/pdfs/" + filename);
                } catch (IOException e) {
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur sauvegarde PDF");
                }
            }

            return ResponseEntity.ok(produitRepository.save(existing));
        }).orElseThrow(() -> new ResourceNotFoundException("Solution non trouvée"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolution(@PathVariable Long id) {  // Changé en Integer
        ProduitEntity solution = produitRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));

        // CORRIGÉ ICI : on utilise getItems() au lieu de getSolutionItemsBought()
        paiementRepository.findAll().forEach(paiement -> {
            paiement.getItems().removeIf(item ->
                    item.getSolution() != null && item.getSolution().getId() == id
            );
            paiementRepository.save(paiement);
        });

        produitRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllSolutions() {
        produitRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}