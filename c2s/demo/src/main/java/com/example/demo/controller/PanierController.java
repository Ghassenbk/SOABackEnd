package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.PanierEntity;
import com.example.demo.model.SolutionEntity;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.SolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin(origins = "http://localhost:50930")
@RestController
@RequestMapping("/paniers")
public class PanierController{

    @Autowired
    private  PanierRepository panierRepository;
    @Autowired
    private SolutionRepository solutionRepository;

    @GetMapping
    public List<PanierEntity> getAllPaniers() {
        return panierRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PanierEntity> getPanierById(@PathVariable Long id) {
        PanierEntity panier = panierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Panier not found with ID " + id));
        return ResponseEntity.ok(panier);
    }

    @PostMapping
    public ResponseEntity<PanierEntity> createPanier() {
        PanierEntity panier = new PanierEntity();
        panier.setSolutions(new ArrayList<>());
        PanierEntity savedPanier = panierRepository.save(panier);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPanier);
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateCart(
            @PathVariable Long panierId,
            @RequestBody List<Long> solutionIds) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<SolutionEntity> solutions = solutionRepository.findAllById(solutionIds);
        panier.setSolutions(solutions);

        panierRepository.save(panier);

        return ResponseEntity.ok("Cart updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCart(@PathVariable Long panierId) {
        PanierEntity panier = panierRepository.findById(panierId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        panierRepository.delete(panier);

        return ResponseEntity.ok("Cart deleted successfully");
    }

    @PostMapping("/{id}/solutions/{solutionId}")
    public ResponseEntity<String> addSolutionToCart(
            @PathVariable Long id,
            @PathVariable Long solutionId) {

        PanierEntity panier = panierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));


        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("Solution not found"));


        panier.getSolutions().add(solution);

        panierRepository.save(panier);

        return ResponseEntity.ok("Solution added to cart successfully");
    }

    @DeleteMapping("/{id}/solutions/{solutionId}")
    public ResponseEntity<String> removeSolutionFromCart(
            @PathVariable Long id,
            @PathVariable Long solutionId) {
        PanierEntity panier = panierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new RuntimeException("Solution not found"));


        if (panier.getSolutions().remove(solution)) {
            panierRepository.save(panier);
            return ResponseEntity.ok("Solution removed from cart successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Solution not found in cart");
        }

    }

    @GetMapping("/{id}/solutions")
    public ResponseEntity<List<SolutionEntity>> getAllSolutionsInCart(@PathVariable Long id) {

        PanierEntity panier = panierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found"));


        return ResponseEntity.ok(panier.getSolutions());
    }

    //@GetMapping("/{id}/solutions")
    //public ResponseEntity<Object> getAllSolutionsInCartWithTotalPrice(@PathVariable Long id) {
       // PanierEntity panier = panierRepository.findById(id)
              //  .orElseThrow(() -> new RuntimeException("Cart not found"));

       // double totalPrice = panier.getSolutions().stream()
              //  .mapToDouble(SolutionEntity::getPrix)
              //  .sum();

        // Create a response containing solutions and total price
        //return ResponseEntity.ok(Map.of(
       //         "solutions", panier.getSolutions(),
          //      "totalPrice", totalPrice
       // ));
    //}






}
