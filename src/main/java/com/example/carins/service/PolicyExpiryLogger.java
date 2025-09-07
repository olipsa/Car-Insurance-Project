package com.example.carins.service;

import com.example.carins.model.InsurancePolicy;
import com.example.carins.repo.InsurancePolicyRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class PolicyExpiryLogger {
    private static final Logger logger = LoggerFactory.getLogger(PolicyExpiryLogger.class);
    private final InsurancePolicyRepository policyRepository;
    private final Set<Long> loggedPolicyIds = ConcurrentHashMap.newKeySet();

    public PolicyExpiryLogger(InsurancePolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    @PostConstruct
    public void getAlreadyExpiredPolicies() {
        LocalDate today = LocalDate.now();
        List<InsurancePolicy> alreadyExpired = policyRepository.findAll()
                .stream().filter(p -> p.getEndDate() !=null && p.getEndDate().isBefore(today.minusDays(1)))
                .toList();
        alreadyExpired.forEach(p -> loggedPolicyIds.add(p.getId()));
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void logRecentlyExpiredPolicies(){
        LocalDate today = LocalDate.now();
        LocalDate expiredOn = today.minusDays(1);
        LocalDateTime midnightToday = today.atStartOfDay();

        LocalDateTime now = LocalDateTime.now();


        if(now.isAfter(midnightToday) && now.isBefore(midnightToday.plusHours(1))){
            List<InsurancePolicy> expiredPolicies = policyRepository.findByEndDateAndIdNotIn(expiredOn, loggedPolicyIds);

            for(InsurancePolicy policy : expiredPolicies){
                if(policy.getId()!=null){
                    logger.info("Policy with id {} for car {} expired on {}", policy.getId(), policy.getCar().getId(),policy.getEndDate());
                    loggedPolicyIds.add(policy.getId());
                }
            }
        }
    }

    public void removeLoggedPolicyId(Long policyId) {
        loggedPolicyIds.remove(policyId);
    }

}
