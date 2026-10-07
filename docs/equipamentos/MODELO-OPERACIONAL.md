# Modelo Operacional dos Equipamentos de Saúde

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0115](../adr/0115-modelo-operacional-dos-equipamentos.md), que aceita e completa
a [ADR-0053](../adr/0053-criterio-de-governanca-para-equipamentos-de-saude.md). **Nada implementado ainda.** A
implementação vem nas fatias MO1 a MO4 (seção 9).

> **Não estamos só modelando tipos de equipamentos; estamos modelando modos de operação diferentes dentro de uma
> mesma rede.** O equipamento define o contexto e as capacidades; os domínios definem os processos de negócio.

## 1. Critério de análise

Para cada equipamento, as perguntas são:
1. Qual é a finalidade?
2. É porta de entrada?
3. Trabalha com território ou adscrição?
4. Trabalha com atendimento?
5. Trabalha com agenda?
6. Trabalha com regulação?
7. Trabalha com internação ou leitos?
8. Trabalha com procedimentos?
9. Trabalha com diagnóstico?
10. Trabalha com dispensação?
11. Trabalha com transporte?
12. Trabalha com vigilância ou fiscalização?
13. Trabalha com equipes?
14. Qual é o fluxo operacional principal?
15. Que capacidades específicas tem?
16. Que outros domínios usa?
17. O que é comum e o que é específico?

**O resultado não são 17 arquiteturas: são os padrões operacionais que se repetem.**

## 2. Os equipamentos, um a um

O documento-fonte analisa 16 equipamentos. Para fechar 17, entra a **Policlínica**, que já é valor de
`TipoUnidadeDeSaude`.

| Equipamento | Finalidade | Fluxo principal | Objeto central | Conclusão |
|---|---|---|---|---|
| **UBS** | ponto de cuidado longitudinal, territorial e de entrada na rede | território → adscrição → equipe → acolhimento → atendimento → acompanhamento → referência | cidadão e família | equipamento genérico + perfil APS |
| **ESF** | modalidade de equipe da APS | equipe → território → área → microárea → famílias | equipe e território | **é equipe, não unidade** (`Equipe` tipo ESF, ADR-0103) |
| **UPA** | urgência de demanda espontânea, com classificação de risco e observação | entrada → acolhimento → classificação de risco → atendimento → observação → alta ou transferência | evento agudo | a porta de entrada e o fluxo precisam ser configuráveis |
| **Hospital** | complexo operacional | vários fluxos ao mesmo tempo: urgência, regulação, internação, cirurgia, ambulatório, diagnóstico, alta, transferência | vários | **o tipo não determina sozinho o comportamento**; o hospital é composto por serviços e capacidades |
| **CAPS** | atenção psicossocial | acolhimento → avaliação → acompanhamento → PTS → intervenções → rede | cidadão e projeto terapêutico | modalidade de cuidado; CAPS ≠ Saúde Mental (ADR-0112) |
| **Farmácia** | assistência farmacêutica | prescrição → validação → dispensação → registro → estoque | recurso (medicamento) | equipamento orientado a recurso, e não a atendimento |
| **Laboratório** | diagnóstico | solicitação → coleta → amostra → processamento → análise → resultado → prontuário | amostra e exame | fluxo próprio, além do assistencial genérico |
| **Centro de Especialidades** | atenção especializada | referência → especialidade → consulta ou procedimento → retorno e contrarreferência | encaminhamento | depende da rede de referência |
| **Centro Odontológico (CEO)** | saúde bucal especializada | referência → avaliação → plano → procedimentos → contrarreferência | cidadão e plano de tratamento | equipamento genérico + domínio especializado (ADR-0111) |
| **SAMU** | atendimento pré-hospitalar móvel | chamado → regulação → classificação → despacho → ambulância → equipe → atendimento no local → destino | chamado e ocorrência | **o local físico da organização é diferente do local onde o cuidado acontece** |
| **Centro de Reabilitação** | reabilitação longitudinal | avaliação → plano terapêutico → sessões → reavaliação → alta | cidadão e plano | acompanhamento longitudinal especializado |
| **Vigilância Sanitária** | fiscalização | estabelecimento → inspeção → irregularidade → medida → licença | **estabelecimento** | não opera sobre o cidadão: não precisa de paciente, agenda nem atendimento |
| **Zoonoses** | controle de zoonoses e vetores | evento → animal ou vetor → investigação → foco → ação de controle → território | **animal, vetor, foco** | ligado a Vigilância, Território e Emergências |
| **Consultório na Rua** | cuidado da população em situação de rua | equipe → território → população → abordagem → cuidado → rede | população e território | território flexível, sem microárea (ADR-0108) |
| **Unidade Móvel** | levar o cuidado aonde não há estrutura fixa | desloca → território → ação → retorna | ação no território | base operacional + localização + rota |
| **Unidade de vigilância e apoio** | suporte (vigilância, diagnóstico, logística, armazenamento, gestão) | próprio de cada uma | varia | **unidade organizacional ≠ ponto de atenção ≠ serviço operacional** |
| **Policlínica** | atenção especializada com vários serviços | referência → especialidades e exames → retorno | encaminhamento | como o Centro de Especialidades, com mais serviços: composição por serviços |

**A maior conclusão:** um equipamento de saúde pode operar sobre **cidadãos, estabelecimentos, animais, territórios ou
eventos**.

## 3. Matriz equipamentos × capacidades

Legenda: ● característico · ○ pode ter, conforme a configuração · — não se aplica

| Equipamento | Porta de entrada | Território | Atendimento | Agenda | Regulação | Leitos | Procedimento | Diagnóstico | Dispensação | Transporte | Vigilância e fiscalização | Equipe | Local do cuidado |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| UBS | ● | ● | ● | ● | ○ origem | — | ● | ○ | ○ | — | ○ notifica | ● | fixo + domicílio |
| ESF (equipe) | ● | ● | ● | ● | ○ origem | — | ○ | — | — | — | ○ notifica | ● | unidade + domicílio |
| UPA | ● | — | ● | — | ● | ○ observação | ● | ○ | ○ | ● | ○ notifica | ○ | fixo |
| Hospital | ● | — | ● | ● | ● | ● | ● | ● | ● | ● | ○ notifica | ● | fixo |
| CAPS | ● | ○ referência | ● | ● | ○ | ○ acolhimento noturno | ○ | — | ○ | — | — | ● | fixo + território |
| Farmácia | — | — | ○ | — | — | — | — | — | ● | — | — | ○ | fixo |
| Laboratório | — | — | ○ coleta | ○ coleta | ○ | — | — | ● | — | ○ amostras | — | ○ | fixo |
| Centro de Especialidades | — | — | ● | ● | ● executante | — | ● | ○ | — | — | — | ○ | fixo |
| Centro Odontológico (CEO) | — | — | ● | ● | ● executante | — | ● | ○ | — | — | — | ○ | fixo |
| SAMU | ● (192) | ○ área de cobertura | ● | — | ● | — | ● | — | — | ● | — | ● | **móvel, no local** |
| Centro de Reabilitação | — | — | ● | ● | ● executante | — | ● | ○ | ○ órteses | — | — | ● | fixo |
| Vigilância Sanitária | — | ● | — | ○ inspeções | — | — | — | — | — | — | ● | ● | **no estabelecimento** |
| Zoonoses | — | ● | — | ○ | — | — | — | ○ | — | ○ | ● | ● | **no território** |
| Consultório na Rua | ● | ● | ● | — | ○ origem | — | ○ | — | ○ | ○ | ○ notifica | ● | **na rua** |
| Unidade Móvel | ○ | ● | ● | ○ | ○ | — | ○ | ○ | ○ | ● | — | ● | **móvel, ponto programado** |
| Unidade de vigilância e apoio | — | ○ | — | — | — | — | — | ○ | ○ | ○ logística | ○ | ○ | fixo |
| Policlínica | — | — | ● | ● | ● executante | — | ● | ● | ○ | — | — | ○ | fixo |

**O que a matriz mostra:**
- **Nenhuma coluna é universal.** Atendimento, agenda e território faltam em vários equipamentos, então o núcleo não
  pode impô-los.
- **A última coluna** confirma o **ponto operacional**: o cuidado acontece no domicílio, na rua, no local do evento ou
  no ponto da unidade móvel, não só no endereço da unidade.
- **Os grupos de linhas parecidas** formam os seis padrões da seção 4.

## 4. Seis padrões operacionais

| Padrão | Equipamentos | Características |
|---|---|---|
| **1 · Cuidado territorial longitudinal** | UBS, ESF, Consultório na Rua | território, adscrição, equipe, acompanhamento, programas |
| **2 · Atendimento episódico** | UPA, Centro de Especialidades, CEO, Policlínica | agenda ou demanda, atendimento, procedimento, alta ou retorno |
| **3 · Cuidado longitudinal especializado** | CAPS, Centro de Reabilitação | avaliação, plano, intervenções, acompanhamento, rede |
| **4 · Complexo assistencial** | Hospital | vários serviços e fluxos: internação, leitos, emergência, cirurgia, diagnóstico |
| **5 · Serviço operacional com fluxo próprio** | Farmácia (dispensação), Laboratório (amostra e resultado), SAMU (despacho e mobilidade) | o objeto central não é o atendimento |
| **6 · Vigilância e controle** | Vigilância Sanitária, Zoonoses | evento, território, investigação, fiscalização, ação de controle |

Os padrões são **referência de desenho, não tipos**. Cada domínio novo declara qual padrão segue.

## 5. Metamodelo

Não basta `UnidadeDeSaude + TipoUnidade`. O metamodelo é:

```text
                    UNIDADE DE SAÚDE ─── tipo estrutural (ADR-0053)
                          │
             ┌────────────┴────────────┐
       Perfis operacionais         Estrutura
             │                         │
        Capacidades                 Setores (Administrativo)
             │                         │
         Serviços                   Equipes (ADR-0103)
             └────────────┬────────────┘
                 Fluxos (nos domínios especializados)

Unidade ── pode ter ── Pontos operacionais (base, local fixo, móvel, rota)
```

**Regra:** a unidade de saúde define onde e em que contexto a operação existe. Perfil e capacidades definem o que ela
pode fazer. Serviços e equipes definem como ela se organiza. Os domínios especializados definem como cada processo
funciona.

**Nada de herança:** `class UBS extends UnidadeDeSaude`, `class Hospital extends UnidadeDeSaude` levariam a
`HospitalMunicipal`, `UPA24h`, `CAPSII`, `CAPSad`, `USF`... A herança passaria a representar classificação, e não
comportamento. **Nem `HospitalService`, `UBSService`, `CAPSService`.**

### Composição: UBS e hospital

```text
UBS                                    Hospital
├── perfil: atenção primária           ├── perfil: hospitalar, urgência
├── capacidade: territorial            ├── serviço: emergência
├── equipe: eSF, eSB                   ├── serviço: internação, UTI
├── serviço: odontologia               ├── serviço: centro cirúrgico
├── serviço: farmácia                  ├── serviço: laboratório
└── serviço: sala de vacina            └── serviço: diagnóstico por imagem
```

Um "hospital especializado" ou uma "maternidade" não são classes novas: são um `HOSPITAL` com outro conjunto de
serviços e capacidades (ADR-0053, critério 3).

### Ponto operacional

- O **endereço da unidade não é necessariamente o local onde o cuidado acontece.**
- Hoje o `Atendimento` só tem a unidade. Ele passa a aceitar um **local de realização** opcional: domicílio, via
  pública, local do evento, escola, ponto da unidade móvel.
- A **unidade continua sendo o vínculo organizacional**: quem responde e onde o registro "mora".
- A **unidade móvel** tem base operacional, localização e rota programadas.
- **SAMU e ambulância** seguem a decisão do Mapa de Equipamentos: não são `UnidadeDeSaude`. A base e a central são
  unidades com perfil de atenção móvel; a ambulância é recurso (Transporte, #16, e Patrimônio, #15); o cuidado
  acontece no ponto do evento.

### Equipamento de saúde ≠ equipamento patrimonial

A **UPA** é um equipamento de saúde (#1 e #29). O **respirador** é um bem patrimonial e assistencial (#15 Patrimônio).

## 6. Catálogos iniciais

**Os catálogos são dado, editáveis,** no padrão de `CapacidadeAdministrativa` e `PerfilAdministrativo` (ADRs 0031,
0032), no contexto assistencial próprio.

| Perfil operacional | Exemplos de unidade |
|---|---|
| Atenção primária | UBS, unidade com eSF |
| Urgência | UPA, pronto-socorro do hospital |
| Internação | hospital |
| Atenção especializada | Centro de Especialidades, Policlínica, CEO |
| Atenção psicossocial | CAPS |
| Diagnóstico | Laboratório, serviço de imagem |
| Assistência farmacêutica | Farmácia |
| Vigilância | Vigilância Sanitária, Zoonoses |
| Reabilitação | Centro de Reabilitação |
| Atenção móvel | central e base do SAMU, unidade móvel |

| Capacidade assistencial (exemplos) | Usada por |
|---|---|
| acolhimento, demanda espontânea, atendimento programado, adscrição, acompanhamento longitudinal | APS, Território, Agenda |
| classificação de risco, observação, estabilização, transferência | urgência, Regulação |
| internação, UTI, leito de saúde mental, cirurgia | Leitos (ADR-0098), Saúde Mental |
| CAPS I, II, III, i, AD, AD III; acolhimento noturno | Saúde Mental (ADR-0112, SM5) |
| especialidades odontológicas (endodontia, periodontia, prótese...) | Saúde Bucal (ADR-0111, SB4) |
| coleta, processamento, análise, resultado | Laboratório |
| dispensação, controle de lote e validade | Farmácia |
| inspeção, fiscalização, licenciamento | Vigilância (ADR-0110, V3) |
| atendimento móvel, despacho | SAMU, Transporte |

A **capacidade emergencial** (ADR-0113, E3) é a capacidade normal mais a ativável, sobre este mesmo catálogo.

## 7. O que já existe

| Peça | Onde | Relação com o modelo |
|---|---|---|
| critério tipo × capacidade × especialização | ADR-0053 | base desta camada, agora aceita |
| equipamento → serviços → capacidades → profissionais → setores | [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md), seção 4 | o mesmo modelo, agora com perfil e ponto operacional |
| catálogo como dado + associação por tipo | `CapacidadeAdministrativa`, `PerfilAdministrativo`, `PerfilPorTipoUnidade` (ADRs 0031, 0032, 0037) | padrão a seguir, sem reaproveitar a entidade administrativa |
| `TipoUnidadeDeSaude` com 12 valores | Organização | continua estrutural; não cresce por classificação |
| setores | Administrativo (ADR-0030) | a estrutura da unidade |
| equipes (eSF, eSB, eMulti, CAPS, Consultório na Rua) | ADR-0103 | as equipes da unidade; a ESF é equipe |
| situação operacional e horário estruturado | ADR-0101 | estado operacional da unidade |
| território com finalidade | ADR-0108 | território flexível por modalidade |

## 8. Reconciliação com o plano de 31 itens

O documento-fonte propôs **renumerar** a plataforma em 31 itens. **Não renumeramos**: os números do
[`MAPA-DE-DOMINIOS.md`](../MAPA-DE-DOMINIOS.md) são citados pelas ADRs 0039 a 0115 e pelo STATUS. Os três itens que
faltavam entram ao fim, e o mapa também fica com 31:

| Plano de 31 itens | Nosso mapa |
|---|---|
| 1 Organização e Estrutura da Rede | #1 |
| 2 Recursos Humanos | #2 |
| 3 Administração | #3 (patrimônio, contratos e fornecedores ficam em #14 e #15) |
| 4 Gestão da Rede de Atenção | **#30 (novo, reservado)** |
| 5 Modelo Operacional dos Equipamentos | **#29 (novo, camada)** |
| 6 Paciente | #5 |
| 7 Atendimento | #4 |
| 8 Agenda | #7 |
| 9 Prontuário | #6 |
| 10 Enfermagem | #8 |
| 11 Saúde Bucal | #25 |
| 12 Saúde Mental | #26 |
| 13 Farmácia | #9 |
| 14 Laboratório e Diagnóstico | #10 |
| 15 Regulação | #11 |
| 16 Leitos e Internação | #12 |
| 17 Transporte Sanitário | #16 |
| 18 Território e Adscrição | #21 |
| 19 Programas, Ações e Linhas de Cuidado | #22 |
| 20 Vigilância (com Imunização) | #23; **Imunização fica como #24 próprio** (ADR-0110: atravessa vigilância, assistência e estoque) |
| 21 Estoque | #13 |
| 22 Compras, Contratos e Fornecedores | #14 |
| 23 Patrimônio e Manutenção | #15 |
| 24 Financeiro | #17 |
| 25 Qualidade, Segurança e Auditoria | #18 (Qualidade e Auditoria) |
| 26 Indicadores e BI | #19 |
| 27 Comunicação e Educação | #28 |
| 28 Integrações | #20 (parte Integrações) |
| 29 Identidade e Segurança | #20 (parte Identidade) |
| 30 Intersetorialidade e Proteção Social | **#31 (novo, reservado)** |
| 31 Emergências e Desastres | #27 |
| — (ausente no plano) | **Documentos**: mantido, pré-requisito de Comunicação e da VISA |

### Os dois domínios novos

- **#30 Gestão da Rede de Atenção** (a desenhar): como os equipamentos trabalham juntos.
  - Abrange oferta e pactuação assistencial, cotas da PPI (hoje pendentes na Regulação), redes temáticas (RAPS, Rede
    de Urgência, Rede Cegonha), participação de cada unidade na rede, referência estrutural e cobertura.
  - **A Regulação processa a necessidade individual; a Gestão da Rede define as relações estruturais.**
- **#31 Intersetorialidade e Proteção Social** (a desenhar): a interface da saúde com CRAS, CREAS, escolas, Conselho
  Tutelar, Defensoria, Ministério Público, abrigos e serviços sociais.
  - É o **catálogo único de instituições externas** e o **encaminhamento intersetorial**, que Saúde Mental (SM6),
    Vigilância (violência), Emergências (abrigos) e Comunicação reaproveitam.
  - A saúde não implementa o sistema social inteiro.

### Camadas conceituais

```text
┌──────────────────────────────────────────────────────────┐
│ GOVERNANÇA E TRANSVERSAIS                                 │
│ Identidade e Segurança · Integrações · BI · Auditoria · Qualidade · Documentos │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│ GESTÃO DA ORGANIZAÇÃO                                     │
│ Organização · MODELO OPERACIONAL · Gestão da Rede · RH · Administração │
│ Estoque · Compras · Patrimônio · Financeiro               │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│ ASSISTÊNCIA                                               │
│ Paciente · Atendimento · Agenda · Prontuário · Enfermagem │
│ Saúde Bucal · Saúde Mental · Farmácia · Laboratório       │
│ Regulação · Leitos · Transporte · Imunização              │
└──────────────────────────────────────────────────────────┘
                         ↓
┌──────────────────────────────────────────────────────────┐
│ TERRITÓRIO, PROGRAMAS E VIGILÂNCIA                        │
│ Território · Programas · Vigilância · Intersetorialidade  │
│ Emergências · Comunicação                                 │
└──────────────────────────────────────────────────────────┘
```

## 9. Fatias de implementação

| Fatia | Entrega | Destrava |
|---|---|---|
| **MO1** · perfis e capacidades | catálogos de perfil operacional e de capacidade assistencial, associação à unidade, aba "Capacidades" na tela Equipamentos | Saúde Bucal SB4 (CEO), Saúde Mental SM5 (CAPS), Emergências E3 (capacidade emergencial) |
| **MO2** · serviços | serviços da unidade (pronto-socorro, UTI, centro cirúrgico, sala de vacina), ligados a setores | Hospital composto; Leitos por serviço |
| **MO3** · ponto operacional | local de realização no Atendimento; base, localização e rota da unidade móvel | atendimento domiciliar, Consultório na Rua, SAMU (com Transporte) |
| **MO4** · domínios consultam capacidades | Leitos, Agenda, Saúde Mental, Saúde Bucal e Emergências passam a perguntar "a unidade tem a capacidade X?" | fim das regras implícitas por tipo |

**Fora do escopo:**
- redesenhar `TipoUnidadeDeSaude`;
- migrar o perfil administrativo;
- motor de fluxo configurável (BPM);
- herança de unidade.
