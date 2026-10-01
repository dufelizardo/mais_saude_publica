# 0049 — Medicamento: primeira entidade do domínio Farmácia

## Status

Aceita e implementada.

## Contexto

Com Enfermagem (#8) tendo `Triagem` (ADR-0047) e `EvolucaoEnfermagem` (ADR-0048) implementadas, o
`MAPA-DE-DOMINIOS.md` lista Farmácia (#9) como próximo domínio "conforme requisito real". Farmácia
também desbloqueia um item hoje travado em Enfermagem: `AdministracaoDeMedicamento` foi
explicitamente adiado nas ADRs 0047/0048 por depender de `Medicamento` existir primeiro.

O `DER.md` (apêndice "Farmácia (#9)") lista como candidatos: `Medicamento`, `Lote` (validade,
quantidade), `Dispensacao`, `MovimentacaoFarmacia`, `TransferenciaEntreUnidades`, `Perda`,
`InventarioFarmacia` — um fluxo completo de estoque farmacêutico
(`Prescrição → Farmácia → Dispensação → Paciente`). Grande demais pra uma fatia só, e contraria a
disciplina de "incrementos pequenos e discutidos" já usada em todo o projeto — RH, Administrativo,
Assistência e Enfermagem sempre começaram por uma única entidade fundacional (`Setor`, `Paciente`,
`Triagem`).

Pesquisa sobre como farmácias de rede pública (SUS) operam na prática, pra não desenhar isso no
vácuo:

- O sistema de referência nacional é o **Hórus** (Sistema Nacional de Gestão da Assistência
  Farmacêutica, DAF/SCTIE/MS): controla lote/validade, exige CNS do paciente na dispensação, e
  rastreia estoque por almoxarifado/farmácia/unidade.
- O padrão operacional de estoque farmacêutico é entrada/saída de movimentação, com subtipos:
  dispensação (saída ao paciente), transferência entre unidades (saída+entrada), perda/baixa
  (vencimento, avaria), e inventário periódico (reconciliação, tipicamente semestral).
- Medicamentos controlados (Portaria 344/98 — psicotrópicos, entorpecentes) têm regras de
  dispensação e rastreabilidade diferentes de medicamentos comuns — mas isso só importa quando
  `Dispensacao` for implementada, não para o catálogo em si.

Conclusão: a fatia certa pra começar é só `Medicamento` — o catálogo do qual tudo mais depende
(`Lote`, `Dispensacao`, `MovimentacaoFarmacia`, e o `AdministracaoDeMedicamento` da Enfermagem). Sem
isso, nenhuma outra entidade da lista tem o que referenciar.

## Decisão

- Nova entidade `Medicamento`: `uuid`, `nome` (nome comercial ou princípio ativo, para genéricos),
  `principioAtivo` (`String`, opcional — só quando o nome comercial difere do princípio ativo),
  `apresentacao` (`String` — forma farmacêutica + dosagem, texto livre, ex.: "Comprimido 500mg",
  convenção usada por catálogos de medicamentos como o RENAME), `codigo` (`String` — identificador
  único, texto livre, sem exigir integração com um código nacional específico como RENAME/ATC
  agora), `ativo` (`boolean`, mesmo padrão de `Paciente.ativo`/`Setor.ativo`).
- **Sem `Lote`, `Dispensacao`, `MovimentacaoFarmacia` etc. nesta fase** — cada um exige seu próprio
  desenho de trade-offs (`Lote` tem controle de validade/quantidade próprio; `Dispensacao` precisa
  decidir como se vincula a `Consulta`/`Atendimento`/`Paciente`). Ficam para fases seguintes,
  "conforme requisito real" — mesma disciplina do resto do mapa.
- **Sem distinção de medicamento controlado (Portaria 344/98) nesta fase** — só passa a importar
  quando `Dispensacao` existir; adicionar agora estruturaria pra um requisito que ainda não chegou
  (YAGNI, mesmo raciocínio de `classificacaoRisco` como campo simples em `Triagem`, ADR-0047).
- CRUD no mesmo formato de `Paciente`/`Triagem`: `criar`/`atualizar` substituem os campos editáveis
  por inteiro, `listar`/`buscarPorId` sem filtro. **Sem FK** — `Medicamento` é raiz do domínio
  Farmácia, igual `Paciente` foi raiz da Assistência.
- Sem `criado_em`/`atualizado_em` — mesma omissão do resto da plataforma.

## Trade-offs considerados

**Modelar `Lote` (validade/quantidade) já nesta fase — rejeitada**
- ✅ Um `Medicamento` sem controle de lote/validade não reflete a prática real de farmácia (Hórus
  exige lote e validade desde a entrada).
- ❌ `Lote` é uma entidade com identidade e ciclo de vida próprios (entra em estoque, vence, é
  consumido) — misturá-la ao catálogo de `Medicamento` agora antecipa uma modelagem de estoque que
  ainda não tem consumidor real (nenhuma tela, nenhum endpoint de dispensação existe).

**Incluir uma flag de medicamento controlado (Portaria 344/98) — rejeitada por ora**
- ✅ Reflete uma distinção regulatória real, existente desde o cadastro do medicamento.
- ❌ Sem `Dispensacao` implementada, essa flag não tem nenhum comportamento que dependa dela — puro
  campo morto até a fase que realmente precisa da distinção.

## Consequências

**Positivas**: primeira entidade do domínio Farmácia; desbloqueia `AdministracaoDeMedicamento`
(Enfermagem, ADRs 0047/0048) e todo o resto do fluxo de estoque farmacêutico quando/se for
implementado.

**Negativas / pendências**: `Lote`, `Dispensacao`, `MovimentacaoFarmacia`, `TransferenciaEntreUnidades`,
`Perda`, `InventarioFarmacia` continuam não modelados — implementar apenas quando houver requisito
real, mesma disciplina do restante do mapa.

## Referências

- [`DER.md`](./DER.md), apêndice "Farmácia (#9)" — esboço original.
- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — domínio #9, posição no roadmap da plataforma.
- [ADR-0047](./0047-triagem-primeira-entidade-da-enfermagem.md) e
  [ADR-0048](./0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md) — precedentes diretos
  de "uma entidade fundacional por vez" e de YAGNI (campo simples em vez de entidade/estrutura
  antecipada); `AdministracaoDeMedicamento` (adiado ali) é desbloqueado por esta ADR.
- [Integração com Hórus](https://grupoassessor.movidesk.com/kb/pt-br/article/32181/integracao-com-horus) —
  sistema de referência nacional para gestão farmacêutica no SUS.
- [Módulo 1 — Conhecendo o Hórus](https://cosemspb.org/wp-content/uploads/2013/11/apostila_m%C3%B3dulo-I.pdf).
- [Movimentação de Estoque (TOTVS)](https://tdn.totvs.com/pages/viewpage.action?pageId=465383538) —
  padrão de entrada/saída usado como referência para fases futuras.
- [Estoque em farmácia hospitalar](https://farmaciahospitalar.com.br/estoque-em-farmacia-hospitalar/) —
  prática de inventário periódico, referência para fases futuras.
