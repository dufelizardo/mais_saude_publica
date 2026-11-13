# 0205 — Qualidade: indicadores com metas e auditoria de protocolos (Q3)

## Status

Aceita e implementada. Fatia **Q3**, a última da Qualidade ([ADR-0202](./0202-qualidade-do-servico.md); detalhe em
[`qualidade/MODELO-QUALIDADE.md`](../qualidade/MODELO-QUALIDADE.md)), depois da Q1 ([ADR-0203](./0203-seguranca-do-paciente.md)) e da Q2
([ADR-0204](./0204-nao-conformidades-e-planos-de-acao.md)). Com ela, o item 18 do mapa fica completo.

## Contexto

- **A gestão da qualidade mede** para saber se o serviço melhora. Os indicadores clássicos de segurança do paciente (quedas e lesão por
  pressão por mil pacientes-dia, eventos adversos graves, reinternação) e de acesso (tempo até o médico, absenteísmo) saem de registros que a
  rede já tem: internações (domínio 12), incidentes (Q1), atendimentos e consultas, agendamentos.
- **Sem meta, o número não diz nada;** a meta é da unidade, muda com o tempo, e o histórico precisa mostrar contra qual meta cada mês foi
  medido.
- **A adesão aos protocolos** (higiene das mãos, cirurgia segura) não aparece em registro clínico: se mede por **auditoria por observação**,
  com um checklist que muda de versão.
- **A Q2 reservou a origem indicador** para esta fatia.

## Decisão

1. **Indicadores** (`IndicadorQualidade`), **calculados, não guardados** (ADR-0202), por unidade ou na abrangência de quem consulta:

   | Indicador | Cálculo | Medida | Melhor |
   |---|---|---|---|
   | Taxa de quedas | incidentes de queda (fora os descartados) × 1000 / pacientes-dia | por mil pacientes-dia | menor |
   | Incidência de lesão por pressão | idem, lesão por pressão | por mil pacientes-dia | menor |
   | Eventos adversos graves | eventos adversos com dano grave ou óbito × 1000 / pacientes-dia | por mil pacientes-dia | menor |
   | Reinternação em 30 dias | altas do período com nova internação **em qualquer unidade da rede** em até 30 dias / altas | % | menor |
   | Tempo até o médico | **mediana** da chegada à primeira consulta do atendimento (0 a 24 h; o resto é erro de registro) | minutos | menor |
   | Absenteísmo | faltas / (realizados + faltas) dos agendamentos | % | menor |
   | Adesão ao protocolo | Σ conformes / Σ aplicáveis das auditorias, **um por checklist** | % | maior |

   - **pacientes-dia:** os dias de cada internação dentro do período (a internação de um dia só conta um);
   - **sem base, nunca zero:** sem denominador, a situação é `SEM_BASE` e o valor fica vazio.
2. **Painel** (`QUALIDADE.GERIR` ou `INDICADOR.CONSULTAR`): a competência (o mês, o anterior por padrão na tela), cada indicador com o
   numerador, o denominador, a meta e a situação (`DENTRO`, `FORA`, `SEM_META`, `SEM_BASE`) e a **série dos 12 meses** até ela. A meta só
   vale com a unidade escolhida; na visão agregada, a situação é sem meta.
3. **Meta** (`TB_META_QUALIDADE`), com `QUALIDADE.GERIR` na unidade (403):
   - indicador, valor (não negativo), **vigente desde** o primeiro dia do mês informado (o corrente por padrão); na adesão, o **checklist**
     é obrigatório, e só nela (400);
   - a meta nova **encerra a anterior na véspera** (vigente até); começar no mesmo mês ou antes da anterior é 422. O histórico fica.
4. **Checklists de protocolo** versionados (`TB_CHECKLIST_PROTOCOLO`, `TB_VERSAO_CHECKLIST_PROTOCOLO`):
   - código único em maiúsculas (409), nome e os itens (código único no checklist, sem diferenciar maiúsculas, 400, e texto);
   - a **nova versão** troca os itens; a auditoria guarda a versão que respondeu;
   - semeados na migração: **higiene das mãos** (os cinco momentos da OMS, M1 a M5) e **cirurgia segura** (OMS: entrada, pausa e saída,
     E1 a S3).
5. **Auditoria de protocolo** (`TB_AUDITORIA_PROTOCOLO`), com `QUALIDADE.GERIR` ou `QUALIDADE.ANALISAR` na unidade:
   - a versão do checklist, a data (não futura, hoje por padrão), setor opcional e a observação;
   - **todos os itens respondidos** (conforme, não conforme, não se aplica), sem item de fora, e ao menos um aplicável (400);
   - os totais (conformes e aplicáveis) ficam na auditoria; a adesão é calculada.
6. **A origem indicador na Q2:**
   - pede o **indicador** e a **competência** (gravada no primeiro dia do mês, não futura), e só ela os aceita; na adesão, o checklist (400);
   - **uma em aberto** por unidade, indicador, competência e checklist (409);
   - **sugestão:** o indicador com meta vigente **fora da meta no último mês fechado**, sem não conformidade da mesma competência, nas
     unidades em que se gere. A rede sugere, a gestão decide (ADR-0202).
7. **Relatório** "Qualidade do serviço" (categoria Qualidade) no catálogo do #19: os indicadores do período contra a meta da unidade (na
   competência do fim), incidentes por tipo, não conformidades por origem e situação e a adesão por protocolo. Só totais.
8. **Tela** `/qualidade/indicadores`, no menu principal (`QUALIDADE.GERIR`, `QUALIDADE.ANALISAR` ou `INDICADOR.CONSULTAR`): abas Painel
   (a série em linha pequena, o atalho "Abrir não conformidade" no fora da meta), Metas, Auditorias de protocolo (o checklist respondido na
   tela, com a adesão parcial) e Checklists (cadastro e nova versão). Na tela das não conformidades, a origem indicador ganha os campos e
   vem preenchida pela sugestão e pelo atalho.

### API

| Método | Caminho (`/api/v1/qualidade/`) | Permissão |
|---|---|---|
| GET | `indicadores` (`unidadeId`, `competencia=AAAA-MM`), `metas` (`unidadeId`) | `QUALIDADE.GERIR` ou `INDICADOR.CONSULTAR` |
| POST | `meta`, `checklist`, `checklist/{id}/versao` | `QUALIDADE.GERIR` |
| GET | `checklists` | `QUALIDADE.GERIR`, `QUALIDADE.ANALISAR` ou `INDICADOR.CONSULTAR` |
| GET, POST | `auditorias-protocolo` (`unidadeId`, `de`, `ate`; três meses por padrão), `auditoria-protocolo` | `QUALIDADE.GERIR` ou `QUALIDADE.ANALISAR` |

### Migração

`V87__qualidade_indicadores_e_protocolos.sql`: `tb_checklist_protocolo`, `tb_versao_checklist_protocolo`, `tb_auditoria_protocolo` e
`tb_meta_qualidade`; em `tb_nao_conformidade`, o indicador, a competência e o checklist; os dois checklists semeados.

## Alternativas descartadas

- **Guardar o valor mensal do indicador:** fica velho quando o registro de origem é corrigido; o cálculo sai dos registros (ADR-0202).
- **Meta da rede inteira:** as unidades têm perfis diferentes (um hospital não tem a taxa de quedas de uma UBS); a meta é por unidade.
- **Zero quando não há base:** esconde a falta de dado e pinta de verde o mês sem internação.
- **Tempo médio até o médico:** um registro esquecido aberto distorce a média; a mediana resiste.
- **Abrir a não conformidade sozinha no fora da meta:** a gestão decide; o sistema sugere.

## Consequências

- **STATUS-DOS-DOMINIOS:** item 18 completo (Auditoria, Q1, Q2 e Q3).
- **Fora do escopo:** indicadores de infecção (IRAS, pedem a CCIH e critérios diagnósticos), envio ao Notivisa ou ao CNES (integração, #20),
  meta por setor, anexo da evidência da auditoria.

## Testes

- **JUnit `IndicadoresQualidadeControllerTest`:** sobre dados conhecidos do mês anterior (17 pacientes-dia de três internações; duas quedas
  e uma descartada; um evento grave; uma reinternação em três altas; uma auditoria com 4 de 5): quedas 117,65, graves 58,82, reinternação
  33,3%, adesão 80%, lesão 0, absenteísmo e tempo sem base; as metas nos dois sentidos (fora, dentro, sem meta), a série de 12 meses, a
  meta seguinte encerrando a anterior, as regras da meta (400 e 422) e da auditoria (400 por item faltando, sobrando, nenhum aplicável e data
  futura); a sugestão do fora da meta e a origem indicador (400, 201, 409, a sugestão some); o relatório. Os checklists: 400, 409, nova versão
  e a linha nova no painel.
- **JUnit `IndicadoresQualidadeAutorizacaoControllerTest`:** o núcleo define meta e audita só na sua unidade; o gestor vê o painel da sua,
  não o da outra; o técnico não vê o painel nem cadastra checklist; a recepção não audita; sem token, 401.
- **Robot `IndicadoresQualidade.robot`** (API) e **`UI_indicadores_qualidade.robot`** (menu, meta e auditoria pela tela); o relatório entra
  no catálogo e na UI dos relatórios.
