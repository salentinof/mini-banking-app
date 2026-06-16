package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.UserRequest;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    void createUser_shouldCreateAndReturnUser() {
        UserRequest request = UserRequest.builder()
                .name("Mario")
                .email("mario@mail.com")
                .password("1234")
                .build();

        User saved = new User();
        saved.setId(1L);
        saved.setName("Mario");
        saved.setEmail("mario@mail.com");
        saved.setPassword("1234");

        when(repository.save(any(User.class))).thenReturn(saved);

        User result = service.createUser(request);

        assertEquals(1L, result.getId());
        assertEquals("mario@mail.com", result.getEmail());

        verify(repository).save(any(User.class));
    }

    @Test
    void getUser_shouldReturnUser() {
        User user = new User();
        user.setId(10L);
        user.setName("Luca");

        when(repository.findById(10L)).thenReturn(Optional.of(user));

        User result = service.getUser(10L);

        assertEquals(10L, result.getId());
        assertEquals("Luca", result.getName());
    }

    @Test
    void getUser_shouldThrowException_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.getUser(99L));
    }

    @Test
    void getAllUser_shouldReturnList() {
        List<User> users = List.of(new User(), new User());

        when(repository.findAll()).thenReturn(users);

        List<User> result = service.getAllUser();

        assertEquals(2, result.size());
        verify(repository).findAll();
    }
}