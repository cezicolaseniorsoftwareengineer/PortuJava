package com.biotechpay.lab.seed;

import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Orchestrator only - each learning track is a separate ModuleSeeder (see CoffeeMachineModuleSeeder).
 * Replaces the old 1853-line DataSeeder god-class: adding a track means adding one ModuleSeeder file,
 * not editing a monolith. Seeding is per-module: a track added after the first launch still reaches
 * existing player databases (the old count() == 0 guard skipped everything once any module existed).
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
            if (moduleRepository.findByModuleCode(seeder.moduleCode()).isEmpty()) {
                seeder.seed();
            }
        }
    }
}
