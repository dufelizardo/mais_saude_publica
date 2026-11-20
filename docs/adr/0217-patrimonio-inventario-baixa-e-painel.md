# 0217 — Patrimônio e Manutenção: inventário, baixa e painel (PM3)

## Status

Aceita e implementada. Terceira e última fatia do domínio 15; o desenho está na
[ADR-0215](./0215-patrimonio-bens-tombamento-e-transferencia.md) e a PM2 na [ADR-0216](./0216-patrimonio-manutencao.md). Com ela, o
domínio 15 fica completo.

## Contexto

- **A Lei 4.320/1964 (arts. 94 a 96) pede o inventário dos bens permanentes,** com o levantamento anual por uma comissão. A divergência
  entre o registro e o que existe tem de ser apurada.
- **A baixa tira o bem do patrimônio,** mas o histórico fica. O motivo e o processo administrativo são obrigatórios; o furto pede o
  boletim de ocorrência.
- **A depreciação dos bens (MCASP) é linear pela vida útil da classe.** O lançamento contábil é do domínio 17, mas o valor contábil
  estimado já ajuda a decidir a baixa e a manutenção.
- **A PM2 registra o tempo parado,** mas o equipamento médico parado não aparecia fora da tela do Patrimônio.

## Decisão

### Inventário

1. **Abertura,** por quem gerencia o Patrimônio da unidade:
   - a comissão tem três profissionais diferentes (400), ativos (404/422);
   - um inventário aberto por unidade (409);
   - os bens tombados e não baixados da unidade entram na lista, com o setor de agora;
   - o número é `AAAA/000001`.
2. **Bens congelados:** enquanto o inventário estiver aberto, os bens da unidade não saem nem entram por transferência (422).
3. **Conferência,** pela comissão (só com o login, como o fiscal da CP3) ou pela gestão da unidade, no inventário aberto (422):
   - encontrado, não encontrado ou em outro local;
   - o bem visto pede a conservação: bom, regular, ruim ou inservível (400);
   - o outro local pede o setor ou a unidade (400): na própria unidade, o setor; em outra, a unidade e, se quiser, o setor dela. O setor
     tem de ser da unidade informada (422);
   - a conferência pode ser refeita até o fechamento.
4. **Achados a mais:** o bem que está na unidade e não está na lista é registrado em texto, com o local, para virar cadastro ou
   transferência.
5. **Fechamento,** por quem gerencia a unidade, com tudo conferido (422). O fechamento trata as divergências:
   - **em outro setor da unidade:** o setor do bem é corrigido;
   - **em outra unidade:** a transferência fica pendente para o aceite do destino;
   - **não encontrado ou inservível:** a baixa fica sugerida, para a gestão decidir.
6. **"Meus inventários":** os membros da comissão sem permissão de Patrimônio veem os próprios inventários. O menu ganha o item quando
   há algum aberto.

### Baixa

7. **Pedido,** por quem gerencia o Patrimônio da unidade:
   - o motivo é inservível, extravio, furto ou roubo (com o boletim, 400), doação a terceiro, alienação ou devolução da cessão (só do bem
     cedido ou em comodato, 422);
   - o processo administrativo e a data, não futura (400);
   - só o bem tombado e não baixado (422), sem ordem de serviço aberta nem transferência pendente (422), e uma baixa à espera por vez
     (409);
   - o valor contábil da data fica registrado na baixa.
8. **Alçada:**
   - até R$ 10.000,00 de valor de aquisição, a baixa vale na hora;
   - acima disso, ela espera a confirmação de quem tem `PATRIMONIO.CONFIRMAR_BAIXA` (o Gestor), que confirma ou recusa com o motivo;
   - quem pediu não confirma (422, segregação de funções);
   - na confirmação, as condições do bem são conferidas de novo.
9. **Efeitos da baixa:**
   - o bem fica baixado, e o histórico continua;
   - o termo de responsabilidade se encerra na data;
   - os planos de manutenção se desativam;
   - o veículo ligado fica inativo na frota (o veículo em serviço não sai, 422).

### Depreciação e painel

10. **Valor contábil estimado** (MCASP, linear, mês a mês):
    - vida útil de 5 anos para informática e veículo, 10 anos para as outras classes;
    - valor residual de 10%;
    - aparece na ficha do bem e no painel, só para leitura.
11. **Painel do Patrimônio** (`PATRIMONIO.CONSULTAR`), nas unidades visíveis:
    - os bens tombados, o valor de aquisição e o contábil, os ociosos, os a tombar, os inventários abertos e as baixas no ano;
    - por unidade: os bens, os valores e o último inventário, com o aviso de mais de 12 meses sem inventário;
    - por classe: os bens e os valores;
    - a manutenção dos 10 bens que mais custaram, com os dias parado;
    - as baixas à espera, com a confirmação e a recusa para a gestão.
12. **Sala de Situação:** um alerta para cada equipamento médico-hospitalar parado há mais de 7 dias (até 3), com o link para a aba
    Manutenção. A lista vem do resumo da manutenção.
13. **Tela,** no Patrimônio:
    - as abas **Inventários** (lista, abertura, conferência, achados e fechamento) e **Painel**;
    - na ficha do bem, o valor contábil, a baixa e o pedido de baixa;
    - a tela aceita `?aba=inventarios` e `?aba=painel`.
14. **Banco:** a `V98__patrimonio_inventario_e_baixa.sql` cria `tb_inventario_patrimonio`, `tb_membro_inventario`, `tb_item_inventario`,
    `tb_achado_inventario` e `tb_baixa_bem`.

## Alternativas descartadas

- **Baixar sozinho o não encontrado no fechamento:** a lei pede a apuração antes da baixa. O fechamento sugere, e a gestão decide com
  o processo.
- **Conferência só pelo Patrimônio:** a comissão é formada por servidores da unidade, sem permissão de Patrimônio. Exigir a permissão
  obrigaria a concedê-la a quem só confere.
- **Depreciação gravada todo mês:** seria uma rotina e um lançamento contábil antes do domínio 17. O valor estimado é derivado da data
  e do valor.
- **Baixa sem alçada:** um bem caro sairia do patrimônio com a assinatura de uma só pessoa.

## Consequências

- **O domínio 15 fica completo:** do bem comprado ao tombado, mantido, inventariado e baixado, com o histórico do começo ao fim.
- **O domínio 17 recebe** o valor contábil estimado e as baixas para o lançamento contábil.
- **Pendências:**
  - a etiqueta com código de barras e a conferência por leitor dependem da D3 de Documentos;
  - o termo de baixa e o relatório do inventário em PDF dependem da D2.

## Testes

- **JUnit `InventarioPatrimonioControllerTest`:**
  - o inventário: comissão repetida (400), a abertura com os bens, o segundo (409), a transferência congelada (422);
  - a conferência: sem conservação e sem local (400), setor de outra unidade (422), em outro setor, em outra unidade, não encontrado;
  - o fechamento: antes de tudo conferido (422), os achados, o setor corrigido, a transferência pendente, a baixa sugerida e a
    conferência depois do fechamento (422);
  - a baixa: data futura e furto sem boletim (400), devolução de bem doado (422), a baixa na hora (bem baixado, termo encerrado, plano
    desativado) e a repetida (422);
  - acima da alçada: a pendente, a repetida (409), a recusa, a confirmação e a confirmada de novo (422); com ordem de serviço aberta
    (422);
  - o painel: o valor contábil, os bens por unidade, o aviso sem inventário, os ociosos e as classes.
- **JUnit `InventarioPatrimonioAutorizacaoControllerTest`:**
  - a comissão vê, confere e não fecha; quem não é da comissão não vê nem confere (403);
  - o Patrimônio pede a baixa e não confirma (403);
  - o gestor não confirma a própria (422) e confirma a dos outros;
  - o painel só para quem consulta o Patrimônio; sem login, 401.
- **Robot de API:** `test/patrimonio/Inventario.robot`, CT-001 a CT-004.
- **Robot de tela:** `UI_patrimonio.robot`, CT-007 (inventário) e CT-008 (baixa e painel).
