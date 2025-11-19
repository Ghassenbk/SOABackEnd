package com.example.demo.repository;

import com.example.demo.model.FormationEntity;
import com.example.demo.model.SolutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface FormationRepository extends JpaRepository<FormationEntity, Long> {
    boolean existsByName(String name);
    boolean existsById(Long id);
}
