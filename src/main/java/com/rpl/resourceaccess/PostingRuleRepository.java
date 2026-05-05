package com.rpl.resourceaccess;

import com.rpl.domain.PostingRule;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostingRuleRepository extends JpaRepository<PostingRule, Long> {
    List<PostingRule> findByTriggerAccount_Id(Long accountId);
}
