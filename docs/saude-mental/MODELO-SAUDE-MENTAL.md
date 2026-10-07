# Modelo do domínio Saúde Mental

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0112](../adr/0112-saude-mental.md). **Nada implementado ainda.** A
implementação vem nas fatias SM1 a SM6 (seção 16), cada uma com ADR própria.

O foco do domínio é a **atenção psicossocial**, e não um "prontuário de psicologia". O núcleo é
**PTS + acompanhamento longitudinal + crise + rede**. É isso que diferencia Saúde Mental de criar uma especialidade
"psicologia" ou "psiquiatria" dentro do Atendimento.

## 1. Objetivo e ciclo

Representar o cuidado em saúde mental do primeiro contato ao acompanhamento longitudinal:

```text
Acolhimento → Avaliação → Necessidade → Plano terapêutico → Acompanhamento → Intervenções → Articulação da rede → Alta / continuidade
```

Vale para a atenção básica, o CAPS, a urgência, a atenção especializada e hospitalar, o cuidado compartilhado e as
situações de crise.

## 2. Não duplicar: quem é dono de quê

| Conceito | Dono | Saúde Mental usa |
|---|---|---|
| Paciente | Paciente (ADR-0040) | o mesmo paciente |
| Atendimento | Atendimento | atendimento com especialidade (psicologia, psiquiatria, ADR-0111) |
| Prontuário | agregação (ADR-0045) | registros do domínio entram na agregação, com o sigilo da seção 13 |
| Profissional, lotação | RH | o profissional do RH |
| Equipe | Equipe (ADR-0103) | CAPS multi, eMulti, eSF como equipe de referência; apoio matricial |
| Agenda | Agenda (ADRs 0091, 0092) | a agenda do profissional |
| Encaminhamento | Regulação (ADRs 0087 a 0089) | referência e contrarreferência |
| Internação | Leitos e internação (ADR-0098) | a internação, com a modalidade da Lei 10.216 (seção 10) |
| Programa | Programas (ADR-0109) | o programa Saúde Mental, para contagem e indicadores (seção 6) |
| Notificação | Vigilância (ADR-0110) | violência autoprovocada (seção 9) |
| **Acolhimento, avaliação, risco, acompanhamento, PTS, intervenções, crise, rede de apoio** | **Saúde Mental** | — |

**Não se cria:** `PacienteMental`, `ProntuarioMental`, `AtendimentoMental` nem `Caps.java`.

## 3. CAPS não é Saúde Mental

O **CAPS é um equipamento** da rede (`TipoUnidadeDeSaude.CAPS`); **Saúde Mental é o domínio assistencial**. A pessoa
pode ser acompanhada pela saúde mental sem estar num CAPS:

```text
Saúde Mental
 ├── UBS (atenção básica, com matriciamento)
 ├── CAPS
 ├── urgência e emergência
 ├── hospital
 ├── serviço residencial
 └── outros pontos da rede
```

As **modalidades de CAPS** (I, II, III, i, AD, AD III) e os serviços oferecidos (acolhimento, acompanhamento,
atividades coletivas, cuidado intensivo, acolhimento noturno) são **capacidades ou especializações da unidade**, pelo
critério da ADR-0053. Não são uma entidade paralela à unidade.

### Componentes da RAPS

A Rede de Atenção Psicossocial (Portaria de Consolidação nº 3/2017) serve de referência para classificar os pontos da
rede:

| Componente | Exemplos |
|---|---|
| Atenção básica | UBS, eSF, eMulti, Consultório na Rua, centros de convivência |
| Atenção psicossocial especializada | CAPS nas modalidades |
| Urgência e emergência | SAMU, UPA, pronto-socorro |
| Atenção residencial de caráter transitório | unidades de acolhimento |
| Atenção hospitalar | leitos de saúde mental em hospital geral |
| Desinstitucionalização | serviços residenciais terapêuticos |
| Reabilitação psicossocial | geração de trabalho e renda, cooperativas |

## 4. Acolhimento não é triagem

A triagem de enfermagem (ADR-0047) classifica risco clínico. O **acolhimento em saúde mental** é outra coisa:

```text
Pessoa → AcolhimentoSaudeMental
          ├── motivo da procura
          ├── necessidade imediata
          ├── situação de vulnerabilidade
          ├── risco inicial
          └── desfecho: acompanhamento na unidade · cuidado compartilhado · encaminhamento · crise
```

## 5. Avaliação

`AvaliacaoSaudeMental` reúne:
- motivo da procura, contexto e história;
- aspectos psicossociais e avaliação clínica;
- fatores de risco e de proteção;
- necessidades identificadas;
- hipótese diagnóstica, quando couber.

O diagnóstico usa o **modelo clínico geral (CID)**: o domínio não tem catálogo próprio de transtornos. Escalas
clínicas ficam para depois.

## 6. Um só acompanhamento

O cidadão tem uma **trajetória de cuidado**: atendimentos, atividade coletiva, crise, retorno.
`AcompanhamentoSaudeMental` é esse **caso clínico longitudinal**:

```text
Paciente → AcompanhamentoSaudeMental
            ├── responsável e equipe de referência
            ├── PTS vigente (e as versões anteriores)
            ├── intervenções e atividades
            ├── encaminhamentos
            ├── crises
            └── início, alta ou continuidade
```

**Sem duplicar Programas:**
- Programas (ADR-0109) já tem o programa **Saúde Mental**, marcado como sensível, e o `AcompanhamentoProgramatico`.
- Ao abrir o acompanhamento em saúde mental, o domínio **registra ou reaproveita o acompanhamento programático**
  desse programa.
- A contagem ("pessoas em acompanhamento") e os indicadores vêm do programa. O detalhe clínico (PTS, risco, crise)
  fica aqui.

## 7. Projeto Terapêutico Singular (PTS)

O **agregado central** do domínio. Organiza o cuidado individualizado e é construído em equipe, com a pessoa e a
família:

```text
ProjetoTerapeuticoSingular
 ├── necessidades
 ├── objetivos e metas
 ├── intervenções previstas
 ├── profissionais responsáveis e equipe de referência
 ├── serviços envolvidos
 ├── rede de apoio
 ├── periodicidade
 └── data de revisão
```

**É versionado.** A revisão gera uma versão nova; a anterior não é sobrescrita:

| Versão | Data | Mudança |
|---|---|---|
| v1 | 03/02 | objetivos: vínculo com o CAPS e adesão à medicação; oficina semanal |
| v2 | 05/05 | revisão: adesão alcançada; novo objetivo de retorno ao trabalho; encaminhamento intersetorial |
| v3 | 02/08 | revisão: alta do CAPS, com cuidado compartilhado com a eSF |

## 8. Intervenções

O cuidado não se resume a consulta. `IntervencaoSaudeMental` tem **tipo configurável**:
- atendimento individual e familiar;
- grupo e oficina;
- atividade comunitária e visita;
- atendimento compartilhado e orientação;
- intervenção em crise.

Grupos e oficinas usam as **atividades coletivas e ações** de Programas (ADR-0109, P4), sem motor próprio.

## 9. Risco e crise

### 9.1 Risco com histórico

O risco é separado da avaliação, porque é acompanhado ao longo do tempo. **Nunca um `boolean riscoAlto`:**

```text
AvaliacaoRisco
 ├── tipo (autoagressão, heteroagressão, vulnerabilidade, uso de substâncias...)
 ├── nível
 ├── data e avaliador
 ├── situação
 └── medidas adotadas
```

A reavaliação gera um registro novo. Exemplo: em 10/03, autoagressão alta, com plano de segurança e contato diário;
em 24/03, autoagressão moderada e contato semanal.

### 9.2 Crise

A crise **não é simplesmente um atendimento**. `EventoCrise` acompanha:

```text
Identificação → Avaliação → Intervenção → Acionamento da rede → Encaminhamento → Acompanhamento pós-crise
```

Envolve a unidade de origem, a equipe, a urgência, o SAMU, o CAPS, o hospital, a família e a rede de apoio.
- **Notificação:** crise com **autoagressão**, inclusive tentativa de suicídio, gera **sugestão de notificação** de
  violência autoprovocada, que é compulsória (Vigilância, ADR-0110). Notificar é ato profissional.
- **Dependência:** acionar a urgência e o SAMU depende da **regulação de urgência**, que está pendente na Regulação.

## 10. Internação

A internação em saúde mental **usa a Internação que já existe** (ADR-0098). Hoje ela tem só o caráter, eletiva ou
urgência. Entram:
- a **modalidade**: voluntária, involuntária ou compulsória (Lei 10.216/2001, art. 6º);
- na **involuntária**, a comunicação obrigatória ao **Ministério Público em até 72 horas**, e de novo na alta (Lei
  10.216/2001, art. 8º), com prazo e registro de quem comunicou.

É um campo e uma regra na Internação, não uma internação paralela.

## 11. Equipe e técnico de referência

```text
Unidade → Equipe (CAPS multi, eMulti, eSF) → profissionais (RH) → pessoa acompanhada
```

- **Equipe de referência:** a equipe responsável pelo acompanhamento. **Não é a lotação** do profissional, que
  continua no RH.
- **Técnico de referência:** o profissional que articula o PTS e é a referência da pessoa.
- **Cuidado compartilhado e matriciamento:** usam o **apoio matricial** da eMulti às eSF e eAP, que já existe
  (ADR-0103).

## 12. Rede de apoio e intersetorialidade

- **Rede de apoio, simples no início:** familiares, cuidadores, pessoas de referência e serviços, ligados ao PTS, com
  contato e papel.
- **Rede de atenção:** referência e contrarreferência pela **Regulação** (ADRs 0087 a 0089). O caminho é UBS → CAPS →
  urgência → hospital → retorno ao CAPS ou à UBS.
- **Intersetorialidade:** assistência social (CRAS, CREAS), educação, justiça, trabalho e habitação entram como
  **encaminhamento a instituição externa**, de um catálogo simples. O sistema não implementa esses domínios.

## 13. Sigilo e permissões

Saúde mental é **dado sensível no grau mais alto** (LGPD, art. 11):
- **Quem vê:** a visibilidade é restrita à equipe de referência e a quem tem vínculo assistencial (ADR-0076). O
  programa Saúde Mental já é marcado sensível (ADR-0109).
- **Leitura auditada** (ADR-0070).
- **Psicoterapia:** o conteúdo das sessões **fica fora do prontuário compartilhado**, por sigilo profissional. Entra
  só o registro de que a intervenção aconteceu, com um resumo.
- **Relatórios e indicadores** só com agregados.

| Permissão | Para quê |
|---|---|
| `SAUDE_MENTAL.ACOLHER` | registrar acolhimento |
| `SAUDE_MENTAL.ACOMPANHAR` | avaliação, PTS, intervenções, acompanhamento |
| `SAUDE_MENTAL.CRISE` | registrar e conduzir evento de crise |

As permissões se combinam com o vínculo e a equipe de referência.

## 14. Entidades previstas

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `AcolhimentoSaudeMental` | paciente, unidade, profissional, motivo, necessidade, vulnerabilidade, risco inicial, desfecho | SM1 |
| `AvaliacaoSaudeMental` | acompanhamento, contexto, história, aspectos psicossociais, fatores de risco e proteção, necessidades, CID (opcional) | SM1 |
| `AcompanhamentoSaudeMental` | paciente, unidade, equipe de referência, técnico de referência, acompanhamento programático, início, fim, situação | SM1 |
| `ProjetoTerapeuticoSingular` | acompanhamento, versão, necessidades, objetivos, metas, periodicidade, revisão em, responsáveis | SM2 |
| `ObjetivoTerapeutico`, `IntervencaoPrevista` | PTS, descrição, meta, prazo, responsável, serviço | SM2 |
| `IntervencaoSaudeMental` | acompanhamento, tipo (catálogo), data, profissionais, resumo, atividade coletiva (opcional) | SM2 |
| `PessoaRedeApoio` | PTS ou acompanhamento, nome, papel, contato, serviço (opcional) | SM2 |
| `AvaliacaoRisco` | acompanhamento, tipo, nível, data, avaliador, situação, medidas | SM3 |
| `EventoCrise` | paciente, local, etapas, intervenção, serviços acionados, encaminhamento, pós-crise, notificação sugerida | SM3 |
| modalidade e comunicação na `Internacao` | modalidade, comunicação ao Ministério Público (prazo, data, responsável) | SM3 |
| capacidades de CAPS e RAPS na unidade | modalidade, componente da RAPS, serviços | SM5 |
| `InstituicaoExterna`, `EncaminhamentoIntersetorial` | nome, setor, contato; acompanhamento, motivo, retorno | SM6 |

Organização lógica prevista:

```text
saude-mental/
├── acolhimento/     AcolhimentoSaudeMental
├── avaliacao/       AvaliacaoSaudeMental, AvaliacaoRisco
├── acompanhamento/  AcompanhamentoSaudeMental
├── terapeutico/     ProjetoTerapeuticoSingular, ObjetivoTerapeutico, IntervencaoSaudeMental, PessoaRedeApoio
├── crise/           EventoCrise
└── rede/            InstituicaoExterna, EncaminhamentoIntersetorial (a referência é da Regulação)
```

## 15. Dependências

```text
                    RH
                    │
                  Equipe (CAPS multi, eMulti, eSF)
                    │
Paciente ─── Atendimento (especialidade)
                    │
              SAÚDE MENTAL
              │     │      │
         Avaliação  PTS    Crise ──► Vigilância (notificação de autoagressão)
              │     │      │
              └─────┼──────┘
                    ▼
             Acompanhamento ──► Programas (programa Saúde Mental)
                    │
             REDE DE ATENÇÃO (Regulação)
              │       │
             CAPS   Hospital (Internação com modalidade)
              └───┬───┘
                  ▼
           Contrarreferência

Território ──► população e adscrição    Programas ──► ações    Intersetorialidade ──► instituições externas
```

## 16. Fatias de implementação

| Fatia | Entrega | Depende de |
|---|---|---|
| **SM1** · base | acolhimento, avaliação, acompanhamento ligado ao programa, equipe e técnico de referência | Programas P2, Atendimento com especialidade (ADR-0111, SB1) |
| **SM2** · PTS | objetivos, metas, intervenções previstas, rede de apoio, versões e revisão; registro de intervenções | SM1 |
| **SM3** · risco e crise | avaliação de risco com histórico, evento de crise, pós-crise, sugestão de notificação; modalidade da internação e comunicação ao Ministério Público | SM1, Vigilância V1, regulação de urgência |
| **SM4** · rede | referência e contrarreferência pela Regulação; cuidado compartilhado e matriciamento | SM1 |
| **SM5** · CAPS e RAPS | modalidades de CAPS e componentes da RAPS como capacidades da unidade; serviços oferecidos | ADR-0053 |
| **SM6** · intersetorialidade e indicadores | instituições externas, encaminhamento intersetorial, indicadores para o #19 | SM2 |

**Fora do escopo agora:**
- prontuário psiquiátrico completo;
- catálogo de transtornos;
- escalas clínicas;
- prescrição específica de psicotrópicos (a Farmácia e o controle especial da Portaria SVS/MS 344/1998 ficam onde
  estão);
- todas as modalidades de CAPS em detalhe;
- integrações externas;
- indicadores complexos;
- protocolos clínicos rígidos.

## 17. Referências

- Lei nº 10.216/2001: proteção e direitos das pessoas com transtornos mentais, modalidades de internação e
  comunicação ao Ministério Público.
- Portaria de Consolidação GM/MS nº 3/2017, anexo V: Rede de Atenção Psicossocial (RAPS).
- Portaria SVS/MS nº 344/1998: medicamentos sujeitos a controle especial.
