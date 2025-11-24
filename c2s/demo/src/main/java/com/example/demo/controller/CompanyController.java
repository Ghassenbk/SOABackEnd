package com.example.demo.controller;

import com.example.demo.model.CompanyInfo;
import com.example.demo.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/company-info")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/{id}")
    public ResponseEntity<CompanyInfo> getCompanyInfoById(@PathVariable long id) {
        try {
            CompanyInfo info = companyService.getCompanyInfoById(id);
            return new ResponseEntity<>(info, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyInfo> updateCompanyInfo(@PathVariable long id, @RequestBody CompanyInfo updated) {
        try {
            CompanyInfo saved = companyService.updateCompanyInfo(id, updated);
            return new ResponseEntity<>(saved, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
