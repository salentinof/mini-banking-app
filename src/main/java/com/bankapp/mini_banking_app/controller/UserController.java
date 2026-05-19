package com.bankapp.mini_banking_app.controller;

import com.bankapp.mini_banking_app.dto.request.UserRequest;
import com.bankapp.mini_banking_app.dto.response.UserResponse;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.mapper.UserMapper;
import com.bankapp.mini_banking_app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request) {
        User user = service.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.responseDto(user));
    }

    @GetMapping("/{idUser}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long idUser) {
        User user = service.getUser(idUser);
        return ResponseEntity.ok(userMapper.responseDto(user));
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUser() {
        List<User> list = service.getAllUser();
        List<UserResponse> listResponse = userMapper.responseDto(list);
        return ResponseEntity.ok(listResponse);
    }

}
