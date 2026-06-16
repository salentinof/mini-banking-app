package com.bankapp.mini_banking_app.mapper;

import com.bankapp.mini_banking_app.dto.response.UserResponse;
import com.bankapp.mini_banking_app.entity.User;
import com.bankapp.mini_banking_app.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(
            target = "accountNumbers",
            expression = "java(user.getAccounts().stream().map(t-> t.getAccountNumber()).toList())")
    UserResponse responseDto(User user);

    List<UserResponse> responseDto(List<User> user);
}
