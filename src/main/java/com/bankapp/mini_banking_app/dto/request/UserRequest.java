package com.bankapp.mini_banking_app.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank
    private String name;
    @NotBlank
    @Email
    private String email;
    private String password;
}
