package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.PaiementEntity;
import com.example.demo.model.SolutionEntity;
import com.example.demo.repository.PaiementRepository;
import com.example.demo.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:50930")
@RestController
@RequestMapping("/solutions")
public class SolutionController {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @GetMapping
    public List<SolutionEntity> getAllSolutions() {
        return solutionRepository.findAll();
    }
    

    @GetMapping("/{id}")
    public ResponseEntity<SolutionEntity> getSolutionById(@PathVariable Long id) {
        SolutionEntity solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));
        return ResponseEntity.ok(solution);
    }

    @PostMapping
    public ResponseEntity<SolutionEntity> createSolution(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("prix") float prix,
            @RequestParam("devis") MultipartFile devis,
            @RequestParam("image") MultipartFile image) {


        System.out.println("Received file: " + image.getOriginalFilename() + " Type: " + image.getContentType());


        List<String> allowedTypes = List.of("image/png", "image/jpeg", "image/jpg", "image/gif", "image/webp");


        if (image.isEmpty() || image.getContentType() == null || !allowedTypes.contains(image.getContentType())) {
            return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
        }


        String folder = "C:/SOAEcommerce/c2sproject/public";
        File directory = new File(folder);
        if (!directory.exists()) {
            directory.mkdirs();
        }


        String imagePath = folder + image.getOriginalFilename();
        try {
            Path path = Paths.get(imagePath);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


        String pdfFolder = "C:/SOAEcommerce/c2sproject/public";
        File pdfDirectory = new File(pdfFolder);
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdirs();
        }


        String pdfFilePath = pdfFolder + devis.getOriginalFilename();
        try {
            Path pdfPath = Paths.get(pdfFilePath);
            Files.copy(devis.getInputStream(), pdfPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }


        SolutionEntity solution = new SolutionEntity();
        solution.setName(name);
        solution.setDescription(description);
        solution.setPrix(prix);
        solution.setDevis("pdfs/"+devis.getOriginalFilename());
        solution.setImage(image.getOriginalFilename());

        SolutionEntity savedEntity = solutionRepository.save(solution);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedEntity);
    }




    @PutMapping("/{id}")
    public ResponseEntity<?> updateSolution(@PathVariable Long id,
                                                         @RequestParam("name") String name,
                                                         @RequestParam("description") String description,
                                                         @RequestParam("prix") float prix,
                                                         @RequestParam("devis") MultipartFile devis,
                                                         @RequestParam(value = "image", required = false) MultipartFile image) {
        String folder = "C:/SOAEcommerce/c2sproject/public";

        if (image != null && !image.isEmpty()) {
            String filePath = folder + image.getOriginalFilename();
            try {
                Files.copy(image.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        return solutionRepository.findById(id).map(existingSolution -> {
            if (name != null && !name.isEmpty() && !name.equals(existingSolution.getName())) {
                if (solutionRepository.existsByName(name)) {
                    throw new IllegalArgumentException("A Solution with the name '" + name + "' already exists.");
                }
                existingSolution.setName(name);
            }

            if (description != null && !description.isEmpty()) {
                existingSolution.setDescription(description);
            }

            if (devis != null && !devis.isEmpty()) {
                String pdfPath = "C:/SOAEcommerce/c2sproject/public/pdfs/" + devis.getOriginalFilename();
                try {
                    Files.copy(devis.getInputStream(), Paths.get(pdfPath), StandardCopyOption.REPLACE_EXISTING);
                    existingSolution.setDevis("pdfs/"+devis.getOriginalFilename());  // Update the PDF path
                } catch (IOException e) {
                    e.printStackTrace();
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }

            if (prix > 0) {
                existingSolution.setPrix(prix);
            }

            if (image != null && !image.isEmpty()) {
                existingSolution.setImage(image.getOriginalFilename());
            }


            SolutionEntity savedSolution = solutionRepository.save(existingSolution);
            return ResponseEntity.<SolutionEntity>ok(savedSolution);  // Explicit return type
        }).orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));
    }






    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSolution(@PathVariable Long id) {
        SolutionEntity solution = solutionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with ID " + id));


        List<PaiementEntity> paiements = paiementRepository.findAll();
        for (PaiementEntity paiement : paiements) {
            paiement.getSolutionsBought().remove(solution);
            paiementRepository.save(paiement);
        }


        solutionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping
    public ResponseEntity<Void> deleteAllSolutions() {
        solutionRepository.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
