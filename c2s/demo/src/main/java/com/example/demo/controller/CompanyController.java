package com.example.demo.controller;

import com.example.demo.model.CompanyInfo;
import com.example.demo.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/company-info")
public class CompanyController {

    @Autowired
    private CompanyRepository companyRepository;

    // Endpoint to update CompanyInfo
    @PutMapping("/{id}")
    public ResponseEntity<CompanyInfo> updateCompanyInfo(@PathVariable long id, @RequestBody CompanyInfo updatedCompanyInfo) {
        Optional<CompanyInfo> existingCompanyInfoOpt = companyRepository.findById(id);

        if (existingCompanyInfoOpt.isPresent()) {
            CompanyInfo existingCompanyInfo = existingCompanyInfoOpt.get();

            existingCompanyInfo.setName(updatedCompanyInfo.getName());
            existingCompanyInfo.setAddress(updatedCompanyInfo.getAddress());
            existingCompanyInfo.setGmaps(updatedCompanyInfo.getGmaps());
            existingCompanyInfo.setPhone(updatedCompanyInfo.getPhone());
            existingCompanyInfo.setEmail(updatedCompanyInfo.getEmail());
            existingCompanyInfo.setFacebookurl(updatedCompanyInfo.getFacebookurl());
            existingCompanyInfo.setInstagramurl(updatedCompanyInfo.getInstagramurl());
            existingCompanyInfo.setTwitterurl(updatedCompanyInfo.getTwitterurl());
            existingCompanyInfo.setIframe(updatedCompanyInfo.getIframe());

            CompanyInfo savedCompanyInfo = companyRepository.save(existingCompanyInfo);
            return new ResponseEntity<>(savedCompanyInfo, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyInfo> getCompanyInfoById(@PathVariable long id) {
        Optional<CompanyInfo> companyInfoOpt = companyRepository.findById(id);

        if (companyInfoOpt.isPresent()) {
            return new ResponseEntity<>(companyInfoOpt.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
