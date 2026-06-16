package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.AuthRequest;
import com.bankapp.mini_banking_app.dto.response.AuthResponse;
import com.bankapp.mini_banking_app.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // =========================
    // DEV LOGIN EASY
    // =========================
    @PostMapping("/login-dev")
    public String loginDev(@RequestParam String username) {
        return jwtService.generateToken(username);
    }

    // =========================
    // MID LOGIN (real)
    // =========================
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        String token = jwtService.generateToken(request.username());

        return ResponseEntity.ok(new AuthResponse(token));
    }
}