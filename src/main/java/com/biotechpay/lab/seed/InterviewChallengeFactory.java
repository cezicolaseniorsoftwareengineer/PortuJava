package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.seed.support.ExerciseBuilder;

import java.util.List;

/** Shared authoring format for original interview-style challenges. */
final class InterviewChallengeFactory {

    record Example(String input, String expected, boolean visible) {}

    private InterviewChallengeFactory() {}

    static Example example(String input, String expected, boolean visible) {
        return new Example(input, expected, visible);
    }

    static Exercise challenge(LearningModule module, int number, String slug, String title,
                              String level, String pattern, String objective, String complexity,
                              String solutionBody, List<Example> examples) {
        String className = "InterviewChallenge%02d".formatted(number);
        String statement = """
                ## Desafio original de entrevista

                **Padrão treinado:** %s

                %s

                Implemente `solve(String input)` e devolva exatamente o formato descrito pelos
                exemplos. Antes de codificar, explique para si mesmo: contrato, casos extremos,
                estrutura de dados escolhida e custo. Depois de passar, compare uma alternativa.

                **Meta de complexidade:** %s

                Este problema foi escrito para o PortuJava a partir de padrões públicos e comuns de
                avaliação técnica. Ele não reproduz enunciado proprietário nem afirma ter sido usado
                literalmente por uma empresa específica.
                """.formatted(pattern, objective, complexity);
        String contract = """
                public final class %s {
                    public String solve(String input) { return null; }
                }
                """.formatted(className);
        String starter = """
                public final class %s {
                    public String solve(String input) {
                        // TODO: parse the contract, choose the data structure and return the result.
                        return null;
                    }
                }
                """.formatted(className);
        String solution = """
                public final class %s {
                    public String solve(String input) {
                %s
                    }
                }
                """.formatted(className, indent(solutionBody, 8));

        ExerciseBuilder builder = ExerciseBuilder.of(
                        "interview-%02d-%s".formatted(number, slug), module,
                        "%02d. %s".formatted(number, title), statement, contract, starter,
                        level, number - 1, estimatedMinutes(level))
                .referenceSolution(solution)
                .solutionAnnotation(solutionBody.lines().findFirst().orElse(""),
                        "A solução de referência privilegia o padrão declarado e mantém o contrato de saída determinístico.")
                .hint("Escreva primeiro dois exemplos à mão e identifique o estado mínimo necessário.")
                .hint("Meça a complexidade da operação dominante; não use uma estrutura mais forte sem necessidade.")
                .hint("Teste entrada vazia, repetição, limites e ordem determinística da resposta.");
        int index = 0;
        for (Example example : examples) {
            builder.equalsCase(index == 0 ? "resolve o exemplo principal" : "preserva o contrato em caso adversarial",
                    "", "new %s().solve(%s)".formatted(className, literal(example.input())),
                    literal(example.expected()), example.visible());
            index++;
        }
        return builder.build();
    }

    private static String indent(String code, int spaces) {
        String prefix = " ".repeat(spaces);
        return code.lines().map(line -> prefix + line).reduce((a, b) -> a + "\n" + b).orElse("");
    }

    private static String literal(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n") + "\"";
    }

    private static int estimatedMinutes(String level) {
        return switch (level) {
            case "BÁSICO" -> 20;
            case "MÉDIO" -> 30;
            case "INTERMEDIÁRIO" -> 45;
            default -> 60;
        };
    }
}
