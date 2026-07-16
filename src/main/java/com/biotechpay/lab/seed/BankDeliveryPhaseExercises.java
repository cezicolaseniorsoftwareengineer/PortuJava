package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.seed.support.ExerciseBuilder;

import java.util.List;

/**
 * Executable release gates for phases 3 through 22 of the real-bank curriculum. Each gate forces
 * the student to distinguish positive evidence from an active veto before an increment can move to
 * the next phase. The multi-file implementation itself lives in the in-product Repository Lab.
 */
final class BankDeliveryPhaseExercises {

    private BankDeliveryPhaseExercises() {}

    static List<Exercise> build(LearningModule module) {
        return List.of(
                phase(module, 3, "identity-access", "Identidade e acesso",
                        "Proteja cadastro, sessão, refresh e autorização por objeto.",
                        List.of("tokenValidated", "sessionActive", "objectOwnershipChecked", "leastPrivilegeApplied"),
                        List.of("sessionReplay", "authorizationBypass"),
                        "backend/src/main/java/com/portujava/bank/identity", "Simule refresh token reutilizado após rotação."),
                phase(module, 4, "payment-authorization", "Autorização de pagamentos",
                        "Modele PIN, tentativas, bloqueio temporal e recuperação sem armazenar segredo em claro.",
                        List.of("pinStoredAsVerifier", "attemptsAreAtomic", "temporaryLockEnforced", "recoveryIsAudited"),
                        List.of("bruteForceWindowOpen", "plaintextPinExposure"),
                        "backend/src/main/java/com/portujava/bank/authorization", "Dispare tentativas concorrentes no limite de bloqueio."),
                phase(module, 5, "passkeys", "Passkeys",
                        "Implemente challenge de uso único, contador de assinatura, cadastro, login e revogação.",
                        List.of("challengeIsSingleUse", "originAndRpIdValidated", "signCountChecked", "credentialCanBeRevoked"),
                        List.of("challengeReplay", "revokedCredentialAccepted"),
                        "backend/src/main/java/com/portujava/bank/passkey", "Repita a mesma assertion e reduza o signCount."),
                phase(module, 6, "auditable-ledger", "Ledger auditável",
                        "Evolua o journal para dupla entrada persistente, append-only, hash chain e projeção reconstruível.",
                        List.of("postingBalancesToZero", "journalIsAppendOnly", "hashChainVerified", "projectionRebuilt"),
                        List.of("unbalancedPosting", "journalMutation"),
                        "backend/src/main/java/com/portujava/bank/ledger", "Altere uma entrada histórica e reconstrua o saldo."),
                phase(module, 7, "internal-transfers", "Transferências internas",
                        "Garanta débito e crédito atômicos, ordenação de locks e ausência de saldo negativo.",
                        List.of("accountsLockedInOrder", "postingIsAtomic", "overdraftRejected", "retryIsIdempotent"),
                        List.of("lostUpdate", "partialTransfer"),
                        "backend/src/main/java/com/portujava/bank/transfer", "Execute transferências A→B e B→A simultaneamente."),
                phase(module, 8, "pix-key", "Pix por chave",
                        "Modele chave, lookup, intent, envio, recebimento e comprovante com rastreabilidade ponta a ponta.",
                        List.of("keyTypeValidated", "intentPersisted", "pixRequestIsIdempotent", "receiptIsTraceable"),
                        List.of("duplicatePixEffect", "unverifiedSettlement"),
                        "backend/src/main/java/com/portujava/bank/pix/key", "Repita a iniciação após timeout ambíguo."),
                phase(module, 9, "pix-qr-emv", "Pix QR e EMV",
                        "Faça parsing defensivo, CRC, payload hash, expiração e corpus dourado.",
                        List.of("crcValidated", "payloadHashStored", "expirationChecked", "goldenCorpusPassed"),
                        List.of("tamperedQrAccepted", "replayedQrPaid"),
                        "backend/src/main/java/com/portujava/bank/pix/qr", "Mude um byte do payload sem recalcular o CRC."),
                phase(module, 10, "baas-adapters", "BaaS e adapters",
                        "Isole o PSP atrás de port, contrato, timeout, retry controlado e circuit breaker.",
                        List.of("gatewayPortDefined", "contractTestPassed", "ambiguousResultIsPending", "circuitBreakerObserved"),
                        List.of("providerControlsDomain", "blindRetryAfterTimeout"),
                        "backend/src/main/java/com/portujava/bank/paymentgateway", "Cause timeout depois do commit remoto."),
                phase(module, 11, "webhooks-settlement", "Webhooks e settlement",
                        "Valide assinatura, use inbox/deduplicação e aceite eventos fora de ordem sem duplo efeito.",
                        List.of("signatureValidated", "inboxPersisted", "duplicateIgnored", "settlementTransitionValidated"),
                        List.of("unsignedWebhookAccepted", "doubleSettlement"),
                        "backend/src/main/java/com/portujava/bank/webhook", "Entregue settlement duas vezes e depois um evento antigo."),
                phase(module, 12, "reconciliation", "Reconciliação",
                        "Compare journal, projeção e PSP; classifique drift e produza reparo rastreável.",
                        List.of("journalCompared", "projectionCompared", "providerCompared", "repairIsAudited"),
                        List.of("unexplainedDrift", "destructiveBalancePatch"),
                        "backend/src/main/java/com/portujava/bank/reconciliation", "Injete uma liquidação externa ausente localmente."),
                phase(module, 13, "charges", "Boleto e cobrança",
                        "Modele emissão, vencimento, baixa, links e parcelamento por eventos versionados.",
                        List.of("contractVersioned", "dueDateEnforced", "paymentEventIdempotent", "accountingEventPosted"),
                        List.of("paidChargeReopened", "unversionedProviderPayload"),
                        "backend/src/main/java/com/portujava/bank/charge", "Receba baixa depois do vencimento e em duplicidade."),
                phase(module, 14, "treasury", "Tesouraria",
                        "Separe disponível, reserva, tarifas, lastro e rendimento com fechamento balanceado.",
                        List.of("reserveSegregated", "yieldRuleVersioned", "closingBalances", "backingReconciled"),
                        List.of("reserveSpentAsAvailable", "unbackedBalance"),
                        "backend/src/main/java/com/portujava/bank/treasury", "Tente gastar a reserva durante fechamento concorrente."),
                phase(module, 15, "kyc-privacy-aml", "KYC, LGPD e PLD/FT",
                        "Implemente consentimento, minimização, retenção, mascaramento e revisão restrita.",
                        List.of("consentRecorded", "piiMasked", "retentionPolicyApplied", "restrictedReviewAudited"),
                        List.of("fullDocumentLogged", "expiredDataRetainedWithoutBasis"),
                        "backend/src/main/java/com/portujava/bank/compliance", "Execute exportação e descarte após expirar a base legal."),
                phase(module, 16, "fraud", "Antifraude",
                        "Produza decisão determinística, explicável, versionada e sujeita a revisão humana.",
                        List.of("ruleVersionRecorded", "velocityCalculated", "reasonsExposed", "humanReviewAvailable"),
                        List.of("silentModelDecision", "unauthorizedAutoApproval"),
                        "backend/src/main/java/com/portujava/bank/fraud", "Simule rajada distribuída no limite de velocity."),
                phase(module, 17, "distributed-controls", "Redis e controles distribuídos",
                        "Modele rate limit, lock, cache e fallback seguro quando o controle distribuído falhar.",
                        List.of("rateLimitIsAtomic", "lockHasOwnerAndLease", "cacheIsNotSourceOfTruth", "outageFailsSafely"),
                        List.of("lockReleasedByNonOwner", "cacheOutageBypassesLimit"),
                        "backend/src/main/java/com/portujava/bank/distributed", "Derrube o Redis durante autorização financeira."),
                phase(module, 18, "bank-operations", "Operação bancária",
                        "Defina métricas, tracing, readiness, SLO, alertas, backup restaurável e replay.",
                        List.of("financialSloDefined", "tracesAreCorrelated", "backupRestoreTested", "runbookRehearsed"),
                        List.of("unknownRecoveryPoint", "alertWithoutAction"),
                        "infrastructure/observability", "Restaure backup e reconcilie o RPO medido."),
                phase(module, 19, "bank-frontend", "Angular e PWA bancário",
                        "Construa jornadas acessíveis sem double submit, cache inseguro ou confiança no cliente.",
                        List.of("backendRemainsAuthoritative", "doubleSubmitBlocked", "cacheVersioned", "criticalFlowIsAccessible"),
                        List.of("clientSideAuthorization", "stalePaymentConfirmation"),
                        "frontend/src/app/features/payments", "Abra duas abas e repita a confirmação com bundle antigo."),
                phase(module, 20, "governed-ai", "IA financeira governada",
                        "Limite tools e autoridade; uma resposta gerada nunca movimenta dinheiro por implicação.",
                        List.of("toolAllowlistEnforced", "userIntentReconfirmed", "financialActionIsDeterministic", "fallbackFailsClosed"),
                        List.of("modelHasImplicitAuthority", "promptInjectionControlsPayment"),
                        "backend/src/main/java/com/portujava/bank/assistant", "Injete instrução maliciosa em conteúdo recuperado."),
                phase(module, 21, "supply-chain", "Supply chain e entrega",
                        "Automatize build, testes, SAST, secrets, SBOM, proveniência, rollout e rollback.",
                        List.of("buildIsReproducible", "securityScansPassed", "sbomPublished", "rollbackRehearsed"),
                        List.of("secretInRepository", "unsignedUntraceableArtifact"),
                        ".github/workflows", "Interrompa o rollout depois de uma regressão de SLO."),
                phase(module, 22, "capstone", "Capstone: outro banco completo",
                        "Parta do repositório virtual vazio, integre todas as fases, faça deploy, responda a incidente, reconcilie e defenda as decisões.",
                        List.of("allPhaseGatesPassed", "deploymentEvidenceRecorded", "incidentDrillClosed", "reconciliationIsZero", "technicalDefenseApproved"),
                        List.of("copiedReferenceSolution", "unresolvedFinancialVeto"),
                        "docs/final-defense", "Recupere um timeout ambíguo com webhook duplicado e drift contábil."));
    }

    private static Exercise phase(LearningModule module, int phase, String slug, String title,
                                  String objective, List<String> requiredEvidence, List<String> vetoes,
                                  String projectPath, String incident) {
        String className = "BankPhase%02dGate".formatted(phase);
        String exerciseId = "bank-real-%02d-phase-%02d-%s".formatted(phase + 14, phase, slug);
        int sortOrder = phase + 13;
        String requiredText = requiredEvidence.stream().map(value -> "`" + value + "`")
                .reduce((left, right) -> left + ", " + right).orElse("");
        String vetoText = vetoes.stream().map(value -> "`" + value + "`")
                .reduce((left, right) -> left + ", " + right).orElse("");

        String statement = """
                ## Fase %d — %s

                %s

                Este é o gate de saída da fase. No **Laboratório de Repositório**, implemente o
                incremento em `%s`, conecte-o às fases anteriores e registre a evidência. Depois,
                implemente este gate em Java para impedir aprovação baseada em narrativa.

                ### Evidências obrigatórias

                %s

                ### Vetos

                %s

                ### Ensaio de falha

                %s

                `released` só pode ser verdadeiro quando todas as evidências forem `true` e nenhum
                veto estiver ativo. O resultado também deve explicar exatamente o que falta e quais
                vetos bloquearam a entrega.
                """.formatted(phase, title, objective, projectPath, requiredText, vetoText, incident);

        String contract = """
                public final class %s {
                    public record GateDecision(boolean released, java.util.List<String> missingEvidence,
                                               java.util.List<String> activeVetoes) {}
                    public GateDecision evaluate(java.util.Map<String, Boolean> evidence) { return null; }
                }
                """.formatted(className);
        String starter = """
                public final class %s {
                    public record GateDecision(boolean released, java.util.List<String> missingEvidence,
                                               java.util.List<String> activeVetoes) {}

                    public GateDecision evaluate(java.util.Map<String, Boolean> evidence) {
                        // TODO: fail closed when evidence is missing or a veto is active.
                        return null;
                    }
                }
                """.formatted(className);
        String solution = """
                public final class %s {
                    private static final java.util.List<String> REQUIRED = java.util.List.of(%s);
                    private static final java.util.List<String> VETOES = java.util.List.of(%s);

                    public record GateDecision(boolean released, java.util.List<String> missingEvidence,
                                               java.util.List<String> activeVetoes) {}

                    public GateDecision evaluate(java.util.Map<String, Boolean> evidence) {
                        if (evidence == null) {
                            throw new IllegalArgumentException("Evidence is required");
                        }
                        java.util.List<String> missing = REQUIRED.stream()
                                .filter(key -> !Boolean.TRUE.equals(evidence.get(key)))
                                .toList();
                        java.util.List<String> active = VETOES.stream()
                                .filter(key -> Boolean.TRUE.equals(evidence.get(key)))
                                .toList();
                        return new GateDecision(missing.isEmpty() && active.isEmpty(), missing, active);
                    }
                }
                """.formatted(className, quoted(requiredEvidence), quoted(vetoes));

        String passingSetup = evidenceSetup(requiredEvidence, vetoes, null, null);
        String missingKey = requiredEvidence.get(0);
        String missingSetup = evidenceSetup(requiredEvidence, vetoes, missingKey, null);
        String activeVeto = vetoes.get(0);
        String vetoSetup = evidenceSetup(requiredEvidence, vetoes, null, activeVeto);

        return ExerciseBuilder.of(exerciseId, module, "Fase %d: %s — gate executável".formatted(phase, title),
                        statement, contract, starter, phase < 8 ? "MÉDIO" : "AVANÇADO", sortOrder, 35)
                .referenceSolution(solution)
                .solutionAnnotation("missing.isEmpty() && active.isEmpty()",
                        "A liberação exige prova positiva completa e ausência de veto. Uma condição não substitui a outra.")
                .solutionAnnotation("!Boolean.TRUE.equals(evidence.get(key))",
                        "Ausência de evidência falha fechado; uma chave não informada não é tratada como aprovação.")
                .equalsCase("libera somente com evidência completa e sem veto", passingSetup,
                        "new %s().evaluate(evidence).released()".formatted(className), "true", true)
                .equalsCase("bloqueia e nomeia a primeira evidência ausente", missingSetup,
                        "new %s().evaluate(evidence).missingEvidence()".formatted(className),
                        "java.util.List.of(\"%s\")".formatted(missingKey), true)
                .equalsCase("bloqueia e nomeia o veto ativo", vetoSetup,
                        "new %s().evaluate(evidence).activeVetoes()".formatted(className),
                        "java.util.List.of(\"%s\")".formatted(activeVeto), false)
                .throwsCase("mapa ausente falha fechado", "",
                        "new %s().evaluate(null)".formatted(className), "IllegalArgumentException", false)
                .hint("Percorra a lista de evidências obrigatórias e colete as que não são Boolean.TRUE.")
                .hint("Percorra os vetos separadamente e colete somente os que estão ativos.")
                .hint("Calcule released a partir das duas listas; não confie em uma flag enviada pelo cliente.")
                .build();
    }

    private static String quoted(List<String> values) {
        return values.stream().map(value -> "\"" + value + "\"")
                .reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String evidenceSetup(List<String> required, List<String> vetoes,
                                        String missingKey, String activeVeto) {
        StringBuilder setup = new StringBuilder("java.util.Map<String, Boolean> evidence = new java.util.HashMap<>(); ");
        for (String key : required) {
            if (!key.equals(missingKey)) {
                setup.append("evidence.put(\"").append(key).append("\", true); ");
            }
        }
        for (String veto : vetoes) {
            setup.append("evidence.put(\"").append(veto).append("\", ")
                    .append(veto.equals(activeVeto)).append("); ");
        }
        return setup.toString();
    }
}
