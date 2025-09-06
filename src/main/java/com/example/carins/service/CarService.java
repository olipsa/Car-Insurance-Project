package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.web.dto.CarHistoryDto;
import com.example.carins.web.dto.CarHistoryEventType;
import com.example.carins.web.exception.CarNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;
        // TODO: optionally throw NotFound if car does not exist
        return policyRepository.existsActiveOnDate(carId, date);
    }

    public List<CarHistoryDto> getCarHistory(Long carId){
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(carId));

        var claims = claimRepository.findByCarId(car.getId()).stream()
                .map(c -> new CarHistoryDto(CarHistoryEventType.CLAIM, "Claim related to "+ c.getDescription() + ", submitted on" + c.getClaimDate()+ " for total amount: " + c.getAmount(), c.getClaimDate()));

        var policies = policyRepository.findByCarId(car.getId()).stream()
                .map(p -> new CarHistoryDto(CarHistoryEventType.POLICY, "Policy with " + p.getProvider() + ", start date: " + p.getStartDate()+ ", end date: "+ p.getEndDate(), p.getStartDate()));

        return Stream.concat(claims, policies)
                .sorted(Comparator.comparing((CarHistoryDto::date))).toList();

    }
}
