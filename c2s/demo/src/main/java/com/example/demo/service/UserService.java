package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class UserService {

    @Autowired private UserRepository userRepository;
    @Autowired private FormationRepository formationRepository;
    @Autowired private AvisRepository avisRepository;
    @Autowired private PanierRepository panierRepository;
    @Autowired private ProduitRepository produitRepository;
    @Autowired private PaiementRepository paiementRepository;

    // ====================== USER CRUD ======================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        PanierEntity panier = new PanierEntity();
        user.setPanier(panier);
        user.setRole(Role.CLIENT);

        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) user.setName(updatedUser.getName());
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isBlank()) user.setEmail(updatedUser.getEmail());
        if (updatedUser.getPhone() != null && !updatedUser.getPhone().isBlank()) user.setPhone(updatedUser.getPhone());
        if (updatedUser.getAdresse() != null && !updatedUser.getAdresse().isBlank()) user.setAdresse(updatedUser.getAdresse());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) user.setPassword(updatedUser.getPassword());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Supprimer paiements
        paiementRepository.findByUser(user).forEach(paiementRepository::delete);

        // Supprimer panier
        if (user.getPanier() != null) {
            panierRepository.delete(user.getPanier());
        }

        userRepository.delete(user);
    }

    // ====================== PANIER ======================
    public Map<String, Object> getUserPanier(Long id) {
        User user = getUserById(id);
        PanierEntity panier = user.getPanier();
        if (panier == null) {
            return Map.of("solutionItems", new ArrayList<>());
        }

        List<Map<String, Object>> items = new ArrayList<>();
        for (PanierSolution ps : panier.getSolutionItems()) {
            Map<String, Object> item = new HashMap<>();
            item.put("quantite", ps.getQuantite());

            Map<String, Object> solution = new HashMap<>();
            solution.put("id", ps.getSolution().getId());
            solution.put("name", ps.getSolution().getName());
            solution.put("description", ps.getSolution().getDescription());
            solution.put("prix", ps.getSolution().getPrix());

            item.put("solution", solution);
            items.add(item);
        }

        return Map.of("solutionItems", items);
    }

    public Map<String, Object> addSolutionToPanier(Long userId, Long solutionId) {
        User user = getUserById(userId);
        ProduitEntity solution = produitRepository.findById(solutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Solution not found"));

        PanierEntity panier = user.getPanier();
        if (panier == null) {
            panier = new PanierEntity();
            user.setPanier(panier);
            userRepository.save(user);
        }

        Optional<PanierSolution> existing = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId()== solutionId )
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantite(existing.get().getQuantite() + 1);
        } else {
            PanierSolution newItem = new PanierSolution();
            newItem.setPanier(panier);
            newItem.setSolution(solution);
            newItem.setQuantite(1);
            panier.getSolutionItems().add(newItem);
        }

        panierRepository.save(panier);

        return Map.of(
                "success", true,
                "message", "Ajouté au panier avec succès !",
                "totalItems", panier.getSolutionItems().size()
        );
    }

    public PanierEntity removeSolutionFromPanier(Long userId, Integer solutionId) {
        User user = getUserById(userId);
        PanierEntity panier = user.getPanier();
        if (panier == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Panier vide");
        }

        Optional<PanierSolution> itemOpt = panier.getSolutionItems().stream()
                .filter(item -> item.getSolution().getId()== solutionId )
                .findFirst();

        if (itemOpt.isPresent()) {
            PanierSolution item = itemOpt.get();
            if (item.getQuantite() > 1) {
                item.setQuantite(item.getQuantite() - 1);
            } else {
                panier.getSolutionItems().remove(item);
            }
            panierRepository.save(panier);
        }

        return panier;
    }

    public void clearPanier(Long userId) {
        User user = getUserById(userId);
        if (user.getPanier() != null) {
            user.getPanier().getSolutionItems().clear();
            panierRepository.save(user.getPanier());
        }
    }

    // ====================== FORMATIONS & AVIS ======================
    public List<FormationEntity> getUserFormations(Long userId) {
        User user = getUserById(userId);
        return user.getFormations();
    }

    public User enrollInFormation(Long userId, Long formationId) {
        User user = getUserById(userId);
        FormationEntity formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found"));

        if (!user.getFormations().contains(formation)) {
            user.getFormations().add(formation);
            formation.getUsers().add(user);
            userRepository.save(user);
            formationRepository.save(formation);
        }
        return user;
    }

    public User removeFromFormation(Long userId, Long formationId) {
        User user = getUserById(userId);
        FormationEntity formation = formationRepository.findById(formationId)
                .orElseThrow(() -> new ResourceNotFoundException("Formation not found"));

        user.getFormations().remove(formation);
        formation.getUsers().remove(user);
        userRepository.save(user);
        formationRepository.save(formation);
        return user;
    }

    public AvisEntity addAvis(Long userId, AvisEntity avis) {
        User user = getUserById(userId);
        avis.setUser(user);
        return avisRepository.save(avis);
    }

    // ====================== AUTH ======================
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() && userOpt.get().getPassword().equals(password)) {
            return userOpt.get();
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
}
