# Modelo do domínio Gestão de Emergências e Desastres

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0113](../adr/0113-emergencias-e-desastres.md). **Nada implementado ainda.** A
implementação vem nas fatias E1 a E6 (seção 17), cada uma com ADR própria.

O domínio é uma **camada de coordenação extraordinária**, ativada quando um evento ultrapassa a operação normal da
rede. Não substitui Vigilância, Regulação, Estoque, Transporte nem a capacidade da rede.

> **A Vigilância pergunta "o que está acontecendo?". Emergências pergunta "como a rede vai responder?".**

## 1. Objetivo e ciclo

O domínio deve permitir:
- reconhecer, registrar e avaliar o impacto de uma emergência;
- ativar planos de contingência;
- coordenar unidades e equipes e mobilizar profissionais, insumos, medicamentos e equipamentos;
- reorganizar a capacidade assistencial e coordenar o transporte;
- acompanhar pacientes deslocados;
- registrar decisões e ações;
- devolver a rede à operação normal.

```text
OPERAÇÃO NORMAL → EVENTO EXTRAORDINÁRIO → AVALIAÇÃO DE IMPACTO → ATIVAÇÃO → PLANO DE CONTINGÊNCIA
   → MOBILIZAÇÃO → EXECUÇÃO E MONITORAMENTO → DESMOBILIZAÇÃO → AVALIAÇÃO PÓS-EVENTO → NORMALIDADE
```

## 2. O princípio central: coordenação, não posse

**Emergências não é dono dos recursos da rede. É dono da coordenação extraordinária deles durante uma emergência.**

| Domínio | É dono de | Emergências registra |
|---|---|---|
| RH (2) | profissionais, lotação, escala | profissional X mobilizado para a unidade Y durante a emergência Z |
| Farmácia (9) e Estoque (13) | estoques | reserva, consumo extraordinário, redistribuição |
| Transporte (16) | veículos, ambulâncias | necessidade e mobilização de veículos |
| Regulação (11) | processos regulatórios | redirecionamentos e prioridades decididos |
| Organização (1) e Leitos (12) | unidades, capacidade, leitos | capacidade emergencial e unidades afetadas |
| Vigilância (23) | investigação, surto | o evento que originou a emergência |
| Território (21) | território, população | a área afetada |
| **Emergências (27)** | **a coordenação da resposta extraordinária** | — |

A "Gestão da Rede" citada na análise não é um domínio do MAPA: é a Organização (1) com Leitos (12) e a capacidade das
unidades.

## 3. Evento, emergência e plano

São três coisas diferentes:

```text
Evento ──pode gerar──► Emergência ──pode ativar──► Plano de contingência
```

- **`EventoEmergencial`:** o fenômeno.
  - Exemplos: surto, epidemia, enchente, deslizamento, incêndio, acidente coletivo, evento de massa, desastre
    ambiental, interrupção de serviço essencial.
  - Tem tipo, data e hora, local, origem, descrição e fonte (Vigilância, unidade, Defesa Civil, gestor, profissional,
    sistema externo).
  - **Nem todo evento vira emergência.**
- **`Emergencia`:** a situação que exige resposta coordenada e extraordinária. É a entidade central.

  ```text
  Emergencia
  ├── identificação, descrição
  ├── tipo, origem (o evento)
  ├── nível, situação
  ├── início, encerramento
  ├── território afetado
  ├── responsável
  ├── plano ativado
  └── avaliações de impacto
  ```

  Exemplo: "Enchente Região Norte", tipo desastre hidrológico, nível 2, ativa desde 05/10/2026 às 14:30.
- **`PlanoDeContingencia`:** a preparação, definida **antes** de a emergência acontecer (seção 5).

## 4. Tipos, níveis e referências legais

- **Tipo de emergência, cadastrável:**
  - para desastres, guarda o **código COBRADE** (Classificação e Codificação Brasileira de Desastres);
  - para a saúde: epidemia, evento de massa, desabastecimento, interrupção de unidade.
- **Nível de resposta, configurável:** por exemplo, de 0 a 3, como no plano de resposta às emergências em saúde
  pública do Ministério da Saúde.
- **Referências registradas:** a **ESPIN** (Emergência em Saúde Pública de Importância Nacional, Decreto 7.616/2011) e
  o **decreto de situação de emergência ou calamidade pública** (Política Nacional de Proteção e Defesa Civil, Lei
  12.608/2012) entram com número e data. **O sistema não declara**: declarar é ato do poder público e da Defesa Civil.

## 5. Plano de contingência

Define **o que fazer** antes da emergência. **É configurável:** não existem `PlanoEnchente`, `PlanoEpidemia` nem
`PlanoEventoMassa`.

```text
PlanoDeContingencia
├── nome, tipo de emergência, objetivo
├── critérios de ativação, níveis
├── responsabilidades
├── ações previstas (por nível)
├── recursos necessários
├── unidades envolvidas
├── contatos
├── vigência
└── versão e situação
```

Exemplos: epidemias, enchentes, eventos de massa, desabastecimento, interrupção de unidade.

**É versionado:** a revisão gera uma versão nova, por exemplo depois do relatório pós-evento (seção 12).

## 6. Ativação com histórico

O plano não é simplesmente "ligado". A ativação é uma operação explícita, e cada mudança gera um registro novo:

```text
AtivacaoPlano: plano, emergência, nível, autoridade, justificativa, data

Ativado no nível 1 → elevado ao nível 2 → elevado ao nível 3 → reduzido ao nível 1 → desmobilizado
```

Cada passo é auditado (ADR-0070).

## 7. Avaliação de impacto

A emergência afeta várias dimensões. `AvaliacaoImpacto` é **datada e repetível**, para acompanhar a evolução:

```text
AvaliacaoImpacto
├── população afetada
├── unidades afetadas
├── profissionais indisponíveis
├── leitos indisponíveis
├── medicamentos críticos
├── equipamentos indisponíveis
├── transporte impactado
├── infraestrutura impactada
└── nível de impacto
```

Exemplo da enchente: 18.000 pessoas afetadas, 3 UBS e 1 hospital atingidos, 4 ambulâncias indisponíveis, estoque
baixo de um medicamento crítico, 32% a menos de leitos disponíveis.

**Os números vêm dos donos sempre que possível:**
- leitos, da Internação (ADR-0098);
- situação das unidades, da Organização (ADR-0101);
- estoque, da Farmácia;
- população, do Território (ADR-0108).

O que não existir no sistema é informado à mão.

## 8. Capacidade emergencial

A rede tem uma capacidade normal e uma capacidade ativável em emergência:

```text
UBS X       capacidade normal 100 atendimentos/dia   →  emergencial 160/dia
Hospital X  leitos normais 120 · disponíveis 90      →  emergenciais ativáveis 30
```

```text
CapacidadeEmergencial
├── unidade
├── tipo de recurso (atendimentos, leitos, consultórios...)
├── normal, emergencial, disponível, reservada
└── período
```

Os **leitos emergenciais ativáveis** usam o `Leito` que já existe, marcado como extra, e não um cadastro paralelo.
Conversa com Organização, Leitos, RH, Estoque e Transporte.

## 9. Mobilização: Emergências registra, o dono executa

```text
Mobilizacao
├── emergência
├── tipo (pessoa, medicamento, insumo, equipamento, veículo, leito, espaço físico)
├── origem e destino
├── recurso e quantidade
├── responsável
├── início e fim
└── situação
```

| Recurso | Quem executa | Como |
|---|---|---|
| Profissional (médico, enfermeiro, técnico, ACS, motorista, administrativo) | Escala (ADR-0105) | a mobilização **autoriza turnos fora da lotação** na unidade de destino enquanto a emergência estiver ativa: uma exceção controlada à regra da Escala, sem lotação falsa; os alertas de jornada e de descanso continuam valendo |
| Medicamento | Farmácia (ADR-0059) | vira transferência entre unidades, com recebimento |
| Insumos, EPI, kits | Estoque (13), quando existir | até lá, a mobilização registra a necessidade |
| Veículos, ambulâncias | Transporte (16), quando existir | até lá, registra a necessidade |
| Leito extra | Leitos (ADR-0098) | ativação do leito marcado como extra |
| Unidade afetada | Organização (ADR-0101) | **situação operacional** (inoperante, em obra), que já fecha a agenda; sem estado novo |

## 10. Ações emergenciais

```text
AcaoEmergencial
├── emergência
├── tipo, descrição
├── responsável, unidade
├── prioridade, prazo
├── situação
└── início, conclusão
```

Exemplos:
- **[ALTA]** Transferir os pacientes da UBS X. Responsável: Organização e Regulação. Situação: em execução.
- **[URGENTE]** Enviar 500 kits de hidratação do almoxarifado central para a UBS X.

As ações podem nascer das **ações previstas no plano ativado** para aquele nível. O quadro de ações é o painel da
coordenação.

## 11. Pessoas vulneráveis e deslocadas

- **Vulneráveis na área afetada:** a lista sai do Território (adscrição, ADR-0108) e de Programas (acompanhamento,
  ADR-0109), por exemplo acamados, dependentes de oxigênio, pessoas em diálise e gestantes de alto risco.
- **Pacientes deslocados:** registram o paciente, a origem, o destino (unidade, abrigo, domicílio de parente) e a
  situação.
- **Abrigo não é domínio:** é um local de destino.

## 12. Desmobilização e relatório pós-evento

Não basta ativar: é preciso controlar o retorno.

```text
ATIVAÇÃO → RESPOSTA → CONTROLE → DESMOBILIZAÇÃO → NORMALIZAÇÃO
```

- **`Desmobilizacao`:** data, recursos liberados, profissionais liberados, unidades normalizadas e responsável. **Encerra
  as exceções de escala** abertas pela mobilização.
- **`RelatorioPosEvento`:** resumo, impacto, ações realizadas, recursos utilizados, problemas, indicadores, lições
  aprendidas e recomendações.
  - Preserva a memória do evento e ajuda a identificar gargalos, revisar capacidade e justificar investimentos.
  - **Alimenta a revisão do plano**, que ganha uma nova versão.

## 13. Território

Sem um modelo geográfico novo:
- **APS:** reaproveita território, área, microárea e unidade quando couber. Exemplo: enchente → 3 áreas → 12
  microáreas → 4.800 pessoas potencialmente afetadas.
- **Desastre:** a área afetada pode ser um **polígono arbitrário** em GeoJSON, como na ADR-0108, que não coincide com a
  microárea.

## 14. Vigilância e Emergências

A Vigilância (ADR-0110) já tem agravo, notificação, caso, investigação e surto. Emergências não duplica nada disso:

```text
Vigilância: surto identificado → Emergências: emergência ativada → plano → ampliação de atendimento
   → mobilização de profissionais → redistribuição de insumos → monitoramento
```

O surto pode ser a origem do evento emergencial.

## 15. Eventos de massa, COE e Defesa Civil

- **Eventos de massa** (carnaval, show, jogo, festival, manifestação, evento religioso) são um **tipo de emergência
  planejada**, com público estimado, local, período, postos de atendimento, ambulâncias e plano de contingência. Ficam
  para a fatia E6, não como domínio separado.
- **COE:** o domínio dá suporte ao Centro de Operações de Emergências, com quem coordena e as decisões e ações
  registradas. **Não é** um sistema de comando e controle militarizado.
- **Defesa Civil:** o Mais Saúde Pública não a substitui. É o sistema da **resposta em saúde**.

## 16. Permissões e entidades

| Permissão | Para quê |
|---|---|
| `EMERGENCIA.REGISTRAR` | registrar evento e avaliação de impacto |
| `EMERGENCIA.COORDENAR` | ações, mobilização, desmobilização |
| `EMERGENCIA.ATIVAR` | ativar o plano e mudar o nível (autoridade sanitária ou gestor) |
| `CONTINGENCIA.GERENCIAR` | cadastrar e revisar planos |

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `TipoEmergencia`, `NivelEmergencia` (catálogos) | nome, código COBRADE; nível, descrição | E1 |
| `EventoEmergencial` | tipo, data, local (GeoJSON), origem, fonte, descrição, surto (opcional) | E1 |
| `Emergencia` | evento, tipo, nível, situação, início, fim, território afetado, responsável, referências legais | E1 |
| `AvaliacaoImpacto` | emergência, data, dimensões de impacto, nível | E1 |
| `PlanoDeContingencia`, `AcaoPlano`, `CriterioAtivacao` | tipo, objetivo, níveis, responsabilidades, recursos, unidades, contatos, versão, vigência | E2 |
| `AtivacaoPlano` | plano, emergência, nível, autoridade, justificativa, data | E2 |
| `Mobilizacao` | emergência, tipo, origem, destino, recurso, quantidade, responsável, período, situação | E3 |
| `CapacidadeEmergencial` | unidade, tipo de recurso, normal, emergencial, disponível, reservada, período | E3 |
| `AcaoEmergencial` | emergência, tipo, descrição, responsável, unidade, prioridade, prazo, situação | E3 |
| `PacienteDeslocado` | emergência, paciente, origem, destino, situação | E4 |
| `Desmobilizacao` | emergência, data, recursos e profissionais liberados, unidades normalizadas, responsável | E5 |
| `RelatorioPosEvento` | emergência, resumo, impacto, ações, recursos, problemas, indicadores, lições, recomendações | E5 |

Organização lógica prevista:

```text
emergencias/
├── emergencia/      Emergencia, EventoEmergencial, TipoEmergencia, NivelEmergencia
├── contingencia/    PlanoDeContingencia, AcaoPlano, CriterioAtivacao, AtivacaoPlano
├── impacto/         AvaliacaoImpacto
├── capacidade/      CapacidadeEmergencial
├── mobilizacao/     Mobilizacao
├── acao/            AcaoEmergencial
├── pessoas/         PacienteDeslocado
└── pos-evento/      Desmobilizacao, RelatorioPosEvento
```

## 17. Fluxo e fatias

```text
EVENTO → DETECÇÃO E REGISTRO → AVALIAÇÃO PRELIMINAR ─┬─ NORMAL ─────────────────────────────┐
                                                     └─ EMERGÊNCIA → CLASSIFICAÇÃO → PLANO   │
                                                        → ATIVAÇÃO → (RH · Farmácia/Estoque · │
                                                        Transporte) → MOBILIZAÇÃO → AÇÕES     │
                                                        → MONITORAMENTO → DESMOBILIZAÇÃO      │
                                                        → RELATÓRIO PÓS-EVENTO → NORMALIZAÇÃO ┘
```

| Fatia | Entrega | Depende de |
|---|---|---|
| **E1** · núcleo | evento, emergência, tipo (COBRADE), nível, situação, avaliação de impacto, território afetado | Território (opcional) |
| **E2** · contingência | plano versionado, critérios, ações previstas, ativação com histórico | E1 |
| **E3** · mobilização | mobilização (com a exceção da Escala), capacidade emergencial, ações, unidade afetada pela situação operacional | E2 |
| **E4** · integração da rede | redistribuição pela Farmácia, Regulação, vulneráveis por Território e Programas, pacientes deslocados; Estoque e Transporte quando existirem | E3 |
| **E5** · pós-evento | desmobilização, relatório, lições, revisão do plano, indicadores | E3 |
| **E6** · especializados | eventos de massa, epidemias, desastres ambientais, hospital de campanha como capacidade | E5 |

**Fora do escopo agora:**
- sistema da Defesa Civil;
- meteorologia;
- gestão completa de desastres ambientais;
- abrigos como domínio;
- hospital de campanha completo;
- comando e controle militarizado;
- comunicação de risco à população;
- qualquer duplicação de Vigilância, Estoque, RH, Transporte ou Regulação.

## 18. Referências

- Decreto nº 7.616/2011: Emergência em Saúde Pública de Importância Nacional (ESPIN) e Força Nacional do SUS.
- Lei nº 12.608/2012: Política Nacional de Proteção e Defesa Civil (situação de emergência e calamidade pública).
- COBRADE: Classificação e Codificação Brasileira de Desastres.
