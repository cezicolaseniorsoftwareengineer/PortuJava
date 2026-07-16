package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;

import java.util.List;

import static com.biotechpay.lab.seed.InterviewChallengeFactory.challenge;
import static com.biotechpay.lab.seed.InterviewChallengeFactory.example;

final class InterviewAdvancedChallenges {

    private InterviewAdvancedChallenges() {}

    static List<Exercise> build(LearningModule module) {
        return List.of(
                challenge(module, 25, "running-median", "Mediana em fluxo", "AVANÇADO",
                        "Dois heaps", "Receba inteiros CSV e devolva a mediana após cada inserção, sempre com uma casa decimal.",
                        "O(n log n) tempo e O(n) espaço", """
                        java.util.PriorityQueue<Integer> lower = new java.util.PriorityQueue<>(java.util.Comparator.reverseOrder());
                        java.util.PriorityQueue<Integer> upper = new java.util.PriorityQueue<>();
                        java.util.List<String> medians = new java.util.ArrayList<>();
                        for (String token : input.split(",")) {
                            int value = Integer.parseInt(token);
                            if (lower.isEmpty() || value <= lower.element()) lower.add(value); else upper.add(value);
                            if (lower.size() > upper.size() + 1) upper.add(lower.remove());
                            if (upper.size() > lower.size()) lower.add(upper.remove());
                            double median = lower.size() == upper.size()
                                    ? ((long) lower.element() + upper.element()) / 2.0 : lower.element();
                            medians.add(String.format(java.util.Locale.ROOT, "%.1f", median));
                        }
                        return String.join(",", medians);
                        """, List.of(example("5,15,1,3", "5.0,10.0,5.0,4.0", true), example("2,2", "2.0,2.0", false))),
                challenge(module, 26, "global-lock-order", "Ordem global de locks", "AVANÇADO",
                        "Ordenação canônica e prevenção de deadlock", "Cada transferência é `origem-destino`. Devolva os pares de locks como `menor>maior`, sem duplicar e em ordem.",
                        "O(n log n) tempo e O(n) espaço", """
                        java.util.Set<String> ordered = new java.util.TreeSet<>();
                        for (String transfer : input.split(",")) {
                            String[] accounts = transfer.split("-");
                            String first = accounts[0].compareTo(accounts[1]) <= 0 ? accounts[0] : accounts[1];
                            String second = first.equals(accounts[0]) ? accounts[1] : accounts[0];
                            if (!first.equals(second)) ordered.add(first + ">" + second);
                        }
                        return String.join(",", ordered);
                        """, List.of(example("B-A,A-C,C-B", "A>B,A>C,B>C", true), example("X-X,Y-X,X-Y", "X>Y", false))),
                challenge(module, 27, "consistent-hash-ring", "Roteamento em anel consistente", "AVANÇADO",
                        "Busca de teto em TreeMap", "A entrada é `nó@posição,...|hashes`. Para cada hash, escolha o primeiro nó no sentido horário, voltando ao início quando necessário.",
                        "O((n+k) log n) tempo e O(n) espaço", """
                        int separator = input.indexOf('|');
                        java.util.NavigableMap<Integer, String> ring = new java.util.TreeMap<>();
                        for (String token : input.substring(0, separator).split(",")) {
                            String[] node = token.split("@");
                            ring.put(Integer.parseInt(node[1]), node[0]);
                        }
                        java.util.List<String> routed = new java.util.ArrayList<>();
                        for (String token : input.substring(separator + 1).split(",")) {
                            int hash = Integer.parseInt(token);
                            java.util.Map.Entry<Integer, String> target = ring.ceilingEntry(hash);
                            if (target == null) target = ring.firstEntry();
                            routed.add(target.getValue());
                        }
                        return String.join(",", routed);
                        """, List.of(example("A@100,B@500,C@900|50,120,700,950", "A,B,C,A", true), example("N1@10,N2@20|10,21", "N1,N1", false))),
                challenge(module, 28, "inbox-reorder-dedup", "Inbox com deduplicação e reorder", "AVANÇADO",
                        "Idempotência e versão monotônica", "Eventos são `agregado:versão:eventId`. Ignore eventId repetido, preserve a maior versão e devolva `agregado=versão` ordenado.",
                        "O(n log a) tempo e O(n+a) espaço", """
                        java.util.Set<String> eventIds = new java.util.HashSet<>();
                        java.util.Map<String, Integer> versions = new java.util.TreeMap<>();
                        for (String token : input.split(",")) {
                            String[] event = token.split(":");
                            if (!eventIds.add(event[2])) continue;
                            versions.merge(event[0], Integer.parseInt(event[1]), Math::max);
                        }
                        return versions.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue())
                                .collect(java.util.stream.Collectors.joining(","));
                        """, List.of(example("payment:1:e1,payment:2:e2,payment:2:e2,account:2:a2,account:1:a1", "account=2,payment=2", true), example("pix:3:x,pix:1:y", "pix=3", false))),
                challenge(module, 29, "fraud-velocity-window", "Velocity antifraude", "AVANÇADO",
                        "Sliding window com soma", "A entrada é `janela,limite;tempo:valor,...`. Devolva o primeiro timestamp em que a soma aceita na janela excede o limite, ou `SAFE`.",
                        "O(n) tempo e O(n) espaço", """
                        String[] parts = input.split(";", -1);
                        String[] policy = parts[0].split(",");
                        long window = Long.parseLong(policy[0]), limit = Long.parseLong(policy[1]);
                        java.util.ArrayDeque<long[]> events = new java.util.ArrayDeque<>();
                        long sum = 0;
                        for (String token : parts[1].split(",")) {
                            String[] event = token.split(":");
                            long time = Long.parseLong(event[0]), amount = Long.parseLong(event[1]);
                            while (!events.isEmpty() && events.peekFirst()[0] <= time - window) sum -= events.removeFirst()[1];
                            events.addLast(new long[]{time, amount}); sum += amount;
                            if (sum > limit) return String.valueOf(time);
                        }
                        return "SAFE";
                        """, List.of(example("1000,500;0:100,100:200,200:250", "200", true), example("100,500;0:300,100:300", "SAFE", false))),
                challenge(module, 30, "ledger-reconciliation", "Reconciliação de saldos", "AVANÇADO",
                        "Mapas ordenados e BigDecimal", "A entrada é `local|externo`, com itens `conta=valor`. Devolva apenas drifts `externo-local`, em ordem, escala 2; ou `RECONCILED`.",
                        "O(n log n) tempo e O(n) espaço", """
                        int separator = input.indexOf('|');
                        java.util.Map<String, java.math.BigDecimal> local = new java.util.HashMap<>();
                        java.util.Map<String, java.math.BigDecimal> external = new java.util.HashMap<>();
                        for (String token : input.substring(0, separator).split(",")) {
                            String[] entry = token.split("="); local.put(entry[0], new java.math.BigDecimal(entry[1]));
                        }
                        for (String token : input.substring(separator + 1).split(",")) {
                            String[] entry = token.split("="); external.put(entry[0], new java.math.BigDecimal(entry[1]));
                        }
                        java.util.Set<String> accounts = new java.util.TreeSet<>(); accounts.addAll(local.keySet()); accounts.addAll(external.keySet());
                        java.util.List<String> drift = new java.util.ArrayList<>();
                        for (String account : accounts) {
                            java.math.BigDecimal delta = external.getOrDefault(account, java.math.BigDecimal.ZERO)
                                    .subtract(local.getOrDefault(account, java.math.BigDecimal.ZERO)).setScale(2);
                            if (delta.signum() != 0) drift.add(account + "=" + delta.toPlainString());
                        }
                        return drift.isEmpty() ? "RECONCILED" : String.join(",", drift);
                        """, List.of(example("a=100.00,b=50.00|a=98.00,b=50.00,c=1.00", "a=-2.00,c=1.00", true), example("cash=10.00|cash=10.00", "RECONCILED", false)))
        );
    }
}
