# 0200 — Vigilância Sanitária: auto de infração, processo administrativo sanitário e medidas (V3b)

## Status

Aceita e implementada. Segunda parte da fatia **V3** do domínio 23 ([ADR-0110](./0110-vigilancia-em-saude.md), seção 4), depois da
V3a ([ADR-0199](./0199-vigilancia-sanitaria-fundacao.md)). Falta a V3c (violência levada à rede intersetorial).

## Contexto

- **A V3a** registra a inspeção e a irregularidade com o prazo de correção, mas a VISA não tinha como punir: faltava o rito da Lei
  6.437/1977 (auto, defesa, decisão, recurso) e as medidas que valem na hora (interdição cautelar e apreensão).
- **O rito tem prazos de lei:** 15 dias da ciência do auto para a defesa (art. 22) e 15 dias da ciência da decisão para o recurso
  (art. 30), que tem efeito suspensivo. A interdição cautelar vai até 90 dias (art. 23, §4º).
- **Os atos são documentos formais:** o autuado, o Ministério Público e o Judiciário precisam conferir que o termo é autêntico.

## Decisão

1. **O auto de infração abre o processo, no mesmo registro** (`TB_PROCESSO_SANITARIO`):
   - número do ano, estabelecimento, inspeção de origem (opcional, do mesmo estabelecimento, 422), dispositivo legal, descrição, data
     da lavratura (não futura nem anterior à inspeção, 400) e os fiscais (`TB_PROCESSO_SANITARIO_FISCAL`);
   - as irregularidades são abertas e do estabelecimento (422) e passam a AUTUADA, ligadas ao processo (`processo_id`);
   - a gravidade é a maior das irregularidades e pode ser agravada, nunca reduzida (400);
   - com login, o fiscal logado está entre os fiscais (403) e assina: o CPF e o SHA-256 do texto canônico do auto.
   - O desenho previa o auto e o processo em tabelas separadas; ficaram juntos porque há sempre um processo por auto e nenhum dado
     do auto vive fora do processo.
2. **As fases**, com os prazos calculados e não guardados:
   - **AGUARDANDO_CIENCIA:** a ciência registra a data (não antes da lavratura, 400) e a forma: pessoal, por AR, por edital ou recusa
     certificada pelo fiscal;
   - **EM_DEFESA:** prazo de 15 dias da ciência. A defesa depois do prazo é recebida como **intempestiva** enquanto não houver
     decisão; sem defesa e com o prazo vencido, é **revelia**;
   - **AGUARDANDO_DECISAO** → a decisão, pela autoridade julgadora (`VISA.JULGAR`), com a defesa ou à revelia; antes do prazo, 422:
     - **arquivamento** (sem penalidade, 400): encerra o processo e devolve as irregularidades às abertas, para o retorno acompanhar;
     - **penalidade**: as atenuantes (art. 7º) e as agravantes (art. 8º) marcadas, a fundamentação e ao menos uma penalidade (400);
   - **DECIDIDO:** a ciência da decisão abre o recurso de 15 dias. Fora do prazo, o recurso não é conhecido (422);
   - **EM_RECURSO** → o julgamento: provido (revoga tudo e reabre as irregularidades), parcialmente provido (as que ficam: ao menos uma e
     não todas, 400) ou improvido;
   - **ENCERRADO:** pelo julgamento do recurso, ou pela coordenação depois do prazo de recurso sem recurso (422 antes).
3. **Penalidades** (`TB_PENALIDADE_SANITARIA`), do art. 2º: advertência, multa (com valor, 400; sem tabela, porque cada município tem o
   seu código sanitário), apreensão, inutilização, interdição parcial ou total, suspensão de vendas, suspensão da licença (com a data
   final, depois da decisão, 400) e cancelamento da licença. Não vão juntas a interdição parcial e a total, nem a suspensão e o
   cancelamento (400); suspender ou cancelar pede licença vigente (422).
   - **Só valem no encerramento** (o recurso tem efeito suspensivo): a suspensão deixa a licença SUSPENSA até a data (`suspensa_ate`;
     depois disso a situação calculada volta a vigente ou vencida); o cancelamento a deixa CASSADA; a interdição gera a medida
     INTERDICAO com o termo.
4. **Medidas** (`TB_MEDIDA_SANITARIA`), imediatas, com número do ano:
   - **interdição cautelar** total ou parcial, até 90 dias do início (90 por padrão; além disso, 400);
   - **apreensão** com produto e quantidade (400), lote e local de guarda;
   - a **interdição** definitiva só vem da decisão (400 pela rota das medidas);
   - o encerramento (só a vigente, 422): a **desinterdição**, com termo, ou a **destinação** do apreendido;
   - interdição vigente impede licença nova (422) e aparece na Sala de Situação; a cautelar a 10 dias do fim, ou vencida, nas pendências.
5. **Termos em PDF com verificação pública** (`TB_TERMO_SANITARIO`):
   - auto de infração, decisão, julgamento do recurso, interdição, apreensão e desinterdição, gerados no servidor (OpenPDF);
   - cada página leva o código (`XXXX-XXXX-XXXX`) e o QR code de `/verificar-visa?codigo=`, como o laudo da
     [ADR-0183](./0183-laudo-pdf-assinado-e-insumos.md);
   - o PDF fica como documento (tipo `TERMO_SANITARIO`) do processo (dono novo `PROCESSO_SANITARIO`) ou, na medida sem processo, do
     estabelecimento; o termo guarda o SHA-256 do arquivo;
   - **verificação sem login:** `publico/visa/verificacao/{codigo}` dá tipo, número, data, razão social, o documento (o CPF da pessoa
     física mascarado), o hash e se o arquivo guardado confere; `publico/visa/licenca?numero=` dá a situação da licença, como o alvará
     afixado. Nenhum dado de pessoa.
6. **Pendências:** ciência do auto ou da decisão por registrar, revelia, decisão parada há mais de 30 dias depois da defesa, prazo de
   recurso vencido (a encerrar), cautelar perto do fim ou vencida.
7. **No #19:** o relatório `VIGILANCIA_SANITARIA` (categoria Vigilância), com inspeções por motivo e resultado, licenças pela situação
   atual, denúncias, autos por gravidade, processos em andamento por fase, penalidades que passaram a valer no período (com a soma das
   multas) e as interdições vigentes. A VISA é municipal: sem recorte por unidade; vale a permissão dos indicadores.
8. **Acesso:** permissão nova `VISA.JULGAR` (decisão e julgamento do recurso), só na Coordenação da VISA. O fiscal lavra o auto,
   registra a ciência e as medidas; a coordenação registra a defesa, o recurso, o encerramento e a desinterdição; as interdições
   vigentes também para quem tem `INDICADOR.CONSULTAR`.
9. **Tela:** as abas **Processos** (lista por fase e o processo em gaveta, com o andamento, as penalidades, os termos e a ação da fase
   para quem pode) e **Medidas** (interdição cautelar, apreensão, desinterdição e destinação); "Lavrar auto de infração" no detalhe do
   estabelecimento com irregularidade aberta; a página pública **Verificar documento da vigilância sanitária**.

### API

| Método | Caminho (`/api/v1/vigilancia-sanitaria/`) | Permissão |
|---|---|---|
| GET | `processos` (`estabelecimentoId`, `fase`), `processo/{id}`, `medidas` (`estabelecimentoId`, `situacao`) | `VISA.INSPECIONAR`, `VISA.GERENCIAR` ou `VISA.JULGAR` |
| POST | `processo` (o auto) | `VISA.INSPECIONAR` |
| POST | `processo/{id}/ciencia` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST | `processo/{id}/defesa`, `processo/{id}/recurso`, `processo/{id}/encerramento` | `VISA.GERENCIAR` |
| POST | `processo/{id}/decisao`, `processo/{id}/julgamento-recurso` | `VISA.JULGAR` |
| POST | `medida` | `VISA.INSPECIONAR` |
| POST | `medida/{id}/encerramento` | `VISA.GERENCIAR` |
| GET | `interdicoes` | as da VISA ou `INDICADOR.CONSULTAR` |
| GET | `/api/v1/publico/visa/verificacao/{codigo}`, `/api/v1/publico/visa/licenca?numero=` | sem login |
| GET | `/api/v1/relatorios/VIGILANCIA_SANITARIA` (e `/exportacao`) | `INDICADOR.CONSULTAR` |

### Migração

`V83__processo_sanitario.sql`: o processo e os fiscais, as penalidades, as medidas, os termos, `processo_id` na irregularidade,
`suspensa_ate` na licença, o dono `PROCESSO_SANITARIO` no CHECK do documento e o tipo de documento `TERMO_SANITARIO`.

## Alternativas descartadas

- **Aplicar a penalidade na decisão:** contraria o efeito suspensivo do recurso (art. 30); a penalidade espera o encerramento.
- **Rotina agendada para mudar a fase no fim do prazo:** os prazos se calculam da ciência; a revelia e o prazo vencido aparecem nas
  pendências, e o encerramento é ato da coordenação.
- **Tabela de valores de multa:** os valores e as faixas são do código sanitário de cada município; o sistema registra o valor decidido.
- **Cobrança da multa:** é da Fazenda (dívida ativa), fora do domínio.

## Consequências

- **STATUS-DOS-DOMINIOS:** V3b marcada; o domínio 23 segue 🟡 até a V3c e a V5.
- **A licença pode voltar sozinha:** a suspensa com data volta a valer depois dela, pela situação calculada.
- **Fora do escopo:** assinatura ICP-Brasil (domínio 20); notificação do autuado por e-mail; integrações com SINAVISA e REDESIM.

## Testes

- **JUnit `ProcessoSanitarioControllerTest`:**
  - o auto (irregularidade de outro estabelecimento, 422; gravidade reduzida, 400; irregularidade já autuada, 422), o termo e a
    verificação pública (íntegro; código desconhecido, 404);
  - nada antes da ciência (422); a ciência antes da lavratura (400); a defesa intempestiva; as regras das penalidades (400); a
    decisão com multa, suspensão e interdição parcial; a licença ainda vigente antes do encerramento;
  - o recurso antes da ciência da decisão e fora do prazo (422); o encerramento: licença suspensa, interdição parcial vigente,
    licença nova recusada (422), a interdição na lista da Sala de Situação; o relatório com a soma das multas;
  - a revelia (decisão antes do prazo, 422; depois, aceita), o recurso no prazo, o parcialmente provido (400 com todas; o cancelamento
    revogado não cassa a licença) e o termo do julgamento;
  - o arquivamento que reabre as irregularidades;
  - as medidas: interdição pela rota das medidas (400), cautelar além de 90 dias (400), apreensão sem produto (400), a licença recusada
    com interdição, a cautelar perto do fim nas pendências, a destinação, a desinterdição com o termo (antes do início, 400; repetida,
    422) e a licença verificada pelo número.
- **JUnit `ProcessoSanitarioAutorizacaoControllerTest`,** com a segurança ligada: só o fiscal com a própria matrícula lavra (a
  recepção, o gestor e a coordenação, 403); o fiscal registra a ciência e não julga; o gestor não julga; a coordenação julga e
  desinterdita; o gestor lê as interdições e a recepção não; a verificação pública sem login e o processo sem login (401).
- **`RelatorioControllerTest` e `RelatorioAutorizacaoControllerTest`** com o nono relatório.
- **Robot:** `ProcessoSanitario.robot` (CT-001 a CT-004); `Relatorios.robot` com os nove relatórios; CT-004 a CT-006 de
  `UI_vigilancia_sanitaria.robot` (auto e ciência, interdição cautelar, verificação pública da licença); CT-007 de `UI_relatorios.robot`.
