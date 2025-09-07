package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyRequestDto;
import com.example.carins.web.dto.InsurancePolicyResponseDto;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidDateException;
import com.example.carins.web.exception.PolicyNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class InsurancePolicyService {

    private final InsurancePolicyRepository policyRepository;
    private final CarRepository carRepository;
    private final PolicyExpiryLogger policyExpiryLogger;

    public InsurancePolicyService(InsurancePolicyRepository policyRepository, CarRepository carRepository, PolicyExpiryLogger policyExpiryLogger) {
        this.policyRepository = policyRepository;
        this.carRepository = carRepository;
        this.policyExpiryLogger = policyExpiryLogger;
    }

    @Transactional
    public InsurancePolicyResponseDto createPolicy(InsurancePolicyRequestDto policyDto) {
        Car car = carRepository.findById(policyDto.carId())
                .orElseThrow(() -> new CarNotFoundException(policyDto.carId()));

        if(policyDto.startDate().isAfter(policyDto.endDate())){
            throw new InvalidDateException("Start date cannot be after end date. ",
                    "Provided start date ("+policyDto.startDate().toString() + ") is after provided end date (" + policyDto.endDate().toString()+")");
        }

        InsurancePolicy policy = new InsurancePolicy(car, policyDto.provider(), policyDto.startDate(), policyDto.endDate());

        policy = policyRepository.save(policy); // id auto-generated

        return new InsurancePolicyResponseDto(
                policy.getId(),
                policy.getCar().getId(),
                policy.getProvider(),
                policy.getStartDate(),
                policy.getEndDate());
    }

    @Transactional
    public InsurancePolicyResponseDto updatePolicy(long id, InsurancePolicyRequestDto policyDto) {
        InsurancePolicy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException(id));

        if (!policy.getCar().getId().equals(policyDto.carId())) {
            // provided car ID does not match with the one in the policy
            Car car = carRepository.findById(policyDto.carId())
                    .orElseThrow(() ->  new CarNotFoundException(policyDto.carId()));
            policy.setCar(car);
        }

        if(policyDto.startDate().isAfter(policyDto.endDate())){
            throw new InvalidDateException("Start date cannot be after end date. ",
                    "Provided start date ("+policyDto.startDate().toString() + ") is after provided end date (" + policyDto.endDate().toString()+")");
        }

        boolean endDateChanged = !policy.getEndDate().equals(policyDto.endDate());

        policy.setProvider(policyDto.provider());
        policy.setStartDate(policyDto.startDate());
        policy.setEndDate(policyDto.endDate());

        policy = policyRepository.save(policy);

        if(endDateChanged){
            // remove policy ID from the list of logged policy IDs if the end date was changed
            policyExpiryLogger.removeLoggedPolicyId(policy.getId());
        }


        return new InsurancePolicyResponseDto(
                policy.getId(),
                policy.getCar().getId(),
                policy.getProvider(),
                policy.getStartDate(),
                policy.getEndDate());
    }
}
