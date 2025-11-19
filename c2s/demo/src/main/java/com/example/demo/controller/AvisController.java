package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AvisEntity;
import com.example.demo.repository.AvisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:50930")
@RestController
@RequestMapping("/avis")
public class AvisController {

    @Autowired
    private AvisRepository avisRepository;

    @GetMapping
    public List<AvisEntity> getAllAvis() {
        return avisRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AvisEntity> getAvisById(@PathVariable int id) {
        AvisEntity avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis not found with ID " + id));
        return ResponseEntity.ok(avis);
    }

    @PostMapping
    public AvisEntity createAvis(@Valid @RequestBody AvisEntity avisEntity) {
        return avisRepository.save(avisEntity);
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAvis(@PathVariable int id) {
        if (avisRepository.existsById(id)) {
            avisRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("Avis not found with ID " + id);
        }
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllAvis() {
        avisRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AvisEntity> updateStatus(@PathVariable int id) {
        AvisEntity avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis not found with ID " + id));

        avis.setStatus(1);
        AvisEntity updatedAvis = avisRepository.save(avis);

        return ResponseEntity.ok(updatedAvis);
    }
    @PutMapping("/status")
    public ResponseEntity<Void> markAllAsRead() {
        List<AvisEntity> avisList = avisRepository.findAll();

        avisList.forEach(avis -> avis.setStatus(1));
        avisRepository.saveAll(avisList);

        return ResponseEntity.noContent().build();
    }
}