package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired private UserRepository userRepository;
    @Autowired private FormationRepository formationRepository;
    @Autowired private AvisRepository avisRepository;
    @Autowired private PanierRepository panierRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private PaiementRepository paiementRepository;

    // ====================== USER CRUD ======================
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        }

        // Créer un panier vide (nouvelle architecture)
        PanierEntity panier = new PanierEntity();
        user.setPanier(panier);
        user.setRole(Role.CLIENT);

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) user.setName(updatedUser.getName());
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().isBlank()) user.setPhone(updatedUser.getPhone());
        if (updatedUser.getAdresse() != null && !updatedUser.getAdresse().isBlank()) user.setAdresse(updatedUser.getAdresse());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) user.setPassword(updatedUser.getPassword());

        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Supprimer les paiements liés
        paiementRepository.findByUser(user).forEach(paiementRepository::delete);

        // Supprimer le panier (cascade gère les PanierSolution)
        if (user.getPanier() != null) {
            panierRepository.delete(user.getPanier());
        }

        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // ====================== PANIER (NOUVELLE ARCHITECTURE AVEC QUANTITÉ) ======================

    @GetMapping("/{id}/panier")
    public ResponseEntity<?> getUserPanier(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            return ResponseEntity.ok(Map.of("solutionItems", new ArrayList<>()));
        }

        // ON RENVOIE UN JSON PROPRE → PAS DE PROXY
        List<Map<String, Object>> items = new ArrayList<>();
        for (PanierSolution ps : panier.getSolutionItems()) {
            Map<String, Object> item = new HashMap<>();
            item.put("quantite", ps.getQuantite());

            Map<String, Object> solution = new HashMap<>();
            solution.put("id", ps.getSolution().getId());
            solution.put("name", ps.getSolution().getName());
            solution.put("description", ps.getSolution().getDescription());
            solution.put("prix", ps.getSolution().getPrix());
            // Ajoute d'autres champs si besoin

            item.put("solution", solution);
            items.add(item);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("solutionItems", items);

        return ResponseEntity.ok(response);
    }

    // AJOUTER UNE SOLUTION AU PANIER (avec gestion de quantité)
    // Remplace ta méthode actuelle par celle-ci (100% fonctionnelle)
    @PostMapping("/{id}/panier/solutions/{solutionId}")
    public ResponseEntity<Map<String, Object>> addSolutionToPanier(
            @PathVariable Long id,
            @PathVariable Long solutionId) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ProduitEntity solution = produitRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found"));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            panier = new PanierEntity();
            user.setPanier(panier);
            userRepository.save(user);
        }

        // Gestion quantité
        Optional<PanierSolution> existing = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId()== solutionId)
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantite(existing.get().getQuantite() + 1);
        } else {
            PanierSolution newItem = new PanierSolution();
            newItem.setPanier(panier);
            newItem.setSolution(solution);
            newItem.setQuantite(1);
            panier.getSolutionItems().add(newItem);
        }

        panierRepository.save(panier);

        // ON NE RENVOIE PAS LE PANIER → ON RENVOIE UN JSON PROPRE
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Ajouté au panier avec succès !");
        response.put("totalItems", panier.getSolutionItems().size());

        return ResponseEntity.ok(response);
    }

    // SUPPRIMER UNE SOLUTION DU PANIER (diminue quantité ou supprime)
    @DeleteMapping("/{id}/panier/solutions/{solutionId}")
    public ResponseEntity<PanierEntity> removeSolutionFromPanier(
            @PathVariable Long id,
            @PathVariable Integer solutionId) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Optional<PanierSolution> itemOpt = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId() == solutionId)
                .findFirst();

        if (itemOpt.isPresent()) {
            PanierSolution item = itemOpt.get();
            if (item.getQuantite() > 1) {
                item.setQuantite(item.getQuantite() - 1);
            } else {
                panier.getSolutionItems().remove(item);
            }
            panierRepository.save(panier);
        }

        return ResponseEntity.ok(panier);
    }

    // VIDER LE PANIER
    @DeleteMapping("/{id}/panier")
    public ResponseEntity<String> clearPanier(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getPanier() != null) {
            user.getPanier().getSolutionItems().clear();
            panierRepository.save(user.getPanier());
        }
        return ResponseEntity.ok("Panier vidé");
    }

    // ====================== FORMATIONS & AVIS ======================
    @GetMapping("/{id}/formations")
    public ResponseEntity<List<FormationEntity>> getUserFormations(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(user.getFormations());
    }

    @PostMapping("/{id}/formations/{formationId}")
    public ResponseEntity<User> enrollInFormation(@PathVariable Long id, @PathVariable Long formationId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        FormationEntity formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found"));

        if (!user.getFormations().contains(formation)) {
            user.getFormations().add(formation);
            formation.getUsers().add(user);
            userRepository.save(user);
            formationRepository.save(formation);
        }
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{id}/formations/{formationId}")
    public ResponseEntity<User> removeFromFormation(@PathVariable Long id, @PathVariable Long formationId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        FormationEntity formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found"));

        user.getFormations().remove(formation);
        formation.getUsers().remove(user);
        userRepository.save(user);
        formationRepository.save(formation);

        return ResponseEntity.ok(user);
    }

    @PostMapping("/{id}/avis")
    public ResponseEntity<AvisEntity> addAvis(@PathVariable Long id, @RequestBody AvisEntity avis) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        avis.setUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(avisRepository.save(avis));
    }

    // ====================== AUTH ======================
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User loginData) {
        Optional<User> userOpt = userRepository.findByEmail(loginData.getEmail());
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(loginData.getPassword())) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}