package com.bankapp.mini_banking_app.repository;

import com.bankapp.mini_banking_app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
