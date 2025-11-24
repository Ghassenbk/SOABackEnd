package com.example.demo.controller;

import com.example.demo.model.AvisEntity;
import com.example.demo.service.AvisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/avis")
public class AvisController {

    @Autowired
    private AvisService avisService;

    @GetMapping
    public List<AvisEntity> getAllAvis() {
        return avisService.getAllAvis();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvisEntity> getAvisById(@PathVariable int id) {
        AvisEntity avis = avisService.getAvisById(id);
        return ResponseEntity.ok(avis);
    }

    @PostMapping
    public AvisEntity createAvis(@Valid @RequestBody AvisEntity avisEntity) {
        return avisService.createAvis(avisEntity);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvis(@PathVariable int id) {
        avisService.deleteAvis(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllAvis() {
        avisService.deleteAllAvis();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AvisEntity> updateStatus(@PathVariable int id) {
        AvisEntity updatedAvis = avisService.updateStatus(id);
        return ResponseEntity.ok(updatedAvis);
    }

    @PutMapping("/status")
    public ResponseEntity<Void> markAllAsRead() {
        avisService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
