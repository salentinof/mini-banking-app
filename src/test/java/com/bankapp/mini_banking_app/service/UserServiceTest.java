package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.exception.UserNotFoundException;
import com.bankapp.mini_banking_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserService service;

    @Test
    void getUser_shouldReturnUser() {
        User user = new User();
        user.setId(10L);
        user.setName("Luca");
        user.setPassword("1234");
        when(repository.findById(10L)).thenReturn(Optional.of(user));

        User result = service.getUser(10L);

        assertEquals(10L, result.getId());
        assertEquals("Luca", result.getName());
    }

    @Test
    void getUser_shouldThrow_whenNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
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