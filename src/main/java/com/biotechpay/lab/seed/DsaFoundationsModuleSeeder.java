package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * Algorithms/data-structures track stub: recursion, choosing the right structure (stack vs queue),
 * and binary search. Sorting and graphs (covered conceptually in the old typing-game curriculum) are
 * explicit backlog for a later session as real graded implementations, not silently dropped.
 */
@Component
public class DsaFoundationsModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public DsaFoundationsModuleSeeder(LearningModuleRepository moduleRepository, ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "dsa-foundations";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Algoritmos e Estruturas de Dados",
                "DSA",
                "Escolher a estrutura de dados certa e pensar recursivamente são habilidades que " +
                        "aparecem em qualquer sistema sério, de bancos a bigtechs. Aqui você implementa " +
                        "e prova por teste, não só lê sobre o assunto.",
                4));

        exerciseRepository.save(buildRecursion(module));
        exerciseRepository.save(buildStackQueueChoice(module));
        exerciseRepository.save(buildBinarySearch(module));

        return module;
    }

    private Exercise buildRecursion(LearningModule module) {
        return ExerciseBuilder.of(
                        "dsa-01-recursion-factorial",
                        module,
                        "Recursão: fatorial",
                        """
                        ## Contexto

                        Recursão é quando um método resolve um problema chamando a si mesmo com uma \
                        versão menor do mesmo problema, até chegar num caso base que não precisa de \
                        mais chamadas.

                        ## Objetivo

                        Implemente `public class MathOps` com:

                        `long factorial(int n)`

                        - Caso base: `factorial(0)` e `factorial(1)` retornam `1`.
                        - Caso recursivo: `factorial(n) = n * factorial(n - 1)` para `n > 1`.

                        ## Critério de sucesso

                        A implementação deve ser genuinamente recursiva (chamar `factorial` dentro de \
                        `factorial`), não um loop disfarcado - ambos funcionam matematicamente, mas o \
                        objetivo aqui é praticar o padrão de recursão.
                        """,
                        """
                        public class MathOps {
                            public long factorial(int n) { return 0; }
                        }
                        """,
                        """
                        public class MathOps {
                            public long factorial(int n) {
                                // TODO: caso base (n <= 1) retorna 1; caso recursivo retorna n * factorial(n - 1).
                                return 0;
                            }
                        }
                        """,
                        "INICIANTE", 0, 10)
                .referenceSolution("""
                        public class MathOps {
                            public long factorial(int n) {
                                if (n <= 1) {
                                    return 1;
                                }
                                return n * factorial(n - 1);
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (n <= 1) {\n        return 1;\n    }",
                        "O caso base é o que impede a recursão de nunca terminar - sem ele, factorial(n - 1) " +
                                "chamaria a si mesmo para sempre.")
                .solutionAnnotation(
                        "return n * factorial(n - 1);",
                        "O caso recursivo resolve o problema chamando a si mesmo com uma versão MENOR (n - 1) - " +
                                "cada chamada se aproxima um passo do caso base.")
                .equalsCase("factorial(0) é o caso base, resultado 1",
                        "MathOps m = new MathOps();", "m.factorial(0)", "1L", true)
                .equalsCase("factorial(1) também é caso base, resultado 1",
                        "MathOps m = new MathOps();", "m.factorial(1)", "1L", false)
                .equalsCase("factorial(5) usa o caso recursivo: 120",
                        "MathOps m = new MathOps();", "m.factorial(5)", "120L", true)
                .equalsCase("factorial(10) confirma a recursão em uma cadeia mais longa",
                        "MathOps m = new MathOps();", "m.factorial(10)", "3628800L", false)
                .hint("O caso base cobre n <= 1, retornando 1 diretamente (sem chamar factorial de novo).")
                .hint("O caso recursivo é: return n * factorial(n - 1);")
                .build();
    }

    private Exercise buildStackQueueChoice(LearningModule module) {
        return ExerciseBuilder.of(
                        "dsa-02-stack-queue-choice",
                        module,
                        "Escolhendo pilha ou fila",
                        """
                        ## Contexto

                        "Desfazer" (undo) e "fila de impressão" parecem parecidos (uma lista de ações \
                        pendentes), mas exigem estruturas OPOSTAS: undo é LIFO (a última ação é a \
                        primeira desfeita), fila de impressão é FIFO (o primeiro documento enviado é o \
                        primeiro impresso).

                        ## Objetivo

                        Implemente `public class TaskHistory` usando `java.util.Deque<String>` \
                        (via `new java.util.ArrayDeque<>()`) com:

                        - `String lastUndo(String[] actions)`: empilha todas as ações (`push`) e \
                        retorna a que seria desfeita primeiro (`pop`) - a ÚLTIMA adicionada.
                        - `String firstPrintJob(String[] jobs)`: enfileira todos os jobs (`offer`) e \
                        retorna o primeiro a ser impresso (`poll`) - o PRIMEIRO adicionado.

                        ## Critério de sucesso

                        `lastUndo` e `firstPrintJob` devem retornar elementos DIFERENTES do array \
                        quando há mais de um elemento - um pega o último, o outro pega o primeiro.
                        """,
                        """
                        public class TaskHistory {
                            public String lastUndo(String[] actions) { return null; }
                            public String firstPrintJob(String[] jobs) { return null; }
                        }
                        """,
                        """
                        public class TaskHistory {
                            public String lastUndo(String[] actions) {
                                // TODO: empilhe tudo com push, depois retorne stack.pop() - LIFO.
                                return null;
                            }

                            public String firstPrintJob(String[] jobs) {
                                // TODO: enfileire tudo com offer, depois retorne queue.poll() - FIFO.
                                return null;
                            }
                        }
                        """,
                        "BÁSICO", 1, 12)
                .referenceSolution("""
                        public class TaskHistory {
                            public String lastUndo(String[] actions) {
                                java.util.Deque<String> stack = new java.util.ArrayDeque<>();
                                for (String action : actions) {
                                    stack.push(action);
                                }
                                return stack.pop();
                            }

                            public String firstPrintJob(String[] jobs) {
                                java.util.Deque<String> queue = new java.util.ArrayDeque<>();
                                for (String job : jobs) {
                                    queue.offer(job);
                                }
                                return queue.poll();
                            }
                        }
                        """)
                .solutionAnnotation(
                        "stack.push(action);\n    }\n    return stack.pop();",
                        "push/pop é LIFO - o último elemento empilhado é o primeiro a sair. Por isso lastUndo " +
                                "devolve a ÚLTIMA ação, não a primeira.")
                .solutionAnnotation(
                        "queue.offer(job);\n    }\n    return queue.poll();",
                        "offer/poll é FIFO - o primeiro elemento enfileirado é o primeiro a sair. Mesma estrutura " +
                                "Deque, comportamento oposto, dependendo de quais métodos você chama.")
                .equalsCase("lastUndo retorna a ÚLTIMA ação (LIFO)",
                        "TaskHistory t = new TaskHistory(); String[] actions = {\"type\", \"delete\", \"paste\"};",
                        "t.lastUndo(actions)", "\"paste\"", true)
                .equalsCase("firstPrintJob retorna o PRIMEIRO job (FIFO)",
                        "TaskHistory t = new TaskHistory(); String[] jobs = {\"doc1\", \"doc2\", \"doc3\"};",
                        "t.firstPrintJob(jobs)", "\"doc1\"", true)
                .equalsCase("pilha com um único elemento retorna esse elemento",
                        "TaskHistory t = new TaskHistory(); String[] actions = {\"solo\"};",
                        "t.lastUndo(actions)", "\"solo\"", false)
                .equalsCase("fila com um único elemento retorna esse elemento",
                        "TaskHistory t = new TaskHistory(); String[] jobs = {\"solo\"};",
                        "t.firstPrintJob(jobs)", "\"solo\"", false)
                .hint("Para LIFO: Deque.push() empilha, Deque.pop() desempilha do topo.")
                .hint("Para FIFO: Deque.offer() enfileira, Deque.poll() remove do início.")
                .build();
    }

    private Exercise buildBinarySearch(LearningModule module) {
        return ExerciseBuilder.of(
                        "dsa-03-binary-search",
                        module,
                        "Busca binária",
                        """
                        ## Contexto

                        Buscar um valor num array ORDENADO não precisa olhar elemento por elemento - \
                        dividir o intervalo pela metade a cada passo torna a busca exponencialmente \
                        mais rápida.

                        ## Objetivo

                        Implemente `public class Search` com:

                        `int binarySearch(int[] sortedArr, int target)`

                        - Retorna o índice de `target` no array (assumido ordenado crescente).
                        - Retorna `-1` se `target` não existir no array.

                        ## Critério de sucesso

                        Funciona para o alvo no início, no meio, no fim, ausente, e para array vazio.
                        """,
                        """
                        public class Search {
                            public int binarySearch(int[] sortedArr, int target) { return -1; }
                        }
                        """,
                        """
                        public class Search {
                            public int binarySearch(int[] sortedArr, int target) {
                                // TODO: left/right ponteiros, calcule o meio, compare e descarte metade;
                                // retorne o índice se encontrar, mantenha -1 se sair do loop sem achar.
                                return -1;
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 2, 15)
                .referenceSolution("""
                        public class Search {
                            public int binarySearch(int[] sortedArr, int target) {
                                int left = 0;
                                int right = sortedArr.length - 1;
                                while (left <= right) {
                                    int mid = left + (right - left) / 2;
                                    if (sortedArr[mid] == target) {
                                        return mid;
                                    } else if (sortedArr[mid] < target) {
                                        left = mid + 1;
                                    } else {
                                        right = mid - 1;
                                    }
                                }
                                return -1;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "int mid = left + (right - left) / 2;",
                        "Essa forma de calcular o meio evita overflow em arrays muito grandes - (left + right) / 2 " +
                                "poderia estourar o int se ambos forem grandes; assim, nunca soma dois valores grandes diretamente.")
                .solutionAnnotation(
                        "} else if (sortedArr[mid] < target) {\n        left = mid + 1;\n    } else {\n        right = mid - 1;\n    }",
                        "A cada iteração, metade do array é descartada com base numa única comparação - é isso " +
                                "que torna a busca binária O(log n) em vez de O(n).")
                .equalsCase("encontra o alvo no início do array",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 10)", "0", true)
                .equalsCase("encontra o alvo no meio do array",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 30)", "2", true)
                .equalsCase("encontra o alvo no fim do array",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 50)", "4", false)
                .equalsCase("alvo ausente retorna -1",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 25)", "-1", true)
                .equalsCase("array vazio retorna -1",
                        "Search s = new Search(); int[] arr = {};", "s.binarySearch(arr, 5)", "-1", false)
                .hint("left começa em 0, right começa em sortedArr.length - 1.")
                .hint("mid = left + (right - left) / 2 evita overflow em arrays muito grandes.")
                .hint("Se sortedArr[mid] < target, descarte a metade esquerda (left = mid + 1); senão, descarte a direita.")
                .build();
    }
}
