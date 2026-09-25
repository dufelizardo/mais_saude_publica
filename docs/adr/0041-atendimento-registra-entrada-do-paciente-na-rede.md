# 0041 — Atendimento registra a entrada do paciente na rede

## Status

Aceita e implementada (segunda fatia da onda Operação Assistencial).

## Contexto

Com `Paciente` implementado (ADR-0040), a ordem definida em ADR-0039/`MAPA-DE-DOMINIOS.md` segue
para `Atendimento` — o registro de que um cidadão entrou na rede para receber um serviço. O modelo
já estava esboçado em [`DER.md`](./DER.md), seção "Modelo revisado para a próxima onda", referindo
os modelos reais (`Profissional` por matrícula — ADR-0034 —, `UnidadeDeSaude`/`Setor` reais) em vez
dos supersedidos.

O esboço do DER.md também previa um campo `agendamento` (FK opcional para `Agendamento`) — mas
`Agendamento` é a **próxima** fatia da mesma onda (ainda não existe). Implementar `Atendimento`
agora exige decidir o que fazer com esse campo que referencia algo que ainda não existe.

## Decisão

- Nova entidade `Atendimento`: `uuid`, `paciente` (`@ManyToOne Paciente`, obrigatório, por uuid —
  `Paciente` não tem um equivalente à matrícula ainda), `profissional` (`@ManyToOne Profissional`,
  obrigatório, FK interna por uuid mas resolvida pela API via `profissionalMatricula`, mesmo padrão
  do `Setor.responsavel` — ADR-0034), `unidade` (`@ManyToOne UnidadeDeSaude`, obrigatório), `setor`
  (`@ManyToOne Setor`, **opcional** — nem todo atendimento passa por um setor específico), `tipo`
  (novo enum `TipoAtendimento`: `CONSULTA, URGENCIA, INTERNACAO`), `status` (novo enum
  `StatusAtendimento`: `AGENDADO, EM_ANDAMENTO, CONCLUIDO`), `dataHora`.
- **Campo `agendamento` fica de fora desta fase.** Quando a entidade `Agendamento` for implementada
  (próxima fatia), o vínculo opcional é *acrescentado* a `Atendimento` — mesmo padrão de "campo
  chega depois" já usado por `Setor.responsavel` (existe desde a ADR-0030 sem o campo, que só foi
  acrescentado na ADR-0034 quando `Profissional` já estava pronto para ser referenciado). Não faz
  sentido modelar uma FK para uma tabela que ainda não existe.
- CRUD no mesmo formato do `Setor` (ADR-0030): `criar`/`atualizar` substituem os campos editáveis
  por inteiro, incluindo `status` — não há um endpoint dedicado de "avançar status", a mudança de
  `AGENDADO` → `EM_ANDAMENTO` → `CONCLUIDO` é uma atualização comum.
- Sem busca por paciente/profissional ainda (`listar`/`buscarPorId` apenas) — motivo: ainda não há
  requisito concreto para essas listagens filtradas; `Prontuário` (próxima onda, ADR-0039 decisão 6)
  é quem vai agregar atendimentos por paciente, então adiar evita construir uma consulta que a
  agregação futura pode tornar redundante ou desenhar diferente.

## Trade-offs considerados

**Modelar `agendamento` como FK para uma entidade futura, criada "vazia" antes da hora (rejeitada)**
- ✅ Evitaria uma segunda migração de schema quando `Agendamento` chegasse.
- ❌ Criaria uma tabela sem nenhum consumidor e sem modelo decidido — contraria a disciplina de "só
  desenhar quando a onda chegar" já usada pelo próprio DER.md (ADR-0039 decisão 7) e pela ADR-0037
  do Administrativo.

**Busca por paciente (`GET /atendimento?pacienteId=`) já nesta fase (rejeitada)**
- ✅ Utilidade óbvia (ver histórico de atendimentos de um paciente).
- ❌ É exatamente o que o `Prontuário` (agregação de leitura, ADR-0039 decisão 6) vai entregar
  quando `Consulta`/`Procedimento` também existirem — implementar agora arrisca desenhar uma
  consulta que a agregação real substituiria.

## Consequências

**Positivas**: `Atendimento` conecta `Paciente` (ADR-0040) aos modelos reais de RH/Organização sem
introduzir nenhuma entidade nova além do previsto; abre caminho para `Agendamento` (próxima fatia)
simplesmente acrescentar seu próprio vínculo opcional.

**Negativas / pendências**: sem o campo `agendamento` até a próxima fatia — um atendimento não pode
ainda ser ligado a um agendamento prévio (não há repositório de agendamentos para ligar); sem
listagem filtrada por paciente/profissional até `Prontuário` existir.

## Referências

- [`DER.md`](./DER.md), seção "Modelo revisado para a próxima onda" — esboço de campos original.
- [ADR-0039](./0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) e
  [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — roadmap da onda Assistência.
- [ADR-0040](./0040-paciente-primeira-entidade-da-assistencia.md) — entidade `Paciente`, referenciada
  aqui.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de FK
  por matrícula resolvida na fronteira da API, reaproveitado para `profissionalMatricula`.
- [ADR-0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) — precedente do formato
  de CRUD (atualização substitui os campos por inteiro) e de "campo chega depois".
