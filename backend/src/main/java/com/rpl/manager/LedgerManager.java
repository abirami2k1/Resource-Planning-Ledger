package com.rpl.manager;

import com.rpl.domain.Account;
import com.rpl.domain.Entry;
import com.rpl.client.dto.AccountBalanceResponse;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.AccountRepository;
import com.rpl.resourceaccess.EntryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class LedgerManager {
    private final AccountRepository accountRepository;
    private final EntryRepository entryRepository;

    public LedgerManager(AccountRepository accountRepository, EntryRepository entryRepository) {
        this.accountRepository = accountRepository;
        this.entryRepository = entryRepository;
    }

    public List<Account> allAccounts() {
        return accountRepository.findAll();
    }

    public List<AccountBalanceResponse> allAccountsWithBalance() {
        return accountRepository.findAll().stream()
                .map(account -> new AccountBalanceResponse(
                        account.getId(),
                        account.getName(),
                        account.getKind(),
                        entryRepository.getBalanceForAccount(account.getId())))
                .toList();
    }

    public List<Entry> entriesForAccount(Long accountId) {
        accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Account not found"));
        return entryRepository.findByAccountIdOrderByBookedAtDesc(accountId);
    }
}
