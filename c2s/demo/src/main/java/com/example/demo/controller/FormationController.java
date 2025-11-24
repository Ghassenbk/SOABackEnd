package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.FormationEntity;
import com.example.demo.repository.FormationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/formations")
public class FormationController {

    @Autowired
    private FormationRepository formationRepository;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public List<FormationEntity> getAllFormations() {
        return formationRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormationEntity> getFormationById(@PathVariable Long id) {
        FormationEntity formation = formationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with ID " + id));
        return ResponseEntity.ok(formation);
    }

    @PostMapping
    public ResponseEntity<FormationEntity> createFormation(@Valid @RequestBody FormationEntity formationEntity) {
        if (formationRepository.existsByName(formationEntity.getName())) {
            throw new IllegalArgumentException("A formation with the name '" + formationEntity.getName() + "' already exists.");
        }
        FormationEntity savedEntity = formationRepository.save(formationEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FormationEntity> updateFormation(@PathVariable Long id, @Valid @RequestBody FormationEntity updatedFormation) {


        return formationRepository.findById(id).map(existingFormation -> {
            if (updatedFormation.getName() != null && !updatedFormation.getName().isEmpty()) {
                existingFormation.setName(updatedFormation.getName());
            }
            if (updatedFormation.getDateDebut() != null) {
                existingFormation.setDateDebut(updatedFormation.getDateDebut());
            }
            if (updatedFormation.getDateFin() != null) {
                existingFormation.setDateFin(updatedFormation.getDateFin());
            }
            if (updatedFormation.getDescription() != null && !updatedFormation.getDescription().isEmpty()) {
                existingFormation.setDescription(updatedFormation.getDescription());
            }
            if (updatedFormation.getPrix() != 0) { // Ensure 0 is not used as a default
                existingFormation.setPrix(updatedFormation.getPrix());
            }

            FormationEntity savedFormation = formationRepository.save(existingFormation);
            return ResponseEntity.ok(savedFormation);
        }).orElseThrow(() -> new ResourceNotFoundException("Formation not found with ID " + id));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFormation(@PathVariable Long id) {
        Optional<FormationEntity> formationOpt = formationRepository.findById(id);

        if (formationOpt.isPresent()) {
            FormationEntity formation = formationOpt.get();

            userRepository.deleteUserFormationLinks(id);

            formationRepository.delete(formation);

            return ResponseEntity.noContent().build();
        } else {
            throw new ResourceNotFoundException("Formation not found with ID " + id);
        }
    }




    @DeleteMapping
    public ResponseEntity<Void> deleteAllFormations() {
        formationRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
