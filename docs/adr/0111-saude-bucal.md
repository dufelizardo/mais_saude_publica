# 0111 — Saúde Bucal: domínio assistencial especializado

## Status

Aceita. **Desenho, sem implementação.** A implementação vem nas fatias SB1 a SB5, cada uma com ADR própria. O
detalhe está em [`saude-bucal/MODELO-SAUDE-BUCAL.md`](../saude-bucal/MODELO-SAUDE-BUCAL.md).

## Contexto

- A análise trazida pelo usuário propõe Saúde Bucal como domínio próprio:
  - avaliação, odontograma com histórico, plano de tratamento, encaminhamento, prótese e ações coletivas;
  - com o cuidado de não duplicar os domínios gerais.
- O que já existe muda parte da análise:
  - **A equipe de Saúde Bucal já existe:** é a `Equipe` do tipo **eSB**, com dentista, técnico e auxiliar de saúde
    bucal e composição mínima (ADR-0103).
  - **`Procedimento` é um registro realizado** na consulta, com tipo em texto livre. Não há catálogo geral, só os de
    Regulação (`ProcedimentoRegulado`) e de Laboratório (`ExameLaboratorial`).
  - **`Atendimento` não tem especialidade,** e a Agenda tem o filtro por especialidade pendente.
  - **O CEO** está no Mapa de Equipamentos, mas não em `TipoUnidadeDeSaude`.
  - **Programas (ADR-0109)** já prevê as ações programáticas.

## Decisão

1. **Domínio novo, assistencial especializado: #25 Saúde Bucal**, no grupo Assistência.
   - **Saúde Bucal é dona do conhecimento odontológico:** avaliação, odontograma, plano de tratamento, prótese e o
     detalhe das ações coletivas.
   - **Continuam compartilhados:** Paciente, Atendimento, Prontuário, Agenda, RH, Equipe, Regulação, Território e
     Programas.
   - **Nada de cópias paralelas:** sem `PacienteOdontologico`, `ProntuarioOdontologico`, `AtendimentoOdontologico`,
     `AgendaOdontologica`, `EquipeSaudeBucal` ou cadastro próprio de dentista.
2. **Equipe:** usa a eSB que já existe (ADR-0103).
3. **O `Atendimento` ganha especialidade** (catálogo), para todas as especialidades.
   - Os dados odontológicos ficam ligados ao atendimento de odontologia.
   - A Agenda ganha o filtro por especialidade.
4. **`AvaliacaoOdontologica`** ligada ao atendimento:
   - queixa e anamnese;
   - exame, achados e diagnóstico;
   - risco;
   - plano inicial;
   - com a retificação da ADR-0062.
5. **O odontograma tem histórico por registros, e não por estado mutável:**
   - notação FDI (ISO 3950): permanentes de 11 a 48 e decíduos de 51 a 85, com cinco faces;
   - cada mudança é um registro (dente, face, condição do catálogo, data e a origem: avaliação ou procedimento);
   - o odontograma de uma data é a projeção dos registros;
   - começa simples.
6. **Catálogo geral de procedimentos, compartilhado:**
   - tem especialidade e o código SIGTAP opcional;
   - o `Procedimento` realizado aponta para ele;
   - o procedimento odontológico é o `Procedimento` normal mais o **complemento odontológico** (dente e faces), que
     gera os registros do odontograma;
   - unificar os catálogos de Regulação e Laboratório fica como pendência.
7. **`PlanoTratamento` longitudinal:**
   - itens planejados, em andamento, concluídos, encaminhados ou cancelados, ligados ao procedimento que os executou;
   - a conclusão do plano é o tratamento concluído.
8. **Encaminhamento pela Regulação:**
   - as especialidades odontológicas entram no catálogo dela, com o CEO como executante;
   - o tipo de unidade do CEO segue a ADR-0053 e é decidido na fatia.
9. **Prótese é um subdomínio** (`TratamentoProtetico`), com ciclo de indicação até acompanhamento e um serviço
   responsável (LRPD, interno ou contratado).
10. **Ações coletivas são ações programáticas de Programas (ADR-0109, P4) com detalhe odontológico**, e não um motor
    próprio:
    - escovação supervisionada, flúor, levantamento epidemiológico, educação;
    - em escola, UBS, comunidade ou território.
11. **Funções:**
    - diagnóstico, plano e atos privativos são do cirurgião-dentista (Lei 5.081/1966);
    - o técnico em saúde bucal registra os procedimentos permitidos, sob supervisão (Lei 11.889/2008);
    - permissões previstas: `ODONTOLOGIA.REGISTRAR` e `ODONTOLOGIA.PLANEJAR`;
    - o registro odontológico é dado de saúde e segue as regras do prontuário.
12. **Prontuário:** a avaliação, o odontograma, o plano e os procedimentos entram na agregação que já existe.
13. **Fatias:**
    - **SB1 · Base:** especialidade no atendimento e na Agenda; avaliação.
    - **SB2 · Odontograma.**
    - **SB3 · Tratamento:** catálogo geral, complemento odontológico, plano.
    - **SB4 · Regulação e prótese.**
    - **SB5 · Saúde bucal coletiva.**

## Consequências

- **Atendimento e Agenda ganham especialidade**, útil para todas as especialidades.
- **O sistema ganha um catálogo geral de procedimentos**, base para o SIGTAP e para os indicadores.
- A eSB passa a ter o que fazer além da composição.
- Programas (P4) e Território passam a ser pré-requisito da saúde bucal coletiva.
- **Fora do escopo agora:**
  - SIGTAP completo;
  - e-SUS e SISAB;
  - laboratório de prótese completo;
  - indicadores odontológicos (CPO-D, primeira consulta programática), que vão para o #19;
  - todas as especialidades;
  - regras por tipo de equipe.
