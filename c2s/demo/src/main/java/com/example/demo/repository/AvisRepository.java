package com.example.demo.repository;

import com.example.demo.model.AvisEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AvisRepository extends JpaRepository<AvisEntity, Integer> {
    // Custom queries can be added here if needed.
}
