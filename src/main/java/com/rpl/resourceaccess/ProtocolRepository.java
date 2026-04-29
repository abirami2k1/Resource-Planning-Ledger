package com.rpl.resourceaccess;

import com.rpl.domain.Protocol;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
}
