package com.rpl.resourceaccess;

import com.rpl.domain.ResourceType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceTypeRepository extends JpaRepository<ResourceType, Long> {
}
