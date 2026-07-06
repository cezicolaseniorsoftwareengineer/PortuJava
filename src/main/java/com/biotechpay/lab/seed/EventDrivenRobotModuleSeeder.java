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
                "Orientacao a Eventos: Robo com Sensores",
                "EVENT_DRIVEN",
                "Um robo que reage a sensores sem que o codigo que dispara o evento saiba quem vai " +
                        "reagir a ele. O padrao Observer e a base de arquiteturas orientadas a eventos " +
                        "usadas em sistemas reais, de UI a microservicos.",
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
                        "Observer basico",
                        """
                        ## Contexto

                        Um robo detecta obstaculos, mas nao deveria saber o que fazer quando isso \
                        acontece - isso e responsabilidade de quem estiver "escutando".

                        ## Objetivo

                        Declare `interface SensorListener { void onObstacleDetected(int distanceCm); }` \
                        e implemente `public class Robot` com:

                        - `void addListener(SensorListener listener)`: registra um listener.
                        - `void notifyObstacle(int distanceCm)`: chama `onObstacleDetected` de TODOS os \
                        listeners registrados, na ordem em que foram adicionados.

                        ## Criterio de sucesso

                        `Robot` nunca conhece o que os listeners fazem - ele so os notifica. Multiplos \
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
                .equalsCase("um listener registrado recebe a distancia notificada",
                        "Robot r = new Robot(); int[] captured = new int[1]; " +
                                "r.addListener(d -> captured[0] = d); r.notifyObstacle(15);",
                        "captured[0]", "15", true)
                .equalsCase("dois listeners registrados sao ambos notificados",
                        "Robot r = new Robot(); int[] a = new int[1]; int[] b = new int[1]; " +
                                "r.addListener(d -> a[0] = d); r.addListener(d -> b[0] = d); r.notifyObstacle(20);",
                        "a[0] + b[0]", "40", true)
                .equalsCase("notificar sem listeners registrados nao lanca excecao",
                        "Robot r = new Robot(); r.notifyObstacle(5); boolean ok = true;", "ok", "true", false)
                .hint("SensorListener e uma interface funcional - pode ser implementada com lambda: d -> ...")
                .hint("addListener guarda o listener numa List<SensorListener>.")
                .hint("notifyObstacle percorre a lista e chama onObstacleDetected(distanceCm) em cada um.")
                .build();
    }

    private Exercise buildMultipleEvents(LearningModule module) {
        return ExerciseBuilder.of(
                        "robot-02-multiple-events",
                        module,
                        "Multiplos tipos de evento",
                        """
                        ## Contexto

                        Um robo real tem mais de um tipo de sensor. Cada tipo de evento deve chegar so \
                        em quem se inscreveu para aquele tipo especifico.

                        ## Objetivo

                        Declare duas interfaces funcionais, `ObstacleListener` (`void onObstacleDetected(int distanceCm)`) \
                        e `BatteryListener` (`void onLowBattery(int percentage)`), e implemente \
                        `public class Robot` com listas SEPARADAS para cada tipo:

                        - `addObstacleListener(ObstacleListener)` / `notifyObstacle(int distanceCm)`.
                        - `addBatteryListener(BatteryListener)` / `notifyLowBattery(int percentage)`.

                        ## Criterio de sucesso

                        Um `ObstacleListener` nunca deve ser chamado quando `notifyLowBattery` disparar, \
                        e vice-versa - os dois canais sao completamente independentes.
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

                            public void addObstacleListener(ObstacleListener listener) { }
                            public void addBatteryListener(BatteryListener listener) { }
                            public void notifyObstacle(int distanceCm) { }
                            public void notifyLowBattery(int percentage) { }
                        }
                        """,
                        "BASICO", 1, 12)
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
                .equalsCase("obstacle listener recebe a distancia no evento correto",
                        "Robot r = new Robot(); int[] captured = new int[1]; " +
                                "r.addObstacleListener(d -> captured[0] = d); r.notifyObstacle(30);",
                        "captured[0]", "30", true)
                .equalsCase("battery listener recebe a porcentagem no evento correto",
                        "Robot r = new Robot(); int[] captured = new int[1]; " +
                                "r.addBatteryListener(p -> captured[0] = p); r.notifyLowBattery(12);",
                        "captured[0]", "12", true)
                .equalsCase("obstacle listener nao e chamado por evento de bateria",
                        "Robot r = new Robot(); int[] captured = new int[]{-1}; " +
                                "r.addObstacleListener(d -> captured[0] = d); r.notifyLowBattery(10);",
                        "captured[0]", "-1", false)
                .equalsCase("multiplos obstacle listeners sao todos notificados",
                        "Robot r = new Robot(); int[] a = new int[1]; int[] b = new int[1]; " +
                                "r.addObstacleListener(d -> a[0] = d); r.addObstacleListener(d -> b[0] = d); r.notifyObstacle(7);",
                        "a[0] + b[0]", "14", false)
                .hint("Use duas listas independentes: uma para ObstacleListener, outra para BatteryListener.")
                .hint("notifyObstacle so percorre a lista de ObstacleListener; notifyLowBattery so a de BatteryListener.")
                .build();
    }

    private Exercise buildEventDrivenReaction(LearningModule module) {
        return ExerciseBuilder.of(
                        "robot-03-event-driven-reaction",
                        module,
                        "Reacao interna a um evento",
                        """
                        ## Contexto

                        Nem todo evento vem de fora - as vezes o proprio robo detecta uma condicao \
                        interna (bateria fraca) e precisa reagir mudando seu proprio estado.

                        ## Objetivo

                        Implemente `public class Robot` com estado inicial `"IDLE"` e:

                        - `String getState()`.
                        - `void checkBattery(int level)`: se `level < 20`, o robo reage ao evento de \
                        bateria fraca mudando seu estado para `"RETURNING_TO_BASE"`. Caso contrario, \
                        o estado permanece `"IDLE"`.

                        ## Criterio de sucesso

                        A mudanca de estado acontece como REACAO ao evento (nivel abaixo do limiar), \
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
                .equalsCase("bateria fraca muda o estado para RETURNING_TO_BASE",
                        "Robot r = new Robot(); r.checkBattery(15);", "r.getState()", "\"RETURNING_TO_BASE\"", true)
                .equalsCase("bateria normal mantem o estado IDLE",
                        "Robot r = new Robot(); r.checkBattery(50);", "r.getState()", "\"IDLE\"", true)
                .equalsCase("limite exato 20 ainda e considerado normal",
                        "Robot r = new Robot(); r.checkBattery(20);", "r.getState()", "\"IDLE\"", false)
                .equalsCase("um abaixo do limite ja aciona a reacao",
                        "Robot r = new Robot(); r.checkBattery(19);", "r.getState()", "\"RETURNING_TO_BASE\"", false)
                .hint("O estado inicial e IDLE, definido no campo.")
                .hint("A condicao e level < 20 (estrito) - 20 exato ainda e normal.")
                .build();
    }
}
