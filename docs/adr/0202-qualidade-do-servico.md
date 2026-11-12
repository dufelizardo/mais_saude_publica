# 0202 — Qualidade do serviço de saúde: segurança do paciente, não conformidades e indicadores

## Status

Aceita. **Desenho** da parte Qualidade do item 18 do mapa (Qualidade / Segurança / Auditoria). A Auditoria já está implementada
(ADRs 0070, 0071, 0082, 0096 e 0097). Cada fatia tem ADR própria. Implementada: Q1 (segurança do paciente,
[ADR-0203](./0203-seguranca-do-paciente.md)). Detalhe em [`qualidade/MODELO-QUALIDADE.md`](../qualidade/MODELO-QUALIDADE.md).

## Contexto

- **O item 18 do mapa** ([MAPA-DE-DOMINIOS](../MAPA-DE-DOMINIOS.md)) previa `Indicador`, `NaoConformidade` e `PlanoDeAcao` para a qualidade da
  prestação do serviço; nada disso existia no código.
- **A base normativa no SUS:**
  - o Programa Nacional de Segurança do Paciente (Portaria MS 529/2013) e a RDC ANVISA 36/2013 mandam todo serviço de saúde ter um
    **Núcleo de Segurança do Paciente (NSP)**, notificar os incidentes internamente, com cultura **não punitiva**, e notificar os eventos
    adversos ao Sistema Nacional de Vigilância Sanitária pelo Notivisa (óbito em 72 horas; os demais até o 15º dia útil do mês seguinte);
  - os **protocolos básicos** (Portarias MS 1.377 e 2.095/2013): identificação do paciente, cirurgia segura, higiene das mãos, prevenção
    de quedas, de lesão por pressão e de erros de medicação;
  - a **gestão da qualidade** pelo ciclo PDCA: não conformidade, plano de ação (5W2H), verificação de eficácia, indicadores com metas.
- **Não cabe noutro domínio** (ADR-0116): a Vigilância Sanitária regula o serviço de fora (V3); a qualidade é a gestão de dentro do serviço.

## Decisão

1. **Três fatias, uma por PR, cada uma com ADR própria:**
   - **Q1 · Segurança do paciente** ([ADR-0203](./0203-seguranca-do-paciente.md)): a notificação do incidente por qualquer profissional,
     anônima ou identificada; a classificação pela taxonomia da OMS (ICPS: natureza e grau de dano); a análise do NSP com os fatores
     contribuintes e a causa raiz no dano grave, no óbito e no never event; o registro do Notivisa com o prazo; o painel;
   - **Q2 · Não conformidades e planos de ação:** a não conformidade com a origem (incidente da Q1, irregularidade da VISA numa unidade
     pública da rede, indicador fora da meta da Q3, auditoria interna ou reclamação), a gravidade e o responsável; o plano 5W2H com as
     ações e os prazos; a verificação de eficácia que fecha ou reabre (PDCA);
   - **Q3 · Indicadores com metas e auditoria de protocolos:** indicadores calculados do que a rede registra (quedas e lesão por pressão
     por mil pacientes-dia, reinternação em 30 dias, tempo porta-médico, absenteísmo, eventos adversos por gravidade), meta por unidade,
     série mensal, o fora da meta sugerindo a não conformidade; checklists versionados de adesão aos protocolos (higiene das mãos,
     cirurgia segura).
2. **Princípios:**
   - **cultura não punitiva:** o notificador só aparece para o núcleo e, na anônima, não é guardado; os números saem sem pessoas;
   - **dado de saúde:** o incidente com paciente segue o acesso por unidade e entra na trilha de auditoria;
   - **calculado, não guardado:** prazos e indicadores saem dos registros;
   - **a rede sugere, a gestão decide:** o indicador fora da meta sugere a não conformidade, sem abri-la sozinha.
3. **Acesso:** `QUALIDADE.NOTIFICAR` em todo papel da rede (o administrador da plataforma não atua no cuidado); `QUALIDADE.ANALISAR` no
   papel novo **Núcleo de Segurança do Paciente**; as fatias seguintes acrescentam as suas.

## Alternativas descartadas

- **Qualidade dentro da Vigilância Sanitária:** a VISA fiscaliza o serviço de fora, com poder de polícia; o NSP é do próprio serviço.
- **Notificação só identificada:** desestimula notificar e contraria a cultura de segurança da RDC 36/2013.
- **Integrar o Notivisa agora:** é integração (domínio 20) e não impede a fatia; o número e a data são registrados à mão.

## Consequências

- **STATUS-DOS-DOMINIOS:** o item 18 passa a ter a Qualidade em andamento.
- **Fora do escopo:** o Notivisa automático, a acreditação ONA, o controle de infecção completo (IRAS pelos critérios da ANVISA) e a
  ouvidoria SUS como domínio; a reclamação entra só como origem textual da não conformidade (Q2).
