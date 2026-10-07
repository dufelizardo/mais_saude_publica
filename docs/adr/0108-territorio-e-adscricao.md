# 0108 — Território e Adscrição: domínio próprio

## Status

Aceita. **Desenho, sem implementação.** A implementação vem nas fatias F1 a F4, cada uma com ADR própria. O detalhe
está em [`territorio/MODELO-TERRITORIO.md`](../territorio/MODELO-TERRITORIO.md).

## Contexto

- As equipes de saúde (ADRs 0103 e 0104) guardam as microáreas como **texto livre**: `Equipe.microareas` e
  `MembroEquipe.microarea`, a do ACS.
- Famílias, pacientes adscritos e cobertura aparecem como "Em breve" em Equipes, e as coordenadas e o mapa como "Em
  breve" em Equipamentos (ADR-0102).
- A análise da Atenção Primária mostra que área e microárea não são atributos da UBS. Segundo o CNES e o e-SUS APS:
  - a **área** é o conjunto de microáreas sob responsabilidade de uma equipe;
  - a **microárea** é o espaço de atuação do ACS, com até 750 pessoas na referência do CNES;
  - acima delas há o **segmento territorial**;
  - o cadastro territorial passa por imóvel, família e indivíduo.
- O usuário quer a tela de território no modelo de `Equipamentos.html`, com **mapa geolocalizado** que destaque a
  área e cada microárea.
  - O mapa do protótipo é um SVG esquemático, sem coordenadas.
  - O banco é `postgres:16-alpine`, sem PostGIS.

## Decisão

1. **Domínio novo e transversal: #21 Território e Adscrição** (MAPA-DE-DOMINIOS).
   - Não fica em RH, em Administrativo, na unidade nem na Enfermagem.
   - O RH sabe quem é o ACS; a Organização sabe onde a equipe está; o Território sabe que espaço ela cobre; a
     Adscrição sabe quais famílias e cidadãos estão sob responsabilidade dela.
2. **Modelo:**
   - `SegmentoTerritorial`;
   - `Territorio`, com **finalidade**: atenção primária, referência assistencial ou vigilância;
   - `Area` → `Microarea`;
   - `Imovel` → `Domicilio` → `NucleoFamiliar` → cidadão. O cidadão é o `Paciente` que já existe (ADR-0040).
3. **Associações são histórico, com início e fim:**
   - `ResponsabilidadeTerritorial`: equipe ↔ área;
   - `AtribuicaoMicroarea`: ACS ↔ microárea;
   - `Adscricao`: família ou cidadão ↔ equipe.
4. **A área é da responsabilidade da equipe, não da UBS.** A unidade chega à área pela equipe.
5. **A microárea é a unidade operacional do ACS.** Médico e enfermeiro pertencem à equipe e alcançam a área inteira;
   atribuir microárea a eles não é regra estrutural.
6. **Nenhum tipo de equipe é codificado como o único com território:**
   - eSF e eAP usam área com microáreas e ACS;
   - o Consultório na Rua tem território de atuação, sem microárea;
   - CAPS e hospital têm território de **referência**, que é outra finalidade;
   - a UPA não tem estrutura territorial;
   - a vigilância usa o território com finalidade própria.
7. **Território geográfico é diferente de adscrição assistencial:** o polígono diz onde fica; a adscrição diz quem
   responde, por quem e desde quando.
8. **Escopo de acesso:**
   - a microárea vira **uma dimensão possível do escopo**, somada ao escopo por unidade da ADR-0066 (o ACS vê a
     própria microárea, a equipe vê a sua área, o gestor municipal vê o agregado);
   - **microárea não é autorização:** a decisão continua sendo usuário + papel + permissão + escopo + contexto;
   - entra atrás de toggle, como a ADR-0076.
9. **Geolocalização sem PostGIS:**
   - **Geometria:** GeoJSON (WGS84) guardado nas entidades: polígono na área e na microárea, ponto no imóvel e
     latitude e longitude na unidade.
   - **Contas espaciais:** feitas no Java com JTS (imóvel na microárea, microárea na área, sobreposição). O PostGIS só
     entra com outra ADR, se o volume pedir.
   - **Mapa:** Leaflet com tiles do OpenStreetMap e a atribuição exigida, destacando a área inteira, as microáreas
     coloridas com código e ACS, e as unidades.
   - **Desenho:** polígonos desenhados com `leaflet-draw` ou importados de GeoJSON ou KML.
   - **Tela:** lista e detalhe como `Equipamentos.html`, com o mapa real no lugar do SVG.
10. **Fatias:**
    - **F1 · Backend:** segmento, território, área e microárea; responsabilidade e atribuição com histórico;
      coordenadas da unidade; migração do texto livre.
    - **F2 · Tela Território com mapa:** inclui o mapa real em Equipamentos.
    - **F3 · Cadastro territorial:** imóvel, domicílio, núcleo familiar e adscrição; preenche famílias, pacientes e
      cobertura em Equipes.
    - **F4 · Escopo territorial de acesso.**
11. **Transição:** na F1, `Equipe.microareas` vira `Microarea` e `MembroEquipe.microarea` vira `AtribuicaoMicroarea`.
    Os dois campos passam a ser leitura derivada e saem depois.

## Consequências

- Equipe deixa de ser só um agrupamento de profissionais: com a área sob responsabilidade dela, vira entidade de
  primeira classe, diferente de `Setor` e de `Lotacao`.
- O "Em breve" de território em Equipes e Equipamentos passa a ter um plano.
- Visita domiciliar, acompanhamento territorial, cadastro do e-SUS e indicadores territoriais ganham base para vir
  depois.
- A tela de território vai depender de tiles externos (OpenStreetMap) no navegador de quem usa.
- **Fora do escopo agora:**
  - visita domiciliar (ficha do ACS);
  - integração com o e-SUS APS e o CNES;
  - indicadores do Previne;
  - vigilância.
