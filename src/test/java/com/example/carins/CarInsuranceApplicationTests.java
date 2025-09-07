package com.example.carins;

import com.example.carins.service.CarService;
import com.example.carins.web.dto.CarHistoryEventType;
import com.example.carins.web.exception.CarNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class CarInsuranceApplicationTests {

    @Autowired
    CarService service;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;


    // Tests for insurance validity
    @Test
    void insuranceValidityBasic() {
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2024-06-01")));
        assertTrue(service.isInsuranceValid(1L, LocalDate.parse("2025-06-01")));
        assertFalse(service.isInsuranceValid(2L, LocalDate.parse("2025-02-01")));
        assertThrows(CarNotFoundException.class, () -> service.isInsuranceValid(999L, LocalDate.parse("2025-02-01")));
    }

    @Test
    void isInsuranceValid_validRequest_returnsOkAndValidity() throws Exception {
        mvc.perform(get("/api/cars/1/insurance-valid")
                        .param("date", "2024-06-01"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.carId").value(1))
                .andExpect(jsonPath("$.date").value("2024-06-01"))
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void isInsuranceValid_invalidCarId_returnsNotFound() throws Exception {
        mvc.perform(get("/api/cars/9999/insurance-valid")
                        .param("date", "2024-06-01"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("does not exist")));
    }

    @Test
    void isInsuranceValid_malformedDate_returnsBadRequest() throws Exception {
        mvc.perform(get("/api/cars/1/insurance-valid")
                        .param("date", "20240-99-AA"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("invalid date")));
    }

    @Test
    void isInsuranceValid_impossibleDate_returnsBadRequest() throws Exception {
        // Year before 1900
        mvc.perform(get("/api/cars/1/insurance-valid")
                        .param("date", "1800-01-01"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("impossible date")));

        // Year after 2100
        mvc.perform(get("/api/cars/1/insurance-valid")
                        .param("date", "2200-03-09"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("impossible date")));
    }


    // Tests for Insurance Policy POST method
    @Test void createPolicy_withoutEndDate_returnsBadRequest() throws Exception{
        Map<String, Object > policyHashMap = new HashMap<String, Object>();

        policyHashMap.put("carId", 1L);
        policyHashMap.put("provider", "Omniasig");
        policyHashMap.put("startDate", "2024-06-01");
        // No endDate

        mvc.perform(post("/api/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("endDate")));
    }

    @Test
    void createPolicy_withEndDate_returnsCreated() throws Exception {
        var policyHashMap = new HashMap<String, Object>();
        policyHashMap.put("carId", 2L);
        policyHashMap.put("provider", "Otto Broker");
        policyHashMap.put("startDate", "2025-09-07");
        policyHashMap.put("endDate", "2026-09-07");

        mvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.endDate", Matchers.is("2026-09-07")));
    }

    @Test
    void createPolicy_withInvalidDate_returnsBadRequest() throws Exception {
        var policyHashMap = new HashMap<String, Object>();
        policyHashMap.put("carId", 1L);
        policyHashMap.put("provider", "Omniasig");
        policyHashMap.put("startDate", "202-06-01");
        policyHashMap.put("endDate", "2023-06-01");

        mvc.perform(post("/api/policies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("failed to deserialize")));
    }

    @Test
    void createPolicy_withInvalidCarId_returnsNotFound() throws Exception {
        var policyHashMap = new HashMap<String, Object>();
        policyHashMap.put("carId", 100L);
        policyHashMap.put("provider", "Asirom");
        policyHashMap.put("startDate", "2022-06-01");
        policyHashMap.put("endDate", "2023-06-01");

        mvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("does not exist")));
    }

    @Test
    void createPolicy_withStartDateAfterEndDate_returnsBadRequest() throws Exception {
        var policyHashMap = new HashMap<String, Object>();
        policyHashMap.put("carId", 1L);
        policyHashMap.put("provider", "Omniasig");
        policyHashMap.put("startDate", "2025-09-10");
        policyHashMap.put("endDate", "2025-09-01");

        mvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("start date cannot be after end date")));
    }


    // Tests for Insurance Policy PUT method
    @Test
    void updatePolicy_withoutEndDate_returnsBadRequest() throws Exception {
        // Create a valid policy
        var validPolicy = new HashMap<String, Object>();
        validPolicy.put("carId", 1L);
        validPolicy.put("provider", "Asirom");
        validPolicy.put("startDate", "2024-08-01");
        validPolicy.put("endDate", "2025-08-01");

        String response = mvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPolicy)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long policyId = objectMapper.readTree(response).get("id").asLong();

        // Try to update without endDate
        var updatePolicy = new HashMap<String, Object>();
        updatePolicy.put("carId", 1L);
        updatePolicy.put("provider", "UpdateTest Insurance (changed)");
        updatePolicy.put("startDate", "2024-08-01");
        // endDate missing

        mvc.perform(put("/api/policies/" + policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePolicy)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("endDate")));
    }

    @Test
    void updatePolicy_withEndDate_succeeds() throws Exception {
        var validPolicy = new HashMap<String, Object>();
        validPolicy.put("carId", 1L);
        validPolicy.put("provider", "Asirom");
        validPolicy.put("startDate", "2024-08-01");
        validPolicy.put("endDate", "2025-08-01");

        String response = mvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPolicy)))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

        Long policyId = objectMapper.readTree(response).get("id").asLong();

        // Now, update it with valid data
        var updatePolicy = new HashMap<String, Object>();
        updatePolicy.put("carId", 1L);
        updatePolicy.put("provider", "UpdateTest Insurance 2 Updated");
        updatePolicy.put("startDate", "2024-08-01");
        updatePolicy.put("endDate", "2026-08-01");

        mvc.perform(put("/api/policies/" + policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePolicy)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.endDate", Matchers.is("2026-08-01")))
                .andExpect(jsonPath("$.provider", Matchers.is("UpdateTest Insurance 2 Updated")));
    }

    @Test
    void updatePolicy_withInvalidDate_returnsBadRequest() throws Exception {
        var policyHashMap = new HashMap<String, Object>();
        long validId = 2L;

        policyHashMap.put("carId", 1L);
        policyHashMap.put("provider", "Omniasig");
        policyHashMap.put("startDate", "202-06-01");
        policyHashMap.put("endDate", "2023-06-01");

        mvc.perform(put("/api/policies/"+validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("failed to deserialize")));
    }

    @Test
    void updatePolicy_nonExistentId_returnsBadRequest() throws Exception {
        // Attempt to update a policy that does NOT exist
        long nonexistentId = 999999L;

        var updatePolicy = new HashMap<String, Object>();
        updatePolicy.put("carId", 1L);
        updatePolicy.put("provider", "Omniasig");
        updatePolicy.put("startDate", "2024-08-01");
        updatePolicy.put("endDate", "2025-08-01");

        mvc.perform(put("/api/policies/" + nonexistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePolicy)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("does not exist")));
    }


    @Test
    void updatePolicy_withStartDateAfterEndDate_returnsBadRequest() throws Exception {
        // Create a valid policy first
        var validPolicy = new HashMap<String, Object>();
        validPolicy.put("carId", 1L);
        validPolicy.put("provider", "Asirom");
        validPolicy.put("startDate", "2025-01-01");
        validPolicy.put("endDate", "2025-12-31");

        String response = mvc.perform(post("/api/policies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPolicy)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long policyId = objectMapper.readTree(response).get("id").asLong();

        // Update policy with an invalid start date
        var updatePolicy = new HashMap<String, Object>();
        updatePolicy.put("carId", 1L);
        updatePolicy.put("provider", "Asirom");
        updatePolicy.put("startDate", "2025-12-31");
        updatePolicy.put("endDate", "2025-01-01");

        mvc.perform(put("/api/policies/" + policyId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatePolicy)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("start date cannot be after end date")));
    }

    @Test
    void updatePolicy_withInvalidCarId_returnsNotFound() throws Exception {
        var policyHashMap = new HashMap<String, Object>();
        long validId = 2L;

        policyHashMap.put("carId", 100L);
        policyHashMap.put("provider", "Asirom");
        policyHashMap.put("startDate", "2022-06-01");
        policyHashMap.put("endDate", "2023-06-01");

        mvc.perform(put("/api/policies/"+validId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(policyHashMap)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message", Matchers.containsStringIgnoringCase("does not exist")));
    }

    // Tests for Claims
    // TODO Add tests


    // Tests for CarHistory
    @Test
    void carHistory_includesClaimAndPolicies_sortedByDate(){
        var history = service.getCarHistory(1L);

        assertFalse(history.isEmpty());

        assertTrue(history.stream().anyMatch(e -> e.type() == CarHistoryEventType.POLICY));
        assertTrue(history.stream().anyMatch(e -> e.type() == CarHistoryEventType.CLAIM));

        for(int i=0; i<history.size()-1; i++){
            assertTrue(history.get(i).date().isBefore(history.get(i+1).date()));
        }
    }

    @Test
    void carHistory_whenNoHistory_returnsEmptyList() {
        var history = service.getCarHistory(3L);
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void carHistory_whenCarMissing_throwsException() {
        assertThrows(CarNotFoundException.class, () -> service.getCarHistory(999L));
    }

    // Tests for Scheduler
    // TODO Add tests
}
