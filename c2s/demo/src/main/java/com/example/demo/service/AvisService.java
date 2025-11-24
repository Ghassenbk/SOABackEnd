package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AvisEntity;
import com.example.demo.repository.AvisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AvisService {

    @Autowired
    private AvisRepository avisRepository;

    // ====================== GET ALL ======================
    public List<AvisEntity> getAllAvis() {
        return avisRepository.findAll();
    }

    // ====================== GET BY ID ======================
    public AvisEntity getAvisById(int id) {
        return avisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis not found with ID " + id));
    }

    // ====================== CREATE ======================
    public AvisEntity createAvis(AvisEntity avis) {
        return avisRepository.save(avis);
    }

    // ====================== DELETE ======================
    public void deleteAvis(int id) {
        if (avisRepository.existsById(id)) {
            avisRepository.deleteById(id);
        } else {
            throw new ResourceNotFoundException("Avis not found with ID " + id);
        }
    }

    public void deleteAllAvis() {
        avisRepository.deleteAll();
    }

    // ====================== UPDATE STATUS ======================
    public AvisEntity updateStatus(int id) {
        AvisEntity avis = avisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Avis not found with ID " + id));

        avis.setStatus(1);
        return avisRepository.save(avis);
    }

    public void markAllAsRead() {
        List<AvisEntity> avisList = avisRepository.findAll();
        avisList.forEach(avis -> avis.setStatus(1));
        avisRepository.saveAll(avisList);
    }
}
