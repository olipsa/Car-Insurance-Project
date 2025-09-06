package com.example.carins.web.controller;

import com.example.carins.service.ClaimService;
import com.example.carins.web.dto.ClaimCreateRequestDto;
import com.example.carins.web.dto.ClaimDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/cars/{carId}/claims")
public class ClaimController {
    private final ClaimService claimService;
    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ClaimDto> createClaim(@PathVariable Long carId, @RequestBody @Valid ClaimCreateRequestDto claimReqDto) {
        ClaimDto created = claimService.createClaim(carId, claimReqDto);
        return ResponseEntity.created(URI.create("/api/cars/"+carId+"/claims/"+created.id()))
                .body(created);
    }
}
