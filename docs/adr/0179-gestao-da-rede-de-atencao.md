# 0179 — Gestão da Rede de Atenção

## Status

Aceita. Desenho do domínio 30, reservado pela [ADR-0115](./0115-modelo-operacional-dos-equipamentos.md), e fatia **GR1** implementada;
GR2 e GR3 só com aprovação. O detalhe fica em [`rede/MODELO-REDE.md`](../rede/MODELO-REDE.md).

## Contexto

- **A Regulação decide o caso; a rede define a estrutura.** A Central de Regulação do Acesso
  ([ADR-0087](./0087-regulacao-do-acesso-backend.md)) autoriza a vaga que o regulador informa, sem teto. As cotas por unidade e procedimento
  (a PPI) ficaram pendentes na Regulação e foram passadas a este domínio pela ADR-0115.
- **O modelo operacional já diz o que cada unidade pode fazer** (capacidades, MO4), e a RAPS já tem o seu painel de componentes
  ([ADR-0153](./0153-saude-mental-caps-e-raps.md)).

## Decisão

1. **Fatias:**
   - **GR1 (esta):** a oferta assistencial e as cotas da PPI, com a Regulação consumindo a cota na autorização;
   - **GR2:** redes temáticas (Rede de Urgência, Rede Cegonha/Alyne, Pessoa com Deficiência, Doenças Crônicas) e a referência estrutural (para
     onde cada unidade encaminha cada especialidade), no padrão da RAPS: o componente da rede como dado do modelo operacional e o painel de
     lacunas;
   - **GR3:** cobertura da rede e indicadores de pactuação no BI.
2. **Oferta assistencial** (`TB_OFERTA_ASSISTENCIAL`): a executante, o procedimento regulado, a competência (o mês, guardado no dia 1) e a
   quantidade de vagas.
   - Uma por executante, procedimento e competência (409).
   - A executante precisa da capacidade que o procedimento exige, a mesma regra da autorização (422).
   - O ajuste muda só a quantidade e a observação, e não fica abaixo do pactuado nem do já usado (422).
3. **Cota da PPI** (`TB_COTA_PPI`): a oferta, a beneficiária e a quantidade.
   - A beneficiária é uma unidade ou uma esfera (município, regional); a cota da esfera vale para as unidades abaixo dela, pela unidade
     superior e pela supervisão regional, como o recorte da [ADR-0175](./0175-coordenacoes-e-gerencia.md).
   - A soma das cotas não passa da oferta (422); uma cota por beneficiária na oferta (409).
   - O ajuste não fica abaixo do usado (422); só sai a cota nunca usada (422).
4. **A Regulação consome a cota** na autorização ([ADR-0087](./0087-regulacao-do-acesso-backend.md)):
   - procura a oferta da executante, do procedimento e do mês da vaga. **Sem oferta cadastrada, nada muda:** o controle começa quando a gestão
     cadastra a oferta, para a implantação ser aos poucos;
   - com oferta, vale a cota **mais específica** que cobre a solicitante: a dela, senão a da esfera mais próxima acima. A cota da unidade
     esgotada não passa para a do município;
   - saldo é a cota menos as autorizações que a usaram (autorizada, agendada, realizada e falta ocupam a vaga; cancelada devolve). A cota fica
     travada durante a autorização, para o saldo não ser usado duas vezes;
   - sem cota ou sem saldo, só como **extracota**, com justificativa (422); fica na solicitação e no texto do evento de autorização;
   - a solicitação ganha a cota consumida, a marca de extracota e a justificativa.
5. **Painel da PPI** por competência: por oferta, o ofertado, o pactuado, o que falta pactuar, o usado e as extracotas; por beneficiária, a
   cota, o usado e o saldo. **Cópia** das ofertas e cotas de um mês para outro (o destino depois da origem, 400); a oferta que já existe no
   destino fica como está.
6. **Permissões:** `REDE.CONSULTAR` e `REDE.GERENCIAR` (operação), a gestão no escopo da executante; o **Gestor** consulta e gerencia; o
   **Médico regulador** consulta, para ver o saldo na hora de autorizar. Sem papel novo.

### API

- `GET /api/v1/rede/ppi` (`competencia`, `executanteId`), `GET /ppi/saldo` (`procedimentoId`, `executanteId`, `solicitanteId`, `competencia`),
  `POST /ppi/copia`;
- `POST /api/v1/rede/oferta`, `PUT /oferta/{uuid}`, `POST /oferta/{uuid}/cota`, `PUT, DELETE /cota/{uuid}`;
- `POST /api/v1/solicitacao-regulacao/{uuid}/autorizacao` aceita `extraCota` e `justificativaExtraCota`; a solicitação traz
  `cotaBeneficiariaNome`, `extraCota` e `justificativaExtraCota`.

### Tela

- **Rede e pactuação (PPI),** em Administrativo: a competência, o resumo (ofertado, pactuado, utilizado, extracotas), as ofertas com as cotas,
  o usado e o saldo, as gavetas de oferta e de cota e "Copiar do mês anterior".
- **Regulação, gaveta Autorizar:** ao escolher a executante e a data, o saldo da cota da solicitante; sem saldo, o aviso, a extracota e a
  justificativa.

### Migração

`V64__gestao_da_rede_oferta_e_ppi.sql`, com a cota, a extracota e a justificativa na solicitação de regulação.

## Consequências

- **A implantação é gradual:** a executante sem oferta cadastrada segue como antes.
- **Ficam fora:** SISREG e SIGTAP; teto financeiro (MAC) e programação físico-orçamentária (Financeiro, #17); PPI entre municípios de estados
  diferentes; as redes temáticas (GR2) e a cobertura (GR3).

## Testes

- **JUnit:**
  - `RedeControllerTest`: oferta (repetida, capacidade exigida, ajuste abaixo do pactuado) e cotas (repetida, acima da oferta); a Regulação
    consumindo a cota da UBS, a extracota sem e com justificativa, a UBS sem cota usando a do município, a unidade fora sem cota, o mês sem
    oferta sem controle, o painel com o usado e as extracotas, a cota usada que não baixa nem sai, o cancelamento que devolve a vaga; a cópia
    da competência;
  - `RedeAutorizacaoControllerTest`, com a segurança ligada: sem login, o gestor de outra unidade, o gestor do município, o médico regulador
    no saldo, o ACS sem acesso;
  - a regressão da Regulação (`SolicitacaoRegulacaoControllerTest`, `RegulacaoAutorizacaoControllerTest`, `RegulacaoOdontologicaControllerTest`).
- **Robot:** `Rede.robot` (API, três casos) e `UI_ppi.robot`; regressão das suítes de Regulação.
