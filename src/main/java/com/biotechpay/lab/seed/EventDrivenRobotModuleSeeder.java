package com.biotechpay.lab.seed;

import com.biotechpay.lab.domain.Exercise;
import com.biotechpay.lab.domain.LearningModule;
import com.biotechpay.lab.persistence.ExerciseRepository;
import com.biotechpay.lab.persistence.LearningModuleRepository;
import com.biotechpay.lab.seed.support.ExerciseBuilder;
import org.springframework.stereotype.Component;

/**
 * Event-driven track stub: a Robot that reacts to sensor events via listeners instead of direct
 * method calls. Three exercises seeded now; more (multi-sensor fusion, priority queues of events) are
 * backlog for a later session.
 */
@Component
public class EventDrivenRobotModuleSeeder implements ModuleSeeder {

    private final LearningModuleRepository moduleRepository;
    private final ExerciseRepository exerciseRepository;

    public EventDrivenRobotModuleSeeder(LearningModuleRepository moduleRepository, ExerciseRepository exerciseRepository) {
        this.moduleRepository = moduleRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    public String moduleCode() {
        return "event-driven-robot";
    }

    @Override
    public LearningModule seed() {
        LearningModule module = moduleRepository.save(new LearningModule(
                moduleCode(),
                "Orientação a Eventos: Robô com Sensores",
                "EVENT_DRIVEN",
                "Um robô que reage a sensores sem que o código que dispara o evento saiba quem vai " +
                        "reagir a ele. O padrão Observer é a base de arquiteturas orientadas a eventos " +
                        "usadas em sistemas reais, de UI a microsserviços.",
                1));

        exerciseRepository.save(buildObserverBasic(module));
        exerciseRepository.save(buildMultipleEvents(module));
        exerciseRepository.save(buildEventDrivenReaction(module));

        return module;
    }

    private Exercise buildObserverBasic(LearningModule module) {
        return ExerciseBuilder.of(
                        "robot-01-observer-basic",
                        module,
                        "Observer básico",
                        """
                        ## Contexto

                        Um robô detecta obstáculos, mas não deveria saber o que fazer quando isso \
                        acontece - isso é responsabilidade de quem estiver "escutando".

                        ## Objetivo

                        Declare `interface SensorListener { void onObstacleDetected(int distanceCm); }` \
                        e implemente `public class Robot` com:

                        - `void addListener(SensorListener listener)`: registra um listener.
                        - `void notifyObstacle(int distanceCm)`: chama `onObstacleDetected` de TODOS os \
                        listeners registrados, na ordem em que foram adicionados.

                        ## Critério de sucesso

                        `Robot` nunca conhece o que os listeners fazem - ele só os notifica. Múltiplos \
                        listeners devem ser suportados.
                        """,
                        """
                        interface SensorListener {
                            void onObstacleDetected(int distanceCm);
                        }

                        public class Robot {
                            public void addListener(SensorListener listener) { }
                            public void notifyObstacle(int distanceCm) { }
                        }
                        """,
                        """
                        interface SensorListener {
                            void onObstacleDetected(int distanceCm);
                        }

                        public class Robot {
                            // TODO: guarde os listeners numa lista (java.util.List<SensorListener>).

                            public void addListener(SensorListener listener) {

                            }

                            public void notifyObstacle(int distanceCm) {
                                // TODO: chame onObstacleDetected em cada listener registrado.
                            }
                        }
                        """,
                        "INICIANTE", 0, 12)
                .referenceSolution("""
                        interface SensorListener {
                            void onObstacleDetected(int distanceCm);
                        }

                        public class Robot {
                            private final java.util.List<SensorListener> listeners = new java.util.ArrayList<>();

                            public void addListener(SensorListener listener) {
                                listeners.add(listener);
                            }

                            public void notifyObstacle(int distanceCm) {
                                for (SensorListener listener : listeners) {
                                    listener.onObstacleDetected(distanceCm);
                                }
                            }
                        }
                        """)
                .solutionAnnotation(
                        "private final java.util.List<SensorListener> listeners = new java.util.ArrayList<>();",
                        "Robot guarda os listeners numa lista, mas nunca sabe o que cada um faz com o evento - " +
                                "essa é a essência do Observer: quem dispara o evento é totalmente desacoplado de quem reage a ele.")
                .solutionAnnotation(
                        "for (SensorListener listener : listeners) {\n        listener.onObstacleDetected(distanceCm);\n    }",
                        "notifyObstacle percorre TODOS os listeners registrados, na ordem em que foram " +
                                "adicionados - suportar múltiplos observadores é o que torna o padrão útil de verdade.")
                .equalsCase("um listener registrado recebe a distância notificada",
                        "Robot r = new Robot(); int[] captured = new int[1]; " +
                                "r.addListener(d -> captured[0] = d); r.notifyObstacle(15);",
                        "captured[0]", "15", true)
                .equalsCase("dois listeners registrados são ambos notificados",
                        "Robot r = new Robot(); int[] a = new int[1]; int[] b = new int[1]; " +
                                "r.addListener(d -> a[0] = d); r.addListener(d -> b[0] = d); r.notifyObstacle(20);",
                        "a[0] + b[0]", "40", true)
                .equalsCase("notificar sem listeners registrados não lança exceção",
                        "Robot r = new Robot(); r.notifyObstacle(5); boolean ok = true;", "ok", "true", false)
                .hint("SensorListener é uma interface funcional - pode ser implementada com lambda: d -> ...")
                .hint("addListener guarda o listener numa List<SensorListener>.")
                .hint("notifyObstacle percorre a lista e chama onObstacleDetected(distanceCm) em cada um.")
                .build();
    }

    private Exercise buildMultipleEvents(LearningModule module) {
        return ExerciseBuilder.of(
                        "robot-02-multiple-events",
                        module,
                        "Múltiplos tipos de evento",
                        """
                        ## Contexto

                        Um robô real tem mais de um tipo de sensor. Cada tipo de evento deve chegar só \
                        em quem se inscreveu para aquele tipo específico.

                        ## Objetivo

                        Declare duas interfaces funcionais, `ObstacleListener` (`void onObstacleDetected(int distanceCm)`) \
                        e `BatteryListener` (`void onLowBattery(int percentage)`), e implemente \
                        `public class Robot` com listas SEPARADAS para cada tipo:

                        - `addObstacleListener(ObstacleListener)` / `notifyObstacle(int distanceCm)`.
                        - `addBatteryListener(BatteryListener)` / `notifyLowBattery(int percentage)`.

                        ## Critério de sucesso

                        Um `ObstacleListener` nunca deve ser chamado quando `notifyLowBattery` disparar, \
                        e vice-versa - os dois canais são completamente independentes.
                        """,
                        """
                        interface ObstacleListener {
                            void onObstacleDetected(int distanceCm);
                        }

                        interface BatteryListener {
                            void onLowBattery(int percentage);
                        }

                        public class Robot {
                            public void addObstacleListener(ObstacleListener listener) { }
                            public void addBatteryListener(BatteryListener listener) { }
                            public void notifyObstacle(int distanceCm) { }
                            public void notifyLowBattery(int percentage) { }
                        }
                        """,
                        """
                        interface ObstacleListener {
                            void onObstacleDetected(int distanceCm);
                        }

                        interface BatteryListener {
                            void onLowBattery(int percentage);
                        }

                        public class Robot {
                            // TODO: duas listas separadas, uma por tipo de listener.

                            public void addObstacleListener(ObstacleListener listener) {
                                // TODO: adicione a listener na lista de obstacle listeners.
                            }

                            public void addBatteryListener(BatteryListener listener) {
                                // TODO: adicione a listener na lista de battery listeners.
                            }

                            public void notifyObstacle(int distanceCm) {
                                // TODO: percorra a lista de obstacle listeners chamando onObstacleDetected.
                            }

                            public void notifyLowBattery(int percentage) {
                                // TODO: percorra a lista de battery listeners chamando onLowBattery.
                            }
                        }
                        """,
                        "BÁSICO", 1, 12)
                .referenceSolution("""
                        interface ObstacleListener {
                            void onObstacleDetected(int distanceCm);
                        }

                        interface BatteryListener {
                            void onLowBattery(int percentage);
                        }

                        public class Robot {
                            private final java.util.List<ObstacleListener> obstacleListeners = new java.util.ArrayList<>();
                            private final java.util.List<BatteryListener> batteryListeners = new java.util.ArrayList<>();

                            public void addObstacleListener(ObstacleListener listener) {
                                obstacleListeners.add(listener);
                            }

                            public void addBatteryListener(BatteryListener listener) {
                                batteryListeners.add(listener);
                            }

                            public void notifyObstacle(int distanceCm) {
                                for (ObstacleListener listener : obstacleListeners) {
                                    listener.onObstacleDetected(distanceCm);
                                }
                            }

                            public void notifyLowBattery(int percentage) {
                                for (BatteryListener listener : batteryListeners) {
                                    listener.onLowBattery(percentage);
                                }
                            }
                        }
                        """)
                .solutionAnnotation(
                        "private final java.util.List<ObstacleListener> obstacleListeners = new java.util.ArrayList<>();\n    private final java.util.List<BatteryListener> batteryListeners = new java.util.ArrayList<>();",
                        "Duas listas completamente separadas, uma por tipo de evento - é isso que garante que um " +
                                "ObstacleListener jamais seja notificado de um evento de bateria.")
                .solutionAnnotation(
                        "public void notifyLowBattery(int percentage) {\n        for (BatteryListener listener : batteryListeners) {",
                        "notifyLowBattery só percorre batteryListeners - cada canal de evento tem seu próprio " +
                                "caminho de notificação, sem cruzar com o outro.")
                .equalsCase("obstacle listener recebe a distância no evento correto",
                        "Robot r = new Robot(); int[] captured = new int[1]; " +
                                "r.addObstacleListener(d -> captured[0] = d); r.notifyObstacle(30);",
                        "captured[0]", "30", true)
                .equalsCase("battery listener recebe a porcentagem no evento correto",
                        "Robot r = new Robot(); int[] captured = new int[1]; " +
                                "r.addBatteryListener(p -> captured[0] = p); r.notifyLowBattery(12);",
                        "captured[0]", "12", true)
                .equalsCase("obstacle listener não é chamado por evento de bateria",
                        "Robot r = new Robot(); int[] captured = new int[]{-1}; " +
                                "r.addObstacleListener(d -> captured[0] = d); r.notifyLowBattery(10);",
                        "captured[0]", "-1", false)
                .equalsCase("múltiplos obstacle listeners são todos notificados",
                        "Robot r = new Robot(); int[] a = new int[1]; int[] b = new int[1]; " +
                                "r.addObstacleListener(d -> a[0] = d); r.addObstacleListener(d -> b[0] = d); r.notifyObstacle(7);",
                        "a[0] + b[0]", "14", false)
                .hint("Use duas listas independentes: uma para ObstacleListener, outra para BatteryListener.")
                .hint("notifyObstacle só percorre a lista de ObstacleListener; notifyLowBattery só a de BatteryListener.")
                .build();
    }

    private Exercise buildEventDrivenReaction(LearningModule module) {
        return ExerciseBuilder.of(
                        "robot-03-event-driven-reaction",
                        module,
                        "Reação interna a um evento",
                        """
                        ## Contexto

                        Nem todo evento vem de fora - às vezes o próprio robô detecta uma condição \
                        interna (bateria fraca) e precisa reagir mudando seu próprio estado.

                        ## Objetivo

                        Implemente `public class Robot` com estado inicial `"IDLE"` e:

                        - `String getState()`.
                        - `void checkBattery(int level)`: se `level < 20`, o robô reage ao evento de \
                        bateria fraca mudando seu estado para `"RETURNING_TO_BASE"`. Caso contrário, \
                        o estado permanece `"IDLE"`.

                        ## Critério de sucesso

                        A mudança de estado acontece como REAÇÃO ao evento (nível abaixo do limiar), \
                        nunca por uma chamada direta de fora que force o estado.
                        """,
                        """
                        public class Robot {
                            public String getState() { return null; }
                            public void checkBattery(int level) { }
                        }
                        """,
                        """
                        public class Robot {
                            private String state = "IDLE";

                            public String getState() {
                                return state;
                            }

                            public void checkBattery(int level) {
                                // TODO: se level < 20, reaja mudando o estado para RETURNING_TO_BASE.
                            }
                        }
                        """,
                        "INICIANTE", 2, 10)
                .referenceSolution("""
                        public class Robot {
                            private String state = "IDLE";

                            public String getState() {
                                return state;
                            }

                            public void checkBattery(int level) {
                                if (level < 20) {
                                    state = "RETURNING_TO_BASE";
                                }
                            }
                        }
                        """)
                .solutionAnnotation(
                        "private String state = \"IDLE\";",
                        "Estado inicial definido no campo - todo Robot nasce IDLE.")
                .solutionAnnotation(
                        "if (level < 20) {\n        state = \"RETURNING_TO_BASE\";\n    }",
                        "A mudança de estado é uma REAÇÃO ao evento (nível abaixo do limiar), não uma chamada " +
                                "externa forçando o estado - o robô decide sozinho, com base no que percebe.")
                .equalsCase("bateria fraca muda o estado para RETURNING_TO_BASE",
                        "Robot r = new Robot(); r.checkBattery(15);", "r.getState()", "\"RETURNING_TO_BASE\"", true)
                .equalsCase("bateria normal mantém o estado IDLE",
                        "Robot r = new Robot(); r.checkBattery(50);", "r.getState()", "\"IDLE\"", true)
                .equalsCase("limite exato 20 ainda é considerado normal",
                        "Robot r = new Robot(); r.checkBattery(20);", "r.getState()", "\"IDLE\"", false)
                .equalsCase("um abaixo do limite já aciona a reação",
                        "Robot r = new Robot(); r.checkBattery(19);", "r.getState()", "\"RETURNING_TO_BASE\"", false)
                .hint("O estado inicial é IDLE, definido no campo.")
                .hint("A condição é level < 20 (estrito) - 20 exato ainda é normal.")
                .build();
    }
}
