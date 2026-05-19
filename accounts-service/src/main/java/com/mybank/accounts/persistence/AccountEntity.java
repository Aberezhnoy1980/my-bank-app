package com.mybank.accounts.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "account", schema = "accounts")
public class AccountEntity {

    @Id
    @Column(name = "username", length = 128, nullable = false)
    private String username;

    @Column(name = "full_name", length = 512, nullable = false)
    private String fullName;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    protected AccountEntity() {
    }

    public AccountEntity(String username, String fullName, LocalDate birthDate, BigDecimal balance) {
        this.username = username;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
