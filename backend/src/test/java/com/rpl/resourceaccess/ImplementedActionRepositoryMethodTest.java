package com.rpl.resourceaccess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rpl.domain.ImplementedAction;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/**
 * Documents the derived-query naming expected by {@link ActionManager} (no Spring context).
 */
class ImplementedActionRepositoryMethodTest {

    @Test
    void findByProposedAction_Id_returnsStubFromMock() {
        ImplementedActionRepository repo = mock(ImplementedActionRepository.class);
        ImplementedAction stub = new ImplementedAction();
        stub.setId(99L);
        when(repo.findByProposedAction_Id(7L)).thenReturn(Optional.of(stub));

        assertEquals(Optional.of(stub), repo.findByProposedAction_Id(7L));
    }
}
