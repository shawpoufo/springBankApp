package com.app.digitalbanking.dtos;

import lombok.Data;

@Data
public class BankAccountDTO {
    private String id;
    private String type;
    private double balance;
}
