# Modelo da Qualidade do serviço de saúde

Desenho na [ADR-0202](../adr/0202-qualidade-do-servico.md). Parte Qualidade do item 18 do mapa (Qualidade / Segurança / Auditoria); a
Auditoria tem as ADRs 0070, 0071, 0082, 0096 e 0097.

## 1. Fatias

| Fatia | O quê | Estado |
|---|---|---|
| Q1 | Segurança do paciente: notificação de incidente, classificação ICPS, análise do NSP, Notivisa | ✅ [ADR-0203](../adr/0203-seguranca-do-paciente.md), V85 |
| Q2 | Não conformidades e planos de ação 5W2H com verificação de eficácia (PDCA) | ✅ [ADR-0204](../adr/0204-nao-conformidades-e-planos-de-acao.md), V86 |
| Q3 | Indicadores de qualidade com metas e auditoria de adesão aos protocolos | ✅ [ADR-0205](../adr/0205-indicadores-de-qualidade-e-auditoria-de-protocolos.md), V87 |

## 2. Segurança do paciente (Q1)

```text
Notificação (qualquer profissional, anônima ou não) → Classificação (natureza e dano, ICPS) → Análise (fatores, causa raiz)
  → Concluída     ·     evento adverso → Notivisa (72 h no óbito e no never event; 15º dia útil do mês seguinte nos demais)
```

| Natureza (ICPS) | Dano |
|---|---|
| Circunstância de risco, quase erro, incidente sem dano | nenhum |
| Evento adverso | leve, moderado, grave ou óbito |

- **Cultura não punitiva:** na anônima, quem notificou não é guardado; na identificada, só o núcleo vê o nome.
- **Causa raiz obrigatória:** dano grave, óbito e never event.
- **Núcleo de Segurança do Paciente:** papel novo, com `QUALIDADE.ANALISAR` na unidade.
- **Painel:** só totais, também para quem consulta indicadores.

## 3. Não conformidades e planos de ação (Q2)

```text
Não conformidade (origem) → análise de causa (6M) → plano 5W2H (ciclo 1) → ações concluídas → verificação de eficácia
  → eficaz: encerrada     ·     não eficaz: ciclo 2 (novo plano) → …
```

- **Origens:** incidente da Q1, irregularidade da VISA em estabelecimento ligado à unidade, auditoria interna, reclamação, outra; uma em
  aberto por vínculo.
- **O responsável atualiza a própria ação** mesmo sem `QUALIDADE.GERIR`; a evidência é texto.
- **Sugestões:** incidente com dano moderado ou maior, ou never event, e irregularidade da VISA na unidade, sem não conformidade.

## 4. Indicadores com metas e auditoria de protocolos (Q3)

```text
Registros da rede (internações, incidentes, atendimentos, agendamentos, auditorias) → indicador do mês → meta vigente da unidade
  → dentro · fora (sugere a não conformidade da Q2) · sem meta · sem base
```

| Indicador | Medida | Fonte |
|---|---|---|
| Taxa de quedas, lesão por pressão, eventos adversos graves | por mil pacientes-dia | incidentes (Q1) e internações |
| Reinternação em 30 dias | % das altas | internações, em qualquer unidade da rede |
| Tempo até o médico | mediana em minutos | chegada do atendimento e primeira consulta |
| Absenteísmo | % de faltas | agendamentos |
| Adesão ao protocolo | % conformes dos aplicáveis, por checklist | auditorias por observação |

- **Sem base, nunca zero;** o painel traz a série de 12 meses.
- **Meta por unidade, com histórico:** a nova encerra a anterior na véspera.
- **Checklists versionados**, semeados com a higiene das mãos e a cirurgia segura da OMS; a auditoria guarda a versão.
- **O fora da meta no mês fechado sugere** a não conformidade de origem indicador; uma em aberto por competência.
- **Relatório** "Qualidade do serviço" no catálogo do #19.

## 5. Fora do escopo

- Indicadores de infecção (IRAS), envio ao Notivisa e ao CNES (#20), meta por setor, anexo de evidência.
