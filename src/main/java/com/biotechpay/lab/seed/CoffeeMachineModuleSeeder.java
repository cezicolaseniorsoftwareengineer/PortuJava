package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * OOP track: a CoffeeMachine domain built up incrementally across exercises (encapsulation,
 * conditional business rules, inheritance/polymorphism, interfaces, custom exceptions, a state
 * machine). Sorting/graphs-flavored follow-ups and a full-integration capstone exercise are explicit
 * backlog for a later session, not silently dropped.
 */
@Component
public class CoffeeMachineModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public CoffeeMachineModuleSeeder(LearningModuleRepository moduleRepository, ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "oop-coffee-machine";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Orientação a Objetos: Máquina de Café",
                "OOP",
                "Construa uma CoffeeMachine do zero, exercício por exercício: encapsulamento, regras de " +
                        "negócio condicionais, herança e polimorfismo, contratos por interface, exceções de " +
                        "domínio e uma máquina de estados. Cada exercício compila de verdade e roda contra " +
                        "casos de teste reais - não há 'digitar o texto certo', só código que funciona.",
                0));

        exerciseRepository.save(buildEncapsulation(module));
        exerciseRepository.save(buildBusinessRules(module));
        exerciseRepository.save(buildInheritancePolymorphism(module));
        exerciseRepository.save(buildInterfaceContract(module));
        exerciseRepository.save(buildCustomException(module));
        exerciseRepository.save(buildStateMachine(module));

        return module;
    }

    private Exercise buildEncapsulation(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-01-encapsulation",
                        module,
                        "Construtor com validação",
                        """
                        ## Contexto

                        Você está construindo uma máquina de café. O primeiro passo de qualquer objeto \
                        é garantir que ele nunca exista em um estado inválido.

                        ## Objetivo

                        Implemente a classe `CoffeeMachine` com:

                        - Um construtor `CoffeeMachine(int waterMl, int beansGrams)` que guarda os dois valores.
                        - `getWaterMl()` e `getBeansGrams()` retornando o que foi guardado.
                        - O construtor deve lançar `IllegalArgumentException` se `waterMl` for negativo ou \
                        se `beansGrams` for negativo.
                        - Zero é um valor válido (máquina vazia, mas não inválida).

                        ## Critério de sucesso

                        Todos os casos de teste abaixo devem passar: construção válida expõe os valores \
                        corretos via getters, e construção com qualquer valor negativo lança a exceção.
                        """,
                        """
                        public class CoffeeMachine {
                            public CoffeeMachine(int waterMl, int beansGrams) { }
                            public int getWaterMl() { return 0; }
                            public int getBeansGrams() { return 0; }
                        }
                        """,
                        """
                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                // TODO: valide - lance IllegalArgumentException se waterMl ou beansGrams
                                // for negativo; depois guarde os dois valores nos campos.
                            }

                            public int getWaterMl() {
                                return waterMl;
                            }

                            public int getBeansGrams() {
                                return beansGrams;
                            }
                        }
                        """,
                        "INICIANTE", 0, 10)
                .referenceSolution("""
                        public class CoffeeMachine {
                            private final int waterMl;
                            private final int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                if (waterMl < 0) {
                                    throw new IllegalArgumentException("waterMl must not be negative");
                                }
                                if (beansGrams < 0) {
                                    throw new IllegalArgumentException("beansGrams must not be negative");
                                }
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }
                        }
                        """)
                .solutionAnnotation(
                        "private final int waterMl;\n    private final int beansGrams;",
                        "Campos privados e final: ninguém de fora altera o estado depois de construído, e o " +
                                "compilador garante que cada campo é atribuído exatamente uma vez.")
                .solutionAnnotation(
                        "if (waterMl < 0) {\n        throw new IllegalArgumentException(\"waterMl must not be negative\");\n    }",
                        "A validação acontece ANTES de qualquer atribuição a this.waterMl - se viesse depois, um " +
                                "objeto inválido chegaria a existir, mesmo que por um instante, antes de lançar a exceção.")
                .solutionAnnotation(
                        "this.waterMl = waterMl;\n    this.beansGrams = beansGrams;",
                        "As atribuições só acontecem depois que as duas validações passaram - todo CoffeeMachine " +
                                "que existe é, por construção, válido.")
                .equalsCase("construção válida expõe getWaterMl() corretamente",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.getWaterMl()", "500", true)
                .equalsCase("construção válida expõe getBeansGrams() corretamente",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.getBeansGrams()", "100", false)
                .equalsCase("zero é um valor válido de fronteira",
                        "CoffeeMachine m = new CoffeeMachine(0, 0);", "m.getWaterMl()", "0", true)
                .throwsCase("waterMl negativo lança IllegalArgumentException",
                        "", "new CoffeeMachine(-1, 100)", "java.lang.IllegalArgumentException", true)
                .throwsCase("beansGrams negativo lança IllegalArgumentException",
                        "", "new CoffeeMachine(100, -1)", "java.lang.IllegalArgumentException", false)
                .hint("Guarde os dois parâmetros em campos privados (private final int).")
                .hint("Valide ANTES de atribuir aos campos: if (waterMl < 0) throw new IllegalArgumentException(...);")
                .hint("A mesma validação se aplica a beansGrams, com sua própria mensagem de erro.")
                .build();
    }

    private Exercise buildBusinessRules(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-02-business-rules",
                        module,
                        "Regras de preparo",
                        """
                        ## Contexto

                        Uma máquina de verdade não prepara qualquer bebida a qualquer custo: ela verifica \
                        se tem recurso suficiente ANTES de gastar, e nunca gasta recurso numa tentativa que falhou.

                        ## Objetivo

                        Implemente `CoffeeMachine` com campos mutáveis `waterMl`/`beansGrams` (sem validação \
                        de negativos desta vez - foco é nas regras de preparo) e o método:

                        `String brew(String drinkType)`

                        Tabela de custo:
                        - `"ESPRESSO"`: 30ml de água, 18g de pó.
                        - `"AMERICANO"`: 150ml de água, 18g de pó.

                        Regras, nesta ordem:
                        1. Se `drinkType` não for nenhum dos dois acima, retorne `"UNKNOWN_DRINK"` (sem gastar nada).
                        2. Se faltar água OU pó para o tipo pedido, retorne `"INSUFFICIENT_RESOURCES"` \
                        **sem alterar** `waterMl`/`beansGrams`.
                        3. Caso contrário, desconte o custo de `waterMl`/`beansGrams` e retorne `"BREWING"`.

                        ## Critério de sucesso

                        O caminho de falha (recurso insuficiente) nunca pode ter efeito colateral - os \
                        valores de água e pó devem permanecer exatamente como estavam.
                        """,
                        """
                        public class CoffeeMachine {
                            public CoffeeMachine(int waterMl, int beansGrams) { }
                            public int getWaterMl() { return 0; }
                            public int getBeansGrams() { return 0; }
                            public String brew(String drinkType) { return null; }
                        }
                        """,
                        """
                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public String brew(String drinkType) {
                                // TODO: declare waterCost/beansCost e preencha por tipo:
                                // "ESPRESSO" -> 30/18; "AMERICANO" -> 150/18; outro valor -> retorne "UNKNOWN_DRINK".
                                // Depois, se faltar água ou pó, retorne "INSUFFICIENT_RESOURCES" sem descontar;
                                // senão desconte waterMl/beansGrams e retorne "BREWING".
                                return null;
                            }
                        }
                        """,
                        "INICIANTE", 1, 12)
                .referenceSolution("""
                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public String brew(String drinkType) {
                                int waterCost;
                                int beansCost;
                                if ("ESPRESSO".equals(drinkType)) {
                                    waterCost = 30;
                                    beansCost = 18;
                                } else if ("AMERICANO".equals(drinkType)) {
                                    waterCost = 150;
                                    beansCost = 18;
                                } else {
                                    return "UNKNOWN_DRINK";
                                }
                                if (waterMl < waterCost || beansGrams < beansCost) {
                                    return "INSUFFICIENT_RESOURCES";
                                }
                                waterMl -= waterCost;
                                beansGrams -= beansCost;
                                return "BREWING";
                            }
                        }
                        """)
                .solutionAnnotation(
                        "} else {\n        return \"UNKNOWN_DRINK\";\n    }",
                        "O tipo de bebida é checado PRIMEIRO. Se não reconhecer, retorna imediatamente - sem " +
                                "gastar água ou pó por um pedido que nem existe.")
                .solutionAnnotation(
                        "if (waterMl < waterCost || beansGrams < beansCost) {\n        return \"INSUFFICIENT_RESOURCES\";\n    }",
                        "Verifica recurso ANTES de descontar. Essa ordem garante que uma tentativa que falha " +
                                "nunca tem efeito colateral no estado da máquina.")
                .solutionAnnotation(
                        "waterMl -= waterCost;\n    beansGrams -= beansCost;\n    return \"BREWING\";",
                        "Só desconta depois de confirmar que há recurso suficiente - o desconto é a última coisa " +
                                "que acontece no caminho de sucesso.")
                .equalsCase("espresso com recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.brew(\"ESPRESSO\")", "\"BREWING\"", true)
                .equalsCase("preparo de sucesso desconta a água",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.brew(\"ESPRESSO\");", "m.getWaterMl()", "470", false)
                .equalsCase("americano com água insuficiente não prepara",
                        "CoffeeMachine m = new CoffeeMachine(100, 100);", "m.brew(\"AMERICANO\")", "\"INSUFFICIENT_RESOURCES\"", true)
                .equalsCase("falha por recurso insuficiente não altera a água",
                        "CoffeeMachine m = new CoffeeMachine(100, 100); m.brew(\"AMERICANO\");", "m.getWaterMl()", "100", false)
                .equalsCase("bebida desconhecida retorna UNKNOWN_DRINK",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.brew(\"SUCO\")", "\"UNKNOWN_DRINK\"", true)
                .hint("Comece checando o tipo de bebida; se não reconhecer, retorne UNKNOWN_DRINK imediatamente.")
                .hint("Só decida INSUFFICIENT_RESOURCES depois de saber o custo do tipo pedido.")
                .hint("Só subtraia de waterMl/beansGrams DEPOIS de confirmar que há recurso suficiente.")
                .build();
    }

    private Exercise buildInheritancePolymorphism(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-03-inheritance-polymorphism",
                        module,
                        "Tipos de bebida por herança",
                        """
                        ## Contexto

                        A tabela de custo hardcoded do exercício anterior não escala: cada bebida nova \
                        exigiria mexer no método `brew`. Com herança, cada bebida sabe o próprio custo.

                        ## Objetivo

                        No MESMO arquivo (Java permite vários tipos por arquivo, desde que só um seja \
                        `public`), declare:

                        - `abstract class Drink` com `abstract int waterCost()` e `abstract int beansCost()`.
                        - `class Espresso extends Drink`: custo 30ml água, 18g pó.
                        - `class Americano extends Drink`: custo 150ml água, 18g pó.
                        - `public class CoffeeMachine` com construtor `(int waterMl, int beansGrams)`, \
                        getters, e `String brew(Drink drink)` usando o **despacho polimórfico** \
                        (`drink.waterCost()`/`drink.beansCost()`) em vez de checar o tipo.

                        Mesma regra de negócio do exercício anterior: sem recurso suficiente, \
                        `"INSUFFICIENT_RESOURCES"` sem efeito colateral; com recurso, desconta e retorna `"BREWING"`.

                        ## Critério de sucesso

                        O método `brew` funciona para QUALQUER `Drink`, inclusive tipos que não existem \
                        ainda - ele nunca deveria precisar de `instanceof` ou `if (drink instanceof Espresso)`.
                        """,
                        """
                        abstract class Drink {
                            public abstract int waterCost();
                            public abstract int beansCost();
                        }

                        class Espresso extends Drink {
                            public int waterCost() { return 30; }
                            public int beansCost() { return 18; }
                        }

                        class Americano extends Drink {
                            public int waterCost() { return 150; }
                            public int beansCost() { return 18; }
                        }

                        public class CoffeeMachine {
                            public CoffeeMachine(int waterMl, int beansGrams) { }
                            public int getWaterMl() { return 0; }
                            public int getBeansGrams() { return 0; }
                            public String brew(Drink drink) { return null; }
                        }
                        """,
                        """
                        abstract class Drink {
                            public abstract int waterCost();
                            public abstract int beansCost();
                        }

                        class Espresso extends Drink {
                            // TODO: implemente waterCost() e beansCost().
                        }

                        class Americano extends Drink {
                            // TODO: implemente waterCost() e beansCost().
                        }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public String brew(Drink drink) {
                                // TODO: use drink.waterCost()/drink.beansCost() - sem instanceof.
                                // Retorne "INSUFFICIENT_RESOURCES" ou "BREWING" conforme o caso.
                                return null;
                            }
                        }
                        """,
                        "BÁSICO", 2, 15)
                .referenceSolution("""
                        abstract class Drink {
                            public abstract int waterCost();
                            public abstract int beansCost();
                        }

                        class Espresso extends Drink {
                            public int waterCost() { return 30; }
                            public int beansCost() { return 18; }
                        }

                        class Americano extends Drink {
                            public int waterCost() { return 150; }
                            public int beansCost() { return 18; }
                        }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public String brew(Drink drink) {
                                if (waterMl < drink.waterCost() || beansGrams < drink.beansCost()) {
                                    return "INSUFFICIENT_RESOURCES";
                                }
                                waterMl -= drink.waterCost();
                                beansGrams -= drink.beansCost();
                                return "BREWING";
                            }
                        }
                        """)
                .solutionAnnotation(
                        "abstract class Drink {\n    public abstract int waterCost();\n    public abstract int beansCost();\n}",
                        "Drink não sabe fazer café - só sabe informar o próprio custo. Cada subtipo é responsável " +
                                "por si mesmo, então adicionar uma bebida nova nunca exige tocar em CoffeeMachine.")
                .solutionAnnotation(
                        "if (waterMl < drink.waterCost() || beansGrams < drink.beansCost()) {",
                        "brew() chama drink.waterCost() sem nunca perguntar 'qual é o tipo concreto?' - é despacho " +
                                "polimórfico: o objeto certo responde, o método nunca precisa de instanceof.")
                .equalsCase("Espresso reporta o próprio custo de água",
                        "", "new Espresso().waterCost()", "30", true)
                .equalsCase("brew com Espresso e recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); Drink d = new Espresso();", "m.brew(d)", "\"BREWING\"", true)
                .equalsCase("brew com Americano e recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); Drink d = new Americano();", "m.brew(d)", "\"BREWING\"", false)
                .equalsCase("despacho polimórfico em lote soma os custos corretos",
                        "java.util.List<Drink> drinks = java.util.List.of(new Espresso(), new Americano()); " +
                                "int total = 0; for (Drink dr : drinks) { total += dr.waterCost(); }",
                        "total", "180", true)
                .equalsCase("recurso insuficiente via Drink não prepara",
                        "CoffeeMachine m = new CoffeeMachine(10, 100); Drink d = new Americano();", "m.brew(d)", "\"INSUFFICIENT_RESOURCES\"", false)
                .hint("Drink e Espresso/Americano NÃO levam o modificador public - só CoffeeMachine leva.")
                .hint("brew(Drink drink) chama drink.waterCost()/drink.beansCost() - nunca verifica o tipo concreto.")
                .hint("A regra de negócio (checar antes de descontar) é idêntica ao exercício anterior.")
                .build();
    }

    private Exercise buildInterfaceContract(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-04-interface-contract",
                        module,
                        "Contrato de pagamento",
                        """
                        ## Contexto

                        Cartão e dinheiro são formas de pagamento completamente diferentes por dentro, \
                        mas a máquina de café não deveria se importar com qual delas está usando.

                        ## Objetivo

                        Declare, no mesmo arquivo:

                        - `interface PaymentMethod` com `boolean charge(int amountCents)`.
                        - `class CardPayment implements PaymentMethod`: `charge` retorna `true` se \
                        `amountCents > 0`.
                        - `class CashPayment implements PaymentMethod`: construtor recebe \
                        `availableCents`; `charge` só retorna `true` (e desconta) se houver saldo \
                        suficiente; expõe `getAvailableCents()`.
                        - `public class CoffeeMachine(int waterMl, int beansGrams)` com \
                        `String brew(PaymentMethod payment)`. Custo fixo: 30ml água, 18g pó, 500 centavos.

                        Ordem das regras em `brew`:
                        1. Se faltar água OU pó, retorne `"INSUFFICIENT_RESOURCES"` - **sem tentar cobrar**.
                        2. Senão, tente `payment.charge(500)`. Se falhar, retorne `"PAYMENT_DECLINED"` - \
                        **sem descontar recurso**.
                        3. Se cobrar com sucesso, desconte água/pó e retorne `"BREWING"`.

                        ## Critério de sucesso

                        `brew` funciona identicamente para `CardPayment` e `CashPayment` - a máquina só \
                        conhece a interface, nunca o tipo concreto.
                        """,
                        """
                        interface PaymentMethod {
                            boolean charge(int amountCents);
                        }

                        class CardPayment implements PaymentMethod {
                            public boolean charge(int amountCents) { return amountCents > 0; }
                        }

                        class CashPayment implements PaymentMethod {
                            public CashPayment(int availableCents) { }
                            public int getAvailableCents() { return 0; }
                            public boolean charge(int amountCents) { return false; }
                        }

                        public class CoffeeMachine {
                            public CoffeeMachine(int waterMl, int beansGrams) { }
                            public int getWaterMl() { return 0; }
                            public int getBeansGrams() { return 0; }
                            public String brew(PaymentMethod payment) { return null; }
                        }
                        """,
                        """
                        interface PaymentMethod {
                            boolean charge(int amountCents);
                        }

                        class CardPayment implements PaymentMethod {
                            public boolean charge(int amountCents) { return amountCents > 0; }
                        }

                        class CashPayment implements PaymentMethod {
                            private int availableCents;

                            public CashPayment(int availableCents) {
                                this.availableCents = availableCents;
                            }

                            public int getAvailableCents() { return availableCents; }

                            public boolean charge(int amountCents) {
                                // TODO: só desconta e retorna true se houver saldo suficiente.
                                return false;
                            }
                        }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public String brew(PaymentMethod payment) {
                                // TODO: checar recurso, só então cobrar, só então descontar.
                                // Retorne "INSUFFICIENT_RESOURCES", "PAYMENT_DECLINED" ou "BREWING".
                                return null;
                            }
                        }
                        """,
                        "BÁSICO", 3, 15)
                .referenceSolution("""
                        interface PaymentMethod {
                            boolean charge(int amountCents);
                        }

                        class CardPayment implements PaymentMethod {
                            public boolean charge(int amountCents) { return amountCents > 0; }
                        }

                        class CashPayment implements PaymentMethod {
                            private int availableCents;

                            public CashPayment(int availableCents) {
                                this.availableCents = availableCents;
                            }

                            public int getAvailableCents() { return availableCents; }

                            public boolean charge(int amountCents) {
                                if (availableCents >= amountCents) {
                                    availableCents -= amountCents;
                                    return true;
                                }
                                return false;
                            }
                        }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public String brew(PaymentMethod payment) {
                                if (waterMl < 30 || beansGrams < 18) {
                                    return "INSUFFICIENT_RESOURCES";
                                }
                                if (!payment.charge(500)) {
                                    return "PAYMENT_DECLINED";
                                }
                                waterMl -= 30;
                                beansGrams -= 18;
                                return "BREWING";
                            }
                        }
                        """)
                .solutionAnnotation(
                        "interface PaymentMethod {\n    boolean charge(int amountCents);\n}",
                        "A máquina só conversa com essa interface. CardPayment e CashPayment implementam do jeito " +
                                "delas - nenhuma precisa que CoffeeMachine mude para funcionar.")
                .solutionAnnotation(
                        "if (waterMl < 30 || beansGrams < 18) {\n                    return \"INSUFFICIENT_RESOURCES\";\n                }\n                if (!payment.charge(500)) {",
                        "A ordem importa: primeiro checa recurso (sem cobrar nada), só depois tenta cobrar. Cobrar " +
                                "antes de saber se dá pra preparar seria cobrar o cliente por um café que não vai sair.")
                .solutionAnnotation(
                        "if (availableCents >= amountCents) {\n                    availableCents -= amountCents;\n                    return true;\n                }\n                return false;",
                        "CashPayment só desconta o saldo quando há dinheiro suficiente - charge() nunca deixa " +
                                "availableCents ficar negativo.")
                .equalsCase("cartão com recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); PaymentMethod p = new CardPayment();", "m.brew(p)", "\"BREWING\"", true)
                .equalsCase("dinheiro insuficiente é recusado sem descontar recurso",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); CashPayment p = new CashPayment(100); m.brew(p);",
                        "m.getWaterMl()", "500", false)
                .equalsCase("dinheiro suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); PaymentMethod p = new CashPayment(1000);", "m.brew(p)", "\"BREWING\"", true)
                .equalsCase("recurso insuficiente nunca tenta cobrar",
                        "CoffeeMachine m = new CoffeeMachine(10, 100); CashPayment p = new CashPayment(1000); m.brew(p);",
                        "p.getAvailableCents()", "1000", true)
                .equalsCase("pagamento recusado retorna PAYMENT_DECLINED",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); PaymentMethod p = new CashPayment(100);", "m.brew(p)", "\"PAYMENT_DECLINED\"", false)
                .hint("brew recebe PaymentMethod, nunca CardPayment ou CashPayment diretamente.")
                .hint("A ordem importa: primeiro cheque recurso, só depois tente cobrar.")
                .hint("CashPayment.charge só desconta availableCents quando há saldo suficiente.")
                .build();
    }

    private Exercise buildCustomException(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-05-custom-exception",
                        module,
                        "Exceção de domínio",
                        """
                        ## Contexto

                        Retornar uma string como `"INSUFFICIENT_RESOURCES"` funciona, mas o chamador \
                        pode simplesmente ignorar o retorno. Uma exceção força quem chama a lidar com o problema.

                        ## Objetivo

                        Declare `class InsufficientResourcesException extends RuntimeException` cujo \
                        construtor recebe a mensagem (`super(message)`), e implemente:

                        `public class CoffeeMachine(int waterMl, int beansGrams)` com \
                        `void brew(int waterCost, int beansCost)` que:

                        - Lança `InsufficientResourcesException` com mensagem contendo a palavra \
                        `"water"` se faltar água.
                        - Lança `InsufficientResourcesException` com mensagem contendo a palavra \
                        `"beans"` se faltar pó (e a água for suficiente).
                        - Caso contrário, desconta os dois recursos normalmente.

                        ## Critério de sucesso

                        A exceção certa é lançada para cada recurso faltante, com a mensagem certa, e o \
                        caminho de sucesso desconta os recursos.
                        """,
                        """
                        class InsufficientResourcesException extends RuntimeException {
                            public InsufficientResourcesException(String message) { super(message); }
                        }

                        public class CoffeeMachine {
                            public CoffeeMachine(int waterMl, int beansGrams) { }
                            public int getWaterMl() { return 0; }
                            public int getBeansGrams() { return 0; }
                            public void brew(int waterCost, int beansCost) { }
                        }
                        """,
                        """
                        class InsufficientResourcesException extends RuntimeException {
                            public InsufficientResourcesException(String message) {
                                super(message);
                            }
                        }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public void brew(int waterCost, int beansCost) {
                                // TODO: lance InsufficientResourcesException para cada recurso faltante.
                            }
                        }
                        """,
                        "BÁSICO", 4, 12)
                .referenceSolution("""
                        class InsufficientResourcesException extends RuntimeException {
                            public InsufficientResourcesException(String message) {
                                super(message);
                            }
                        }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public int getWaterMl() { return waterMl; }
                            public int getBeansGrams() { return beansGrams; }

                            public void brew(int waterCost, int beansCost) {
                                if (waterMl < waterCost) {
                                    throw new InsufficientResourcesException("not enough water");
                                }
                                if (beansGrams < beansCost) {
                                    throw new InsufficientResourcesException("not enough beans");
                                }
                                waterMl -= waterCost;
                                beansGrams -= beansCost;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "class InsufficientResourcesException extends RuntimeException {\n    public InsufficientResourcesException(String message) {\n        super(message);\n    }\n}",
                        "Uma RuntimeException customizada só precisa repassar a mensagem pro construtor da " +
                                "superclasse - o resto (stack trace, getMessage()) já vem de graça de RuntimeException.")
                .solutionAnnotation(
                        "if (waterMl < waterCost) {\n        throw new InsufficientResourcesException(\"not enough water\");\n    }\n    if (beansGrams < beansCost) {",
                        "Checa água primeiro; só chega a checar pó se a água já passou. Cada exceção carrega uma " +
                                "mensagem específica do recurso que faltou.")
                .throwsCase("água insuficiente lança InsufficientResourcesException",
                        "", "new CoffeeMachine(10, 100).brew(30, 18)", "InsufficientResourcesException", true)
                .exceptionMessageContainsCase("mensagem da exceção de água menciona 'water'",
                        "", "new CoffeeMachine(10, 100).brew(30, 18)", "InsufficientResourcesException", "\"water\"", false)
                .throwsCase("pó insuficiente lança InsufficientResourcesException",
                        "", "new CoffeeMachine(500, 5).brew(30, 18)", "InsufficientResourcesException", true)
                .exceptionMessageContainsCase("mensagem da exceção de pó menciona 'beans'",
                        "", "new CoffeeMachine(500, 5).brew(30, 18)", "InsufficientResourcesException", "\"beans\"", false)
                .equalsCase("caminho de sucesso desconta a água",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.brew(30, 18);", "m.getWaterMl()", "470", true)
                .hint("InsufficientResourcesException só precisa de um construtor que chama super(message).")
                .hint("Cheque água primeiro; só cheque pó se a água for suficiente.")
                .hint("A mensagem de cada exceção deve conter literalmente 'water' ou 'beans'.")
                .build();
    }

    private Exercise buildStateMachine(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-06-state-machine",
                        module,
                        "Máquina de estados",
                        """
                        ## Contexto

                        Uma máquina de café de verdade não prepara duas bebidas ao mesmo tempo. Ela tem \
                        um estado, e certas ações só fazem sentido em certos estados.

                        ## Objetivo

                        Declare `enum MachineState { IDLE, BREWING, DONE, ERROR }` e implemente \
                        `public class CoffeeMachine(int waterMl, int beansGrams)` com estado inicial \
                        `IDLE` e:

                        - `MachineState getState()`.
                        - `void startBrew(int waterCost, int beansCost)`: só pode ser chamado em `IDLE` \
                        (senão lança `IllegalStateException`). Se houver recurso suficiente, desconta e \
                        vai para `BREWING`. Se não houver, vai para `ERROR` (sem lançar exceção - \
                        estado, não exceção, é como este exercício modela falta de recurso).
                        - `void finishBrew()`: só pode ser chamado em `BREWING` (senão lança \
                        `IllegalStateException`). Vai para `DONE`.

                        ## Critério de sucesso

                        As transições de estado seguem exatamente IDLE -> BREWING -> DONE no caminho \
                        feliz, IDLE -> ERROR quando falta recurso, e chamadas fora de ordem lançam \
                        `IllegalStateException`.
                        """,
                        """
                        enum MachineState { IDLE, BREWING, DONE, ERROR }

                        public class CoffeeMachine {
                            public CoffeeMachine(int waterMl, int beansGrams) { }
                            public MachineState getState() { return null; }
                            public void startBrew(int waterCost, int beansCost) { }
                            public void finishBrew() { }
                        }
                        """,
                        """
                        enum MachineState { IDLE, BREWING, DONE, ERROR }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;
                            private MachineState state = MachineState.IDLE;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public MachineState getState() { return state; }

                            public void startBrew(int waterCost, int beansCost) {
                                // TODO: só válido em IDLE; decide entre BREWING e ERROR.
                            }

                            public void finishBrew() {
                                // TODO: só válido em BREWING; vai para DONE.
                            }
                        }
                        """,
                        "INTERMEDIÁRIO", 5, 15)
                .referenceSolution("""
                        enum MachineState { IDLE, BREWING, DONE, ERROR }

                        public class CoffeeMachine {
                            private int waterMl;
                            private int beansGrams;
                            private MachineState state = MachineState.IDLE;

                            public CoffeeMachine(int waterMl, int beansGrams) {
                                this.waterMl = waterMl;
                                this.beansGrams = beansGrams;
                            }

                            public MachineState getState() { return state; }

                            public void startBrew(int waterCost, int beansCost) {
                                if (state != MachineState.IDLE) {
                                    throw new IllegalStateException("cannot start brewing from state " + state);
                                }
                                if (waterMl < waterCost || beansGrams < beansCost) {
                                    state = MachineState.ERROR;
                                    return;
                                }
                                waterMl -= waterCost;
                                beansGrams -= beansCost;
                                state = MachineState.BREWING;
                            }

                            public void finishBrew() {
                                if (state != MachineState.BREWING) {
                                    throw new IllegalStateException("cannot finish brewing from state " + state);
                                }
                                state = MachineState.DONE;
                            }
                        }
                        """)
                .solutionAnnotation(
                        "private MachineState state = MachineState.IDLE;",
                        "O estado inicial é definido no próprio campo, não no construtor - garante que toda " +
                                "CoffeeMachine nasce em IDLE, sem exceção.")
                .solutionAnnotation(
                        "if (state != MachineState.IDLE) {\n        throw new IllegalStateException(\"cannot start brewing from state \" + state);\n    }",
                        "startBrew só é válida a partir de IDLE. Chamar fora de ordem lança IllegalStateException " +
                                "em vez de silenciosamente corromper o estado.")
                .solutionAnnotation(
                        "if (waterMl < waterCost || beansGrams < beansCost) {\n        state = MachineState.ERROR;\n        return;\n    }",
                        "Falta de recurso aqui vira um ESTADO (ERROR), não uma exceção - nem toda falha precisa " +
                                "interromper o fluxo com throw.")
                .equalsCase("estado inicial é IDLE",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.getState()", "MachineState.IDLE", true)
                .equalsCase("startBrew com recurso suficiente vai para BREWING",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.startBrew(30, 18);", "m.getState()", "MachineState.BREWING", true)
                .equalsCase("startBrew sem recurso vai para ERROR",
                        "CoffeeMachine m = new CoffeeMachine(10, 100); m.startBrew(30, 18);", "m.getState()", "MachineState.ERROR", false)
                .throwsCase("finishBrew a partir de IDLE lança IllegalStateException",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.finishBrew()", "java.lang.IllegalStateException", true)
                .equalsCase("caminho feliz completo termina em DONE",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.startBrew(30, 18); m.finishBrew();", "m.getState()", "MachineState.DONE", false)
                .hint("O estado inicial é IDLE - defina isso no campo, não no construtor.")
                .hint("startBrew decide entre BREWING e ERROR sem lançar exceção para recurso insuficiente.")
                .hint("finishBrew só lança IllegalStateException se o estado atual não for BREWING.")
                .build();
    }
}
