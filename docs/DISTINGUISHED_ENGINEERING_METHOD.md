# Método Distinguished de engenharia

## O que este método é

Este documento ensina um processo reproduzível de resolução de problemas. Ele não tenta copiar a
mente de uma pessoa, não revela raciocínio privado e não afirma conhecer práticas confidenciais de
Google, Apple, Meta, Netflix, Amazon, Tesla, OpenAI, Anthropic ou instituições financeiras.

A síntese combina práticas públicas: mudanças pequenas e revisão de código do Google, SRE e
postmortems, foco de produto, transparência e recuperação da Apple, mecanismos operacionais da
Amazon, produção orientada a SLO da Meta, e desenvolvimento orientado a avaliações e feedback loops
publicado por OpenAI e Anthropic. O resultado é medido por evidência, não por marca ou cargo.

## O ciclo OMCIVL

### 1. Observar

Antes de propor código:

1. Reproduza o comportamento real.
2. Localize a fonte da verdade: código, banco, contrato, runtime ou norma vigente.
3. Separe fato observado, hipótese, inferência e risco previsto.
4. Defina quem e o que está dentro do escopo autorizado.
5. Escreva o resultado verificável em uma frase.

Pergunta de controle: **qual evidência faria eu mudar de opinião?**

### 2. Modelar

Modele atores, ativos, estados, transições, dependências e limites. Em finanças, escreva antes os
invariantes:

- dinheiro não nasce nem desaparece sem contrapartida;
- repetição e concorrência não duplicam efeito;
- estado ambíguo não é confirmado por conveniência;
- saldo deriva de journal reconciliável;
- autorização é validada no objeto e no momento da ação;
- falha deixa caminho de recuperação e trilha de auditoria.

Pergunta de controle: **o que nunca pode acontecer, mesmo sob timeout, retry ou crash?**

### 3. Contestar

Tente invalidar a alternativa preferida antes de investir nela:

- duas requisições simultâneas;
- mensagem duplicada, atrasada ou fora de ordem;
- timeout antes e depois do commit;
- dependência indisponível ou inconsistente;
- credencial revogada, replay e autorização por objeto;
- rollback incompleto, backup inválido e drift de reconciliação;
- usuário com bundle antigo, duas abas ou double submit;
- abuso intencional e entrada maliciosa.

Pergunta de controle: **como um operador cansado, uma rede instável ou um atacante quebraria isto?**

### 4. Implementar

Escolha a menor mudança que preserve as invariantes, seja reversível e mantenha as fronteiras. Evite
misturar refatoração, mudança funcional e migração irreversível. O núcleo de domínio não depende de
Spring, HTTP, JPA, Redis ou PSP; essas tecnologias entram por ports e adapters.

Pergunta de controle: **qual é a menor unidade autocontida que melhora o sistema sem quebrar o build?**

### 5. Verificar

Execute testes proporcionais ao risco:

1. unitário e de propriedade do domínio;
2. integração com banco e constraints reais;
3. contrato de API, fila, webhook e PSP;
4. concorrência, falha, recovery, carga e segurança;
5. build do frontend, E2E e cache/PWA;
6. probe em staging e observação pós-deploy.

Teste não é evidência apenas porque ficou verde. Confirme que falha quando a implementação é
quebrada e que o grader não pode ser enganado por texto ou caminho alternativo.

Pergunta de controle: **qual afirmação exata este teste prova, e o que ele ainda não prova?**

### 6. Legar

Transforme o aprendizado em um mecanismo que sobreviva ao autor:

- teste de regressão para o contraexemplo;
- ADR para decisão duradoura;
- runbook para operação e recuperação;
- SLO, alerta e dashboard para comportamento observado;
- política no CI para impedir recorrência;
- postmortem sem culpa para incidente;
- corpus dourado para contratos e payloads.

Pergunta de controle: **se eu sair hoje, outra pessoa consegue explicar, operar e recuperar isto?**

## Antes, durante e na entrega

| Momento | Saída obrigatória | Gate de bloqueio |
| --- | --- | --- |
| Antes | problema, fonte da verdade, invariantes, alternativas e sucesso mensurável | escopo não autorizado ou hipótese tratada como fato |
| Durante | mudança pequena, testes junto do código, logs correlacionados e contraditório | falha de segurança, dinheiro não reconciliado ou caminho irreversível desconhecido |
| Entrega | diff revisado, CI verde, rollout, rollback, SLO, runbook e evidência | teste requerido vermelho, segredo exposto ou veto material pendente |
| Depois | observação real, reconciliação, incidente fechado e regressão legada | ausência de fonte da verdade ou drift sem explicação |

## Árvore Distinguished do banco construído pelo aluno

Não existe uma árvore universal que comece sempre em `src`, `data` ou `app`. A raiz representa o
produto e seu contrato de construção. Para o capstone em monorepo:

```text
bank-platform/
|-- README.md
|-- pom.xml                         # agregador do monorepo
|-- mvnw
|-- mvnw.cmd
|-- .mvn/wrapper/maven-wrapper.properties
|-- backend/
|   |-- pom.xml
|   |-- src/main/java/com/portujava/bank/
|   |   |-- domain/                 # invariantes, agregados e value objects
|   |   |-- application/            # casos de uso
|   |   |-- ports/                  # contratos de entrada e saída
|   |   `-- adapters/               # HTTP, JPA, PSP, filas e cache
|   `-- src/test/java/com/portujava/bank/
|-- frontend/
|   |-- package.json
|   `-- src/app/
|       |-- core/
|       |-- shared/
|       `-- features/
|-- contracts/
|   |-- openapi/
|   `-- events/
|-- infrastructure/
|   |-- docker/
|   |-- terraform/
|   `-- observability/
|-- docs/
|   |-- adr/
|   |-- runbooks/
|   |-- threat-model/
|   `-- final-defense/
|-- scripts/
`-- .github/workflows/
```

`data/` pode existir para fixtures públicas, migrations ou datasets sanitizados, mas não armazena
segredos nem representa a fonte da verdade de produção. `app/` é convenção válida em stacks que a
adotam, especialmente Python; em Java/Spring, `src/main/java` e `src/test/java` são convenções de
build mais previsíveis.

## Git: PowerShell e Bash

No Windows PowerShell:

```powershell
git status --short
git diff --check
.\mvnw.cmd test
git add <arquivos-revisados>
git commit -m "Implement verified bank increment"
git push origin <branch>
```

No macOS, Ubuntu ou outra distribuição Linux com Bash:

```bash
git status --short
git diff --check
./mvnw test
git add <arquivos-revisados>
git commit -m "Implement verified bank increment"
git push origin <branch>
```

O shell muda a invocação do wrapper; a disciplina Git não muda. Revise antes de adicionar, escolha
arquivos conscientemente, nunca versione segredos e não faça push de uma unidade que não compila.

## Fontes públicas da síntese

- Google Engineering Practices, code review e small changes:
  <https://google.github.io/eng-practices/review/> e
  <https://google.github.io/eng-practices/review/developer/small-cls.html>
- Google SRE, operação, monitoramento e postmortems: <https://sre.google/sre-book/part-III-practices/>
- Apple Human Interface Guidelines, propósito, agência, responsabilidade e feedback:
  <https://developer.apple.com/design/human-interface-guidelines/design-principles>
- Apple Security, autenticação, autorização, proteção de dados e code signing:
  <https://developer.apple.com/documentation/security/>
- Amazon Builders' Library, arquitetura, entrega e operação:
  <https://aws.amazon.com/builders-library/>
- Meta Production Engineering e SLOs:
  <https://engineering.fb.com/2025/01/21/production-engineering/> e
  <https://engineering.fb.com/2021/12/13/production-engineering/slick/>
- OpenAI, evals e feedback loops:
  <https://openai.com/index/evals-drive-next-chapter-of-ai/> e
  <https://openai.com/index/harness-engineering/>
- Anthropic, avaliação de agentes e graders resistentes a atalhos:
  <https://www.anthropic.com/engineering/demystifying-evals-for-ai-agents>

Essas fontes sustentam princípios delimitados. Elas não sustentam a alegação de que todas as
empresas pensam igual nem que o método garante contratação, licença bancária ou sucesso em produção.
