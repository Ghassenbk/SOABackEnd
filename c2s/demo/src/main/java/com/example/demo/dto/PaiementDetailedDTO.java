package com.example.demo.dto;

import java.util.Date;
import java.util.List;

public record PaiementDetailedDTO(
        Long id,
        String userName,
        String userEmail,
        Double totalPayed,
        Date date,
        String card,
        List<String> solutionNames
) {}