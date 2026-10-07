# 0112 — Saúde Mental: domínio de atenção psicossocial

## Status

Aceita. **Desenho, sem implementação.** A implementação vem nas fatias SM1 a SM6, cada uma com ADR própria. O
detalhe está em [`saude-mental/MODELO-SAUDE-MENTAL.md`](../saude-mental/MODELO-SAUDE-MENTAL.md).

## Contexto

- A análise trazida pelo usuário propõe Saúde Mental como domínio de primeira classe, focado em **atenção
  psicossocial**:
  - acolhimento, avaliação, risco, acompanhamento longitudinal, Projeto Terapêutico Singular (PTS), intervenções,
    crise, equipe de referência e rede;
  - sem virar "prontuário de psicologia";
  - sem confundir CAPS com Saúde Mental.
- O que já existe pesa no desenho:
  - `TipoUnidadeDeSaude.CAPS`;
  - a `Equipe` dos tipos CAPS multi e eMulti, com apoio matricial (ADR-0103);
  - o programa **Saúde Mental**, sensível, e o `AcompanhamentoProgramatico` previsto em Programas (ADR-0109);
  - a especialidade no atendimento (ADR-0111);
  - a Internação (ADR-0098), que só registra eletiva ou urgência;
  - a notificação compulsória de violência autoprovocada (Vigilância, ADR-0110);
  - a regulação de urgência e o SAMU, que estão pendentes.

## Decisão

1. **Domínio novo de primeira classe: #26 Saúde Mental**, no grupo Assistência.
   - É dono da lógica do cuidado psicossocial.
   - **Continuam compartilhados:** Paciente, Atendimento, Prontuário, RH, Equipe, Agenda, Regulação e Rede.
   - **Sem cópias paralelas:** nada de `PacienteMental`, `ProntuarioMental` ou `AtendimentoMental`. Psicologia e
     psiquiatria são especialidades do atendimento.
2. **CAPS não é Saúde Mental:**
   - CAPS é equipamento. O cuidado acontece em UBS, CAPS, urgência, hospital e serviço residencial;
   - as modalidades de CAPS (I, II, III, i, AD, AD III) são capacidades da unidade (ADR-0053), sem `Caps.java`;
   - os componentes da RAPS (Portaria de Consolidação nº 3/2017) classificam os pontos da rede.
3. **`AcolhimentoSaudeMental` é diferente da triagem de enfermagem.** Registra motivo, necessidade imediata,
   vulnerabilidade, risco inicial e desfecho.
4. **`AvaliacaoSaudeMental`** traz contexto, aspectos psicossociais e fatores de risco e de proteção. O diagnóstico usa
   o CID do modelo clínico geral, sem catálogo próprio de transtornos e sem escalas agora.
5. **`AvaliacaoRisco` fica separada da avaliação e tem histórico:** tipo, nível, data, avaliador, situação e medidas.
   Nunca um booleano.
6. **Um só acompanhamento.**
   - `AcompanhamentoSaudeMental` é o caso clínico longitudinal.
   - Ao abrir, ele registra ou reaproveita o `AcompanhamentoProgramatico` do programa Saúde Mental.
   - Contagem e indicadores vêm do programa; o detalhe clínico fica no #26.
7. **O PTS é o agregado central e é versionado:** necessidades, objetivos, metas, intervenções, responsáveis,
   serviços, rede de apoio, periodicidade e revisão. A revisão gera uma versão nova, sem sobrescrever a anterior.
8. **`IntervencaoSaudeMental` com tipo configurável.** Grupos e oficinas usam as atividades coletivas de Programas
   (P4).
9. **`EventoCrise` é subdomínio obrigatório:**
   - identificação, avaliação, intervenção, acionamento da rede, encaminhamento e pós-crise;
   - crise com autoagressão gera **sugestão de notificação** de violência autoprovocada (ADR-0110);
   - acionar urgência e SAMU depende da regulação de urgência.
10. **A Internação ganha a modalidade** voluntária, involuntária ou compulsória (Lei 10.216/2001).
    - A involuntária exige **comunicação ao Ministério Público em até 72 horas**, e de novo na alta.
    - É campo e regra na Internação que já existe.
11. **Equipe e técnico de referência:**
    - reaproveitam `Equipe` e `Profissional`;
    - equipe de referência não é lotação;
    - o cuidado compartilhado e o matriciamento usam o apoio matricial da eMulti.
12. **Rede de apoio simples, ligada ao PTS.** Referência e contrarreferência pela Regulação. A articulação
    intersetorial é encaminhamento a uma instituição externa de catálogo simples.
13. **Sigilo no grau mais alto:**
    - visibilidade restrita à equipe de referência e ao vínculo (ADR-0076);
    - leitura auditada;
    - **conteúdo de psicoterapia fora do prontuário compartilhado** (só o registro e um resumo);
    - indicadores só agregados.
14. **Permissões previstas:** `SAUDE_MENTAL.ACOLHER`, `SAUDE_MENTAL.ACOMPANHAR` e `SAUDE_MENTAL.CRISE`.
15. **Fatias:**
    - **SM1 · Base:** acolhimento, avaliação, acompanhamento e referência.
    - **SM2 · PTS** e intervenções.
    - **SM3 · Risco e crise,** com a modalidade da internação.
    - **SM4 · Rede.**
    - **SM5 · CAPS e RAPS** como capacidades.
    - **SM6 · Intersetorialidade e indicadores.**

## Consequências

- **Domínios compartilhados ganham peças:**
  - a Internação ganha a modalidade e a comunicação ao Ministério Público;
  - a Organização ganha as modalidades de CAPS e os componentes da RAPS;
  - a Vigilância ganha a crise como fonte de sugestão de notificação.
- **Programas fica como a fonte única** da contagem de pessoas em acompanhamento.
- **A regulação de urgência ganha mais um motivo** para sair da pendência.
- **Fora do escopo agora:**
  - prontuário psiquiátrico completo;
  - catálogo de transtornos;
  - escalas;
  - prescrição de psicotrópicos (Farmácia, Portaria SVS/MS 344/1998);
  - modalidades de CAPS em detalhe;
  - integrações externas;
  - indicadores complexos;
  - protocolos rígidos.
