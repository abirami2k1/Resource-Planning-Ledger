package com.rpl.client.dto;

import com.rpl.domain.AccountKind;

public record AccountBalanceResponse(Long id, String name, AccountKind kind, double balance) {
}
