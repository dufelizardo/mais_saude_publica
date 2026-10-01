# 0058 — Tela da Farmácia: abas, gaveta lateral e livro de estoque

## Status

Aceita e implementada.

## Contexto

O backend da Farmácia (domínio #9) tinha quatro grupos de endpoints — `Medicamento` (ADR-0049),
`Lote` (ADR-0050), `Dispensacao` (ADR-0051) e o livro de movimentação (ADR-0057) — e nenhuma tela.
O usuário trouxe um mockup (`Farmacia.html`, mesma família do design system de `modelo_front/`)
desenhado já em cima desses endpoints: uma página única com quatro cards de resumo, abas em pílula
(Estoque por lote, Dispensações, Medicamentos, Livro de estoque) e **gavetas laterais** (painel à
direita) para cada formulário.

Comparando o mockup com a API, cinco pontos precisavam de decisão:

1. A lista de dispensações mostra o CPF do paciente, mas a resposta da dispensação só trazia o nome.
2. O mockup prevê quatro tipos de lançamento no livro e não tem `SALDO_INICIAL` — o primeiro
   lançamento de todo lote que existia antes da ADR-0057.
3. A coluna "Detalhe" do extrato mostra o paciente de uma dispensação, mas o livro só guarda o id
   da dispensação.
4. O mockup tem elementos de protótipo voltados a quem desenvolve: selo "dev · Bearer ativo", o
   método e a rota em cada gaveta (`POST /api/v1/...`) e um aviso com o código HTTP a cada ação.
5. A dispensação escolhe o paciente numa lista fixa de sete nomes.

## Decisão

1. **Uma rota só, `assistencia/farmacia`**, filha do `AppShell`, com item "Farmácia" no grupo
   Assistência do menu. As quatro seções do mockup viram abas da mesma tela (`role="tablist"`,
   navegação por setas), não rotas separadas — o mockup liga as seções entre si (o botão de extrato
   na linha do lote abre a aba Livro já no lote certo; dispensar e registrar perda partem da linha).
2. **Gaveta lateral como componente compartilhado** (`shared/drawer`, mesmo formato do `Modal`:
   `titulo` + `(fechar)`, Esc, clique fora, foco no primeiro campo, foco preso dentro da gaveta e
   devolvido ao fechar). O conteúdo projetado usa `.dw-form`/`.dw-body`/`.dw-foot` globais, para que
   o `<form>` envolva corpo e rodapé. É a primeira tela com gaveta; o `Modal` continua sendo o padrão
   das telas de cadastro simples.
3. **Os cinco pontos acima:**
   - `pacienteCpf` entra no `DispensacaoResponseDto` (JUnit e Robot ajustados).
   - `SALDO_INICIAL` ganha rótulo "Saldo inicial" e cor roxa (`.badge--purple`), distinta dos quatro
     tipos do mockup.
   - O paciente da dispensação no extrato é cruzado na própria tela com a lista de dispensações,
     sem mudar o livro.
   - Os elementos de protótipo saem. A mensagem de sucesso fica, sem código HTTP nem rota; o erro da
     API aparece no topo da gaveta com a mensagem do backend.
   - A escolha de paciente usa uma busca por nome ou CPF que filtra a lista (no máximo 50 por vez),
     em vez de mostrar a base inteira.
4. **Saldos vêm do backend.** O saldo do lote é `Lote.quantidade` e o saldo de cada linha do extrato
   é `saldoApos` — a tela não recalcula o livro. Cards de resumo, "saldo total" por medicamento e o
   filtro "vence em até 90 dias" são contas feitas na tela sobre as listas já carregadas.
5. **Listas vazias.** Os endpoints de listagem respondem 404 quando não há registros; a tela trata
   isso como lista vazia, como as demais.
6. **Nomes de classe que colidiam foram trocados** ao portar o CSS do mockup para `styles.css`:
   as abas em pílula viraram `.page-tabs` (`.tabs` continua sendo a aba sublinhada do painel de
   detalhe, ADR-0052) e os campos da gaveta viraram `.dw-field`/`.dw-row`/`.dw-hint`/`.dw-err`
   (`.field` é da tela de Login, ADR-0055). Os botões seguem o desvio já documentado em
   `docs/frontend/PADRAO-TELAS-INTERNAS.md` (`.btn` preenchido para a ação principal, `.btn--ghost`
   para as demais). O card que envolve a tabela não usa `overflow: hidden` — a tabela rola dentro de
   `.tbl-wrap`.

## Trade-offs considerados

**Uma tela com abas (escolhida)** × **quatro rotas separadas**
- ✅ Fiel ao mockup e ao fluxo real do balcão (ver o lote, dispensar, conferir o extrato sem trocar
  de página).
- ❌ Um componente maior que as outras telas de Assistência. Aceitável por ora; se crescer (ex.:
  transferência entre unidades), cada aba pode virar um componente filho sem mudar a rota.

**Gaveta lateral (escolhida)** × **reaproveitar o `Modal`**
- ✅ Formulários com mais campos (dispensação, perda/ajuste) cabem sem rolagem apertada e a tabela
  continua visível ao lado.
- ❌ Um segundo padrão de diálogo no projeto. Fica registrado aqui e em `PADRAO-TELAS-INTERNAS.md`
  quando usar cada um.

**Busca de paciente filtrando uma lista carregada (escolhida)** × **busca no servidor**
- ✅ Usa o `GET /api/v1/paciente/` que já existe, sem endpoint novo.
- ❌ Carrega todos os pacientes ao abrir a tela. Quando a base crescer, vale um endpoint de busca
  paginada — o mesmo vale para a tela de Agendamentos, que hoje lista todos num `<select>`.

## Consequências

**Positivas:** a Farmácia passa a ser operável pela interface — cadastro de medicamentos, entrada e
correção de lotes, dispensação, perda, ajuste de inventário e extrato de cada lote.

**Negativas / pendências:**
- O bundle inicial cresceu cerca de 75 KB (as rotas do projeto são carregadas de uma vez; o aviso
  de orçamento já existia antes desta tela).
- A matrícula do profissional ainda é digitada à mão, como nas outras telas de Assistência; com o
  login ligado, dá para sugerir a do usuário autenticado.
- ~~**Transferência entre unidades**~~ — feita (ADRs 0059 e 0061), com tela no adendo abaixo.

## Adendo — aba Transferências (ADRs 0059 e 0061)

A transferência entre unidades ganhou tela seguindo as decisões acima, sem mockup próprio:

- **Nova aba "Transferências"** entre Dispensações e Medicamentos. O contador da aba mostra quantas
  estão **em trânsito**, não o total — é o que pede ação. Abre filtrada por "Em trânsito"; os filtros
  são situação (em trânsito, recebidas, com divergência, canceladas, todas) e unidade de destino, que
  funciona como a lista "a receber" de uma unidade.
- **Botão "Transferir"** na linha do lote, desabilitado sem saldo ou com o lote vencido (lote vencido
  vai para perda, ADR-0059).
- **Três gavetas novas**, no padrão das demais:
  - *Transferir*: lote de origem, unidade de destino (a unidade do lote fica de fora da lista),
    quantidade, matrícula de quem envia e observação; aviso de que a quantidade sai agora e fica em
    trânsito.
  - *Conferir recebimento*: resumo do envio, quantidade que chegou (já preenchida com a enviada),
    matrícula de quem conferiu e a conta enviada × recebida × não chegou. Motivo e "o que aconteceu"
    só aparecem, obrigatórios, quando chegou menos. A tela já recusa a matrícula de quem enviou; o
    backend confirma (422).
  - *Cancelar transferência*: resumo, aviso de que a quantidade volta à origem, motivo e matrícula.
- **Detalhe da transferência** com as três etapas (envio, recebimento, cancelamento) e seus
  responsáveis.
- O componente continua um só (ver "Trade-offs"): as transferências usam as mesmas listas de lotes e
  unidades e as mesmas gavetas das outras abas. Se a Farmácia ganhar mais uma área desse porte, é o
  momento de separar cada aba em componente filho.

## Referências

- [ADR-0057](./0057-livro-de-movimentacao-do-estoque-da-farmacia.md) — livro de movimentação que a
  aba Livro de estoque exibe.
- [ADR-0051](./0051-dispensacao-terceira-entidade-da-farmacia.md) — dispensação, que ganha
  `pacienteCpf` na resposta.
- [ADR-0052](./0052-tela-de-pacientes-lista-mais-painel-de-detalhe.md) — tiras de KPI e padrão de
  portar mockup reaproveitados aqui.
- [`docs/frontend/PADRAO-TELAS-INTERNAS.md`](../frontend/PADRAO-TELAS-INTERNAS.md) — padrão de
  telas internas, atualizado com a gaveta lateral.
