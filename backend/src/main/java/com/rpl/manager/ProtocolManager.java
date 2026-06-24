package com.rpl.manager;

import com.rpl.domain.Protocol;
import com.rpl.domain.ProtocolStep;
import com.rpl.exception.NotFoundException;
import com.rpl.resourceaccess.ProtocolRepository;
import com.rpl.resourceaccess.ProtocolStepRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProtocolManager {
    private final ProtocolRepository protocolRepository;
    private final ProtocolStepRepository stepRepository;

    public ProtocolManager(ProtocolRepository protocolRepository, ProtocolStepRepository stepRepository) {
        this.protocolRepository = protocolRepository;
        this.stepRepository = stepRepository;
    }

    public List<Protocol> list() { return protocolRepository.findAll(); }
    public Protocol create(Protocol protocol) { return protocolRepository.save(protocol); }
    public Protocol get(Long id) { return protocolRepository.findById(id).orElseThrow(() -> new NotFoundException("Protocol not found")); }
    public Protocol update(Long id, Protocol request) {
        Protocol current = get(id);
        current.setName(request.getName());
        current.setDescription(request.getDescription());
        return protocolRepository.save(current);
    }
    public void delete(Long id) { protocolRepository.delete(get(id)); }
    public ProtocolStep addStep(Long protocolId, ProtocolStep step) {
        Protocol protocol = get(protocolId);
        step.setProtocol(protocol);
        return stepRepository.save(step);
    }
}
