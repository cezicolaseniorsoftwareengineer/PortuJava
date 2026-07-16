package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;

import java.util.List;

import static com.biotechpay.lab.seed.InterviewChallengeFactory.challenge;
import static com.biotechpay.lab.seed.InterviewChallengeFactory.example;

final class InterviewMediumChallenges {

    private InterviewMediumChallenges() {}

    static List<Exercise> build(LearningModule module) {
        return List.of(
                challenge(module, 9, "longest-unique-window", "Maior janela sem repetição", "MÉDIO",
                        "Sliding window", "Devolva o tamanho da maior substring sem caracteres repetidos.",
                        "O(n) tempo e O(k) espaço", """
                        java.util.Map<Character, Integer> last = new java.util.HashMap<>();
                        int start = 0, best = 0;
                        for (int end = 0; end < input.length(); end++) {
                            char value = input.charAt(end);
                            if (last.containsKey(value)) start = Math.max(start, last.get(value) + 1);
                            last.put(value, end);
                            best = Math.max(best, end - start + 1);
                        }
                        return String.valueOf(best);
                        """, List.of(example("abcbank", "5", true), example("aaaa", "1", false))),
                challenge(module, 10, "anagram-buckets", "Agrupe assinaturas equivalentes", "MÉDIO",
                        "HashMap por assinatura", "Receba palavras CSV. Agrupe anagramas e devolva apenas os tamanhos dos grupos em ordem crescente.",
                        "O(n·k log k) tempo e O(n·k) espaço", """
                        java.util.Map<String, Integer> sizes = new java.util.HashMap<>();
                        for (String word : input.split(",")) {
                            char[] letters = word.toCharArray();
                            java.util.Arrays.sort(letters);
                            sizes.merge(new String(letters), 1, Integer::sum);
                        }
                        return sizes.values().stream().sorted().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("eat,tea,tan,ate,nat,bat", "1,2,3", true), example("a,a,a", "3", false))),
                challenge(module, 11, "product-except-position", "Produto sem a própria posição", "MÉDIO",
                        "Prefixo e sufixo", "Receba inteiros CSV e devolva, para cada posição, o produto dos demais sem divisão.",
                        "O(n) tempo e O(1) espaço extra além da saída", """
                        int[] values = java.util.Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
                        int[] result = new int[values.length];
                        int prefix = 1;
                        for (int i = 0; i < values.length; i++) { result[i] = prefix; prefix *= values[i]; }
                        int suffix = 1;
                        for (int i = values.length - 1; i >= 0; i--) { result[i] *= suffix; suffix *= values[i]; }
                        return java.util.Arrays.stream(result).mapToObj(String::valueOf).collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("1,2,3,4", "24,12,8,6", true), example("0,2,3", "6,0,0", false))),
                challenge(module, 12, "merge-maintenance-windows", "Mescle janelas sobrepostas", "MÉDIO",
                        "Ordenação e intervalos", "A entrada usa `início-fim` separados por vírgula. Mescle sobreposições e devolva no mesmo formato.",
                        "O(n log n) tempo e O(n) espaço", """
                        java.util.List<int[]> intervals = new java.util.ArrayList<>();
                        for (String token : input.split(",")) {
                            String[] bounds = token.split("-");
                            intervals.add(new int[]{Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1])});
                        }
                        intervals.sort(java.util.Comparator.comparingInt(value -> value[0]));
                        java.util.List<int[]> merged = new java.util.ArrayList<>();
                        for (int[] current : intervals) {
                            if (merged.isEmpty() || merged.get(merged.size() - 1)[1] < current[0]) merged.add(current.clone());
                            else merged.get(merged.size() - 1)[1] = Math.max(merged.get(merged.size() - 1)[1], current[1]);
                        }
                        return merged.stream().map(value -> value[0] + "-" + value[1]).collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("1-3,2-6,8-10,9-12", "1-6,8-12", true), example("1-2,3-4", "1-2,3-4", false))),
                challenge(module, 13, "top-frequent-codes", "Códigos mais frequentes", "MÉDIO",
                        "Contagem e heap conceitual", "A entrada é `k;valores`. Devolva os k valores mais frequentes, desempate pelo menor número.",
                        "O(n log u) tempo e O(u) espaço", """
                        String[] parts = input.split(";", -1);
                        int k = Integer.parseInt(parts[0]);
                        java.util.Map<Integer, Integer> count = new java.util.HashMap<>();
                        for (String token : parts[1].split(",")) count.merge(Integer.parseInt(token), 1, Integer::sum);
                        return count.entrySet().stream()
                                .sorted(java.util.Comparator.<java.util.Map.Entry<Integer, Integer>>comparingInt(java.util.Map.Entry::getValue)
                                        .reversed().thenComparingInt(java.util.Map.Entry::getKey))
                                .limit(k).map(entry -> String.valueOf(entry.getKey()))
                                .collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("2;1,1,1,2,2,3", "1,2", true), example("1;4,5,4,5", "4", false))),
                challenge(module, 14, "cycle-in-routing", "Ciclo no roteamento", "MÉDIO",
                        "Ponteiros lento e rápido", "Cada posição contém o próximo índice, ou `-1`. Partindo de zero, devolva se existe ciclo.",
                        "O(n) tempo e O(1) espaço", """
                        int[] next = java.util.Arrays.stream(input.split(",")).mapToInt(Integer::parseInt).toArray();
                        int slow = 0, fast = 0;
                        while (fast != -1 && next[fast] != -1) {
                            slow = next[slow];
                            fast = next[next[fast]];
                            if (slow == fast) return "true";
                        }
                        return "false";
                        """, List.of(example("1,2,3,1", "true", true), example("1,2,3,-1", "false", false))),
                challenge(module, 15, "level-capacity-sums", "Soma de capacidade por nível", "MÉDIO",
                        "Árvore em vetor e BFS por níveis", "Receba uma árvore binária em ordem de heap, usando `#` para ausente, e devolva a soma de cada nível não vazio.",
                        "O(n) tempo e O(n) espaço", """
                        String[] nodes = input.split(",");
                        java.util.Map<Integer, Integer> sums = new java.util.TreeMap<>();
                        for (int index = 0; index < nodes.length; index++) {
                            if (!nodes[index].equals("#")) {
                                int level = 31 - Integer.numberOfLeadingZeros(index + 1);
                                sums.merge(level, Integer.parseInt(nodes[index]), Integer::sum);
                            }
                        }
                        return sums.values().stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("1,2,3,4,5,6,7", "1,5,22", true), example("5,#,2,#,#,3,4", "5,2,7", false))),
                challenge(module, 16, "kth-largest-balance", "K-ésimo maior saldo", "MÉDIO",
                        "Min-heap de tamanho k", "A entrada é `k;valores`. Devolva o k-ésimo maior sem ordenar toda a lista.",
                        "O(n log k) tempo e O(k) espaço", """
                        String[] parts = input.split(";", -1);
                        int k = Integer.parseInt(parts[0]);
                        java.util.PriorityQueue<Integer> heap = new java.util.PriorityQueue<>();
                        for (String token : parts[1].split(",")) {
                            heap.add(Integer.parseInt(token));
                            if (heap.size() > k) heap.remove();
                        }
                        if (heap.size() < k) throw new IllegalArgumentException("k exceeds input size");
                        return String.valueOf(heap.element());
                        """, List.of(example("2;3,2,1,5,6,4", "5", true), example("3;7,7,8,9", "7", false)))
        );
    }
}
