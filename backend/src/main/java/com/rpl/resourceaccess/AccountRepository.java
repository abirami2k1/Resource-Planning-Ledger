package com.rpl.resourceaccess;

import com.rpl.domain.Account;
import com.rpl.domain.AccountKind;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, Long> {
    @Query("select a from Account a where a.kind = com.rpl.domain.AccountKind.POOL")
    List<Account> findPoolAccounts();

    @Query("select coalesce(sum(e.amount), 0) from Entry e where e.account.id = :accountId")
    BigDecimal getBalance(@Param("accountId") Long accountId);

    Optional<Account> findFirstByKindAndResourceType_Id(AccountKind kind, Long resourceTypeId);
}
