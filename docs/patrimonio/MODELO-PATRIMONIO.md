# Patrimônio e Manutenção (domínio 15)

Desenho na [ADR-0215](../adr/0215-patrimonio-bens-tombamento-e-transferencia.md). Este documento acompanha o estado do domínio e é
atualizado em cada fatia.

## Fatias

| Fatia | Conteúdo | Estado |
|-------|----------|--------|
| PM1 | Bem, tombamento, termo de responsabilidade, transferência em duas etapas, bem permanente comprado a tombar, veículo da frota, "Meus bens" | ✅ ADR-0215, V96 |
| PM2 | Plano preventivo e calibração, chamado corretivo, ordem de serviço interna ou por contrato, peças pelo Estoque | ✅ ADR-0216, V97 |
| PM3 | Inventário anual com comissão, baixa, depreciação para leitura, painel e alerta na Sala de Situação | ⏳ |

## Entidades (PM1)

```text
BemPatrimonial   numeroTombamento (AAAA/000001) · descricao · classe · marca · modelo · numeroSerie · unidade · setor
                 situacao: A_TOMBAR · EM_USO · OCIOSO · EM_MANUTENCAO · BAIXADO
                 origem: COMPRA · DOACAO · CESSAO_COMODATO · TRANSFERENCIA_ENTE · valorAquisicao · dataAquisicao · documento
                 fornecedor e itemRecebimento (o bem comprado, ADR-0214) · veiculo (a frota, ADR-0178) · tombadoEm
  ├── ResponsavelBem       profissional · desde · ate (o atual sem "ate")
  └── TransferenciaBem     origem · destino · motivo · enviadaEm · situacao: PENDENTE · ACEITA · RECUSADA · CANCELADA · motivoRecusa
ItemSolicitacaoCompra / ItemContratacao  tipo BEM_PERMANENTE (descrito em texto, como o serviço)
```

## Entidades (PM2)

```text
PlanoManutencao          bem · tipo: PREVENTIVA · CALIBRACAO · SEGURANCA_ELETRICA · periodicidadeDias · ultimaExecucao · proximaEm · ativo
OrdemServicoManutencao   numero (AAAA/000001) · bem · plano · tipo (+ CORRETIVA) · defeito · bemParado · paradoDesde · abertaPorCpf
                         prioridade: BAIXA · NORMAL · ALTA · URGENTE · situacao: ABERTA · EM_EXECUCAO · CONCLUIDA · CANCELADA
                         execucao: INTERNA (tecnico) · CONTRATO (instrumento, ADR-0213) · situacaoBemAnterior
                         servicoRealizado · custoMaoDeObra · certificado · calibracaoValidaAte · motivoCancelamento
  └── PecaOrdemServico   material (do Estoque) ou descricao · quantidade · custoUnitario
```

## Regras

- **Tombamento:**
  - o número é sequencial no ano e dado pelo sistema;
  - o bem cadastrado à mão entra tombado e em uso;
  - o bem comprado nasce a tombar, um por unidade aceita, e é tombado com a classe, o setor e o responsável.
- **Responsável:** todo bem tombado tem um. A troca encerra o termo anterior, e a mesma pessoa não troca.
- **Uso:** por aqui, só em uso ou ocioso. A manutenção (PM2) e a baixa (PM3) têm registro próprio.
- **Transferência:**
  - a origem envia o bem em uso ou ocioso, uma transferência por vez;
  - o destino aceita, com o setor e o novo responsável, ou recusa;
  - a origem cancela enquanto está pendente;
  - o bem só muda de unidade no aceite.
- **Veículo:** só o bem da classe veículo, com um veículo da mesma unidade, e um veículo por bem.
- **Plano:** um ativo por tipo; a ordem do plano vencido nasce na leitura, uma por vencimento; a conclusão e o cancelamento contam a
  próxima data a partir de hoje.
- **Chamado:** qualquer pessoa logada abre, do bem tombado e em operação; quem abriu acompanha em "Meus chamados".
- **Ordem de serviço:**
  - a triagem dá a prioridade;
  - o início é interno (com o técnico) ou por contrato vigente com item de serviço; o bem e o veículo ficam em manutenção;
  - a conclusão registra o serviço, a mão de obra e as peças (as do Estoque saem da unidade do bem); a calibração pede o certificado e
    a validade;
  - o cancelamento devolve o bem e o veículo.

## Acesso

| Permissão | Papéis padrão | O que libera |
|-----------|---------------|--------------|
| `PATRIMONIO.CONSULTAR` | Patrimônio, Gestor | Ver os bens e as transferências das unidades no escopo |
| `PATRIMONIO.GERENCIAR` | Patrimônio, Gestor | Cadastrar, tombar, corrigir, trocar o responsável, mudar o uso, transferir, aceitar e ligar o veículo |
| `MANUTENCAO.EXECUTAR` | Manutenção, Gestor | Planos, triagem, execução, conclusão e cancelamento das ordens; resumo e contratos de serviço |
| (login) | Todos | "Meus bens", a ficha dos próprios bens, o chamado e "Meus chamados" |

## Tela

Tela **Patrimônio** (Administrativo):

- **Bens:** a lista com os filtros por unidade, classe e situação e a busca por tombamento, descrição ou série. O cadastro e a ficha
  abrem em gaveta; a ficha tem as ações e o histórico dos responsáveis e das transferências.
- **A tombar:** os bens comprados à espera do número, com o fornecedor, a nota e o valor.
- **Transferências:** as enviadas e as recebidas, com o aceite, a recusa e o cancelamento.
- **Manutenção** (com `MANUTENCAO.EXECUTAR`): o resumo, os planos vencendo, as calibrações vencidas e as ordens, com a gaveta da
  ordem (triagem, início, conclusão com as peças, cancelamento). Na ficha do bem: o chamado, os planos, as ordens, o custo e os dias
  parado.
- **Meus bens:** os bens sob a responsabilidade de quem está logado e "Meus chamados". Quem não tem permissão de Patrimônio vê só esta aba, e o menu
  ganha o item "Meus bens" quando há algum.
