# 0048 — Evolução de Enfermagem: segunda entidade do domínio Enfermagem

## Status

Aceita e implementada.

## Contexto

Com `Triagem` (ADR-0047) implementada, o `DER.md` (seção "Prontuário", nota sobre candidatos sem
lar claro) já registrava **Evolução** como um candidato citado pelo usuário: "registro de
acompanhamento entre consultas, hoje só existe como conceito para o RH (`HistoricoFuncional`,
ADR-0026) — precisaria de um equivalente clínico". O mesmo trecho lista `Alergia` e `Documento
clínico` como candidatos igualmente sem lar, mas ambos dependem de pré-requisitos que não existem
ainda (`Alergia` não tem consumidor concreto definido; `Documento clínico` depende do domínio
transversal "Documentos"). `Evolução` é a única das três autocontida.

Dos quatro candidatos remanescentes do apêndice "Enfermagem (#8)" do `DER.md`
(`EvolucaoDeEnfermagem`, `AdministracaoDeMedicamento`, `Cuidado`, `Escala`), `AdministracaoDeMedicamento`
depende de `Medicamento` (domínio Farmácia, #9, ainda não iniciado); `Cuidado` e `Escala` não têm
estrutura própria definida em nenhum material trazido. `EvolucaoDeEnfermagem` é a única com escopo
claro e sem dependência de outro domínio ainda não construído.

## Decisão

- Nova entidade `EvolucaoEnfermagem`: `uuid`, `atendimento` (`@ManyToOne Atendimento`, obrigatório
  — mesmo papel estrutural de `Triagem`/`Consulta`), `profissional` (`@ManyToOne Profissional`,
  obrigatório, FK por `matricula` resolvida na fronteira da API — ADR-0034/ADR-0041),
  `dataHora`, `descricao` (`String`, obrigatório — o texto livre da nota de evolução).
- **Vinculada a `Atendimento`, não a `Consulta`** — o próprio `DER.md` descreve a evolução como algo
  que acontece "entre consultas", ou seja, ao longo do atendimento como um todo, não amarrada a uma
  consulta específica. Mesma reconciliação estrutural já feita para `Triagem` na ADR-0047.
  Diferente de `Triagem` (registrada uma vez, no início do atendimento) e de `Consulta` (evento
  pontual), `EvolucaoEnfermagem` pode se repetir várias vezes ao longo do mesmo `Atendimento`
  (várias notas de acompanhamento) — o modelo (`@ManyToOne` sem unicidade) já suporta isso sem
  ajuste.
- **Só o campo `descricao` como texto livre** — sem estruturar em campos separados (ex.: SOAP:
  Subjetivo/Objetivo/Avaliação/Plano). Mesma disciplina YAGNI de `Consulta.diagnostico` (ADR-0043):
  nenhum requisito concreto pede estruturação além de texto corrido.
- **Prontuário (ADR-0045, já estendido pela ADR-0047) passa a agregar também `EvolucaoEnfermagem`**
  — irmã de `triagens` e `consultas` dentro de cada `ProntuarioAtendimentoDto`
  (`evolucoes: List<EvolucaoEnfermagemResponseDto>`).
- CRUD no mesmo formato de `Triagem`/`Consulta`: `criar`/`atualizar` substituem os campos editáveis
  por inteiro, `listar`/`buscarPorId` sem filtro.
- Sem `criado_em`/`atualizado_em` — mesma omissão já feita em toda a onda Assistência e em `Triagem`.

## Trade-offs considerados

**Vincular a `Consulta` em vez de `Atendimento` (rejeitada)**
- ✅ Aproveitaria o mesmo padrão exato de `Procedimento` (filho de `Consulta`).
- ❌ Contradiria a própria descrição do candidato no `DER.md` ("entre consultas") — a evolução de
  enfermagem é contínua ao longo do atendimento, não amarrada a um evento de consulta específico.
  Exigir uma Consulta associada obrigaria o enfermeiro a "inventar" uma consulta só para registrar
  uma observação de rotina.

**Estruturar em campos SOAP (Subjetivo/Objetivo/Avaliação/Plano) — rejeitada**
- ✅ Mais próximo do padrão de documentação de enfermagem usado na prática.
- ❌ Nenhum requisito concreto pede essa granularidade; texto livre cobre o caso de uso atual sem
  antecipar estrutura não pedida.

## Consequências

**Positivas**: segunda entidade do domínio Enfermagem; Prontuário passa a mostrar o acompanhamento
contínuo do paciente durante o atendimento, não só os eventos pontuais (Triagem/Consulta/Procedimento).

**Negativas / pendências**: `AdministracaoDeMedicamento` (depende de Farmácia), `Cuidado` e
`Escala` continuam não modelados — implementar apenas quando houver requisito real.

## Referências

- [`DER.md`](./DER.md), seção "Prontuário" (nota sobre candidatos sem lar claro) e apêndice
  "Enfermagem (#8)".
- [ADR-0047](./0047-triagem-primeira-entidade-da-enfermagem.md) — precedente estrutural direto
  (FK obrigatória a `Atendimento`, resultado como campo simples em vez de entidade própria).
- [ADR-0043](./0043-consulta-registrada-durante-o-atendimento.md) — precedente de texto livre em
  vez de estruturação prematura (`diagnostico`).
- [ADR-0045](./0045-prontuario-agregacao-de-leitura.md) — agregação do Prontuário, estendida aqui.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API.
