# Escopo do domínio Assistência — roadmap e estado

## 1. Contexto e como ler este documento

A onda "Operação Assistencial" (ver [ADR-0039](../adr/0039-mapa-de-dominios-e-prioridades-de-arquitetura.md)
e [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md), domínio #4/#5/#6/#7) é a segunda onda de negócio
do Mais Saúde Pública, depois da Fundação (Organização, RH, Administrativo). Cobre o atendimento
real do cidadão pela rede: `Paciente`, `Atendimento`, `Agendamento`, `Consulta`, `Procedimento`, e
o `Prontuário` (agregação de leitura sobre os três últimos). Este documento é o mesmo tipo de
guarda-chuva por módulo que [`ESCOPO-RH.md`](../rh/ESCOPO-RH.md) e
[`ESCOPO-ADMINISTRATIVO.md`](../administrativo/ESCOPO-ADMINISTRATIVO.md) já são pros seus módulos —
criado retroativamente aqui, já que o backend desta onda foi implementado ADR a ADR
(0040-0045) sem um documento de escopo próprio até este ponto.

## 2. Princípio arquitetural

Mesma convenção flat já usada em RH e Administrativo (`models/`, `services/version1/`,
`controllers/version1/`, sem separação por domínio/Clean Architecture — ver ADR-0039 decisão 3).
`Profissional` é referenciado por matrícula (FK direta por trás, ver ADR-0034), nunca duplicado.
Sem entidade `Pessoa` compartilhada entre `Profissional` e `Paciente` (YAGNI, ADR-0039 decisão 5).

## 3. Relação com RH e Administrativo

`Atendimento`/`Consulta`/`Procedimento`/`Agendamento` referenciam `Profissional` (RH) só por
matrícula — nunca por uuid interno, que `ProfissionalResponseDto` não expõe (mesmo padrão do
Administrativo, ADR-0034). `Atendimento` referencia `Setor` (Administrativo) como campo opcional.
Nenhuma dependência no sentido inverso.

## 4. Fases planejadas (backend, completo)

| Fase | Entrega | ADR(s) |
|---|---|---|
| 1 ✅ | `Paciente` — primeira entidade da onda | [0040](../adr/0040-paciente-primeira-entidade-da-assistencia.md) |
| 2 ✅ | `Atendimento` — registra a entrada do paciente na rede | [0041](../adr/0041-atendimento-registra-entrada-do-paciente-na-rede.md) |
| 3 ✅ | `Agendamento` — independente do Atendimento | [0042](../adr/0042-agendamento-independente-do-atendimento.md) |
| 4 ✅ | `Consulta` — registrada durante o Atendimento | [0043](../adr/0043-consulta-registrada-durante-o-atendimento.md) |
| 5 ✅ | `Procedimento` — realizado durante a Consulta | [0044](../adr/0044-procedimento-realizado-durante-a-consulta.md) |
| 6 ✅ | `Prontuário` — agregação de leitura (Atendimento/Consulta/Procedimento por paciente) | [0045](../adr/0045-prontuario-agregacao-de-leitura.md) |

Onda backend completa — ver [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) pra próximos domínios
(Enfermagem, Farmácia, Laboratório, Regulação, Leitos, Transporte), só quando houver requisito real.

## 5. Estado de implementação (backend)

| Fase | Escopo | PR(s) | Status |
|---|---|---|---|
| 1 | `Paciente`, CRUD em `/api/v1/paciente/` (+ busca por cpf/cartão SUS), testes JUnit + Robot (ver [ADR-0040](../adr/0040-paciente-primeira-entidade-da-assistencia.md)) | [#209](https://github.com/dufelizardo/mais_saude_publica/pull/209) | ✅ |
| 2 | `Atendimento`, CRUD em `/api/v1/atendimento/`, testes JUnit + Robot (ver [ADR-0041](../adr/0041-atendimento-registra-entrada-do-paciente-na-rede.md)) | [#210](https://github.com/dufelizardo/mais_saude_publica/pull/210) | ✅ |
| 3 | `Agendamento`, CRUD em `/api/v1/agendamento/` + vínculo opcional `Atendimento.agendamento`, testes JUnit + Robot (ver [ADR-0042](../adr/0042-agendamento-independente-do-atendimento.md)) | [#211](https://github.com/dufelizardo/mais_saude_publica/pull/211) | ✅ |
| 4 | `Consulta`, CRUD em `/api/v1/consulta/`, testes JUnit + Robot (ver [ADR-0043](../adr/0043-consulta-registrada-durante-o-atendimento.md)) | [#212](https://github.com/dufelizardo/mais_saude_publica/pull/212) | ✅ |
| 5 | `Procedimento`, CRUD em `/api/v1/procedimento/`, testes JUnit + Robot (ver [ADR-0044](../adr/0044-procedimento-realizado-durante-a-consulta.md)) | [#213](https://github.com/dufelizardo/mais_saude_publica/pull/213) | ✅ |
| 6 | `Prontuário`, `GET /api/v1/prontuario/{pacienteId}` (agregação de leitura), testes JUnit + Robot (ver [ADR-0045](../adr/0045-prontuario-agregacao-de-leitura.md)) | [#214](https://github.com/dufelizardo/mais_saude_publica/pull/214) | ✅ |

Todas as 6 fases mergeadas em `developer`. Promoção a `qaa`/`homologacao`/`main` ainda não
solicitada.

## 6. Estado do frontend

Backend completo (seção acima); frontend construído fase a fase, seguindo o mesmo padrão dos
módulos de RH e Administrativo (ver [ADR-0046](../adr/0046-telas-de-frontend-da-onda-assistencia.md)
pras decisões de frontend específicas deste módulo).

| Fase | Escopo | PR(s) | Status |
|---|---|---|---|
| F1 | Tela **Pacientes** (`assistencia/pacientes`) + grupo de menu "Assistência" no `AppShell` + ADR-0046 | [#215](https://github.com/dufelizardo/mais_saude_publica/pull/215) | ✅ |
| F2 | Tela **Atendimentos** (`assistencia/atendimentos`) | [#216](https://github.com/dufelizardo/mais_saude_publica/pull/216) | ✅ |
| F3 | Tela **Agendamentos** (`assistencia/agendamentos`) | [#217](https://github.com/dufelizardo/mais_saude_publica/pull/217) | ✅ |
| F4 | Tela **Consultas** (`assistencia/consultas`) | [#218](https://github.com/dufelizardo/mais_saude_publica/pull/218) | ✅ |
| F5 | Tela **Procedimentos** (`assistencia/procedimentos`) | [#219](https://github.com/dufelizardo/mais_saude_publica/pull/219) | ✅ |
| F6 | Tela **Prontuário** (`assistencia/prontuario`, somente leitura) | — | ⏳ Em andamento |

## 7. Referências

- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) e
  [ADR-0039](../adr/0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) — roadmap da plataforma
  e decisões guarda-chuva que esta onda implementa.
- [`ESCOPO-RH.md`](../rh/ESCOPO-RH.md) e
  [`ESCOPO-ADMINISTRATIVO.md`](../administrativo/ESCOPO-ADMINISTRATIVO.md) — precedente direto de
  como este documento foi estruturado.
- [ADR-0034](../adr/0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — padrão de FK
  por matrícula, reaproveitado em toda esta onda.
- [ADR-0046](../adr/0046-telas-de-frontend-da-onda-assistencia.md) — decisões de frontend
  específicas deste módulo.
