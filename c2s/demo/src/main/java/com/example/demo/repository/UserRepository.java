package com.example.demo.repository;


import com.example.demo.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_formation WHERE formation_id = :formationId", nativeQuery = true)
    void deleteUserFormationLinks(@Param("formationId") Long formationId);


}
