package com.example.carins.service;

import com.example.carins.model.Car;
import com.example.carins.model.Owner;
import com.example.carins.repo.CarRepository;
import com.example.carins.repo.ClaimRepository;
import com.example.carins.repo.InsurancePolicyRepository;
import com.example.carins.repo.OwnerRepository;
import com.example.carins.web.dto.*;
import com.example.carins.web.exception.CarNotFoundException;
import com.example.carins.web.exception.InvalidCarVinException;
import com.example.carins.web.exception.OwnerNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final InsurancePolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final OwnerRepository ownerRepository;

    public CarService(CarRepository carRepository, InsurancePolicyRepository policyRepository, ClaimRepository claimRepository, OwnerRepository ownerRepository) {
        this.carRepository = carRepository;
        this.policyRepository = policyRepository;
        this.claimRepository = claimRepository;
        this.ownerRepository = ownerRepository;
    }

    public List<Car> listCars() {
        return carRepository.findAll();
    }

    public boolean isInsuranceValid(Long carId, LocalDate date) {
        if (carId == null || date == null) return false;

        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException(carId));

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

    @Transactional
    public CarDto createCar(CarCreateRequestDto car) {
        Owner owner = ownerRepository.findById(car.ownerId())
                .orElseThrow(() -> new OwnerNotFoundException(car.ownerId()));

        carRepository.findByVin(car.vin()).ifPresent(c -> {
            throw new InvalidCarVinException(car.vin());});

        Car newCar = new Car(car.vin(),car.make(),car.model(),car.yearOfManufacture(),owner);

        newCar = carRepository.save(newCar); // id auto-generated

        return new CarDto(
                newCar.getId(),
                newCar.getVin(),
                newCar.getMake(),
                newCar.getModel(),
                newCar.getYearOfManufacture(),
                newCar.getOwner().getId(),
                newCar.getOwner().getName(),
                newCar.getOwner().getEmail());
    }
}
