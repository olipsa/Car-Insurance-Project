package com.example.carins.web.controller;

import com.example.carins.model.Car;
import com.example.carins.service.CarService;
import com.example.carins.web.dto.*;
import com.example.carins.web.exception.InvalidDateException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CarController {

    private final CarService service;

    public CarController(CarService service) {
        this.service = service;
    }

    @GetMapping("/cars")
    public List<CarDto> getCars() {
        return service.listCars().stream().map(this::toDto).toList();
    }

    @GetMapping("/cars/{carId}/insurance-valid")
    public ResponseEntity<?> isInsuranceValid(@PathVariable Long carId, @RequestParam String date) {
        LocalDate reqDate;

        try {
            reqDate = LocalDate.parse(date);
        } catch (Exception ex) {
            throw new InvalidDateException(date);
        }

        if (reqDate.getYear() < 1900 || reqDate.getYear() > 2100) {
            throw new InvalidDateException("Impossible date provided: ", date);
        }

        boolean valid = service.isInsuranceValid(carId, reqDate);
        return ResponseEntity.ok(new InsuranceValidityResponse(carId, reqDate.toString(), valid));
    }

    @GetMapping("/cars/{carId}/history")
    public List<CarHistoryDto> getCarHistory(@PathVariable Long carId){
        return service.getCarHistory(carId);
    }

    @PostMapping("/cars")
    @ResponseStatus(HttpStatus.CREATED)
    public CarDto createCar(@Valid @RequestBody CarCreateRequestDto car){
        return service.createCar(car);

    }

    private CarDto toDto(Car c) {
        var o = c.getOwner();
        return new CarDto(c.getId(), c.getVin(), c.getMake(), c.getModel(), c.getYearOfManufacture(),
                o != null ? o.getId() : null,
                o != null ? o.getName() : null,
                o != null ? o.getEmail() : null);
    }

    public record InsuranceValidityResponse(Long carId, String date, boolean valid) {}
}
