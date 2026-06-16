package com.bankapp.mini_banking_app.mapper;

import com.bankapp.mini_banking_app.dto.response.TransactionResponse;
import com.bankapp.mini_banking_app.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "fromAccountId",
            expression = "java(transaction.getAccount().getAccountNumber())")
    @Mapping(target = "type", expression = "java(transaction.getType().name())")
    TransactionResponse toResponse(Transaction transaction);
}
