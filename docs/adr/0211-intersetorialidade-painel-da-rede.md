# 0211 — Intersetorialidade: painel da rede (IS2)

## Status

Aceita e implementada. Segunda fatia do item 31 do mapa, desenhado na [ADR-0154](./0154-intersetorialidade.md). Fecha o
domínio: o uso pelos outros domínios já estava feito na Saúde Mental ([ADR-0155](./0155-saude-mental-intersetorialidade-e-indicadores.md)),
nas Emergências ([ADR-0193](./0193-emergencias-vulneraveis-e-deslocados.md)), na violência
([ADR-0201](./0201-violencia-comunicacao-intersetorial.md)) e na saúde do trabalhador ([ADR-0208](./0208-vigilancia-investigacao-do-trabalho.md)).

## Contexto

- **Cada domínio vê os próprios encaminhamentos, mas ninguém vê a rede.** A gestão não sabe quais instituições recebem os
  casos, quais respondem e quais deixam o paciente sem retorno. A ADR-0154 deixou esse painel para depois.
- **Os encaminhamentos de violência e de saúde mental são sigilosos.** Quem mantém a rede (gestor, assistente social) não
  precisa saber quem é o paciente para cobrar uma instituição: basta o número do encaminhamento e as datas.
- **"Sem retorno" já é derivado** (ADR-0154): em aberto depois do retorno esperado, sem rotina agendada.

## Decisão

1. **O painel** (`GET intersetorial/painel`), com os filtros período, setor, origem e unidade:
   - **do período (pela data de envio):** enviados, concluídos, recusados, cancelados, a taxa de resolução (concluídos sobre
     concluídos mais recusados) e o tempo médio do envio ao retorno, em dias;
   - **de hoje, de qualquer data de envio:** em aberto e sem retorno, porque é o que se cobra da instituição;
   - **por instituição:** os mesmos números e os enviados por origem (Saúde Mental, Emergências, Vigilância), da que tem
     mais casos sem retorno para a que tem menos;
   - **período:** sem datas, os últimos 90 dias; início depois do fim ou mais de 366 dias, 400.
2. **A lista de cobrança** (`GET intersetorial/instituicao/{id}/sem-retorno`): os encaminhamentos sem retorno da instituição,
   do mais atrasado ao menos, com o número (os 8 primeiros caracteres do identificador), a origem, a unidade, a situação
   (enviado ou recebido), as datas e os dias de atraso. Instituição inexistente, 404.
3. **Sem paciente e sem motivo** nas duas respostas. Quem encaminhou continua vendo o caso no próprio domínio, com as
   regras de acesso de lá.
4. **Acesso:** `INTERSETORIAL.GERENCIAR` (Gestor e Assistente social), a mesma permissão que mantém o catálogo. O catálogo
   segue aberto a quem está logado.
5. **Tela:** a aba **Painel** em Administrativo · Rede intersetorial, ao lado de **Instituições**, só para quem gerencia:
   - **cartões:** enviados, em aberto, sem retorno, taxa de resolução e tempo médio;
   - **tabela por instituição;**
   - **gaveta "Cobrar":** a lista de cobrança, com o contato da instituição.

   A aba abre direto com `?aba=painel`.
6. **Sala de Situação:** um alerta para cada instituição com 3 ou mais encaminhamentos sem retorno (até 3 alertas), com o
   botão "Ver no painel". Sem a permissão, o alerta não aparece.
7. **Banco:** os números são calculados das tabelas da IS1. A `V92__intersetorial_painel.sql` só cria o índice
   `(instituicao_id, situacao)`, usado pela lista de cobrança.

## Alternativas descartadas

- **Mostrar o paciente para o gestor:** quebraria o sigilo da violência e da saúde mental sem necessidade, porque a cobrança
  se faz pelo número.
- **Guardar "sem retorno" numa coluna atualizada por rotina:** a ADR-0154 já decidiu derivar; uma rotina só criaria atraso
  e trava entre réplicas (ADR-0209).
- **Painel no BI (domínio 19):** o BI olha a produção da rede de saúde; a cobrança da rede intersetorial é tarefa de quem
  mantém o catálogo, na mesma tela.

## Consequências

- **O domínio 31 fica completo.** Seguem fora: a guia de encaminhamento para imprimir (depende da D3 de Documentos) e a
  integração com o CadÚnico e o prontuário SUAS (domínio 20, adiado).
- **O número do encaminhamento** passa a ser a referência para falar com a instituição. São os 8 primeiros caracteres do
  identificador: curto para ditar ao telefone, e a repetição entre encaminhamentos da mesma instituição é improvável no volume
  de um município.

## Testes

- **JUnit `PainelIntersetorialControllerTest`:**
  - as contas do período e as de hoje: o encaminhamento antigo ainda em aberto entra em "sem retorno" e não em "enviados";
  - a taxa e o tempo médio;
  - a ordem das instituições e os enviados por origem;
  - os filtros por setor, origem e período, e o período inválido (400);
  - a lista de cobrança em ordem de atraso, a instituição inexistente (404);
  - nenhum dado do paciente nem o motivo nas respostas.
- **JUnit `PainelIntersetorialAutorizacaoControllerTest`:** o gestor e o assistente social veem; a recepção e o psicólogo, 403
  (e continuam lendo o catálogo); sem login, 401.
- **Robot de API:** `test/intersetorialidade/Intersetorialidade.robot`, CT-011 a CT-014.
- **Robot de tela:** `test/ui/administrativo/instituicoes_externas/UI_instituicoes_externas.robot`, CT-004 (painel e
  cobrança) e CT-005 (o alerta da Sala de Situação leva ao painel).
