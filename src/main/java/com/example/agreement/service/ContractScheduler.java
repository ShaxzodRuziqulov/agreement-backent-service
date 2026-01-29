//package com.example.agreement.service;
//
//import com.example.agreement.entity.Contract;
//import com.example.agreement.repository.ContractRepository;
//import com.example.agreement.repository.PaymentClaimRepository;
//import com.example.agreement.entity.enumerated.ContractStatus;
//import com.example.agreement.entity.enumerated.EvidenceAction;
//import com.example.agreement.entity.enumerated.PaymentClaimStatus;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.YearMonth;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class ContractScheduler {
//
//    private final ContractRepository repository;
//    private final PaymentClaimRepository paymentClaimRepository;
//    private final EvidenceLogService evidenceLogService;
//
//    @Scheduled(cron = "0 0 2 * * *")
//    @Transactional
//    public void checkContracts() {
//        List<Contract> contracts = repository.findByStatus(ContractStatus.ACTIVE);
//
//        LocalDate today = LocalDate.now();
//
//        for (Contract contract : contracts) {
//
//            int paymentDay = contract.getPaymentDay();
//
//            YearMonth ym = YearMonth.now();
//            int safeDay = Math.min(paymentDay, ym.lengthOfMonth());
//            LocalDate dueDate = ym.atDay(safeDay);
//
//            if (today.isAfter(dueDate)) {
//
//                boolean paid = paymentClaimRepository.existsByContractIdAndPeriod(
//                        contract.getId(), YearMonth.now()
//                );
//
//                if (!paid) {
//                    contract.setStatus(ContractStatus.LATE);
//                    repository.save(contract);
//
//                    evidenceLogService.log(contract, EvidenceAction.PAYMENT_LATE);
//                }
//            }
//        }
//    }
//
//    @Scheduled(cron = "0 30 2 * * *")
//    @Transactional
//    public void suspendLateContracts() {
//
//        List<Contract> contracts = repository.findByStatus(ContractStatus.LATE);
//
//        LocalDateTime now = LocalDateTime.now();
//
//        for (Contract contract : contracts) {
//            LocalDateTime lateSince = contract.getUpdatedAt();
//
//            if (lateSince.plusDays(7).isBefore(now)) {
//                contract.setStatus(ContractStatus.SUSPENDED);
//                repository.save(contract);
//
//                evidenceLogService.log(contract,EvidenceAction.CONTRACT_SUSPENDED);
//
//            }
//        }
//    }
//}
