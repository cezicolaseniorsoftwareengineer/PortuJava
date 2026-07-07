package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * The "pentatonic scale" of logic track: seven core shapes (loop with state, HashMap, HashSet,
 * two pointers, sliding window, stack, queue) that cover most everyday logic problems. Each shape
 * is one graded exercise built around the mantra: input -> data structure -> state -> transition
 * rule -> output.
 */
@Component
public class LogicShapesModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public LogicShapesModuleSeeder(LearningModuleRepository moduleRepository, ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "logic-shapes";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Os 7 Shapes da Logica",
                "LOGIC_SHAPES",
                "A pentatonica da programacao: quase todo exercicio de logica e variacao de 7 " +
                        "movimentos - loop com estado, HashMap, HashSet, dois ponteiros, janela " +
                        "deslizante, pilha e fila. A frase para decorar: entrada -> estrutura de " +
                        "dados -> estado -> regra de transicao -> saida. Voce nao precisa decorar " +
                        "mil exercicios; precisa decorar esses movimentos ate a mao fazer sozinha.",
                5));

        exerciseRepository.save(buildLoopWithState(module));
        exerciseRepository.save(buildHashMapFrequency(module));
        exerciseRepository.save(buildHashSetFirstRepeated(module));
        exerciseRepository.save(buildTwoPointers(module));
        exerciseRepository.save(buildSlidingWindow(module));
        exerciseRepository.save(buildStackBrackets(module));
        exerciseRepository.save(buildQueueHotPotato(module));

        return module;
    }

    private Exercise buildLoopWithState(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-01-loop-with-state",
                        module,
                        "Shape 1: loop com estado",
                        """
                        ## Contexto

                        Este e o primeiro shape da pentatonica. O padrao mental:

                        > Eu tenho uma colecao. Eu mantenho um estado. A cada item, eu decido se \
                        atualizo esse estado. No final, o estado e a resposta.

                        A escala mestre para decorar:

                        ```java
                        for (T item : collection) {
                            // 1. observo o item
                            // 2. consulto meu estado
                            // 3. aplico uma regra
                            // 4. atualizo o estado
                        }
                        // 5. retorno o estado final
                        ```

                        Com esse unico movimento voce resolve: maior, menor, soma, media, contador, \
                        busca e validacao.

                        ## Objetivo

                        Implemente `public class ArrayStats` com tres metodos, cada um em UMA \
                        passada pelo array (os testes nunca passam array vazio):

                        - `int max(int[] nums)`: maior valor.
                        - `int min(int[] nums)`: menor valor.
                        - `int sum(int[] nums)`: soma de todos os valores.

                        ## Criterio de sucesso

                        Funciona tambem com arrays so de numeros negativos - se `max` inicializar o \
                        estado com `0` em vez de `nums[0]`, esse caso quebra.
                        """,
                        """
                        public class ArrayStats {
                            public int max(int[] nums) { return 0; }
                            public int min(int[] nums) { return 0; }
                            public int sum(int[] nums) { return 0; }
                        }
                        """,
                        """
                        public class ArrayStats {
                            public int max(int[] nums) {
                                // TODO: estado inicial = nums[0]; atualize quando n > max.
                                return 0;
                            }

                            public int min(int[] nums) {
                                // TODO: estado inicial = nums[0]; atualize quando n < min.
                                return 0;
                            }

                            public int sum(int[] nums) {
                                // TODO: estado inicial = 0; acumule cada n.
                                return 0;
                            }
                        }
                        """,
                        "INICIANTE", 0, 10)
                .referenceSolution("""
                        public class ArrayStats {
                            public int max(int[] nums) {
                                int max = nums[0];
                                for (int n : nums) {
                                    if (n > max) {
                                        max = n;
                                    }
                                }
                                return max;
                            }

                            public int min(int[] nums) {
                                int min = nums[0];
                                for (int n : nums) {
                                    if (n < min) {
                                        min = n;
                                    }
                                }
                                return min;
                            }

                            public int sum(int[] nums) {
                                int sum = 0;
                                for (int n : nums) {
                                    sum += n;
                                }
                                return sum;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "int max = nums[0];",
                        "O estado inicial e o PRIMEIRO elemento do array, nunca 0 - um array so de numeros " +
                                "negativos expoe esse erro na hora: se comecasse em 0, nunca encontraria o verdadeiro maximo negativo.")
                .solutionAnnotation(
                        "if (n > max) {\n        max = n;\n    }",
                        "O corpo do loop e uma unica decisao: compara o item atual com o estado guardado, e " +
                                "atualiza se for maior. Esse e o shape inteiro - loop, consulta o estado, decide, atualiza.")
                .equalsCase("max de {4, 2, 7, 2, 9, 4, 1, 8} e 9",
                        "ArrayStats s = new ArrayStats(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "s.max(nums)", "9", true)
                .equalsCase("min de {4, 2, 7, 2, 9, 4, 1, 8} e 1",
                        "ArrayStats s = new ArrayStats(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "s.min(nums)", "1", true)
                .equalsCase("sum de {4, 2, 7, 2, 9, 4, 1, 8} e 37",
                        "ArrayStats s = new ArrayStats(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "s.sum(nums)", "37", false)
                .equalsCase("max de array so com negativos: estado inicial errado (0) quebra aqui",
                        "ArrayStats s = new ArrayStats(); int[] nums = {-5, -2, -9};",
                        "s.max(nums)", "-2", true)
                .equalsCase("array de um unico elemento: min e o proprio elemento",
                        "ArrayStats s = new ArrayStats(); int[] nums = {7};",
                        "s.min(nums)", "7", false)
                .hint("Inicialize max e min com nums[0], nunca com 0 - um array so de negativos expoe o erro.")
                .hint("O corpo do loop e uma unica decisao: if (n > max) max = n;")
                .hint("Pergunta obrigatoria: qual estado eu mantive? Um int. Custo: tempo O(n), memoria O(1).")
                .build();
    }

    private Exercise buildHashMapFrequency(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-02-hashmap-frequency",
                        module,
                        "Shape 2: HashMap de frequencia",
                        """
                        ## Contexto

                        O shape mais importante depois do loop. O pensamento para decorar:

                        > Quando eu preciso lembrar algo que ja passou, uso HashMap ou HashSet.

                        Esse padrao resolve: contar letras, achar duplicados, agrupar por categoria, \
                        two sum, anagramas, frequencia de eventos e ranking.

                        ## Objetivo

                        Implemente `public class FrequencyCounter` com:

                        `java.util.Map<Character, Integer> frequency(String text)`

                        - Retorna um mapa de cada caractere para quantas vezes ele aparece em `text`.
                        - String vazia retorna um mapa vazio.

                        A linha central e `freq.put(c, freq.getOrDefault(c, 0) + 1);` - decore-a.

                        ## Criterio de sucesso

                        `frequency("abracadabra")` produz a=5, b=2, r=2, c=1, d=1.
                        """,
                        """
                        public class FrequencyCounter {
                            public java.util.Map<Character, Integer> frequency(String text) { return null; }
                        }
                        """,
                        """
                        public class FrequencyCounter {
                            public java.util.Map<Character, Integer> frequency(String text) {
                                // TODO: para cada char de text.toCharArray(),
                                // use getOrDefault(c, 0) + 1 para acumular a contagem.
                                return null;
                            }
                        }
                        """,
                        "INICIANTE", 1, 12)
                .referenceSolution("""
                        public class FrequencyCounter {
                            public java.util.Map<Character, Integer> frequency(String text) {
                                java.util.Map<Character, Integer> freq = new java.util.HashMap<>();
                                for (char c : text.toCharArray()) {
                                    freq.put(c, freq.getOrDefault(c, 0) + 1);
                                }
                                return freq;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "freq.put(c, freq.getOrDefault(c, 0) + 1);",
                        "getOrDefault(c, 0) devolve 0 na primeira vez que o caractere aparece, sem precisar de um " +
                                "if separado para 'ja existe ou nao' - essa linha E o padrao HashMap inteiro.")
                .equalsCase("frequency(\"abracadabra\") conta a=5, b=2, r=2, c=1, d=1",
                        "FrequencyCounter f = new FrequencyCounter();",
                        "f.frequency(\"abracadabra\")",
                        "java.util.Map.of('a', 5, 'b', 2, 'r', 2, 'c', 1, 'd', 1)", true)
                .equalsCase("frequency(\"aaa\") conta a=3",
                        "FrequencyCounter f = new FrequencyCounter();",
                        "f.frequency(\"aaa\")", "java.util.Map.of('a', 3)", true)
                .equalsCase("string vazia retorna mapa vazio",
                        "FrequencyCounter f = new FrequencyCounter();",
                        "f.frequency(\"\")", "java.util.Map.of()", false)
                .hint("Percorra com for (char c : text.toCharArray()).")
                .hint("getOrDefault(c, 0) devolve 0 na primeira vez que o char aparece - sem if de existencia.")
                .hint("Pergunta obrigatoria: usei HashMap porque preciso consultar rapido o que ja apareceu. Tempo O(n), memoria O(n).")
                .build();
    }

    private Exercise buildHashSetFirstRepeated(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-03-hashset-first-repeated",
                        module,
                        "Shape 3: HashSet para duplicidade",
                        """
                        ## Contexto

                        Decore a divisao de trabalho:

                        > HashSet responde: ja vi isso antes? \
                        HashMap responde: quantas vezes, onde, ou com qual valor?

                        Quando a pergunta e apenas "ja passou por aqui?", HashSet e mais simples e \
                        mais barato que HashMap.

                        ## Objetivo

                        Implemente `public class DuplicateFinder` com:

                        `int firstRepeated(int[] nums)`

                        - Retorna o PRIMEIRO valor que aparece pela segunda vez, percorrendo da \
                        esquerda para a direita.
                        - Retorna `-1` se nenhum valor se repete.

                        ## Criterio de sucesso

                        Em `{4, 2, 7, 2, 9, 4, 1, 8}` a resposta e `2` (o segundo `2` chega antes do \
                        segundo `4`), nao `4`.
                        """,
                        """
                        public class DuplicateFinder {
                            public int firstRepeated(int[] nums) { return -1; }
                        }
                        """,
                        """
                        public class DuplicateFinder {
                            public int firstRepeated(int[] nums) {
                                // TODO: mantenha um java.util.Set<Integer> seen;
                                // se seen.contains(n), n e o primeiro repetido; senao seen.add(n).
                                return -1;
                            }
                        }
                        """,
                        "BASICO", 2, 10)
                .referenceSolution("""
                        public class DuplicateFinder {
                            public int firstRepeated(int[] nums) {
                                java.util.Set<Integer> seen = new java.util.HashSet<>();
                                for (int n : nums) {
                                    if (seen.contains(n)) {
                                        return n;
                                    }
                                    seen.add(n);
                                }
                                return -1;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (seen.contains(n)) {\n        return n;\n    }\n    seen.add(n);",
                        "A ordem importa: CONSULTA o Set antes de adicionar. Se adicionasse primeiro, todo " +
                                "elemento pareceria repetido, porque acabou de ser inserido antes da checagem.")
                .equalsCase("primeiro repetido de {4, 2, 7, 2, 9, 4, 1, 8} e 2, nao 4",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "d.firstRepeated(nums)", "2", true)
                .equalsCase("sem repetidos retorna -1",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {1, 2, 3};",
                        "d.firstRepeated(nums)", "-1", true)
                .equalsCase("repeticao imediata: {5, 5} retorna 5",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {5, 5};",
                        "d.firstRepeated(nums)", "5", false)
                .equalsCase("{9, 4, 9, 4} retorna 9 - a ordem de chegada da segunda ocorrencia decide",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {9, 4, 9, 4};",
                        "d.firstRepeated(nums)", "9", false)
                .hint("Consulte o Set ANTES de adicionar - se adicionar primeiro, todo elemento parece repetido.")
                .hint("Retorne dentro do loop assim que achar; nao continue percorrendo.")
                .hint("Pergunta obrigatoria: o estado e o conjunto do que ja vi. Tempo O(n), memoria O(n).")
                .build();
    }

    private Exercise buildTwoPointers(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-04-two-pointers",
                        module,
                        "Shape 4: dois ponteiros",
                        """
                        ## Contexto

                        Shape classico de entrevista. O padrao mental:

                        > Se esta ordenado, eu posso andar pelas pontas. \
                        Se a soma ficou pequena, avanco a esquerda. \
                        Se a soma ficou grande, recuo a direita.

                        Isso resolve busca de pares, palindromo, merge e compactacao - sem HashMap e \
                        sem loop duplo O(n2).

                        ## Objetivo

                        Implemente `public class PairFinder` com:

                        `int[] pairWithSum(int[] sortedNums, int target)`

                        - `sortedNums` esta ordenado crescente.
                        - Retorna um array `{menor, maior}` com o par cuja soma e `target`.
                        - Retorna `new int[0]` (array vazio) se nao existir par.

                        ## Criterio de sucesso

                        Usa exatamente um ponteiro em cada ponta convergindo ao centro - uma unica \
                        passada, sem loops aninhados.
                        """,
                        """
                        public class PairFinder {
                            public int[] pairWithSum(int[] sortedNums, int target) { return new int[0]; }
                        }
                        """,
                        """
                        public class PairFinder {
                            public int[] pairWithSum(int[] sortedNums, int target) {
                                // TODO: left = 0, right = length - 1;
                                // soma menor que target -> left++; maior -> right--; igual -> achou.
                                return new int[0];
                            }
                        }
                        """,
                        "INTERMEDIARIO", 3, 15)
                .referenceSolution("""
                        public class PairFinder {
                            public int[] pairWithSum(int[] sortedNums, int target) {
                                int left = 0;
                                int right = sortedNums.length - 1;
                                while (left < right) {
                                    int sum = sortedNums[left] + sortedNums[right];
                                    if (sum == target) {
                                        return new int[]{sortedNums[left], sortedNums[right]};
                                    }
                                    if (sum < target) {
                                        left++;
                                    } else {
                                        right--;
                                    }
                                }
                                return new int[0];
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (sum < target) {\n        left++;\n    } else {\n        right--;\n    }",
                        "So um ponteiro se move por iteracao, e a direcao depende da comparacao: soma pequena " +
                                "avanca a esquerda (valores maiores), soma grande recua a direita (valores menores) - aproveitando o array ja ordenado.")
                .equalsCase("em {1, 2, 4, 7, 8, 9} com target 10, o par e {1, 9}",
                        "PairFinder p = new PairFinder(); int[] nums = {1, 2, 4, 7, 8, 9};",
                        "p.pairWithSum(nums, 10)", "new int[]{1, 9}", true)
                .equalsCase("com target 12 os ponteiros convergem ate {4, 8}",
                        "PairFinder p = new PairFinder(); int[] nums = {1, 2, 4, 7, 8, 9};",
                        "p.pairWithSum(nums, 12)", "new int[]{4, 8}", true)
                .equalsCase("sem par possivel retorna array vazio",
                        "PairFinder p = new PairFinder(); int[] nums = {1, 2, 4, 7, 8, 9};",
                        "p.pairWithSum(nums, 100)", "new int[0]", false)
                .equalsCase("array vazio retorna array vazio sem lancar excecao",
                        "PairFinder p = new PairFinder(); int[] nums = {};",
                        "p.pairWithSum(nums, 5)", "new int[0]", false)
                .hint("Condicao do while e left < right - quando se cruzam, nao ha mais pares para testar.")
                .hint("So um dos ponteiros se move por iteracao, e a direcao depende de sum comparado a target.")
                .hint("Pergunta obrigatoria: a ordenacao e o que permite descartar metade das combinacoes. Tempo O(n), memoria O(1).")
                .build();
    }

    private Exercise buildSlidingWindow(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-05-sliding-window",
                        module,
                        "Shape 5: janela deslizante",
                        """
                        ## Contexto

                        A ideia para decorar:

                        > Eu nao recalculo tudo. Eu adiciono quem entrou e removo quem saiu.

                        Recalcular a soma de cada janela do zero custa O(n*k). Deslizar a janela \
                        (somar quem entra, subtrair quem sai) custa O(n). E o shape que transforma \
                        solucoes lentas em rapidas.

                        ## Objetivo

                        Implemente `public class WindowScanner` com:

                        `int maxWindowSum(int[] nums, int k)`

                        - Retorna a maior soma entre todas as janelas de `k` elementos consecutivos.
                        - Os testes garantem `1 <= k <= nums.length`.

                        ## Criterio de sucesso

                        Em `{4, 2, 7, 2, 9, 4, 1, 8}` com `k = 3`, a janela `{7, 2, 9}` vence com \
                        soma `18`. A solucao deve atualizar a soma incrementalmente, nao recalcular \
                        cada janela.
                        """,
                        """
                        public class WindowScanner {
                            public int maxWindowSum(int[] nums, int k) { return 0; }
                        }
                        """,
                        """
                        public class WindowScanner {
                            public int maxWindowSum(int[] nums, int k) {
                                // TODO: some os k primeiros; depois, para cada i a partir de k,
                                // windowSum += nums[i] - nums[i - k] e guarde o maximo.
                                return 0;
                            }
                        }
                        """,
                        "INTERMEDIARIO", 4, 15)
                .referenceSolution("""
                        public class WindowScanner {
                            public int maxWindowSum(int[] nums, int k) {
                                int windowSum = 0;
                                for (int i = 0; i < k; i++) {
                                    windowSum += nums[i];
                                }
                                int maxSum = windowSum;
                                for (int i = k; i < nums.length; i++) {
                                    windowSum += nums[i];
                                    windowSum -= nums[i - k];
                                    maxSum = Math.max(maxSum, windowSum);
                                }
                                return maxSum;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "windowSum += nums[i];\n                    windowSum -= nums[i - k];",
                        "Em vez de recalcular a soma inteira da janela a cada passo, soma quem ENTRA (nums[i]) e " +
                                "subtrai quem SAI (nums[i - k]) - essa unica troca transforma O(n*k) em O(n).")
                .equalsCase("maior soma de 3 consecutivos em {4, 2, 7, 2, 9, 4, 1, 8} e 18",
                        "WindowScanner w = new WindowScanner(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "w.maxWindowSum(nums, 3)", "18", true)
                .equalsCase("k = 1 degenera para o maior elemento: 9",
                        "WindowScanner w = new WindowScanner(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "w.maxWindowSum(nums, 1)", "9", true)
                .equalsCase("k = nums.length e a soma total: 37",
                        "WindowScanner w = new WindowScanner(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "w.maxWindowSum(nums, 8)", "37", false)
                .equalsCase("funciona com negativos: {5, -2, 3} com k = 2 da 3",
                        "WindowScanner w = new WindowScanner(); int[] nums = {5, -2, 3};",
                        "w.maxWindowSum(nums, 2)", "3", false)
                .hint("Primeira janela: um loop simples somando nums[0..k-1].")
                .hint("A cada deslize, quem entra e nums[i] e quem sai e nums[i - k].")
                .hint("Pergunta obrigatoria: o estado e a soma da janela atual mais o maximo visto. Tempo O(n), memoria O(1).")
                .build();
    }

    private Exercise buildStackBrackets(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-06-stack-brackets",
                        module,
                        "Shape 6: pilha",
                        """
                        ## Contexto

                        Pilha e para "ultimo que entrou, primeiro que sai" (LIFO). Ela resolve \
                        parenteses, historico, desfazer, recursao simulada, backtracking e avaliacao \
                        de expressoes.

                        No problema dos parenteses, o insight e: quando um fechamento chega, ele \
                        DEVE casar com a abertura mais recente ainda aberta - e "mais recente" e \
                        exatamente o topo da pilha.

                        ## Objetivo

                        Implemente `public class BracketValidator` usando \
                        `java.util.Deque<Character>` (via `new java.util.ArrayDeque<>()`) com:

                        `boolean isBalanced(String s)`

                        - `s` contem apenas os caracteres `()[]{}`.
                        - Retorna `true` se cada fechamento casa com a abertura correta e nada fica \
                        aberto no final.

                        ## Criterio de sucesso

                        Cobre os tres jeitos de falhar: fechamento errado (`"(]"`), fechamento sem \
                        abertura (`")"`), e abertura sem fechamento (`"(("`).
                        """,
                        """
                        public class BracketValidator {
                            public boolean isBalanced(String s) { return false; }
                        }
                        """,
                        """
                        public class BracketValidator {
                            public boolean isBalanced(String s) {
                                // TODO: abertura -> push; fechamento -> pilha vazia e invalido,
                                // senao pop e confira o par. No final, valido se a pilha esvaziou.
                                return false;
                            }
                        }
                        """,
                        "INTERMEDIARIO", 5, 18)
                .referenceSolution("""
                        public class BracketValidator {
                            public boolean isBalanced(String s) {
                                java.util.Deque<Character> stack = new java.util.ArrayDeque<>();
                                for (char c : s.toCharArray()) {
                                    if (c == '(' || c == '[' || c == '{') {
                                        stack.push(c);
                                    } else {
                                        if (stack.isEmpty()) {
                                            return false;
                                        }
                                        char open = stack.pop();
                                        boolean match =
                                                open == '(' && c == ')' ||
                                                open == '[' && c == ']' ||
                                                open == '{' && c == '}';
                                        if (!match) {
                                            return false;
                                        }
                                    }
                                }
                                return stack.isEmpty();
                            }
                        }
                        """)
                .solutionAnnotation(
                        "if (stack.isEmpty()) {\n                            return false;\n                        }",
                        "Um fechamento chegando com a pilha vazia significa que nao ha nenhuma abertura pendente " +
                                "para ele casar - e invalido antes mesmo de comparar o tipo de parentese.")
                .solutionAnnotation(
                        "return stack.isEmpty();",
                        "No final, so e valido se a pilha ESVAZIOU - se sobrou alguma abertura sem fechamento " +
                                "correspondente, ainda ha algo pendente.")
                .equalsCase("\"({[]})\" e balanceada",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"({[]})\")", "true", true)
                .equalsCase("\"(]\" falha: fechamento nao casa com o topo da pilha",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"(]\")", "false", true)
                .equalsCase("\"((\" falha: sobrou abertura na pilha no final",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"((\")", "false", false)
                .equalsCase("\")\" falha: fechamento com pilha vazia",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\")\")", "false", false)
                .equalsCase("string vazia e balanceada",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"\")", "true", false)
                .hint("Aberturas sempre entram na pilha; fechamentos sempre tentam um pop.")
                .hint("Nao esqueca os dois casos de borda: pop com pilha vazia e pilha nao vazia no final.")
                .hint("Pergunta obrigatoria: a pilha guarda as aberturas pendentes em ordem LIFO. Tempo O(n), memoria O(n).")
                .build();
    }

    private Exercise buildQueueHotPotato(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-07-queue-hot-potato",
                        module,
                        "Shape 7: fila",
                        """
                        ## Contexto

                        Fila e para processar em ordem de chegada (FIFO): BFS, eventos, mensageria, \
                        tarefas e simulacao. Este exercicio usa a fila como SIMULACAO - o classico \
                        "batata quente": jogadores em circulo, a cada rodada conta-se `step` \
                        posicoes e quem parar na contagem sai do jogo.

                        O truque: um circulo se simula com fila fazendo quem esta na frente voltar \
                        para o fim (`offer(poll())`).

                        ## Objetivo

                        Implemente `public class TurnSimulator` usando `java.util.Queue<String>` \
                        (via `new java.util.ArrayDeque<>()`) com:

                        `String lastRemaining(String[] players, int step)`

                        - Enfileire todos os jogadores na ordem dada.
                        - Enquanto houver mais de um: mova `step - 1` jogadores da frente para o \
                        fim, depois remova o da frente (eliminado).
                        - Retorna o ultimo jogador restante. Os testes garantem `step >= 1`.

                        ## Criterio de sucesso

                        Com `{"A", "B", "C", "D", "E"}` e `step = 2`, a eliminacao e B, D, A, E - \
                        sobra `"C"`.
                        """,
                        """
                        public class TurnSimulator {
                            public String lastRemaining(String[] players, int step) { return null; }
                        }
                        """,
                        """
                        public class TurnSimulator {
                            public String lastRemaining(String[] players, int step) {
                                // TODO: enfileire todos; enquanto size() > 1,
                                // rotacione step - 1 vezes com offer(poll()) e elimine com poll().
                                return null;
                            }
                        }
                        """,
                        "INTERMEDIARIO", 6, 15)
                .referenceSolution("""
                        public class TurnSimulator {
                            public String lastRemaining(String[] players, int step) {
                                java.util.Queue<String> queue = new java.util.ArrayDeque<>();
                                for (String player : players) {
                                    queue.offer(player);
                                }
                                while (queue.size() > 1) {
                                    for (int i = 0; i < step - 1; i++) {
                                        queue.offer(queue.poll());
                                    }
                                    queue.poll();
                                }
                                return queue.poll();
                            }
                        }
                        """)
                .solutionAnnotation(
                        "queue.offer(queue.poll());",
                        "Essa unica linha simula o circulo: tira quem esta na frente e recoloca no fim da fila - " +
                                "e a 'rotacao' que faz uma fila linear se comportar como um circulo de jogadores.")
                .solutionAnnotation(
                        "queue.poll();\n                }\n                return queue.poll();",
                        "O poll() de dentro do while elimina o jogador; o poll() final (fora do loop) retorna " +
                                "quem sobrou, quando a fila chega a ter so um jogador.")
                .equalsCase("{A, B, C, D, E} com step 2: elimina B, D, A, E - sobra C",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"A\", \"B\", \"C\", \"D\", \"E\"};",
                        "t.lastRemaining(players, 2)", "\"C\"", true)
                .equalsCase("step 1 elimina sempre o da frente - sobra o ultimo da fila original",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"A\", \"B\", \"C\", \"D\", \"E\"};",
                        "t.lastRemaining(players, 1)", "\"E\"", true)
                .equalsCase("um unico jogador ja e o vencedor",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"solo\"};",
                        "t.lastRemaining(players, 3)", "\"solo\"", false)
                .equalsCase("sete jogadores com step 3: sobrevive D",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\"};",
                        "t.lastRemaining(players, 3)", "\"D\"", false)
                .hint("offer(poll()) move o jogador da frente para o fim - e a rotacao do circulo.")
                .hint("A cada rodada: step - 1 rotacoes, depois um poll() que elimina.")
                .hint("Pergunta obrigatoria: a fila E o circulo de jogadores; a ordem FIFO preserva os turnos. Tempo O(n * step), memoria O(n).")
                .build();
    }
}
