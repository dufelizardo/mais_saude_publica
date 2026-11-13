# 0197 — Gestão da Rede: redes temáticas e referência estrutural (GR2)

## Status

Aceita e implementada. Fatia **GR2** do domínio 30 ([ADR-0179](./0179-gestao-da-rede-de-atencao.md); detalhe em
[`rede/MODELO-REDE.md`](../rede/MODELO-REDE.md)). A GR3 (cobertura e indicadores de pactuação) continua só com aprovação.

## Contexto

- **A GR1 cuidou da quantidade:** a oferta e as cotas da PPI, que a Regulação consome. Ali ficou a regra de que vale a cota mais específica
  que cobre a solicitante, a da unidade ou a da esfera acima.
- **A RAPS ([ADR-0153](./0153-saude-mental-caps-e-raps.md)) já mostrou o padrão das redes temáticas:** o componente é dado do modelo
  operacional, e o painel mostra os pontos e as lacunas.
- **Faltavam as demais redes da Portaria de Consolidação GM/MS nº 3/2017** e a referência estrutural: para onde cada unidade encaminha cada
  procedimento.
- **O procedimento regulado já diz a capacidade que exige,** o que dá a regra de que a executante pode fazer o procedimento.

## Decisão

1. **Redes temáticas e componentes, fixos no código** (`RedeTematica`, `ComponenteRedeTematica`). Ficam só os componentes que são ponto de
   atendimento; a Força Nacional, a governança e a regulação como função ficam fora.

   | Rede | Componentes |
   |---|---|
   | Urgência e Emergência | atenção básica; SAMU 192 e central de regulação; sala de estabilização; UPA 24h e serviços de urgência 24h; hospitalar; atenção domiciliar |
   | Cegonha / Alyne | pré-natal; parto e nascimento; puerpério e saúde da criança; sistema logístico |
   | Pessoa com Deficiência | atenção básica; atenção especializada em reabilitação; hospitalar e de urgência |
   | Doenças Crônicas | atenção básica; atenção especializada; sistemas de apoio; sistemas logísticos |

2. **A ligação ao modelo operacional** (`TB_COMPONENTE_REDE_ITEM`):
   - cada item liga um componente a um perfil ou a uma capacidade, um dos dois (400);
   - uma capacidade pode estar em várias redes (a atenção primária está em todas), por isso é uma tabela e não uma coluna, como na RAPS;
   - a carga liga os códigos existentes, por exemplo urgência, classificação de risco e observação na UPA 24h, obstetrícia no parto e
     nascimento, reabilitação física, auditiva, visual e intelectual, órteses e próteses e odontologia para PcD na reabilitação, e análises
     clínicas, diagnóstico por imagem e assistência farmacêutica nos sistemas de apoio;
   - o catálogo vale para a rede inteira: só quem gerencia a rede toda liga ou desliga (403); repetido, 409;
   - a RAPS continua no seu painel, e a tela das redes leva até ele.
3. **Painel da rede temática:**
   - por componente, as unidades ativas do acesso com o perfil ou a capacidade vigente ligados a ele, e quais;
   - **lacunas:** o componente sem nenhum ponto; com mais de um município no acesso, o componente com ponto na rede e sem ponto no município
     (pela unidade superior e pela supervisão regional);
   - só unidades e números, nada de paciente.
4. **Referência estrutural** (`TB_REFERENCIA_REDE`):
   - a origem (unidade ou esfera, como a beneficiária da cota), o procedimento regulado, a executante, a vigência e a observação;
   - **histórico, não campo mutável:** a nova referência da mesma origem e procedimento começa depois da vigente (422) e a encerra na véspera;
     a mesma executante da vigente é conflito (409); encerrar sem substituir fecha hoje, e a que ainda não começou não se encerra (422);
   - a executante precisa estar ativa e ter a capacidade que o procedimento exige (422) e não ser a origem (400);
   - **resolução:** vale a mais específica vigente no dia, a da unidade e, sem ela, a da esfera mais próxima acima (a mesma regra da cota).
5. **A Regulação recebe a referência como sugestão:**
   - na gaveta Autorizar, a executante vem escolhida pela referência, com o rótulo "Referência da rede" e o aviso quando o regulador escolhe
     outra;
   - o regulador pode autorizar em outra executante sem justificativa, porque a decisão do caso é dele;
   - a autorização guarda `foraDaReferencia` (nulo sem referência vigente), porque a referência muda com o tempo; fora dela, o evento da
     autorização diz qual era a referência;
   - sem referência cadastrada, nada muda: a implantação é gradual, como a da PPI.
6. **Painel das referências:** as do acesso (origem ou executante), vigentes, futuras e encerradas; e, nos últimos 90 dias, por
   procedimento, as autorizações que tinham referência e quantas foram para outra executante, com o percentual. É o dado que a GR3 vai
   consolidar.
7. **Acesso:** sem permissão nova. `REDE.CONSULTAR` para os painéis; `REDE.GERENCIAR` para as referências, no escopo da origem (quem pactua
   para onde os seus encaminham é a gestão da origem); a sugestão também com `REGULACAO.REGULAR`.
8. **Telas:**
   - **Rede e pactuação** (Administrativo) ganha abas: PPI (a tela da GR1), Redes temáticas (a rede, as lacunas no alto, os componentes com as
     unidades e o link para a RAPS) e Referências (os números de fora da referência, a lista com a vigência e a situação, a gaveta da nova
     referência e o encerramento);
   - **Regulação, gaveta Autorizar:** a executante sugerida.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `rede/tematica` (catálogo) e `rede/tematica/{rede}` (painel) | `REDE.CONSULTAR` ou `REDE.GERENCIAR` |
| POST, DELETE | `rede/tematica/componente/{componente}/item` (`perfilId` ou `capacidadeId`), `rede/tematica/item/{id}` | `REDE.GERENCIAR` na rede inteira |
| GET | `rede/referencia` (`procedimentoId`) | `REDE.CONSULTAR` ou `REDE.GERENCIAR` |
| POST | `rede/referencia` (`origemId`, `procedimentoId`, `executanteId`, `inicio`, `observacao`) e `rede/referencia/{id}/encerramento` | `REDE.GERENCIAR` (origem) |
| GET | `rede/referencia/sugestao` (`procedimentoId`, `solicitanteId`) | e também `REGULACAO.REGULAR` |

A solicitação de regulação passa a trazer `foraDaReferencia`.

### Migração

`V81__redes_tematicas_e_referencia.sql`: `tb_componente_rede_item` com a carga, `tb_referencia_rede` (uma aberta por origem e procedimento) e
a coluna `fora_da_referencia` em `tb_solicitacao_regulacao`.

## Alternativas descartadas

- **O componente como coluna do perfil e da capacidade, como na RAPS:** a mesma capacidade está em várias redes.
- **Obrigar a executante da referência ou pedir justificativa:** a Regulação decide o caso; a rede define o caminho esperado e mede o desvio.
- **Calcular "fora da referência" na hora do painel:** a referência muda; o que vale é a do dia da autorização.
- **Tela de edição do catálogo das redes:** a carga cobre os códigos existentes; o ajuste é raro e fica na API, para quem gerencia a rede
  toda.

## Consequências

- **Implantação:** cadastrar as referências por procedimento, começando pelas esferas (município), e refinar por unidade onde for diferente.
- **Fora do escopo:** a cobertura da rede e os indicadores de pactuação (GR3); financiamento e habilitações da Rede Alyne e da RUE; SISREG
  (domínio 20); as linhas de cuidado, que estão em Programas (P5).

## Testes

- **JUnit `RedeTematicaControllerTest`:**
  - o catálogo das quatro redes com a carga; a ligação sem alvo e com os dois (400), a nova (201), a repetida (409) e o desligamento;
  - o painel com a policlínica na atenção especializada e a lacuna do segundo município;
  - a referência sem a capacidade exigida (422), na mesma unidade (400), repetida (409); a da unidade vencendo a da esfera; sem referência;
  - a troca que começa no mesmo dia (422) e a do dia seguinte, que encerra a vigente hoje;
  - a autorização dentro, fora (com o evento) e sem referência; o painel com 50% fora; o encerramento da futura (422) e da vigente.
- **JUnit `RedeTematicaAutorizacaoControllerTest`,** com a segurança ligada: sem login (401), o gestor de outra unidade e o regulador sem
  registrar (403), o gestor do município registrando e vendo o painel, o regulador com a sugestão, o gestor sem alterar o catálogo (403) e o
  ACS sem os painéis (403).
- **Regressão:** `RedeControllerTest`, `SolicitacaoRegulacaoControllerTest`, `RegulacaoAutorizacaoControllerTest` e as demais da Regulação.
- **Robot:** `RedeTematica.robot` (API, CT-001) e os CT-002 a CT-004 de `UI_ppi.robot` (redes temáticas, nova referência e a sugestão na
  gaveta da Regulação).
