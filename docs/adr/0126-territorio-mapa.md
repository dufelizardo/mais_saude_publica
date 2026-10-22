# 0126 — Território: mapa (F2)

## Status

Aceita e implementada (fatia F2 da [ADR-0108](./0108-territorio-e-adscricao.md), sobre a
[ADR-0125](./0125-territorio-fundacao.md); detalhe em [`territorio/MODELO-TERRITORIO.md`](../territorio/MODELO-TERRITORIO.md)).

## Contexto

- **A F1 (ADR-0125) criou o recorte territorial como dado:**
  - área e microárea com GeoJSON validado no backend;
  - equipe da área e ACS da microárea;
  - coordenadas da unidade.
- **Mas a tela só aceitava GeoJSON colado,** e o mapa aparecia como "Em breve" no Território e em Equipamentos de
  Saúde.
- **Sem desenhar na tela, o cadastro territorial não sai do papel.** A V2 (mapa dos casos) e a E1 (área afetada) também
  esperam o mapa.

## Decisão

1. **Base:**
   - **Leaflet 1.9** com os tiles do **OpenStreetMap** e a atribuição sempre visível.
   - **A URL dos tiles é configuração** (`core/config/mapa.ts`). A política de uso do OSM não admite uso pesado em
     produção, então a implantação aponta para um servidor de tiles próprio ou contratado.
   - **`localStorage['msp.mapa.tiles']` sobrepõe a URL,** e `off` desliga o fundo. Os testes usam isso para não depender
     da internet.
2. **Carregado sob demanda:**
   - **Componente compartilhado `app-mapa`:**
     - o Leaflet e o geoman entram por import dinâmico, em chunks próprios;
     - o CSS deles é copiado para `/leaflet/` e injetado só quando um mapa abre;
     - **o bundle inicial não cresce:** só ganha algumas linhas de CSS.
   - **O componente mostra polígonos e pinos, avisa a seleção, marca um ponto por clique e desenha.**
3. **Desenho com o `leaflet-geoman`, não com o `leaflet-draw` que a ADR-0108 citava.** O `leaflet-draw` está sem
   manutenção. O geoman é mantido e tem interface em português.
   - **Ferramentas:** polígono, retângulo, edição de vértices, arrastar, recortar (buraco) e apagar.
   - **Vários polígonos viram um `MultiPolygon`.**
   - **A gravação usa o PATCH da F1, sem endpoint novo.** O backend continua decidindo "dentro da área" e "sem
     sobreposição" (422), e a mensagem aparece na barra do mapa.
   - **A tela avisa durante o desenho** (turf: diferença e interseção, com o mesmo limite de ruído de 12 m²) quando o
     desenho sai da área, sobrepõe outra microárea ou, ao redesenhar a área, deixaria microárea de fora.
   - **A área desenhada aparece em hectares.**
4. **KML:** a importação das gavetas aceita `.kml`, convertido no navegador (`@tmcw/togeojson`). Os polígonos viram um
   Polygon ou um MultiPolygon em 2D, e a API continua recebendo só GeoJSON.
5. **Mapa da tela Território:** fica no detalhe da área, entre a equipe e as microáreas.
   - **Camadas:**
     - a área em contorno tracejado;
     - as microáreas coloridas com o código;
     - em âmbar, as microáreas **sem ACS**;
     - os pinos das unidades, com destaque para a unidade da equipe responsável.
   - **"Esta área / Território inteiro"** mostra as áreas vizinhas do território, para desenhar a divisa.
   - **Clicar na microárea destaca a linha da lista.**
   - **Botões de desenho:** "Desenhar área" ou "Editar desenho da área", e em cada microárea "Desenhar" ou "Editar
     desenho". Quando é a microárea que está sendo desenhada, a área e as vizinhas ficam de referência.
6. **Equipamentos de Saúde:**
   - **O botão "Mapa" passa a funcionar:**
     - mostra os pinos da lista filtrada, com a cor do tipo e da situação;
     - destaca a unidade aberta;
     - clicar no pino abre a ficha;
     - conta as unidades "sem coordenadas".
   - **Na aba Dados gerais da ficha entra "Localização":**
     - um minimapa;
     - "Marcar no mapa", que marca por clique ou com latitude e longitude digitadas;
     - a gravação usa `PUT /territorio/unidade/{id}/coordenadas`, com `ORGANIZACAO.GERENCIAR`;
     - `?visao=mapa` abre a rede já no mapa.
7. **Acessibilidade:**
   - **A lista continua sendo a navegação principal,** e o mapa é complementar.
   - **Microáreas e pinos recebem foco e nome acessível** ("Microárea 07, ACS Carlos"; o nome da unidade) e respondem
     ao Enter.
   - **O modo de desenho tem instruções em texto,** e o GeoJSON segue como alternativa nas gavetas.
8. **Layout:** o painel de detalhe das telas com lista e detalhe era fixo (`sticky`) sem rolagem própria. Mais alto
   que a janela, o fim dele ficava inalcançável, e o mapa tornou isso comum. O painel passa a ter altura máxima e
   rolagem interna.

## Consequências

- **O território se desenha e se vê na tela,** e as unidades ganham localização.
- **Destrava** o mapa dos casos (V2), a área afetada (E1) e o mapa da F3 (imóveis).
- **Dependências novas no frontend:**
  - para o mapa: `leaflet`, `@geoman-io/leaflet-geoman-free` e `@tmcw/togeojson`;
  - para as contas do desenho: os módulos `@turf/area`, `@turf/difference`, `@turf/intersect` e `@turf/helpers`.
- **Fora do escopo:**
  - imóveis e famílias no mapa (F3);
  - o servidor de tiles próprio, que é configuração de implantação;
  - camadas da vigilância (V2);
  - geocodificação de endereço (F3).

## Testes

- **Sem endpoint novo:** não há suíte de API nova. As suítes do Território (ADR-0125) seguem valendo.
- **Robot de interface** (`test/ui/administrativo/territorio/UI_territorio.robot`, CT-004 a CT-008), com o fundo
  desligado:
  - o mapa mostra a área, a microárea com ACS e a sem ACS;
  - desenhar a microárea com o retângulo dentro da área e salvar;
  - o desenho fora da área mostra o aviso e o backend recusa;
  - Equipamentos no modo Mapa: o pino e a ficha pelo clique;
  - marcar a localização da unidade no minimapa.
