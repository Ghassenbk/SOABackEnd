package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.PaiementEntity;
import com.example.demo.model.PanierEntity;
import com.example.demo.model.SolutionEntity;
import com.example.demo.model.User;
import com.example.demo.repository.PaiementRepository;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:50930")
@RestController
@RequestMapping("/paiements")
public class PaiementController {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/all-detailed")
    public ResponseEntity<List<Map<String, Object>>> getPaiementsWithDetails() {
        List<PaiementEntity> paiements = paiementRepository.findAll();
        List<Map<String, Object>> detailedPaiements = new ArrayList<>();

        for (PaiementEntity paiement : paiements) {
            Map<String, Object> paiementDetails = new HashMap<>();
            paiementDetails.put("id", paiement.getId());
            paiementDetails.put("userName", paiement.getUser().getName());
            paiementDetails.put("userEmail", paiement.getUser().getEmail());
            paiementDetails.put("totalPayed", paiement.getTotalPayed());

            List<String> solutionNames = paiement.getSolutionNames().stream()

                    .toList();
            paiementDetails.put("solutionNames", solutionNames);

            detailedPaiements.add(paiementDetails);
        }

        return ResponseEntity.ok(detailedPaiements);
    }


    @PostMapping("/pay/{userId}")
    public ResponseEntity<?> processPaiement(@PathVariable Long userId, @RequestBody PaiementEntity paiementRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID " + userId));

        // cherche le panier de l'utilisateur
        PanierEntity panier = panierRepository.findById(user.getPanier().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Panier not found for User with ID " + userId));


        if (panier.getSolutions() == null || panier.getSolutions().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Panier is empty. Nothing to pay for.");
        }

        double totalPayed = panier.getSolutions().stream()
                .mapToDouble(SolutionEntity::getPrix)
                .sum();

        paiementRequest.setTotalPayed(totalPayed);
        paiementRequest.setUser(user);
        paiementRequest.setPanier(panier);
        paiementRequest.setSolutionsBought(new ArrayList<>(panier.getSolutions()));
        paiementRequest.setSolutionNames(
                panier.getSolutions().stream().map(SolutionEntity::getName).toList()
        );


        if (paiementRequest.getCard() == null || paiementRequest.getCard().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Payment method (card) is required.");
        }

        PaiementEntity savedPaiement = paiementRepository.save(paiementRequest);

        panier.getSolutions().clear();
        panierRepository.save(panier);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedPaiement);
    }





    @GetMapping("/all")
    public List<PaiementEntity> getAllPaiements() {
        // Fetch all paiements from the repository
        return paiementRepository.findAll();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiementById(@PathVariable Long id) {
        PaiementEntity paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement not found with ID " + id));

        // Clear the relationship with solutions
        paiement.getSolutionsBought().clear();
        paiementRepository.save(paiement);

        // Delete the paiement
        paiementRepository.delete(paiement);
        return ResponseEntity.noContent().build();
    }


    // Supprimer tous les paiements
    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllPaiements() {
        paiementRepository.deleteAll();
        return ResponseEntity.noContent().build(); // Retourne un statut 204 (No Content)
    }
}
