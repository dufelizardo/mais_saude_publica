# 0028 — Fatia 9: Benefícios

## Status

Aceita. Backend implementado (`encerrar`); frontend em implementação.

## Contexto

O levantamento de endpoint-para-uso do módulo RH inteiro (25 controllers) pedido pelo usuário
surgiu de forma independente do roadmap de fatias já planejado — não foi uma tela esquecida, foi
um subdomínio inteiro (`TipoBeneficio`, `ValorBeneficio`, `AdesaoBeneficio`, 8 endpoints) sem
nenhum consumo no frontend. O usuário rejeitou explicitamente "endpoint sem consumo" como critério
de decisão ("o critério deveria ser: existe uma capacidade de negócio que o usuário precisa
executar ou consultar e que ainda não possui uma interface adequada?") e pediu o mesmo levantamento
de models/DTOs/controllers feito para as fatias anteriores antes de desenhar qualquer tela.

Esse levantamento revelou um gap real de backend: `AdesaoBeneficio.dataFim` existia no model e no
`AdesaoBeneficioResponseDto` desde a criação do domínio, mas nenhum endpoint jamais conseguia
escrevê-lo — `POST` cria a adesão sem `dataFim`, e não havia `PATCH` algum. Diferente da fatia 8
(onde a ausência de `PATCH` era aceita como está), aqui a ausência bloqueava uma capacidade de
negócio óbvia (encerrar uma adesão a um benefício) que o próprio model já antecipava.

## Decisão

### Novo endpoint de backend: `PATCH /adesao-beneficio/{uuid}/encerrar`

Operação de negócio explícita — não um `PATCH` genérico de atualização de `AdesaoBeneficio`. A
adesão não é apagada nem alterada destrutivamente, ela ganha um fim. Recebe `dataFim` via query
param (`@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)`, mesmo estilo já usado em
`RegistroPontoController`/ADR-0024 para parâmetros de data tipados). Recusa (409) encerrar uma
adesão que já tem `dataFim` preenchido: não é "atualizar dataFim", é uma ação que só acontece uma
vez. Segue o mesmo precedente de nomenclatura de `des-habilitar` do `ProfissionalController`
(ADR-0017): ação de negócio como sub-recurso do path, não um `PATCH` genérico com corpo livre.

### Sem campo de `status`: situação continua derivada

Confirmação explícita do usuário: `AdesaoBeneficio` não ganha um campo `status`. A situação
(`Vigente`/`Encerrado`) é derivada no frontend de `dataFim == null` vs. `!= null` — mesmo padrão já
usado em `CicloAvaliacao`/`Epi` na fatia 8, e coerente com o padrão de "Vigente" usado em
`TabelaSalarial`/`ValorBeneficio` (`findFirstBy..._DataVigenciaLessThanEqualOrderByDataVigenciaDesc`).

### Escopo de telas: reaproveita padrões já estabelecidos, não inventa novos

- **`/rh/tipos-beneficio`**: catálogo (listar + criar via modal), mesmo padrão de
  `/rh/treinamentos`/`/rh/ciclos-avaliacao` (fatia 8a) — sem edição, `TipoBeneficio` também só tem
  `POST`/`GET`.
- **`/rh/tipos-beneficio/:tipoId/valores`**: rota aninhada, mesmo padrão de
  `/rh/cargos/:cargoId/tabela-salarial` (fatia 1) — "vigente" em destaque + histórico + form de
  novo valor, sem edição de valor histórico.
- **Aba "Benefícios" em `/profissionais/perfil`**: tabela (Benefício/Início/Dependentes/Situação
  derivada) + "Nova adesão", mesmo padrão de histórico + form das outras ~11 abas do perfil.
  "Encerrar adesão" só entra na UI agora que o endpoint existe — não antes, mesma disciplina da
  fatia 8.

## Trade-offs considerados

**Adicionar `encerrar` como ação explícita vs. `PATCH` genérico com corpo `{ dataFim }`**
- ✅ Mantém o padrão conceitual do projeto: a intenção da API (encerrar) fica explícita no path, não
  escondida atrás de um corpo de atualização genérico que poderia, em teoria, alterar qualquer
  campo.
- ✅ A regra de negócio (não pode encerrar duas vezes) vive naturalmente no endpoint, não como uma
  validação condicional dentro de um handler de update genérico.
- ❌ Mais um endpoint no controller em vez de reaproveitar um `PATCH` já existente — aceito, não
  existia nenhum `PATCH` em `AdesaoBeneficio` para reaproveitar.

**Situação derivada vs. campo `status` gravado (escolhida)**
- ✅ Mesmo raciocínio já validado na fatia 8: nunca fica desatualizado, não existe estado
  inconsistente entre "o que `dataFim` diz" e "o que o campo diz".
- ❌ Não permite um estado manual fora da lógica de data — não foi pedido.

## Consequências

**Positivas**: fecha o único gap de backend real encontrado no levantamento completo do módulo RH;
a UI de "Encerrar adesão" pode ser construída sem violar a regra "sem PATCH, sem edição" porque o
PATCH agora existe de verdade, com semântica de negócio explícita.

**Negativas / pendências**: `TipoBeneficio`/`ValorBeneficio` continuam sem `PATCH` — os catálogos
correspondentes (`/rh/tipos-beneficio`, tela de valores) não oferecem edição, mesma aceitação já
documentada na ADR-0027 para os demais catálogos sem `PATCH`.

## Referências

- [ADR-0017](./0017-numero-de-matricula-automatico-e-cpf-nao-unico.md) — precedente de
  `des-habilitar` como operação de negócio explícita, reaproveitado para `encerrar`.
- [ADR-0024](./0024-ponto-com-filtro-de-periodo.md) — estilo de `@RequestParam` com
  `@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)` para parâmetros de data.
- [ADR-0027](./0027-fatia-8-treinamento-avaliacao-sst-recrutamento.md) — regra "sem PATCH, sem
  edição na UI" e o padrão de situação derivada, ambos estendidos aqui.
- PR #154 — `PATCH /adesao-beneficio/{uuid}/encerrar`.
