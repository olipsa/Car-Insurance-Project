package com.example.carins.web.controller;

import com.example.carins.service.InsurancePolicyService;

import com.example.carins.web.dto.InsurancePolicyDto;
import com.example.carins.web.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class InsurancePolicyController {

    private final InsurancePolicyService policyService;

    public InsurancePolicyController(InsurancePolicyService policyService) {
        this.policyService = policyService;
    }

    @PostMapping("/policies")
    @ResponseStatus(HttpStatus.CREATED)
    public InsurancePolicyDto createPolicy(@Valid @RequestBody InsurancePolicyDto policyDto) {
        return policyService.createPolicy(policyDto);
    }

    @PutMapping("/policies/{id}")
    public InsurancePolicyDto updatePolicy(@PathVariable("id")long id, @Valid @RequestBody InsurancePolicyDto policyDto) throws BadRequestException {
        return policyService.updatePolicy(id, policyDto);
    }

}
