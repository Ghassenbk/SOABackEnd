package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    @Autowired
    private JavaMailSender mailSender;

    // ====================== USER CRUD ======================
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    // Updated createUser method
    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        PanierEntity panier = new PanierEntity();
        user.setPanier(panier);
        user.setRole(Role.CLIENT);

        User savedUser = userRepository.save(user);

        // Send welcome email
        sendWelcomeEmail(savedUser);

        return savedUser;
    }

    // Welcome email method
    private void sendWelcomeEmail(User user) {
        String htmlContent = buildWelcomeEmailHtml(user);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(user.getEmail());
            helper.setSubject("Bienvenue chez Lmarchi ! 🎉");
            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Log the error but don't fail user creation
            System.err.println("Failed to send welcome email to: " + user.getEmail());
        }
    }

    // Build welcome email HTML
    private String buildWelcomeEmailHtml(User user) {
        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "</head>" +
                        "<body style='margin: 0; padding: 0; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +

                        "<!-- Main Container -->" +
                        "<div style='max-width: 600px; margin: 40px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +

                        "<!-- Header with Gradient -->" +
                        "<div style='background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 40px 30px; text-align: center;'>" +
                        "<h1 style='margin: 0; color: white; font-size: 32px; letter-spacing: 2px;'>LMARCHI</h1>" +
                        "<p style='margin: 10px 0 0 0; color: rgba(255,255,255,0.9); font-size: 16px;'>Votre marketplace de confiance</p>" +
                        "</div>" +

                        "<!-- Content Section -->" +
                        "<div style='padding: 40px 30px;'>" +

                        "<!-- Welcome Message -->" +
                        "<div style='text-align: center; margin-bottom: 30px;'>" +
                        "<h2 style='margin: 0 0 15px 0; color: #333; font-size: 28px;'>Bienvenue %s ! 🎉</h2>" +
                        "<p style='margin: 0; color: #666; font-size: 16px; line-height: 1.6;'>" +
                        "Nous sommes ravis de vous compter parmi nous !<br>" +
                        "Votre compte a été créé avec succès." +
                        "</p>" +
                        "</div>" +

                        "<!-- Features Section -->" +
                        "<div style='background: #f8f9fa; border-radius: 8px; padding: 25px; margin: 30px 0;'>" +
                        "<h3 style='margin: 0 0 20px 0; color: #333; font-size: 18px; text-align: center;'>Ce que vous pouvez faire maintenant :</h3>" +

                        "<div style='margin: 15px 0;'>" +
                        "<div style='display: inline-block; width: 40px; height: 40px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 50%%; text-align: center; line-height: 40px; color: white; font-size: 20px; margin-right: 15px; vertical-align: middle;'>🛍️</div>" +
                        "<div style='display: inline-block; vertical-align: middle; width: calc(100%% - 60px);'>" +
                        "<strong style='color: #333; font-size: 15px;'>Explorer nos produits</strong><br>" +
                        "<span style='color: #666; font-size: 14px;'>Découvrez notre large sélection</span>" +
                        "</div>" +
                        "</div>" +

                        "<div style='margin: 15px 0;'>" +
                        "<div style='display: inline-block; width: 40px; height: 40px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 50%%; text-align: center; line-height: 40px; color: white; font-size: 20px; margin-right: 15px; vertical-align: middle;'>🛒</div>" +
                        "<div style='display: inline-block; vertical-align: middle; width: calc(100%% - 60px);'>" +
                        "<strong style='color: #333; font-size: 15px;'>Ajouter au panier</strong><br>" +
                        "<span style='color: #666; font-size: 14px;'>Votre panier personnel est prêt</span>" +
                        "</div>" +
                        "</div>" +

                        "<div style='margin: 15px 0;'>" +
                        "<div style='display: inline-block; width: 40px; height: 40px; background: linear-gradient(135deg, #667eea, #764ba2); border-radius: 50%%; text-align: center; line-height: 40px; color: white; font-size: 20px; margin-right: 15px; vertical-align: middle;'>💳</div>" +
                        "<div style='display: inline-block; vertical-align: middle; width: calc(100%% - 60px);'>" +
                        "<strong style='color: #333; font-size: 15px;'>Paiement sécurisé</strong><br>" +
                        "<span style='color: #666; font-size: 14px;'>Commandez en toute sécurité</span>" +
                        "</div>" +
                        "</div>" +

                        "</div>" +

                        "<!-- CTA Button -->" +
                        "<div style='text-align: center; margin: 30px 0;'>" +
                        "<a href='https://votre-site.com' style='display: inline-block; background: linear-gradient(135deg, #667eea, #764ba2); color: white; text-decoration: none; padding: 15px 40px; border-radius: 25px; font-size: 16px; font-weight: bold; box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);'>" +
                        "Commencer mes achats" +
                        "</a>" +
                        "</div>" +

                        "<!-- Account Info -->" +
                        "<div style='background: #fff9e6; border-left: 4px solid #ffc107; padding: 15px; border-radius: 5px; margin: 25px 0;'>" +
                        "<p style='margin: 0; color: #856404; font-size: 14px;'>" +
                        "<strong>📧 Votre email :</strong> %s<br>" +
                        "<strong>👤 Votre rôle :</strong> Client" +
                        "</p>" +
                        "</div>" +

                        "<!-- Support Section -->" +
                        "<div style='text-align: center; margin-top: 30px; padding-top: 25px; border-top: 1px solid #eee;'>" +
                        "<p style='margin: 0 0 10px 0; color: #666; font-size: 14px;'>Besoin d'aide ?</p>" +
                        "<p style='margin: 0; color: #667eea; font-size: 14px;'>" +
                        "📧 support@lmarchi.com | 📞 +216 22 877 662" +
                        "</p>" +
                        "</div>" +

                        "</div>" +

                        "<!-- Footer -->" +
                        "<div style='background: #f8f9fa; padding: 25px 30px; text-align: center; border-top: 1px solid #eee;'>" +
                        "<p style='margin: 0 0 10px 0; color: #999; font-size: 12px;'>" +
                        "© 2024 Lmarchi. Tous droits réservés." +
                        "</p>" +
                        "<p style='margin: 0; color: #999; font-size: 12px;'>" +
                        "Tunis, Tunisia" +
                        "</p>" +
                        "</div>" +

                        "</div>" +
                        "</body>" +
                        "</html>",
                user.getName(),
                user.getEmail()
        );
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
