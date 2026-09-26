# Mapa de Equipamentos de Saúde

**Data:** 2026-09-26
**Status:** Vivo — levantamento conceitual, sem implementação associada. Ver
[ADR-0053](./adr/0053-criterio-de-governanca-para-equipamentos-de-saude.md) para a decisão de
governança que este mapa fundamenta.

## 1. Propósito e como ler este documento

Este documento nasce de [`docs/pm/sistema_de_saude_brasileiro.md`](./pm/sistema_de_saude_brasileiro.md),
um levantamento de ~30 tipos de estabelecimento/serviço de saúde brasileiro com base na estrutura do
CNES. Recebe o **mesmo tratamento** que todo material externo já trazido para este projeto (ver
`MAPA-DE-DOMINIOS.md` §1): é **checklist de vocabulário e de estrutura**, não especificação literal
a adotar em bloco.

O documento-fonte identifica uma conclusão importante: **"equipamento de saúde" não é sinônimo de
"tipo de `UnidadeDeSaude`"**. A rede de saúde brasileira é mais rica que uma lista plana — tem
naturezas diferentes (ponto assistencial, serviço de apoio, estrutura territorial, recurso móvel), e
tratar tudo como "tipo de unidade" acabaria inflando `TipoUnidadeDeSaude` com dezenas de valores
equivalentes entre si só na forma, não no comportamento.

Este mapa **não decide implementação** — decide vocabulário e organiza o levantamento para que
nenhuma informação se perca até que a onda de cada equipamento chegue (mesma disciplina de
`DER.md` §Apêndice e ADR-0039 decisão 8). Quando um domínio precisar de fato deste equipamento, a
implementação segue o critério da ADR-0053 (enum estrutural vs. capacidade-como-dado vs.
especialização de domínio nova).

## 2. As 4 naturezas

O documento-fonte agrupa os ~30 equipamentos em 4 naturezas, preservadas aqui como taxonomia
descritiva (não campo/coluna — ver ADR-0053 critério 4):

```text
Natureza
  ├── Ponto assistencial               (presta atendimento direto ao usuário)
  ├── Serviço/estrutura de apoio à rede (dá suporte técnico a outros pontos, não atende
  │                                      diretamente o cidadão como público principal)
  ├── Estrutura territorial/especializada (foco em população ou agravo específico)
  └── Recurso/serviço móvel             (não tem endereço fixo como unidade de atendimento)
```

## 3. Tabela de equipamentos

Colunas:
- **Papel na rede** — função da estrutura na rede de atenção (não repete "Função", é a lente de
  "que tipo de trabalho a rede espera desse equipamento").
- **Correspondência atual** — valor de `TipoUnidadeDeSaude` relacionado, ou "nenhuma".
- **Situação** — `Implementado` (equivale 1:1 a um valor do enum), `Base existente` (o enum cobre a
  categoria ampla, mas a especialização/perfil não está modelada), `Não modelado` (é especialização
  de uma categoria existente, mas sem perfil/campo próprio ainda) ou `Não implementado` (nenhuma
  correspondência hoje).

> Uma correspondência "Base existente" ou "Não modelado" **não** é um convite para criar um novo
> valor de enum — ver ADR-0053 critério 3. A pergunta certa para, por exemplo, Hospital
> Especializado, é "isso muda comportamento estrutural do enum, ou é um perfil/especialidade de
> `HOSPITAL`?" — e essa decisão fica para quando o domínio de fato for desenhado.

### 3.1 Natureza: Ponto assistencial

| Equipamento | Papel na rede | Função | Público | Serviços | Capacidades | Depende de | Pode conter | Correspondência atual | Situação |
|---|---|---|---|---|---|---|---|---|---|
| UBS / Centro de Saúde | Porta de entrada / APS | Consulta, enfermagem, vacinação, pré-natal, saúde da família, vigilância territorial | População adscrita | Medicina, Enfermagem, Vacinação, Saúde Bucal, Farmácia, Procedimentos, Visita domiciliar | APS, procedimento, vacinação, visita domiciliar | RH, Agenda | Farmácia, vacinação, saúde bucal | `TipoUnidadeDeSaude.UBS` | Implementado |
| UBS Fluvial | Porta de entrada / APS (ribeirinha) | Igual à UBS, adaptada à logística fluvial | População ribeirinha | Igual à UBS | Igual à UBS | RH, Agenda, logística fluvial | Igual à UBS | `TipoUnidadeDeSaude.UBS` (diferença é logística/território, não clínica — modalidade `FLUVIAL`, não tipo novo) | Base existente |
| Consultório na Rua | APS itinerante | Levar cuidado à população em situação de rua | Pessoas em situação de rua | Clínico, enfermagem, saúde mental, redução de danos | Redução de danos, acolhimento | Rede de APS/eMulti | — | nenhuma (é um serviço/equipe de APS, não necessariamente uma unidade própria) | Não implementado |
| Academia da Saúde | Promoção da saúde | Atividade física, práticas corporais, educação em saúde | Comunidade | Atividade física, educação em saúde | Promoção, prevenção | APS | — | nenhuma | Não implementado |
| Policlínica / Centro de Especialidades / SADT | Atenção especializada ambulatorial | Consultas/exames além da capacidade da APS | Encaminhados pela APS | Cardiologia, ortopedia, neurologia, etc. | Consulta especializada | Regulação | — | `TipoUnidadeDeSaude.POLICLINICA` / `CENTRO_ESPECIALIDADES` | Implementado |
| CEO — Centro de Especialidades Odontológicas | Atenção especializada em saúde bucal | Continuidade odontológica especializada | Encaminhados pela APS | Cirurgia oral, periodontia, endodontia | Saúde bucal especializada | UBS/Saúde Bucal | — | `TipoUnidadeDeSaude.CENTRO_ESPECIALIDADES` (especialidade odontológica, não distinta estruturalmente) | Base existente |
| CAPS (I/II/III/i/AD/AD III) | Atenção psicossocial | Acolhimento, PTS, atendimento de crise, reinserção social | Usuários RAPS | Acolhimento, grupos, terapias | Saúde mental | Rede | — | `TipoUnidadeDeSaude.CAPS` (modalidades I/II/III/i/AD não distinguidas ainda) | Base existente |
| UPA 24h | Urgência (rede pré-hospitalar fixa) | Estabilização, observação, encaminhamento | Demanda espontânea de urgência | Urgência, observação | Urgência, estabilização | Regulação | Laboratório, RX | `TipoUnidadeDeSaude.UPA` | Implementado |
| Pronto-Socorro / Porta hospitalar | Urgência/emergência hospitalar | Estabilização, diagnóstico, internação | Urgência/emergência | Urgência, emergência | Urgência, internação | Hospital | — | `TipoUnidadeDeSaude.HOSPITAL` (é um serviço dentro do hospital, não tipo próprio) | Base existente |
| Hospital Geral | Atenção hospitalar | Internação, cirurgia, cuidados de maior complexidade | Referenciados/urgência | Emergência, internação, UTI, centro cirúrgico, laboratório, farmácia | Leitos, cirurgia, UTI | Regulação | UTI, centro cirúrgico, farmácia, laboratório | `TipoUnidadeDeSaude.HOSPITAL` | Implementado |
| Hospital Especializado | Atenção hospitalar de alta especialidade | Igual ao Hospital Geral, focado em conjunto de condições (oncologia, cardiologia, etc.) | Referenciados de alta complexidade | Igual ao Hospital Geral + especialidade | Especialidade clínica | Regulação | Igual ao Hospital Geral | `TipoUnidadeDeSaude.HOSPITAL` + perfil/especialidade ainda não modelado | Não modelado |
| Maternidade / Centro de Parto Normal | Atenção obstétrica e neonatal | Pré-parto, parto, pós-parto, cuidado ao RN | Gestantes/puérperas | Obstetrícia, neonatologia | Parto, cuidado neonatal | Regulação | — | `TipoUnidadeDeSaude.HOSPITAL` ou estrutura especializada — não decidido | Não modelado |
| Serviço de Atenção Domiciliar (SAD) | Cuidado no domicílio | Tratamento, reabilitação, cuidados paliativos, desospitalização | Pacientes desospitalizados | EMAD, EMAP | Cuidado domiciliar | Hospital (origem) | — | nenhuma | Não implementado |
| Laboratório / Unidade de Apoio Diagnóstico | Apoio diagnóstico assistencial | Coleta, processamento, laudo | Encaminhados para exame | Coleta, análise, laudo | Diagnóstico laboratorial | Regulação | — | `TipoUnidadeDeSaude.LABORATORIO` | Implementado |
| Farmácia | Assistência farmacêutica | Dispensação, orientação, controle de medicamento | Pacientes com prescrição | Dispensação, acompanhamento farmacoterapêutico | Dispensação, estoque de medicamento | Prescrição (Consulta) | — | nenhuma — Farmácia não é (e não precisa ser) um `TipoUnidadeDeSaude`; já é o domínio #9 implementado como conjunto de entidades próprias (`Medicamento`/`Lote`/`Dispensacao`, ADR-0049/0050/0051) | Não implementado *como tipo de unidade* — mas domínio já implementado (ver nota) |
| Unidade de Reabilitação | Reabilitação funcional | Fisioterapia, TO, fonoaudiologia | Pessoas com necessidade de reabilitação | Fisioterapia, TO, fonoaudiologia | Reabilitação física/neurológica/auditiva/visual | Regulação | — | `TipoUnidadeDeSaude.CENTRO_REABILITACAO` | Implementado |
| Centro de Imunização | Vacinação especializada | Vacinação, conservação de imunobiológicos, cadeia de frio, campanhas | População em geral / vacinação especial | Vacinação | Vacinação, cadeia de frio | UBS (pode existir dentro dela) ou estrutura própria | — | nenhuma — o documento-fonte destaca que "serviço não precisa necessariamente ser uma UnidadeDeSaude independente" | Não implementado |
| Oficina Ortopédica | Órteses/próteses | Confecção, adaptação, manutenção de dispositivos | Pessoas com deficiência | Confecção, adaptação, manutenção | Órtese/prótese | Reabilitação | — | nenhuma | Não implementado |

### 3.2 Natureza: Serviço/estrutura de apoio à rede

| Equipamento | Papel na rede | Função | Público | Serviços | Capacidades | Depende de | Pode conter | Correspondência atual | Situação |
|---|---|---|---|---|---|---|---|---|---|
| Central de Regulação (do Acesso / Médica das Urgências) | Coordenação do acesso | Gerenciar fila/prioridade/vaga conforme critério clínico | Rede inteira (não um público direto) | Regulação de acesso, regulação de urgência | Coordenação de fila/vaga | Toda a rede assistencial | — | nenhuma — domínio #11 Regulação (`DER.md`) já esboça `SolicitacaoRegulacao`/`Fila`/`Vaga`, mas a "Central" como estrutura organizacional não está modelada | Não implementado |
| LACEN / Laboratório de Saúde Pública | Vigilância laboratorial | Exames de maior complexidade, investigação de surto, apoio epidemiológico | Rede/SUS (não paciente direto) | Análises de vigilância, controle de qualidade | Vigilância laboratorial (distinta de diagnóstico assistencial) | Vigilância epidemiológica | — | `TipoUnidadeDeSaude.LABORATORIO` cobre laboratório **assistencial** — LACEN é função distinta, não modelada | Não modelado |
| Hemoterapia / Hemocentro | Suporte hematológico à rede | Coleta, processamento, armazenamento, distribuição de sangue | Doadores e rede hospitalar | Coleta, transfusão, testes | Hemoterapia | Rede hospitalar | — | nenhuma | Não implementado |
| Telessaúde | Apoio técnico remoto | Teleconsultoria, segunda opinião, educação permanente | Profissionais da rede (não paciente direto) | Teleconsultoria, teleatendimento | Apoio diagnóstico remoto | UBS/rede | — | nenhuma | Não implementado |
| CEREST | Referência técnica em saúde do trabalhador | Assistência especializada + vigilância + articulação de política | Trabalhadores | Assistência, vigilância, educação permanente | Saúde do trabalhador | Vigilância em saúde | — | nenhuma | Não implementado |
| Central de Abastecimento | Logística de insumos | Receber, armazenar, distribuir insumos às unidades | Rede inteira | Armazenagem, distribuição | Logística | Estoque/Compras | — | nenhuma como `TipoUnidadeDeSaude` — se relaciona com o domínio #13 Estoque (`DER.md`) | Não implementado |
| Central de Transplantes | Coordenação de transplantes | Gerenciar doadores, lista de espera, compatibilidade, captação | Pacientes em lista de espera | Coordenação de captação/distribuição | Coordenação de transplantes | Rede hospitalar | — | nenhuma | Não implementado |

### 3.3 Natureza: Estrutura territorial/especializada

| Equipamento | Papel na rede | Função | Público | Serviços | Capacidades | Depende de | Pode conter | Correspondência atual | Situação |
|---|---|---|---|---|---|---|---|---|---|
| DSEI | Coordenação distrital de saúde indígena | Coordena Polo Base/UBSI/CASAI de um distrito | Povos indígenas de um território | Coordenação distrital | Coordenação territorial indígena | Rede de saúde indígena | Polo Base, UBSI, CASAI | nenhuma | Não implementado |
| Polo Base | Apoio/coordenação de equipes indígenas | Coordenação e, conforme o tipo, ações assistenciais | Povos indígenas | Coordenação de equipe | Coordenação de equipe indígena | DSEI | UBSI | nenhuma | Não implementado |
| UBSI | Atendimento indígena no território | Atenção primária no território indígena | Povos indígenas | Igual à UBS, adaptado ao território | APS indígena | Polo Base | — | nenhuma — funcionalmente próxima de UBS, mas o documento-fonte explicitamente não recomenda encaixar em `UBS` sem mais definição | Não implementado |
| CASAI | Acolhimento de indígena referenciado | Apoio/acolhimento fora do território | Povos indígenas referenciados | Acolhimento, hospedagem de apoio | Acolhimento | DSEI | — | nenhuma | Não implementado |
| Unidade de Vigilância de Zoonoses (UVZ) | Vigilância de zoonoses | Investigação, controle de vetores, monitoramento animal | População geral (indireto) | Vigilância, controle de vetores | Vigilância de zoonoses | Vigilância em saúde | — | nenhuma — explicitamente não é clínica veterinária | Não implementado |
| Serviço de Verificação de Óbito (SVO) | Vigilância de mortalidade | Investigar mortes naturais sem causa esclarecida | População geral (indireto) | Verificação de óbito | Investigação de óbito | Vigilância epidemiológica | — | nenhuma | Não implementado |

### 3.4 Natureza: Recurso/serviço móvel

| Equipamento | Papel na rede | Função | Público | Serviços | Capacidades | Depende de | Pode conter | Correspondência atual | Situação |
|---|---|---|---|---|---|---|---|---|---|
| SAMU 192 | Atendimento pré-hospitalar móvel | Regulação médica, despacho, atendimento no local, transporte | População (via 192) | Atendimento móvel de urgência | APH (atenção pré-hospitalar) | Central de Regulação | — | nenhuma — SAMU é serviço/rede, não deve virar `UnidadeDeSaude` (ver §4) | Não implementado |
| Ambulância (USB/USA/Motolância/Ambulancha/Aeromédico) | Recurso de transporte de urgência | Transporte assistido | Paciente em atendimento móvel | Transporte assistido | Transporte de urgência | SAMU | — | nenhuma — recurso móvel, não `UnidadeDeSaude` (ver §4) | Não implementado |
| Unidade móvel (terrestre/fluvial/pré-hospitalar) | Extensão móvel de atendimento | Atendimento onde não há estrutura fixa | População de território de difícil acesso | Varia conforme modalidade | Modalidade `MOVEL` | Unidade de origem | — | nenhuma — modalidade de uma unidade existente, não tipo novo (ver §4) | Não implementado |

## 4. Modelo conceitual de composição

O documento-fonte e a investigação do código convergem no mesmo modelo, já usado no domínio
Administrativo (`Setor`/`Perfil`/`CapacidadeAdministrativa`, ADR-0030/0031/0032/0037) e compatível
com a organização de profissionais do módulo RH:

```text
Equipamento de saúde
        ↓ pode possuir
Serviços
        ↓ que possuem
Capacidades
        ↓ executadas por
Profissionais
        ↓ organizados em
Setores
```

Isso é **modelo conceitual**, não uma relação JPA a implementar diretamente — cada seta vira
relacionamento real só quando o domínio correspondente for desenhado (ADR-0053).

Exemplo (Hospital) — mostra por que isso evita uma subclasse por especialidade de hospital:

```text
HOSPITAL
├── Serviço: Pronto-Socorro   → capacidades: urgência, estabilização
├── Serviço: UTI              → capacidades: terapia intensiva, ventilação mecânica
├── Serviço: Centro Cirúrgico → capacidade: cirurgia
└── Serviço: Laboratório      → capacidade: diagnóstico laboratorial
```

Um "Hospital Especializado" (oncológico, cardiológico, etc.) não é uma classe nova nesse modelo — é
o mesmo `HOSPITAL` com um subconjunto diferente de Serviços/Capacidades habilitadas. O mesmo raciocínio
vale para Maternidade/CPN (subconjunto obstétrico/neonatal de serviços de um `HOSPITAL`, ou
estrutura própria — decisão adiada, ver linha correspondente na tabela).

Este modelo conecta diretamente com:
- **Administrativo** (`Setor`/`PerfilAdministrativo`/`CapacidadeAdministrativa`, ADR-0030/0031/0032):
  mesmo padrão de catálogo-como-dado, mesma disciplina de não condicional por tipo no núcleo
  (ADR-0037, generalizada pela ADR-0053).
- **RH**: os "Profissionais" do modelo acima são a mesma entidade `Profissional` já implementada, com
  sua própria organização por cargo/lotação — este mapa não propõe nenhuma duplicação, apenas
  reconhece que Serviço/Capacidade de um equipamento é executado por profissionais que o RH já
  modela.

## 5. Referências

- [`docs/pm/sistema_de_saude_brasileiro.md`](./pm/sistema_de_saude_brasileiro.md) — documento-fonte.
- [ADR-0053](./adr/0053-criterio-de-governanca-para-equipamentos-de-saude.md) — critério de
  governança sobre quando um item desta tabela vira enum, capacidade ou especialização.
- [`MAPA-DE-DOMINIOS.md`](./MAPA-DE-DOMINIOS.md) — mapa dos 20 domínios de negócio da plataforma;
  vários equipamentos desta tabela (Central de Regulação → domínio #11, Central de Abastecimento →
  domínio #13, SAMU/Ambulância → domínio #16 Transporte Sanitário) já têm uma responsabilidade de
  domínio esboçada lá, mesmo sem a estrutura organizacional (a "central", o "equipamento") ainda
  modelada.
- [`adr/DER.md`](./adr/DER.md) §Apêndice — esboços de campos/fluxos por domínio, incluindo os
  equipamentos novos identificados por este mapa que ainda não tinham nenhum registro.
- [`adr/DER-atual.md`](./adr/DER-atual.md) — schema real de `UnidadeDeSaude`/`TipoUnidadeDeSaude`
  hoje implementado.
