package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.seed.support.ExerciseBuilder;

import java.util.List;

/**
 * Executable phase 2 of the real-bank curriculum. These exercises model the bank's core aggregates
 * before persistence and frameworks are introduced, keeping privacy, lifecycle, currency, limits,
 * fees and plan decisions explicit and independently testable.
 */
final class BankModelingPhaseExercises {

    private BankModelingPhaseExercises() {
    }

    static List<Exercise> build(LearningModule module) {
        return List.of(
                buildCustomerProfile(module),
                buildBankAccountLifecycle(module),
                buildMultiCurrencyWallet(module),
                buildPaymentLimitPolicy(module),
                buildFeePolicy(module));
    }

    private static Exercise buildCustomerProfile(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-12-customer-profile",
                        module,
                        "Cliente: identidade sem vazar PII",
                        """
                        ## Contexto real

                        O cadastro de um cliente combina identidade interna com dados pessoais. CPF,
                        CNPJ, nome e e-mail não podem aparecer integralmente em logs, auditoria ou
                        respostas de suporte. O domínio deve nascer com uma representação segura.

                        ## Objetivo

                        Implemente `public final class CustomerProfile`:

                        - `register` valida identificador, nome, documento e e-mail.
                        - O documento aceita 11 ou 14 dígitos depois da normalização.
                        - O e-mail é normalizado para minúsculas.
                        - `maskedDocument` revela somente os quatro últimos dígitos.
                        - `safeLabel` e `toString` nunca expõem nome, documento completo ou e-mail.

                        ## Invariante

                        Dados pessoais completos ficam encapsulados. Toda representação destinada a
                        log ou suporte é minimizada por construção, não por disciplina do chamador.
                        """,
                        """
                        public final class CustomerProfile {
                            public static CustomerProfile register(
                                    String customerId, String fullName, String document, String email) { return null; }
                            public String customerId() { return null; }
                            public String normalizedEmail() { return null; }
                            public String maskedDocument() { return null; }
                            public String safeLabel() { return null; }
                        }
                        """,
                        """
                        public final class CustomerProfile {
                            private final String customerId;
                            private final String fullName;
                            private final String documentDigits;
                            private final String normalizedEmail;

                            private CustomerProfile(String customerId, String fullName,
                                                    String documentDigits, String normalizedEmail) {
                                this.customerId = customerId;
                                this.fullName = fullName;
                                this.documentDigits = documentDigits;
                                this.normalizedEmail = normalizedEmail;
                            }

                            public static CustomerProfile register(
                                    String customerId, String fullName, String document, String email) {
                                return null;
                            }

                            public String customerId() { return null; }
                            public String normalizedEmail() { return null; }
                            public String maskedDocument() { return null; }
                            public String safeLabel() { return null; }
                        }
                        """,
                        "INTERMEDIÁRIO", 11, 25)
                .referenceSolution("""
                        public final class CustomerProfile {
                            private final String customerId;
                            private final String fullName;
                            private final String documentDigits;
                            private final String normalizedEmail;

                            private CustomerProfile(String customerId, String fullName,
                                                    String documentDigits, String normalizedEmail) {
                                this.customerId = customerId;
                                this.fullName = fullName;
                                this.documentDigits = documentDigits;
                                this.normalizedEmail = normalizedEmail;
                            }

                            public static CustomerProfile register(
                                    String customerId, String fullName, String document, String email) {
                                String id = required(customerId, "customerId");
                                String name = required(fullName, "fullName");
                                if (name.length() < 3) {
                                    throw new IllegalArgumentException("Full name is invalid");
                                }
                                String digits = required(document, "document").replaceAll("\\\\D", "");
                                if (digits.length() != 11 && digits.length() != 14) {
                                    throw new IllegalArgumentException("Document must have 11 or 14 digits");
                                }
                                String normalized = required(email, "email")
                                        .toLowerCase(java.util.Locale.ROOT);
                                int at = normalized.indexOf('@');
                                if (at <= 0 || normalized.indexOf('.', at) < at + 2) {
                                    throw new IllegalArgumentException("Email is invalid");
                                }
                                return new CustomerProfile(id, name, digits, normalized);
                            }

                            private static String required(String value, String field) {
                                if (value == null || value.isBlank()) {
                                    throw new IllegalArgumentException(field + " is required");
                                }
                                return value.trim();
                            }

                            public String customerId() {
                                return customerId;
                            }

                            public String normalizedEmail() {
                                return normalizedEmail;
                            }

                            public String maskedDocument() {
                                return "*".repeat(documentDigits.length() - 4)
                                        + documentDigits.substring(documentDigits.length() - 4);
                            }

                            public String safeLabel() {
                                return "CustomerProfile{id='" + customerId
                                        + "', document='" + maskedDocument() + "'}";
                            }

                            @Override
                            public String toString() {
                                return safeLabel();
                            }
                        }
                        """)
                .solutionAnnotation(
                        "required(document, \"document\").replaceAll(\"\\\\D\", \"\")",
                        "A normalização acontece na fronteira; o restante do domínio trabalha somente com dígitos validados.")
                .solutionAnnotation(
                        "return safeLabel();",
                        "A representação padrão também é segura, reduzindo vazamento acidental em logs e exceções.")
                .equalsCase("normaliza e-mail e mascara CPF",
                        "CustomerProfile profile = CustomerProfile.register(\"CUS-1\", \"Ana Silva\", \"123.456.789-01\", \" ANA@EXAMPLE.COM \" );",
                        "java.util.List.of(profile.customerId(), profile.normalizedEmail(), profile.maskedDocument())",
                        "java.util.List.of(\"CUS-1\", \"ana@example.com\", \"*******8901\")", true)
                .equalsCase("rótulo seguro não expõe PII completa",
                        "CustomerProfile profile = CustomerProfile.register(\"CUS-2\", \"Carlos Souza\", \"11222333000181\", \"carlos@example.com\"); String label = profile.safeLabel();",
                        "!label.contains(\"Carlos\") && !label.contains(\"11222333000181\") && !label.contains(\"carlos@example.com\")",
                        "true", true)
                .equalsCase("toString herda a representação segura",
                        "CustomerProfile profile = CustomerProfile.register(\"CUS-3\", \"Maria Lima\", \"98765432100\", \"maria@example.com\");",
                        "profile.toString().equals(profile.safeLabel()) && profile.toString().endsWith(\"2100'}\")",
                        "true", false)
                .throwsCase("documento inválido é rejeitado",
                        "", "CustomerProfile.register(\"CUS-4\", \"Nome Válido\", \"123\", \"n@example.com\")",
                        "IllegalArgumentException", true)
                .throwsCase("nome insuficiente é rejeitado",
                        "", "CustomerProfile.register(\"CUS-5\", \"A\", \"12345678901\", \"a@example.com\")",
                        "IllegalArgumentException", false)
                .hint("Normalize documento e e-mail apenas depois de validar valores nulos ou vazios.")
                .hint("Não forneça getter público para o documento completo ou para o nome quando o contrato não exige isso.")
                .hint("Faça toString delegar para a mesma representação segura usada em logs.")
                .build();
    }

    private static Exercise buildBankAccountLifecycle(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-13-account-lifecycle",
                        module,
                        "Conta: agregado e ciclo de vida",
                        """
                        ## Contexto real

                        Uma conta não é apenas um saldo. Ela possui proprietário, identidade, estado
                        e versão. Bloqueio, encerramento e movimentação precisam obedecer a uma máquina
                        de estados para impedir operações depois do encerramento ou débitos indevidos.

                        ## Objetivo

                        Implemente `public final class BankAccount` com `Status ACTIVE`, `BLOCKED` e
                        `CLOSED`, além de `Snapshot` imutável:

                        - `open` cria a conta com valor decimal não negativo.
                        - `credit` aceita crédito em conta ativa ou bloqueada.
                        - `tryDebit` exige conta ativa e preserva estado se não houver saldo.
                        - `block` e `unblock` respeitam transições válidas.
                        - `close` exige saldo zero.
                        - Toda mutação aceita incrementa a versão exatamente uma vez.

                        ## Invariante

                        Estado, saldo e versão formam um único agregado. Operações recusadas nunca
                        deixam mutação parcial e conta encerrada nunca volta a movimentar.
                        """,
                        """
                        public final class BankAccount {
                            public enum Status { ACTIVE, BLOCKED, CLOSED }
                            public record Snapshot(java.math.BigDecimal balance, Status status, long version) {}
                            public static BankAccount open(String accountId, String ownerId, String openingBalance) { return null; }
                            public void credit(String amount) {}
                            public boolean tryDebit(String amount) { return false; }
                            public void block() {}
                            public void unblock() {}
                            public void close() {}
                            public Snapshot snapshot() { return null; }
                        }
                        """,
                        """
                        public final class BankAccount {
                            public enum Status { ACTIVE, BLOCKED, CLOSED }
                            public record Snapshot(java.math.BigDecimal balance, Status status, long version) {}
                            private final String accountId;
                            private final String ownerId;
                            private java.math.BigDecimal balance;
                            private Status status;
                            private long version;

                            private BankAccount(String accountId, String ownerId, java.math.BigDecimal balance) {
                                this.accountId = accountId;
                                this.ownerId = ownerId;
                                this.balance = balance;
                                this.status = Status.ACTIVE;
                            }

                            public static BankAccount open(String accountId, String ownerId, String openingBalance) { return null; }
                            public void credit(String amount) {}
                            public boolean tryDebit(String amount) { return false; }
                            public void block() {}
                            public void unblock() {}
                            public void close() {}
                            public Snapshot snapshot() { return null; }
                        }
                        """,
                        "INTERMEDIÁRIO", 12, 30)
                .referenceSolution("""
                        public final class BankAccount {
                            public enum Status { ACTIVE, BLOCKED, CLOSED }
                            public record Snapshot(java.math.BigDecimal balance, Status status, long version) {}

                            private final String accountId;
                            private final String ownerId;
                            private java.math.BigDecimal balance;
                            private Status status;
                            private long version;

                            private BankAccount(String accountId, String ownerId, java.math.BigDecimal balance) {
                                this.accountId = accountId;
                                this.ownerId = ownerId;
                                this.balance = balance;
                                this.status = Status.ACTIVE;
                            }

                            public static BankAccount open(String accountId, String ownerId, String openingBalance) {
                                requireId(accountId, "accountId");
                                requireId(ownerId, "ownerId");
                                return new BankAccount(accountId.trim(), ownerId.trim(), nonNegative(openingBalance));
                            }

                            public void credit(String rawAmount) {
                                ensureNotClosed();
                                balance = balance.add(positive(rawAmount));
                                version++;
                            }

                            public boolean tryDebit(String rawAmount) {
                                if (status != Status.ACTIVE) {
                                    throw new IllegalStateException("Account is not active");
                                }
                                java.math.BigDecimal amount = positive(rawAmount);
                                if (balance.compareTo(amount) < 0) {
                                    return false;
                                }
                                balance = balance.subtract(amount);
                                version++;
                                return true;
                            }

                            public void block() {
                                if (status != Status.ACTIVE) throw new IllegalStateException("Only active account can be blocked");
                                status = Status.BLOCKED;
                                version++;
                            }

                            public void unblock() {
                                if (status != Status.BLOCKED) throw new IllegalStateException("Only blocked account can be activated");
                                status = Status.ACTIVE;
                                version++;
                            }

                            public void close() {
                                ensureNotClosed();
                                if (balance.signum() != 0) throw new IllegalStateException("Account balance must be zero");
                                status = Status.CLOSED;
                                version++;
                            }

                            public Snapshot snapshot() {
                                return new Snapshot(balance, status, version);
                            }

                            private void ensureNotClosed() {
                                if (status == Status.CLOSED) throw new IllegalStateException("Account is closed");
                            }

                            private static void requireId(String value, String field) {
                                if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
                            }

                            private static java.math.BigDecimal nonNegative(String raw) {
                                java.math.BigDecimal value = money(raw);
                                if (value.signum() < 0) throw new IllegalArgumentException("Balance cannot be negative");
                                return value;
                            }

                            private static java.math.BigDecimal positive(String raw) {
                                java.math.BigDecimal value = money(raw);
                                if (value.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                return value;
                            }

                            private static java.math.BigDecimal money(String raw) {
                                if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Amount is required");
                                return new java.math.BigDecimal(raw).setScale(2, java.math.RoundingMode.UNNECESSARY);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (balance.compareTo(amount) < 0) { return false; }",
                        "A recusa por saldo insuficiente ocorre antes de qualquer mutação e não avança a versão.")
                .solutionAnnotation(
                        "if (balance.signum() != 0) throw new IllegalStateException(\"Account balance must be zero\");",
                        "Encerrar uma conta com saldo quebraria a responsabilidade sobre o dinheiro ainda custodiado.")
                .equalsCase("débito aceito altera saldo e versão",
                        "BankAccount account = BankAccount.open(\"ACC-1\", \"CUS-1\", \"100.00\"); boolean accepted = account.tryDebit(\"40.00\");",
                        "java.util.List.of(accepted, account.snapshot())",
                        "java.util.List.of(true, new BankAccount.Snapshot(new java.math.BigDecimal(\"60.00\"), BankAccount.Status.ACTIVE, 1L))", true)
                .equalsCase("saldo insuficiente não altera o agregado",
                        "BankAccount account = BankAccount.open(\"ACC-2\", \"CUS-2\", \"10.00\"); boolean accepted = account.tryDebit(\"10.01\");",
                        "java.util.List.of(accepted, account.snapshot())",
                        "java.util.List.of(false, new BankAccount.Snapshot(new java.math.BigDecimal(\"10.00\"), BankAccount.Status.ACTIVE, 0L))", true)
                .equalsCase("conta bloqueada recebe crédito mas não débito",
                        "BankAccount account = BankAccount.open(\"ACC-3\", \"CUS-3\", \"0.00\"); account.block(); account.credit(\"5.00\");",
                        "account.snapshot()",
                        "new BankAccount.Snapshot(new java.math.BigDecimal(\"5.00\"), BankAccount.Status.BLOCKED, 2L)", false)
                .throwsCase("conta com saldo não pode ser encerrada",
                        "BankAccount account = BankAccount.open(\"ACC-4\", \"CUS-4\", \"1.00\");",
                        "account.close()", "IllegalStateException", true)
                .equalsCase("encerramento com saldo zero é terminal",
                        "BankAccount account = BankAccount.open(\"ACC-5\", \"CUS-5\", \"0.00\"); account.close();",
                        "account.snapshot()",
                        "new BankAccount.Snapshot(new java.math.BigDecimal(\"0.00\"), BankAccount.Status.CLOSED, 1L)", true)
                .throwsCase("conta encerrada rejeita novo crédito",
                        "BankAccount account = BankAccount.open(\"ACC-6\", \"CUS-6\", \"0.00\"); account.close();",
                        "account.credit(\"1.00\")", "IllegalStateException", false)
                .hint("Modele as transições como regras explícitas; não use um booleano genérico para todos os estados.")
                .hint("Incremente a versão somente depois de uma mutação aceita.")
                .hint("Crédito em conta bloqueada pode ser permitido, mas conta encerrada é sempre terminal.")
                .build();
    }

    private static Exercise buildMultiCurrencyWallet(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-14-multi-currency-wallet",
                        module,
                        "Carteira: moedas nunca se misturam",
                        """
                        ## Contexto real

                        Uma carteira pode manter saldos em moedas diferentes, mas BRL, USD e EUR não
                        são somáveis sem uma operação cambial explícita. Tratar todos os valores como
                        um único saldo cria dinheiro ou apaga exposição cambial.

                        ## Objetivo

                        Implemente `public final class MultiCurrencyWallet`:

                        - Códigos de moeda são normalizados para três letras maiúsculas.
                        - `credit` e `tryDebit` alteram somente a moeda informada.
                        - Valores usam duas casas decimais sem arredondamento implícito.
                        - Débito insuficiente retorna `false` sem mutar o saldo.
                        - Não existe conversão automática entre moedas.

                        ## Invariante

                        Cada moeda possui saldo e identidade próprios. Qualquer conversão futura deve
                        produzir uma operação explícita com taxa, origem, destino e trilha de auditoria.
                        """,
                        """
                        public final class MultiCurrencyWallet {
                            public void credit(String currency, String amount) {}
                            public boolean tryDebit(String currency, String amount) { return false; }
                            public java.math.BigDecimal balance(String currency) { return null; }
                            public int currencyCount() { return 0; }
                        }
                        """,
                        """
                        public final class MultiCurrencyWallet {
                            private final java.util.Map<String, java.math.BigDecimal> balances = new java.util.HashMap<>();
                            public void credit(String currency, String amount) {}
                            public boolean tryDebit(String currency, String amount) { return false; }
                            public java.math.BigDecimal balance(String currency) { return null; }
                            public int currencyCount() { return 0; }
                        }
                        """,
                        "INTERMEDIÁRIO", 13, 25)
                .referenceSolution("""
                        public final class MultiCurrencyWallet {
                            private final java.util.Map<String, java.math.BigDecimal> balances = new java.util.HashMap<>();

                            public void credit(String rawCurrency, String rawAmount) {
                                String currency = currency(rawCurrency);
                                java.math.BigDecimal amount = positive(rawAmount);
                                balances.merge(currency, amount, java.math.BigDecimal::add);
                            }

                            public boolean tryDebit(String rawCurrency, String rawAmount) {
                                String currency = currency(rawCurrency);
                                java.math.BigDecimal amount = positive(rawAmount);
                                java.math.BigDecimal current = balance(currency);
                                if (current.compareTo(amount) < 0) {
                                    return false;
                                }
                                balances.put(currency, current.subtract(amount));
                                return true;
                            }

                            public java.math.BigDecimal balance(String rawCurrency) {
                                return balances.getOrDefault(currency(rawCurrency), new java.math.BigDecimal("0.00"));
                            }

                            public int currencyCount() {
                                return balances.size();
                            }

                            private static String currency(String raw) {
                                if (raw == null) throw new IllegalArgumentException("Currency is required");
                                String normalized = raw.trim().toUpperCase(java.util.Locale.ROOT);
                                if (!normalized.matches("[A-Z]{3}")) {
                                    throw new IllegalArgumentException("Currency must use ISO alpha-3 format");
                                }
                                return normalized;
                            }

                            private static java.math.BigDecimal positive(String raw) {
                                if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Amount is required");
                                java.math.BigDecimal amount = new java.math.BigDecimal(raw)
                                        .setScale(2, java.math.RoundingMode.UNNECESSARY);
                                if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                return amount;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "balances.merge(currency, amount, java.math.BigDecimal::add);",
                        "O mapa mantém uma projeção independente por código de moeda; nenhum saldo global mistura unidades diferentes.")
                .solutionAnnotation(
                        "return false;",
                        "Saldo insuficiente é uma decisão sem efeito colateral, preservando integralmente a carteira.")
                .equalsCase("mantém BRL e USD separados",
                        "MultiCurrencyWallet wallet = new MultiCurrencyWallet(); wallet.credit(\"brl\", \"100.00\"); wallet.credit(\"USD\", \"10.00\");",
                        "java.util.List.of(wallet.balance(\"BRL\"), wallet.balance(\"usd\"), wallet.currencyCount())",
                        "java.util.List.of(new java.math.BigDecimal(\"100.00\"), new java.math.BigDecimal(\"10.00\"), 2)", true)
                .equalsCase("débito altera somente a moeda informada",
                        "MultiCurrencyWallet wallet = new MultiCurrencyWallet(); wallet.credit(\"BRL\", \"50.00\"); wallet.credit(\"USD\", \"20.00\"); boolean accepted = wallet.tryDebit(\"BRL\", \"15.00\");",
                        "java.util.List.of(accepted, wallet.balance(\"BRL\"), wallet.balance(\"USD\"))",
                        "java.util.List.of(true, new java.math.BigDecimal(\"35.00\"), new java.math.BigDecimal(\"20.00\"))", true)
                .equalsCase("saldo insuficiente preserva carteira",
                        "MultiCurrencyWallet wallet = new MultiCurrencyWallet(); wallet.credit(\"EUR\", \"5.00\"); boolean accepted = wallet.tryDebit(\"EUR\", \"5.01\");",
                        "java.util.List.of(accepted, wallet.balance(\"EUR\"))",
                        "java.util.List.of(false, new java.math.BigDecimal(\"5.00\"))", false)
                .throwsCase("moeda inválida é rejeitada",
                        "MultiCurrencyWallet wallet = new MultiCurrencyWallet();",
                        "wallet.credit(\"REAL\", \"1.00\")", "IllegalArgumentException", true)
                .throwsCase("fração além da escala contratada é rejeitada",
                        "MultiCurrencyWallet wallet = new MultiCurrencyWallet();",
                        "wallet.credit(\"BRL\", \"1.001\")", "ArithmeticException", false)
                .hint("Normalize o código da moeda antes de acessar o mapa.")
                .hint("Use BigDecimal com UNNECESSARY para impedir arredondamento silencioso nesta camada.")
                .hint("Uma taxa de câmbio pertence a outro caso de uso; não a esconda dentro de credit ou debit.")
                .build();
    }

    private static Exercise buildPaymentLimitPolicy(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-15-payment-limit-policy",
                        module,
                        "Limites: decisão diária idempotente",
                        """
                        ## Contexto real

                        Limites protegem cliente e banco, mas retries não podem consumir o limite duas
                        vezes. Uma decisão precisa considerar valor por operação, acumulado diário,
                        data efetiva e identidade idempotente da tentativa.

                        ## Objetivo

                        Implemente `public final class PaymentLimitPolicy`:

                        - O construtor recebe limite por operação e limite diário.
                        - `authorize` recebe `operationId`, valor e `LocalDate`.
                        - A mesma operação sempre devolve a primeira decisão sem consumir novamente.
                        - O acumulado reinicia ao avançar o dia.
                        - Datas anteriores à última decisão são rejeitadas como evento fora de ordem.
                        - Decisões recusadas não alteram o total consumido.

                        ## Invariante

                        Uma operação lógica consome limite no máximo uma vez. Retry, duplicidade e
                        evento atrasado não podem ampliar nem reduzir silenciosamente a exposição.
                        """,
                        """
                        public final class PaymentLimitPolicy {
                            public record Decision(boolean approved, String reason, java.math.BigDecimal usedToday) {}
                            public PaymentLimitPolicy(String perTransaction, String daily) {}
                            public Decision authorize(String operationId, String amount, java.time.LocalDate date) { return null; }
                            public java.math.BigDecimal usedToday() { return null; }
                        }
                        """,
                        """
                        public final class PaymentLimitPolicy {
                            public record Decision(boolean approved, String reason, java.math.BigDecimal usedToday) {}
                            private final java.math.BigDecimal perTransaction;
                            private final java.math.BigDecimal daily;
                            private final java.util.Map<String, Decision> decisions = new java.util.HashMap<>();
                            private java.time.LocalDate currentDate;
                            private java.math.BigDecimal used = new java.math.BigDecimal("0.00");

                            public PaymentLimitPolicy(String perTransaction, String daily) {
                                this.perTransaction = null;
                                this.daily = null;
                            }

                            public Decision authorize(String operationId, String amount, java.time.LocalDate date) { return null; }
                            public java.math.BigDecimal usedToday() { return used; }
                        }
                        """,
                        "AVANÇADO", 14, 35)
                .referenceSolution("""
                        public final class PaymentLimitPolicy {
                            public record Decision(boolean approved, String reason, java.math.BigDecimal usedToday) {}

                            private final java.math.BigDecimal perTransaction;
                            private final java.math.BigDecimal daily;
                            private final java.util.Map<String, Decision> decisions = new java.util.HashMap<>();
                            private java.time.LocalDate currentDate;
                            private java.math.BigDecimal used = new java.math.BigDecimal("0.00");

                            public PaymentLimitPolicy(String perTransaction, String daily) {
                                this.perTransaction = positive(perTransaction);
                                this.daily = positive(daily);
                                if (this.perTransaction.compareTo(this.daily) > 0) {
                                    throw new IllegalArgumentException("Per-transaction limit cannot exceed daily limit");
                                }
                            }

                            public Decision authorize(String operationId, String rawAmount, java.time.LocalDate date) {
                                String id = required(operationId);
                                java.util.Objects.requireNonNull(date, "date");
                                Decision previous = decisions.get(id);
                                if (previous != null) {
                                    return previous;
                                }
                                if (currentDate != null && date.isBefore(currentDate)) {
                                    throw new IllegalArgumentException("Out-of-order decision date");
                                }
                                if (currentDate == null || date.isAfter(currentDate)) {
                                    currentDate = date;
                                    used = new java.math.BigDecimal("0.00");
                                }
                                java.math.BigDecimal amount = positive(rawAmount);
                                Decision decision;
                                if (amount.compareTo(perTransaction) > 0) {
                                    decision = new Decision(false, "PER_TRANSACTION_LIMIT", used);
                                } else if (used.add(amount).compareTo(daily) > 0) {
                                    decision = new Decision(false, "DAILY_LIMIT", used);
                                } else {
                                    used = used.add(amount);
                                    decision = new Decision(true, "APPROVED", used);
                                }
                                decisions.put(id, decision);
                                return decision;
                            }

                            public java.math.BigDecimal usedToday() {
                                return used;
                            }

                            private static String required(String value) {
                                if (value == null || value.isBlank()) throw new IllegalArgumentException("Operation id is required");
                                return value.trim();
                            }

                            private static java.math.BigDecimal positive(String raw) {
                                if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Amount is required");
                                java.math.BigDecimal value = new java.math.BigDecimal(raw)
                                        .setScale(2, java.math.RoundingMode.UNNECESSARY);
                                if (value.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                return value;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "Decision previous = decisions.get(id);",
                        "A decisão idempotente é consultada antes de qualquer cálculo ou consumo adicional.")
                .solutionAnnotation(
                        "if (currentDate != null && date.isBefore(currentDate))",
                        "Evento fora de ordem falha fechado; aceitar silenciosamente poderia reabrir um limite antigo.")
                .equalsCase("retry consome limite uma única vez",
                        "PaymentLimitPolicy policy = new PaymentLimitPolicy(\"100.00\", \"200.00\"); var date = java.time.LocalDate.of(2026, 7, 15); var first = policy.authorize(\"OP-1\", \"80.00\", date); var retry = policy.authorize(\"OP-1\", \"99.00\", date);",
                        "java.util.List.of(first, retry, policy.usedToday())",
                        "java.util.List.of(new PaymentLimitPolicy.Decision(true, \"APPROVED\", new java.math.BigDecimal(\"80.00\")), new PaymentLimitPolicy.Decision(true, \"APPROVED\", new java.math.BigDecimal(\"80.00\")), new java.math.BigDecimal(\"80.00\"))", true)
                .equalsCase("limite diário considera operações acumuladas",
                        "PaymentLimitPolicy policy = new PaymentLimitPolicy(\"100.00\", \"150.00\"); var date = java.time.LocalDate.of(2026, 7, 15); policy.authorize(\"OP-1\", \"100.00\", date); var second = policy.authorize(\"OP-2\", \"60.00\", date);",
                        "java.util.List.of(second, policy.usedToday())",
                        "java.util.List.of(new PaymentLimitPolicy.Decision(false, \"DAILY_LIMIT\", new java.math.BigDecimal(\"100.00\")), new java.math.BigDecimal(\"100.00\"))", true)
                .equalsCase("limite por operação não consome acumulado",
                        "PaymentLimitPolicy policy = new PaymentLimitPolicy(\"50.00\", \"100.00\"); var decision = policy.authorize(\"OP-1\", \"50.01\", java.time.LocalDate.of(2026, 7, 15));",
                        "java.util.List.of(decision, policy.usedToday())",
                        "java.util.List.of(new PaymentLimitPolicy.Decision(false, \"PER_TRANSACTION_LIMIT\", new java.math.BigDecimal(\"0.00\")), new java.math.BigDecimal(\"0.00\"))", false)
                .equalsCase("novo dia reinicia o acumulado",
                        "PaymentLimitPolicy policy = new PaymentLimitPolicy(\"100.00\", \"100.00\"); policy.authorize(\"OP-1\", \"100.00\", java.time.LocalDate.of(2026, 7, 15)); var next = policy.authorize(\"OP-2\", \"25.00\", java.time.LocalDate.of(2026, 7, 16));",
                        "java.util.List.of(next.approved(), policy.usedToday())",
                        "java.util.List.of(true, new java.math.BigDecimal(\"25.00\"))", true)
                .throwsCase("evento de dia anterior falha fechado",
                        "PaymentLimitPolicy policy = new PaymentLimitPolicy(\"100.00\", \"100.00\"); policy.authorize(\"OP-1\", \"10.00\", java.time.LocalDate.of(2026, 7, 16));",
                        "policy.authorize(\"OP-2\", \"10.00\", java.time.LocalDate.of(2026, 7, 15))",
                        "IllegalArgumentException", false)
                .hint("Consulte a operação idempotente antes de avaliar data e valor.")
                .hint("Recusas também precisam ser persistidas no mapa de decisões para permanecer determinísticas em retries.")
                .hint("Avançar o dia reinicia o acumulado; voltar no tempo deve ser rejeitado.")
                .build();
    }

    private static Exercise buildFeePolicy(LearningModule module) {
        return ExerciseBuilder.of(
                        "bank-real-16-fee-and-plan-policy",
                        module,
                        "Tarifas e planos: cotação determinística",
                        """
                        ## Contexto real

                        Tarifas fazem parte do valor total debitado e precisam ser explicáveis antes
                        da confirmação. Plano, operação, piso, teto e arredondamento não podem depender
                        da interface ou de um cálculo posterior.

                        ## Objetivo

                        Implemente `public final class FeePolicy` com planos `BASIC` e `PREMIUM`:

                        - Pix BASIC custa 1%, com mínimo de R$ 0,50 e máximo de R$ 10,00.
                        - Pix PREMIUM é gratuito.
                        - Boleto BASIC custa R$ 2,50 e PREMIUM custa R$ 1,00.
                        - `quote` devolve valor, tarifa e débito total com escala dois.
                        - Valores nulos, zero ou negativos são rejeitados.

                        ## Invariante

                        A cotação é pura e determinística: mesma política e mesma entrada produzem o
                        mesmo total. O débito real deve usar a cotação confirmada, não recalcular regras escondidas.
                        """,
                        """
                        public final class FeePolicy {
                            public enum Plan { BASIC, PREMIUM }
                            public enum Operation { PIX, BOLETO }
                            public record Quote(java.math.BigDecimal amount, java.math.BigDecimal fee,
                                                java.math.BigDecimal totalDebit) {}
                            public Quote quote(Plan plan, Operation operation, String amount) { return null; }
                        }
                        """,
                        """
                        public final class FeePolicy {
                            public enum Plan { BASIC, PREMIUM }
                            public enum Operation { PIX, BOLETO }
                            public record Quote(java.math.BigDecimal amount, java.math.BigDecimal fee,
                                                java.math.BigDecimal totalDebit) {}
                            public Quote quote(Plan plan, Operation operation, String amount) { return null; }
                        }
                        """,
                        "INTERMEDIÁRIO", 15, 25)
                .referenceSolution("""
                        public final class FeePolicy {
                            public enum Plan { BASIC, PREMIUM }
                            public enum Operation { PIX, BOLETO }
                            public record Quote(java.math.BigDecimal amount, java.math.BigDecimal fee,
                                                java.math.BigDecimal totalDebit) {}

                            public Quote quote(Plan plan, Operation operation, String rawAmount) {
                                java.util.Objects.requireNonNull(plan, "plan");
                                java.util.Objects.requireNonNull(operation, "operation");
                                java.math.BigDecimal amount = money(rawAmount);
                                if (amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
                                java.math.BigDecimal fee = switch (operation) {
                                    case PIX -> pixFee(plan, amount);
                                    case BOLETO -> plan == Plan.BASIC
                                            ? new java.math.BigDecimal("2.50")
                                            : new java.math.BigDecimal("1.00");
                                };
                                fee = fee.setScale(2, java.math.RoundingMode.HALF_UP);
                                return new Quote(amount, fee, amount.add(fee));
                            }

                            private static java.math.BigDecimal pixFee(Plan plan, java.math.BigDecimal amount) {
                                if (plan == Plan.PREMIUM) return new java.math.BigDecimal("0.00");
                                java.math.BigDecimal proportional = amount.multiply(new java.math.BigDecimal("0.01"));
                                java.math.BigDecimal minimum = new java.math.BigDecimal("0.50");
                                java.math.BigDecimal maximum = new java.math.BigDecimal("10.00");
                                return proportional.max(minimum).min(maximum);
                            }

                            private static java.math.BigDecimal money(String raw) {
                                if (raw == null || raw.isBlank()) throw new IllegalArgumentException("Amount is required");
                                return new java.math.BigDecimal(raw).setScale(2, java.math.RoundingMode.UNNECESSARY);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "return proportional.max(minimum).min(maximum);",
                        "Piso e teto são parte da política explícita, mantendo a cotação previsível em todos os canais.")
                .solutionAnnotation(
                        "return new Quote(amount, fee, amount.add(fee));",
                        "O débito total é apresentado como contrato único antes da autorização do pagamento.")
                .equalsCase("Pix BASIC aplica tarifa mínima",
                        "FeePolicy policy = new FeePolicy();",
                        "policy.quote(FeePolicy.Plan.BASIC, FeePolicy.Operation.PIX, \"10.00\")",
                        "new FeePolicy.Quote(new java.math.BigDecimal(\"10.00\"), new java.math.BigDecimal(\"0.50\"), new java.math.BigDecimal(\"10.50\"))", true)
                .equalsCase("Pix BASIC aplica percentual intermediário",
                        "FeePolicy policy = new FeePolicy();",
                        "policy.quote(FeePolicy.Plan.BASIC, FeePolicy.Operation.PIX, \"500.00\")",
                        "new FeePolicy.Quote(new java.math.BigDecimal(\"500.00\"), new java.math.BigDecimal(\"5.00\"), new java.math.BigDecimal(\"505.00\"))", true)
                .equalsCase("Pix BASIC respeita tarifa máxima",
                        "FeePolicy policy = new FeePolicy();",
                        "policy.quote(FeePolicy.Plan.BASIC, FeePolicy.Operation.PIX, \"2000.00\")",
                        "new FeePolicy.Quote(new java.math.BigDecimal(\"2000.00\"), new java.math.BigDecimal(\"10.00\"), new java.math.BigDecimal(\"2010.00\"))", false)
                .equalsCase("Pix PREMIUM é gratuito",
                        "FeePolicy policy = new FeePolicy();",
                        "policy.quote(FeePolicy.Plan.PREMIUM, FeePolicy.Operation.PIX, \"100.00\")",
                        "new FeePolicy.Quote(new java.math.BigDecimal(\"100.00\"), new java.math.BigDecimal(\"0.00\"), new java.math.BigDecimal(\"100.00\"))", true)
                .equalsCase("boleto varia por plano",
                        "FeePolicy policy = new FeePolicy();",
                        "java.util.List.of(policy.quote(FeePolicy.Plan.BASIC, FeePolicy.Operation.BOLETO, \"50.00\").fee(), policy.quote(FeePolicy.Plan.PREMIUM, FeePolicy.Operation.BOLETO, \"50.00\").fee())",
                        "java.util.List.of(new java.math.BigDecimal(\"2.50\"), new java.math.BigDecimal(\"1.00\"))", true)
                .throwsCase("valor zero é rejeitado",
                        "FeePolicy policy = new FeePolicy();",
                        "policy.quote(FeePolicy.Plan.BASIC, FeePolicy.Operation.PIX, \"0.00\")",
                        "IllegalArgumentException", false)
                .hint("Calcule a tarifa antes de construir o Quote e normalize-a para escala dois.")
                .hint("Use switch sobre a operação e trate o plano dentro da política correspondente.")
                .hint("Quote deve carregar o total debitado para impedir que a interface faça um segundo cálculo.")
                .build();
    }
}
