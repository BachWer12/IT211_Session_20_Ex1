package com.session20ex01.repository;

import com.session20ex01.entity.Employee;
import com.session20ex01.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByTokenValue(String tokenValue);

    List<Token> findAllByEmployeeAndExpiredFalseAndRevokedFalse(Employee employee);
}
