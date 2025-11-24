package com.example.demo.controller;

import com.example.demo.model.FournisseurEntity;
import com.example.demo.service.FournisseurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/fournisseurs")
public class FournisseurController {

    @Autowired
    private FournisseurService service;

    @GetMapping
    public List<FournisseurEntity> getAll() {
        return service.getAllFournisseurs();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody FournisseurEntity f) {
        try {
            FournisseurEntity saved = service.createFournisseur(f);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @Valid @RequestBody FournisseurEntity updated) {
        try {
            FournisseurEntity saved = service.updateFournisseur(id, updated);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.deleteFournisseur(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
