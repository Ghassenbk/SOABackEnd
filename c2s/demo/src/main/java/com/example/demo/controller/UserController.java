package com.example.demo.controller;

import com.example.demo.model.*;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    @Autowired
    private UserService userService;

    // ====================== USER CRUD ======================
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.status(201).body(userService.createUser(user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ====================== PANIER ======================
    @GetMapping("/{id}/panier")
    public ResponseEntity<?> getUserPanier(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserPanier(id));
    }

    @PostMapping("/{id}/panier/solutions/{solutionId}")
    public ResponseEntity<Map<String, Object>> addSolutionToPanier(
            @PathVariable Long id,
            @PathVariable Long solutionId) {
        return ResponseEntity.ok(userService.addSolutionToPanier(id, solutionId));
    }

    @DeleteMapping("/{id}/panier/solutions/{solutionId}")
    public ResponseEntity<PanierEntity> removeSolutionFromPanier(
            @PathVariable Long id,
            @PathVariable Integer solutionId) {
        return ResponseEntity.ok(userService.removeSolutionFromPanier(id, solutionId));
    }

    @DeleteMapping("/{id}/panier")
    public ResponseEntity<String> clearPanier(@PathVariable Long id) {
        userService.clearPanier(id);
        return ResponseEntity.ok("Panier vidé");
    }

    // ====================== FORMATIONS & AVIS ======================
    @GetMapping("/{id}/formations")
    public ResponseEntity<List<FormationEntity>> getUserFormations(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserFormations(id));
    }

    @PostMapping("/{id}/formations/{formationId}")
    public ResponseEntity<User> enrollInFormation(@PathVariable Long id, @PathVariable Long formationId) {
        return ResponseEntity.ok(userService.enrollInFormation(id, formationId));
    }

    @DeleteMapping("/{id}/formations/{formationId}")
    public ResponseEntity<User> removeFromFormation(@PathVariable Long id, @PathVariable Long formationId) {
        return ResponseEntity.ok(userService.removeFromFormation(id, formationId));
    }

    @PostMapping("/{id}/avis")
    public ResponseEntity<AvisEntity> addAvis(@PathVariable Long id, @RequestBody AvisEntity avis) {
        return ResponseEntity.status(201).body(userService.addAvis(id, avis));
    }

    // ====================== AUTH ======================
    @PostMapping("/login")
    public ResponseEntity<User> loginUser(@RequestBody User loginData) {
        return ResponseEntity.ok(userService.loginUser(loginData.getEmail(), loginData.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        return ResponseEntity.ok("Logged out successfully");
    }
}
