package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Claim;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.web.dto.ClaimCreateRequestDto;
import com.example.carins.web.dto.ClaimDto;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidDateException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final CarRepository carRepository;

    public ClaimService(ClaimRepository claimRepository, CarRepository carRepository) {
        this.claimRepository = claimRepository;
        this.carRepository = carRepository;
    }


    public ClaimDto createClaim(Long carId, ClaimCreateRequestDto claimReqDto) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(carId));

        LocalDate claimDate;
        try {
            claimDate = LocalDate.parse(claimReqDto.claimDate());
        } catch (Exception ex) {
            throw new InvalidDateException(claimReqDto.claimDate());
        }

        Claim claim = new Claim(car, claimDate, claimReqDto.description(), claimReqDto.amount());

        claim = claimRepository.save(claim);
        return new ClaimDto(claim.getId(),claim.getCar().getId(),claim.getClaimDate(),claim.getDescription(),claim.getAmount());
    }
}
