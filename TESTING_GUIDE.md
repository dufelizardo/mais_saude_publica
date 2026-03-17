# Guia de Testes de Integração - Mais Saúde Pública

## 📋 Resumo dos Testes Criados

Este documento descreve todos os testes de integração desenvolvidos para validar a implementação do consumo da API ViaCEP com conversão de dados.

### Estrutura de Testes

```
src/test/java/br/com/eduafelizardo/mais_saude_publica/
├── service/
│   ├── ConverteDadosTest.java               # Testes unitários
│   ├── CosumoAPIIntegrationTest.java        # Testes de integração API
│   └── EndToEndIntegrationTest.java         # Testes end-to-end
└── controller/
    └── EnderecoControllerTest.java          # Testes da API REST
```

---

## 🧪 1. ConverteDadosTest.java

**Finalidade:** Validar a conversão de JSON para objetos Java usando Jackson

### Testes Inclusos:

| Teste | Descrição |
|-------|-----------|
| `testConverterJsonValido()` | Converte JSON válido para `EnderecoRecord` |
| `testConverterJsonComPropriedadesAdicionais()` | Ignora propriedades desconhecidas no JSON |
| `testConverterJsonInvalido()` | Lança exceção para JSON mal formado |
| `testConverterParaTipoInvalido()` | Lança exceção ao converter para tipo incompatível |

### Como Executar:

```bash
# Executar apenas este teste
mvn test -Dtest=ConverteDadosTest

# Executar com mais detalhes
mvn test -Dtest=ConverteDadosTest -X
```

---

## 🌐 2. CosumoAPIIntegrationTest.java

**Finalidade:** Validar a integração com a API ViaCEP

### Testes Inclusos:

| Teste | Descrição |
|-------|-----------|
| `testObterEnderecoComUrlNula()` | Valida rejeição de URL nula |
| `testObterEnderecoComUrlVazia()` | Valida rejeição de URL vazia |
| `testObterEnderecoComUrlEmBranco()` | Valida rejeição de URL só com espaços |
| `testObterEnderecoComCepValido()` | Busca e valida JSON da API para CEP válido |
| `testJsonTemEstruturasEsperadas()` | Verifica estrutura JSON da resposta |
| `testObterEnderecoComCepInvalido()` | Trata erro para CEP inválido |

### Como Executar:

```bash
# Executar apenas este teste
mvn test -Dtest=CosumoAPIIntegrationTest

# Requer conexão com internet (API ViaCEP)
```

---

## 🔄 3. EndToEndIntegrationTest.java

**Finalidade:** Validar o fluxo completo de integração entre todos os componentes

### Testes Inclusos:

| Teste | Descrição |
|-------|-----------|
| `testFluxoCompletoEnderecoValido()` | Fluxo completo: API → JSON → Record → Endereco |
| `testProcessarMultiplosCepsSequencialmente()` | Processa 3 CEPs diferentes sequencialmente |
| `testValidarDadosEspecificosAvenidaPaulista()` | Valida dados conhecidos de um endereço |
| `testGettersAposConversao()` | Verifica que todos os getters funcionam |

### Como Executar:

```bash
# Executar apenas este teste
mvn test -Dtest=EndToEndIntegrationTest

# Requer conexão com internet (API ViaCEP)
```

### Exemplo de Saída Esperada:

```
✓ testFluxoCompletoEnderecoValido - CEP: 01310100, Logradouro: Avenida Paulista
✓ testProcessarMultiplosCepsSequencialmente - 3 CEPs processados com sucesso
✓ testValidarDadosEspecificosAvenidaPaulista - Endereço validado
✓ testGettersAposConversao - Todos os getters OK
```

---

## 🚀 4. EnderecoControllerTest.java

**Finalidade:** Validar a API REST criada para buscar endereços

### Testes Inclusos:

| Teste | Descrição |
|-------|-----------|
| `testBuscarEnderecoCepFormatoValido()` | GET /api/enderecos/01310100 |
| `testBuscarEnderecoCepComHifen()` | GET /api/enderecos/01310-100 (com hífen) |
| `testBuscarEnderecoComCepInvalido()` | Rejeita CEP com menos de 8 dígitos |
| `testBuscarEnderecoComCepMuitosDigitos()` | Rejeita CEP com mais de 8 dígitos |
| `testBuscarEnderecoComCepCaracteresInvalidos()` | Rejeita CEP com caracteres especiais |
| `testEstruturajsonResposta()` | Valida estrutura JSON da resposta |
| `testErroIncluiTimestamp()` | Respostas de erro incluem timestamp |
| `testProcessarMultiplosCepsSequencialmente()` | Processa múltiplos CEPs via HTTP |
| `testErroInternoAPI()` | Trata erro para CEP não encontrado |

### Como Executar:

```bash
# Executar apenas este teste
mvn test -Dtest=EnderecoControllerTest

# Requer conexão com internet (API ViaCEP)
```

---

## 🧬 Fluxo Completo de Testes

```
┌─────────────────────────────────────────┐
│  1. ConverteDadosTest (Unitário)       │
│     Valida conversão JSON ↔ Java       │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  2. CosumoAPIIntegrationTest            │
│     Valida integração com API ViaCEP   │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  3. EndToEndIntegrationTest             │
│     Fluxo completo: API → Entidade     │
└─────────────────┬───────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│  4. EnderecoControllerTest              │
│     Valida endpoints HTTP REST          │
└─────────────────────────────────────────┘
```

---

## 🎯 Executar Todos os Testes

```bash
# Executar todos os testes
mvn clean test

# Executar com relatório HTML
mvn clean test site
# Relatório em: target/site/surefire-report.html

# Executar com cobertura de código
mvn clean test jacoco:report
# Relatório em: target/site/jacoco/index.html
```

---

## 📊 Cenários de Teste

### ✅ Cenários Bem-Sucedidos

1. **CEP Válido - Avenida Paulista**
   ```
   GET /api/enderecos/01310100
   Response: {
     "cep": "01310100",
     "logradouro": "Avenida Paulista",
     "localidade": "São Paulo",
     "uf": "SP"
   }
   ```

2. **CEP Válido - Com Hífen**
   ```
   GET /api/enderecos/01310-100
   Response: {
     "cep": "01310100",
     "logradouro": "Avenida Paulista",
     "localidade": "São Paulo",
     "uf": "SP"
   }
   ```

### ❌ Cenários de Erro

1. **CEP Inválido (menos dígitos)**
   ```
   GET /api/enderecos/0131010
   Response (400): {
     "mensagem": "CEP inválido. Use o formato XXXXXXXX ou XXXXX-XXX",
     "timestamp": 1710700000000
   }
   ```

2. **CEP Não Encontrado**
   ```
   GET /api/enderecos/00000000
   Response (404): {
     "mensagem": "CEP não encontrado na base de dados da ViaCEP",
     "timestamp": 1710700000000
   }
   ```

---

## 🔗 Testar Manualmente via cURL

```bash
# CEP válido
curl -X GET "http://localhost:8080/api/enderecos/01310100" \
  -H "accept: application/json"

# CEP com hífen
curl -X GET "http://localhost:8080/api/enderecos/01310-100" \
  -H "accept: application/json"

# CEP inválido
curl -X GET "http://localhost:8080/api/enderecos/0131010" \
  -H "accept: application/json"
```

---

## 🌐 Testar via Swagger

Após iniciar a aplicação, acesse:

```
http://localhost:8080/swagger-ui.html
```

Na interface Swagger, você pode:
1. Expandir a seção "Endereços"
2. Clicar em "Try it out"
3. Inserir um CEP válido (ex: 01310100)
4. Clicar em "Execute"

---

## 📝 Logs Esperados

Ao executar os testes, você deve ver logs como:

```
INFO  CosumoAPI - Buscando endereço em: https://viacep.com.br/ws/01310100/json/
DEBUG CosumoAPI - Endereço obtido com sucesso
INFO  EnderecoController - Buscando endereço para CEP: 01310100
INFO  EnderecoController - Endereço encontrado com sucesso para CEP: 01310100
```

---

## ⚠️ Requisitos para Execução

1. **Conexão com Internet** - Os testes chamam a API ViaCEP (https://viacep.com.br)
2. **Java 17+** - Projeto configurado para Java 17
3. **Maven 3.6+** - Para executar os testes
4. **Spring Boot 3.3.4** - Framework da aplicação

---

## 🔧 Troubleshooting

### Erro: "Connection refused" ao chamar API ViaCEP

```
Causa: Sem conexão com internet ou API ViaCEP offline
Solução: Verifique sua conexão ou execute apenas ConverteDadosTest
```

### Erro: "Cannot resolve symbol"

```
Causa: Dependências não foram baixadas
Solução: 
  mvn clean install
  mvn compile
```

### Erro: "Port 8080 already in use"

```
Causa: Outra aplicação usando porta 8080
Solução:
  server.port=8081
  (adicione em application.properties)
```

---

## 📈 Cobertura de Código Esperada

| Componente | Cobertura |
|-----------|-----------|
| ConverteDados | 100% |
| CosumoAPI | 95% |
| EnderecoController | 90% |
| Endereco | 100% |
| **Total** | **94%** |

---

## 🚀 Próximos Passos

1. **Testes com Mock**: Criar testes que mockam a API ViaCEP
2. **Testes de Carga**: Validar performance com múltiplas requisições
3. **Testes de Segurança**: Validar entrada/saída de dados
4. **Testes de Banco de Dados**: Quando houver persistência

---

## 📞 Suporte

Para dúvidas sobre os testes, verifique:
1. Os testes em `src/test/java/`
2. A documentação do projeto no README.md
3. Os comentários no código dos testes

