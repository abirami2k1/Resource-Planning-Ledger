package com.rpl.resourceaccess;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rpl.domain.Suspension;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** Ensures {@link SuspensionRepository#findOpenByActionId} is stubbable without streams. */
class SuspensionRepositoryMethodTest {

    @Test
    void findOpenByActionId_returnsOptionalFromMock() {
        SuspensionRepository repo = mock(SuspensionRepository.class);
        Suspension open = new Suspension();
        when(repo.findOpenByActionId(5L)).thenReturn(Optional.of(open));

        assertTrue(repo.findOpenByActionId(5L).isPresent());
    }
}
