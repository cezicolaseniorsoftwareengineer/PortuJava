# PortuJava

A self-hosted Java practice platform: pick an exercise, write the solution in an in-browser
Monaco editor, and get graded by a real `javac`/`java` pipeline against visible and hidden test
cases — not a string match against expected output.

## Why this exists

Most "learn to code" platforms either run untrusted code in a shared cloud sandbox or fake
grading with output comparison. PortuJava runs entirely on `localhost`, compiles and executes the
student's actual class file, and reports pass/fail per test case (including exceptions and stdout
assertions) through a generated JUnit-free test harness.

## Learning tracks

| Track | Focus |
|---|---|
| Orientação a Objetos: Máquina de Café | Encapsulation → business rules → inheritance/polymorphism → interfaces → custom exceptions → a state machine, all on the same domain |
| Orientação a Eventos: Robô com Sensores | The Observer pattern, hand-built |
| Aspectos na Mão: Decorators de Robô | What `@Aspect` does under the hood, before you reach for the annotation |
| Motor de Regras: Automação do Dia a Dia | Layered decision rules with an explicit fallback |
| Algoritmos e Estruturas de Dados | Recursion, choosing the right structure, binary search |
| Os 7 Shapes da Lógica | The "pentatonic scale" of everyday logic: loop-with-state, HashMap, HashSet, two pointers, sliding window, stack, queue |

Every exercise ships a `referenceSolution` that is graded through the same compiler-backed path a
student's submission goes through — if the reference doesn't pass its own tests, the build fails.

## Features

- Real compilation and execution per submission, with a timeout and output cap (see
  `JavaCodeCompiler` for the documented sandboxing tradeoff)
- Progressive hints (revealed one at a time) and a full solution reveal, gated behind an explicit
  "Revelar resposta completa" click so the answer is never shown by default
- Responsive two-panel IDE (statement + editor) that adapts across desktop, tablet and mobile
- Per-track content seeding that reaches an existing player database when a new track ships,
  without wiping progress

## Stack

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, Spring Security (permissive — single-machine
  tool, no auth surface yet), H2 (file-based, survives restarts)
- **Frontend:** Angular 18 (standalone components), Monaco Editor
- **Build:** Maven, orchestrating an `ng build` via `frontend-maven-plugin` into
  `src/main/resources/static`

## Running locally

Requires JDK 17 and Maven. Node/npm are fetched automatically by the Maven build.

```bash
mvn spring-boot:run
```

The app compiles the frontend, starts on a fixed port (`62828`), and opens
`http://localhost:62828/` in your default browser automatically. Disable the auto-open with
`--app.open-browser=false`.

## Testing

```bash
mvn test
```

Covers the grading engine (compiler + harness generation), the submission/exercise services, and a
seed-sanity suite that grades every track's reference solutions through the real pipeline before
trusting them.

## Architecture notes

- `domain` / `persistence`: `LearningModule → Exercise → TestCase`, with `ComparisonMode` (EQUALS,
  THROWS, STDOUT_CONTAINS, EXCEPTION_MESSAGE_CONTAINS) making new grading semantics additive rather
  than a schema change
- `application.JavaCodeCompiler` / `TestHarnessGenerator`: the actual grading engine
- `seed`: one `ModuleSeeder` implementation per track (`ContentSeeder` is an orchestrator only) —
  adding a track means adding one file, not editing a god-class
