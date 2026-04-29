package com.rpl.resourceaccess;

import com.rpl.domain.ProposedAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProposedActionRepository extends JpaRepository<ProposedAction, Long> {
}
