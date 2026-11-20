# 0221 — Financeiro: rateio do PNGC em degraus e lotação por setor (F4)

## Status

Aceita e implementada. Quarta fatia do domínio 17, aberta depois do domínio completo para fechar a pendência que a
[ADR-0220](./0220-financeiro-centros-de-custo-e-apuracao.md) deixou: o rateio dos centros de apoio e administrativos.

## Contexto

- **A F3 apura o custo direto de cada centro:** produtivo, de apoio ou administrativo (PNGC/MS).
- **O PNGC tem uma segunda etapa:** o custo dos centros que não atendem (administração, limpeza, almoxarifado) é distribuído aos
  produtivos. Sem ela, o custo do centro produtivo fica subestimado, e a unidade mista não separa a internação do ambulatório.
- **A F3 deixou três pendências:**
  - o critério do rateio, que o município precisa escolher;
  - a lotação por setor no RH, para a folha ir ao centro do setor e servir de base ao critério de pessoas;
  - a unidade mista (hospital com ambulatório).
- **A plataforma já tem as bases:**
  - a jornada da lotação (RH);
  - as saídas de consumo do Estoque por setor;
  - os leitos por setor (ADR-0185).

## Decisão

1. **Lotação por setor:**
   - a lotação ganha o setor, opcional;
   - o setor precisa ser da unidade da lotação e estar ativo (422); o inexistente dá 404;
   - trocar de setor é uma nova lotação, como trocar de unidade (histórico, docs/rh/MODELO-RH.md);
   - com o setor, a folha vai ao centro do setor; sem ele, ao geral da unidade, como na F3.
2. **Critério do centro:**

   | Critério | Base no destino |
   |----------|-----------------|
   | `PESSOAS` | as horas semanais das lotações vigentes no mês no centro (sem jornada informada, 40) |
   | `CUSTO_DIRETO` | o custo direto do centro |
   | `AREA` | os m² informados no centro |
   | `REQUISICOES` | as saídas de consumo do Estoque no mês para o setor do centro |

   - sem critério, o padrão do tipo: pessoas no administrativo, custo direto no apoio;
   - o produtivo recebe e não tem critério (422);
   - a área é maior que zero (400).
3. **Rateio em degraus (step-down), em cada unidade:**
   - a sequência: os administrativos, depois os de apoio, na ordem do código;
   - cada centro manda o que tem, o direto mais o recebido, aos centros ativos da unidade que ainda não ratearam e aos produtivos,
     na proporção da base;
   - quem já rateou não recebe de volta;
   - base zero nos destinos: vai pelo custo direto;
   - sem destino com base (a unidade de gestão, o laboratório, a unidade só de apoio): o custo fica no centro e é contado como
     **não rateado**;
   - o arredondamento: cada parte em centavos, a diferença na última;
   - o total da unidade não muda, porque o rateio fica dentro dela.
4. **Unidade mista:**
   - o centro produtivo cujo setor tem leito ativo é de **internação**;
   - o custo final da internação dividido pelos pacientes-dia dá o custo por paciente-dia;
   - o resto da unidade dividido pelos atendimentos dá o custo por atendimento;
   - a unidade sem internação segue a F3: o total pelos dois.
5. **Apuração:**
   - o rateio roda ao apurar e ao refazer;
   - fechada, vira fotografia com o resto; mudar o critério depois não muda a competência fechada;
   - a apuração guarda, por centro, o direto, o recebido, o enviado e se é internação, e as linhas origem → destino com o critério
     aplicado e o degrau;
   - a apuração feita antes da F4 tem o não rateado nulo e aparece como "apurada antes do rateio".
6. **API:**
   - o centro ganha o critério e a área, no pedido e na resposta (a resposta traz o critério aplicado);
   - a apuração traz, por centro, o recebido, o enviado, o custo final e a internação; as linhas do rateio e o não rateado;
   - a lotação ganha o `setorId` no pedido e o `setorNome` na resposta;
   - as permissões não mudam: `FINANCEIRO.GERENCIAR` no centro e na apuração, `RH.GERENCIAR` na lotação.
7. **Tela:**
   - na aba **Custos**:
     - o cartão do não rateado;
     - as colunas Direto, Recebido, Enviado e Final por centro;
     - a tabela do rateio, com o degrau, a origem, o destino, o critério e o valor;
     - o critério e a área no centro;
   - no perfil do profissional, a transferência escolhe o setor da unidade; a lotação mostra o setor;
   - na Sala de Situação, o cartão Custo por unidade mostra o custo por paciente-dia quando há.
8. **Banco:** a `V102__financeiro_rateio_pngc.sql`:
   - põe o `setor_id` em `tb_lotacao`;
   - põe o `criterio_rateio` e a `area_m2` em `tb_centro_custo`;
   - põe o `nao_rateado` em `tb_apuracao_custo`;
   - cria `tb_custo_centro_apuracao` e `tb_rateio_apuracao_custo`.

## Alternativas descartadas

- **Rateio recíproco (equações simultâneas):**
  - mais exato quando os centros de apoio se atendem uns aos outros;
  - o PNGC recomenda os degraus pela simplicidade, e o gestor confere a conta linha a linha.
- **Ratear o custo da secretaria (unidade de gestão) para a rede:**
  - o PNGC para no estabelecimento;
  - fica como não rateado, à vista, em vez de inflar o custo por atendimento das unidades.
- **Percentual fixo informado à mão:**
  - pediria mais uma tabela por centro e destino;
  - os quatro critérios cobrem o que a plataforma registra.
- **Quilo de roupa e refeições servidas,** critérios clássicos de lavanderia e nutrição:
  - a plataforma não registra esses dados; o custo direto ou a área substituem.
- **Setor obrigatório na lotação:** quebraria as lotações existentes e o profissional que circula pela unidade.

## Consequências

- **O custo do centro produtivo fica completo,** com o apoio e a administração absorvidos.
- **A unidade mista separa** o custo por paciente-dia do custo por atendimento.
- **A folha passa a ir ao centro do setor** quando a lotação informa o setor.
- **Pendências:**
  - o teto financeiro da PPI pela tabela SIGTAP e o envio ao ApuraSUS (domínio 20);
  - o pacientes-dia por setor, quando o censo for por setor (hoje é por unidade).

## Testes

- **JUnit `CustosControllerTest`:**
  - o critério padrão (custo direto no apoio, pessoas no administrativo), o produtivo com critério (422) e a área negativa (400);
  - o rateio em degraus: a administração por pessoas (250 e 750), a limpeza por área (25 e 75), sem devolver à administração;
  - o geral de apoio da outra unidade sem destino, não rateado;
  - a unidade mista: o custo por atendimento sem a internação e o paciente-dia nulo sem censo;
  - sem base no critério, pelo custo direto.
- **JUnit `LotacaoControllerTest`:** o setor da unidade (201, com o nome na resposta), o de outra unidade e o inativo (422), o
  inexistente (404).
- **JUnit `LiquidacaoAutorizacaoControllerTest`, com a autorização ligada:** o critério do centro só pelo Financeiro (403 ao gestor e
  ao Compras).
- **Robot de API:** `test/financeiro/Custos.robot`, CT-003 (critério, área, lotação no setor e a apuração com o não rateado).
- **Robot de tela:**
  - `UI_financeiro.robot`, CT-007 (o centro com o critério área e as colunas do rateio);
  - `UI_perfil_abas_dados_a_ajustes.robot`, CT-004 (a transferência para um setor da unidade).
