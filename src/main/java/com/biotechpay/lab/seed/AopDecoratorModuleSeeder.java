package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
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
                "Aspectos na Mao: Decorators de Robo",
                "AOP_SIMULATED",
                "Antes de usar @Aspect do Spring, entenda o que ele faz por baixo dos panos: um " +
                        "decorator que envolve o codigo de negocio com uma preocupacao transversal " +
                        "(logging, auditoria) sem que o codigo de negocio saiba disso.",
                2));

        exerciseRepository.save(buildManualWrapper(module));
        exerciseRepository.save(buildCrossCuttingMultiple(module));
        exerciseRepository.save(buildConditionalAudit(module));

        return module;
    }

    private Exercise buildManualWrapper(LearningModule module) {
        return ExerciseBuilder.of(
                        "aop-01-manual-wrapper",
                        module,
                        "Decorator manual de logging",
                        """
                        ## Contexto

                        `@Around` do Spring AOP intercepta uma chamada, roda algo antes, deixa o metodo \
                        original rodar, e roda algo depois. Antes de usar a anotacao magica, construa \
                        esse mecanismo na mao.

                        ## Objetivo

                        Declare `interface RobotCommand { void execute(); }` e \
                        `public class LoggingRobotCommand implements RobotCommand`, cujo construtor recebe um \
                        `RobotCommand inner` e uma `java.util.List<String> log`. Ao chamar `execute()`:

                        1. Adicione `"BEFORE"` ao log.
                        2. Chame `inner.execute()`.
                        3. Adicione `"AFTER"` ao log.

                        So uma classe do arquivo pode ser `public` - use `LoggingRobotCommand` para isso; \
                        `RobotCommand` fica sem modificador (package-private).

                        ## Criterio de sucesso

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
                        "BASICO", 0, 12)
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
                .equalsCase("log registra BEFORE, a acao interna, e AFTER nesta ordem",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> log.add(\"MOVE\"); " +
                                "RobotCommand wrapped = new LoggingRobotCommand(inner, log); wrapped.execute();",
                        "String.join(\",\", log)", "\"BEFORE,MOVE,AFTER\"", true)
                .equalsCase("duas execucoes acumulam seis entradas no log",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> log.add(\"TURN\"); " +
                                "RobotCommand wrapped = new LoggingRobotCommand(inner, log); wrapped.execute(); wrapped.execute();",
                        "log.size()", "6", false)
                .hint("RobotCommand e uma interface funcional - pode usar lambda: () -> log.add(\"MOVE\")")
                .hint("execute() sempre segue a ordem: BEFORE, inner.execute(), AFTER.")
                .build();
    }

    private Exercise buildCrossCuttingMultiple(LearningModule module) {
        return ExerciseBuilder.of(
                        "aop-02-cross-cutting-multiple",
                        module,
                        "Reuso da preocupacao transversal",
                        """
                        ## Contexto

                        O ponto central do AOP: a MESMA logica de logging deve funcionar para QUALQUER \
                        comando, sem duplicar codigo de logging dentro de cada comando.

                        ## Objetivo

                        Implemente a mesma estrutura do exercicio anterior (`RobotCommand` + \
                        `LoggingRobotCommand`) e comprove que o MESMO decorator funciona para comandos \
                        diferentes, sem alterar `LoggingRobotCommand`.

                        ## Criterio de sucesso

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
                            // TODO: igual ao exercicio anterior.

                            public LoggingRobotCommand(RobotCommand inner, java.util.List<String> log) {

                            }

                            public void execute() {

                            }
                        }
                        """,
                        "BASICO", 1, 10)
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
                .equalsCase("dois comandos diferentes envolvidos pelo mesmo decorator produzem o log correto",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand move = () -> log.add(\"MOVE\"); " +
                                "RobotCommand turn = () -> log.add(\"TURN\"); " +
                                "new LoggingRobotCommand(move, log).execute(); new LoggingRobotCommand(turn, log).execute();",
                        "String.join(\",\", log)", "\"BEFORE,MOVE,AFTER,BEFORE,TURN,AFTER\"", true)
                .hint("Nao precisa mudar nada em LoggingRobotCommand - so criar duas instancias, uma por comando.")
                .build();
    }

    private Exercise buildConditionalAudit(LearningModule module) {
        return ExerciseBuilder.of(
                        "aop-03-conditional-audit",
                        module,
                        "Auditoria condicional (after-throwing)",
                        """
                        ## Contexto

                        Alguns aspectos so agem quando algo da errado - e o equivalente a `@AfterThrowing` \
                        do Spring AOP: audita a falha, mas deixa a excecao seguir seu curso normal.

                        ## Objetivo

                        Declare `interface RobotCommand { void execute(); }` e \
                        `public class AuditOnFailureCommand implements RobotCommand` (construtor recebe \
                        `RobotCommand inner` e `java.util.List<String> log`):

                        - Se `inner.execute()` completar sem excecao, o log permanece vazio.
                        - Se `inner.execute()` lancar uma `RuntimeException`, registre \
                        `"FAILURE: " + mensagem` no log e **relance a mesma excecao** (auditoria nunca \
                        deve engolir o erro).

                        ## Criterio de sucesso

                        A excecao original sempre continua se propagando - a auditoria observa, nunca \
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
                            // TODO: se inner.execute() lancar RuntimeException, registre e relance.

                            public AuditOnFailureCommand(RobotCommand inner, java.util.List<String> log) {

                            }

                            public void execute() {

                            }
                        }
                        """,
                        "INTERMEDIARIO", 2, 15)
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
                .equalsCase("execucao bem sucedida nao registra nada no log",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> { }; " +
                                "new AuditOnFailureCommand(inner, log).execute();",
                        "log.size()", "0", true)
                .equalsCase("falha e auditada E relancada",
                        "java.util.List<String> log = new java.util.ArrayList<>(); " +
                                "RobotCommand inner = () -> { throw new RuntimeException(\"boom\"); }; " +
                                "RobotCommand wrapped = new AuditOnFailureCommand(inner, log); " +
                                "boolean rethrown = false; " +
                                "try { wrapped.execute(); } catch (RuntimeException e) { rethrown = true; }",
                        "rethrown && log.get(0).contains(\"boom\")", "true", true)
                .hint("Use try/catch (RuntimeException e) ao redor de inner.execute().")
                .hint("Depois de registrar no log, use 'throw e;' para relancar a mesma excecao.")
                .build();
    }
}
