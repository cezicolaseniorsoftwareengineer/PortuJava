package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;

import java.util.List;

import static com.biotechpay.lab.seed.InterviewChallengeFactory.challenge;
import static com.biotechpay.lab.seed.InterviewChallengeFactory.example;

final class InterviewIntermediateChallenges {

    private InterviewIntermediateChallenges() {}

    static List<Exercise> build(LearningModule module) {
        return List.of(
                challenge(module, 17, "lru-session-cache", "Cache LRU de sessões", "INTERMEDIÁRIO",
                        "LinkedHashMap com ordem de acesso", "A entrada é `capacidade;operações`. `Pchave:valor` grava e `Gchave` lê. Devolva os resultados de leitura, usando `-1` para ausência.",
                        "O(1) amortizado por operação e O(capacidade) espaço", """
                        String[] parts = input.split(";", -1);
                        int capacity = Integer.parseInt(parts[0]);
                        java.util.LinkedHashMap<String, String> cache = new java.util.LinkedHashMap<>(16, 0.75f, true);
                        java.util.List<String> output = new java.util.ArrayList<>();
                        for (String operation : parts[1].split(",")) {
                            if (operation.startsWith("P")) {
                                String[] pair = operation.substring(1).split(":", -1);
                                cache.put(pair[0], pair[1]);
                                if (cache.size() > capacity) cache.remove(cache.keySet().iterator().next());
                            } else if (operation.startsWith("G")) {
                                output.add(cache.getOrDefault(operation.substring(1), "-1"));
                            }
                        }
                        return String.join(",", output);
                        """, List.of(example("2;P1:10,P2:20,G1,P3:30,G2,G3", "10,-1,30", true), example("1;PA:x,GA,PB:y,GA,GB", "x,-1,y", false))),
                challenge(module, 18, "dependency-cycle", "Dependências de serviços", "INTERMEDIÁRIO",
                        "Ordenação topológica", "A entrada é `n;serviço-préRequisito`. Devolva `true` se todos os serviços podem ser ativados sem ciclo.",
                        "O(V+E) tempo e O(V+E) espaço", """
                        String[] parts = input.split(";", -1);
                        int count = Integer.parseInt(parts[0]);
                        java.util.List<java.util.List<Integer>> graph = new java.util.ArrayList<>();
                        for (int i = 0; i < count; i++) graph.add(new java.util.ArrayList<>());
                        int[] indegree = new int[count];
                        if (!parts[1].isBlank()) for (String edge : parts[1].split(",")) {
                            String[] nodes = edge.split("-");
                            int service = Integer.parseInt(nodes[0]), prerequisite = Integer.parseInt(nodes[1]);
                            graph.get(prerequisite).add(service);
                            indegree[service]++;
                        }
                        java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
                        for (int i = 0; i < count; i++) if (indegree[i] == 0) queue.add(i);
                        int visited = 0;
                        while (!queue.isEmpty()) {
                            int node = queue.remove(); visited++;
                            for (int next : graph.get(node)) if (--indegree[next] == 0) queue.add(next);
                        }
                        return String.valueOf(visited == count);
                        """, List.of(example("4;1-0,2-1,3-2", "true", true), example("3;1-0,2-1,0-2", "false", false))),
                challenge(module, 19, "shortest-safe-route", "Menor rota operacional", "INTERMEDIÁRIO",
                        "BFS em grade", "Linhas são separadas por `;`: `S` início, `E` destino, `#` bloqueio. Devolva a menor quantidade de passos, ou `-1`.",
                        "O(rows·cols) tempo e espaço", """
                        String[] rows = input.split(";");
                        int height = rows.length, width = rows[0].length();
                        int startRow = -1, startCol = -1;
                        for (int r = 0; r < height; r++) for (int c = 0; c < width; c++) if (rows[r].charAt(c) == 'S') { startRow = r; startCol = c; }
                        java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
                        boolean[][] seen = new boolean[height][width];
                        queue.add(new int[]{startRow, startCol, 0}); seen[startRow][startCol] = true;
                        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
                        while (!queue.isEmpty()) {
                            int[] current = queue.remove();
                            if (rows[current[0]].charAt(current[1]) == 'E') return String.valueOf(current[2]);
                            for (int[] direction : directions) {
                                int r = current[0] + direction[0], c = current[1] + direction[1];
                                if (r >= 0 && r < height && c >= 0 && c < width && !seen[r][c] && rows[r].charAt(c) != '#') {
                                    seen[r][c] = true; queue.add(new int[]{r, c, current[2] + 1});
                                }
                            }
                        }
                        return "-1";
                        """, List.of(example("S..;##.;..E", "4", true), example("S#E", "-1", false))),
                challenge(module, 20, "minimum-cash-composition", "Composição mínima de valores", "INTERMEDIÁRIO",
                        "Programação dinâmica", "A entrada é `valor;denominações`. Devolva a menor quantidade de itens para compor o valor, ou `-1`.",
                        "O(valor·denominações) tempo e O(valor) espaço", """
                        String[] parts = input.split(";", -1);
                        int amount = Integer.parseInt(parts[0]);
                        int[] coins = java.util.Arrays.stream(parts[1].split(",")).mapToInt(Integer::parseInt).toArray();
                        int[] best = new int[amount + 1];
                        java.util.Arrays.fill(best, amount + 1); best[0] = 0;
                        for (int current = 1; current <= amount; current++)
                            for (int coin : coins) if (coin <= current) best[current] = Math.min(best[current], best[current - coin] + 1);
                        return String.valueOf(best[amount] > amount ? -1 : best[amount]);
                        """, List.of(example("11;1,2,5", "3", true), example("3;2", "-1", false))),
                challenge(module, 21, "connected-risk-clusters", "Clusters de risco conectados", "INTERMEDIÁRIO",
                        "DFS/BFS em matriz", "Linhas binárias são separadas por `;`. Conte componentes de `1` conectados vertical ou horizontalmente.",
                        "O(rows·cols) tempo e espaço", """
                        String[] rows = input.split(";");
                        boolean[][] seen = new boolean[rows.length][rows[0].length()];
                        int clusters = 0;
                        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
                        for (int r = 0; r < rows.length; r++) for (int c = 0; c < rows[r].length(); c++) {
                            if (rows[r].charAt(c) != '1' || seen[r][c]) continue;
                            clusters++;
                            java.util.ArrayDeque<int[]> queue = new java.util.ArrayDeque<>();
                            queue.add(new int[]{r,c}); seen[r][c] = true;
                            while (!queue.isEmpty()) {
                                int[] current = queue.remove();
                                for (int[] direction : directions) {
                                    int nr = current[0] + direction[0], nc = current[1] + direction[1];
                                    if (nr >= 0 && nr < rows.length && nc >= 0 && nc < rows[nr].length()
                                            && rows[nr].charAt(nc) == '1' && !seen[nr][nc]) {
                                        seen[nr][nc] = true; queue.add(new int[]{nr,nc});
                                    }
                                }
                            }
                        }
                        return String.valueOf(clusters);
                        """, List.of(example("110;010;001", "2", true), example("101;010;101", "5", false))),
                challenge(module, 22, "prefix-directory", "Diretório por prefixo", "INTERMEDIÁRIO",
                        "Trie", "A entrada é `palavras|prefixo`. Construa uma trie e devolva quantas palavras compartilham o prefixo.",
                        "O(total de caracteres + prefixo) tempo e espaço", """
                        int separator = input.indexOf('|');
                        String[] words = input.substring(0, separator).split(",");
                        String prefix = input.substring(separator + 1);
                        class Node { java.util.Map<Character, Node> children = new java.util.HashMap<>(); int count; }
                        Node root = new Node();
                        for (String word : words) {
                            Node node = root;
                            for (char value : word.toCharArray()) { node = node.children.computeIfAbsent(value, ignored -> new Node()); node.count++; }
                        }
                        Node node = root;
                        for (char value : prefix.toCharArray()) { node = node.children.get(value); if (node == null) return "0"; }
                        return String.valueOf(node.count);
                        """, List.of(example("bank,banana,balance,boleto|ba", "3", true), example("pix,ted|doc", "0", false))),
                challenge(module, 23, "sliding-rate-limit", "Rate limit em janela móvel", "INTERMEDIÁRIO",
                        "Deque e sliding window", "A entrada é `limite,janela;timestamps`. Para cada tentativa ordenada, devolva se foi aceita. Eventos com idade igual à janela expiram.",
                        "O(n) tempo e O(limite) espaço", """
                        String[] parts = input.split(";", -1);
                        String[] policy = parts[0].split(",");
                        int limit = Integer.parseInt(policy[0]); long window = Long.parseLong(policy[1]);
                        java.util.ArrayDeque<Long> accepted = new java.util.ArrayDeque<>();
                        java.util.List<String> result = new java.util.ArrayList<>();
                        for (String token : parts[1].split(",")) {
                            long now = Long.parseLong(token);
                            while (!accepted.isEmpty() && accepted.peekFirst() <= now - window) accepted.removeFirst();
                            boolean allowed = accepted.size() < limit;
                            if (allowed) accepted.addLast(now);
                            result.add(String.valueOf(allowed));
                        }
                        return String.join(",", result);
                        """, List.of(example("3,1000;0,100,200,999,1000", "true,true,true,false,true", true), example("1,10;5,6,15", "true,false,true", false))),
                challenge(module, 24, "idempotency-registry", "Registro de idempotência", "INTERMEDIÁRIO",
                        "Set com decisão determinística", "Receba chaves CSV e devolva `NEW` na primeira ocorrência e `DUPLICATE` nas repetições.",
                        "O(n) tempo e O(n) espaço", """
                        java.util.Set<String> processed = new java.util.HashSet<>();
                        java.util.List<String> decisions = new java.util.ArrayList<>();
                        for (String key : input.split(",")) decisions.add(processed.add(key) ? "NEW" : "DUPLICATE");
                        return String.join(",", decisions);
                        """, List.of(example("a,b,a,c,b", "NEW,NEW,DUPLICATE,NEW,DUPLICATE", true), example("pix,pix", "NEW,DUPLICATE", false)))
        );
    }
}
