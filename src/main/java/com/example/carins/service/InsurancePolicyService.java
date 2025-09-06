package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.InsurancePolicyDto;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.PolicyNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class InsurancePolicyService {

    private final InsurancePolicyRepository policyRepository;
    private final CarRepository carRepository;

    public InsurancePolicyService(InsurancePolicyRepository policyRepository, CarRepository carRepository) {
        this.policyRepository = policyRepository;
        this.carRepository = carRepository;
    }

    @Transactional
    public InsurancePolicyDto createPolicy(InsurancePolicyDto policyDto) {
        // TODO handle case when policy ID is provided => return 400 Bad Request
        Car car = carRepository.findById(policyDto.carId())
                .orElseThrow(() -> new CarNotFoundException(policyDto.carId()));

        InsurancePolicy policy = new InsurancePolicy(car, policyDto.provider(), policyDto.startDate(), policyDto.endDate());

        policy = policyRepository.save(policy); // id auto-generated

        return new InsurancePolicyDto(
                policy.getId(),
                policy.getCar().getId(),
                policy.getProvider(),
                policy.getStartDate(),
                policy.getEndDate());
    }

    @Transactional
    public InsurancePolicyDto updatePolicy(InsurancePolicyDto policyDto) {
        InsurancePolicy policy = policyRepository.findById(policyDto.id())
                .orElseThrow(() -> new PolicyNotFoundException(policyDto.id()
                ));

        if (!policy.getCar().getId().equals(policyDto.carId())) {
            Car car = carRepository.findById(policyDto.carId())
                    .orElseThrow(() ->  new CarNotFoundException(policyDto.carId()));
            policy.setCar(car);
        }

        policy.setProvider(policyDto.provider());
        policy.setStartDate(policyDto.startDate());
        policy.setEndDate(policyDto.endDate());

        policy = policyRepository.save(policy);


        return new InsurancePolicyDto(
                policy.getId(),
                policy.getCar().getId(),
                policy.getProvider(),
                policy.getStartDate(),
                policy.getEndDate());
    }
}
