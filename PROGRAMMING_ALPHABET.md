# Alfabetização de Programadores — 10 Exercícios Progressivos

## Missão

Criar um sistema de aprendizado eficiente que ensine programação Java 17 de **verdade**, baseado em cenários reais do dia a dia de um cidadão comum. Cada exercício cobre:

- **Estruturas de Dados**
- **Lógica de Programação**
- **Algoritmos**
- **Paradigmas** (Imperativo, OOP, Funcional)
- **Princípios** (SOLID, DDD, Clean Code, Clean Architecture)

## Por Que Isso Importa

Programadores iniciantes **não conseguem codificar** porque:

1. Aprendem sintaxe isolada sem contexto real
2. Não veem conexão entre conceito e vida prática
3. Não entendem **por que** cada padrão existe
4. Faltam exemplos que fazem sentido

Este currículo resolve com exemplos do **seu dia a dia**: caixa de mercado, mochila, receita, contas domésticas, PIX, etc.

---

## 10 Exercícios

### Exercício 1: Caixa de Mercado — Variáveis e Tipos
**Cenário Real:** Você está no supermercado. Compra 3 itens, o caixa soma tudo, você paga, recebe troco.

**Conceitos:**
- Tipos primitivos (BigDecimal)
- Operadores aritméticos (+, -)
- Variáveis e atribuição
- Entrada/saída básica

**Por que BigDecimal?** Dinheiro tem centavos exatos. `double` e `float` causam erros de arredondamento. No mundo real (fintech, banco), sempre usamos BigDecimal.

**Aprendizado:**
- ✓ Valores monetários exigem tipo certo
- ✓ Validação é responsabilidade do código
- ✓ Operações financeiras são sequenciais

---

### Exercício 2: Mochila para o Dia — Arrays e Listas
**Cenário Real:** Você está saindo de casa. Precisa decidir o que leva na mochila: notebook, fone, caneta, garrafa...

**Conceitos:**
- Arrays vs ArrayList
- Adição dinâmica
- Iteração (for, for-each)
- Índices

**Por que ArrayList?** No início do dia você não sabe quantos itens vai levar. ArrayList cresce automaticamente.

**Aprendizado:**
- ✓ Collections para múltiplos valores
- ✓ Iteração sobre dados
- ✓ Índices começam em 0

---

### Exercício 3: Receita de Bolo — Funções e Métodos
**Cenário Real:** Você segue uma receita passo a passo. Cada passo é reutilizável: "misturar ingredientes" pode ser usado para farinha+ovo ou açúcar+manteiga.

**Conceitos:**
- Métodos (funções)
- Parâmetros e retorno
- Escopo
- Reutilização

**Por que métodos?** Você não copia receita inteira cada vez que cozinha. Métodos são receitas — código reutilizável.

**Aprendizado:**
- ✓ Funções encapsulam lógica
- ✓ Parâmetros tornam reutilizáveis
- ✓ Retorno comunica resultado

---

### Exercício 4: Contas Domésticas — Condicionais
**Cenário Real:** Você recebe o salário (3000), gasta (2500). Sobrou? Sim! Se não, precisa economizar.

**Conceitos:**
- if/else
- Operadores de comparação
- Operadores lógicos (&&, ||)
- Validação

**Por que importa?** Decisões são o coração de qualquer programa. Sem if/else, tudo é linear.

**Aprendizado:**
- ✓ Bifurcação lógica
- ✓ Condições complexas
- ✓ Ordem de operandos importa

---

### Exercício 5: Tarefas Diárias — Loops
**Cenário Real:** Sua rotina: acordar → café → trabalho → voltar → dormir. Você passa pela lista inteira, executando cada passo.

**Conceitos:**
- for (com índice)
- while (condicional)
- do-while (garantir execução)
- Infinite loops

**Por que loops?** Você faz as mesmas coisas dia após dia. Loops automatizam repetição.

**Aprendizado:**
- ✓ Iteração controlada
- ✓ Condições de parada
- ✓ Cuidado com loops infinitos

---

### Exercício 6: Conta Bancária Pessoal — Introdução a POO
**Cenário Real:** Você abre uma conta no banco. Recebe número e saldo inicial. Depois deposita, saca, consulta saldo.

**Conceitos:**
- Classes e objetos
- Encapsulamento (private/public)
- Construtores
- Getters/Setters
- Validação de invariantes

**Por que private?** Ninguém mexe diretamente no seu saldo. Tudo passa por métodos que validam.

**Aprendizado:**
- ✓ Objetos encapsulam estado + comportamento
- ✓ Invariantes protegem integridade
- ✓ Mensagens (métodos) comunicam intenção

---

### Exercício 7: Supermercado com Categorias — Herança e Polimorfismo
**Cenário Real:** No supermercado tem alimentos, eletrônicos, higiene. Cada categoria tem preço e nome, mas funcionam diferente (higiene não se come!).

**Conceitos:**
- Interfaces
- Implementação de contrato
- Polimorfismo
- Coleções polimórficas

**Por que?** Ao invés de 3 classes (Food, Electronics, Hygiene) separadas, você tem 1 interface (Product) que todas implementam. Assim, um Loop chama `produto.getPreco()` para qualquer tipo.

**Aprendizado:**
- ✓ Interfaces definem contrato
- ✓ Polimorfismo permite tratar diferentes tipos igual
- ✓ Extensibilidade: adicionar novo tipo = criar classe, sem mudar código existente

---

### Exercício 8: Biblioteca Pessoal — Abstract Classes e Padrões
**Cenário Real:** Sua biblioteca tem livros, DVDs, revistas. Todos têm título e autor, mas livros têm páginas, DVDs têm minutos.

**Conceitos:**
- Abstract class
- Métodos abstratos
- Design Pattern: Factory
- Strategy Pattern

**Por que abstract class?** Alguns métodos são iguais (título, autor), outros específicos. Abstract class agrupa comum, força subclass implementar específico.

**Aprendizado:**
- ✓ Abstração reduz duplicação
- ✓ Polimorfismo + composição = flexibilidade
- ✓ Factory desacopla criação

---

### Exercício 9: Enviando PIX para Amigos — DDD e Agregados
**Cenário Real:** Você quer mandar 100 reais via PIX para um amigo. Valida chave (CPF, email), sistema processa, confirma.

**Conceitos:**
- Value Objects (imutáveis, validados)
- Agregados (raiz agregada, invariantes)
- Domain Events
- State Machine
- Domínio vs Infraestrutura

**Por que Value Objects?** Uma PixKey (CPF) é imutável — não muda de valor. Se mudar, é outra chave. Equals baseado em valor, não identidade.

**Por que Agregados?** Uma transferência PIX agrupa: remetente, destinatário, valor, status. Tudo junto garante invariantes (ex: não pode processar duas vezes).

**Aprendizado:**
- ✓ DDD separa regra de negócio de tecnologia
- ✓ Value Objects protegem invariantes
- ✓ Agregados são transações lógicas
- ✓ Domain language = código e negócio falam igual

---

### Exercício 10: App Finanças Pessoais — Arquitetura Completa
**Cenário Real:** Dashboard pessoal: vê histórico de transações, saldo, pode fazer transferências.

**Conceitos:**
- Layered Architecture
- Clean Architecture (Domain, Application, Ports, Adapters)
- SOLID Principles
- Dependency Injection
- Repository Pattern
- Service Layer

**Camadas:**

1. **Domain** — Regras de negócio puras (Wallet, Transaction, Balance)
   - Sem dependência de BD, HTTP, nada
   - Testável isoladamente

2. **Application** — Orquestração (WalletService, TransferUseCase)
   - Usa Domain + Repositories
   - Não tem lógica de negócio

3. **Ports** — Contratos abstratos (WalletRepository)
   - Define como falar com mundo externo
   - Implementação desacoplada

4. **Adapters** — Tecnologia (JpaWalletRepository, PixGatewayAdapter)
   - Convertem BD/API em Domain objects
   - Podem trocar sem afetar resto

**Por que?** Você pode:
- Testar Domain sem BD
- Trocar BD sem mudar Domain
- Reusar Domain em aplicações diferentes
- Escalar sem bagunçar código

**Aprendizado:**
- ✓ Arquitetura é inversão de controle
- ✓ Domain puro é testável e durável
- ✓ Ports abstraem tecnologia
- ✓ SOLID = flexibilidade + manutenibilidade

---

## Como Usar

### Para Aprender (Estudante)

1. **Comece pelo Exercício 1** — não pule
2. **Estude os 3 níveis de explicação:**
   - **Simples** — explica como se fala com criança
   - **Prático** — conexão com código e trabalho real
   - **Engineering** — como a JVM e compilador fazem funcionar
3. **Use os Hints** — dicas progressivas, não respostas diretas
4. **Tente sozinho** — solução só se não conseguir
5. **Rode no IDE** — código vive em runtime, não em página

### Para Ensinar (Instrutor/Mentor)

1. **Use cenários reais** — não código abstrato
2. **Mostre 3 níveis** — simplicidade → prática → profundidade
3. **Valide frequência** — exercício a exercício, não todos de vez
4. **Code walkthrough** — explique linha por linha
5. **Pergunte "por quê?"** — não apenas "como?"

### Estrutura Esperada de Solução

Cada exercício tem steps:
- **COPY** — copie e entenda
- **COMPLETE** — preencha os brancos
- **FIX** — encontre e corrija o bug

---

## Mapeamento de Competências

| Exercício | Conceitos | SOLID | Padrões | Paradigma |
|-----------|-----------|-------|---------|-----------|
| 1 | Tipos, Operadores | — | — | Imperativo |
| 2 | Collections, Iteração | SRP | — | Funcional |
| 3 | Métodos, Composição | SRP | Command | Imperativo |
| 4 | Condicionais, Lógica | — | Strategy | Imperativo |
| 5 | Loops, Controle | — | Iterator | Imperativo |
| 6 | POO, Encapsulamento | SRP, OCP | — | OOP |
| 7 | Interfaces, Polimorfismo | LSP, OCP | Factory | OOP |
| 8 | Abstract, Padrões | DIP, OCP | Factory, Strategy | OOP |
| 9 | DDD, Agregados | SRP, DIP | Aggregate, Value Object | Domain |
| 10 | Arquitetura Completa | Todos | Todos | Tudo junto |

---

## Invariantes de Qualidade

Cada exercício mantém:

1. **Realismo** — cenário que você realmente vive
2. **Progressão** — 1→2→3... sem saltos
3. **Completude** — conceito totalmente coberto
4. **Validação** — código roda, testes passam
5. **Clareza** — 3 níveis de explicação
6. **Rastreabilidade** — hints guiam, não resolvem

---

## Visão de Longo Prazo

Depois desses 10 exercícios, você sabe:

- ✓ Escrever código que compila e roda
- ✓ Estruturar dados (types, collections)
- ✓ Controlar fluxo (if, loops, métodos)
- ✓ Organizar em classes e interfaces
- ✓ Validar invariantes
- ✓ Separar domínio de tecnologia
- ✓ Arquitetar aplicação em camadas
- ✓ Ler código outro programador
- ✓ Debugar e testar
- ✓ Colaborar em equipe

**Acima de tudo:** você compreende **por quê** cada coisa existe, não apenas **como** usar.

---

## Próximos Passos (Após Alphabet)

1. **Build a project** — aplicativo real de A→Z
2. **Read open source** — estude código que funciona
3. **Code review** — critique código seu e alheio
4. **Learn test-driven** — escreva testes primeiro
5. **Mergulhe em domínio** — fintech, games, web, IA

---

## Autores

Cezi Cola — Senior Software Engineer | Fintech + Game Engineering Architect  
Época: Junho 2026  
Objetivo: Criar geração de programadores que realmente sabem programar.
