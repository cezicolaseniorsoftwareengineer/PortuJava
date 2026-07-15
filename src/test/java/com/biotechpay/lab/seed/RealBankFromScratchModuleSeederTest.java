package com.biotechpay.lab.seed;

import com.biotechpay.lab.application.SubmissionService;
import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Proves that the implemented real-banking curriculum phases are present and that every published
 * answer passes through the same javac-based grading path used for student submissions.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class RealBankFromScratchModuleSeederTest {

    private static final String MODULE_CODE = "real-bank-from-scratch";

    @Autowired
    private LearningModuleRepository moduleRepository;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private ExerciseRepository exerciseRepository;

    @Autowired
    private RealBankFromScratchModuleSeeder moduleSeeder;

    @Test
    void seedsTheNamedSessionWithTheExpectedProgression() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();

        assertThat(module.getTitle()).isEqualTo("Construindo um banco inteiro sozinho Real");
        assertThat(module.getParadigm()).isEqualTo("ENGENHARIA BANCÁRIA");
        assertThat(module.getExercises())
                .extracting(Exercise::getExerciseId)
                .containsExactly(
                        "bank-real-01-money-value-object",
                        "bank-real-02-balanced-posting",
                        "bank-real-03-payment-state-machine",
                        "bank-real-04-financial-idempotency",
                        "bank-real-05-atomic-account",
                        "bank-real-06-tamper-evident-journal",
                        "bank-real-07-reconciliation-decision",
                        "bank-real-08-ambiguous-gateway-result",
                        "bank-real-09-atomic-journal-sequence",
                        "bank-real-10-cas-versioned-balance",
                        "bank-real-11-atomic-transfer",
                        "bank-real-12-customer-profile",
                        "bank-real-13-account-lifecycle",
                        "bank-real-14-multi-currency-wallet",
                        "bank-real-15-payment-limit-policy",
                        "bank-real-16-fee-and-plan-policy");
    }

    @Test
    void everyExerciseDeclaresAnInvariantAndAdversarialChecks() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();

        assertThat(module.getExercises()).hasSize(16).allSatisfy(exercise -> {
            assertThat(exercise.getStatementMarkdown()).containsAnyOf("## Invariante", "## Contexto real");
            assertThat(exercise.getTestCases()).hasSizeGreaterThanOrEqualTo(4);
            assertThat(exercise.getTestCases()).anyMatch(testCase -> !testCase.isVisible());
            assertThat(exercise.getHints()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(exercise.getReferenceSolution()).isNotBlank();
        });
    }

    @Test
    void phaseTwoCoversEveryBankModelingCapability() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();

        assertThat(module.getExercises().subList(11, 16))
                .extracting(Exercise::getExerciseId)
                .containsExactly(
                        "bank-real-12-customer-profile",
                        "bank-real-13-account-lifecycle",
                        "bank-real-14-multi-currency-wallet",
                        "bank-real-15-payment-limit-policy",
                        "bank-real-16-fee-and-plan-policy");
    }

    @Test
    void everyReferenceSolutionCompilesAndPassesItsOwnTests() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();

        for (Exercise exercise : module.getExercises()) {
            SubmissionService.SubmissionResult result =
                    submissionService.submit(exercise.getExerciseId(), exercise.getReferenceSolution());

            assertThat(result.compileSuccess())
                    .as("reference solution for %s must compile: %s",
                            exercise.getExerciseId(), result.compileErrors())
                    .isTrue();
            assertThat(result.allPassed())
                    .as("reference solution for %s must pass all tests: %s",
                            exercise.getExerciseId(), result.testResults())
                    .isTrue();
        }
    }

    @Test
    void moduleIsExposedInTheGlobalOrderedCatalog() {
        List<String> moduleCodes = moduleRepository.findAllByOrderBySortOrderAsc().stream()
                .map(LearningModule::getModuleCode)
                .toList();

        assertThat(moduleCodes).contains(MODULE_CODE);
    }

    @Test
    void synchronizationRestoresMissingExercisesWithoutDuplicatingExistingContent() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();
        Exercise removed = exerciseRepository.findByExerciseId("bank-real-08-ambiguous-gateway-result")
                .orElseThrow();
        module.getExercises().remove(removed);
        exerciseRepository.delete(removed);
        exerciseRepository.flush();

        assertThat(exerciseRepository.findByExerciseId(removed.getExerciseId())).isEmpty();

        moduleSeeder.synchronize(module);
        moduleSeeder.synchronize(module);

        assertThat(exerciseRepository.findByModuleOrderBySortOrderAsc(module))
                .extracting(Exercise::getExerciseId)
                .containsExactly(
                        "bank-real-01-money-value-object",
                        "bank-real-02-balanced-posting",
                        "bank-real-03-payment-state-machine",
                        "bank-real-04-financial-idempotency",
                        "bank-real-05-atomic-account",
                        "bank-real-06-tamper-evident-journal",
                        "bank-real-07-reconciliation-decision",
                        "bank-real-08-ambiguous-gateway-result",
                        "bank-real-09-atomic-journal-sequence",
                        "bank-real-10-cas-versioned-balance",
                        "bank-real-11-atomic-transfer",
                        "bank-real-12-customer-profile",
                        "bank-real-13-account-lifecycle",
                        "bank-real-14-multi-currency-wallet",
                        "bank-real-15-payment-limit-policy",
                        "bank-real-16-fee-and-plan-policy");
    }
}
