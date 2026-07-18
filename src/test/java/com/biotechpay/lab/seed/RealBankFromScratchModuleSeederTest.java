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
                .extracting(RealBankFromScratchModuleSeederTest::exerciseId)
                .containsExactlyElementsOf(expectedExerciseIds());
    }

    @Test
    void everyExerciseDeclaresAnInvariantAndAdversarialChecks() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();

        assertThat(module.getExercises()).hasSize(36).allSatisfy(exercise -> {
            assertThat(exercise.getStatementMarkdown()).containsAnyOf("## Invariante", "## Contexto real", "## Fase");
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
                .extracting(RealBankFromScratchModuleSeederTest::exerciseId)
                .containsExactly(
                        "bank-real-12-customer-profile",
                        "bank-real-13-account-lifecycle",
                        "bank-real-14-multi-currency-wallet",
                        "bank-real-15-payment-limit-policy",
                        "bank-real-16-fee-and-plan-policy");
    }

    @Test
    void phasesThreeThroughTwentyTwoEachHaveAnExecutableReleaseGate() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();

        assertThat(module.getExercises().subList(16, 36)).hasSize(20);
        for (int phase = 3; phase <= 22; phase++) {
            String phaseMarker = "phase-%02d".formatted(phase);
            assertThat(module.getExercises().subList(16, 36))
                    .anyMatch(exercise -> exercise.getExerciseId().contains(phaseMarker));
        }
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
                .map(RealBankFromScratchModuleSeederTest::moduleCode)
                .toList();

        assertThat(moduleCodes).contains(MODULE_CODE);
    }

    @Test
    void synchronizationRestoresMissingExercisesWithoutDuplicatingExistingContent() {
        LearningModule module = moduleRepository.findByModuleCode(MODULE_CODE).orElseThrow();
        Exercise removed = exerciseRepository.findByExerciseId("bank-real-08-ambiguous-gateway-result")
                .orElseThrow();
        if (removed == null) {
            throw new AssertionError("Seeded exercise must not be null");
        }
        module.getExercises().remove(removed);
        exerciseRepository.delete(removed);
        exerciseRepository.flush();

        assertThat(exerciseRepository.findByExerciseId(removed.getExerciseId())).isEmpty();

        moduleSeeder.synchronize(module);
        moduleSeeder.synchronize(module);

        assertThat(exerciseRepository.findByModuleOrderBySortOrderAsc(module))
                .extracting(RealBankFromScratchModuleSeederTest::exerciseId)
                .containsExactlyElementsOf(expectedExerciseIds());
    }

    private static String exerciseId(Exercise exercise) {
        if (exercise == null) {
            throw new AssertionError("Seeded exercise must not be null");
        }
        return exercise.getExerciseId();
    }

    private static String moduleCode(LearningModule module) {
        if (module == null) {
            throw new AssertionError("Seeded module must not be null");
        }
        return module.getModuleCode();
    }

    private static List<String> expectedExerciseIds() {
        return List.of(
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
                "bank-real-16-fee-and-plan-policy",
                "bank-real-17-phase-03-identity-access",
                "bank-real-18-phase-04-payment-authorization",
                "bank-real-19-phase-05-passkeys",
                "bank-real-20-phase-06-auditable-ledger",
                "bank-real-21-phase-07-internal-transfers",
                "bank-real-22-phase-08-pix-key",
                "bank-real-23-phase-09-pix-qr-emv",
                "bank-real-24-phase-10-baas-adapters",
                "bank-real-25-phase-11-webhooks-settlement",
                "bank-real-26-phase-12-reconciliation",
                "bank-real-27-phase-13-charges",
                "bank-real-28-phase-14-treasury",
                "bank-real-29-phase-15-kyc-privacy-aml",
                "bank-real-30-phase-16-fraud",
                "bank-real-31-phase-17-distributed-controls",
                "bank-real-32-phase-18-bank-operations",
                "bank-real-33-phase-19-bank-frontend",
                "bank-real-34-phase-20-governed-ai",
                "bank-real-35-phase-21-supply-chain",
                "bank-real-36-phase-22-capstone");
    }
}
