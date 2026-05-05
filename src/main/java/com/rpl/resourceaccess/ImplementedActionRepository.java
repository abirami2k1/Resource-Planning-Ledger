package com.rpl.resourceaccess;

import com.rpl.domain.ImplementedAction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImplementedActionRepository extends JpaRepository<ImplementedAction, Long> {
    Optional<ImplementedAction> findByProposedAction_Id(Long proposedActionId);
}
