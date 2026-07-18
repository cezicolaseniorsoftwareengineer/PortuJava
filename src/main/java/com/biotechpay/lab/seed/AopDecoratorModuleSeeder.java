package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/**
 * AOP-simulated track stub: hand-rolled decorators that separate a cross-cutting concern (logging,
 * audit) from business logic, without any framework. This is deliberately "AOP by hand" - real Spring
 * {@code @Aspect} needs a live ApplicationContext to weave proxies, which does not fit the
 * per-submission compiled-snippet sandbox this platform grades exercises in. A live-Spring-AOP module
 * is backlog for a future phase where exercises can run inside a real Spring context.
 */
@Component
public class AopDecoratorModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public AopDecoratorModuleSeeder(LearningModuleRepository moduleRepository, ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "aop-decorator-simulation";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Aspectos na Mão: Decorators de Robô",
                "AOP_SIMULATED",
                "Antes de usar @Aspect do Spring, entenda o que ele faz por baixo dos panos: um " +
                        "decorator que envolve o código de negócio com uma preocupação transversal " +
                        "(logging, auditoria) sem que o código de negócio saiba disso.",
                2));

        exerciseRepository.save(buildManualWrapper(module));
        exerciseRepository.save(buildCrossCuttingMultiple(module));
        exerciseRepository.save(buildConditionalAudit(module));

        return module;
    }

    @NonNull
    private Exercise buildManualWrapper(LearningModule module) {
        return ExerciseBuilder.of(
                        "aop-01-manual-wrapper",
                        module,
                        "Decorator manual de logging",
                        """
                        ## Contexto

                        `@Around` do Spring AOP intercepta uma chamada, roda algo antes, deixa o método \
                        original rodar, e roda algo depois. Antes de usar a anotação mágica, construa \
                        esse mecanismo na mão.

                        ## Objetivo

                        Declare `interface RobotCommand { void execute(); }` e \
                        `public class LoggingRobotCommand implements RobotCommand`, cujo construtor recebe um \
                        `RobotCommand inner` e uma `java.util.List<String> log`. Ao chamar `execute()`:

                        1. Adicione `"BEFORE"` ao log.
                        2. Chame `inner.execute()`.
                        3. Adicione `"AFTER"` ao log.

                        Só uma classe do arquivo pode ser `public` - use `LoggingRobotCommand` para isso; \
                        `RobotCommand` fica sem modificador (package-private).

                        ## Critério de sucesso

                        O log deve conter exatamente `BEFORE`, depois o que o comando interno registrar, \
                        depois `AFTER` - nessa ordem.
                        """,
                        """
                        interface RobotCommand {
                            void execute();
                        }

                        public class LoggingRobotCommand implements RobotCommand {
                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) { }
                            public void execute() { }
                        }
                        """,
                        """
                        interface RobotCommand {
                            void execute();
                        }

                        public class LoggingRobotCommand implements RobotCommand {
                            // TODO: guarde inner e log; em execute(), registre BEFORE, chame inner, registre AFTER.

                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) {

                            }

                            public void execute() {

                            }
                        }
                        """,
                        "BÁSICO", 0, 12)
                .referenceSolution("""
                        interface RobotCommand {
                            void execute();
                        }

                        public class LoggingRobotCommand implements RobotCommand {
                            private final RobotCommand inner;
                            private final java.util.List<String> log;

                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) {
                                this.inner = inner;
                                this.log = log;
                            }

                            public void execute() {
                                log.add("BEFORE");
                                inner.execute();
                                log.add("AFTER");
                            }
                        }
                        """)
                .solutionAnnotation(
                        "private final RobotCommand inner;\n    private final java.util.List<String> log;",
                        "O decorator guarda uma REFERÊNCIA ao comando original (inner) - ele não substitui o " +
                                "comando, ele o envolve.")
                .solutionAnnotation(
                        "log.add(\"BEFORE\");\n        inner.execute();\n        log.add(\"AFTER\");",
                        "Essa é a estrutura de um @Around do Spring AOP, na mão: algo antes, o método original " +
                                "roda no meio, algo depois. inner.execute() é o 'proceed()' do mundo real.")
                .equalsCase("log registra BEFORE, a ação interna, e AFTER nesta ordem",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> log.add(\"MOVE\"); " +
                                "RobotCommand wrapped = new LoggingRobotCommand(inner, log); wrapped.execute();",
                        "String.join(\",\", log)", "\"BEFORE,MOVE,AFTER\"", true)
                .equalsCase("duas execuções acumulam seis entradas no log",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> log.add(\"TURN\"); " +
                                "RobotCommand wrapped = new LoggingRobotCommand(inner, log); wrapped.execute(); wrapped.execute();",
                        "log.size()", "6", false)
                .hint("RobotCommand é uma interface funcional - pode usar lambda: () -> log.add(\"MOVE\")")
                .hint("execute() sempre segue a ordem: BEFORE, inner.execute(), AFTER.")
                .build();
    }

    @NonNull
    private Exercise buildCrossCuttingMultiple(LearningModule module) {
        return ExerciseBuilder.of(
                        "aop-02-cross-cutting-multiple",
                        module,
                        "Reuso da preocupação transversal",
                        """
                        ## Contexto

                        O ponto central do AOP: a MESMA lógica de logging deve funcionar para QUALQUER \
                        comando, sem duplicar código de logging dentro de cada comando.

                        ## Objetivo

                        Implemente a mesma estrutura do exercício anterior (`RobotCommand` + \
                        `LoggingRobotCommand`) e comprove que o MESMO decorator funciona para comandos \
                        diferentes, sem alterar `LoggingRobotCommand`.

                        ## Critério de sucesso

                        Envolver dois comandos diferentes com `LoggingRobotCommand` produz um log \
                        combinado correto, sem que `LoggingRobotCommand` precise saber o que cada \
                        comando faz.
                        """,
                        """
                        interface RobotCommand {
                            void execute();
                        }

                        public class LoggingRobotCommand implements RobotCommand {
                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) { }
                            public void execute() { }
                        }
                        """,
                        """
                        interface RobotCommand {
                            void execute();
                        }

                        public class LoggingRobotCommand implements RobotCommand {
                            // TODO: igual ao exercício anterior.

                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) {

                            }

                            public void execute() {

                            }
                        }
                        """,
                        "BÁSICO", 1, 10)
                .referenceSolution("""
                        interface RobotCommand {
                            void execute();
                        }

                        public class LoggingRobotCommand implements RobotCommand {
                            private final RobotCommand inner;
                            private final java.util.List<String> log;

                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) {
                                this.inner = inner;
                                this.log = log;
                            }

                            public void execute() {
                                log.add("BEFORE");
                                inner.execute();
                                log.add("AFTER");
                            }
                        }
                        """)
                .solutionAnnotation(
                        "public void execute() {\n        log.add(\"BEFORE\");\n        inner.execute();\n        log.add(\"AFTER\");\n    }",
                        "Essa mesma classe funciona para QUALQUER RobotCommand - envolver um comando diferente " +
                                "não exige mudar uma linha aqui. É o ponto central do AOP: a preocupação transversal é escrita uma vez só.")
                .equalsCase("dois comandos diferentes envolvidos pelo mesmo decorator produzem o log correto",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand move = () -> log.add(\"MOVE\"); " +
                                "RobotCommand turn = () -> log.add(\"TURN\"); " +
                                "new LoggingRobotCommand(move, log).execute(); new LoggingRobotCommand(turn, log).execute();",
                        "String.join(\",\", log)", "\"BEFORE,MOVE,AFTER,BEFORE,TURN,AFTER\"", true)
                .hint("Não precisa mudar nada em LoggingRobotCommand - só criar duas instâncias, uma por comando.")
                .build();
    }

    @NonNull
    private Exercise buildConditionalAudit(LearningModule module) {
        return ExerciseBuilder.of(
                        "aop-03-conditional-audit",
                        module,
                        "Auditoria condicional (after-throwing)",
                        """
                        ## Contexto

                        Alguns aspectos só agem quando algo dá errado - é o equivalente a `@AfterThrowing` \
                        do Spring AOP: audita a falha, mas deixa a exceção seguir seu curso normal.

                        ## Objetivo

                        Declare `interface RobotCommand { void execute(); }` e \
                        `public class AuditOnFailureCommand implements RobotCommand` (construtor recebe \
                        `RobotCommand inner` e `java.util.List<String> log`):

                        - Se `inner.execute()` completar sem exceção, o log permanece vazio.
                        - Se `inner.execute()` lançar uma `RuntimeException`, registre \
                        `"FAILURE: " + mensagem` no log e **relance a mesma exceção** (auditoria nunca \
                        deve engolir o erro).

                        ## Critério de sucesso

                        A exceção original sempre continua se propagando - a auditoria observa, nunca \
                        intercepta silenciosamente.
                        """,
                        """
                        interface RobotCommand {
                            void execute();
                        }

                        public class AuditOnFailureCommand implements RobotCommand {
                            public AuditOnFailureCommand(RobotCommand inner, java.util.List<String> log) { }
                            public void execute() { }
                        }
                        """,
                        """
                        interface RobotCommand {
                            void execute();
                        }

                        public class AuditOnFailureCommand implements RobotCommand {
                            // TODO: se inner.execute() lançar RuntimeException, registre e relance.

                            public AuditOnFailureCommand(RobotCommand inner, java.util.List<String> log) {

                            }

                            public void execute() {

                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 2, 15)
                .referenceSolution("""
                        interface RobotCommand {
                            void execute();
                        }

                        public class AuditOnFailureCommand implements RobotCommand {
                            private final RobotCommand inner;
                            private final java.util.List<String> log;

                            public AuditOnFailureCommand(RobotCommand inner, java.util.List<String> log) {
                                this.inner = inner;
                                this.log = log;
                            }

                            public void execute() {
                                try {
                                    inner.execute();
                                } catch (RuntimeException e) {
                                    log.add("FAILURE: " + e.getMessage());
                                    throw e;
                                }
                            }
                        }
                        """)
                .solutionAnnotation(
                        "try {\n                    inner.execute();\n                } catch (RuntimeException e) {\n                    log.add(\"FAILURE: \" + e.getMessage());\n                    throw e;\n                }",
                        "O catch registra a falha e IMEDIATAMENTE relança a mesma exceção (throw e;) - auditoria " +
                                "observa, nunca engole o erro. É o equivalente na mão de um @AfterThrowing.")
                .equalsCase("execução bem sucedida não registra nada no log",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> { }; " +
                                "new AuditOnFailureCommand(inner, log).execute();",
                        "log.size()", "0", true)
                .equalsCase("falha é auditada E relançada",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> { throw new RuntimeException(\"boom\"); }; " +
                                "RobotCommand wrapped = new AuditOnFailureCommand(inner, log); " +
                                "boolean rethrown = false; " +
                                "try { wrapped.execute(); } catch (RuntimeException e) { rethrown = true; }",
                        "rethrown && log.get(0).contains(\"boom\")", "true", true)
                .hint("Use try/catch (RuntimeException e) ao redor de inner.execute().")
                .hint("Depois de registrar no log, use 'throw e;' para relançar a mesma exceção.")
                .build();
    }
}
