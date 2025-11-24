package com.example.demo.repository;

import com.example.demo.model.PaiementEntity;
import com.example.demo.model.PanierEntity;
import com.example.demo.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaiementRepository extends JpaRepository<PaiementEntity, Long> {


    List<PaiementEntity> findAll();  // Default method to fetch all paiements

    List<PaiementEntity> findByUser(User user);

}
