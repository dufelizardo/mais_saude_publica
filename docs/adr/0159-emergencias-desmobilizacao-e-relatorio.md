# 0159 — Emergências e Desastres: desmobilização e relatório pós-evento (E5)

## Status

Aceita e implementada. Quarta fatia implementada do domínio 27 (a E5 do desenho), na
[ADR-0113](./0113-emergencias-e-desastres.md) e em [`emergencias/MODELO-EMERGENCIAS.md`](../emergencias/MODELO-EMERGENCIAS.md),
seção 12. Usa as E1 a E3 ([ADR-0156](./0156-emergencias-nucleo.md), [ADR-0157](./0157-emergencias-contingencia.md),
[ADR-0158](./0158-emergencias-coordenacao.md)). A E4 (integração da rede) fica para depois, porque depende de Estoque e
Transporte.

## Contexto

- **Não basta ativar: é preciso controlar o retorno** (ADR-0113). Até a E3, encerrar a emergência era um passo só, e a
  exceção da Escala só acabava ali.
- **O relatório pós-evento preserva a memória do evento** e alimenta a revisão do plano, que a E2 já suporta com o
  "Revisar".

## Decisão

1. **A emergência ganha a situação "em desmobilização":** ativa → em desmobilização → encerrada (CHECK atualizado em
   `tb_emergencia`).
2. **Iniciar a desmobilização** (`EMERGENCIA.ATIVAR`, da ativa; fora dela, 422), com o responsável e a justificativa:
   - de uma vez: conclui as mobilizações em aberto (fim hoje; a Escala deixa de aceitar turnos novos sem lotação),
     desativa a capacidade emergencial e os planos ativos. As mobilizações ligadas a uma transferência da Farmácia seguem
     a transferência;
   - registra o que foi liberado (`TB_DESMOBILIZACAO`, uma por emergência): mobilizações, profissionais, capacidades e
     planos;
   - **durante a desmobilização:** não entra ação, mobilização, capacidade nem plano novos, e o nível não muda (422). As
     ações abertas ainda se iniciam, concluem e cancelam; "Cancelar as ações restantes" cancela todas com o motivo
     (nenhuma aberta, 422);
   - **pendências** (no detalhe): as ações em aberto e as unidades afetadas da última avaliação que não voltaram à
     operação (só aviso: a situação muda em Equipamentos de Saúde).
3. **Encerrar:**
   - da desmobilização, pede nenhuma ação aberta (422);
   - direto da ativa continua possível, para o evento curto, e faz as mesmas liberações (inclusive concluir as
     mobilizações, que antes ficavam abertas).
4. **Relatório pós-evento** (`TB_RELATORIO_POS_EVENTO`, um por emergência; recomendações em `TB_RECOMENDACAO_RELATORIO`):
   - **texto da equipe:** resumo, problemas, lições aprendidas e recomendações (a lista substitui a anterior);
   - **números calculados na leitura,** do que foi registrado: duração, horas do evento à ativação, nível máximo e
     mudanças de nível, ações (total, concluídas, canceladas, percentual concluído no prazo), mobilizações por tipo e
     profissionais, capacidade emergencial ativada e a última avaliação de impacto;
   - **ciclo:** o rascunho começa na desmobilização ou depois de encerrada (na ativa, 422); finalizar pede a emergência
     encerrada e o resumo (422); finalizado não muda (422);
   - **revisão do plano:** a recomendação pode apontar um plano usado na emergência (outro, 422). "Revisar plano" abre a
     revisão da E2 (a vigente vira rascunho novo, com `CONTINGENCIA.GERENCIAR`) e a recomendação guarda a versão gerada
     (de novo, 409; sem plano, 422). Reescrever o rascunho mantém a ligação da recomendação igual;
   - **CSV** dos números, para a planilha da gestão.
5. **Permissões** (sem nova): `EMERGENCIA.ATIVAR` desmobiliza, encerra e finaliza; `EMERGENCIA.COORDENAR` ou
   `EMERGENCIA.ATIVAR` escrevem o rascunho e cancelam as ações restantes; `CONTINGENCIA.GERENCIAR` revisa o plano; a
   leitura é a da E1.

### API

| Método | Caminho | Permissão |
|---|---|---|
| POST | `emergencia/{id}/desmobilizacao` | `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/acoes-restantes/cancelamento` | `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR` |
| GET | `emergencia/{id}/relatorio`, `emergencia/{id}/relatorio.csv` | leitura |
| PUT | `emergencia/{id}/relatorio` | `EMERGENCIA.COORDENAR` ou `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/relatorio/finalizacao` | `EMERGENCIA.ATIVAR` |
| POST | `emergencia/{id}/relatorio/recomendacao/{rec}/revisao-plano` | `CONTINGENCIA.GERENCIAR` |

A emergência ganha `desmobilizacao` e `pendencias`; a situação ganha `EM_DESMOBILIZACAO`.

### Tela

- **Detalhe da emergência:** "Iniciar desmobilização"; na desmobilização, o quadro do que foi liberado, as pendências,
  "Cancelar ações restantes" e "Encerrar" (só sem ação aberta); a coordenação mostra só o que ainda se conclui.
- **Seção "Relatório pós-evento"** (na desmobilização e depois): os números, o editor (resumo, problemas, lições,
  recomendações com o plano a revisar), salvar, finalizar, "Revisar plano" e "Exportar números (CSV)". Finalizado, só
  leitura.
- **Aba "Em desmobilização"** na tela Emergências.
- **Sala de Situação:** a emergência em desmobilização aparece no alto, como alerta médio.

### Migração

`V46__emergencias_desmobilizacao_e_relatorio.sql`: o CHECK da situação da emergência, e as tabelas da desmobilização, do
relatório e das recomendações.

## Consequências

- **O retorno à normalidade tem passo próprio e registro,** e a exceção da Escala acaba quando a resposta acaba, não só
  no encerramento.
- **O relatório sai com os números prontos;** a equipe escreve só o que o sistema não sabe.
- **Ficam para depois:**
  - a E4 (integração da rede), que depende de Estoque e Transporte;
  - a E6 (eventos especializados);
  - o relatório como documento formal (PDF assinado), que depende do domínio Documentos.

## Testes

- **JUnit:**
  - `DesmobilizacaoRelatorioControllerTest`:
    - desmobilização: as liberações e o registro, pendências, o fim da exceção da Escala, os bloqueios (ação,
      mobilização, nível, desmobilizar de novo), encerrar com ação aberta (422), cancelar as restantes (e de novo, 422),
      encerrar; encerrar direto da ativa também libera;
    - relatório: na ativa (422), os números sem rascunho, plano não usado (422), rascunho, finalizar antes de encerrar
      (422), revisão do plano (v2; de novo 409; sem plano 422), a ligação mantida ao reescrever, finalizar, ajustar e
      finalizar de novo (422), o CSV;
  - `EmergenciaAutorizacaoControllerTest.desmobilizacaoERelatorioPorPapel`, com segurança ligada: desmobilizar e
    finalizar são do gestor; o coordenador escreve o rascunho; a vigilância lê e não escreve.
- **Robot de API:** `test/emergencias/Desmobilizacao.robot`, CT-001 a CT-011.
- **Robot de tela:** `UI_emergencias.robot`, CT-007 (desmobilizar e encerrar) e CT-008 (relatório e revisão do plano).
