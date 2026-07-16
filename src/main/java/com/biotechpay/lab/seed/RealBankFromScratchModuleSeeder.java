package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * First executable slice of the real-banking curriculum derived from BioCodeTechPay's strongest
 * financial primitives. The exercises are intentionally framework-free: students must understand
 * the invariants in plain Java before Spring, JPA, Redis, queues or a PSP can hide the mechanics.
 */
@Component
public class RealBankFromScratchModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public RealBankFromScratchModuleSeeder(LearningModuleRepository moduleRepository,
                                            ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "real-bank-from-scratch";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Construindo um banco inteiro sozinho Real",
                "ENGENHARIA BANCÁRIA",
                "Construa outro banco do zero em 22 fases: invariantes financeiras, modelagem, " +
                        "identidade, ledger, Pix, integrações, reconciliação, risco, operação, " +
                        "frontend e entrega. As fases 3 a 22 possuem gates executáveis e são " +
                        "integradas no Laboratório de Repositório vazio do PortuJava.",
                7));

        synchronize(module);
        return module;
    }

    @Override
    public void synchronize(LearningModule existingModule) {
        saveIfMissing(buildMoneyValueObject(existingModule));
        saveIfMissing(buildBalancedPosting(existingModule));
        saveIfMissing(buildPaymentStateMachine(existingModule));
        saveIfMissing(buildFinancialIdempotency(existingModule));
        saveIfMissing(buildAtomicAccount(existingModule));
        saveIfMissing(buildTamperEvidentJournal(existingModule));
        saveIfMissing(buildReconciliationDecision(existingModule));
        saveIfMissing(buildAmbiguousGatewayResult(existingModule));
        saveIfMissing(buildAtomicJournalSequence(existingModule));
        saveIfMissing(buildCasBalance(existingModule));
        saveIfMissing(buildAtomicTransfer(existingModule));
        BankModelingPhaseExercises.build(existingModule).forEach(this::saveIfMissing);
        BankDeliveryPhaseExercises.build(existingModule).forEach(this::saveIfMissing);
    }

    private void saveIfMissing(Exercise exercise) {
        if (!exerciseRepository.existsByExerciseId(exercise.getExerciseId())) {
            exerciseRepository.save(exercise);
        }
    }

    private Exercise buildMoneyValueObject(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-01-money-value-object",
                        module,
                        "Dinheiro não é double",
                        """
                        ## Contexto real

                        `double` não representa valores decimais com exatidão. Em um banco, um erro de
                        arredondamento repetido milhões de vezes vira divergência contábil. O valor deve
                        nascer válido e continuar válido depois de cada operação.

                        ## Objetivo

                        Implemente `public final class Money` usando `BigDecimal`:

                        - `Money of(String raw)` cria BRL com escala 2 e `RoundingMode.HALF_UP`.
                        - Valores negativos são rejeitados com `IllegalArgumentException`.
                        - `add` soma sem perder precisão.
                        - `subtract` rejeita saldo insuficiente com `IllegalStateException`.
                        - `value` devolve o `BigDecimal` normalizado.

                        ## Invariante

                        Nenhuma operação pode produzir valor negativo ou uma escala diferente de dois
                        centavos. Não use `double` em nenhum caminho do domínio.
                        """,
                        """
                        public final class Money implements Comparable<Money> {
                            public static Money of(String raw) { return null; }
                            public java.math.BigDecimal value() { return null; }
                            public Money add(Money other) { return null; }
                            public Money subtract(Money other) { return null; }
                            public int compareTo(Money other) { return 0; }
                        }
                        """,
                        """
                        public final class Money implements Comparable<Money> {
                            private final java.math.BigDecimal value;

                            private Money(java.math.BigDecimal value) {
                                // TODO: normalize to two decimal places and reject negative values.
                                this.value = value;
                            }

                            public static Money of(String raw) {
                                return null;
                            }

                            public java.math.BigDecimal value() { return value; }
                            public Money add(Money other) { return null; }
                            public Money subtract(Money other) { return null; }
                            public int compareTo(Money other) { return 0; }
                        }
                        """,
                        "BÁSICO", 0, 20)
                .referenceSolution("""
                        public final class Money implements Comparable<Money> {
                            private static final int SCALE = 2;
                            private final java.math.BigDecimal value;

                            private Money(java.math.BigDecimal value) {
                                java.math.BigDecimal normalized = value.setScale(
                                        SCALE, java.math.RoundingMode.HALF_UP);
                                if (normalized.signum() < 0) {
                                    throw new IllegalArgumentException("Money cannot be negative");
                                }
                                this.value = normalized;
                            }

                            public static Money of(String raw) {
                                if (raw == null || raw.isBlank()) {
                                    throw new IllegalArgumentException("Money value is required");
                                }
                                return new Money(new java.math.BigDecimal(raw.trim()));
                            }

                            public java.math.BigDecimal value() {
                                return value;
                            }

                            public Money add(Money other) {
                                java.util.Objects.requireNonNull(other, "other");
                                return new Money(value.add(other.value));
                            }

                            public Money subtract(Money other) {
                                java.util.Objects.requireNonNull(other, "other");
                                if (value.compareTo(other.value) < 0) {
                                    throw new IllegalStateException("Insufficient funds");
                                }
                                return new Money(value.subtract(other.value));
                            }

                            @Override
                            public int compareTo(Money other) {
                                return value.compareTo(other.value);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "value.setScale(SCALE, java.math.RoundingMode.HALF_UP)",
                        "A regra de arredondamento é parte do contrato financeiro. Ela não pode depender " +
                                "do default da JVM, do banco ou da interface.")
                .solutionAnnotation(
                        "if (value.compareTo(other.value) < 0)",
                        "O saldo é comparado antes da subtração. Falhar antes de mutar evita criar um " +
                                "estado negativo que depois precisaria ser compensado.")
                .equalsCase("arredonda meio centavo pelo contrato HALF_UP",
                        "Money money = Money.of(\"10.005\");",
                        "money.value()", "new java.math.BigDecimal(\"10.01\")", true)
                .equalsCase("soma decimal sem erro binário",
                        "Money result = Money.of(\"0.10\").add(Money.of(\"0.20\"));",
                        "result.value()", "new java.math.BigDecimal(\"0.30\")", true)
                .equalsCase("subtração válida preserva escala",
                        "Money result = Money.of(\"100.00\").subtract(Money.of(\"37.41\"));",
                        "result.value()", "new java.math.BigDecimal(\"62.59\")", false)
                .throwsCase("valor negativo é inválido",
                        "", "Money.of(\"-0.01\")", "IllegalArgumentException", true)
                .throwsCase("débito acima do saldo falha fechado",
                        "Money balance = Money.of(\"10.00\");",
                        "balance.subtract(Money.of(\"10.01\"))", "IllegalStateException", false)
                .hint("Crie o BigDecimal diretamente da String; não passe por double.")
                .hint("Normalize no construtor para que toda instância de Money já nasça válida.")
                .hint("Compare antes de subtrair e lance a exceção antes de criar o novo valor.")
                .build();
    }

    private Exercise buildBalancedPosting(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-02-balanced-posting",
                        module,
                        "Dupla entrada: dinheiro não surge do nada",
                        """
                        ## Contexto real

                        Um saldo isolado não explica de onde o dinheiro veio. Um posting de dupla entrada
                        registra todas as contrapartidas e deve sempre resultar em zero no journal global.

                        ## Objetivo

                        Implemente `public class BalancedPosting` com os tipos aninhados `Side`, `Leg` e
                        `Posting`. O método `post(String transactionId, List<Leg> legs)` deve:

                        - exigir identificador e pelo menos duas pernas;
                        - exigir conta, lado e valor positivo em cada perna;
                        - somar créditos como positivo e débitos como negativo;
                        - rejeitar posting cujo total não seja exatamente `0.00`;
                        - devolver cópia imutável das pernas aceitas.

                        ## Invariante

                        A soma de créditos deve ser igual à soma de débitos antes de qualquer persistência.
                        """,
                        """
                        public class BalancedPosting {
                            public enum Side { DEBIT, CREDIT }
                            public record Leg(String accountId, Side side, java.math.BigDecimal amount) {}
                            public record Posting(String transactionId, java.util.List<Leg> legs,
                                                  java.math.BigDecimal net) {}
                            public Posting post(String transactionId, java.util.List<Leg> legs) { return null; }
                        }
                        """,
                        """
                        public class BalancedPosting {
                            public enum Side { DEBIT, CREDIT }
                            public record Leg(String accountId, Side side, java.math.BigDecimal amount) {}
                            public record Posting(String transactionId, java.util.List<Leg> legs,
                                                  java.math.BigDecimal net) {}

                            public Posting post(String transactionId, java.util.List<Leg> legs) {
                                // TODO: validate every leg, compute the signed net and require 0.00.
                                return null;
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 1, 25)
                .referenceSolution("""
                        public class BalancedPosting {
                            public enum Side { DEBIT, CREDIT }
                            public record Leg(String accountId, Side side, java.math.BigDecimal amount) {}
                            public record Posting(String transactionId, java.util.List<Leg> legs,
                                                  java.math.BigDecimal net) {}

                            public Posting post(String transactionId, java.util.List<Leg> legs) {
                                if (transactionId == null || transactionId.isBlank()) {
                                    throw new IllegalArgumentException("Transaction id is required");
                                }
                                if (legs == null || legs.size() < 2) {
                                    throw new IllegalArgumentException("At least two legs are required");
                                }
                                java.math.BigDecimal net = new java.math.BigDecimal("0.00");
                                for (Leg leg : legs) {
                                    if (leg == null || leg.accountId() == null || leg.accountId().isBlank()
                                            || leg.side() == null || leg.amount() == null) {
                                        throw new IllegalArgumentException("Invalid ledger leg");
                                    }
                                    java.math.BigDecimal amount = leg.amount().setScale(
                                            2, java.math.RoundingMode.UNNECESSARY);
                                    if (amount.signum() <= 0) {
                                        throw new IllegalArgumentException("Leg amount must be positive");
                                    }
                                    net = leg.side() == Side.CREDIT ? net.add(amount) : net.subtract(amount);
                                }
                                net = net.setScale(2, java.math.RoundingMode.UNNECESSARY);
                                if (net.signum() != 0) {
                                    throw new IllegalArgumentException("Unbalanced posting: " + net);
                                }
                                return new Posting(transactionId, java.util.List.copyOf(legs), net);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "net = leg.side() == Side.CREDIT ? net.add(amount) : net.subtract(amount);",
                        "Cada perna possui sinal contábil explícito. Se o total não for zero, existe " +
                                "dinheiro sem contrapartida.")
                .solutionAnnotation(
                        "java.util.List.copyOf(legs)",
                        "O posting aceito não pode mudar depois da validação; a cópia imutável fecha " +
                                "essa porta dentro do modelo em memória.")
                .equalsCase("depósito é balanceado contra o mundo externo",
                        "BalancedPosting ledger = new BalancedPosting(); " +
                                "var legs = java.util.List.of(" +
                                "new BalancedPosting.Leg(\"customer\", BalancedPosting.Side.CREDIT, new java.math.BigDecimal(\"100.00\"))," +
                                "new BalancedPosting.Leg(\"external:world\", BalancedPosting.Side.DEBIT, new java.math.BigDecimal(\"100.00\")));",
                        "ledger.post(\"tx-1\", legs).net()", "new java.math.BigDecimal(\"0.00\")", true)
                .equalsCase("transferência pode ter mais de duas pernas e continuar balanceada",
                        "BalancedPosting ledger = new BalancedPosting(); " +
                                "var legs = java.util.List.of(" +
                                "new BalancedPosting.Leg(\"sender\", BalancedPosting.Side.DEBIT, new java.math.BigDecimal(\"101.00\"))," +
                                "new BalancedPosting.Leg(\"receiver\", BalancedPosting.Side.CREDIT, new java.math.BigDecimal(\"100.00\"))," +
                                "new BalancedPosting.Leg(\"fees\", BalancedPosting.Side.CREDIT, new java.math.BigDecimal(\"1.00\")));",
                        "ledger.post(\"tx-2\", legs).legs().size()", "3", false)
                .throwsCase("posting desbalanceado é rejeitado antes de persistir",
                        "BalancedPosting ledger = new BalancedPosting(); " +
                                "var legs = java.util.List.of(" +
                                "new BalancedPosting.Leg(\"a\", BalancedPosting.Side.DEBIT, new java.math.BigDecimal(\"10.00\"))," +
                                "new BalancedPosting.Leg(\"b\", BalancedPosting.Side.CREDIT, new java.math.BigDecimal(\"9.99\")));",
                        "ledger.post(\"tx-broken\", legs)", "IllegalArgumentException", true)
                .throwsCase("perna de valor zero é inválida",
                        "BalancedPosting ledger = new BalancedPosting(); " +
                                "var legs = java.util.List.of(" +
                                "new BalancedPosting.Leg(\"a\", BalancedPosting.Side.DEBIT, new java.math.BigDecimal(\"0.00\"))," +
                                "new BalancedPosting.Leg(\"b\", BalancedPosting.Side.CREDIT, new java.math.BigDecimal(\"0.00\")));",
                        "ledger.post(\"tx-zero\", legs)", "IllegalArgumentException", false)
                .hint("Comece o net em 0.00; crédito soma e débito subtrai.")
                .hint("Valide todas as pernas antes de devolver o Posting.")
                .hint("Use List.copyOf para não expor uma lista mutável já validada.")
                .build();
    }

    private Exercise buildPaymentStateMachine(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-03-payment-state-machine",
                        module,
                        "Pagamento é uma máquina de estados",
                        """
                        ## Contexto real

                        Um pagamento não pode saltar de criado para confirmado porque a tela recebeu uma
                        resposta otimista. Estados terminais também não podem ser reabertos por webhook
                        atrasado. A transição é uma regra de domínio, não um `setStatus` genérico.

                        ## Objetivo

                        Implemente `PaymentStateMachine.transition(current, next)`:

                        - `CREATED` aceita `PROCESSING` ou `CANCELED`.
                        - `PROCESSING` aceita `CONFIRMED` ou `FAILED`.
                        - `CONFIRMED`, `FAILED` e `CANCELED` são terminais.
                        - Repetir exatamente o estado atual é idempotente e devolve o próprio estado.
                        - Qualquer outra transição lança `IllegalStateException`.
                        """,
                        """
                        public class PaymentStateMachine {
                            public enum Status { CREATED, PROCESSING, CONFIRMED, FAILED, CANCELED }
                            public Status transition(Status current, Status next) { return null; }
                        }
                        """,
                        """
                        public class PaymentStateMachine {
                            public enum Status { CREATED, PROCESSING, CONFIRMED, FAILED, CANCELED }

                            public Status transition(Status current, Status next) {
                                // TODO: accept only the declared state graph; same-state is a no-op.
                                return null;
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 2, 20)
                .referenceSolution("""
                        public class PaymentStateMachine {
                            public enum Status { CREATED, PROCESSING, CONFIRMED, FAILED, CANCELED }

                            public Status transition(Status current, Status next) {
                                java.util.Objects.requireNonNull(current, "current");
                                java.util.Objects.requireNonNull(next, "next");
                                if (current == next) {
                                    return current;
                                }
                                boolean allowed = switch (current) {
                                    case CREATED -> next == Status.PROCESSING || next == Status.CANCELED;
                                    case PROCESSING -> next == Status.CONFIRMED || next == Status.FAILED;
                                    case CONFIRMED, FAILED, CANCELED -> false;
                                };
                                if (!allowed) {
                                    throw new IllegalStateException("Invalid transition: " + current + " -> " + next);
                                }
                                return next;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (current == next) { return current; }",
                        "Webhooks e retries podem repetir o mesmo fato. Um evento já aplicado deve ser " +
                                "um no-op, não uma nova movimentação.")
                .solutionAnnotation(
                        "case CONFIRMED, FAILED, CANCELED -> false;",
                        "Estados terminais ficam fechados. Reabrir uma liquidação ou falha exigiria um " +
                                "novo evento compensatório, não uma alteração retroativa.")
                .equalsCase("fluxo criado para processamento é válido",
                        "PaymentStateMachine machine = new PaymentStateMachine();",
                        "machine.transition(PaymentStateMachine.Status.CREATED, PaymentStateMachine.Status.PROCESSING)",
                        "PaymentStateMachine.Status.PROCESSING", true)
                .equalsCase("processamento pode confirmar",
                        "PaymentStateMachine machine = new PaymentStateMachine();",
                        "machine.transition(PaymentStateMachine.Status.PROCESSING, PaymentStateMachine.Status.CONFIRMED)",
                        "PaymentStateMachine.Status.CONFIRMED", true)
                .equalsCase("webhook confirmado repetido é idempotente",
                        "PaymentStateMachine machine = new PaymentStateMachine();",
                        "machine.transition(PaymentStateMachine.Status.CONFIRMED, PaymentStateMachine.Status.CONFIRMED)",
                        "PaymentStateMachine.Status.CONFIRMED", false)
                .throwsCase("não é permitido confirmar sem passar por processamento",
                        "PaymentStateMachine machine = new PaymentStateMachine();",
                        "machine.transition(PaymentStateMachine.Status.CREATED, PaymentStateMachine.Status.CONFIRMED)",
                        "IllegalStateException", true)
                .throwsCase("falha terminal não pode virar confirmação tardia",
                        "PaymentStateMachine machine = new PaymentStateMachine();",
                        "machine.transition(PaymentStateMachine.Status.FAILED, PaymentStateMachine.Status.CONFIRMED)",
                        "IllegalStateException", false)
                .hint("Trate current == next antes do grafo de transições.")
                .hint("Use switch sobre o estado atual e liste explicitamente os destinos aceitos.")
                .build();
    }

    private Exercise buildFinancialIdempotency(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-04-financial-idempotency",
                        module,
                        "Idempotência persistente por escopo",
                        """
                        ## Contexto real

                        O mesmo botão, retry HTTP ou webhook pode chegar várias vezes. A chave só é única
                        dentro do escopo correto: operação + sujeito + idempotency key. Reusar a chave com
                        payload diferente é suspeito e deve ser bloqueado.

                        ## Objetivo

                        Implemente `FinancialIdempotencyRegistry` em memória para aprender o contrato:

                        - A primeira reserva retorna `CLAIMED` e fica `PROCESSING`.
                        - Repetição em `PROCESSING` ou `COMPLETED` retorna `REPLAY`.
                        - Mesmo escopo com outro payload hash retorna `PAYLOAD_MISMATCH`.
                        - Uma tentativa marcada `FAILED` pode ser reservada novamente.
                        - `COMPLETED` preserva e devolve a mesma referência de resultado.
                        - Os métodos devem ser `synchronized` para que a decisão seja atômica.
                        """,
                        """
                        public class FinancialIdempotencyRegistry {
                            public enum DecisionType { CLAIMED, REPLAY, PAYLOAD_MISMATCH }
                            public record Decision(DecisionType type, String state, String resultReference) {}
                            public synchronized Decision claim(String operation, String subject, String key,
                                                               String payloadHash) { return null; }
                            public synchronized void complete(String operation, String subject, String key,
                                                              String resultReference) {}
                            public synchronized void fail(String operation, String subject, String key) {}
                        }
                        """,
                        """
                        public class FinancialIdempotencyRegistry {
                            public enum DecisionType { CLAIMED, REPLAY, PAYLOAD_MISMATCH }
                            public record Decision(DecisionType type, String state, String resultReference) {}
                            // TODO: model Scope and mutable Entry, then make claim/complete/fail atomic.
                            public synchronized Decision claim(String operation, String subject, String key,
                                                               String payloadHash) { return null; }
                            public synchronized void complete(String operation, String subject, String key,
                                                              String resultReference) {}
                            public synchronized void fail(String operation, String subject, String key) {}
                        }
                        """,
                        "AVANÇADO", 3, 35)
                .referenceSolution("""
                        public class FinancialIdempotencyRegistry {
                            public enum DecisionType { CLAIMED, REPLAY, PAYLOAD_MISMATCH }
                            public record Decision(DecisionType type, String state, String resultReference) {}
                            private record Scope(String operation, String subject, String key) {}
                            private static final class Entry {
                                private final String payloadHash;
                                private String state = "PROCESSING";
                                private String resultReference;
                                private Entry(String payloadHash) { this.payloadHash = payloadHash; }
                            }
                            private final java.util.Map<Scope, Entry> entries = new java.util.HashMap<>();

                            public synchronized Decision claim(String operation, String subject, String key,
                                                               String payloadHash) {
                                validate(operation, subject, key, payloadHash);
                                Scope scope = new Scope(operation, subject, key);
                                Entry existing = entries.get(scope);
                                if (existing == null) {
                                    entries.put(scope, new Entry(payloadHash));
                                    return new Decision(DecisionType.CLAIMED, "PROCESSING", null);
                                }
                                if (!existing.payloadHash.equals(payloadHash)) {
                                    return new Decision(DecisionType.PAYLOAD_MISMATCH, existing.state,
                                            existing.resultReference);
                                }
                                if (existing.state.equals("FAILED")) {
                                    existing.state = "PROCESSING";
                                    existing.resultReference = null;
                                    return new Decision(DecisionType.CLAIMED, existing.state, null);
                                }
                                return new Decision(DecisionType.REPLAY, existing.state,
                                        existing.resultReference);
                            }

                            public synchronized void complete(String operation, String subject, String key,
                                                              String resultReference) {
                                Entry entry = requireEntry(new Scope(operation, subject, key));
                                entry.state = "COMPLETED";
                                entry.resultReference = java.util.Objects.requireNonNull(resultReference);
                            }

                            public synchronized void fail(String operation, String subject, String key) {
                                Entry entry = requireEntry(new Scope(operation, subject, key));
                                entry.state = "FAILED";
                                entry.resultReference = null;
                            }

                            private Entry requireEntry(Scope scope) {
                                Entry entry = entries.get(scope);
                                if (entry == null) throw new IllegalStateException("Idempotency key was not claimed");
                                return entry;
                            }

                            private static void validate(String... values) {
                                for (String value : values) {
                                    if (value == null || value.isBlank()) {
                                        throw new IllegalArgumentException("Idempotency fields are required");
                                    }
                                }
                            }
                        }
                        """)
                .solutionAnnotation(
                        "private record Scope(String operation, String subject, String key) {}",
                        "A chave global isolada é insuficiente. O escopo impede colisão entre operações " +
                                "ou clientes diferentes.")
                .solutionAnnotation(
                        "if (!existing.payloadHash.equals(payloadHash))",
                        "A mesma chave com outro corpo não é replay legítimo. Processar esse caso poderia " +
                                "executar uma intenção diferente sob a identidade da primeira.")
                .equalsCase("primeira chamada conquista a reserva",
                        "FinancialIdempotencyRegistry registry = new FinancialIdempotencyRegistry();",
                        "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\").type()",
                        "FinancialIdempotencyRegistry.DecisionType.CLAIMED", true)
                .equalsCase("repetição durante processamento não executa de novo",
                        "FinancialIdempotencyRegistry registry = new FinancialIdempotencyRegistry(); " +
                                "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\");",
                        "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\").type()",
                        "FinancialIdempotencyRegistry.DecisionType.REPLAY", true)
                .equalsCase("payload diferente sob a mesma chave é bloqueado",
                        "FinancialIdempotencyRegistry registry = new FinancialIdempotencyRegistry(); " +
                                "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\");",
                        "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-b\").type()",
                        "FinancialIdempotencyRegistry.DecisionType.PAYLOAD_MISMATCH", true)
                .equalsCase("falha anterior libera retry controlado",
                        "FinancialIdempotencyRegistry registry = new FinancialIdempotencyRegistry(); " +
                                "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\"); " +
                                "registry.fail(\"PIX_SEND\", \"user-1\", \"key-1\");",
                        "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\").type()",
                        "FinancialIdempotencyRegistry.DecisionType.CLAIMED", false)
                .equalsCase("operação concluída devolve a mesma referência",
                        "FinancialIdempotencyRegistry registry = new FinancialIdempotencyRegistry(); " +
                                "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\"); " +
                                "registry.complete(\"PIX_SEND\", \"user-1\", \"key-1\", \"pix-123\");",
                        "registry.claim(\"PIX_SEND\", \"user-1\", \"key-1\", \"hash-a\").resultReference()",
                        "\"pix-123\"", false)
                .hint("Use um record Scope como chave do Map.")
                .hint("A Entry precisa guardar payloadHash, state e resultReference.")
                .hint("FAILED pode voltar a PROCESSING; COMPLETED nunca reabre.")
                .build();
    }

    private Exercise buildAtomicAccount(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-05-atomic-account",
                        module,
                        "Débito concorrente sem saldo negativo",
                        """
                        ## Contexto real

                        Dois pagamentos podem ler o mesmo saldo antes que qualquer um grave o débito. Sem
                        exclusão mútua, ambos são aprovados e o banco cria um overdraft por lost update.

                        ## Objetivo

                        Implemente `public final class AtomicAccount` com `ReentrantLock`:

                        - O construtor recebe o saldo inicial como `String`.
                        - `tryDebit(String rawAmount)` valida valor positivo, bloqueia a região crítica,
                          devolve `false` sem mutar quando não há saldo e `true` após débito atômico.
                        - `credit(String rawAmount)` credita sob o mesmo lock.
                        - `balance()` lê o saldo sob o mesmo lock e devolve escala 2.

                        ## Invariante

                        A decisão "há saldo?" e a subtração pertencem à mesma seção crítica.
                        """,
                        """
                        public final class AtomicAccount {
                            public AtomicAccount(String openingBalance) {}
                            public boolean tryDebit(String rawAmount) { return false; }
                            public void credit(String rawAmount) {}
                            public java.math.BigDecimal balance() { return null; }
                        }
                        """,
                        """
                        public final class AtomicAccount {
                            private final java.util.concurrent.locks.ReentrantLock lock =
                                    new java.util.concurrent.locks.ReentrantLock();
                            private java.math.BigDecimal balance;

                            public AtomicAccount(String openingBalance) {
                                // TODO: parse and normalize the opening balance.
                            }

                            public boolean tryDebit(String rawAmount) {
                                // TODO: validate, lock, check and subtract atomically.
                                return false;
                            }

                            public void credit(String rawAmount) {}
                            public java.math.BigDecimal balance() { return null; }
                        }
                        """,
                        "AVANÇADO", 4, 35)
                .referenceSolution("""
                        public final class AtomicAccount {
                            private final java.util.concurrent.locks.ReentrantLock lock =
                                    new java.util.concurrent.locks.ReentrantLock(true);
                            private java.math.BigDecimal balance;

                            public AtomicAccount(String openingBalance) {
                                this.balance = money(openingBalance);
                                if (balance.signum() < 0) {
                                    throw new IllegalArgumentException("Opening balance cannot be negative");
                                }
                            }

                            public boolean tryDebit(String rawAmount) {
                                java.math.BigDecimal amount = positive(rawAmount);
                                lock.lock();
                                try {
                                    if (balance.compareTo(amount) < 0) {
                                        return false;
                                    }
                                    balance = balance.subtract(amount);
                                    return true;
                                } finally {
                                    lock.unlock();
                                }
                            }

                            public void credit(String rawAmount) {
                                java.math.BigDecimal amount = positive(rawAmount);
                                lock.lock();
                                try {
                                    balance = balance.add(amount);
                                } finally {
                                    lock.unlock();
                                }
                            }

                            public java.math.BigDecimal balance() {
                                lock.lock();
                                try {
                                    return balance;
                                } finally {
                                    lock.unlock();
                                }
                            }

                            private static java.math.BigDecimal positive(String raw) {
                                java.math.BigDecimal amount = money(raw);
                                if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                return amount;
                            }

                            private static java.math.BigDecimal money(String raw) {
                                return new java.math.BigDecimal(raw).setScale(2, java.math.RoundingMode.UNNECESSARY);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (balance.compareTo(amount) < 0) { return false; }\n        balance = balance.subtract(amount);",
                        "A comparação e a mutação acontecem sob o mesmo lock. Separá-las recriaria a " +
                                "janela de lost update.")
                .solutionAnnotation(
                        "} finally {\n        lock.unlock();\n    }",
                        "O unlock no finally evita que uma exceção mantenha a conta bloqueada para sempre.")
                .equalsCase("um entre dois débitos concorrentes é aceito",
                        "AtomicAccount account = new AtomicAccount(\"100.00\"); " +
                                "java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2); " +
                                "java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1); " +
                                "var first = pool.submit(() -> { start.await(); return account.tryDebit(\"80.00\"); }); " +
                                "var second = pool.submit(() -> { start.await(); return account.tryDebit(\"80.00\"); }); " +
                                "start.countDown(); boolean exactlyOne = first.get() ^ second.get(); pool.shutdown();",
                        "java.util.List.of(exactlyOne, account.balance())",
                        "java.util.List.of(true, new java.math.BigDecimal(\"20.00\"))", true)
                .equalsCase("muitos débitos convergem exatamente para zero",
                        "AtomicAccount account = new AtomicAccount(\"50.00\"); " +
                                "java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8); " +
                                "java.util.List<java.util.concurrent.Future<Boolean>> futures = new java.util.ArrayList<>(); " +
                                "for (int i = 0; i < 50; i++) futures.add(pool.submit(() -> account.tryDebit(\"1.00\"))); " +
                                "int accepted = 0; for (var future : futures) if (future.get()) accepted++; pool.shutdown();",
                        "java.util.List.of(accepted, account.balance())",
                        "java.util.List.of(50, new java.math.BigDecimal(\"0.00\"))", false)
                .equalsCase("saldo insuficiente não altera a projeção",
                        "AtomicAccount account = new AtomicAccount(\"10.00\"); boolean accepted = account.tryDebit(\"10.01\");",
                        "java.util.List.of(accepted, account.balance())",
                        "java.util.List.of(false, new java.math.BigDecimal(\"10.00\"))", true)
                .throwsCase("débito zero é rejeitado",
                        "AtomicAccount account = new AtomicAccount(\"10.00\");",
                        "account.tryDebit(\"0.00\")", "IllegalArgumentException", false)
                .hint("O lock precisa cobrir tanto a comparação quanto a subtração.")
                .hint("Sempre libere o lock em finally.")
                .hint("Leitura de balance também deve usar o lock para não observar estado intermediário.")
                .build();
    }

    private Exercise buildTamperEvidentJournal(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-06-tamper-evident-journal",
                        module,
                        "Journal append-only com hash chain",
                        """
                        ## Contexto real

                        Um log editável não é evidência contábil. Cada nova linha deve carregar o hash da
                        linha anterior da mesma conta e seu próprio hash sobre os campos canônicos. Alterar
                        valor, saldo, transação ou ligação quebra a verificação.

                        ## Objetivo

                        Implemente `TamperEvidentJournal`:

                        - `append` recebe conta, valor assinado, transação e evento; valor zero é inválido.
                        - A primeira linha de cada conta usa 64 zeros como `previousHash`.
                        - `balanceAfter` é derivado do head anterior da conta.
                        - SHA-256 cobre todos os campos do record `Entry`, exceto o próprio `hash`.
                        - `verify(List<Entry>)` reconstrói links, saldos e hashes sem confiar nos valores.
                        - `entries()` devolve cópia imutável.
                        """,
                        """
                        public class TamperEvidentJournal {
                            public record Entry(long sequence, String accountId, java.math.BigDecimal amount,
                                                java.math.BigDecimal balanceAfter, String transactionId,
                                                String event, String previousHash, String hash) {}
                            public Entry append(String accountId, String signedAmount, String transactionId,
                                                String event) { return null; }
                            public boolean verify(java.util.List<Entry> candidate) { return false; }
                            public java.util.List<Entry> entries() { return null; }
                        }
                        """,
                        """
                        public class TamperEvidentJournal {
                            public record Entry(long sequence, String accountId, java.math.BigDecimal amount,
                                                java.math.BigDecimal balanceAfter, String transactionId,
                                                String event, String previousHash, String hash) {}
                            // TODO: keep per-account heads and balances, append immutable entries and verify them.
                            public Entry append(String accountId, String signedAmount, String transactionId,
                                                String event) { return null; }
                            public boolean verify(java.util.List<Entry> candidate) { return false; }
                            public java.util.List<Entry> entries() { return null; }
                        }
                        """,
                        "ESPECIALISTA", 5, 50)
                .referenceSolution("""
                        public class TamperEvidentJournal {
                            private static final String GENESIS = "0".repeat(64);
                            public record Entry(long sequence, String accountId, java.math.BigDecimal amount,
                                                java.math.BigDecimal balanceAfter, String transactionId,
                                                String event, String previousHash, String hash) {}
                            private final java.util.List<Entry> journal = new java.util.ArrayList<>();
                            private final java.util.Map<String, String> heads = new java.util.HashMap<>();
                            private final java.util.Map<String, java.math.BigDecimal> balances = new java.util.HashMap<>();

                            public Entry append(String accountId, String signedAmount, String transactionId,
                                                String event) {
                                require(accountId, transactionId, event);
                                java.math.BigDecimal amount = money(signedAmount);
                                if (amount.signum() == 0) throw new IllegalArgumentException("Amount cannot be zero");
                                long sequence = journal.size() + 1L;
                                String previous = heads.getOrDefault(accountId, GENESIS);
                                java.math.BigDecimal after = balances.getOrDefault(accountId,
                                        new java.math.BigDecimal("0.00")).add(amount);
                                String hash = hash(sequence, accountId, amount, after, transactionId, event, previous);
                                Entry entry = new Entry(sequence, accountId, amount, after, transactionId,
                                        event, previous, hash);
                                journal.add(entry);
                                heads.put(accountId, hash);
                                balances.put(accountId, after);
                                return entry;
                            }

                            public boolean verify(java.util.List<Entry> candidate) {
                                java.util.Map<String, String> expectedHeads = new java.util.HashMap<>();
                                java.util.Map<String, java.math.BigDecimal> expectedBalances = new java.util.HashMap<>();
                                long expectedSequence = 1L;
                                for (Entry entry : candidate) {
                                    if (entry.sequence() != expectedSequence++) return false;
                                    String previous = expectedHeads.getOrDefault(entry.accountId(), GENESIS);
                                    java.math.BigDecimal after = expectedBalances.getOrDefault(entry.accountId(),
                                            new java.math.BigDecimal("0.00")).add(entry.amount());
                                    String expectedHash = hash(entry.sequence(), entry.accountId(), entry.amount(),
                                            after, entry.transactionId(), entry.event(), previous);
                                    if (!previous.equals(entry.previousHash()) || after.compareTo(entry.balanceAfter()) != 0
                                            || !expectedHash.equals(entry.hash())) return false;
                                    expectedHeads.put(entry.accountId(), entry.hash());
                                    expectedBalances.put(entry.accountId(), after);
                                }
                                return true;
                            }

                            public java.util.List<Entry> entries() { return java.util.List.copyOf(journal); }

                            private static String hash(long sequence, String accountId, java.math.BigDecimal amount,
                                                       java.math.BigDecimal after, String transactionId,
                                                       String event, String previous) {
                                String canonical = String.join("|", Long.toString(sequence), accountId,
                                        amount.toPlainString(), after.toPlainString(), transactionId, event, previous);
                                try {
                                    byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                                            .digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                                    return java.util.HexFormat.of().formatHex(digest);
                                } catch (java.security.NoSuchAlgorithmException exception) {
                                    throw new IllegalStateException(exception);
                                }
                            }

                            private static java.math.BigDecimal money(String raw) {
                                return new java.math.BigDecimal(raw).setScale(2, java.math.RoundingMode.UNNECESSARY);
                            }
                            private static void require(String... values) {
                                for (String value : values) if (value == null || value.isBlank())
                                    throw new IllegalArgumentException("Journal fields are required");
                            }
                        }
                        """)
                .solutionAnnotation(
                        "String previous = heads.getOrDefault(accountId, GENESIS);",
                        "A cadeia é por conta. A primeira linha usa um genesis conhecido; as seguintes " +
                                "apontam para o hash do head anterior daquela conta.")
                .solutionAnnotation(
                        "if (!previous.equals(entry.previousHash()) || after.compareTo(entry.balanceAfter()) != 0",
                        "A verificação não confia no saldo gravado. Ela o recalcula junto com o link e o hash.")
                .equalsCase("primeira linha começa no genesis",
                        "TamperEvidentJournal journal = new TamperEvidentJournal(); " +
                                "var entry = journal.append(\"account-1\", \"100.00\", \"tx-1\", \"OPENING\");",
                        "entry.previousHash()", "\"0\".repeat(64)", true)
                .equalsCase("segunda linha aponta para o hash anterior",
                        "TamperEvidentJournal journal = new TamperEvidentJournal(); " +
                                "var first = journal.append(\"account-1\", \"100.00\", \"tx-1\", \"OPENING\"); " +
                                "var second = journal.append(\"account-1\", \"-25.00\", \"tx-2\", \"PAYMENT\");",
                        "java.util.List.of(second.previousHash().equals(first.hash()), second.balanceAfter())",
                        "java.util.List.of(true, new java.math.BigDecimal(\"75.00\"))", true)
                .equalsCase("journal original verifica com sucesso",
                        "TamperEvidentJournal journal = new TamperEvidentJournal(); " +
                                "journal.append(\"account-1\", \"100.00\", \"tx-1\", \"OPENING\"); " +
                                "journal.append(\"account-1\", \"-25.00\", \"tx-2\", \"PAYMENT\");",
                        "journal.verify(journal.entries())", "true", false)
                .equalsCase("alterar valor mantendo hash antigo é detectado",
                        "TamperEvidentJournal journal = new TamperEvidentJournal(); " +
                                "var original = journal.append(\"account-1\", \"100.00\", \"tx-1\", \"OPENING\"); " +
                                "var tampered = new TamperEvidentJournal.Entry(original.sequence(), original.accountId(), " +
                                "new java.math.BigDecimal(\"999.00\"), original.balanceAfter(), original.transactionId(), " +
                                "original.event(), original.previousHash(), original.hash());",
                        "journal.verify(java.util.List.of(tampered))", "false", true)
                .equalsCase("contas diferentes possuem genesis independente",
                        "TamperEvidentJournal journal = new TamperEvidentJournal(); " +
                                "journal.append(\"a\", \"10.00\", \"tx-a\", \"OPENING\"); " +
                                "var firstB = journal.append(\"b\", \"20.00\", \"tx-b\", \"OPENING\");",
                        "firstB.previousHash()", "\"0\".repeat(64)", false)
                .hint("Mantenha Map de head e Map de balance por accountId.")
                .hint("A forma canônica precisa usar sempre a mesma ordem e amount.toPlainString().")
                .hint("verify deve reconstruir seu próprio estado temporário; não use os Maps do objeto.")
                .build();
    }

    private Exercise buildReconciliationDecision(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-07-reconciliation-decision",
                        module,
                        "Reconciliação: detectar drift sem esconder o passado",
                        """
                        ## Contexto real

                        O saldo rápido da conta é uma projeção. O journal é a fonte auditável. Quando eles
                        divergem, sobrescrever silenciosamente um dos dois destrói a evidência da causa.

                        ## Objetivo

                        Implemente `ReconciliationDecision.inspect(derived, projected, chainValid)`:

                        - Se a hash chain estiver inválida, devolva `BLOCK_TAMPERING`.
                        - Se os saldos forem iguais, devolva `IN_SYNC` com drift `0.00`.
                        - Se divergirem com cadeia válida, devolva `APPEND_BALANCED_ADJUSTMENT`.
                        - O drift é `projected - derived` e a contrapartida é `external:world`.
                        - O método apenas decide; não altera retrospectivamente o journal.
                        """,
                        """
                        public class ReconciliationDecision {
                            public enum Action { IN_SYNC, APPEND_BALANCED_ADJUSTMENT, BLOCK_TAMPERING }
                            public record Decision(Action action, java.math.BigDecimal drift,
                                                   String counterpartyAccount) {}
                            public Decision inspect(java.math.BigDecimal derived,
                                                    java.math.BigDecimal projected,
                                                    boolean chainValid) { return null; }
                        }
                        """,
                        """
                        public class ReconciliationDecision {
                            public enum Action { IN_SYNC, APPEND_BALANCED_ADJUSTMENT, BLOCK_TAMPERING }
                            public record Decision(Action action, java.math.BigDecimal drift,
                                                   String counterpartyAccount) {}
                            public Decision inspect(java.math.BigDecimal derived,
                                                    java.math.BigDecimal projected,
                                                    boolean chainValid) {
                                // TODO: validate, normalize, compute drift and fail closed on tampering.
                                return null;
                            }
                        }
                        """,
                        "AVANÇADO", 6, 25)
                .referenceSolution("""
                        public class ReconciliationDecision {
                            public enum Action { IN_SYNC, APPEND_BALANCED_ADJUSTMENT, BLOCK_TAMPERING }
                            public record Decision(Action action, java.math.BigDecimal drift,
                                                   String counterpartyAccount) {}

                            public Decision inspect(java.math.BigDecimal derived,
                                                    java.math.BigDecimal projected,
                                                    boolean chainValid) {
                                java.util.Objects.requireNonNull(derived, "derived");
                                java.util.Objects.requireNonNull(projected, "projected");
                                java.math.BigDecimal normalizedDerived = derived.setScale(
                                        2, java.math.RoundingMode.UNNECESSARY);
                                java.math.BigDecimal normalizedProjected = projected.setScale(
                                        2, java.math.RoundingMode.UNNECESSARY);
                                java.math.BigDecimal drift = normalizedProjected.subtract(normalizedDerived);
                                if (!chainValid) {
                                    return new Decision(Action.BLOCK_TAMPERING, drift, null);
                                }
                                if (drift.signum() == 0) {
                                    return new Decision(Action.IN_SYNC, new java.math.BigDecimal("0.00"), null);
                                }
                                return new Decision(Action.APPEND_BALANCED_ADJUSTMENT, drift, "external:world");
                            }
                        }
                        """)
                .solutionAnnotation(
                        "java.math.BigDecimal drift = normalizedProjected.subtract(normalizedDerived);",
                        "O sinal explica a direção: positivo significa projeção acima do journal; negativo, abaixo.")
                .solutionAnnotation(
                        "if (!chainValid) { return new Decision(Action.BLOCK_TAMPERING, drift, null); }",
                        "Com integridade quebrada não existe base segura para ajuste automático. A operação " +
                                "deve ser bloqueada e investigada.")
                .equalsCase("saldos iguais não geram lançamento",
                        "ReconciliationDecision service = new ReconciliationDecision();",
                        "service.inspect(new java.math.BigDecimal(\"50.00\"), new java.math.BigDecimal(\"50.00\"), true).action()",
                        "ReconciliationDecision.Action.IN_SYNC", true)
                .equalsCase("drift positivo pede ajuste balanceado",
                        "ReconciliationDecision service = new ReconciliationDecision(); var decision = " +
                                "service.inspect(new java.math.BigDecimal(\"40.00\"), new java.math.BigDecimal(\"45.00\"), true);",
                        "java.util.List.of(decision.action(), decision.drift(), decision.counterpartyAccount())",
                        "java.util.List.of(ReconciliationDecision.Action.APPEND_BALANCED_ADJUSTMENT, " +
                                "new java.math.BigDecimal(\"5.00\"), \"external:world\")", true)
                .equalsCase("drift negativo preserva o sinal",
                        "ReconciliationDecision service = new ReconciliationDecision();",
                        "service.inspect(new java.math.BigDecimal(\"50.00\"), new java.math.BigDecimal(\"47.50\"), true).drift()",
                        "new java.math.BigDecimal(\"-2.50\")", false)
                .equalsCase("cadeia adulterada bloqueia automação",
                        "ReconciliationDecision service = new ReconciliationDecision();",
                        "service.inspect(new java.math.BigDecimal(\"50.00\"), new java.math.BigDecimal(\"55.00\"), false).action()",
                        "ReconciliationDecision.Action.BLOCK_TAMPERING", true)
                .hint("Normalize os dois saldos antes de subtrair.")
                .hint("Verifique chainValid antes de autorizar qualquer ajuste.")
                .hint("A contrapartida external:world mantém o journal global balanceado.")
                .build();
    }

    private Exercise buildAmbiguousGatewayResult(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-08-ambiguous-gateway-result",
                        module,
                        "Timeout do PSP não significa falha",
                        """
                        ## Contexto real

                        O PSP pode liquidar um pagamento e a resposta se perder. Marcar como falha e tentar
                        novamente pode pagar duas vezes; marcar como confirmado sem referência pode criar
                        dinheiro local. A resposta correta é aguardar reconciliação.

                        ## Objetivo

                        Implemente `PaymentExecution.execute(Command, PaymentGateway)`:

                        - Comando exige valor positivo e idempotency key.
                        - `CONFIRMED` só vira confirmação local quando existe `providerReference`.
                        - `FAILED` vira falha local.
                        - `PENDING`, retorno nulo, referência ausente ou exceção viram
                          `AWAITING_RECONCILIATION`.
                        - Não faça retry automático dentro do método.
                        """,
                        """
                        public class PaymentExecution {
                            public enum GatewayStatus { CONFIRMED, FAILED, PENDING }
                            public enum LocalStatus { CONFIRMED, FAILED, AWAITING_RECONCILIATION }
                            public record Command(java.math.BigDecimal amount, String idempotencyKey) {}
                            public record GatewayResult(GatewayStatus status, String providerReference) {}
                            public record Outcome(LocalStatus status, String providerReference) {}
                            public interface PaymentGateway { GatewayResult pay(Command command) throws Exception; }
                            public Outcome execute(Command command, PaymentGateway gateway) { return null; }
                        }
                        """,
                        """
                        public class PaymentExecution {
                            public enum GatewayStatus { CONFIRMED, FAILED, PENDING }
                            public enum LocalStatus { CONFIRMED, FAILED, AWAITING_RECONCILIATION }
                            public record Command(java.math.BigDecimal amount, String idempotencyKey) {}
                            public record GatewayResult(GatewayStatus status, String providerReference) {}
                            public record Outcome(LocalStatus status, String providerReference) {}
                            public interface PaymentGateway { GatewayResult pay(Command command) throws Exception; }
                            public Outcome execute(Command command, PaymentGateway gateway) {
                                // TODO: validate and map every ambiguous outcome to reconciliation.
                                return null;
                            }
                        }
                        """,
                        "ESPECIALISTA", 7, 30)
                .referenceSolution("""
                        public class PaymentExecution {
                            public enum GatewayStatus { CONFIRMED, FAILED, PENDING }
                            public enum LocalStatus { CONFIRMED, FAILED, AWAITING_RECONCILIATION }
                            public record Command(java.math.BigDecimal amount, String idempotencyKey) {}
                            public record GatewayResult(GatewayStatus status, String providerReference) {}
                            public record Outcome(LocalStatus status, String providerReference) {}
                            public interface PaymentGateway { GatewayResult pay(Command command) throws Exception; }

                            public Outcome execute(Command command, PaymentGateway gateway) {
                                java.util.Objects.requireNonNull(command, "command");
                                java.util.Objects.requireNonNull(gateway, "gateway");
                                if (command.amount() == null || command.amount().signum() <= 0) {
                                    throw new IllegalArgumentException("Amount must be positive");
                                }
                                if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
                                    throw new IllegalArgumentException("Idempotency key is required");
                                }
                                try {
                                    GatewayResult result = gateway.pay(command);
                                    if (result == null || result.status() == null) {
                                        return new Outcome(LocalStatus.AWAITING_RECONCILIATION, null);
                                    }
                                    if (result.status() == GatewayStatus.CONFIRMED) {
                                        if (result.providerReference() == null || result.providerReference().isBlank()) {
                                            return new Outcome(LocalStatus.AWAITING_RECONCILIATION, null);
                                        }
                                        return new Outcome(LocalStatus.CONFIRMED, result.providerReference());
                                    }
                                    if (result.status() == GatewayStatus.FAILED) {
                                        return new Outcome(LocalStatus.FAILED, result.providerReference());
                                    }
                                    return new Outcome(LocalStatus.AWAITING_RECONCILIATION,
                                            result.providerReference());
                                } catch (Exception ambiguousFailure) {
                                    return new Outcome(LocalStatus.AWAITING_RECONCILIATION, null);
                                }
                            }
                        }
                        """)
                .solutionAnnotation(
                        "catch (Exception ambiguousFailure) { return new Outcome(LocalStatus.AWAITING_RECONCILIATION, null); }",
                        "A exceção descreve o canal de resposta, não o estado financeiro no PSP. Aguardar " +
                                "consulta/reconciliação evita retry cego.")
                .solutionAnnotation(
                        "if (result.providerReference() == null || result.providerReference().isBlank())",
                        "Uma confirmação sem identidade externa não é reconciliável e portanto não é prova " +
                                "suficiente de liquidação.")
                .equalsCase("confirmação válida preserva referência do PSP",
                        "PaymentExecution service = new PaymentExecution(); " +
                                "var command = new PaymentExecution.Command(new java.math.BigDecimal(\"10.00\"), \"key-1\"); " +
                                "PaymentExecution.PaymentGateway gateway = ignored -> new PaymentExecution.GatewayResult(" +
                                "PaymentExecution.GatewayStatus.CONFIRMED, \"psp-123\");",
                        "service.execute(command, gateway)",
                        "new PaymentExecution.Outcome(PaymentExecution.LocalStatus.CONFIRMED, \"psp-123\")", true)
                .equalsCase("timeout permanece aguardando reconciliação",
                        "PaymentExecution service = new PaymentExecution(); " +
                                "var command = new PaymentExecution.Command(new java.math.BigDecimal(\"10.00\"), \"key-1\"); " +
                                "PaymentExecution.PaymentGateway gateway = ignored -> { throw new java.io.IOException(\"timeout\"); };",
                        "service.execute(command, gateway).status()",
                        "PaymentExecution.LocalStatus.AWAITING_RECONCILIATION", true)
                .equalsCase("confirmação sem referência é ambígua",
                        "PaymentExecution service = new PaymentExecution(); " +
                                "var command = new PaymentExecution.Command(new java.math.BigDecimal(\"10.00\"), \"key-1\"); " +
                                "PaymentExecution.PaymentGateway gateway = ignored -> new PaymentExecution.GatewayResult(" +
                                "PaymentExecution.GatewayStatus.CONFIRMED, null);",
                        "service.execute(command, gateway).status()",
                        "PaymentExecution.LocalStatus.AWAITING_RECONCILIATION", false)
                .equalsCase("rejeição explícita do PSP vira falha local",
                        "PaymentExecution service = new PaymentExecution(); " +
                                "var command = new PaymentExecution.Command(new java.math.BigDecimal(\"10.00\"), \"key-1\"); " +
                                "PaymentExecution.PaymentGateway gateway = ignored -> new PaymentExecution.GatewayResult(" +
                                "PaymentExecution.GatewayStatus.FAILED, \"psp-456\");",
                        "service.execute(command, gateway).status()",
                        "PaymentExecution.LocalStatus.FAILED", true)
                .throwsCase("comando sem idempotency key é rejeitado antes do gateway",
                        "PaymentExecution service = new PaymentExecution(); " +
                                "var command = new PaymentExecution.Command(new java.math.BigDecimal(\"10.00\"), \"\"); " +
                                "PaymentExecution.PaymentGateway gateway = ignored -> null;",
                        "service.execute(command, gateway)", "IllegalArgumentException", false)
                .hint("Valide o comando antes de chamar o port externo.")
                .hint("PENDING e qualquer exceção convergem para AWAITING_RECONCILIATION.")
                .hint("Somente CONFIRMED com providerReference pode confirmar localmente.")
                .build();
    }

    private Exercise buildAtomicJournalSequence(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-09-atomic-journal-sequence",
                        module,
                        "Números atômicos: sequência concorrente",
                        """
                        ## Contexto real

                        Um journal local pode precisar atribuir números monotônicos quando muitas threads
                        publicam ao mesmo tempo. `long++` é uma operação composta e pode perder incrementos.
                        `AtomicLong` oferece leitura, atualização e compare-and-set atômicos dentro de uma JVM.

                        ## Objetivo

                        Implemente `public final class AtomicJournalSequence`:

                        - O construtor aceita um valor inicial não negativo.
                        - `next()` devolve o próximo número sem duplicação.
                        - `reserve(int count)` reserva uma faixa contígua de forma atômica.
                        - `current()` devolve o último número reservado.
                        - Quantidade inválida e overflow devem falhar sem alterar a sequência.

                        ## Limite da garantia

                        `AtomicLong` protege apenas memória compartilhada no mesmo processo. Ele não cria uma
                        sequência durável entre réplicas, não substitui constraint no banco e não deve ser usado
                        como justificativa para representar dinheiro sem moeda, escala e regra de arredondamento.
                        """,
                        """
                        public final class AtomicJournalSequence {
                            public record Range(long first, long last) {}
                            public AtomicJournalSequence(long initialValue) {}
                            public long next() { return 0L; }
                            public Range reserve(int count) { return null; }
                            public long current() { return 0L; }
                        }
                        """,
                        """
                        public final class AtomicJournalSequence {
                            public record Range(long first, long last) {}
                            private final java.util.concurrent.atomic.AtomicLong sequence;

                            public AtomicJournalSequence(long initialValue) {
                                // TODO: reject negative values and initialize the atomic sequence.
                                this.sequence = null;
                            }

                            public long next() { return 0L; }
                            public Range reserve(int count) { return null; }
                            public long current() { return 0L; }
                        }
                        """,
                        "AVANÇADO", 8, 30)
                .referenceSolution("""
                        public final class AtomicJournalSequence {
                            public record Range(long first, long last) {}
                            private final java.util.concurrent.atomic.AtomicLong sequence;

                            public AtomicJournalSequence(long initialValue) {
                                if (initialValue < 0) {
                                    throw new IllegalArgumentException("Initial value cannot be negative");
                                }
                                this.sequence = new java.util.concurrent.atomic.AtomicLong(initialValue);
                            }

                            public long next() {
                                return reserve(1).first();
                            }

                            public Range reserve(int count) {
                                if (count <= 0) {
                                    throw new IllegalArgumentException("Count must be positive");
                                }
                                while (true) {
                                    long before = sequence.get();
                                    long after = Math.addExact(before, count);
                                    if (sequence.compareAndSet(before, after)) {
                                        return new Range(Math.addExact(before, 1L), after);
                                    }
                                }
                            }

                            public long current() {
                                return sequence.get();
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (sequence.compareAndSet(before, after))",
                        "O CAS publica a faixa somente se nenhuma outra thread alterou a sequência desde a leitura.")
                .solutionAnnotation(
                        "long after = Math.addExact(before, count);",
                        "O overflow é detectado antes do CAS; portanto a falha não corrompe o contador.")
                .equalsCase("duzentas threads recebem números únicos e contínuos",
                        "AtomicJournalSequence sequence = new AtomicJournalSequence(0); " +
                                "java.util.Set<Long> values = java.util.concurrent.ConcurrentHashMap.newKeySet(); " +
                                "java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8); " +
                                "java.util.List<java.util.concurrent.Future<?>> futures = new java.util.ArrayList<>(); " +
                                "for (int i = 0; i < 200; i++) futures.add(pool.submit(() -> values.add(sequence.next()))); " +
                                "for (var future : futures) future.get(); pool.shutdown();",
                        "java.util.List.of(values.size(), java.util.Collections.min(values), " +
                                "java.util.Collections.max(values), sequence.current())",
                        "java.util.List.of(200, 1L, 200L, 200L)", true)
                .equalsCase("reserva devolve faixa contígua",
                        "AtomicJournalSequence sequence = new AtomicJournalSequence(10);",
                        "java.util.List.of(sequence.reserve(3), sequence.current())",
                        "java.util.List.of(new AtomicJournalSequence.Range(11L, 13L), 13L)", true)
                .equalsCase("sequência continua a partir do valor inicial",
                        "AtomicJournalSequence sequence = new AtomicJournalSequence(41);",
                        "sequence.next()", "42L", false)
                .throwsCase("quantidade zero é rejeitada",
                        "AtomicJournalSequence sequence = new AtomicJournalSequence(0);",
                        "sequence.reserve(0)", "IllegalArgumentException", true)
                .throwsCase("overflow falha sem publicar valor inválido",
                        "AtomicJournalSequence sequence = new AtomicJournalSequence(Long.MAX_VALUE);",
                        "sequence.next()", "ArithmeticException", false)
                .hint("Leia o valor atual, calcule o próximo com Math.addExact e publique com compareAndSet.")
                .hint("Quando o CAS falhar, repita porque outra thread venceu a corrida.")
                .hint("AtomicLong é local à JVM; persistência e múltiplas réplicas exigem outro contrato.")
                .build();
    }

    private Exercise buildCasBalance(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-10-cas-versioned-balance",
                        module,
                        "CAS: saldo imutável e versionado",
                        """
                        ## Contexto real

                        `AtomicReference` permite trocar um estado imutável somente quando ele ainda é o estado
                        observado. A versão faz parte do contrato: mesmo que o saldo volte ao mesmo valor, o
                        histórico de mudanças não volta ao estado anterior e o problema ABA fica visível.

                        ## Objetivo

                        Implemente `public final class CasBalance` com um `AtomicReference<State>`:

                        - `State` contém `BigDecimal balance` e `long version`.
                        - `tryDebit` decide e subtrai por CAS, sem permitir saldo negativo.
                        - `credit` soma por CAS.
                        - Cada mutação aceita incrementa a versão exatamente uma vez.
                        - Débito recusado não muda saldo nem versão.

                        ## Limite da garantia

                        CAS resolve concorrência sobre esse objeto dentro de uma JVM. Ele não torna atômicas duas
                        contas, uma gravação no banco e uma publicação em fila, nem substitui ledger e idempotência.
                        """,
                        """
                        public final class CasBalance {
                            public record State(java.math.BigDecimal balance, long version) {}
                            public CasBalance(String openingBalance) {}
                            public boolean tryDebit(String rawAmount) { return false; }
                            public State credit(String rawAmount) { return null; }
                            public State snapshot() { return null; }
                        }
                        """,
                        """
                        public final class CasBalance {
                            public record State(java.math.BigDecimal balance, long version) {}
                            private final java.util.concurrent.atomic.AtomicReference<State> state;

                            public CasBalance(String openingBalance) {
                                // TODO: validate and initialize version zero.
                                this.state = null;
                            }

                            public boolean tryDebit(String rawAmount) { return false; }
                            public State credit(String rawAmount) { return null; }
                            public State snapshot() { return null; }
                        }
                        """,
                        "AVANÇADO", 9, 35)
                .referenceSolution("""
                        public final class CasBalance {
                            public record State(java.math.BigDecimal balance, long version) {}
                            private final java.util.concurrent.atomic.AtomicReference<State> state;

                            public CasBalance(String openingBalance) {
                                java.math.BigDecimal opening = money(openingBalance);
                                if (opening.signum() < 0) {
                                    throw new IllegalArgumentException("Opening balance cannot be negative");
                                }
                                this.state = new java.util.concurrent.atomic.AtomicReference<>(new State(opening, 0L));
                            }

                            public boolean tryDebit(String rawAmount) {
                                java.math.BigDecimal amount = positive(rawAmount);
                                while (true) {
                                    State before = state.get();
                                    if (before.balance().compareTo(amount) < 0) {
                                        return false;
                                    }
                                    State after = new State(before.balance().subtract(amount), before.version() + 1L);
                                    if (state.compareAndSet(before, after)) {
                                        return true;
                                    }
                                }
                            }

                            public State credit(String rawAmount) {
                                java.math.BigDecimal amount = positive(rawAmount);
                                while (true) {
                                    State before = state.get();
                                    State after = new State(before.balance().add(amount), before.version() + 1L);
                                    if (state.compareAndSet(before, after)) {
                                        return after;
                                    }
                                }
                            }

                            public State snapshot() {
                                return state.get();
                            }

                            private static java.math.BigDecimal positive(String raw) {
                                java.math.BigDecimal amount = money(raw);
                                if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                return amount;
                            }

                            private static java.math.BigDecimal money(String raw) {
                                return new java.math.BigDecimal(raw).setScale(2, java.math.RoundingMode.UNNECESSARY);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "State after = new State(before.balance().subtract(amount), before.version() + 1L);",
                        "Saldo e versão formam um único snapshot imutável; não existe publicação parcial.")
                .solutionAnnotation(
                        "if (state.compareAndSet(before, after))",
                        "A mutação só vence quando o snapshot lido continua atual; caso contrário a regra é reavaliada.")
                .equalsCase("dois débitos concorrentes não aprovam overdraft",
                        "CasBalance balance = new CasBalance(\"100.00\"); " +
                                "java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(2); " +
                                "java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1); " +
                                "var first = pool.submit(() -> { start.await(); return balance.tryDebit(\"80.00\"); }); " +
                                "var second = pool.submit(() -> { start.await(); return balance.tryDebit(\"80.00\"); }); " +
                                "start.countDown(); boolean exactlyOne = first.get() ^ second.get(); pool.shutdown();",
                        "java.util.List.of(exactlyOne, balance.snapshot())",
                        "java.util.List.of(true, new CasBalance.State(new java.math.BigDecimal(\"20.00\"), 1L))", true)
                .equalsCase("débitos concorrentes incrementam uma versão por mutação",
                        "CasBalance balance = new CasBalance(\"50.00\"); " +
                                "java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8); " +
                                "java.util.List<java.util.concurrent.Future<Boolean>> futures = new java.util.ArrayList<>(); " +
                                "for (int i = 0; i < 50; i++) futures.add(pool.submit(() -> balance.tryDebit(\"1.00\"))); " +
                                "for (var future : futures) future.get(); pool.shutdown();",
                        "balance.snapshot()",
                        "new CasBalance.State(new java.math.BigDecimal(\"0.00\"), 50L)", false)
                .equalsCase("débito recusado preserva a versão",
                        "CasBalance balance = new CasBalance(\"10.00\"); boolean accepted = balance.tryDebit(\"10.01\");",
                        "java.util.List.of(accepted, balance.snapshot())",
                        "java.util.List.of(false, new CasBalance.State(new java.math.BigDecimal(\"10.00\"), 0L))", true)
                .equalsCase("saldo igual não apaga o histórico de mudanças",
                        "CasBalance balance = new CasBalance(\"10.00\"); " +
                                "balance.tryDebit(\"1.00\"); balance.credit(\"1.00\");",
                        "balance.snapshot()",
                        "new CasBalance.State(new java.math.BigDecimal(\"10.00\"), 2L)", false)
                .throwsCase("crédito negativo é rejeitado",
                        "CasBalance balance = new CasBalance(\"10.00\");",
                        "balance.credit(\"-1.00\")", "IllegalArgumentException", true)
                .hint("O estado comparado pelo CAS deve ser o mesmo objeto retornado por state.get().")
                .hint("Crie um novo State para cada mutação; não torne o snapshot mutável.")
                .hint("A versão diferencia estados com o mesmo saldo após débito e crédito.")
                .build();
    }

    private Exercise buildAtomicTransfer(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-11-atomic-transfer",
                        module,
                        "Atomicidade financeira entre duas contas",
                        """
                        ## Contexto real

                        Uma transferência contém duas mutações inseparáveis: debitar a origem e creditar o
                        destino. Proteger cada conta isoladamente ainda permite deadlock ou estado parcial. Os
                        dois locks devem ser adquiridos em ordem determinística e mantidos até o fim da decisão.

                        ## Objetivo

                        Implemente `public final class AtomicTransferService`:

                        - `Account` possui identidade única, saldo decimal e lock próprio.
                        - `transfer` valida valor positivo e contas distintas.
                        - Os locks são adquiridos pela ordem lexicográfica do identificador.
                        - Saldo insuficiente devolve `false` sem alterar nenhuma conta.
                        - Sucesso debita e credita sob a mesma seção crítica.

                        ## Limite da garantia

                        O exercício prova atomicidade em memória. Em produção, contas persistidas exigem uma
                        transação de banco com isolamento e locks adequados; serviços distintos exigem journal,
                        idempotência, outbox e saga/compensação. `AtomicLong` ou CAS isolado não resolve isso.
                        """,
                        """
                        public final class AtomicTransferService {
                            public static final class Account {
                                public Account(String id, String openingBalance) {}
                                public String id() { return null; }
                                public java.math.BigDecimal balance() { return null; }
                            }
                            public boolean transfer(Account from, Account to, String rawAmount) { return false; }
                        }
                        """,
                        """
                        public final class AtomicTransferService {
                            public static final class Account {
                                private final String id;
                                private final java.util.concurrent.locks.ReentrantLock lock =
                                        new java.util.concurrent.locks.ReentrantLock();
                                private java.math.BigDecimal balance;

                                public Account(String id, String openingBalance) {
                                    // TODO: validate identity and opening balance.
                                    this.id = null;
                                }

                                public String id() { return id; }
                                public java.math.BigDecimal balance() { return null; }
                            }

                            public boolean transfer(Account from, Account to, String rawAmount) { return false; }
                        }
                        """,
                        "ESPECIALISTA", 10, 45)
                .referenceSolution("""
                        public final class AtomicTransferService {
                            public static final class Account {
                                private final String id;
                                private final java.util.concurrent.locks.ReentrantLock lock =
                                        new java.util.concurrent.locks.ReentrantLock(true);
                                private java.math.BigDecimal balance;

                                public Account(String id, String openingBalance) {
                                    if (id == null || id.isBlank()) throw new IllegalArgumentException("Account id is required");
                                    this.id = id;
                                    this.balance = money(openingBalance);
                                    if (balance.signum() < 0) throw new IllegalArgumentException("Opening balance cannot be negative");
                                }

                                public String id() {
                                    return id;
                                }

                                public java.math.BigDecimal balance() {
                                    lock.lock();
                                    try {
                                        return balance;
                                    } finally {
                                        lock.unlock();
                                    }
                                }
                            }

                            public boolean transfer(Account from, Account to, String rawAmount) {
                                java.util.Objects.requireNonNull(from, "from");
                                java.util.Objects.requireNonNull(to, "to");
                                java.math.BigDecimal amount = positive(rawAmount);
                                if (from == to || from.id.equals(to.id)) {
                                    throw new IllegalArgumentException("Accounts must be distinct");
                                }
                                Account first = from.id.compareTo(to.id) < 0 ? from : to;
                                Account second = first == from ? to : from;
                                first.lock.lock();
                                second.lock.lock();
                                try {
                                    if (from.balance.compareTo(amount) < 0) {
                                        return false;
                                    }
                                    from.balance = from.balance.subtract(amount);
                                    to.balance = to.balance.add(amount);
                                    return true;
                                } finally {
                                    second.lock.unlock();
                                    first.lock.unlock();
                                }
                            }

                            private static java.math.BigDecimal positive(String raw) {
                                java.math.BigDecimal amount = money(raw);
                                if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                return amount;
                            }

                            private static java.math.BigDecimal money(String raw) {
                                return new java.math.BigDecimal(raw).setScale(2, java.math.RoundingMode.UNNECESSARY);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "Account first = from.id.compareTo(to.id) < 0 ? from : to;",
                        "Uma ordem global de aquisição remove o ciclo de espera que produz deadlock.")
                .solutionAnnotation(
                        "from.balance = from.balance.subtract(amount);\n            to.balance = to.balance.add(amount);",
                        "Débito e crédito acontecem enquanto os dois locks estão retidos; nenhuma leitura externa observa metade da transferência.")
                .equalsCase("transferência preserva o total",
                        "AtomicTransferService service = new AtomicTransferService(); " +
                                "var from = new AtomicTransferService.Account(\"A\", \"100.00\"); " +
                                "var to = new AtomicTransferService.Account(\"B\", \"0.00\"); " +
                                "boolean accepted = service.transfer(from, to, \"40.00\");",
                        "java.util.List.of(accepted, from.balance(), to.balance(), from.balance().add(to.balance()))",
                        "java.util.List.of(true, new java.math.BigDecimal(\"60.00\"), " +
                                "new java.math.BigDecimal(\"40.00\"), new java.math.BigDecimal(\"100.00\"))", true)
                .equalsCase("saldo insuficiente não produz mutação parcial",
                        "AtomicTransferService service = new AtomicTransferService(); " +
                                "var from = new AtomicTransferService.Account(\"A\", \"10.00\"); " +
                                "var to = new AtomicTransferService.Account(\"B\", \"5.00\"); " +
                                "boolean accepted = service.transfer(from, to, \"10.01\");",
                        "java.util.List.of(accepted, from.balance(), to.balance())",
                        "java.util.List.of(false, new java.math.BigDecimal(\"10.00\"), new java.math.BigDecimal(\"5.00\"))", true)
                .equalsCase("transferências opostas não causam deadlock nem criam dinheiro",
                        "AtomicTransferService service = new AtomicTransferService(); " +
                                "var a = new AtomicTransferService.Account(\"A\", \"100.00\"); " +
                                "var b = new AtomicTransferService.Account(\"B\", \"100.00\"); " +
                                "java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(8); " +
                                "java.util.List<java.util.concurrent.Future<Boolean>> futures = new java.util.ArrayList<>(); " +
                                "for (int i = 0; i < 50; i++) { futures.add(pool.submit(() -> service.transfer(a, b, \"1.00\"))); " +
                                "futures.add(pool.submit(() -> service.transfer(b, a, \"1.00\"))); } " +
                                "boolean allAccepted = true; for (var future : futures) allAccepted &= future.get(); pool.shutdown();",
                        "java.util.List.of(allAccepted, a.balance(), b.balance(), a.balance().add(b.balance()))",
                        "java.util.List.of(true, new java.math.BigDecimal(\"100.00\"), " +
                                "new java.math.BigDecimal(\"100.00\"), new java.math.BigDecimal(\"200.00\"))", false)
                .throwsCase("mesma conta não aceita transferência",
                        "AtomicTransferService service = new AtomicTransferService(); " +
                                "var account = new AtomicTransferService.Account(\"A\", \"10.00\");",
                        "service.transfer(account, account, \"1.00\")", "IllegalArgumentException", true)
                .throwsCase("identidades duplicadas são rejeitadas",
                        "AtomicTransferService service = new AtomicTransferService(); " +
                                "var first = new AtomicTransferService.Account(\"A\", \"10.00\"); " +
                                "var second = new AtomicTransferService.Account(\"A\", \"10.00\");",
                        "service.transfer(first, second, \"1.00\")", "IllegalArgumentException", false)
                .hint("Adquira os dois locks sempre na mesma ordem, independentemente da direção da transferência.")
                .hint("Verifique saldo e aplique as duas mutações antes de liberar qualquer lock.")
                .hint("O exemplo é local: no banco, a atomicidade pertence à transação persistente.")
                .build();
    }
}
