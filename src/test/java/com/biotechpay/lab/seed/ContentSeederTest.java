package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ContentSeederTest {

    @Test
    void synchronizesAnExistingModuleInsteadOfTreatingItAsPermanentlyComplete() throws Exception {
        LearningModuleRepository moduleRepository = mock(LearningModuleRepository.class);
        ModuleSeeder moduleSeeder = mock(ModuleSeeder.class);
        LearningModule existingModule = new LearningModule();
        when(moduleSeeder.moduleCode()).thenReturn("evolving-module");
        when(moduleRepository.findByModuleCode("evolving-module")).thenReturn(Optional.of(existingModule));

        new ContentSeeder(moduleRepository, List.of(moduleSeeder)).run();

        verify(moduleSeeder).synchronize(existingModule);
        verify(moduleSeeder, never()).seed();
    }
}
