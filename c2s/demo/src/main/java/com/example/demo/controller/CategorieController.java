package com.example.demo.controller;

import com.example.demo.dto.CategorieWithCountDTO;
import com.example.demo.model.CategorieEntity;
import com.example.demo.service.CategorieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategorieController {

    @Autowired
    private CategorieService service;

    @GetMapping
    public List<CategorieWithCountDTO> getAllCategories() {
        return service.getAllCategories();
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CategorieEntity c) {
        try {
            CategorieEntity saved = service.createCategorie(c);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody CategorieEntity updated) {
        try {
            CategorieEntity saved = service.updateCategorie(id, updated);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            service.deleteCategorie(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
