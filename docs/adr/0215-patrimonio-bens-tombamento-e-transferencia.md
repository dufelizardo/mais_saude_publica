# 0215 — Patrimônio e Manutenção: desenho do domínio, bens, tombamento e transferência (PM1)

## Status

Aceita e implementada (PM1). Desenho do domínio 15 e primeira fatia. A PM2 (manutenção) está na
[ADR-0216](./0216-patrimonio-manutencao.md) e a PM3 (inventário, baixa e painel) na
[ADR-0217](./0217-patrimonio-inventario-baixa-e-painel.md); o domínio está completo.

## Contexto

- **Não havia cadastro de bens.** Só existia o rótulo `PATRIMONIO`, reservado no catálogo de capacidades (ADR-0032).
- **O que já existia guardava o bem em texto:**
  - a frota (ADR-0178) tem o veículo com a situação "manutenção", sem o bem patrimonial por trás;
  - o exame de imagem guarda o aparelho em texto (ADR-0182).
- **Compras não comprava bem permanente:** só material, medicamento e serviço (ADR-0212). O equipamento comprado não tinha para onde ir
  depois do recebimento (ADR-0214).
- **Controle patrimonial na administração pública:**
  - o bem permanente recebe um número de tombamento (plaqueta) e fica sob a guarda de um responsável que assina o termo de
    responsabilidade;
  - a troca de unidade passa pela entrega e pelo aceite de quem recebe;
  - o inventário é anual (Lei 4.320/1964, arts. 94 a 96);
  - a depreciação segue o MCASP.
- **A manutenção de equipamentos de saúde** segue a RDC 509/2021 (gestão de tecnologias em saúde): plano de manutenção, calibração e
  registro das intervenções.

## Decisão

### O domínio em três fatias

| Fatia | Conteúdo |
|-------|----------|
| **PM1** (esta) | Bem patrimonial, tombamento, termo de responsabilidade com histórico, transferência em duas etapas, bem permanente comprado que nasce a tombar, vínculo com o veículo da frota, "Meus bens" |
| **PM2** | Plano preventivo (preventiva, calibração, segurança elétrica) com a ordem gerada na leitura; chamado corretivo; ordem de serviço interna ou por contrato da CP2; peças pelo Estoque; o bem e o veículo em manutenção; calibração vencida |
| **PM3** | Inventário anual por unidade com comissão e conferência; baixa com motivo e processo; depreciação linear só para leitura; painel e alerta de equipamento parado na Sala de Situação |

### PM1

1. **Bem patrimonial:**
   - descrição, classe (equipamento médico-hospitalar, informática, mobiliário, veículo, eletrodoméstico, outro), marca, modelo e
     número de série;
   - a unidade e, opcionalmente, o setor dela (422 de outra unidade);
   - a aquisição: a origem (compra, doação, cessão ou comodato, transferência de outro ente), a data (não futura, 400), o valor e o
     documento;
   - a **situação**: a tombar, em uso, ocioso, em manutenção (PM2) ou baixado (PM3).
2. **Tombamento:** o número `AAAA/000001`, sequencial no ano, dado no tombamento.
   - **O bem cadastrado à mão** (doação, cessão, transferência de outro ente ou o acervo antigo) já entra tombado e em uso.
   - **O bem comprado** chega a tombar (próximo item) e ganha o número quando o Patrimônio completa a classe, a identificação, o setor e
     o responsável. Tombar de novo dá 422.
3. **Bem permanente em Compras:**
   - um tipo novo de item, `BEM_PERMANENTE`, descrito em texto como o serviço, na solicitação e na contratação;
   - no recebimento definitivo (ADR-0214), **cada unidade aceita vira um bem a tombar** na unidade da ordem, com o fornecedor, o preço
     do contrato e a nota. A entrada passa por um método interno do Patrimônio, porque quem atesta já foi conferido pelo recebimento;
   - o bem permanente não pede lote nem validade, e não entra em serviço contínuo.
4. **Termo de responsabilidade:**
   - todo bem tombado tem um responsável, que é um profissional ativo (404/422);
   - a troca encerra o termo anterior na data e abre o novo; a mesma pessoa dá 422;
   - o histórico fica na ficha, como os outros dados temporais.
5. **Uso:** o bem tombado fica em uso ou ocioso. As outras situações têm o próprio registro (400), e o bem a tombar, em manutenção ou
   baixado não muda por aqui (422).
6. **Transferência entre unidades, em duas etapas** (como a da Farmácia, ADR-0061):
   - **origem:** quem gerencia o Patrimônio da unidade do bem envia, com o motivo. Só o bem em uso ou ocioso (422), para outra unidade
     (400), uma transferência por vez (409);
   - **destino:** quem gerencia o Patrimônio da unidade de destino aceita, com o setor do destino (422) e o novo responsável, ou recusa
     com o motivo;
   - **cancelamento:** a origem desiste;
   - o bem só muda de unidade no aceite. Se o veículo ligado for de outra unidade, o vínculo se desfaz.
7. **Veículo da frota:** o bem da classe veículo (422) se liga a um veículo da frota da mesma unidade (422), e o veículo é de um bem só
   (409).
8. **Acesso:**

   | Permissão | Papéis padrão | O que libera |
   |-----------|---------------|--------------|
   | `PATRIMONIO.CONSULTAR` | Patrimônio (novo), Gestor | Lista, ficha e transferências das unidades no escopo |
   | `PATRIMONIO.GERENCIAR` | Patrimônio, Gestor | Cadastro, tombamento, correção, responsável, uso, transferência e veículo, na unidade do bem (ou do destino, no aceite) |
   | (login) | Todos | "Meus bens" e a ficha dos bens sob a própria responsabilidade |

   O papel novo **Patrimônio** também consulta a frota.
9. **Tela Patrimônio** (Administrativo):
   - abas **Bens** (filtros por unidade, classe, situação e busca), **A tombar**, **Transferências** e **Meus bens**;
   - a ficha em gaveta, com as ações e o histórico dos responsáveis e das transferências;
   - quem não tem permissão de Patrimônio vê só "Meus bens". O menu ganha **"Meus bens"** para essas pessoas quando há algum bem.
10. **Banco:** a `V96__patrimonio_bens_e_tombamento.sql` cria `tb_bem_patrimonial`, `tb_responsavel_bem` e `tb_transferencia_bem`. Também
    acrescenta `BEM_PERMANENTE` aos tipos de item da solicitação e da contratação.

## Alternativas descartadas

- **Bem permanente como material do Estoque:** o material é contado por quantidade, e o bem é individual (número, série,
  responsável). Somar cadeiras no saldo apagaria a identificação de cada uma.
- **Tombar no recebimento definitivo:** o fiscal que atesta a entrega não é o setor de patrimônio, e a plaqueta, o setor e o
  responsável costumam ser definidos depois. O "a tombar" separa as duas etapas sem perder o vínculo com a nota.
- **Transferência direta, sem aceite:** o termo de responsabilidade passaria para outra unidade sem que ela confirmasse o recebimento
  físico.
- **Número de tombamento informado à mão:** a plaqueta pode ser impressa a partir do número gerado (D3 de Documentos). Um número livre
  abriria colisões e lacunas sem explicação.

## Consequências

- **O caminho do bem fica completo:** do pedido de compra ao bem tombado na unidade, com o fornecedor, a nota e o preço.
- **A PM2 parte deste cadastro:** a manutenção liga a ordem de serviço ao bem e põe o veículo da frota em manutenção pelo vínculo
  feito aqui.
- **Pendências:**
  - a etiqueta com código de barras depende da D3 de Documentos;
  - o aparelho do exame de imagem continua em texto até ser ligado a um bem;
  - o lançamento contábil e a depreciação contábil são do domínio 17.

## Testes

- **JUnit `PatrimonioControllerTest`:**
  - o cadastro: aquisição futura (400), setor de outra unidade, responsável inativo (422) e inexistente (404); o número, a situação e o
    responsável;
  - a correção, a busca e o filtro por classe;
  - a troca do responsável (a mesma pessoa, 422; o termo anterior encerrado) e o uso (400 para outra situação);
  - o veículo: bem que não é da classe (422) e veículo de outro bem (409);
  - a transferência: destino igual à origem (400), duas ao mesmo tempo (409), a recusa, o cancelamento, o aceite com setor de outra
    unidade (422) e o aceite que muda a unidade, o setor e o responsável;
  - o bem permanente: sem descrição (400); a compra inteira até o definitivo, os bens a tombar com o fornecedor, a nota e o preço; o
    bem a tombar não troca de responsável nem é transferido (422); o tombamento e o repetido (422).
- **JUnit `PatrimonioAutorizacaoControllerTest`:**
  - o papel Patrimônio cadastra na própria unidade e não na outra (403);
  - outro profissional não cadastra nem vê (403);
  - o responsável vê o bem em "Meus bens" e na ficha;
  - só o destino aceita; a origem não aceita e o destino não cancela (403);
  - depois do aceite, a origem e o responsável anterior perdem o acesso; sem login, 401.
- **Robot de API:** `test/patrimonio/Patrimonio.robot`, CT-001 a CT-004.
- **Robot de tela:** `test/ui/administrativo/patrimonio/UI_patrimonio.robot`, CT-001 a CT-004 (menu, cadastro, transferência com
  aceite e tombamento do bem comprado).
