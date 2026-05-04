package com.rpl.resourceaccess;

import com.rpl.domain.Entry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByAccountIdOrderByBookedAtDesc(Long accountId);

    @Query("select coalesce(sum(e.amount), 0) from Entry e where e.account.id = :accountId")
    double getBalanceForAccount(@Param("accountId") Long accountId);
}
