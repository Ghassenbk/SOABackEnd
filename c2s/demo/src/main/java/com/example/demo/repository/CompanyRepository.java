package com.example.demo.repository;

import com.example.demo.model.CompanyInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyInfo,Long> {

    List<CompanyInfo> id(long id);
}
