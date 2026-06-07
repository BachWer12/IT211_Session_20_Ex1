package com.session20ex01.service;

import com.session20ex01.dto.AuthResponse;
import com.session20ex01.dto.LoginRequest;
import com.session20ex01.dto.RefreshTokenRequest;
import com.session20ex01.entity.Employee;
import com.session20ex01.entity.Token;
import com.session20ex01.entity.TokenType;
import com.session20ex01.repository.EmployeeRepository;
import com.session20ex01.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final EmployeeRepository employeeRepository;
    private final TokenRepository tokenRepository;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        Employee employee = employeeRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        String accessToken = jwtService.generateAccessToken(employee);
        String refreshToken = jwtService.generateRefreshToken(employee);

        saveToken(employee, accessToken, TokenType.ACCESS);
        saveToken(employee, refreshToken, TokenType.REFRESH);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        Token storedToken = tokenRepository.findByTokenValue(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedToken.isExpired() || storedToken.isRevoked()) {
            throw new RuntimeException("Refresh token is invalid");
        }

        if (storedToken.getTokenType() != TokenType.REFRESH) {
            throw new RuntimeException("Token is not refresh token");
        }

        Employee employee = storedToken.getEmployee();

        if (!jwtService.isTokenValid(request.getRefreshToken(), employee)) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            tokenRepository.save(storedToken);
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = jwtService.generateAccessToken(employee);
        saveToken(employee, newAccessToken, TokenType.ACCESS);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken())
                .build();
    }

    public void logout(String accessToken) {
        Token token = tokenRepository.findByTokenValue(accessToken)
                .orElseThrow(() -> new RuntimeException("Token not found"));

        Employee employee = token.getEmployee();

        var validTokens = tokenRepository.findAllByEmployeeAndExpiredFalseAndRevokedFalse(employee);

        var revokedTokens = validTokens.stream()
                .peek(item -> {
                    item.setExpired(true);
                    item.setRevoked(true);
                })
                .collect(Collectors.toList());

        tokenRepository.saveAll(revokedTokens);

        SecurityContextHolder.clearContext();
    }

    private void saveToken(Employee employee, String tokenValue, TokenType tokenType) {
        Token token = Token.builder()
                .employee(employee)
                .tokenValue(tokenValue)
                .tokenType(tokenType)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }
}
