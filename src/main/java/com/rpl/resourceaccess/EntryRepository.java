package com.rpl.resourceaccess;

import com.rpl.domain.Entry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntryRepository extends JpaRepository<Entry, Long> {
    List<Entry> findByAccountIdOrderByBookedAtDesc(Long accountId);
}
