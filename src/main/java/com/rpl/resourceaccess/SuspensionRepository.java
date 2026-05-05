package com.rpl.resourceaccess;

import com.rpl.domain.Suspension;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SuspensionRepository extends JpaRepository<Suspension, Long> {
    List<Suspension> findByProposedAction_Id(Long actionId);

    @Query("select s from Suspension s where s.proposedAction.id = :id and s.endDate is null")
    Optional<Suspension> findOpenByActionId(@Param("id") Long actionId);
}
