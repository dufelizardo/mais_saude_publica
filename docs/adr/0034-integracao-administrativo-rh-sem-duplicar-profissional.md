# 0034 — Integração Administrativo ↔ RH sem duplicar Profissional

## Status

Proposta.

## Contexto

O documento-fonte (ver [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md))
define, nas suas seções 5 a 7 e 18, que o RH permanece autoridade funcional sobre `Profissional`,
`Cargo`, `Vínculo`, `Lotação` e histórico funcional, e que a Administração não deve criar
representação paralela de funcionário — apenas referenciar o profissional existente no RH.

O vocabulário do documento usa "Vínculo" como conceito genérico; o código já cobre exatamente esse
papel com a entidade `Lotacao` (histórico profissional↔unidade↔cargo, com `dataInicio`/`dataFim`).
Nenhuma entidade `Vinculo` nova será criada — é o mesmo conceito com nome já definido no RH.

Também existe um precedente de referência fraca no código: `UnidadeDeSaude.responsavel` é resolvido
por reconciliação de CPF (ADR-0014), porque `UnidadeDeSaude` foi criada antes de o módulo de RH
existir. Hoje o RH já é maduro e estável (`Profissional` com matrícula como fonte de verdade —
ADR-0017), então esse padrão de vínculo fraco não precisa ser repetido para entidades novas.

## Decisão

- Mapeamento de vocabulário: "Vínculo" (documento-fonte) = `Lotacao` (código, já existente) —
  nenhuma entidade `Vinculo` será criada.
- Toda referência do domínio Administrativo a um profissional é **FK direta para `Profissional`**
  (por `uuid`), nunca cópia de nome/matrícula/CPF. Isso vale para `Setor.responsavel` (ADR-0030),
  `ResponsabilidadeAdministrativa` (ADR-0035) e qualquer entidade administrativa futura que precise
  identificar um responsável.
- Leitura de lotação/cargo pelo Administrativo é sempre via consulta ao RH (join/consulta),
  nunca via campo espelhado ou sincronizado.
- O RH continua sendo o único a escrever em `Profissional`, `Cargo` e `Lotacao`; o Administrativo
  nunca escreve nessas tabelas.

## Trade-offs considerados

**FK direta a `Profissional` para toda referência nova (escolhida)**
- ✅ Simples e correto agora que o RH é estável — evita a complexidade de reconciliação por CPF da
  ADR-0014, que resolvia um problema de ordem de criação que não existe para entidades
  administrativas novas (criadas depois do RH já existir).
- ❌ Nenhuma desvantagem identificada — é estritamente mais simples que a alternativa de vínculo
  fraco.

**Reaproveitar o padrão de vínculo fraco por CPF (ADR-0014) para as novas referências (rejeitada)**
- ✅ Consistência "visual" com o padrão mais antigo do projeto.
- ❌ Esse padrão resolvia um problema específico (unidade existia antes do profissional) que não se
  repete aqui — usá-lo seria complexidade sem motivo, contrariando a diretriz de não construir para
  cenário hipotético.

## Consequências

**Positivas**: a fronteira de responsabilidade entre Administrativo e RH fica inequívoca e simples
de auditar — uma FK, uma direção de leitura, nenhuma escrita cruzada.

**Negativas / pendências**: nenhuma pendência conhecida — é a aplicação direta das Regras 1 a 3 do
documento-fonte, sem exceção identificada até o momento.

## Referências

- [ESCOPO-ADMINISTRATIVO.md](../administrativo/ESCOPO-ADMINISTRATIVO.md)
- [ADR-0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — contraste com o
  vínculo fraco por CPF, e por que ele não se aplica a entidades novas.
- [ADR-0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — `Profissional`/matrícula
  como fonte de verdade estável.
- [ESCOPO-RH.md](../rh/ESCOPO-RH.md)
