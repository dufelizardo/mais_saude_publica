# Modelo do domínio Saúde Bucal

**Data:** 2026-10-07 (criado) · 2026-10-21 (SB1 a SB3 implementadas) · 2026-10-22 (SB4 e SB5 implementadas)
**Status:** desenho aprovado na [ADR-0111](../adr/0111-saude-bucal.md). **A SB1 (especialidade e avaliação
odontológica), a SB2 (odontograma) e a SB3 (catálogo de procedimentos, procedimento odontológico e plano de
tratamento) estão implementadas** nas ADRs [0134](../adr/0134-saude-bucal-base.md),
[0135](../adr/0135-saude-bucal-odontograma.md), [0136](../adr/0136-saude-bucal-procedimento-odontologico.md) e
[0137](../adr/0137-saude-bucal-plano-de-tratamento.md); a SB4 (encaminhamento pela Regulação e prótese) nas
[0138](../adr/0138-saude-bucal-encaminhamento-pela-regulacao.md) e [0139](../adr/0139-saude-bucal-tratamento-protetico.md); e a SB5 (saúde bucal
coletiva) na [0140](../adr/0140-saude-bucal-coletiva.md). **O domínio está completo** (seção 14).

Saúde Bucal é um **domínio assistencial especializado**, e não um sistema odontológico isolado. Este documento
mostra o que ele traz de próprio, o que reaproveita dos domínios que já existem, o odontograma com histórico, o plano
de tratamento, a prótese e as ações coletivas.

## 1. Objetivo e ciclo

Representar o cuidado odontológico do cidadão ao longo do tempo:
- avaliação, diagnóstico e odontograma;
- procedimentos, plano de tratamento, evolução e conclusão;
- encaminhamentos e próteses;
- ações coletivas e acompanhamento preventivo;
- integração com a Atenção Primária e os demais níveis da rede.

```text
Paciente → Avaliação odontológica → Diagnóstico → Plano de tratamento → Procedimentos → Evolução → Conclusão / acompanhamento
```

## 2. Não duplicar: quem é dono de quê

**Regra principal:** Saúde Bucal é dona do conhecimento odontológico. O resto continua compartilhado.

| Conceito | Dono | Saúde Bucal usa |
|---|---|---|
| Paciente | Paciente (ADR-0040) | o mesmo paciente |
| Atendimento | Atendimento (ADR-0041) | o atendimento com especialidade **odontologia** |
| Prontuário | agregação (ADR-0045) | a avaliação, o odontograma, o plano e os procedimentos entram na agregação |
| Agenda | Agenda (ADRs 0091, 0092) | a agenda do dentista, filtrada por especialidade |
| Profissional e lotação | RH | o cirurgião-dentista, o técnico e o auxiliar do RH |
| Equipe | Equipe (ADR-0103) | a equipe **eSB** que já existe |
| Encaminhamento | Regulação (ADR-0087) | especialidades odontológicas no catálogo da Regulação |
| Território | #21 (ADR-0108) | local das ações coletivas |
| Ações | Programas (#22, ADR-0109) | ação programática com detalhe odontológico |
| **Avaliação, odontograma, plano, prótese** | **Saúde Bucal** | — |

**Não se cria:** `PacienteOdontologico`, `ProntuarioOdontologico`, `AtendimentoOdontologico`, `AgendaOdontologica`,
`EquipeSaudeBucal` nem um cadastro próprio de dentista.

```text
Paciente
   └── Atendimento (especialidade: ODONTOLOGIA)
          └── dados odontológicos (avaliação, registros do odontograma, procedimentos com dente e face)
```

## 3. A equipe de Saúde Bucal já existe

A `Equipe` do tipo **eSB** (ADR-0103) já tem:
- as funções cirurgião-dentista, técnico em saúde bucal e auxiliar de saúde bucal;
- a composição mínima calculada (dentista e técnico ou auxiliar);
- os membros como histórico, com lotação vigente na unidade.

O domínio usa essa equipe. Os profissionais continuam no RH:

```text
RH: Profissional → Lotação          Equipe (ADR-0103): eSB → MembroEquipe (função)
```

## 4. Especialidade no atendimento

Hoje o `Atendimento` tem só o tipo (consulta, urgência, internação). Ele ganha uma **especialidade**: clínica geral,
enfermagem, odontologia, pediatria e outras, como catálogo. A mudança é pequena e serve a todas as especialidades, não
só à odontologia:
- os dados odontológicos ficam ligados ao atendimento de odontologia;
- a Agenda ganha o filtro por especialidade que está pendente desde a ADR-0092.

## 5. Avaliação odontológica

`AvaliacaoOdontologica`, ligada ao atendimento:

```text
Atendimento
   └── AvaliacaoOdontologica
          ├── queixa principal e anamnese
          ├── exame clínico e achados
          ├── diagnóstico e condições bucais
          ├── risco (classificação de risco em saúde bucal)
          ├── observações
          └── plano inicial
```

Registro clínico não se edita: uma correção é uma nova versão ligada à anterior (ADR-0062).

## 6. Odontograma com histórico

O elemento mais específico do domínio. **Não é um conjunto de campos no paciente**, porque a situação dos dentes
muda ao longo do tempo.

### 6.1 Notação

- **FDI (ISO 3950):** dentes permanentes de **11 a 48**, em quatro quadrantes; decíduos de **51 a 85**.
- **Faces:** mesial (M), distal (D), oclusal ou incisal (O/I), vestibular (V) e lingual ou palatina (L/P).

### 6.2 Registros, não estado

Cada mudança é um **registro de condição**, e o odontograma de uma data é a **projeção** dos registros até ela:

```text
RegistroOdontograma
 ├── paciente
 ├── dente (FDI) e face (opcional: a condição pode ser do dente inteiro)
 ├── condição (do catálogo)
 ├── data
 └── origem: a avaliação ou o procedimento que causou a mudança
```

As **condições são catálogo**, sem enum: hígido, cárie, restaurado, restauração insatisfatória, ausente, extração
indicada, selante, prótese, fratura, implante, raiz residual.

Exemplo do dente 36:

| Data | Face | Condição | Origem |
|---|---|---|---|
| 02/03 | O | cárie | avaliação de 02/03 |
| 02/03 | — | — | plano: restauração do 36 (item planejado) |
| 16/03 | O | restaurado | procedimento "restauração em resina", 16/03 |

Com isso o sistema responde:
- **como estava o 36 antes do tratamento?** A projeção em 15/03 mostra cárie na face oclusal.
- **qual procedimento mudou a condição?** O registro de 16/03 aponta para o procedimento.

O desenho começa simples, sem odontograma extremamente detalhado. Mais faces ou condições entram pelo catálogo.

**Implementado na SB2 ([ADR-0135](../adr/0135-saude-bucal-odontograma.md)):** as marcações vêm da avaliação
odontológica vigente; a oclusal só nos posteriores e a incisal só nos anteriores; o registro do dente inteiro vale por
cima das faces registradas antes; a marcação errada é anulada com motivo e fica no histórico.

## 7. Procedimentos: catálogo geral compartilhado

Hoje o `Procedimento` é um registro **realizado** na consulta, com tipo em texto livre. Não há catálogo geral, só
catálogos isolados: `ProcedimentoRegulado` (Regulação) e `ExameLaboratorial` (Laboratório).

**Decisão:**
- Cria-se um **catálogo geral de procedimentos**, com especialidade e com o código SIGTAP opcional para a integração
  futura. **Não é um catálogo odontológico.**
- O `Procedimento` realizado passa a apontar para o catálogo. O texto livre continua nos registros antigos.
- O procedimento odontológico é o **`Procedimento` normal mais um complemento odontológico**: dente e faces. O
  complemento gera os registros do odontograma.

```text
Atendimento odontológico
 ├── Avaliação
 ├── Procedimento: restauração em resina (dente 36, face O)
 ├── Procedimento: aplicação tópica de flúor
 └── Procedimento: exodontia (dente 48)
```

**Unificar os catálogos** de Regulação, Laboratório e o geral fica registrado como pendência, fora desta ADR.

**Implementado na SB3a ([ADR-0136](../adr/0136-saude-bucal-procedimento-odontologico.md)):** o procedimento passou a
ficar no atendimento, com a consulta opcional, para o técnico em saúde bucal registrar sem consulta. O item do
catálogo diz o que o complemento pede (nada, o dente ou o dente e as faces), a condição que grava no odontograma
quando realizado e se é só do dentista. A retificação do procedimento anula os registros dele no odontograma e grava
os novos.

## 8. Plano de tratamento

O tratamento odontológico acontece em várias consultas, então não basta "atendimento → procedimentos":

```text
PlanoTratamento
 ├── diagnóstico, objetivo, prioridade
 ├── situação (aberto, concluído, abandonado)
 └── itens
      ├── Restauração 16        CONCLUÍDO     (procedimento de 09/03)
      ├── Tratamento 26         EM_ANDAMENTO
      ├── Extração 38           PLANEJADO
      └── Prótese               ENCAMINHADO   (solicitação de regulação)
```

Cada item tem o procedimento previsto, o dente e a situação (planejado, em andamento, concluído, encaminhado,
cancelado), e aponta para o procedimento que o executou. A conclusão do plano é o **tratamento concluído**, base de
indicador da APS.

**Implementado na SB3b ([ADR-0137](../adr/0137-saude-bucal-plano-de-tratamento.md)):** um plano aberto por paciente;
o item fica concluído só pelo procedimento realizado que corresponde a ele (mesmo item do catálogo, dente e faces);
o encaminhado e o cancelado não impedem a conclusão, que pede ao menos um item concluído; o abandono pede motivo.

## 9. Encaminhamento e CEO

Não há fluxo odontológico próprio. O encaminhamento usa a **Regulação** (ADRs 0087 a 0089):
- **Especialidades:** endodontia, periodontia, cirurgia bucomaxilofacial, prótese, odontopediatria e atendimento a
  pacientes com necessidades especiais entram como itens do **catálogo da Regulação**.
- **Executante:** o **CEO** (Centro de Especialidades Odontológicas, no Mapa de Equipamentos).
- **Plano:** o item fica "encaminhado", e a contrarreferência volta para ele.
- **Tipo do CEO:** segue o critério da ADR-0053 (tipo próprio ou `CENTRO_ESPECIALIDADES` com especialização).
  **Decidido na SB4a ([ADR-0138](../adr/0138-saude-bucal-encaminhamento-pela-regulacao.md)):** sem tipo próprio; o CEO
  é a unidade de especialidades com as capacidades odontológicas do Modelo Operacional, e o item regulado exige a
  capacidade da executante.

```text
Saúde Bucal (item do plano) → Regulação → CEO / serviço de referência → contrarreferência
```

## 10. Prótese

Não é um procedimento só: tem um ciclo.

```text
Avaliação → Indicação → Planejamento → Moldagem e etapas → Produção → Entrega → Adaptação → Acompanhamento
```

`TratamentoProtetico`, ligado ao plano:
- tipo (total, parcial removível, unitária);
- situação e etapa;
- datas de solicitação e de entrega;
- **serviço responsável**: o laboratório regional de prótese dentária (LRPD), interno ou contratado;
- observações.

O detalhe do laboratório (etapas de produção, custos) fica para depois.

**Implementado na SB4b ([ADR-0139](../adr/0139-saude-bucal-tratamento-protetico.md)):** as etapas seguem a ordem do
ciclo, com a prova e o laboratório podendo se alternar; o laboratório é a unidade com a capacidade LRPD do Modelo
Operacional ou o contratado; a entrega da unitária grava "Prótese" no odontograma; a prótese não impede concluir o
plano da APS.

## 11. Ações coletivas

São diferentes do atendimento individual, mas **não ganham motor próprio**: são **ações programáticas ou atividades
coletivas** de Programas (ADR-0109, P4), com o **detalhe odontológico**:
- **tipos:** escovação supervisionada, aplicação tópica de flúor, levantamento epidemiológico, educação em saúde,
  avaliação coletiva;
- **local:** escola (Programa Saúde na Escola), UBS, comunidade, empresa, instituição ou território;
- **público:** a população atendida (contagem, e lista quando houver).

```text
Programa (Saúde da Criança, Saúde na Escola) → Ação → detalhe de Saúde Bucal
Equipe eSB → Ação coletiva → Território / instituição → população atendida
```

Programas não ficam dentro de Saúde Bucal.

**Implementado na SB5 ([ADR-0140](../adr/0140-saude-bucal-coletiva.md)):** o detalhe vale para a ação coletiva dos
programas Saúde Bucal e Saúde na Escola (PSE, que entrou no catálogo); a avaliação de risco por participante pede a
lista nominal e é do dentista; quem precisa de tratamento e não tem plano aberto vai para a lista a convocar.

## 12. Quem faz o quê

| Função | Pode | Base |
|---|---|---|
| Cirurgião-dentista | avaliação, diagnóstico, plano, todos os procedimentos | Lei 5.081/1966 |
| Técnico em saúde bucal | procedimentos permitidos, sob supervisão do dentista | Lei 11.889/2008 |
| Auxiliar de saúde bucal | apoio, sem registro clínico próprio | Lei 11.889/2008 |

- **Permissões previstas:** `ODONTOLOGIA.REGISTRAR` para avaliação e procedimentos e `ODONTOLOGIA.PLANEJAR` para o
  plano de tratamento, combinadas com a função na equipe.
- **Dado de saúde:** o registro odontológico segue as regras do prontuário: vínculo (ADR-0076), auditoria de leitura
  (ADR-0070) e retificação (ADR-0062).

## 13. Entidades previstas

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `Especialidade` (catálogo) e `Atendimento.especialidade` | nome, ativa | SB1 |
| `AvaliacaoOdontologica` | atendimento, queixa, anamnese, exame, achados, diagnóstico, risco, plano inicial, retificação | SB1 |
| `CondicaoOdontologica` (catálogo) | código, nome, de face ou de dente, cor no odontograma | SB2 |
| `RegistroOdontograma` | paciente, dente (FDI), face, condição, data, origem (avaliação ou procedimento) | SB2 |
| `ProcedimentoCatalogo` | código, nome, especialidade, SIGTAP (opcional), ativo | SB3 |
| `ComplementoOdontologico` | procedimento realizado, dente, faces | SB3 |
| `PlanoTratamento`, `ItemPlanoTratamento` | paciente, diagnóstico, objetivo, prioridade, situação; item com procedimento previsto, dente, situação, procedimento executado | SB3 |
| `TratamentoProtetico` | plano, tipo, situação, etapa, datas, serviço responsável | SB4 |
| detalhe odontológico da ação coletiva | tipo de ação, local, público atendido | SB5 |

Organização lógica prevista (não é uma entidade por pasta):

```text
saude-bucal/
├── avaliacao/     AvaliacaoOdontologica
├── odontograma/   CondicaoOdontologica, RegistroOdontograma
├── tratamento/    PlanoTratamento, ItemPlanoTratamento, ComplementoOdontologico, TratamentoProtetico
└── coletivo/      detalhe odontológico das ações de Programas
```

## 14. Fatias de implementação

| Fatia | Entrega | Depende de |
|---|---|---|
| **SB1** · base ✅ ([ADR-0134](../adr/0134-saude-bucal-base.md)) | especialidade no atendimento e na Agenda; avaliação odontológica | Atendimento, Equipe eSB |
| **SB2** · odontograma ✅ ([ADR-0135](../adr/0135-saude-bucal-odontograma.md)) | catálogo de condições, registros com histórico, projeção por data, tela do odontograma | SB1 |
| **SB3** · tratamento ✅ (SB3a [ADR-0136](../adr/0136-saude-bucal-procedimento-odontologico.md); SB3b [ADR-0137](../adr/0137-saude-bucal-plano-de-tratamento.md)) | catálogo geral de procedimentos, complemento odontológico, plano de tratamento, evolução | SB2 |
| **SB4** · regulação e prótese ✅ (SB4a [ADR-0138](../adr/0138-saude-bucal-encaminhamento-pela-regulacao.md); SB4b [ADR-0139](../adr/0139-saude-bucal-tratamento-protetico.md)) | especialidades no catálogo da Regulação, CEO como executante, tratamento protético | SB3, Regulação |
| **SB5** · saúde bucal coletiva ✅ ([ADR-0140](../adr/0140-saude-bucal-coletiva.md)) | detalhe odontológico das ações coletivas | Programas P4, Território |

## 15. Dependências

```text
                 RH
                  │
                Equipe (eSB)
                  │
Paciente ─── Atendimento (especialidade)
                  │
            SAÚDE BUCAL
             │         │
       Odontograma  Tratamento
                       │
                   Regulação → Rede de atenção (CEO, LRPD)

Território ──► Ações coletivas      Programas ──► Ações
Agenda ──────► Atendimento          Prontuário ──► registro clínico
```

**Fora do escopo agora:**
- todos os códigos oficiais (SIGTAP completo);
- integração com e-SUS e SISAB;
- módulo completo de laboratório de prótese;
- indicadores odontológicos (CPO-D, primeira consulta odontológica programática, tratamento concluído), que vão para
  o #19 Indicadores;
- todas as especialidades;
- regras por tipo de equipe.
