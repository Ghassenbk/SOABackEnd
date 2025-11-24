package com.example.demo.controller;

import com.example.demo.model.PaiementEntity;
import com.example.demo.service.PaiementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/paiements")
@CrossOrigin(origins = "http://localhost:4200")
public class PaiementController {

    @Autowired
    private PaiementService paiementService;

    // ====================== ADMIN ======================
    @GetMapping("/all-detailed")
    public ResponseEntity<List<Map<String, Object>>> getAllPaiementsDetailed() {
        return ResponseEntity.ok(paiementService.getAllPaiementsDetailed());
    }

    // ====================== PAIEMENT CLIENT ======================
    @PostMapping("/pay/{userId}")
    public ResponseEntity<?> processPaiement(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request
    ) {
        String card = request.get("card");
        try {
            Map<String, Object> response = paiementService.processPaiement(userId, card);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ====================== AUTRES ======================
    @GetMapping("/all")
    public List<PaiementEntity> getAllPaiements() {
        return paiementService.getAllPaiements();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePaiement(@PathVariable Long id) {
        paiementService.deletePaiement(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<Void> deleteAllPaiements() {
        paiementService.deleteAllPaiements();
        return ResponseEntity.noContent().build();
    }
}
