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
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                "dsa-foundations",
                "Algoritmos e Estruturas de Dados",
                "DSA",
                "Escolher a estrutura de dados certa e pensar recursivamente sao habilidades que " +
                        "aparecem em qualquer sistema serio, de bancos a bigtechs. Aqui voce implementa " +
                        "e prova por teste, nao so le sobre o assunto.",
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
                        "Recursao: fatorial",
                        """
                        ## Contexto

                        Recursao e quando um metodo resolve um problema chamando a si mesmo com uma \
                        versao menor do mesmo problema, ate chegar num caso base que nao precisa de \
                        mais chamadas.

                        ## Objetivo

                        Implemente `public class MathOps` com:

                        `long factorial(int n)`

                        - Caso base: `factorial(0)` e `factorial(1)` retornam `1`.
                        - Caso recursivo: `factorial(n) = n * factorial(n - 1)` para `n > 1`.

                        ## Criterio de sucesso

                        A implementacao deve ser genuinamente recursiva (chamar `factorial` dentro de \
                        `factorial`), nao um loop disfarcado - ambos funcionam matematicamente, mas o \
                        objetivo aqui e praticar o padrao de recursao.
                        """,
                        """
                        public class MathOps {
                            public long factorial(int n) { return 0; }
                        }
                        """,
                        """
                        public class MathOps {
                            public long factorial(int n) {
                                // TODO: caso base (n <= 1) e caso recursivo (n * factorial(n - 1)).
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
                .equalsCase("factorial(0) e o caso base, resultado 1",
                        "MathOps m = new MathOps();", "m.factorial(0)", "1L", true)
                .equalsCase("factorial(1) tambem e caso base, resultado 1",
                        "MathOps m = new MathOps();", "m.factorial(1)", "1L", false)
                .equalsCase("factorial(5) usa o caso recursivo: 120",
                        "MathOps m = new MathOps();", "m.factorial(5)", "120L", true)
                .equalsCase("factorial(10) confirma a recursao em uma cadeia mais longa",
                        "MathOps m = new MathOps();", "m.factorial(10)", "3628800L", false)
                .hint("O caso base cobre n <= 1, retornando 1 diretamente (sem chamar factorial de novo).")
                .hint("O caso recursivo e: return n * factorial(n - 1);")
                .build();
    }

    private Exercise buildStackQueueChoice(LearningModule module) {
        return ExerciseBuilder.of(
                        "dsa-02-stack-queue-choice",
                        module,
                        "Escolhendo pilha ou fila",
                        """
                        ## Contexto

                        "Desfazer" (undo) e "fila de impressao" parecem parecidos (uma lista de acoes \
                        pendentes), mas exigem estruturas OPOSTAS: undo e LIFO (a ultima acao e a \
                        primeira desfeita), fila de impressao e FIFO (o primeiro documento enviado e o \
                        primeiro impresso).

                        ## Objetivo

                        Implemente `public class TaskHistory` usando `java.util.Deque<String>` \
                        (via `new java.util.ArrayDeque<>()`) com:

                        - `String lastUndo(String[] actions)`: empilha todas as acoes (`push`) e \
                        retorna a que seria desfeita primeiro (`pop`) - a ULTIMA adicionada.
                        - `String firstPrintJob(String[] jobs)`: enfileira todos os jobs (`offer`) e \
                        retorna o primeiro a ser impresso (`poll`) - o PRIMEIRO adicionado.

                        ## Criterio de sucesso

                        `lastUndo` e `firstPrintJob` devem retornar elementos DIFERENTES do array \
                        quando ha mais de um elemento - um pega o ultimo, o outro pega o primeiro.
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
                                // TODO: use Deque como pilha (push/pop) - LIFO.
                                return null;
                            }

                            public String firstPrintJob(String[] jobs) {
                                // TODO: use Deque como fila (offer/poll) - FIFO.
                                return null;
                            }
                        }
                        """,
                        "BASICO", 1, 12)
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
                .equalsCase("lastUndo retorna a ULTIMA acao (LIFO)",
                        "TaskHistory t = new TaskHistory(); String[] actions = {\"type\", \"delete\", \"paste\"};",
                        "t.lastUndo(actions)", "\"paste\"", true)
                .equalsCase("firstPrintJob retorna o PRIMEIRO job (FIFO)",
                        "TaskHistory t = new TaskHistory(); String[] jobs = {\"doc1\", \"doc2\", \"doc3\"};",
                        "t.firstPrintJob(jobs)", "\"doc1\"", true)
                .equalsCase("pilha com um unico elemento retorna esse elemento",
                        "TaskHistory t = new TaskHistory(); String[] actions = {\"solo\"};",
                        "t.lastUndo(actions)", "\"solo\"", false)
                .equalsCase("fila com um unico elemento retorna esse elemento",
                        "TaskHistory t = new TaskHistory(); String[] jobs = {\"solo\"};",
                        "t.firstPrintJob(jobs)", "\"solo\"", false)
                .hint("Para LIFO: Deque.push() empilha, Deque.pop() desempilha do topo.")
                .hint("Para FIFO: Deque.offer() enfileira, Deque.poll() remove do inicio.")
                .build();
    }

    private Exercise buildBinarySearch(LearningModule module) {
        return ExerciseBuilder.of(
                        "dsa-03-binary-search",
                        module,
                        "Busca binaria",
                        """
                        ## Contexto

                        Buscar um valor num array ORDENADO nao precisa olhar elemento por elemento - \
                        dividir o intervalo pela metade a cada passo torna a busca exponencialmente \
                        mais rapida.

                        ## Objetivo

                        Implemente `public class Search` com:

                        `int binarySearch(int[] sortedArr, int target)`

                        - Retorna o indice de `target` no array (assumido ordenado crescente).
                        - Retorna `-1` se `target` nao existir no array.

                        ## Criterio de sucesso

                        Funciona para o alvo no inicio, no meio, no fim, ausente, e para array vazio.
                        """,
                        """
                        public class Search {
                            public int binarySearch(int[] sortedArr, int target) { return -1; }
                        }
                        """,
                        """
                        public class Search {
                            public int binarySearch(int[] sortedArr, int target) {
                                // TODO: left/right ponteiros, calcule o meio, compare e descarte metade.
                                return -1;
                            }
                        }
                        """,
                        "INTERMEDIARIO", 2, 15)
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
                .equalsCase("encontra o alvo no inicio do array",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 10)", "0", true)
                .equalsCase("encontra o alvo no meio do array",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 30)", "2", true)
                .equalsCase("encontra o alvo no fim do array",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 50)", "4", false)
                .equalsCase("alvo ausente retorna -1",
                        "Search s = new Search(); int[] arr = {10, 20, 30, 40, 50};", "s.binarySearch(arr, 25)", "-1", true)
                .equalsCase("array vazio retorna -1",
                        "Search s = new Search(); int[] arr = {};", "s.binarySearch(arr, 5)", "-1", false)
                .hint("left comeca em 0, right comeca em sortedArr.length - 1.")
                .hint("mid = left + (right - left) / 2 evita overflow em arrays muito grandes.")
                .hint("Se sortedArr[mid] < target, descarte a metade esquerda (left = mid + 1); senao, descarte a direita.")
                .build();
    }
}
