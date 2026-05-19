package com.mybank.cash.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CashOperationRepository extends JpaRepository<CashOperationEntity, Long> {
}
