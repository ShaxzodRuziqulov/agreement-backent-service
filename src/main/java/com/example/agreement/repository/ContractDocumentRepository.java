package com.example.agreement.repository;

import com.example.agreement.entity.ContractDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractDocumentRepository extends JpaRepository<ContractDocument, Long> {

    @Query("select max(cd.version) from ContractDocument cd where cd.contract.id = :contractId")
    Optional<Integer> findMaxVersionByContractId(@Param("contractId") Long contractId);

    List<ContractDocument> findByContractIdOrderByVersionDesc(Long contractId);
}


