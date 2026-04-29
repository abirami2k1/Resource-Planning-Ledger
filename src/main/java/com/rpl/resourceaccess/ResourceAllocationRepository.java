package com.rpl.resourceaccess;

import com.rpl.domain.ResourceAllocation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceAllocationRepository extends JpaRepository<ResourceAllocation, Long> {
    List<ResourceAllocation> findByActionId(Long actionId);
    long countByResourceTypeId(Long resourceTypeId);
}
