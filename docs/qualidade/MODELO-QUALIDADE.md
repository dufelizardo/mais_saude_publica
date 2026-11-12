# Modelo da Qualidade do serviço de saúde

Desenho na [ADR-0202](../adr/0202-qualidade-do-servico.md). Parte Qualidade do item 18 do mapa (Qualidade / Segurança / Auditoria); a
Auditoria tem as ADRs 0070, 0071, 0082, 0096 e 0097.

## 1. Fatias

| Fatia | O quê | Estado |
|---|---|---|
| Q1 | Segurança do paciente: notificação de incidente, classificação ICPS, análise do NSP, Notivisa | ✅ [ADR-0203](../adr/0203-seguranca-do-paciente.md), V85 |
| Q2 | Não conformidades e planos de ação 5W2H com verificação de eficácia (PDCA) | ⬜ só com aprovação |
| Q3 | Indicadores de qualidade com metas e auditoria de adesão aos protocolos | ⬜ só com aprovação |

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

## 3. O que vem (Q2 e Q3)

- **Não conformidade** de várias origens (incidente, irregularidade da VISA numa unidade pública, indicador fora da meta, auditoria interna,
  reclamação) com o plano 5W2H e a verificação de eficácia.
- **Indicadores** calculados dos registros da rede com meta por unidade; **checklists** versionados de adesão aos protocolos básicos.
