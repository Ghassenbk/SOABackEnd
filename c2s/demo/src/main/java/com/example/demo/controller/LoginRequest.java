package com.example.demo.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class LoginRequest {
    private String email;
    private String password;

    // Getters and setters
}
