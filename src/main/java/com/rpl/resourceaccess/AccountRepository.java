package com.rpl.resourceaccess;

import com.rpl.domain.Account;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("select a from Account a where a.kind = com.rpl.domain.AccountKind.POOL")
    List<Account> findPoolAccounts();
}
