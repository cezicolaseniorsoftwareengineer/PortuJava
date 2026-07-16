package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;

import java.util.List;

import static com.biotechpay.lab.seed.InterviewChallengeFactory.challenge;
import static com.biotechpay.lab.seed.InterviewChallengeFactory.example;

final class InterviewBasicChallenges {

    private InterviewBasicChallenges() {}

    static List<Exercise> build(LearningModule module) {
        return List.of(
                challenge(module, 1, "distinct-event-types", "Tipos de evento distintos", "BÁSICO",
                        "Set e normalização", "Receba palavras separadas por espaço e devolva quantos valores distintos existem, ignorando caixa.",
                        "O(n) tempo e O(n) espaço", """
                        if (input == null || input.isBlank()) return "0";
                        java.util.Set<String> distinct = new java.util.HashSet<>();
                        for (String token : input.trim().split(" +")) distinct.add(token.toLowerCase(java.util.Locale.ROOT));
                        return String.valueOf(distinct.size());
                        """, List.of(example("PIX pix TED boleto", "3", true), example("", "0", false))),
                challenge(module, 2, "anagram-signatures", "Assinaturas anagramas", "BÁSICO",
                        "Ordenação de caracteres", "Receba duas palavras separadas por vírgula e devolva `true` quando tiverem os mesmos caracteres, ignorando caixa.",
                        "O(n log n) tempo e O(n) espaço", """
                        String[] parts = input.split(",", -1);
                        if (parts.length != 2) throw new IllegalArgumentException("Two words are required");
                        char[] left = parts[0].toLowerCase(java.util.Locale.ROOT).toCharArray();
                        char[] right = parts[1].toLowerCase(java.util.Locale.ROOT).toCharArray();
                        java.util.Arrays.sort(left);
                        java.util.Arrays.sort(right);
                        return String.valueOf(java.util.Arrays.equals(left, right));
                        """, List.of(example("Risco,Coris", "true", true), example("saldo,dados", "false", false))),
                challenge(module, 3, "first-unique-symbol", "Primeiro símbolo não repetido", "BÁSICO",
                        "LinkedHashMap", "Devolva o primeiro caractere que aparece uma única vez; devolva `-` se não existir.",
                        "O(n) tempo e O(k) espaço", """
                        java.util.Map<Character, Integer> counts = new java.util.LinkedHashMap<>();
                        for (char value : input.toCharArray()) counts.merge(value, 1, Integer::sum);
                        for (var entry : counts.entrySet()) if (entry.getValue() == 1) return String.valueOf(entry.getKey());
                        return "-";
                        """, List.of(example("ledger", "l", true), example("aabbcc", "-", false))),
                challenge(module, 4, "pair-sum-indexes", "Índices do par-alvo", "BÁSICO",
                        "HashMap de complemento", "A entrada é `alvo;lista`. Devolva os dois primeiros índices cujo valor soma o alvo, ou `-1,-1`.",
                        "O(n) tempo e O(n) espaço", """
                        String[] parts = input.split(";", -1);
                        int target = Integer.parseInt(parts[0]);
                        String[] values = parts[1].split(",");
                        java.util.Map<Integer, Integer> seen = new java.util.HashMap<>();
                        for (int index = 0; index < values.length; index++) {
                            int value = Integer.parseInt(values[index]);
                            Integer other = seen.get(target - value);
                            if (other != null) return other + "," + index;
                            seen.putIfAbsent(value, index);
                        }
                        return "-1,-1";
                        """, List.of(example("9;2,7,11,15", "0,1", true), example("8;4,4", "0,1", false))),
                challenge(module, 5, "merge-sorted-streams", "Mescle fluxos ordenados", "BÁSICO",
                        "Dois ponteiros", "A entrada contém duas listas CSV separadas por `|`. Devolva uma única lista ordenada.",
                        "O(n+m) tempo e O(n+m) espaço", """
                        int separator = input.indexOf('|');
                        int[] left = java.util.Arrays.stream(input.substring(0, separator).split(",")).mapToInt(Integer::parseInt).toArray();
                        int[] right = java.util.Arrays.stream(input.substring(separator + 1).split(",")).mapToInt(Integer::parseInt).toArray();
                        int[] merged = new int[left.length + right.length];
                        int i = 0, j = 0, k = 0;
                        while (i < left.length && j < right.length) merged[k++] = left[i] <= right[j] ? left[i++] : right[j++];
                        while (i < left.length) merged[k++] = left[i++];
                        while (j < right.length) merged[k++] = right[j++];
                        return java.util.Arrays.stream(merged).mapToObj(String::valueOf).collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("1,3,5|2,4,6", "1,2,3,4,5,6", true), example("1,1|1,2", "1,1,1,2", false))),
                challenge(module, 6, "best-single-trade", "Melhor ganho em uma operação", "BÁSICO",
                        "Mínimo prefixado", "Receba preços CSV e devolva o maior ganho possível comprando antes de vender; nunca devolva negativo.",
                        "O(n) tempo e O(1) espaço", """
                        int minimum = Integer.MAX_VALUE;
                        int best = 0;
                        for (String token : input.split(",")) {
                            int price = Integer.parseInt(token);
                            minimum = Math.min(minimum, price);
                            best = Math.max(best, price - minimum);
                        }
                        return String.valueOf(best);
                        """, List.of(example("7,1,5,3,6,4", "5", true), example("9,7,4,1", "0", false))),
                challenge(module, 7, "balanced-delimiters", "Delimitadores balanceados", "BÁSICO",
                        "Stack", "Valide `()`, `[]` e `{}` com fechamento na ordem correta.",
                        "O(n) tempo e O(n) espaço", """
                        java.util.ArrayDeque<Character> stack = new java.util.ArrayDeque<>();
                        java.util.Map<Character, Character> pairs = java.util.Map.of(')', '(', ']', '[', '}', '{');
                        for (char value : input.toCharArray()) {
                            if (pairs.containsValue(value)) stack.push(value);
                            else if (pairs.containsKey(value) && (stack.isEmpty() || stack.pop() != pairs.get(value))) return "false";
                        }
                        return String.valueOf(stack.isEmpty());
                        """, List.of(example("{[()]}", "true", true), example("([)]", "false", false))),
                challenge(module, 8, "binary-search-position", "Busca binária auditável", "BÁSICO",
                        "Busca binária", "A entrada é `alvo;lista ordenada`. Devolva o índice ou `-1`.",
                        "O(log n) tempo e O(1) espaço", """
                        String[] parts = input.split(";", -1);
                        int target = Integer.parseInt(parts[0]);
                        int[] values = java.util.Arrays.stream(parts[1].split(",")).mapToInt(Integer::parseInt).toArray();
                        int low = 0, high = values.length - 1;
                        while (low <= high) {
                            int middle = low + (high - low) / 2;
                            if (values[middle] == target) return String.valueOf(middle);
                            if (values[middle] < target) low = middle + 1; else high = middle - 1;
                        }
                        return "-1";
                        """, List.of(example("7;1,3,5,7,9", "3", true), example("4;1,3,5,7", "-1", false)))
        );
    }
}
