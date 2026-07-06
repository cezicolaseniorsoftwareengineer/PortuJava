package com.biotechpay.lab.api;

import com.biotechpay.lab.application.ModuleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@CrossOrigin(origins = "${app.cors.allowed-origins:*}")
public class ModuleController {

    private final ModuleService moduleService;

    public ModuleController(ModuleService moduleService) {
        this.moduleService = moduleService;
    }

    @GetMapping
    public ResponseEntity<List<ModuleService.ModuleSummary>> getAllModules() {
        return ResponseEntity.ok(moduleService.getAllModules());
    }

    @GetMapping("/{moduleCode}")
    public ResponseEntity<ModuleService.ModuleSummary> getModule(@PathVariable String moduleCode) {
        return ResponseEntity.ok(moduleService.getModule(moduleCode));
    }
}
