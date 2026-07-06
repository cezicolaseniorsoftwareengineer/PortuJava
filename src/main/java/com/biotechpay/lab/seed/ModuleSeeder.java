package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.LearningModule;

/**
 * One implementation per learning track (see CoffeeMachineModuleSeeder). Adding a new module/paradigm
 * track means adding one new file here - ContentSeeder discovers it via Spring and seeds it when its
 * moduleCode is not yet present, so new tracks reach existing player databases without a wipe.
 */
public interface ModuleSeeder {

    /** Must match the moduleCode of the LearningModule this seeder creates. */
    String moduleCode();

    LearningModule seed();
}
