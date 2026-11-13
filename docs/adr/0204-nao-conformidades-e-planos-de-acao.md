# 0204 — Qualidade: não conformidades e planos de ação (Q2)

## Status

Aceita e implementada. Fatia **Q2** da Qualidade ([ADR-0202](./0202-qualidade-do-servico.md); detalhe em
[`qualidade/MODELO-QUALIDADE.md`](../qualidade/MODELO-QUALIDADE.md)), depois da Q1 ([ADR-0203](./0203-seguranca-do-paciente.md)).

## Contexto

- **A Q1** registra e analisa o incidente, mas a gestão da qualidade pede tratar a causa e conferir se resolveu: o ciclo PDCA (planejar,
  fazer, verificar, agir).
- **As fontes de não conformidade** já existem no sistema: o incidente concluído (Q1) e a irregularidade da VISA numa unidade pública,
  cujo estabelecimento está ligado à unidade (V3a, [ADR-0199](./0199-vigilancia-sanitaria-fundacao.md)). Outras chegam de fora: a auditoria
  interna e a reclamação.
- **Ferramentas consagradas:** o diagrama de Ishikawa (6M) para a causa e o 5W2H para o plano.

## Decisão

1. **Não conformidade** (`TB_NAO_CONFORMIDADE`), com `QUALIDADE.GERIR` na unidade (403):
   - número do ano, unidade, setor opcional (da mesma unidade, 422), data de identificação (não futura, 400), descrição, **correção
     imediata** (o que se fez na hora, diferente da ação corretiva), gravidade (menor, maior, crítica) e o **responsável** pela matrícula
     (404);
   - **origem:** incidente, inspeção sanitária, auditoria interna, reclamação ou outra. O incidente e a inspeção pedem o vínculo e só eles
     o aceitam (400); o incidente é da mesma unidade e a irregularidade é de estabelecimento ligado à unidade (422); **uma em aberto por
     vínculo** (409; a cancelada e a encerrada liberam). A origem indicador ficou reservada para a Q3 (400) e chegou com ela ([ADR-0205](./0205-indicadores-de-qualidade-e-auditoria-de-protocolos.md)):
     o indicador e a competência;
   - **análise de causa** em texto, com as categorias dos 6M (método, máquina, mão de obra, material, medição, meio ambiente), enquanto não
     fechou (422);
   - situação: ABERTA → EM_TRATAMENTO (com a primeira ação) → EM_VERIFICACAO → ENCERRADA; ou CANCELADA, com o motivo, levando as ações
     abertas.
2. **Plano 5W2H por ciclo** (`TB_ACAO_QUALIDADE`):
   - o quê, por quê, quem (matrícula), onde, quando (o prazo, não antes da identificação, 400), como e quanto custa;
   - as ações entram com a não conformidade aberta ou em tratamento (422) e pertencem ao **ciclo** corrente;
   - PENDENTE → EM_ANDAMENTO → CONCLUIDA (com a **evidência**, 400 sem ela, e a data não futura), ou CANCELADA pela gestão com o motivo;
     **atrasada** é calculada pelo prazo;
   - o **andamento** é da gestão ou do **próprio responsável**, mesmo sem `QUALIDADE.GERIR` (403 aos outros); o cancelamento, só da gestão;
   - terminadas as ações do ciclo com ao menos uma concluída, a não conformidade vai à **verificação**, prevista para 30 dias.
3. **Verificação de eficácia** (`TB_VERIFICACAO_EFICACIA`), só em verificação (422): **eficaz** encerra; **não eficaz** pede a observação
   (400) e abre o **ciclo seguinte**, em tratamento. As ações dos ciclos anteriores ficam no histórico e não mudam mais (422).
4. **Sugestões**, sem abrir nada: o incidente concluído com dano moderado, grave ou óbito, ou never event, e a irregularidade aberta da VISA
   em estabelecimento ligado à unidade, sem não conformidade (fora as canceladas), nas unidades em que se gere.
5. **Minhas ações:** as ações abertas do ciclo corrente cujo responsável é quem está logado (pelo CPF do profissional).
6. **Painel**, só com totais (`QUALIDADE.GERIR` ou `INDICADOR.CONSULTAR`): total e em aberto, ações atrasadas, verificadas e a **eficácia
   na primeira verificação** (%), por origem, gravidade e situação; sem as canceladas.
7. **Acesso:** `QUALIDADE.GERIR` (nova) no Núcleo de Segurança do Paciente e no Gestor; a rota de andamento e "minhas ações" aceitam
   `QUALIDADE.NOTIFICAR`, que todo papel da rede tem (ADR-0203), com a regra do responsável no serviço.
8. **Tela** `/qualidade/nao-conformidades`, no menu principal: as abas Não conformidades, Minhas ações, Sugestões e Painel; o registro e o
   detalhe em gaveta (a análise, o plano com o quadro das ações, as verificações, o cancelamento). **Atalhos** "Abrir não conformidade" no
   incidente concluído (Segurança do paciente) e na irregularidade aberta da unidade da rede (Vigilância sanitária), que abrem o registro
   já vinculado.

### API

| Método | Caminho (`/api/v1/qualidade/`) | Permissão |
|---|---|---|
| GET | `nao-conformidades` (`unidadeId`, `situacao`, `origem`), `nao-conformidade/{id}`, `nao-conformidades/sugestoes` | `QUALIDADE.GERIR` |
| POST | `nao-conformidade`, `nao-conformidade/{id}/analise`, `/acao`, `/verificacao`, `/cancelamento`, `acao/{id}/cancelamento` | `QUALIDADE.GERIR` |
| POST | `acao/{id}/andamento` | `QUALIDADE.GERIR` ou o responsável (`QUALIDADE.NOTIFICAR`) |
| GET | `minhas-acoes` | `QUALIDADE.GERIR` ou `QUALIDADE.NOTIFICAR` |
| GET | `nao-conformidades/painel` (`unidadeId`) | `QUALIDADE.GERIR` ou `INDICADOR.CONSULTAR` |

### Migração

`V86__qualidade_nao_conformidades.sql`: `tb_nao_conformidade`, `tb_acao_qualidade` e `tb_verificacao_eficacia`.

## Alternativas descartadas

- **Abrir a não conformidade sozinha** a partir do incidente grave: a gestão decide; o sistema sugere.
- **Uma tabela de planos com versões:** o ciclo na ação e na verificação guarda o histórico sem tabela a mais.
- **Evidência como anexo:** depende de um dono de documento novo (D2); a evidência fica em texto por ora.

## Consequências

- **STATUS-DOS-DOMINIOS:** item 18 com a Q1 e a Q2; falta a Q3.
- **Fora do escopo:** anexo de evidência, aviso ao responsável por e-mail. A origem indicador chegou com a Q3 (ADR-0205).

## Testes

- **JUnit `NaoConformidadeControllerTest`:** as regras da origem (indicador 400; vínculo faltando ou sobrando 400; incidente de outra
  unidade 422; setor de outra unidade 422; responsável inexistente 404; data futura 400; duplicada em aberto 409; a cancelada leva as ações
  e libera o vínculo); o ciclo (verificar antes 422; análise com 6M sem repetir; prazo antes da identificação 400; atrasada; andamento
  inválido ou sem evidência 400; cancelar ação; em verificação com a data prevista; ação nova em verificação 422; não eficaz sem observação
  400; ciclo 2; ação antiga 422; conclusão futura 400; eficaz encerra; análise depois 422; painel com eficácia na primeira 0%); as
  sugestões do incidente grave e da irregularidade da unidade da rede, que somem com a não conformidade, e a irregularidade de
  estabelecimento privado (fora das sugestões, 422).
- **JUnit `NaoConformidadeAutorizacaoControllerTest`,** com a segurança ligada: o gestor de outra unidade e o técnico não registram; o núcleo
  registra e planeja; o responsável vê e conclui a própria ação e o colega não; o responsável não cancela nem verifica; o gestor da unidade
  verifica e vê o painel; a recepção não lista; sem login, 401.
- **Robot:** `NaoConformidade.robot` (CT-001 a CT-004) e `UI_nao_conformidades.robot` (menu, registro, plano de ação).
