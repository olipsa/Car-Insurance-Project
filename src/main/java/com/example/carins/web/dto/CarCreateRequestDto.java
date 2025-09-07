package com.example.carins.web.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record CarCreateRequestDto(
        @NotNull(message = "Vin is required") String vin,
        @NotBlank(message = "Make is required") String make,
        @NotBlank(message = "Model is required") String model,
        @NotNull(message = "Year is required") int yearOfManufacture,
        @NotNull(message = "Owner is required") Long ownerId) {
}
