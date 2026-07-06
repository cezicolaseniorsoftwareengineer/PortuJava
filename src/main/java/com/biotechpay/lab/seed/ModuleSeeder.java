package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.LearningModule;

/**
 * One implementation per learning track (see CoffeeMachineModuleSeeder). Adding a new module/paradigm
 * track means adding one new file here and one line in ContentSeeder - not editing a god-class.
 */
public interface ModuleSeeder {
    LearningModule seed();
}
