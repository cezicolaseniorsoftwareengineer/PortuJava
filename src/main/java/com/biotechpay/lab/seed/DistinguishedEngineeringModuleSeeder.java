package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

/** Teaches a reproducible engineering method instead of personality imitation or slogans. */
@Component
public class DistinguishedEngineeringModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public DistinguishedEngineeringModuleSeeder(LearningModuleRepository moduleRepository,
                                                  ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "distinguished-engineering-method";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Como pensar antes, durante e depois do código",
                "RACIOCÍNIO",
                "Treine um método reproduzível: observar fatos, modelar invariantes, contestar " +
                        "hipóteses, implementar a menor mudança segura, verificar por evidência e " +
                        "legar testes, ADRs e runbooks. A síntese usa práticas públicas de engenharia, " +
                        "não uma alegação sobre pensamento interno ou proprietário de empresas.",
                8));
        synchronize(module);
        return module;
    }

    @Override
    public void synchronize(LearningModule module) {
        saveIfMissing(checklist(module, 1, "problem-framing", "Enquadre o problema antes da solução",
                "Antes de abrir o editor, separe fato observado, hipótese, fonte da verdade, limite de autoridade e resultado verificável.",
                List.of("problemObserved", "sourceOfTruthLocated", "invariantsWritten", "successCriteriaMeasurable"),
                List.of("assumptionPresentedAsFact", "scopeNotAuthorized")));
        saveIfMissing(checklist(module, 2, "adversarial-review", "Tente invalidar sua própria proposta",
                "Durante o desenho, produza contraexemplos de concorrência, repetição, falha parcial, abuso e recuperação.",
                List.of("concurrencyChallenged", "retryChallenged", "partialFailureChallenged", "abuseCaseChallenged", "recoveryChallenged"),
                List.of("unresolvedSafetyFailure", "unreconciledMoney")));
        saveIfMissing(repositoryArchitecture(module));
        saveIfMissing(gitDelivery(module));
        saveIfMissing(checklist(module, 5, "production-delivery", "Entregue para produção com saída conhecida",
                "Antes do deploy, conecte mudança, testes, observabilidade, rollout, rollback e runbook. Depois, observe o efeito real.",
                List.of("testsGreen", "observabilityReady", "rolloutIsProgressive", "rollbackRehearsed", "runbookReady"),
                List.of("redRequiredTest", "unknownRollback", "unresolvedProductionVeto")));
        saveIfMissing(checklist(module, 6, "technical-defense", "Defenda a decisão com evidência",
                "Explique problema, invariantes, alternativas, trade-offs, evidência, riscos residuais e condição objetiva de liberação.",
                List.of("decisionExplained", "invariantsProtected", "alternativesCompared", "evidenceLinked", "residualRiskOwned"),
                List.of("unverifiableSuperlative", "externalValidationMisrepresented")));
    }

    private void saveIfMissing(Exercise exercise) {
        if (!exerciseRepository.existsByExerciseId(exercise.getExerciseId())) {
            exerciseRepository.save(exercise);
        }
    }

    private Exercise checklist(LearningModule module, int order, String slug, String title,
                               String context, List<String> required, List<String> vetoes) {
        String className = "EngineeringGate%02d".formatted(order);
        String statement = """
                ## Método OMCIVL

                %s

                O ciclo completo é: **Observar, Modelar, Contestar, Implementar, Verificar e Legar**.
                Este exercício transforma uma parte do método em gate. Evidência ausente bloqueia;
                veto ativo também bloqueia. Não existe aprovação por confiança, cargo ou eloquência.

                Obrigatórias: %s.

                Vetos: %s.
                """.formatted(context, markdown(required), markdown(vetoes));
        String contract = """
                public final class %s {
                    public record Decision(boolean released, java.util.List<String> missing,
                                           java.util.List<String> vetoes) {}
                    public Decision evaluate(java.util.Map<String, Boolean> evidence) { return null; }
                }
                """.formatted(className);
        String starter = """
                public final class %s {
                    public record Decision(boolean released, java.util.List<String> missing,
                                           java.util.List<String> vetoes) {}
                    public Decision evaluate(java.util.Map<String, Boolean> evidence) {
                        return null;
                    }
                }
                """.formatted(className);
        String solution = """
                public final class %s {
                    private static final java.util.List<String> REQUIRED = java.util.List.of(%s);
                    private static final java.util.List<String> VETOES = java.util.List.of(%s);
                    public record Decision(boolean released, java.util.List<String> missing,
                                           java.util.List<String> vetoes) {}
                    public Decision evaluate(java.util.Map<String, Boolean> evidence) {
                        if (evidence == null) throw new IllegalArgumentException("Evidence is required");
                        java.util.List<String> missing = REQUIRED.stream()
                                .filter(key -> !Boolean.TRUE.equals(evidence.get(key))).toList();
                        java.util.List<String> active = VETOES.stream()
                                .filter(key -> Boolean.TRUE.equals(evidence.get(key))).toList();
                        return new Decision(missing.isEmpty() && active.isEmpty(), missing, active);
                    }
                }
                """.formatted(className, quoted(required), quoted(vetoes));

        String pass = evidenceSetup(required, vetoes, null, null);
        String missing = evidenceSetup(required, vetoes, required.get(0), null);
        String veto = evidenceSetup(required, vetoes, null, vetoes.get(0));
        return ExerciseBuilder.of("distinguished-%02d-%s".formatted(order, slug), module, title,
                        statement, contract, starter, order <= 2 ? "BÁSICO" : "INTERMEDIÁRIO", order - 1, 25)
                .referenceSolution(solution)
                .solutionAnnotation("missing.isEmpty() && active.isEmpty()",
                        "O gate exige evidência positiva e ausência de condição impeditiva.")
                .equalsCase("aprova evidência completa", pass,
                        "new %s().evaluate(evidence).released()".formatted(className), "true", true)
                .equalsCase("nomeia evidência ausente", missing,
                        "new %s().evaluate(evidence).missing()".formatted(className),
                        "java.util.List.of(\"%s\")".formatted(required.get(0)), true)
                .equalsCase("nomeia veto ativo", veto,
                        "new %s().evaluate(evidence).vetoes()".formatted(className),
                        "java.util.List.of(\"%s\")".formatted(vetoes.get(0)), false)
                .hint("Trate chave ausente como false, nunca como aprovação implícita.")
                .hint("Calcule missing e vetoes separadamente antes de decidir released.")
                .build();
    }

    private Exercise repositoryArchitecture(LearningModule module) {
        String required = quoted(List.of(
                "README.md",
                "pom.xml",
                "mvnw",
                "mvnw.cmd",
                ".mvn/wrapper/maven-wrapper.properties",
                "backend/pom.xml",
                "backend/src/main/java/com/portujava/bank/domain/package-info.java",
                "backend/src/main/java/com/portujava/bank/application/package-info.java",
                "backend/src/main/java/com/portujava/bank/ports/package-info.java",
                "backend/src/main/java/com/portujava/bank/adapters/package-info.java",
                "backend/src/test/java/com/portujava/bank/ArchitectureTest.java",
                "frontend/package.json",
                "frontend/src/app/app.config.ts",
                "contracts/openapi/bank-api.yaml",
                "infrastructure/docker/compose.yaml",
                "docs/adr/0001-architecture.md",
                "docs/runbooks/incident.md",
                ".github/workflows/ci.yml"));
        return ExerciseBuilder.of("distinguished-03-repository-architecture", module,
                        "A árvore correta começa na raiz do produto",
                        """
                        Não existe uma árvore universal que sempre começa por `src`, `data` ou `app`.
                        A raiz representa o **produto e o contrato de entrega**. Num monorepo bancário,
                        `backend/` e `frontend/` são aplicações irmãs; cada uma contém seu próprio
                        `src`. `data/` não é fonte da verdade do banco nem lugar de segredos. `app/` é
                        uma convenção de algumas stacks, principalmente Python, não uma lei.

                        Valide a árvore Distinguished usada no Laboratório de Repositório:

                        - domínio sem Spring no núcleo;
                        - application orquestra casos de uso;
                        - ports definem contratos;
                        - adapters falam com web, banco, PSP e filas;
                        - contratos, infraestrutura, ADRs, runbooks e CI ficam explícitos na raiz.
                        """,
                        """
                        public final class BankRepositoryArchitecture {
                            public java.util.List<String> missing(java.util.Set<String> paths) { return null; }
                            public boolean valid(java.util.Set<String> paths) { return false; }
                        }
                        """,
                        """
                        public final class BankRepositoryArchitecture {
                            public java.util.List<String> missing(java.util.Set<String> paths) { return null; }
                            public boolean valid(java.util.Set<String> paths) { return false; }
                        }
                        """, "MÉDIO", 2, 30)
                .referenceSolution("""
                        public final class BankRepositoryArchitecture {
                            private static final java.util.List<String> REQUIRED = java.util.List.of(%s);
                            public java.util.List<String> missing(java.util.Set<String> paths) {
                                if (paths == null) throw new IllegalArgumentException("Paths are required");
                                return REQUIRED.stream().filter(path -> !paths.contains(path)).toList();
                            }
                            public boolean valid(java.util.Set<String> paths) { return missing(paths).isEmpty(); }
                        }
                        """.formatted(required))
                .equalsCase("aceita a árvore completa",
                        "java.util.Set<String> paths = new java.util.HashSet<>(java.util.List.of(" + required + "));",
                        "new BankRepositoryArchitecture().valid(paths)", "true", true)
                .equalsCase("aponta camada de domínio ausente",
                        "java.util.Set<String> paths = new java.util.HashSet<>(java.util.List.of(" + required + ")); " +
                                "paths.remove(\"backend/src/main/java/com/portujava/bank/domain/package-info.java\");",
                        "new BankRepositoryArchitecture().missing(paths)",
                        "java.util.List.of(\"backend/src/main/java/com/portujava/bank/domain/package-info.java\")", true)
                .equalsCase("arquivos extras não invalidam o produto",
                        "java.util.Set<String> paths = new java.util.HashSet<>(java.util.List.of(" + required + ")); paths.add(\"scripts/dev.ps1\");",
                        "new BankRepositoryArchitecture().valid(paths)", "true", false)
                .hint("Modele a lista obrigatória como constante imutável.")
                .hint("missing é a diferença ordenada entre REQUIRED e os caminhos recebidos.")
                .build();
    }

    private Exercise gitDelivery(LearningModule module) {
        return ExerciseBuilder.of("distinguished-04-git-delivery", module,
                        "Commit e push seguros no PowerShell e no Bash",
                        """
                        Git usa os mesmos comandos no Windows, macOS e Linux; o que muda é a sintaxe
                        do shell e, às vezes, o wrapper de build. Nunca comece por `git add .` sem
                        revisar o escopo. Primeiro veja status e diff, execute a validação, selecione
                        arquivos intencionalmente, faça um commit técnico e só então envie.

                        Implemente um plano que bloqueie a entrega se testes ou revisão do diff não
                        estiverem verdes. Para `POWERSHELL`, use `mvnw.cmd test`; para `BASH`, use
                        `./mvnw test`. Em ambos, finalize com commit e push explícitos.
                        """,
                        """
                        public final class GitDeliveryPlan {
                            public enum Shell { POWERSHELL, BASH }
                            public java.util.List<String> commands(Shell shell, boolean testsGreen,
                                                                   boolean diffReviewed, String branch) { return null; }
                        }
                        """,
                        """
                        public final class GitDeliveryPlan {
                            public enum Shell { POWERSHELL, BASH }
                            public java.util.List<String> commands(Shell shell, boolean testsGreen,
                                                                   boolean diffReviewed, String branch) { return null; }
                        }
                        """, "BÁSICO", 3, 25)
                .referenceSolution("""
                        public final class GitDeliveryPlan {
                            public enum Shell { POWERSHELL, BASH }
                            public java.util.List<String> commands(Shell shell, boolean testsGreen,
                                                                   boolean diffReviewed, String branch) {
                                if (shell == null || branch == null || branch.isBlank()) {
                                    throw new IllegalArgumentException("Shell and branch are required");
                                }
                                if (!testsGreen || !diffReviewed) {
                                    throw new IllegalStateException("Delivery gate is blocked");
                                }
                                String test = shell == Shell.POWERSHELL ? ".\\\\mvnw.cmd test" : "./mvnw test";
                                return java.util.List.of("git status --short", "git diff --check", test,
                                        "git add <reviewed-files>",
                                        "git commit -m \\\"Implement verified change\\\"",
                                        "git push origin " + branch);
                            }
                        }
                        """)
                .equalsCase("gera plano PowerShell",
                        "GitDeliveryPlan plan = new GitDeliveryPlan();",
                        "plan.commands(GitDeliveryPlan.Shell.POWERSHELL, true, true, \"main\").get(2)",
                        "\".\\\\mvnw.cmd test\"", true)
                .equalsCase("gera push explícito no Bash",
                        "GitDeliveryPlan plan = new GitDeliveryPlan();",
                        "plan.commands(GitDeliveryPlan.Shell.BASH, true, true, \"feature/bank\").get(5)",
                        "\"git push origin feature/bank\"", true)
                .throwsCase("bloqueia quando testes estão vermelhos", "",
                        "new GitDeliveryPlan().commands(GitDeliveryPlan.Shell.BASH, false, true, \"main\")",
                        "IllegalStateException", false)
                .throwsCase("bloqueia quando o diff não foi revisado", "",
                        "new GitDeliveryPlan().commands(GitDeliveryPlan.Shell.POWERSHELL, true, false, \"main\")",
                        "IllegalStateException", false)
                .hint("Valide todos os gates antes de montar a lista de comandos.")
                .hint("A diferença do build está no terceiro comando; Git permanece igual.")
                .build();
    }

    private static String markdown(List<String> values) {
        return values.stream().map(value -> "`" + value + "`")
                .reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String quoted(List<String> values) {
        return values.stream().map(value -> "\"" + value + "\"")
                .reduce((left, right) -> left + ", " + right).orElse("");
    }

    private static String evidenceSetup(List<String> required, List<String> vetoes,
                                        String missingKey, String activeVeto) {
        StringBuilder setup = new StringBuilder("java.util.Map<String, Boolean> evidence = new java.util.HashMap<>(); ");
        required.stream().filter(key -> !key.equals(missingKey))
                .forEach(key -> setup.append("evidence.put(\"").append(key).append("\", true); "));
        vetoes.forEach(key -> setup.append("evidence.put(\"").append(key).append("\", ")
                .append(key.equals(activeVeto)).append("); "));
        return setup.toString();
    }
}
