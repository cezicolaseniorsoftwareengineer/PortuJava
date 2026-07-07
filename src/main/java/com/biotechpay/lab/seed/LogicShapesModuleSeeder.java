package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * Core logic patterns track: seven recurring patterns (loop with state, HashMap, HashSet,
 * two pointers, sliding window, stack, queue) that cover most everyday logic problems. Each
 * pattern is one graded exercise built around the same structure: input -> data structure ->
 * state -> transition rule -> output.
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
                "Os 7 Padrões Fundamentais de Lógica",
                "LOGIC_SHAPES",
                "Quase todo exercício de lógica de programação é uma variação de 7 padrões: loop " +
                        "com estado, HashMap, HashSet, dois ponteiros, janela deslizante, pilha e " +
                        "fila. O raciocínio se repete sempre: entrada -> estrutura de dados -> " +
                        "estado -> regra de transição -> saída. Você não precisa memorizar mil " +
                        "exercícios diferentes; precisa reconhecer esses 7 padrões e aplicar cada " +
                        "um até virar automático.",
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
                        "Padrão 1: loop com estado",
                        """
                        ## Contexto

                        Este é o primeiro dos 7 padrões. O raciocínio:

                        > Eu tenho uma coleção. Eu mantenho um estado. A cada item, eu decido se \
                        atualizo esse estado. No final, o estado é a resposta.

                        O modelo para aplicar sempre:

                        ```java
                        for (T item : collection) {
                            // 1. observo o item
                            // 2. consulto meu estado
                            // 3. aplico uma regra
                            // 4. atualizo o estado
                        }
                        // 5. retorno o estado final
                        ```

                        Com esse único padrão você resolve: maior, menor, soma, média, contador, \
                        busca e validação.

                        ## Objetivo

                        Implemente `public class ArrayStats` com três métodos, cada um em UMA \
                        passada pelo array (os testes nunca passam array vazio):

                        - `int max(int[] nums)`: maior valor.
                        - `int min(int[] nums)`: menor valor.
                        - `int sum(int[] nums)`: soma de todos os valores.

                        ## Critério de sucesso

                        Funciona também com arrays só de números negativos - se `max` inicializar o \
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
                        "O estado inicial é o PRIMEIRO elemento do array, nunca 0 - um array só de números " +
                                "negativos expõe esse erro na hora: se começasse em 0, nunca encontraria o verdadeiro máximo negativo.")
                .solutionAnnotation(
                        "if (n > max) {\n        max = n;\n    }",
                        "O corpo do loop é uma única decisão: compara o item atual com o estado guardado, e " +
                                "atualiza se for maior. Esse é o padrão inteiro - loop, consulta o estado, decide, atualiza.")
                .equalsCase("max de {4, 2, 7, 2, 9, 4, 1, 8} é 9",
                        "ArrayStats s = new ArrayStats(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "s.max(nums)", "9", true)
                .equalsCase("min de {4, 2, 7, 2, 9, 4, 1, 8} é 1",
                        "ArrayStats s = new ArrayStats(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "s.min(nums)", "1", true)
                .equalsCase("sum de {4, 2, 7, 2, 9, 4, 1, 8} é 37",
                        "ArrayStats s = new ArrayStats(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "s.sum(nums)", "37", false)
                .equalsCase("max de array só com negativos: estado inicial errado (0) quebra aqui",
                        "ArrayStats s = new ArrayStats(); int[] nums = {-5, -2, -9};",
                        "s.max(nums)", "-2", true)
                .equalsCase("array de um único elemento: min é o próprio elemento",
                        "ArrayStats s = new ArrayStats(); int[] nums = {7};",
                        "s.min(nums)", "7", false)
                .hint("Inicialize max e min com nums[0], nunca com 0 - um array só de negativos expõe o erro.")
                .hint("O corpo do loop é uma única decisão: if (n > max) max = n;")
                .hint("Pergunta obrigatória: qual estado eu mantive? Um int. Custo: tempo O(n), memória O(1).")
                .build();
    }

    private Exercise buildHashMapFrequency(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-02-hashmap-frequency",
                        module,
                        "Padrão 2: HashMap de frequência",
                        """
                        ## Contexto

                        O padrão mais importante depois do loop. O raciocínio:

                        > Quando eu preciso lembrar algo que já passou, uso HashMap ou HashSet.

                        Esse padrão resolve: contar letras, achar duplicados, agrupar por categoria, \
                        two sum, anagramas, frequência de eventos e ranking.

                        ## Objetivo

                        Implemente `public class FrequencyCounter` com:

                        `java.util.Map<Character, Integer> frequency(String text)`

                        - Retorna um mapa de cada caractere para quantas vezes ele aparece em `text`.
                        - String vazia retorna um mapa vazio.

                        A linha central do padrão é `freq.put(c, freq.getOrDefault(c, 0) + 1);`.

                        ## Critério de sucesso

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
                                "if separado para 'já existe ou não' - essa linha É o padrão HashMap inteiro.")
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
                .hint("getOrDefault(c, 0) devolve 0 na primeira vez que o char aparece - sem if de existência.")
                .hint("Pergunta obrigatória: usei HashMap porque preciso consultar rápido o que já apareceu. Tempo O(n), memória O(n).")
                .build();
    }

    private Exercise buildHashSetFirstRepeated(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-03-hashset-first-repeated",
                        module,
                        "Padrão 3: HashSet para duplicidade",
                        """
                        ## Contexto

                        Entenda a divisão de trabalho:

                        > HashSet responde: já vi isso antes? \
                        HashMap responde: quantas vezes, onde, ou com qual valor?

                        Quando a pergunta é apenas "já passou por aqui?", HashSet é mais simples e \
                        mais barato que HashMap.

                        ## Objetivo

                        Implemente `public class DuplicateFinder` com:

                        `int firstRepeated(int[] nums)`

                        - Retorna o PRIMEIRO valor que aparece pela segunda vez, percorrendo da \
                        esquerda para a direita.
                        - Retorna `-1` se nenhum valor se repete.

                        ## Critério de sucesso

                        Em `{4, 2, 7, 2, 9, 4, 1, 8}` a resposta é `2` (o segundo `2` chega antes do \
                        segundo `4`), não `4`.
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
                                // se seen.contains(n), n é o primeiro repetido; senão seen.add(n).
                                return -1;
                            }
                        }
                        """,
                        "BÁSICO", 2, 10)
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
                .equalsCase("primeiro repetido de {4, 2, 7, 2, 9, 4, 1, 8} é 2, não 4",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "d.firstRepeated(nums)", "2", true)
                .equalsCase("sem repetidos retorna -1",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {1, 2, 3};",
                        "d.firstRepeated(nums)", "-1", true)
                .equalsCase("repetição imediata: {5, 5} retorna 5",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {5, 5};",
                        "d.firstRepeated(nums)", "5", false)
                .equalsCase("{9, 4, 9, 4} retorna 9 - a ordem de chegada da segunda ocorrência decide",
                        "DuplicateFinder d = new DuplicateFinder(); int[] nums = {9, 4, 9, 4};",
                        "d.firstRepeated(nums)", "9", false)
                .hint("Consulte o Set ANTES de adicionar - se adicionar primeiro, todo elemento parece repetido.")
                .hint("Retorne dentro do loop assim que achar; não continue percorrendo.")
                .hint("Pergunta obrigatória: o estado é o conjunto do que já vi. Tempo O(n), memória O(n).")
                .build();
    }

    private Exercise buildTwoPointers(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-04-two-pointers",
                        module,
                        "Padrão 4: dois ponteiros",
                        """
                        ## Contexto

                        Padrão clássico de entrevista técnica. O raciocínio:

                        > Se está ordenado, eu posso andar pelas pontas. \
                        Se a soma ficou pequena, avanço a esquerda. \
                        Se a soma ficou grande, recuo a direita.

                        Isso resolve busca de pares, palíndromo, merge e compactação - sem HashMap e \
                        sem loop duplo O(n2).

                        ## Objetivo

                        Implemente `public class PairFinder` com:

                        `int[] pairWithSum(int[] sortedNums, int target)`

                        - `sortedNums` está ordenado crescente.
                        - Retorna um array `{menor, maior}` com o par cuja soma é `target`.
                        - Retorna `new int[0]` (array vazio) se não existir par.

                        ## Critério de sucesso

                        Usa exatamente um ponteiro em cada ponta convergindo ao centro - uma única \
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
                        "INTERMEDIÁRIO", 3, 15)
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
                        "Só um ponteiro se move por iteração, e a direção depende da comparação: soma pequena " +
                                "avança a esquerda (valores maiores), soma grande recua a direita (valores menores) - aproveitando o array já ordenado.")
                .equalsCase("em {1, 2, 4, 7, 8, 9} com target 10, o par é {1, 9}",
                        "PairFinder p = new PairFinder(); int[] nums = {1, 2, 4, 7, 8, 9};",
                        "p.pairWithSum(nums, 10)", "new int[]{1, 9}", true)
                .equalsCase("com target 12 os ponteiros convergem até {4, 8}",
                        "PairFinder p = new PairFinder(); int[] nums = {1, 2, 4, 7, 8, 9};",
                        "p.pairWithSum(nums, 12)", "new int[]{4, 8}", true)
                .equalsCase("sem par possível retorna array vazio",
                        "PairFinder p = new PairFinder(); int[] nums = {1, 2, 4, 7, 8, 9};",
                        "p.pairWithSum(nums, 100)", "new int[0]", false)
                .equalsCase("array vazio retorna array vazio sem lançar exceção",
                        "PairFinder p = new PairFinder(); int[] nums = {};",
                        "p.pairWithSum(nums, 5)", "new int[0]", false)
                .hint("Condição do while é left < right - quando se cruzam, não há mais pares para testar.")
                .hint("Só um dos ponteiros se move por iteração, e a direção depende de sum comparado a target.")
                .hint("Pergunta obrigatória: a ordenação é o que permite descartar metade das combinações. Tempo O(n), memória O(1).")
                .build();
    }

    private Exercise buildSlidingWindow(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-05-sliding-window",
                        module,
                        "Padrão 5: janela deslizante",
                        """
                        ## Contexto

                        A ideia central:

                        > Eu não recalculo tudo. Eu adiciono quem entrou e removo quem saiu.

                        Recalcular a soma de cada janela do zero custa O(n*k). Deslizar a janela \
                        (somar quem entra, subtrair quem sai) custa O(n). É o padrão que transforma \
                        soluções lentas em rápidas.

                        ## Objetivo

                        Implemente `public class WindowScanner` com:

                        `int maxWindowSum(int[] nums, int k)`

                        - Retorna a maior soma entre todas as janelas de `k` elementos consecutivos.
                        - Os testes garantem `1 <= k <= nums.length`.

                        ## Critério de sucesso

                        Em `{4, 2, 7, 2, 9, 4, 1, 8}` com `k = 3`, a janela `{7, 2, 9}` vence com \
                        soma `18`. A solução deve atualizar a soma incrementalmente, não recalcular \
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
                                // windowSum += nums[i] - nums[i - k] e guarde o máximo.
                                return 0;
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 4, 15)
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
                                "subtrai quem SAI (nums[i - k]) - essa única troca transforma O(n*k) em O(n).")
                .equalsCase("maior soma de 3 consecutivos em {4, 2, 7, 2, 9, 4, 1, 8} é 18",
                        "WindowScanner w = new WindowScanner(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "w.maxWindowSum(nums, 3)", "18", true)
                .equalsCase("k = 1 degenera para o maior elemento: 9",
                        "WindowScanner w = new WindowScanner(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "w.maxWindowSum(nums, 1)", "9", true)
                .equalsCase("k = nums.length é a soma total: 37",
                        "WindowScanner w = new WindowScanner(); int[] nums = {4, 2, 7, 2, 9, 4, 1, 8};",
                        "w.maxWindowSum(nums, 8)", "37", false)
                .equalsCase("funciona com negativos: {5, -2, 3} com k = 2 dá 3",
                        "WindowScanner w = new WindowScanner(); int[] nums = {5, -2, 3};",
                        "w.maxWindowSum(nums, 2)", "3", false)
                .hint("Primeira janela: um loop simples somando nums[0..k-1].")
                .hint("A cada deslize, quem entra é nums[i] e quem sai é nums[i - k].")
                .hint("Pergunta obrigatória: o estado é a soma da janela atual mais o máximo visto. Tempo O(n), memória O(1).")
                .build();
    }

    private Exercise buildStackBrackets(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-06-stack-brackets",
                        module,
                        "Padrão 6: pilha",
                        """
                        ## Contexto

                        Pilha é para "último que entrou, primeiro que sai" (LIFO). Ela resolve \
                        parênteses, histórico, desfazer, recursão simulada, backtracking e avaliação \
                        de expressões.

                        No problema dos parênteses, o insight é: quando um fechamento chega, ele \
                        DEVE casar com a abertura mais recente ainda aberta - e "mais recente" é \
                        exatamente o topo da pilha.

                        ## Objetivo

                        Implemente `public class BracketValidator` usando \
                        `java.util.Deque<Character>` (via `new java.util.ArrayDeque<>()`) com:

                        `boolean isBalanced(String s)`

                        - `s` contém apenas os caracteres `()[]{}`.
                        - Retorna `true` se cada fechamento casa com a abertura correta e nada fica \
                        aberto no final.

                        ## Critério de sucesso

                        Cobre os três jeitos de falhar: fechamento errado (`"(]"`), fechamento sem \
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
                                // TODO: abertura -> push; fechamento -> pilha vazia é inválido,
                                // senão pop e confira o par. No final, válido se a pilha esvaziou.
                                return false;
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 5, 18)
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
                        "Um fechamento chegando com a pilha vazia significa que não há nenhuma abertura pendente " +
                                "para ele casar - é inválido antes mesmo de comparar o tipo de parêntese.")
                .solutionAnnotation(
                        "return stack.isEmpty();",
                        "No final, só é válido se a pilha ESVAZIOU - se sobrou alguma abertura sem fechamento " +
                                "correspondente, ainda há algo pendente.")
                .equalsCase("\"({[]})\" é balanceada",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"({[]})\")", "true", true)
                .equalsCase("\"(]\" falha: fechamento não casa com o topo da pilha",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"(]\")", "false", true)
                .equalsCase("\"((\" falha: sobrou abertura na pilha no final",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"((\")", "false", false)
                .equalsCase("\")\" falha: fechamento com pilha vazia",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\")\")", "false", false)
                .equalsCase("string vazia é balanceada",
                        "BracketValidator v = new BracketValidator();",
                        "v.isBalanced(\"\")", "true", false)
                .hint("Aberturas sempre entram na pilha; fechamentos sempre tentam um pop.")
                .hint("Não esqueça os dois casos de borda: pop com pilha vazia e pilha não vazia no final.")
                .hint("Pergunta obrigatória: a pilha guarda as aberturas pendentes em ordem LIFO. Tempo O(n), memória O(n).")
                .build();
    }

    private Exercise buildQueueHotPotato(LearningModule module) {
        return ExerciseBuilder.of(
                        "shape-07-queue-hot-potato",
                        module,
                        "Padrão 7: fila",
                        """
                        ## Contexto

                        Fila é para processar em ordem de chegada (FIFO): BFS, eventos, mensageria, \
                        tarefas e simulação. Este exercício usa a fila como SIMULAÇÃO - o clássico \
                        "batata quente": jogadores em círculo, a cada rodada conta-se `step` \
                        posições e quem parar na contagem sai do jogo.

                        O truque: um círculo se simula com fila fazendo quem está na frente voltar \
                        para o fim (`offer(poll())`).

                        ## Objetivo

                        Implemente `public class TurnSimulator` usando `java.util.Queue<String>` \
                        (via `new java.util.ArrayDeque<>()`) com:

                        `String lastRemaining(String[] players, int step)`

                        - Enfileire todos os jogadores na ordem dada.
                        - Enquanto houver mais de um: mova `step - 1` jogadores da frente para o \
                        fim, depois remova o da frente (eliminado).
                        - Retorna o último jogador restante. Os testes garantem `step >= 1`.

                        ## Critério de sucesso

                        Com `{"A", "B", "C", "D", "E"}` e `step = 2`, a eliminação é B, D, A, E - \
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
                        "INTERMEDIÁRIO", 6, 15)
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
                        "Essa única linha simula o círculo: tira quem está na frente e recoloca no fim da fila - " +
                                "é a 'rotação' que faz uma fila linear se comportar como um círculo de jogadores.")
                .solutionAnnotation(
                        "queue.poll();\n                }\n                return queue.poll();",
                        "O poll() de dentro do while elimina o jogador; o poll() final (fora do loop) retorna " +
                                "quem sobrou, quando a fila chega a ter só um jogador.")
                .equalsCase("{A, B, C, D, E} com step 2: elimina B, D, A, E - sobra C",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"A\", \"B\", \"C\", \"D\", \"E\"};",
                        "t.lastRemaining(players, 2)", "\"C\"", true)
                .equalsCase("step 1 elimina sempre o da frente - sobra o último da fila original",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"A\", \"B\", \"C\", \"D\", \"E\"};",
                        "t.lastRemaining(players, 1)", "\"E\"", true)
                .equalsCase("um único jogador já é o vencedor",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"solo\"};",
                        "t.lastRemaining(players, 3)", "\"solo\"", false)
                .equalsCase("sete jogadores com step 3: sobrevive D",
                        "TurnSimulator t = new TurnSimulator(); String[] players = {\"A\", \"B\", \"C\", \"D\", \"E\", \"F\", \"G\"};",
                        "t.lastRemaining(players, 3)", "\"D\"", false)
                .hint("offer(poll()) move o jogador da frente para o fim - é a rotação do círculo.")
                .hint("A cada rodada: step - 1 rotações, depois um poll() que elimina.")
                .hint("Pergunta obrigatória: a fila É o círculo de jogadores; a ordem FIFO preserva os turnos. Tempo O(n * step), memória O(n).")
                .build();
    }
}
