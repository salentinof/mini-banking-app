package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.UserRequest;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.exception.UserNotFoundException;
import com.bankapp.mini_banking_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserRequest request){
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = repository.save(user);

        log.info("User created | id = {} | email = {}", user.getId(), user.getEmail());

        return user;
    }

    public User getUser(Long idUser) {
        return repository.findById(idUser)
                .orElseThrow(()-> new UserNotFoundException(idUser));
    }

    public List<User> getAllUser(){
        return repository.findAll();
    }
}
