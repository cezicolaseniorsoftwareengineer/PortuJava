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
                "Orientacao a Objetos: Maquina de Cafe",
                "OOP",
                "Construa uma CoffeeMachine do zero, exercicio por exercicio: encapsulamento, regras de " +
                        "negocio condicionais, heranca e polimorfismo, contratos por interface, excecoes de " +
                        "dominio e uma maquina de estados. Cada exercicio compila de verdade e roda contra " +
                        "casos de teste reais - nao ha 'digitar o texto certo', so codigo que funciona.",
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
                        "Construtor com validacao",
                        """
                        ## Contexto

                        Voce esta construindo uma maquina de cafe. O primeiro passo de qualquer objeto \
                        e garantir que ele nunca exista em um estado invalido.

                        ## Objetivo

                        Implemente a classe `CoffeeMachine` com:

                        - Um construtor `CoffeeMachine(int waterMl, int beansGrams)` que guarda os dois valores.
                        - `getWaterMl()` e `getBeansGrams()` retornando o que foi guardado.
                        - O construtor deve lancar `IllegalArgumentException` se `waterMl` for negativo ou \
                        se `beansGrams` for negativo.
                        - Zero e um valor valido (maquina vazia, mas nao invalida).

                        ## Criterio de sucesso

                        Todos os casos de teste abaixo devem passar: construcao valida expõe os valores \
                        corretos via getters, e construcao com qualquer valor negativo lanca a excecao.
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
                            // TODO: guarde waterMl e beansGrams em campos privados.
                            // TODO: valide no construtor - lance IllegalArgumentException se algum for negativo.

                            public CoffeeMachine(int waterMl, int beansGrams) {

                            }

                            public int getWaterMl() {
                                return 0;
                            }

                            public int getBeansGrams() {
                                return 0;
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
                .equalsCase("construcao valida expõe getWaterMl() corretamente",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.getWaterMl()", "500", true)
                .equalsCase("construcao valida expõe getBeansGrams() corretamente",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.getBeansGrams()", "100", false)
                .equalsCase("zero e um valor valido de fronteira",
                        "CoffeeMachine m = new CoffeeMachine(0, 0);", "m.getWaterMl()", "0", true)
                .throwsCase("waterMl negativo lanca IllegalArgumentException",
                        "", "new CoffeeMachine(-1, 100)", "java.lang.IllegalArgumentException", true)
                .throwsCase("beansGrams negativo lanca IllegalArgumentException",
                        "", "new CoffeeMachine(100, -1)", "java.lang.IllegalArgumentException", false)
                .hint("Guarde os dois parametros em campos privados (private final int).")
                .hint("Valide ANTES de atribuir aos campos: if (waterMl < 0) throw new IllegalArgumentException(...);")
                .hint("A mesma validacao se aplica a beansGrams, com sua propria mensagem de erro.")
                .build();
    }

    private Exercise buildBusinessRules(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-02-business-rules",
                        module,
                        "Regras de preparo",
                        """
                        ## Contexto

                        Uma maquina de verdade nao prepara qualquer bebida a qualquer custo: ela verifica \
                        se tem recurso suficiente ANTES de gastar, e nunca gasta recurso numa tentativa que falhou.

                        ## Objetivo

                        Implemente `CoffeeMachine` com campos mutaveis `waterMl`/`beansGrams` (sem validacao \
                        de negativos desta vez - foco e nas regras de preparo) e o metodo:

                        `String brew(String drinkType)`

                        Tabela de custo:
                        - `"ESPRESSO"`: 30ml de agua, 18g de po.
                        - `"AMERICANO"`: 150ml de agua, 18g de po.

                        Regras, nesta ordem:
                        1. Se `drinkType` nao for nenhum dos dois acima, retorne `"UNKNOWN_DRINK"` (sem gastar nada).
                        2. Se faltar agua OU po para o tipo pedido, retorne `"INSUFFICIENT_RESOURCES"` \
                        **sem alterar** `waterMl`/`beansGrams`.
                        3. Caso contrario, desconte o custo de `waterMl`/`beansGrams` e retorne `"BREWING"`.

                        ## Criterio de sucesso

                        O caminho de falha (recurso insuficiente) nunca pode ter efeito colateral - os \
                        valores de agua e po devem permanecer exatamente como estavam.
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
                                // TODO: tabela de custo, checagem de recurso, desconto so no caminho de sucesso.
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
                .equalsCase("espresso com recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.brew(\"ESPRESSO\")", "\"BREWING\"", true)
                .equalsCase("preparo de sucesso desconta a agua",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.brew(\"ESPRESSO\");", "m.getWaterMl()", "470", false)
                .equalsCase("americano com agua insuficiente nao prepara",
                        "CoffeeMachine m = new CoffeeMachine(100, 100);", "m.brew(\"AMERICANO\")", "\"INSUFFICIENT_RESOURCES\"", true)
                .equalsCase("falha por recurso insuficiente nao altera a agua",
                        "CoffeeMachine m = new CoffeeMachine(100, 100); m.brew(\"AMERICANO\");", "m.getWaterMl()", "100", false)
                .equalsCase("bebida desconhecida retorna UNKNOWN_DRINK",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.brew(\"SUCO\")", "\"UNKNOWN_DRINK\"", true)
                .hint("Comece checando o tipo de bebida; se nao reconhecer, retorne UNKNOWN_DRINK imediatamente.")
                .hint("So decida INSUFFICIENT_RESOURCES depois de saber o custo do tipo pedido.")
                .hint("So subtraia de waterMl/beansGrams DEPOIS de confirmar que ha recurso suficiente.")
                .build();
    }

    private Exercise buildInheritancePolymorphism(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-03-inheritance-polymorphism",
                        module,
                        "Tipos de bebida por heranca",
                        """
                        ## Contexto

                        A tabela de custo hardcoded do exercicio anterior nao escala: cada bebida nova \
                        exigiria mexer no metodo `brew`. Com heranca, cada bebida sabe o proprio custo.

                        ## Objetivo

                        No MESMO arquivo (Java permite varios tipos por arquivo, desde que so um seja \
                        `public`), declare:

                        - `abstract class Drink` com `abstract int waterCost()` e `abstract int beansCost()`.
                        - `class Espresso extends Drink`: custo 30ml agua, 18g po.
                        - `class Americano extends Drink`: custo 150ml agua, 18g po.
                        - `public class CoffeeMachine` com construtor `(int waterMl, int beansGrams)`, \
                        getters, e `String brew(Drink drink)` usando o **despacho polimorfico** \
                        (`drink.waterCost()`/`drink.beansCost()`) em vez de checar o tipo.

                        Mesma regra de negocio do exercicio anterior: sem recurso suficiente, \
                        `"INSUFFICIENT_RESOURCES"` sem efeito colateral; com recurso, desconta e retorna `"BREWING"`.

                        ## Criterio de sucesso

                        O metodo `brew` funciona para QUALQUER `Drink`, inclusive tipos que nao existem \
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
                                return null;
                            }
                        }
                        """,
                        "BASICO", 2, 15)
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
                .equalsCase("Espresso reporta o proprio custo de agua",
                        "", "new Espresso().waterCost()", "30", true)
                .equalsCase("brew com Espresso e recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); Drink d = new Espresso();", "m.brew(d)", "\"BREWING\"", true)
                .equalsCase("brew com Americano e recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); Drink d = new Americano();", "m.brew(d)", "\"BREWING\"", false)
                .equalsCase("despacho polimorfico em lote soma os custos corretos",
                        "java.util.List<Drink> drinks = java.util.List.of(new Espresso(), new Americano()); " +
                                "int total = 0; for (Drink dr : drinks) { total += dr.waterCost(); }",
                        "total", "180", true)
                .equalsCase("recurso insuficiente via Drink nao prepara",
                        "CoffeeMachine m = new CoffeeMachine(10, 100); Drink d = new Americano();", "m.brew(d)", "\"INSUFFICIENT_RESOURCES\"", false)
                .hint("Drink e Espresso/Americano NAO levam o modificador public - so CoffeeMachine leva.")
                .hint("brew(Drink drink) chama drink.waterCost()/drink.beansCost() - nunca verifica o tipo concreto.")
                .hint("A regra de negocio (checar antes de descontar) e identica ao exercicio anterior.")
                .build();
    }

    private Exercise buildInterfaceContract(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-04-interface-contract",
                        module,
                        "Contrato de pagamento",
                        """
                        ## Contexto

                        Cartao e dinheiro sao formas de pagamento completamente diferentes por dentro, \
                        mas a maquina de cafe nao deveria se importar com qual delas esta usando.

                        ## Objetivo

                        Declare, no mesmo arquivo:

                        - `interface PaymentMethod` com `boolean charge(int amountCents)`.
                        - `class CardPayment implements PaymentMethod`: `charge` retorna `true` se \
                        `amountCents > 0`.
                        - `class CashPayment implements PaymentMethod`: construtor recebe \
                        `availableCents`; `charge` so retorna `true` (e desconta) se houver saldo \
                        suficiente; expõe `getAvailableCents()`.
                        - `public class CoffeeMachine(int waterMl, int beansGrams)` com \
                        `String brew(PaymentMethod payment)`. Custo fixo: 30ml agua, 18g po, 500 centavos.

                        Ordem das regras em `brew`:
                        1. Se faltar agua OU po, retorne `"INSUFFICIENT_RESOURCES"` - **sem tentar cobrar**.
                        2. Senao, tente `payment.charge(500)`. Se falhar, retorne `"PAYMENT_DECLINED"` - \
                        **sem descontar recurso**.
                        3. Se cobrar com sucesso, desconte agua/po e retorne `"BREWING"`.

                        ## Criterio de sucesso

                        `brew` funciona identicamente para `CardPayment` e `CashPayment` - a maquina so \
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
                                // TODO: so desconta e retorna true se houver saldo suficiente.
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
                                // TODO: checar recurso, so entao cobrar, so entao descontar.
                                return null;
                            }
                        }
                        """,
                        "BASICO", 3, 15)
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
                .equalsCase("cartao com recurso suficiente prepara",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); PaymentMethod p = new CardPayment();", "m.brew(p)", "\"BREWING\"", true)
                .equalsCase("dinheiro insuficiente e recusado sem descontar recurso",
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
                .hint("A ordem importa: primeiro cheque recurso, so depois tente cobrar.")
                .hint("CashPayment.charge so desconta availableCents quando ha saldo suficiente.")
                .build();
    }

    private Exercise buildCustomException(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-05-custom-exception",
                        module,
                        "Excecao de dominio",
                        """
                        ## Contexto

                        Retornar uma string como `"INSUFFICIENT_RESOURCES"` funciona, mas o chamador \
                        pode simplesmente ignorar o retorno. Uma excecao forca quem chama a lidar com o problema.

                        ## Objetivo

                        Declare `class InsufficientResourcesException extends RuntimeException` cujo \
                        construtor recebe a mensagem (`super(message)`), e implemente:

                        `public class CoffeeMachine(int waterMl, int beansGrams)` com \
                        `void brew(int waterCost, int beansCost)` que:

                        - Lanca `InsufficientResourcesException` com mensagem contendo a palavra \
                        `"water"` se faltar agua.
                        - Lanca `InsufficientResourcesException` com mensagem contendo a palavra \
                        `"beans"` se faltar po (e a agua for suficiente).
                        - Caso contrario, desconta os dois recursos normalmente.

                        ## Criterio de sucesso

                        A excecao certa e lancada para cada recurso faltante, com a mensagem certa, e o \
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
                        "BASICO", 4, 12)
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
                .throwsCase("agua insuficiente lanca InsufficientResourcesException",
                        "", "new CoffeeMachine(10, 100).brew(30, 18)", "InsufficientResourcesException", true)
                .exceptionMessageContainsCase("mensagem da excecao de agua menciona 'water'",
                        "", "new CoffeeMachine(10, 100).brew(30, 18)", "InsufficientResourcesException", "\"water\"", false)
                .throwsCase("po insuficiente lanca InsufficientResourcesException",
                        "", "new CoffeeMachine(500, 5).brew(30, 18)", "InsufficientResourcesException", true)
                .exceptionMessageContainsCase("mensagem da excecao de po menciona 'beans'",
                        "", "new CoffeeMachine(500, 5).brew(30, 18)", "InsufficientResourcesException", "\"beans\"", false)
                .equalsCase("caminho de sucesso desconta a agua",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.brew(30, 18);", "m.getWaterMl()", "470", true)
                .hint("InsufficientResourcesException so precisa de um construtor que chama super(message).")
                .hint("Cheque agua primeiro; so cheque po se a agua for suficiente.")
                .hint("A mensagem de cada excecao deve conter literalmente 'water' ou 'beans'.")
                .build();
    }

    private Exercise buildStateMachine(LearningModule module) {
        return ExerciseBuilder.of(
                        "coffee-06-state-machine",
                        module,
                        "Maquina de estados",
                        """
                        ## Contexto

                        Uma maquina de cafe de verdade nao prepara duas bebidas ao mesmo tempo. Ela tem \
                        um estado, e certas acoes so fazem sentido em certos estados.

                        ## Objetivo

                        Declare `enum MachineState { IDLE, BREWING, DONE, ERROR }` e implemente \
                        `public class CoffeeMachine(int waterMl, int beansGrams)` com estado inicial \
                        `IDLE` e:

                        - `MachineState getState()`.
                        - `void startBrew(int waterCost, int beansCost)`: so pode ser chamado em `IDLE` \
                        (senao lanca `IllegalStateException`). Se houver recurso suficiente, desconta e \
                        vai para `BREWING`. Se nao houver, vai para `ERROR` (sem lancar excecao - \
                        estado, nao excecao, e como este exercicio modela falta de recurso).
                        - `void finishBrew()`: so pode ser chamado em `BREWING` (senao lanca \
                        `IllegalStateException`). Vai para `DONE`.

                        ## Criterio de sucesso

                        As transicoes de estado seguem exatamente IDLE -> BREWING -> DONE no caminho \
                        feliz, IDLE -> ERROR quando falta recurso, e chamadas fora de ordem lancam \
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
                                // TODO: so valido em IDLE; decide entre BREWING e ERROR.
                            }

                            public void finishBrew() {
                                // TODO: so valido em BREWING; vai para DONE.
                            }
                        }
                        """,
                        "INTERMEDIARIO", 5, 15)
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
                .equalsCase("estado inicial e IDLE",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.getState()", "MachineState.IDLE", true)
                .equalsCase("startBrew com recurso suficiente vai para BREWING",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.startBrew(30, 18);", "m.getState()", "MachineState.BREWING", true)
                .equalsCase("startBrew sem recurso vai para ERROR",
                        "CoffeeMachine m = new CoffeeMachine(10, 100); m.startBrew(30, 18);", "m.getState()", "MachineState.ERROR", false)
                .throwsCase("finishBrew a partir de IDLE lanca IllegalStateException",
                        "CoffeeMachine m = new CoffeeMachine(500, 100);", "m.finishBrew()", "java.lang.IllegalStateException", true)
                .equalsCase("caminho feliz completo termina em DONE",
                        "CoffeeMachine m = new CoffeeMachine(500, 100); m.startBrew(30, 18); m.finishBrew();", "m.getState()", "MachineState.DONE", false)
                .hint("O estado inicial e IDLE - defina isso no campo, nao no construtor.")
                .hint("startBrew decide entre BREWING e ERROR sem lancar excecao para recurso insuficiente.")
                .hint("finishBrew so lanca IllegalStateException se o estado atual nao for BREWING.")
                .build();
    }
}
