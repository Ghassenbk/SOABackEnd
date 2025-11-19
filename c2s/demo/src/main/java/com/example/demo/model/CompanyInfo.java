package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@Entity
public class CompanyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    private String address;

    private String gmaps;

    private String phone;

    private String email;

    private String facebookurl;

    private String instagramurl;

    private String twitterurl;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String iframe;



}
