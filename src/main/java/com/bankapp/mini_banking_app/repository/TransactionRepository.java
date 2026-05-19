package com.bankapp.mini_banking_app.repository;

import com.bankapp.mini_banking_app.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}
