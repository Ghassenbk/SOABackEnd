package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.PaiementRepository;
import com.example.demo.repository.PanierRepository;
import com.example.demo.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaiementService {

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private PanierRepository panierRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender; // ← injected

    // ====================== ADMIN : Tous les paiements détaillés ======================
    public List<Map<String, Object>> getAllPaiementsDetailed() {
        List<PaiementEntity> paiements = paiementRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (PaiementEntity p : paiements) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("userName", p.getUser().getName());
            item.put("userEmail", p.getUser().getEmail());
            item.put("totalPayed", p.getTotalPayed());
            item.put("date", p.getDate());
            item.put("card", p.getCard());

            List<String> solutionDetails = new ArrayList<>();
            for (PaiementItem pi : p.getItems()) {
                solutionDetails.add(
                        pi.getSolution().getName() +
                                " (x" + pi.getQuantite() +
                                ") - " + (pi.getQuantite() * pi.getPrixUnitaire()) + " DT"
                );
            }
            item.put("solutionNames", solutionDetails);
            result.add(item);
        }

        return result;
    }

    // ====================== PAIEMENT CLIENT ======================
    public Map<String, Object> processPaiement(Long userId, String card) {
        if (card == null || card.trim().isEmpty()) {
            throw new IllegalArgumentException("Méthode de paiement requise.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        PanierEntity panier = user.getPanier();
        if (panier == null || panier.getSolutionItems().isEmpty()) {
            throw new IllegalArgumentException("Votre panier est vide.");
        }

        double total = panier.getSolutionItems().stream()
                .mapToDouble(ps -> ps.getQuantite() * ps.getSolution().getPrix())
                .sum();

        PaiementEntity paiement = new PaiementEntity();
        paiement.setUser(user);
        paiement.setPanier(panier);
        paiement.setCard(card);
        paiement.setTotalPayed(total);
        paiement.setDate(new Date());

        List<PaiementItem> boughtItems = new ArrayList<>();
        for (PanierSolution ps : panier.getSolutionItems()) {
            PaiementItem item = new PaiementItem();
            item.setSolution(ps.getSolution());
            item.setQuantite(ps.getQuantite());
            item.setPrixUnitaire(ps.getSolution().getPrix());
            boughtItems.add(item);
        }
        paiement.setItems(boughtItems);

        paiement.getSolutionNames().addAll(
                panier.getSolutionItems().stream()
                        .map(ps -> ps.getSolution().getName() + " (x" + ps.getQuantite() + ")")
                        .toList()
        );

        PaiementEntity saved = paiementRepository.save(paiement);

        // Vider le panier
        panier.getSolutionItems().clear();
        panierRepository.save(panier);

        // Envoyer le reçu par email
        sendReceiptEmail(user.getEmail(), saved);

        return Map.of(
                "message", "Paiement réussi !",
                "total", total,
                "paiementId", saved.getId()
        );
    }

    // ====================== MÉTHODE D’ENVOI EMAIL ======================
    private void sendReceiptEmail(String toEmail, PaiementEntity paiement) {
        String htmlContent = buildReceiptHtml(paiement);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Votre reçu de paiement Lmarchi - #" + paiement.getId());
            helper.setText(htmlContent, true); // true indicates HTML

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
            // Handle exception appropriately
        }
    }

    private String buildReceiptHtml(PaiementEntity paiement) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String formattedDate = dateFormat.format(paiement.getDate());

        StringBuilder itemsHtml = new StringBuilder();
        for (PaiementItem item : paiement.getItems()) {
            double itemTotal = item.getQuantite() * item.getPrixUnitaire();
            itemsHtml.append(String.format(
                    "<tr>" +
                            "<td style='padding: 8px 0; border-bottom: 1px dashed #ddd;'>%s</td>" +
                            "<td style='padding: 8px 0; border-bottom: 1px dashed #ddd; text-align: center;'>%d</td>" +
                            "<td style='padding: 8px 0; border-bottom: 1px dashed #ddd; text-align: right;'>%.2f</td>" +
                            "</tr>",
                    item.getSolution().getName(),
                    item.getQuantite(),
                    itemTotal
            ));
        }

        return String.format(
                "<!DOCTYPE html>" +
                        "<html>" +
                        "<head>" +
                        "<meta charset='UTF-8'>" +
                        "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                        "</head>" +
                        "<body style='margin: 0; padding: 20px; font-family: Arial, sans-serif; background-color: #f5f5f5;'>" +
                        "<div style='max-width: 400px; margin: 0 auto; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; border-radius: 10px;'>" +

                        "<!-- Receipt Paper -->" +
                        "<div style='background: white; padding: 30px 25px; border-radius: 5px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>" +

                        "<!-- Header -->" +
                        "<div style='text-align: center; border-bottom: 2px dashed #ddd; padding-bottom: 15px; margin-bottom: 20px;'>" +
                        "<h1 style='margin: 0; font-size: 24px; color: #333; letter-spacing: 2px;'>LMARCHI</h1>" +
                        "<p style='margin: 5px 0; font-size: 12px; color: #888;'>Tunis, Tunisia</p>" +
                        "<p style='margin: 5px 0; font-size: 12px; color: #888;'>Tel: +216 22 877 662</p>" +
                        "</div>" +

                        "<!-- Receipt Title -->" +
                        "<div style='text-align: center; margin: 20px 0;'>" +
                        "<h2 style='margin: 0; font-size: 16px; color: #666; letter-spacing: 1px;'>REÇU DE PAIEMENT</h2>" +
                        "<p style='margin: 5px 0; font-size: 12px; color: #999;'>N° %d</p>" +
                        "</div>" +

                        "<!-- Divider -->" +
                        "<div style='border-bottom: 2px dashed #ddd; margin: 20px 0;'></div>" +

                        "<!-- Items Table -->" +
                        "<table style='width: 100%%; border-collapse: collapse; margin: 20px 0;'>" +
                        "<thead>" +
                        "<tr style='border-bottom: 2px solid #ddd;'>" +
                        "<th style='padding: 8px 0; text-align: left; font-size: 12px; color: #666;'>Description</th>" +
                        "<th style='padding: 8px 0; text-align: center; font-size: 12px; color: #666;'>Qté</th>" +
                        "<th style='padding: 8px 0; text-align: right; font-size: 12px; color: #666;'>Prix</th>" +
                        "</tr>" +
                        "</thead>" +
                        "<tbody style='font-size: 13px; color: #333;'>" +
                        "%s" +
                        "</tbody>" +
                        "</table>" +

                        "<!-- Divider -->" +
                        "<div style='border-bottom: 2px dashed #ddd; margin: 20px 0;'></div>" +

                        "<!-- Total Section -->" +
                        "<div style='margin: 20px 0;'>" +
                        "<div style='display: flex; justify-content: space-between; align-items: center; margin: 10px 0;'>" +
                        "<span style='font-size: 18px; font-weight: bold; color: #333;'>Total</span>" +
                        "<span style='font-size: 24px; font-weight: bold; color: #667eea;'>%.2f DT</span>" +
                        "</div>" +
                        "<div style='display: flex; justify-content: space-between; font-size: 13px; color: #888; margin: 5px 0;'>" +
                        "<span>Espèces</span>" +
                        "<span>%.2f DT</span>" +
                        "</div>" +
                        "</div>" +

                        "<!-- Divider -->" +
                        "<div style='border-bottom: 2px dashed #ddd; margin: 20px 0;'></div>" +

                        "<!-- Payment Info -->" +
                        "<div style='font-size: 12px; color: #888; margin: 15px 0;'>" +
                        "<p style='margin: 5px 0;'><strong>Client:</strong> %s</p>" +
                        "<p style='margin: 5px 0;'><strong>Date:</strong> %s</p>" +
                        "<p style='margin: 5px 0;'><strong>ID Paiement:</strong> #%d</p>" +
                        "</div>" +

                        "<!-- Thank You Message -->" +
                        "<div style='text-align: center; margin: 25px 0 20px 0;'>" +
                        "<h3 style='margin: 0; font-size: 16px; color: #333; letter-spacing: 1px;'>MERCI!</h3>" +
                        "<p style='margin: 10px 0 0 0; font-size: 12px; color: #888;'>Merci de votre confiance</p>" +
                        "</div>" +

                        "<!-- Barcode Placeholder -->" +
                        "<div style='text-align: center; margin: 20px 0;'>" +
                        "<div style='display: inline-block; padding: 10px; background: white; border: 1px solid #ddd;'>" +
                        "<svg width='200' height='50' style='display: block;'>" +
                        "<rect x='0' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='5' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='10' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='16' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='20' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='26' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='30' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='36' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='42' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='48' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='52' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='58' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='64' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='68' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='74' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='80' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='86' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='90' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='96' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='102' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='108' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='114' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='120' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='126' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='132' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='138' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='144' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='150' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='156' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='162' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='168' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='174' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='180' y='0' width='4' height='50' fill='black'/>" +
                        "<rect x='186' y='0' width='2' height='50' fill='black'/>" +
                        "<rect x='192' y='0' width='3' height='50' fill='black'/>" +
                        "<rect x='197' y='0' width='2' height='50' fill='black'/>" +
                        "</svg>" +
                        "</div>" +
                        "</div>" +

                        "</div>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                paiement.getId(),
                itemsHtml.toString(),
                paiement.getTotalPayed(),
                paiement.getTotalPayed(),
                paiement.getUser().getName(),
                formattedDate,
                paiement.getId()
        );
    }

    // ====================== AUTRES ======================
    public List<PaiementEntity> getAllPaiements() {
        return paiementRepository.findAll();
    }

    public void deletePaiement(Long id) {
        PaiementEntity paiement = paiementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement non trouvé"));

        paiement.getItems().clear();
        paiementRepository.save(paiement);
        paiementRepository.delete(paiement);
    }

    public void deleteAllPaiements() {
        paiementRepository.deleteAll();
    }
}
