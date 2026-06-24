package com.rpl.client;

import com.rpl.domain.Protocol;
import com.rpl.domain.ProtocolStep;
import com.rpl.manager.ProtocolManager;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/protocols")
public class ProtocolController {
    private final ProtocolManager protocolManager;
    public ProtocolController(ProtocolManager protocolManager) {
         this.protocolManager = protocolManager; 
    }

    @GetMapping 
    public List<Protocol> list() { 
        return protocolManager.list(); 
    }

    @PostMapping 
    public Protocol create(@RequestBody Protocol protocol) { 
        return protocolManager.create(protocol); 
    
    }
    @GetMapping("/{id}") 
    public Protocol get(@PathVariable Long id) { 
        return protocolManager.get(id); 
    }

    @PutMapping("/{id}") 
    public Protocol update(@PathVariable Long id, @RequestBody Protocol protocol) { 
        return protocolManager.update(id, protocol); 
    }

    @DeleteMapping("/{id}") 
    public void delete(@PathVariable Long id) { 
        protocolManager.delete(id); 
    }

    @PostMapping("/{id}/steps") 
    public ProtocolStep addStep(@PathVariable Long id, @RequestBody ProtocolStep step) { 
        return protocolManager.addStep(id, step); 
    }
}
