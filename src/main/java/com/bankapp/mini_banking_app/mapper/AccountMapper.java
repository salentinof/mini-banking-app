package com.bankapp.mini_banking_app.mapper;

import com.bankapp.mini_banking_app.dto.response.AccountResponse;
import com.bankapp.mini_banking_app.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(target= "userId", source = "user.id")
    AccountResponse responseDto(Account account);

    List<AccountResponse> responseDto(List<Account> accounts);

}
