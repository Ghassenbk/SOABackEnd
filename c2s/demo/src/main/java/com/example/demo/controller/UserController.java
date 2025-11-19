package com.example.demo.controller;


import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:50930")
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FormationRepository formationRepository;

    @Autowired
    private AvisRepository avisRepository;

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private SolutionRepository solutionRepository;

    @Autowired
    private PaiementRepository paiementRepository;




    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        return ResponseEntity.ok(user);
    }



    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("A user with the email " + user.getEmail() + " already exists.");
        }
        PanierEntity panier = new PanierEntity();
        panier.setSolutions(new ArrayList<>());
        user.setPanier(panier);
        user.setRole(Role.CLIENT);
        user.setPassword(user.getPassword());

        User savedUser = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }



    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (updatedUser.getName() != null && !updatedUser.getName().isEmpty()) {
            existingUser.setName(updatedUser.getName());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().isEmpty()) {
            existingUser.setPhone(updatedUser.getPhone());
        }
        if (updatedUser.getAdresse() != null && !updatedUser.getAdresse().isEmpty()) {
            existingUser.setAdresse(updatedUser.getAdresse());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(updatedUser.getPassword());
        }

        User savedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(savedUser);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllUsers() {
        try {
            List<User> users = userRepository.findAll();

            if (users.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            for (User user : users) {
                List<PaiementEntity> paiements = paiementRepository.findByUser(user);
                if (paiements != null && !paiements.isEmpty()) {
                    paiementRepository.deleteAll(paiements);
                }

                PanierEntity panier = user.getPanier();
                if (panier != null) {
                    panierRepository.delete(panier);
                }
            }

            userRepository.deleteAll();

            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting users: " + ex.getMessage());
        }
    }




    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));


        List<PaiementEntity> paiements = paiementRepository.findByUser(user);
        if (paiements != null && !paiements.isEmpty()) {
            paiementRepository.deleteAll(paiements);
        }

        PanierEntity panier = user.getPanier();
        if (panier != null) {
            panierRepository.delete(panier);
        }

        userRepository.delete(user);

        return ResponseEntity.noContent().build();
    }







    // les formation dans lesquels un client est abonné
    @GetMapping("/{id}/formations")
    public ResponseEntity<List<FormationEntity>> getUserFormations(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        return ResponseEntity.ok(user.getFormations());
    }

    @GetMapping("/{id}/avis")
    public ResponseEntity<List<AvisEntity>> getUserAvis(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        return ResponseEntity.ok(user.getAvis());
    }


    // le contenu d'un panier d'un utilisateur
    @GetMapping("/{id}/panier")
    public ResponseEntity<PanierEntity> getUserPanier(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            throw new ResourceNotFoundException("No cart found for user with id " + id);
        }

        return ResponseEntity.ok(panier);
    }

    //s'inscrire a une fromation
    @PostMapping("/{id}/formations/{formationId}")
    public ResponseEntity<User> enrollInFormation(@PathVariable Long id, @PathVariable Long formationId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        FormationEntity formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with id " + formationId));

        // inscrit dans la formation??
        if (user.getFormations().contains(formation)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        }

        user.getFormations().add(formation);
        formation.getUsers().add(user);

        userRepository.save(user);
        formationRepository.save(formation);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    //envoyer un avis
    @PostMapping("/{id}/avis")
    public ResponseEntity<AvisEntity> addAvis(@PathVariable Long id, @RequestBody AvisEntity avisEntity) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));


        avisEntity.setUser(user);

        AvisEntity savedAvis = avisRepository.save(avisEntity);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedAvis);
    }

    //se desabonner a la formation
    @DeleteMapping("/{id}/formations/{formationId}")
    public ResponseEntity<User> removeFromFormation(@PathVariable Long id, @PathVariable Long formationId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        FormationEntity formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found with id " + formationId));

        if (!user.getFormations().contains(formation)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);  // deja desabonné
        }

        user.getFormations().remove(formation);
        formation.getUsers().remove(user);

        userRepository.save(user);
        formationRepository.save(formation);

        return ResponseEntity.ok(user);
    }

    //ajouter une solution a son panier
    @PostMapping("/{id}/panier/solutions/{solutionId}")
    public ResponseEntity<PanierEntity> addSolutionToPanier(@PathVariable Long id, @PathVariable Long solutionId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id " + solutionId));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            // cree un panier si n'existe pas
            panier = new PanierEntity();
            panier.setSolutions(new ArrayList<>());
            user.setPanier(panier);
        }

        //ajouter la solution
        if (!panier.getSolutions().contains(solution)) {
            panier.getSolutions().add(solution);
        }

        panierRepository.save(panier);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(panier);
    }

    //supprimer la solution de son panier
    @DeleteMapping("/{id}/panier/solutions/{solutionId}")
    public ResponseEntity<PanierEntity> removeSolutionFromPanier(@PathVariable Long id, @PathVariable Long solutionId) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        SolutionEntity solution = solutionRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found with id " + solutionId));

        PanierEntity panier = user.getPanier();
        if (panier == null || !panier.getSolutions().contains(solution)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);  //la solution n'est pas dans le panier
        }

        panier.getSolutions().remove(solution);
        panierRepository.save(panier);

        return ResponseEntity.ok(panier);
    }

    @PostMapping("/{id}/panier")
    public ResponseEntity<PanierEntity> createOrUpdatePanier(@PathVariable Long id, @RequestBody PanierEntity panierEntity) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            panier = new PanierEntity();
            user.setPanier(panier);
        }


        panier.setSolutions(panierEntity.getSolutions());
        panierRepository.save(panier);
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(panier);
    }



    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User loginData) {
        Optional<User> userOpt = userRepository.findByEmail(loginData.getEmail());
        System.out.println(loginData.getPassword());
        System.out.println(loginData.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            if (user.getPassword().equals(loginData.getPassword())) {
                return ResponseEntity.ok(user);
            } else {
                System.out.println("Password mismatch.");
            }
        } else {
            System.out.println("User not found.");
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");

    }



    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully.");
    }












}
