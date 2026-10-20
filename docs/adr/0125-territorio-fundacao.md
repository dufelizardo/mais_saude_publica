# 0125 — Território: fundação (F1)

## Status

Aceita e implementada (fatia F1 da [ADR-0108](./0108-territorio-e-adscricao.md); detalhe em
[`territorio/MODELO-TERRITORIO.md`](../territorio/MODELO-TERRITORIO.md)).

## Contexto

- **A ADR-0108 desenhou o Território e Adscrição (#21)** como base territorial da Atenção Primária. A área é da equipe,
  não da UBS, e a microárea é do ACS. O território tem uma finalidade (APS, referência ou vigilância), e as associações
  são histórico.
- **Hoje as microáreas são texto livre:** `Equipe.microareas` ("14, 17, 18") e `MembroEquipe.microarea` (ADR-0103),
  de forma provisória.
- **Outras fatias esperam o recorte territorial:**
  - a Vigilância, para o mapa dos casos (V2);
  - Emergências, para a área afetada (E1);
  - Equipamentos, para o mapa que hoje mostra "Em breve".
- **Banco sem PostGIS** (`postgres:16-alpine`): a ADR-0108 decidiu guardar GeoJSON e fazer as contas no Java.

## Decisão

1. **Recorte** (migração V19):
   - **`SegmentoTerritorial`:**
     - código único (409) e nome;
     - tipo (`DISTRITO_SANITARIO · ZONA_IBGE · OUTRO`);
     - unidade de gestão opcional;
     - geometria.
   - **`Territorio`:**
     - segmento (não muda) e nome único no segmento (409);
     - **finalidade** (`ATENCAO_PRIMARIA · REFERENCIA_ASSISTENCIAL · VIGILANCIA`);
     - ativo. Território inativo não recebe área (422).
   - **`Area`:**
     - território (não muda);
     - código único no território (409) e código CNES opcional (até 10 dígitos);
     - nome e geometria.
   - **`Microarea`:**
     - área (não muda) e código único na área (409);
     - nome, geometria e **população de referência**. Acima de 750 pessoas (referência do CNES), **aviso, não
       bloqueio**.
   - **Nenhum tipo de equipe é codificado:** nada de "só a eSF tem território" (ADR-0108).
2. **Geometria: GeoJSON em WGS84, em texto, sem PostGIS.** A validação usa o **JTS** (`jts-core`, dependência nova):
   - **Formato aceito:** `Polygon` ou `MultiPolygon`, ou uma `Feature` (ou `FeatureCollection` de uma só) com um
     deles, para o arquivo exportado de outro SIG. Guarda só a geometria.
   - **O que dá 400:** posição fora da faixa (longitude de -180 a 180, latitude de -90 a 90), anel aberto ou com menos
     de 4 posições, e geometria inválida para o JTS (autointerseção, por exemplo).
   - **Microárea dentro da área (422),** com folga de borda de 0,00001° (cerca de 1 m).
   - **Sem sobreposição entre microáreas da mesma área (422).** Encostar é permitido; sobreposição abaixo de 10⁻⁹ graus²
     (cerca de 12 m²) é ruído de desenho.
   - **Mudar o desenho da área** não pode deixar microárea desenhada de fora (422).
   - **Geometria opcional:** as microáreas migradas não têm desenho, que vem na F2.
3. **Equipe responsável pela área** (`ResponsabilidadeTerritorial`), como histórico:
   - **Unicidade (409):** uma equipe vigente por área e **uma área vigente por equipe**. Com isso, as microáreas da
     equipe vêm de uma área só.
   - **A equipe precisa estar ativa (422).**
   - **Trocar a equipe pede motivo (400).** A anterior fecha na data de início da nova, e os ACS dela deixam as
     microáreas da área.
   - **Início não futuro**, e não antes do início da atual (400).
4. **ACS da microárea** (`AtribuicaoMicroarea`), como histórico, ligado à **participação** do ACS na equipe
   (`MembroEquipe`):
   - **Condições (422):** a área precisa ter equipe responsável, e o ACS precisa ser membro vigente, **com a função
     ACS**, dessa equipe.
   - **Unicidade (409):** um ACS vigente por microárea e uma microárea vigente por ACS.
   - **Trocar o ACS pede motivo (400).** O anterior sai na data de início do novo.
   - **Microárea sem ACS:** o encerramento tem motivo, e a data fica entre o início e hoje (400).
   - **A saída do ACS da equipe** (Equipes) fecha a atribuição na mesma data.
   - **Os índices únicos parciais** do banco garantem as mesmas regras.
   - **Médico e enfermeiro** alcançam a área pela equipe: não há vínculo deles com microárea.
5. **Coordenadas da unidade:** `UnidadeDeSaude` ganha latitude e longitude, informadas juntas ou nenhuma (400). Servem
   aos pinos do mapa do território e ao mapa de Equipamentos na F2.
   - Gravar exige `ORGANIZACAO.GERENCIAR`, como o resto do cadastro da unidade.
6. **Migração do texto livre, na própria V19:**
   - **Estrutura criada:** cada equipe com microárea em texto (na equipe ou num ACS dela) ganha uma **área própria,
     sem desenho**:
     - num território "Migrado das equipes" (APS), dentro do segmento `MIGRACAO`;
     - com a equipe responsável;
     - o código da área é o INE, ou `EQ-` mais o começo do id da equipe.
   - **Microáreas e atribuições:** cada código vira uma microárea. Cada participação de ACS com microárea vira
     atribuição: a vigente, e a encerrada com o mesmo fim.
   - **Código repetido:** se dois ACS vigentes tinham o mesmo código, fica o que entrou primeiro. O outro segue sem
     microárea, para acerto na tela.
   - **O Território passa a ser a fonte:**
     - numa equipe com área, `Equipe.microareas` e `MembroEquipe.microarea` são **lidos de lá**, e a resposta de
       Equipes diz `microareasPeloTerritorio`;
     - mandar outras microáreas na edição da equipe dá 422 ("edite pelo Território"), e mandar as mesmas, em qualquer
       ordem, passa;
     - incluir um membro com microárea numa equipe com área também dá 422.
   - **Equipe sem área** segue como antes, até os dois campos saírem numa migração seguinte.
7. **Acesso:**
   - **Permissão nova `TERRITORIO.GERENCIAR`**, que vai para Gestor e Coordenador de enfermagem.
   - **Escopo de cada operação:**
     - o recorte (segmento, território, área e microárea) pede a permissão na **unidade de gestão do segmento**, ou na
       rede inteira quando o segmento não tem unidade;
     - a equipe da área e o ACS da microárea pedem a permissão na **unidade da equipe**.
   - **Ler é de quem está logado:** o recorte geográfico não é dado de saúde. A população por família vem na F3, e o
     território como escopo de acesso, na F4.
8. **API** (`/api/v1/territorio/`):
   - **Consulta:** `arvore`, `area/{id}`, `equipe/{id}` e `unidades`;
   - **Cadastro (POST/PATCH):** `segmento`, `territorio`, `area` e `microarea`;
   - **Associações:** `area/{id}/responsabilidade`, `microarea/{id}/atribuicao` e `microarea/{id}/atribuicao/encerrar`;
   - **Unidade:** `unidade/{id}/coordenadas` (PUT).
   - **A árvore** traz os indicadores (áreas sem equipe, microáreas sem ACS, sem desenho, acima da referência) e os
     avisos de cada microárea.
9. **Tela "Território"** no Administrativo, aberta a quem está logado:
   - **Navegação:** a árvore segmento → território → área com busca, e o detalhe da área. O detalhe mostra a equipe,
     as microáreas com o ACS e os avisos, os ACS da equipe sem microárea e o histórico.
   - **Gavetas:** de cadastro, com GeoJSON colado ou importado de arquivo `.geojson`, e de atribuição de equipe, de ACS
     e de microárea sem ACS. A troca pede motivo.
   - **Mapa:** a tela reserva o espaço com "Em breve: mapa".
   - **Em Equipes:** as microáreas vindas do Território ficam só para leitura, com o link para lá, e a inclusão de
     membro não pede microárea.

## Consequências

- **O recorte territorial existe como dado**, com histórico de quem responde por quê, e o texto livre deixa de ser a
  fonte.
- **Destrava** a F2 (mapa) e a F3 (imóvel, família e adscrição), e por elas a V2, a E1 e o mapa de Equipamentos.
- **Fora do escopo:**
  - o mapa com Leaflet, o desenho de polígonos e o mapa real em Equipamentos (F2);
  - imóvel, família e adscrição (F3);
  - o escopo territorial do ACS (F4);
  - a remoção dos campos de texto;
  - a importação de KML.

## Testes

- **JUnit:**
  - **`TerritorioControllerTest`:**
    - GeoJSON inválido (tipo, anel aberto, fora do WGS84, autointerseção, texto que não é JSON);
    - microárea dentro da área, encostada, fora e sobreposta; Feature aceita; desenho da área que deixaria microárea de
      fora; avisos e indicadores da árvore;
    - equipe da área e ACS da microárea, com 409 e 422, troca com motivo e o histórico;
    - saída do ACS da equipe fechando a atribuição e troca de equipe fechando os ACS;
    - Equipes lendo do Território e recusando a escrita;
    - a migração do texto livre, executada sobre equipes de teste numa transação desfeita;
    - coordenadas da unidade.
  - **`TerritorioAutorizacaoControllerTest`:** o gestor cadastra na unidade dele, mas não na rede inteira; o
    enfermeiro só lê; coordenadas pedem `ORGANIZACAO.GERENCIAR`.
- **Robot:**
  - **de API:** `test/territorio/Territorio.robot`;
  - **de interface:** `test/ui/administrativo/territorio/UI_territorio.robot`.
