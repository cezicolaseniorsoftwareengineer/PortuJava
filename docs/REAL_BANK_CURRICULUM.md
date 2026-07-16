# Construindo um banco inteiro sozinho Real

## Objetivo

Esta trilha transforma as capacidades observadas no `BioCodeTechPay` em exercícios progressivos de
Java 17. O projeto de referência define os problemas, contratos e falhas reais; as soluções dos
exercícios devem ser implementadas em Java, sem tradução mecânica do código Python.

"100%" significa cobertura rastreável das capacidades listadas neste documento, com solução de
referência compilável, testes visíveis e ocultos e uma etapa final de integração. Não significa
licença bancária, certificação regulatória, auditoria independente ou garantia de contratação.

## Compromisso de formação

O objetivo não é fazer o aluno memorizar o código do `BioCodeTechPay`. O currículo deve torná-lo
capaz de explicar, implementar, testar, operar e reconstruir as decisões que formam um banco digital.
Cada capacidade do sistema de referência precisa aparecer em quatro níveis:

1. **Contexto:** por que a capacidade existe e qual falha real ela evita.
2. **Invariante:** o que nunca pode acontecer com dinheiro, identidade ou autorização.
3. **Implementação:** código produzido pelo aluno, inicialmente isolado e depois integrado.
4. **Evidência:** testes adversariais, logs, métricas, reconciliação ou recuperação executada.

Conclusão de conteúdo não equivale automaticamente a domínio. O sistema comprova competências
observáveis por exercícios e capstones; a proficiência individual depende de prática, revisão e
capacidade de resolver novamente o problema sem copiar a solução.

## Invariantes obrigatórias

1. Valores monetários usam representação decimal explícita e arredondamento definido.
2. Toda movimentação possui identidade, causalidade, correlação e idempotência.
3. Todo lançamento contábil é balanceado; saldo é uma projeção reconstruível do journal.
4. O journal financeiro é append-only e qualquer adulteração deve ser detectável.
5. Nenhum pagamento é confirmado apenas por timeout, resposta visual ou callback isolado.
6. Repetição, concorrência, retry e mensagens fora de ordem não podem duplicar dinheiro.
7. PII, credenciais, PINs e documentos completos nunca aparecem em logs ou respostas inseguras.
8. Falha financeira ambígua bloqueia nova movimentação até consulta ou reconciliação.
9. Integrações externas ficam atrás de ports; adapters não controlam regras do domínio.
10. Toda correção relevante deixa teste de regressão, evidência operacional ou ambos.
11. Primitivas `Atomic*` protegem somente estado em memória na mesma JVM e nunca substituem
    transação persistente, constraint, idempotência, ledger ou coordenação distribuída.

## Matriz curricular integral

| Fase | Módulo | Capacidades de referência | Prova de domínio | Estado |
| --- | --- | --- | --- | --- |
| 1 | Fundação financeira | `money_guard`, ledger, idempotência, `AtomicLong`, CAS versionado, atomicidade, concorrência, reconciliação | Exercícios unitários e adversariais compilados pelo PortuJava | Implementada |
| 2 | Modelagem do banco | Cliente, conta, carteira, moeda, limites, taxas e planos | Agregados com invariantes e testes de fronteira | Implementada |
| 3 | Identidade e acesso | Cadastro, senha, sessão, refresh, cookies, autorização por objeto | Fluxo autenticado com revogação, replay e least privilege | Exercício + gate + projeto |
| 4 | Autorização de pagamentos | PIN, bloqueio temporal, tentativas e recuperação | Máquina de estados resistente a brute force | Exercício + gate + projeto |
| 5 | Passkeys | Challenge nonce, contador de assinatura, cadastro e revogação | Login sem replay e ciclo de vida recuperável | Exercício + gate + projeto |
| 6 | Ledger auditável | Dupla entrada, opening balance, hash chain, imutabilidade e projeção | Journal balanceado, reconstrução e detecção de adulteração | Exercício + gate + projeto |
| 7 | Transferências internas | Débito/crédito atômico, ordenação de locks e overdraft | Teste concorrente sem lost update ou saldo negativo | Exercício + gate + projeto |
| 8 | Pix por chave | Tipos de chave, lookup, intent, envio, recebimento e comprovante | State machine idempotente com rastreabilidade E2E | Exercício + gate + projeto |
| 9 | Pix QR e EMV | Decode, CRC, payload hash, golden corpus e expiração | Parser seguro e bloqueio de QR repetido/adulterado | Exercício + gate + projeto |
| 10 | BaaS e adapters | Payment gateway port, Asaas, timeout, retry e circuit breaker | Adapter por contrato, fake determinístico e falha fechada | Exercício + gate + projeto |
| 11 | Webhooks e settlement | Assinatura, inbox, deduplicação, reorder, liquidação e estorno | Replay sem efeito duplo e transições válidas | Exercício + gate + projeto |
| 12 | Reconciliação | Estado interno, journal, projeção, PSP e reparo auditável | Detecção de drift, compensação e relatório de divergência | Exercício + gate + projeto |
| 13 | Boleto e cobrança | Emissão, baixa, vencimento, payment links e parcelamento | Contratos versionados e contabilização por evento | Exercício + gate + projeto |
| 14 | Tesouraria | Reserva, cofrinho, lastro, tarifas, Selic e rendimento | Fechamento balanceado e disponibilidade correta | Exercício + gate + projeto |
| 15 | KYC, LGPD e PLD/FT | Consentimento, documentos, retenção, mascaramento e revisão | Evidência mínima, acesso restrito e descarte controlado | Exercício + gate + projeto |
| 16 | Antifraude | Strategy rules, score, velocity, limites e revisão humana | Decisão determinística, explicável e versionada | Exercício + gate + projeto |
| 17 | Redis e controles distribuídos | Rate limit, locks, cache, outage e fallback | Falha segura com cache indisponível e recuperação | Exercício + gate + projeto |
| 18 | Operação bancária | Métricas, tracing, readiness, SLO, alertas, backup e replay | Simulações de timeout, crash, carga e desastre | Exercício + gate + projeto |
| 19 | Angular e PWA bancário | Jornadas, acessibilidade, cache, double submit e estados de erro | E2E sem contornar validações do backend | Exercício + gate + projeto |
| 20 | IA financeira governada | Contexto, tools, políticas, dedupe e fallback | Assistente sem autoridade implícita para movimentar dinheiro | Exercício + gate + projeto |
| 21 | Supply chain e entrega | CI, SAST, secrets, SBOM, proveniência, rollout e rollback | Pipeline reproduzível com gates executáveis | Exercício + gate + projeto |
| 22 | Capstone: banco completo | Integração de todas as fases em modular monolith | Cenário end-to-end, chaos, reconciliação e defesa técnica | Exercício + gate + projeto |

## Critério para marcar uma fase como concluída

- Cada capacidade de referência possui ao menos um exercício identificado.
- Toda solução de referência compila e passa pelos mesmos testes usados para corrigir o aluno.
- Existem testes de happy path, fronteira e pelo menos uma falha adversarial relevante.
- O enunciado explica fonte da verdade, invariantes e por que a alternativa ingênua falha.
- A integração com as fases anteriores é exercitada no capstone incremental.
- Conteúdo regulatório instável é identificado como dependente de fonte oficial atualizada.

## Laboratório integrado de repositório vazio

O PortuJava possui a rota `/laboratorio-repositorio`. O aluno começa com um repositório virtual
sem arquivos, cria a árvore do produto, escreve os incrementos e executa gates estruturais das fases
3 a 22. O estado é persistido no `localStorage` do navegador e pode ser exportado como evidência
JSON ou como o repositório real `bank-platform.zip`. Nenhum conteúdo escrito pelo aluno é executado
como shell no servidor.

Os gates do laboratório verificam caminhos e marcadores mínimos. A corretude continua sendo provada
pelos exercícios Java compilados, pelos testes do projeto construído pelo aluno e pela defesa final.
Um arquivo vazio ou uma palavra isolada não constitui domínio técnico.

## Teste final: repositório vazio

O capstone só é concluído quando o aluno, sem copiar a solução de referência:

1. constrói a árvore e o modular monolith no laboratório;
2. materializa e compila o projeto com backend e frontend;
3. passa os gates das fases 3 a 21 e todos os testes de invariantes;
4. cria commits revisáveis e faz push usando PowerShell ou Bash;
5. publica uma versão observável e registra URL, commit e rollback;
6. responde a um incidente com timeout ambíguo, evento duplicado e falha parcial;
7. reconcilia journal, projeções e provedor até drift explicado igual a `0.00`;
8. defende arquitetura, segurança, contabilidade, alternativas, trade-offs e risco residual.

Avaliação estrutural automática não substitui revisão independente de segurança, compliance,
contabilidade, acessibilidade ou operação real.

## Evidência executável de cobertura

A sessão bancária executável possui 36 exercícios:

- 11 exercícios de fundação financeira, cobrindo precisão decimal, dupla entrada, estado,
  idempotência, concorrência, journal, reconciliação, falha externa e atomicidade local.
- 5 exercícios de modelagem, cobrindo cliente e PII, ciclo de vida da conta, carteira multimoeda,
  limites diários idempotentes e cotação determinística de tarifas e planos.
- 20 gates de saída, um para cada fase 3 a 22, cobrindo evidência obrigatória, veto, ensaio de falha
  e artefato incremental no Laboratório de Repositório.

A trilha define todo o caminho, mas a conclusão individual só ocorre quando o aluno produzir e
validar seu próprio banco no capstone. A existência do currículo não certifica automaticamente que
um aluno específico dominou, publicou ou opera um banco em produção.
