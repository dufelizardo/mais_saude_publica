# 0039 — Mapa de domínios do Mais Saúde Pública e prioridades de arquitetura

## Status

Aceita.

## Contexto

RH (ADRs 0014–0029) e Administrativo (ADRs 0030–0038) estão completos, backend e frontend,
mergeados em `developer`. O usuário trouxe uma visão de **20 domínios** para a plataforma inteira —
Organização, RH, Administrativo, Atendimento, Paciente, Prontuário, Agendamento, Enfermagem,
Farmácia, Laboratório, Regulação, Gestão de Leitos, Estoque, Compras/Contratos, Patrimônio,
Transporte, Financeiro, Qualidade, Indicadores/BI, e um grupo transversal (Identidade, Segurança,
Auditoria, Integrações, Documentos) — organizados em 4 grandes áreas (Governança / Gestão /
Assistência / Transversais), documentados em [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md).

Antes de simplesmente anexar essa visão ao projeto, uma investigação encontrou três achados que
mudam o quadro:

1. **[`DER.md`](./DER.md) já existe** — uma proposta de modelo de dados clínico "futuro", de
   2026-09-06 (antes de RH e Administrativo existirem sequer), com 15 entidades (Usuario,
   Profissional, Paciente, UnidadeSaude, Atendimento, Consulta, Procedimento, Equipe, Agendamento,
   Notificação, Auditoria, etc.). Estava truncada no meio de um DDL e **conflitava** com o que foi
   realmente implementado depois: seu `PROFISSIONAL` é uma casca ligada a um `USUARIO` de
   autenticação que nunca existiu, e seu `UNIDADE_SAUDE` é um modelo plano por CNES — diferente da
   entidade real, autorreferenciada de 5 níveis (ADR-0002/0009/0013).
2. **ADRs 0003–0008 formam um "roadmap de arquitetura futura"** (Clean Architecture, padrões de
   projeto, separação domínio/JPA, JWT, Docker/CI, Angular) cuja própria ADR-0003 registra que
   deveria vir **antes de qualquer novo domínio de negócio** — mas RH e Administrativo (que são,
   objetivamente, novos domínios de negócio) foram construídos inteiros ignorando essa ordem, sem
   nenhuma dessas camadas. Isso nunca tinha sido formalmente revisto até agora.
3. **Nenhum domínio clínico existe em nenhuma forma no código hoje** — Paciente, Atendimento,
   Prontuário, Farmácia, Laboratório, Regulação, Leitos e Financeiro têm zero ocorrências em
   `src/main/java`. É uma folha em branco.

Perguntado explicitamente se a entrada no domínio Assistência (dado de saúde real, sensível por
LGPD) deveria elevar a segurança (ADR-0006) a pré-requisito — diferente do padrão adotado em
RH/Administrativo, que ficaram 100% abertos — **o usuário decidiu manter o padrão de adiar**, a
mesma disciplina já usada em RH/Administrativo.

## Decisão

1. **Adota o mapa de 4 grupos / 20 domínios** documentado em `MAPA-DE-DOMINIOS.md`, organizado em 3
   ondas de implementação (🟢 Fundação — feito; 🔵 Operação Assistencial — próxima; 🟣 Gestão e
   Inteligência — depois), com a próxima onda começando por **Paciente → Atendimento (Agendamento
   como referência opcional) → Prontuário (agregação)**.
2. **Segurança (ADR-0006) continua adiada** — decisão explícita do usuário, não descuido. Registrada
   aqui como risco aceito conscientemente: a próxima onda lida com dado de saúde real sem nenhuma
   autenticação, exatamente como RH lidou com dado de folha de pagamento e Administrativo com dado
   operacional. Fica a ressalva explícita de revisitar a ADR-0006 **antes de qualquer deploy com
   dado real de paciente** (fora do escopo de desenvolvimento/homologação).
3. **Convenção de pacote flat é reafirmada — Clean Architecture (ADR-0003) não é adotada agora.**
   100% das ~35 entidades já construídas (RH + Administrativo) usam a estrutura flat `models/` /
   `services/version1/` / `controllers/version1/`. Não há relato de dor real que justifique
   introduzir um segundo estilo arquitetural só para o próximo domínio — revisitar apenas se um
   problema concreto de manutenibilidade aparecer.
4. **`DER.md` fica parcialmente supersedido, não apagado** — revisado no próprio arquivo, no mesmo
   estilo de "nota de reconciliação" que as ADR-0003/0006/0007 já usam. As seções `USUARIO`,
   `PROFISSIONAL` e `UNIDADE_SAUDE` (que conflitam com o real) ficam marcadas como supersedidas,
   apontando para onde está o modelo real (`MODELO-RH.md`, `DER-atual.md`). A seção de
   Atendimento/Consulta/Procedimento/Agendamento foi revisada para referenciar os modelos reais
   (`Profissional` por matrícula — ADR-0034 —, `UnidadeDeSaude`/`Setor` reais) e ganhou uma seção
   de `Paciente` nova e simples. O DDL truncado foi removido.
5. **Sem entidade `Pessoa` compartilhada entre `Profissional` e `Paciente`** — YAGNI. Nenhum
   requisito concreto força a deduplicação hoje, e seria o primeiro acoplamento direto entre RH e
   o novo domínio clínico, contrariando a fronteira de bounded-context que o projeto já pratica
   (ver ADR-0034, que manteve Administrativo↔RH desacoplados pelo mesmo motivo). O caso raro de
   alguém ser funcionário e paciente ao mesmo tempo fica resolvido por vínculo fraco por CPF, se um
   dia for preciso — mesmo princípio já usado pela ADR-0014.
6. **Prontuário não é uma tabela nova, é uma visão agregada** sobre Atendimento/Consulta/
   Procedimento de um Paciente — mesmo espírito do "Histórico funcional consolidado" (ADR-0026) do
   RH, que também foi implementado como agregação de leitura, não uma entidade própria.
7. **Nem todos os 20 domínios são desenhados agora.** Só a próxima onda (Paciente, Atendimento,
   Agendamento, Prontuário) ganhou um modelo revisado no `DER.md`. Os demais 15+ ficam com
   responsabilidade e entidades candidatas em uma frase no mapa, a desenhar de verdade quando sua
   própria onda chegar — mesma disciplina do "Fase 7+" do Administrativo ("só quando houver
   requisito real", ADR-0037).
8. **Domínio ainda não desenhado ganha esboço no DER.md, nunca uma ADR própria.** Para não perder o
   material detalhado (campos, fluxos, relações) que o usuário já trouxe para os 15+ domínios da
   onda seguinte, cada um recebeu um esboço no apêndice de `DER.md` — mas nenhum ganhou ADR. Uma
   ADR registra uma decisão com trade-off real; um domínio ainda não desenhado não tem decisão
   nenhuma tomada além de "isso existirá algum dia", e forçar uma ADR aí produziria um documento
   oco. Um DER é descritivo por natureza (a própria proposta original deste arquivo já era um
   "modelo futuro/proposto", nunca uma decisão), então é o lugar certo para o esboço. Cada domínio
   ganha sua própria ADR quando sua onda chegar e houver uma decisão de verdade a registrar — mesmo
   padrão que a ADR-0037 já segue para "Patrimônio"/"Compras"/"Gestão de Leitos" no Administrativo
   (rótulo no catálogo, sem ADR própria, até que a especialização seja de fato decidida).

## Trade-offs considerados

**Clean Architecture agora, para o novo domínio clínico (rejeitada)**
- ✅ Alinharia o código com a proposta original da ADR-0003 antes que o domínio clínico cresça.
- ❌ Introduziria dois estilos arquiteturais coexistindo (flat em RH/Administrativo, em camadas no
  novo domínio) sem nenhum problema concreto que justifique o custo — contraria a diretriz de não
  reformar por antecipação.

**Segurança (ADR-0006) como pré-requisito do domínio Assistência (rejeitada pelo usuário)**
- ✅ Dado de saúde é objetivamente mais sensível (LGPD) que dado de RH/Administrativo.
- ❌ Decisão explícita do usuário: manter a mesma disciplina incremental já usada — segurança vira
  sua própria onda quando fizer sentido, não um bloqueio automático do próximo domínio de negócio.

**Entidade `Pessoa` compartilhada entre `Profissional` e `Paciente` (rejeitada)**
- ✅ Evitaria duplicar nome/CPF/endereço/contatos entre os dois.
- ❌ Seria o primeiro acoplamento direto entre RH e o domínio clínico, contrariando a fronteira de
  bounded-context já praticada (ADR-0034); resolve um caso raro (mesma pessoa como funcionário e
  paciente) que o vínculo fraco por CPF já resolve sem acoplamento, se algum dia for necessário.

**Prontuário como entidade própria (rejeitada)**
- ✅ Um lugar único para consultar tudo sobre o histórico clínico do paciente.
- ❌ Duplicaria dado que já vive em `Atendimento`/`Consulta`/`Procedimento` — mesmo raciocínio que
  levou a ADR-0026 a implementar o histórico funcional do RH como agregação, não tabela.

## Consequências

**Positivas**: a próxima onda (Paciente → Atendimento → Prontuário) pode começar sem esperar
nenhum pré-requisito de infraestrutura novo (nem Clean Architecture, nem JWT); `DER.md` deixa de
ser uma fonte de confusão (schema conflitante, DDL truncado) e vira uma base de referência
reconciliada; os 20 domínios do usuário ficam todos registrados, nenhum perdido, mesmo os que só
serão desenhados muito depois.

**Negativas / pendências**: a API segue 100% aberta mesmo entrando em dado de saúde real — risco
aceito conscientemente, não esquecido, com a ressalva explícita de revisitar antes de produção real
com dado de paciente de verdade. Farmácia/Laboratório/Regulação/Leitos/Financeiro/Qualidade/
Indicadores continuam sem nenhum desenho de dados — ficam para quando sua própria onda chegar.

## Referências

- [`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) — o mapa completo dos 20 domínios e das ondas de
  implementação.
- [`DER.md`](./DER.md) — modelo de dados revisado para a próxima onda.
- [ADR-0003](./0003-adocao-clean-architecture.md), [ADR-0006](./0006-seguranca-jwt.md) — propostas
  de arquitetura cuja prioridade esta ADR revisita.
- [ADR-0014](./0014-modulo-profissional-rh-com-vinculo-fraco-por-reconciliacao.md) — precedente de
  vínculo fraco por CPF, reaproveitado na decisão 5.
- [ADR-0026](./0026-historico-funcional-consolidado.md) — precedente de agregação de leitura,
  reaproveitado na decisão 6.
- [ADR-0034](./0034-integracao-administrativo-rh-sem-duplicar-profissional.md) — precedente de
  fronteira de bounded-context entre módulos, reaproveitado na decisão 5.
- [ADR-0036](./0036-necessidade-de-pessoal-encaminhada-ao-rh.md) — precedente de referência
  opcional entre entidades, reaproveitado no desenho de `Atendimento.agendamento`.
