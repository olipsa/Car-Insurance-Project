package com.example.carins.web.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record InsurancePolicyRequestDto(
        @NotNull(message = "carId is required") Long carId,
        @NotNull(message = "provider is required") String provider,
        @NotNull(message = "startDate is required") LocalDate startDate,
        @NotNull(message = "endDate is required") LocalDate endDate
){}
