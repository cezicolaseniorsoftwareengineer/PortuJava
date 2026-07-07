package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * Rule-engine track stub: layered if/else decisions with an explicit fallback, modeling "se isso,
 * senão aquilo, e se tudo mais falhar" business logic for everyday automation (alarms, irrigation,
 * access control) rather than toy conditionals.
 */
@Component
public class RuleEngineModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public RuleEngineModuleSeeder(LearningModuleRepository moduleRepository, ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "rule-engine-daily-routine";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Motor de Regras: Automação do Dia a Dia",
                "RULE_ENGINE",
                "Sistemas de automação doméstica e de negócio raramente são um único if - são regras " +
                        "em camadas, com prioridade entre elas e um caminho de fallback seguro quando " +
                        "nenhuma regra específica se aplica.",
                3));

        exerciseRepository.save(buildAlarmDecision(module));
        exerciseRepository.save(buildIrrigationSystem(module));
        exerciseRepository.save(buildCombinedCriteria(module));

        return module;
    }

    private Exercise buildAlarmDecision(LearningModule module) {
        return ExerciseBuilder.of(
                        "routine-01-alarm-decision",
                        module,
                        "Decisão do despertador",
                        """
                        ## Contexto

                        Um despertador inteligente não tem uma regra só - ele prioriza sinais diferentes: \
                        o usuário pediu soneca? É fim de semana? É horário de trabalho?

                        ## Objetivo

                        Implemente `public class AlarmDecision` com:

                        `String decideAction(int hour, boolean isWeekday, boolean userSnoozed)`

                        Prioridade das regras (a primeira que combinar decide, nesta ordem):

                        1. Se `userSnoozed` for `true` -> `"ADIAR_5_MIN"` (soneca sempre vence).
                        2. Senão, se NÃO for dia de semana -> `"SILENCIAR"` (fim de semana, sem alarme).
                        3. Senão, se `hour` estiver entre 6 e 9 (inclusive) -> `"TOCAR_ALARME"`.
                        4. Caso nenhuma regra acima combine -> `"MANTER_ESTADO"` (fallback).

                        ## Critério de sucesso

                        A ordem de prioridade importa: soneca vence fim de semana, que vence horário de \
                        trabalho, que vence o fallback.
                        """,
                        """
                        public class AlarmDecision {
                            public String decideAction(int hour, boolean isWeekday, boolean userSnoozed) { return null; }
                        }
                        """,
                        """
                        public class AlarmDecision {
                            public String decideAction(int hour, boolean isWeekday, boolean userSnoozed) {
                                // TODO: implemente as 4 regras, nesta ordem de prioridade.
                                return null;
                            }
                        }
                        """,
                        "INICIANTE", 0, 12)
                .referenceSolution("""
                        public class AlarmDecision {
                            public String decideAction(int hour, boolean isWeekday, boolean userSnoozed) {
                                if (userSnoozed) {
                                    return "ADIAR_5_MIN";
                                }
                                if (!isWeekday) {
                                    return "SILENCIAR";
                                }
                                if (hour >= 6 && hour <= 9) {
                                    return "TOCAR_ALARME";
                                }
                                return "MANTER_ESTADO";
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (userSnoozed) {\n        return \"ADIAR_5_MIN\";\n    }",
                        "A checagem de maior prioridade vem primeiro - soneca vence qualquer outra regra, então " +
                                "é a primeira pergunta que o método faz.")
                .solutionAnnotation(
                        "return \"MANTER_ESTADO\";",
                        "O fallback é a última linha, alcançada só se nenhuma das três regras anteriores " +
                                "combinar - um motor de regras sempre precisa de um caminho seguro para quando nada específico se aplica.")
                .equalsCase("dia de semana, horário de trabalho, sem soneca -> toca o alarme",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, true, false)", "\"TOCAR_ALARME\"", true)
                .equalsCase("fim de semana, mesmo horário -> silencia",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, false, false)", "\"SILENCIAR\"", true)
                .equalsCase("soneca vence mesmo em dia de semana no horário de trabalho",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, true, true)", "\"ADIAR_5_MIN\"", false)
                .equalsCase("dia de semana fora do horário de trabalho -> fallback",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(14, true, false)", "\"MANTER_ESTADO\"", true)
                .equalsCase("soneca vence até no fim de semana",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, false, true)", "\"ADIAR_5_MIN\"", false)
                .hint("Cheque userSnoozed PRIMEIRO - ele vence qualquer outra regra.")
                .hint("Só depois de descartar soneca é que faz sentido checar fim de semana.")
                .hint("O fallback MANTER_ESTADO só acontece se nenhuma das três regras acima combinar.")
                .build();
    }

    private Exercise buildIrrigationSystem(LearningModule module) {
        return ExerciseBuilder.of(
                        "routine-02-irrigation-system",
                        module,
                        "Sistema de irrigação",
                        """
                        ## Contexto

                        Irrigar automaticamente parece simples ("se o solo estiver seco, irrigue"), mas \
                        um sistema real precisa respeitar overrides manuais e previsão de chuva antes \
                        de gastar água.

                        ## Objetivo

                        Implemente `public class IrrigationSystem` com:

                        `String decideIrrigation(int soilMoisturePercent, boolean rainForecast, boolean manualOverride)`

                        Prioridade das regras, nesta ordem:

                        1. Se `manualOverride` for `true` -> `"IRRIGAR_MANUAL"` (vence tudo o mais).
                        2. Senão, se `rainForecast` for `true` -> `"NAO_IRRIGAR"` (chuva a caminho, não desperdice água).
                        3. Senão, se `soilMoisturePercent < 30` -> `"IRRIGAR"`.
                        4. Caso contrário -> `"NAO_IRRIGAR"` (fallback seguro: sem necessidade clara, não irriga).

                        ## Critério de sucesso

                        `manualOverride` vence QUALQUER outra condição, inclusive solo seco com chuva prevista.
                        """,
                        """
                        public class IrrigationSystem {
                            public String decideIrrigation(int soilMoisturePercent, boolean rainForecast, boolean manualOverride) { return null; }
                        }
                        """,
                        """
                        public class IrrigationSystem {
                            public String decideIrrigation(int soilMoisturePercent, boolean rainForecast, boolean manualOverride) {
                                // TODO: implemente as 4 regras, nesta ordem de prioridade.
                                return null;
                            }
                        }
                        """,
                        "INICIANTE", 1, 12)
                .referenceSolution("""
                        public class IrrigationSystem {
                            public String decideIrrigation(int soilMoisturePercent, boolean rainForecast, boolean manualOverride) {
                                if (manualOverride) {
                                    return "IRRIGAR_MANUAL";
                                }
                                if (rainForecast) {
                                    return "NAO_IRRIGAR";
                                }
                                if (soilMoisturePercent < 30) {
                                    return "IRRIGAR";
                                }
                                return "NAO_IRRIGAR";
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (manualOverride) {\n        return \"IRRIGAR_MANUAL\";\n    }",
                        "manualOverride é a PRIMEIRA checagem - vence até chuva prevista e solo seco. Um override " +
                                "manual sempre tem a última palavra sobre a automação.")
                .solutionAnnotation(
                        "if (rainForecast) {\n        return \"NAO_IRRIGAR\";\n    }",
                        "Só chega aqui se não houver override. Chuva prevista impede irrigação mesmo com solo " +
                                "seco - não faz sentido gastar água se ela está a caminho de graça.")
                .equalsCase("solo seco, sem chuva prevista, sem override -> irriga",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, false, false)", "\"IRRIGAR\"", true)
                .equalsCase("chuva prevista impede irrigação mesmo com solo seco",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, true, false)", "\"NAO_IRRIGAR\"", true)
                .equalsCase("override manual vence tudo, inclusive chuva prevista",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, true, true)", "\"IRRIGAR_MANUAL\"", false)
                .equalsCase("solo úmido sem chuva -> fallback não irriga",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(80, false, false)", "\"NAO_IRRIGAR\"", true)
                .equalsCase("override manual vence mesmo com solo seco e sem chuva",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, false, true)", "\"IRRIGAR_MANUAL\"", false)
                .hint("manualOverride é a PRIMEIRA checagem - nada mais importa se ele for true.")
                .hint("rainForecast só é checado depois de descartar o override manual.")
                .build();
    }

    private Exercise buildCombinedCriteria(LearningModule module) {
        return ExerciseBuilder.of(
                        "routine-03-combined-criteria",
                        module,
                        "Critérios combinados com validação",
                        """
                        ## Contexto

                        Regras de negócio combinam múltiplas condições com E/OU, e antes de avaliar \
                        qualquer regra, é preciso garantir que a entrada faz sentido.

                        ## Objetivo

                        Implemente `public class AccessControl` com:

                        `String evaluateAccess(int hour, boolean isHoliday, boolean hasKeycard)`

                        - Precondição: se `hour` for menor que 0 ou maior que 23, lance \
                        `IllegalArgumentException` ANTES de avaliar qualquer regra.
                        - Se `hasKeycard` E NÃO for feriado E `hour` estiver entre 8 e 18 (inclusive), \
                        retorne `"ACESSO_LIBERADO"`.
                        - Em qualquer outro caso válido, retorne `"ACESSO_NEGADO"` (fallback).

                        ## Critério de sucesso

                        A validação de entrada acontece ANTES da lógica de negócio, e feriado bloqueia \
                        o acesso mesmo com cartão válido e horário correto.
                        """,
                        """
                        public class AccessControl {
                            public String evaluateAccess(int hour, boolean isHoliday, boolean hasKeycard) { return null; }
                        }
                        """,
                        """
                        public class AccessControl {
                            public String evaluateAccess(int hour, boolean isHoliday, boolean hasKeycard) {
                                // TODO: valide hour primeiro, depois avalie a regra combinada.
                                return null;
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 2, 15)
                .referenceSolution("""
                        public class AccessControl {
                            public String evaluateAccess(int hour, boolean isHoliday, boolean hasKeycard) {
                                if (hour < 0 || hour > 23) {
                                    throw new IllegalArgumentException("hour must be between 0 and 23");
                                }
                                if (hasKeycard && !isHoliday && hour >= 8 && hour <= 18) {
                                    return "ACESSO_LIBERADO";
                                }
                                return "ACESSO_NEGADO";
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (hour < 0 || hour > 23) {\n        throw new IllegalArgumentException(\"hour must be between 0 and 23\");\n    }",
                        "A validação de entrada acontece ANTES de qualquer regra de negócio - nunca avalia " +
                                "lógica de acesso com um dado que nem faz sentido existir.")
                .solutionAnnotation(
                        "if (hasKeycard && !isHoliday && hour >= 8 && hour <= 18) {",
                        "A liberação exige as QUATRO condições simultaneamente (E lógico) - falhar em qualquer " +
                                "uma delas, incluindo feriado, já é suficiente para negar.")
                .equalsCase("cartão válido, horário comercial, sem feriado -> acesso liberado",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(10, false, true)", "\"ACESSO_LIBERADO\"", true)
                .equalsCase("sem cartão -> acesso negado mesmo no horário certo",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(10, false, false)", "\"ACESSO_NEGADO\"", true)
                .equalsCase("feriado bloqueia mesmo com cartão e horário válidos",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(10, true, true)", "\"ACESSO_NEGADO\"", false)
                .throwsCase("hora inválida lança IllegalArgumentException antes de avaliar regras",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(24, false, true)", "java.lang.IllegalArgumentException", true)
                .equalsCase("fora do horário comercial -> acesso negado",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(19, false, true)", "\"ACESSO_NEGADO\"", false)
                .hint("A validação de hour (0-23) é a PRIMEIRA coisa que o método faz.")
                .hint("A regra de liberação é um E de quatro condições: cartão, não-feriado, hora >= 8, hora <= 18.")
                .build();
    }
}
