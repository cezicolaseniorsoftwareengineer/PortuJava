# Como Testar a Alfabetização de Programadores

## ✅ Pré-requisitos

- Java 17+ instalado
- Maven 3.8.1+
- Git (opcional)
- Postman ou curl (para testar API)

---

## 🚀 Iniciando o Servidor

### 1. Compilar Projeto
```bash
cd "c:\Users\LENOVO\OneDrive\Área de Trabalho\PortuJava_Game"
mvn clean compile
```

**Esperado:**
```
[INFO] BUILD SUCCESS
✅ Compilado sem erros
```

### 2. Iniciar Servidor Spring Boot
```bash
mvn spring-boot:run
```

**Esperado:**
```
[...INFO...] Started Java17PracticeLabApplication in 5.859 seconds
✅ Servidor rodando em http://localhost:8080
```

---

## 🧪 Testando os Endpoints

### Teste 1: Submeter Solução CORRETA

**CURL:**
```bash
curl -X POST http://localhost:8080/api/exercise/submit \
  -H "Content-Type: application/json" \
  -d '{
    "lessonId": "abc-01-caixa-mercado",
    "stepIndex": 0,
    "studentCode": "BigDecimal paoFrances = new BigDecimal(\"4.50\");\nBigDecimal leite = new BigDecimal(\"3.20\");\nBigDecimal ovos = new BigDecimal(\"12.00\");",
    "elapsedMs": 45000
  }'
```

**Resposta Esperada:**
```json
{
    "isCorrect": true,
    "feedback": [
        "✅ Solução CORRETA!",
        "Conceito: Declarar variáveis de preço",
        "Próximo passo: Somar todos os preços (total da compra)"
    ],
    "hints": [],
    "output": "",
    "accuracy": 100,
    "passed": true,
    "errors": []
}
```

✅ **Resultado:** Feedback positivo, próximo passo indicado

---

### Teste 2: Submeter Solução INCORRETA

**CURL:**
```bash
curl -X POST http://localhost:8080/api/exercise/submit \
  -H "Content-Type: application/json" \
  -d '{
    "lessonId": "abc-01-caixa-mercado",
    "stepIndex": 0,
    "studentCode": "double paoFrances = 4.50;\ndouble leite = 3.20;\ndouble ovos = 12.00;",
    "elapsedMs": 120000
  }'
```

**Resposta Esperada:**
```json
{
    "isCorrect": false,
    "feedback": [
        "Use BigDecimal para valores monetários, não double ou float"
    ],
    "hints": [
        "Use BigDecimal para dinheiro, não double",
        "Cada item tem um nome e um preço",
        "Separe os preços com ponto e vírgula",
        "new BigDecimal(\"4.50\") — note as aspas",
        "BigDecimal paoFrances = new BigDecimal(\"4.50\");"
    ],
    "output": "",
    "accuracy": 30,
    "passed": false,
    "errors": [
        "Use BigDecimal para valores monetários, não double ou float"
    ]
}
```

✅ **Resultado:** Erro específico + hints progressivos + acurácia baixa

---

### Teste 3: Compilar e Executar Código

**CURL:**
```bash
curl -X POST http://localhost:8080/api/exercise/compile-and-run \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BigDecimal paoFrances = new BigDecimal(\"4.50\");\nBigDecimal leite = new BigDecimal(\"3.20\");\nBigDecimal ovos = new BigDecimal(\"12.00\");\nBigDecimal total = paoFrances.add(leite).add(ovos);\nSystem.out.println(\"Total: R$ \" + total);"
  }'
```

**Resposta Esperada:**
```json
{
    "success": true,
    "feedback": [
        "✅ Compilação bem-sucedida!",
        "Saída do programa:",
        "Total: R$ 19.70"
    ],
    "output": "Total: R$ 19.70",
    "errors": [],
    "warnings": []
}
```

✅ **Resultado:** Código compilado + output correto

---

### Teste 4: Erro de Compilação

**CURL:**
```bash
curl -X POST http://localhost:8080/api/exercise/compile-and-run \
  -H "Content-Type: application/json" \
  -d '{
    "code": "BigDecimal paoFrances = new BigDecimal(\"4.50\""
  }'
```

**Resposta Esperada:**
```json
{
    "success": false,
    "feedback": [
        "❌ Erros de compilação:",
        "[erro de sintaxe capturado]"
    ],
    "output": "",
    "errors": [
        "Linha X: ... error ..."
    ],
    "warnings": []
}
```

✅ **Resultado:** Erro capturado e reportado

---

## 🎯 Testando Todos os 10 Exercícios

### Exercício 1: Caixa de Mercado
```bash
# Step 0: Declarar variáveis
curl ... "abc-01-caixa-mercado", stepIndex: 0, ...

# Step 1: Somar preços
curl ... "abc-01-caixa-mercado", stepIndex: 1, ...

# Step 2: Receber pagamento
curl ... "abc-01-caixa-mercado", stepIndex: 2, ...

# Step 3: Calcular troco (FIX mode)
curl ... "abc-01-caixa-mercado", stepIndex: 3, ...

# Step 4: Exibir recibo
curl ... "abc-01-caixa-mercado", stepIndex: 4, ...
```

### Exercício 2: Mochila
```bash
# Step 0: Criar lista
curl ... "abc-02-mochila-dia", stepIndex: 0, ...

# Step 1: Adicionar itens
curl ... "abc-02-mochila-dia", stepIndex: 1, ...

# Step 2: Acessar item
curl ... "abc-02-mochila-dia", stepIndex: 2, ...

# Step 3: Iterar
curl ... "abc-02-mochila-dia", stepIndex: 3, ...
```

*(Mesmo padrão para Exercícios 3-10)*

---

## 🧩 Testando com Postman

### 1. Criar Nova Request
- Método: **POST**
- URL: `http://localhost:8080/api/exercise/submit`
- Header: `Content-Type: application/json`

### 2. Body (JSON)
```json
{
    "lessonId": "abc-01-caixa-mercado",
    "stepIndex": 0,
    "studentCode": "BigDecimal paoFrances = new BigDecimal(\"4.50\");",
    "elapsedMs": 45000
}
```

### 3. Send
- Clique em **Send**
- Veja resposta em **Body**

### 4. Validar Resposta
- ✅ Status 200 OK
- ✅ JSON bem formado
- ✅ `isCorrect` = true/false conforme esperado

---

## 📊 Validações por Exercício

### Exercício 1 (Caixa)
```
❌ Errado: double paoFrances = 4.50;
✅ Certo: BigDecimal paoFrances = new BigDecimal("4.50");

Erro: "Use BigDecimal para valores monetários, não double ou float"
Hint: "Comece com: BigDecimal paoFrances = new BigDecimal("
```

### Exercício 2 (Mochila)
```
❌ Errado: int[] itens = {1, 2, 3};
✅ Certo: List<String> itens = new ArrayList<>();

Erro: "Use ArrayList para guardar múltiplos itens"
Hint: "List<String> itens = new ArrayList<>();"
```

### Exercício 3 (Receita)
```
❌ Errado: public void misturarIngredientes() {}
✅ Certo: public String misturarIngredientes(String ing1, String ing2) { return ...; }

Erro: "Método precisa retornar String"
Hint: "public String misturarIngredientes(String ing1, String ing2)"
```

### Exercício 4 (Contas)
```
❌ Errado: if (gastoMes.compareTo(salario) > 0) { ... }
✅ Certo: if (salario.compareTo(gastoMes) >= 0) { ... }

Erro: "Ordem invertida!"
Hint: "salario.compareTo(gastoMes) > 0 é correto"
```

### Exercício 5 (Tarefas)
```
❌ Errado: while (i < tarefas.size()) { ... } // sem i++
✅ Certo: while (i < tarefas.size()) { ... i++; }

Erro: "Loop infinito!"
Hint: "Sem incremento: loop infinito!"
```

### Exercícios 6-10
Seguem o mesmo padrão com validações específicas

---

## 🔍 Analisando Respostas

### Resposta Bem-Sucedida
```json
{
    "isCorrect": true,          // ✅ Validação passou
    "feedback": ["✅ Correto"],  // ✅ Mensagem positiva
    "hints": [],                 // ✅ Sem hints necessárias
    "accuracy": 100,             // ✅ 100% de acurácia
    "passed": true,              // ✅ Passo completo
    "errors": []                 // ✅ Sem erros
}
```

**Ação:** Avançar para próximo passo

### Resposta com Erro
```json
{
    "isCorrect": false,          // ❌ Validação falhou
    "feedback": ["...erro..."],  // ❌ Mensagem de erro
    "hints": ["...", "...", ...], // 💡 Hints disponíveis
    "accuracy": 35,              // ⚠️ Acurácia baixa
    "passed": false,             // ❌ Passo incompleto
    "errors": ["..."]            // ❌ Erros capturados
}
```

**Ação:** Mostrar erro + hints, permitir nova tentativa

---

## 📈 Métricas de Teste

### Taxa de Sucesso Esperada
- Primeira tentativa: ~30-40%
- Segunda tentativa: ~60-70%
- Terceira tentativa: ~85-95%
- Após hints: ~95%+

### Tempo Médio por Step
- Iniciante: 2-5 minutos
- Intermediário: 1-3 minutos
- Experiente: <1 minuto

### Acurácia Esperada
- Primeira tentativa: 30-80%
- Corrigida: 95-100%

---

## 🐛 Debugging

### Problema: Compilador não encontrado
```
❌ Erro: "java" ou "javac" não é reconhecido
✅ Solução: Adicionar Java ao PATH
  setx JAVA_HOME "C:\Program Files\Java\jdk-17"
  setx PATH "%PATH%;%JAVA_HOME%\bin"
```

### Problema: Porta 8080 em uso
```
❌ Erro: "Port 8080 already in use"
✅ Solução: Trocar porta
  mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

### Problema: Resposta vazia
```
❌ Erro: { } ou null
✅ Solução: Verificar logs do servidor
  Checar stdout do terminal onde rodar mvn spring-boot:run
```

---

## ✅ Checklist de Validação

- [ ] Compilação bem-sucedida (`mvn clean compile`)
- [ ] Servidor inicia (`mvn spring-boot:run`)
- [ ] POST /api/exercise/submit funciona
- [ ] POST /api/exercise/compile-and-run funciona
- [ ] Solução correta é validada como true
- [ ] Solução incorreta é validada como false
- [ ] Hints são fornecidos em caso de erro
- [ ] Acurácia é calculada corretamente
- [ ] Todos os 10 exercícios podem ser testados
- [ ] Feedback é pedagogicamente útil

---

## 📞 Suporte

Se algo não funcionar:

1. Verifique compilação:
   ```bash
   mvn clean compile -X 2>&1 | grep ERROR
   ```

2. Verifique servidor:
   ```bash
   curl http://localhost:8080/api/exercise/submit
   ```

3. Verifique logs:
   ```bash
   # Terminal deve mostrar:
   [INFO] Started Java17PracticeLabApplication in X seconds
   ```

4. Verifique JSON:
   ```bash
   # Use jsonlint ou ferrament online para validar
   ```

---

## 🎓 Próximo Passo

Após confirmar que tudo funciona:

1. Implementar Frontend (Fase 3)
2. Adicionar Dashboard de Progresso
3. Implementar Gamificação
4. Deploy em produção

**Parabéns! Sistema de Alfabetização está funcionando.** 🎉
