package com.rpl.client;

import com.rpl.domain.ResourceType;
import com.rpl.manager.ResourceTypeManager;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resource-types")
public class ResourceTypeController {
    private final ResourceTypeManager manager;
    public ResourceTypeController(ResourceTypeManager manager) { this.manager = manager; }
    @GetMapping public List<ResourceType> list() { return manager.list(); }
    @PostMapping public ResourceType create(@RequestBody ResourceType resourceType) { return manager.create(resourceType); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { manager.delete(id); }
}
