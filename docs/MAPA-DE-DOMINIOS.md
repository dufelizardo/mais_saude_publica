# Mapa de Domínios do Mais Saúde Pública

**Data:** 2026-09-24 (criado)
**Status:** Vivo. Documento guarda-chuva de toda a plataforma — decisões formalizadas na
[ADR-0039](./adr/0039-mapa-de-dominios-e-prioridades-de-arquitetura.md). Não substitui os
documentos de escopo por módulo ([`ESCOPO-RH.md`](./rh/ESCOPO-RH.md),
[`ESCOPO-ADMINISTRATIVO.md`](./administrativo/ESCOPO-ADMINISTRATIVO.md)) — é o nível acima deles,
onde cada módulo é posicionado dentro da plataforma inteira.

## 1. Contexto e como ler este documento

Este documento nasceu de uma visão de 20 domínios trazida pelo usuário para o "Mais Saúde Pública"
como plataforma completa de gestão de uma rede pública de saúde — não apenas cadastro de unidades.
Recebe o **mesmo tratamento** que todo material externo já trazido para este projeto (a
"Especificação Preliminar do Setor Administrativo Adaptativo" que originou o módulo Administrativo,
o material que originou o módulo de RH): serve como **checklist de vocabulário e de estrutura**,
não como especificação literal a adotar em bloco.

Confrontar essa visão com o código real e com a documentação já existente revelou 3 pontos que
precisaram de reconciliação — detalhados na ADR-0039:

1. Já existe uma proposta de modelo de dados clínico ([`adr/DER.md`](./adr/DER.md), de
   2026-09-06, anterior a RH e Administrativo) que conflitava com o que foi implementado depois.
   Foi revisada, não descartada.
2. As ADRs 0003–0008 (Clean Architecture, JWT, Docker/CI, Angular) formam um "roadmap de
   arquitetura futura" que a própria ADR-0003 dizia vir antes de qualquer novo domínio de negócio —
   mas RH e Administrativo inteiros foram construídos ignorando essa ordem. A ADR-0039 reafirma a
   convenção flat já praticada e mantém a segurança adiada, por decisão explícita do usuário.
3. Nenhum domínio de Assistência existe em nenhuma forma no código hoje — é uma folha em branco.

O projeto segue avançando por **incrementos pequenos e discutidos** (mesma prática de RH e
Administrativo) — este mapa existe para que a próxima década de decisões tenha um lugar único para
ver "onde isso se encaixa", sem forçar a modelagem detalhada de tudo de uma vez.

## 2. O mapa em 4 grupos

```text
                    MAIS SAÚDE PÚBLICA
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   ORGANIZAÇÃO         GESTÃO             ASSISTÊNCIA
        │                  │                  │
        │                  ├── RH             ├── Paciente
        │                  ├── Administrativo ├── Atendimento
        │                  ├── Financeiro     ├── Agendamento
        │                  ├── Compras        ├── Prontuário (agregação)
        │                  ├── Estoque        ├── Enfermagem
        │                  ├── Patrimônio     ├── Farmácia
        │                  └── Qualidade      ├── Laboratório
        │                                     ├── Regulação
        │                                     ├── Gestão de Leitos
        │                                     └── Transporte Sanitário
        │
        └────────────────── TRANSVERSAL ──────────────────
                           │
                  ├── Identidade / Segurança
                  ├── Auditoria
                  ├── Integrações
                  ├── Indicadores / BI
                  └── Documentos
```

Os **20 domínios** do usuário estão todos numerados abaixo (nenhum foi descartado), organizados
dentro desses 4 grupos e atribuídos a uma onda de implementação (seção 4).

## 3. Os 20 domínios

### Organização

| # | Domínio | Responsabilidade | Entidades candidatas | Hoje |
|---|---|---|---|---|
| 1 | **Organização e Estrutura da Rede** | Onde a saúde pública acontece e como as unidades se organizam hierarquicamente. | `UnidadeDeSaude`, `TipoUnidadeDeSaude`, `Setor` | ✅ Implementado — hierarquia autorreferenciada de 5 níveis (ADR-0002/0009/0013) + `Setor` do Administrativo (ADR-0030) |

### Gestão

| # | Domínio | Responsabilidade | Entidades candidatas | Hoje |
|---|---|---|---|---|
| 2 | **Recursos Humanos** | Dono das informações funcionais de quem trabalha na rede — cargos, vínculos, lotação, remuneração, histórico funcional. | `Profissional`, `Cargo`, `Lotacao`, `FolhaPagamento`, ... (26 entidades) | ✅ Implementado — ver [`MODELO-RH.md`](./rh/MODELO-RH.md) |
| 3 | **Administração** | Operação estrutural da unidade — processos, patrimônio embrionário, necessidades de pessoal encaminhadas ao RH. | `Setor`, `CapacidadeAdministrativa`, `PerfilAdministrativo`, `ProcessoAdministrativo`, `ResponsabilidadeAdministrativa`, `NecessidadeDePessoal` | ✅ Implementado — ver [`ESCOPO-ADMINISTRATIVO.md`](./administrativo/ESCOPO-ADMINISTRATIVO.md) |
| 13 | **Estoque e Almoxarifado** | Materiais e insumos gerais (luvas, seringas, EPI administrativo) — diferente de Farmácia (medicamento tem regra própria). | `Produto`, `Lote`, `Estoque`, `MovimentacaoEstoque`, `Inventario`, `Almoxarifado` | ⏳ Não iniciado — catálogo `CapacidadeAdministrativa` já reserva `ESTOQUE` como rótulo (ADR-0032), sem implementação |
| 14 | **Compras, Contratos e Fornecedores** | Adquire o que a rede precisa: solicitação → cotação → contrato → entrega. | `Fornecedor`, `SolicitacaoCompra`, `Contrato`, `ItemContratado`, `Entrega` | ⏳ Não iniciado — rótulo `COMPRAS`/`CONTRATOS` reservado no catálogo (ADR-0032/0037) |
| 15 | **Patrimônio e Manutenção** | Bens físicos da rede (equipamentos, veículos, mobiliário) — tombamento, localização, manutenção. | `Equipamento`, `Manutencao` | ⏳ Não iniciado — rótulo `PATRIMONIO` reservado no catálogo (ADR-0032) |
| 17 | **Financeiro** | Dimensão financeira da operação — orçamento, empenho, despesa, centro de custo. | `Orcamento`, `Empenho`, `Despesa`, `CentroDeCusto` | ⏳ Não iniciado. Decisão a tomar quando esta onda chegar: sistema financeiro completo vs. só registro/acompanhamento integrado a um ERP externo — tendência inicial pela segunda opção |
| 18 (parte) | **Qualidade** | Indicadores de qualidade **da prestação do serviço de saúde**, não conformidades, planos de ação. | `Indicador`, `NaoConformidade`, `PlanoDeAcao` | ⏳ Não iniciado |

### Assistência

| # | Domínio | Responsabilidade | Entidades candidatas | Hoje |
|---|---|---|---|---|
| 5 | **Paciente / Cidadão** | Identidade da pessoa que usa a rede pública de saúde. | `Paciente` (identificação, contatos, endereço, CNS) | ✅ Implementado (Fase 1 da onda) — ver [ADR-0040](./adr/0040-paciente-primeira-entidade-da-assistencia.md) |
| 4 | **Atendimento** | Momento em que o cidadão entra na rede para receber um serviço. | `Atendimento`, `TipoAtendimento`, `Acolhimento` | ✅ Implementado (Fase 2 da onda) — ver [ADR-0041](./adr/0041-atendimento-registra-entrada-do-paciente-na-rede.md) |
| 7 | **Agendamento e Agenda** | Quando e onde os serviços serão realizados — agendas, horários, fila de espera. | `Agendamento`, `Horario` | ✅ `Agendamento` implementado (Fase 3 da onda) — ver [ADR-0042](./adr/0042-agendamento-independente-do-atendimento.md). `Horario` (agenda do profissional) ainda não desenhado |
| 6 | **Prontuário / Histórico Clínico** | Histórico clínico do paciente na rede. | *(nenhuma — agregação de leitura)* | ✅ Implementado — `GET /api/v1/prontuario/{pacienteId}` agrega Atendimento/Consulta/Procedimento sob demanda (ADR-0039 decisão 6, ADR-0045); não é uma tabela nova |
| 8 | **Enfermagem** | Triagem, sinais vitais, classificação de risco, cuidados — processos próprios da enfermagem. | `Triagem`, `EvolucaoEnfermagem` | ✅ `Triagem` (ver [ADR-0047](./adr/0047-triagem-primeira-entidade-da-enfermagem.md)) e `EvolucaoEnfermagem` (ver [ADR-0048](./adr/0048-evolucao-de-enfermagem-segunda-entidade-da-enfermagem.md)) implementadas. `AdministracaoDeMedicamento` (depende de Farmácia), `Cuidado`, `Escala` ainda não iniciados |
| 9 | **Farmácia** | Medicamentos e sua dispensação — regras próprias, diferente de Estoque. | `Medicamento`, `Lote`, `Dispensacao` | ⏳ Não iniciado — "só quando houver requisito real" |
| 10 | **Laboratório e Diagnóstico** | Exames laboratoriais e outros serviços diagnósticos. | `SolicitacaoExame`, `Amostra`, `Resultado`, `Laudo` | ⏳ Não iniciado — "só quando houver requisito real" |
| 11 | **Regulação** | Coordena o acesso do cidadão a serviços que não estão na unidade de origem — filas, prioridades, referência/contrarreferência. | `SolicitacaoRegulacao`, `Fila`, `Encaminhamento` | ⏳ Não iniciado — conecta a rede inteira, não uma unidade só |
| 12 | **Gestão de Leitos e Internação** | Leitos, ocupação, internação, alta — relevante principalmente para hospitais/UPA. | `Leito`, `Internacao` | ⏳ Não iniciado — rótulo `GESTAO_DE_LEITOS` já reservado no catálogo administrativo (ADR-0032/0037) |
| 16 | **Transporte Sanitário** | Deslocamentos relacionados à saúde — ambulâncias, transferências entre unidades. | `Veiculo`, `SolicitacaoTransporte` | ⏳ Não iniciado — integra com Patrimônio (veículo), RH (motorista) e Regulação (necessidade) |

### Transversal

| # | Domínio | Responsabilidade | Entidades candidatas | Hoje |
|---|---|---|---|---|
| 20 (parte) | **Identidade e Segurança** | Autenticação, perfis, permissões por contexto organizacional. | `Usuario`, `Perfil`, `Permissao` | ⏳ Adiado por decisão explícita (ADR-0006, reafirmada na ADR-0039) — revisitar antes de produção com dado real de paciente |
| 18 (parte) | **Auditoria** | Rastreabilidade: quem alterou o quê, quando — fundamental para sistema público e LGPD. | `EventoDeAuditoria` | ⏳ Não iniciado — proposta original em `adr/DER.md` (entidade `AUDITORIA`), mantida como referência |
| 20 (parte) | **Integrações** | Adapters para sistemas externos (SUS, CNES, DATASUS, e-SUS, SIGTAP). | *(camada de integração, não entidade de domínio)* | ⏳ Não iniciado — arquitetura de adapters antes de implementar qualquer integração específica |
| 19 | **Indicadores, BI e Gestão** | Transforma fatos operacionais (já registrados pelos domínios acima) em informação de gestão — nunca o inverso. | `Indicador`, `Dashboard` (camada de leitura, não fonte de dado) | ⏳ Não iniciado — depende dos domínios operacionais existirem primeiro para ter dado confiável para agregar |
| — | **Documentos** | Anexos/documentos formais (contrato, prontuário, processo, compra) — reaproveitável por qualquer domínio, em vez de cada um ter seu próprio conceito de anexo. Citado na visão original do usuário, sem número na lista final de 20 — mantido aqui para não se perder. | `Documento`, `Versao`, `Tipo`, `Aprovacao`, `Assinatura` | ⏳ Não iniciado |

## 4. Ondas de implementação

| Onda | Domínios | Status |
|---|---|---|
| 🟢 **Fundação** | #1 Organização, #2 RH, #3 Administração | ✅ Completo (backend + frontend) |
| 🔵 **Operação Assistencial** | #5 Paciente → #4 Atendimento → #7 Agendamento → #6 Prontuário (agregação), depois #8 Enfermagem, #9 Farmácia, #10 Laboratório, #11 Regulação, #12 Leitos, #16 Transporte, conforme requisito real | 🔵 Em andamento — #5 Paciente (ADR-0040), #4 Atendimento (ADR-0041), #7 Agendamento (ADR-0042), #6 Consulta/Procedimento/Prontuário (ADR-0043/0044/0045) implementados; #8 Enfermagem iniciado (`Triagem`/ADR-0047, `EvolucaoEnfermagem`/ADR-0048). Próximo passo (não iniciado): #9 Farmácia, #10 Laboratório, #11 Regulação, #12 Leitos, #16 Transporte, conforme requisito real |
| 🟣 **Gestão e Inteligência** | #13 Estoque, #14 Compras, #15 Patrimônio, #17 Financeiro, #18 Qualidade, #19 Indicadores/BI | ⏳ Depois — mesma disciplina do "Fase 7+" do Administrativo (ADR-0037): só quando houver requisito real |
| — (distribuído por dependência) | #20 Identidade/Segurança/Integrações, Auditoria | ⏳ Adiado — Segurança revisitada antes de produção real; Auditoria/Integrações quando houver necessidade concreta |

### Por que essa ordem — é um ecossistema, não itens isolados

Mesmo raciocínio já registrado em `ESCOPO-RH.md` §4: os domínios se alimentam uns dos outros, não
são features independentes que dá para construir em qualquer ordem.

- **Paciente antes de Atendimento**: não dá para registrar um atendimento sem saber quem é o
  paciente — mesma lógica de `Lotacao` exigir um `Profissional` já existente.
- **Atendimento antes de Agendamento formal**: um atendimento pode nascer de um agendamento ou ser
  espontâneo (acolhimento) — por isso `Atendimento.agendamento` é uma referência **opcional**, não
  o inverso. Mesmo padrão já usado por `NecessidadeDePessoal.vagaAssociada` (ADR-0036): o elo mais
  "fraco" fica com a FK nullable.
- **Prontuário depois de Atendimento existir**: é uma agregação, não pode existir antes da fonte de
  dado que agrega — mesma ordem que `ADR-0026` (histórico funcional) seguiu dentro do RH.
- **Farmácia/Laboratório/Regulação/Leitos/Transporte ficam para quando houver requisito real**:
  cada um tem regras e stakeholders próprios que merecem sua própria conversa — mesma disciplina já
  aplicada ao "Fase 7+" do Administrativo (ADR-0037), evitando desenhar 6 domínios em paralelo sem
  ninguém usando nenhum deles ainda.
- **Gestão e Inteligência por último**: Financeiro/Compras/Patrimônio/Estoque/Qualidade/
  Indicadores fazem mais sentido quando já existe operação real (RH+Administrativo+Assistência)
  gerando os fatos que eles vão gerenciar/agregar — Indicadores em particular **não pode** ser a
  fonte de dado, só o consumidor (ver ADR-0039).

## 5. Rastreamento

Convenção já usada por RH e Administrativo: um épico Jira por módulo (projeto AQUAQE), uma
História por fase/ADR. Nenhum épico da Onda Assistencial foi aberto ainda — abrir quando a
implementação de fato começar (mesmo padrão do épico AQUAQE-274 do RH e AQUAQE-318 do
Administrativo).

## 6. Referências

- [ADR-0039](./adr/0039-mapa-de-dominios-e-prioridades-de-arquitetura.md) — decisões formais deste
  mapa (segurança adiada, Clean Architecture não adotada, `Pessoa` compartilhada rejeitada,
  Prontuário como agregação).
- [`adr/DER.md`](./adr/DER.md) — modelo de dados revisado para a próxima onda (Paciente,
  Atendimento, Agendamento, Consulta, Procedimento), e um apêndice com esboço de campos/fluxos
  para todos os demais domínios listados na seção 3 — nenhuma informação do material original do
  usuário foi descartada, mesmo a que ainda não tem onda de implementação definida.
- [`adr/DER-atual.md`](./adr/DER-atual.md) — schema real implementado hoje (hierarquia
  `UnidadeDeSaude`).
- [`rh/ESCOPO-RH.md`](./rh/ESCOPO-RH.md) e [`rh/MODELO-RH.md`](./rh/MODELO-RH.md) — módulo RH
  completo, precedente direto do raciocínio de fases/dependências usado aqui.
- [`administrativo/ESCOPO-ADMINISTRATIVO.md`](./administrativo/ESCOPO-ADMINISTRATIVO.md) — módulo
  Administrativo completo, precedente direto do formato deste documento.
- ADRs 0003–0008 — roadmap de arquitetura futura, prioridade revisitada pela ADR-0039.
