package com.example.carins.web.dto;

import java.time.LocalDate;


public record CarHistoryDto (CarHistoryEventType type, String description, LocalDate date){
}
