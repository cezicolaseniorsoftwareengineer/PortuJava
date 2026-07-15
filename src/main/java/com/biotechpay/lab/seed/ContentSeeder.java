package com.biotechpay.lab.seed;

import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrator only - each learning track is a separate ModuleSeeder (see CoffeeMachineModuleSeeder).
 * Replaces the old 1853-line DataSeeder god-class: adding a track means adding one ModuleSeeder file,
 * not editing a monolith. New modules are seeded once; existing modules may synchronize newly
 * published exercises without deleting player progress.
 */
@Component
public class ContentSeeder implements CommandLineRunner {

    private final LearningModuleRepository moduleRepository;
    private final List<ModuleSeeder> moduleSeeders;

    public ContentSeeder(LearningModuleRepository moduleRepository, List<ModuleSeeder> moduleSeeders) {
        this.moduleRepository = moduleRepository;
        this.moduleSeeders = moduleSeeders;
    }

    @Override
    public void run(String... args) {
        for (ModuleSeeder seeder : moduleSeeders) {
            var existingModule = moduleRepository.findByModuleCode(seeder.moduleCode());
            if (existingModule.isEmpty()) {
                seeder.seed();
            } else {
                seeder.synchronize(existingModule.orElseThrow());
            }
        }
    }
}
