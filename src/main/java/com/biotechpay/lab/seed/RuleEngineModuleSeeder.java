package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * Rule-engine track stub: layered if/else decisions with an explicit fallback, modeling "se isso,
 * senao aquilo, e se tudo mais falhar" business logic for everyday automation (alarms, irrigation,
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
                "Motor de Regras: Automacao do Dia a Dia",
                "RULE_ENGINE",
                "Sistemas de automacao domestica e de negocio raramente sao um unico if - sao regras " +
                        "em camadas, com prioridade entre elas e um caminho de fallback seguro quando " +
                        "nenhuma regra especifica se aplica.",
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
                        "Decisao do despertador",
                        """
                        ## Contexto

                        Um despertador inteligente nao tem uma regra so - ele prioriza sinais diferentes: \
                        o usuario pediu soneca? E fim de semana? E horario de trabalho?

                        ## Objetivo

                        Implemente `public class AlarmDecision` com:

                        `String decideAction(int hour, boolean isWeekday, boolean userSnoozed)`

                        Prioridade das regras (a primeira que combinar decide, nesta ordem):

                        1. Se `userSnoozed` for `true` -> `"ADIAR_5_MIN"` (soneca sempre vence).
                        2. Senao, se NAO for dia de semana -> `"SILENCIAR"` (fim de semana, sem alarme).
                        3. Senao, se `hour` estiver entre 6 e 9 (inclusive) -> `"TOCAR_ALARME"`.
                        4. Caso nenhuma regra acima combine -> `"MANTER_ESTADO"` (fallback).

                        ## Criterio de sucesso

                        A ordem de prioridade importa: soneca vence fim de semana, que vence horario de \
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
                .equalsCase("dia de semana, horario de trabalho, sem soneca -> toca o alarme",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, true, false)", "\"TOCAR_ALARME\"", true)
                .equalsCase("fim de semana, mesmo horario -> silencia",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, false, false)", "\"SILENCIAR\"", true)
                .equalsCase("soneca vence mesmo em dia de semana no horario de trabalho",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, true, true)", "\"ADIAR_5_MIN\"", false)
                .equalsCase("dia de semana fora do horario de trabalho -> fallback",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(14, true, false)", "\"MANTER_ESTADO\"", true)
                .equalsCase("soneca vence ate no fim de semana",
                        "AlarmDecision a = new AlarmDecision();", "a.decideAction(7, false, true)", "\"ADIAR_5_MIN\"", false)
                .hint("Cheque userSnoozed PRIMEIRO - ele vence qualquer outra regra.")
                .hint("So depois de descartar soneca e que faz sentido checar fim de semana.")
                .hint("O fallback MANTER_ESTADO so acontece se nenhuma das tres regras acima combinar.")
                .build();
    }

    private Exercise buildIrrigationSystem(LearningModule module) {
        return ExerciseBuilder.of(
                        "routine-02-irrigation-system",
                        module,
                        "Sistema de irrigacao",
                        """
                        ## Contexto

                        Irrigar automaticamente parece simples ("se o solo estiver seco, irrigue"), mas \
                        um sistema real precisa respeitar overrides manuais e previsao de chuva antes \
                        de gastar agua.

                        ## Objetivo

                        Implemente `public class IrrigationSystem` com:

                        `String decideIrrigation(int soilMoisturePercent, boolean rainForecast, boolean manualOverride)`

                        Prioridade das regras, nesta ordem:

                        1. Se `manualOverride` for `true` -> `"IRRIGAR_MANUAL"` (vence tudo o mais).
                        2. Senao, se `rainForecast` for `true` -> `"NAO_IRRIGAR"` (chuva a caminho, nao desperdice agua).
                        3. Senao, se `soilMoisturePercent < 30` -> `"IRRIGAR"`.
                        4. Caso contrario -> `"NAO_IRRIGAR"` (fallback seguro: sem necessidade clara, nao irriga).

                        ## Criterio de sucesso

                        `manualOverride` vence QUALQUER outra condicao, inclusive solo seco com chuva prevista.
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
                .equalsCase("solo seco, sem chuva prevista, sem override -> irriga",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, false, false)", "\"IRRIGAR\"", true)
                .equalsCase("chuva prevista impede irrigacao mesmo com solo seco",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, true, false)", "\"NAO_IRRIGAR\"", true)
                .equalsCase("override manual vence tudo, inclusive chuva prevista",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, true, true)", "\"IRRIGAR_MANUAL\"", false)
                .equalsCase("solo umido sem chuva -> fallback nao irriga",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(80, false, false)", "\"NAO_IRRIGAR\"", true)
                .equalsCase("override manual vence mesmo com solo seco e sem chuva",
                        "IrrigationSystem s = new IrrigationSystem();", "s.decideIrrigation(15, false, true)", "\"IRRIGAR_MANUAL\"", false)
                .hint("manualOverride e a PRIMEIRA checagem - nada mais importa se ele for true.")
                .hint("rainForecast so e checado depois de descartar o override manual.")
                .build();
    }

    private Exercise buildCombinedCriteria(LearningModule module) {
        return ExerciseBuilder.of(
                        "routine-03-combined-criteria",
                        module,
                        "Criterios combinados com validacao",
                        """
                        ## Contexto

                        Regras de negocio combinam multiplas condicoes com E/OU, e antes de avaliar \
                        qualquer regra, e preciso garantir que a entrada faz sentido.

                        ## Objetivo

                        Implemente `public class AccessControl` com:

                        `String evaluateAccess(int hour, boolean isHoliday, boolean hasKeycard)`

                        - Precondicao: se `hour` for menor que 0 ou maior que 23, lance \
                        `IllegalArgumentException` ANTES de avaliar qualquer regra.
                        - Se `hasKeycard` E NAO for feriado E `hour` estiver entre 8 e 18 (inclusive), \
                        retorne `"ACESSO_LIBERADO"`.
                        - Em qualquer outro caso valido, retorne `"ACESSO_NEGADO"` (fallback).

                        ## Criterio de sucesso

                        A validacao de entrada acontece ANTES da logica de negocio, e feriado bloqueia \
                        o acesso mesmo com cartao valido e horario correto.
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
                        "INTERMEDIARIO", 2, 15)
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
                .equalsCase("cartao valido, horario comercial, sem feriado -> acesso liberado",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(10, false, true)", "\"ACESSO_LIBERADO\"", true)
                .equalsCase("sem cartao -> acesso negado mesmo no horario certo",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(10, false, false)", "\"ACESSO_NEGADO\"", true)
                .equalsCase("feriado bloqueia mesmo com cartao e horario validos",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(10, true, true)", "\"ACESSO_NEGADO\"", false)
                .throwsCase("hora invalida lanca IllegalArgumentException antes de avaliar regras",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(24, false, true)", "java.lang.IllegalArgumentException", true)
                .equalsCase("fora do horario comercial -> acesso negado",
                        "AccessControl a = new AccessControl();", "a.evaluateAccess(19, false, true)", "\"ACESSO_NEGADO\"", false)
                .hint("A validacao de hour (0-23) e a PRIMEIRA coisa que o metodo faz.")
                .hint("A regra de liberacao e um E de quatro condicoes: cartao, nao-feriado, hora >= 8, hora <= 18.")
                .build();
    }
}
