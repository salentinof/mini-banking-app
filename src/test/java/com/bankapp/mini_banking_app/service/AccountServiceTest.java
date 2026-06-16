package com.bankapp.mini_banking_app.service;

import com.bankapp.mini_banking_app.dto.request.AccountRequest;
import com.bankapp.mini_banking_app.entity.Account;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.exception.AccountNotFoundException;
import com.bankapp.mini_banking_app.repository.AccountRepository;
import com.bankapp.mini_banking_app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService service;

    @Test
    void createAccount_shouldCreateAccountForExistingUser() {
        AccountRequest request = new AccountRequest();
        request.setUserId(1L);

        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Account saved = new Account();
        saved.setId(10L);
        saved.setUser(user);
        saved.setBalance(BigDecimal.ZERO);
        saved.setAccountNumber("ACC-123");

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account result = service.createAccount(request);

        assertEquals(10L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        verify(userRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_shouldThrow_whenUserNotFound() {
        AccountRequest request = new AccountRequest();
        request.setUserId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> service.createAccount(request));

        verify(userRepository).findById(99L);
        verify(accountRepository, never()).save(any());
    }

    @Test
    void getAccount_shouldReturnAccount() {
        Account acc = new Account();
        acc.setAccountNumber("ACC-1");

        when(accountRepository.findByAccountNumber("ACC-1"))
                .thenReturn(Optional.of(acc));

        Account result = service.getAccount("ACC-1");

        assertEquals("ACC-1", result.getAccountNumber());
        verify(accountRepository).findByAccountNumber("ACC-1");
    }

    @Test
    void getAccount_shouldThrow_whenNotFound() {
        when(accountRepository.findByAccountNumber("X"))
                .thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> service.getAccount("X"));
    }

    @Test
    void getAll_shouldReturnList() {
        List<Account> accounts = List.of(new Account(), new Account());

        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> result = service.getAll();

        assertEquals(2, result.size());
        verify(accountRepository).findAll();
    }
}