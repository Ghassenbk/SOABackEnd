package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.CompanyInfo;
import com.example.demo.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    // ====================== GET BY ID ======================
    public CompanyInfo getCompanyInfoById(long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyInfo not found with ID " + id));
    }

    // ====================== UPDATE ======================
    public CompanyInfo updateCompanyInfo(long id, CompanyInfo updated) {
        CompanyInfo existing = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CompanyInfo not found with ID " + id));

        existing.setName(updated.getName());
        existing.setAddress(updated.getAddress());
        existing.setGmaps(updated.getGmaps());
        existing.setPhone(updated.getPhone());
        existing.setEmail(updated.getEmail());
        existing.setFacebookurl(updated.getFacebookurl());
        existing.setInstagramurl(updated.getInstagramurl());
        existing.setTwitterurl(updated.getTwitterurl());
        existing.setIframe(updated.getIframe());

        return companyRepository.save(existing);
    }
}
