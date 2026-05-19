package com.mybank.transfer.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRecordRepository extends JpaRepository<TransferRecordEntity, Long> {
}
