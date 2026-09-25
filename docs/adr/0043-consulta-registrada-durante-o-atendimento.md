# 0043 — Consulta registrada durante o Atendimento

## Status

Aceita e implementada (quarta fatia da onda Operação Assistencial — [PR #212](https://github.com/dufelizardo/mais_saude_publica/pull/212)).

## Contexto

Com `Paciente` (ADR-0040), `Atendimento` (ADR-0041) e `Agendamento` (ADR-0042) implementados, a
ordem definida em ADR-0039/`MAPA-DE-DOMINIOS.md` segue para `Consulta` — parte do domínio
`Prontuário` (#6), que a ADR-0039 já decidiu ser uma **agregação de leitura**, não uma entidade
própria (decisão 6). `Consulta` e `Procedimento` são os dados reais que essa agregação vai reunir.

O desenho original (DER.md, seção 7) já existia desde antes da reconciliação: `atendimento_id`,
`profissional_id`, `data_hora`, `tipo_consulta` (PRIMEIRA/RETORNO/URGENCIA), `queixa_principal`,
`diagnostico`, `receituario`, `exames_solicitados`, `retorno`. A seção "Modelo revisado" do DER.md
já registrava a intenção de mantê-lo campo a campo, só trocando as FKs supersedidas pelas reais.

## Decisão

- Nova entidade `Consulta`: `uuid`, `atendimento` (`@ManyToOne Atendimento`, obrigatório, por uuid),
  `profissional` (`@ManyToOne Profissional`, obrigatório, FK interna por uuid resolvida pela API via
  `profissionalMatricula` — mesmo padrão de `Atendimento.profissional`, ADR-0034/ADR-0041),
  `dataHora`, `tipoConsulta` (novo enum `TipoConsulta`: `PRIMEIRA, RETORNO, URGENCIA`),
  `queixaPrincipal`, `diagnostico`, `receituario`, `examesSolicitados` (todos `String`, opcionais),
  `retorno` (`LocalDate`, opcional).
- **`diagnostico`/`receituario`/`examesSolicitados` continuam como campos de texto livre** — sem
  entidades `Diagnostico`/`Exame`/`Prescricao` próprias (reafirma o que o DER.md já previa: YAGNI,
  nenhum requisito concreto pede consulta estruturada desses dados ainda; revisitar quando
  Farmácia/Laboratório entrarem no roadmap).
- CRUD no mesmo formato de `Atendimento`/`Agendamento`: `criar`/`atualizar` substituem os campos
  editáveis por inteiro.
- Sem `criado_em`/`atualizado_em` — mesma omissão já feita em `Atendimento`/`Agendamento`.

## Trade-offs considerados

**Modelar `diagnostico`/`receituario` como entidades estruturadas agora (rejeitada)**
- ✅ Permitiria buscas/relatórios mais ricos sobre diagnósticos específicos.
- ❌ Nenhum requisito concreto pede isso hoje; o próprio DER.md original já antecipava manter esses
  campos como texto — estruturar precisa de um catálogo (CID, por exemplo) que não existe e não foi
  pedido.

## Consequências

**Positivas**: fornece o segundo dos três insumos (`Atendimento`, `Consulta`, `Procedimento`) que o
futuro endpoint de `Prontuário` vai agregar por paciente.

**Negativas / pendências**: `Procedimento` (próxima fatia) ainda não existe — `Consulta` fica sem
seus procedimentos associados até lá. `Prontuário` como agregação de leitura só faz sentido depois
de `Procedimento` também existir.

## Referências

- [`DER.md`](./DER.md), seção 7 (desenho original) e "Modelo revisado para a próxima onda".
- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) e
  [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — roadmap da onda Assistência, decisão 6
  (Prontuário como agregação).
- [ADR-0041](./0041-atendimento-registra-entrada-do-paciente-na-rede.md) — entidade `Atendimento`,
  referenciada aqui.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API.
