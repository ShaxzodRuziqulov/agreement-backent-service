package com.example.agreement.repository;

import com.example.agreement.entity.Contract;
import com.example.agreement.entity.enumerated.ContractStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByOwnerIdOrRenterId(Long ownerId, Long renterId);

    List<Contract> findByStatus(ContractStatus status);
}
