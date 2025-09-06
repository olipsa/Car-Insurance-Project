package com.example.carins.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ClaimCreateRequestDto(
    @NotNull(message = "Claim date is required")
    String claimDate,
    @NotBlank(message = "Description is required")
    String description,
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be positive")
    BigDecimal amount){ }
