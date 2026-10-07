# Modelo do domínio Território e Adscrição

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0108](../adr/0108-territorio-e-adscricao.md). **Nada implementado ainda.** A
implementação vem nas fatias F1 a F4 (seção 10), cada uma com ADR própria.

Este documento é o detalhamento do domínio: conceitos, entidades, relações com os outros domínios, uso por tipo de
unidade e de equipe, escopo de acesso e geolocalização. A ADR-0108 registra as decisões; aqui fica o porquê e o
como.

## 1. Por que um domínio próprio

Área e microárea **não são atributos da UBS**. São a base territorial da Atenção Primária: o espaço sob
responsabilidade de uma equipe e a população que vive nele. Por isso não ficam em RH, em Administrativo, na unidade
nem na Enfermagem. Cada domínio sabe uma coisa diferente:

| Domínio | Sabe |
|---|---|
| RH | quem é o ACS, qual o cargo, onde está lotado (`Profissional`, `Lotacao`) |
| Organização | onde a equipe está (`UnidadeDeSaude`, `Equipe`, ADR-0103) |
| **Território** | qual espaço a equipe cobre (área, microárea, imóveis) |
| **Adscrição** | quais famílias e cidadãos estão sob responsabilidade de qual equipe, desde quando |

Hoje o projeto guarda as microáreas como **texto livre**: `Equipe.microareas` ("14, 17, 18") e
`MembroEquipe.microarea` (a microárea do ACS), da ADR-0103. É provisório; a seção 11 diz como sai.

## 2. Conceitos

- **Segmento territorial:** conjunto de áreas contíguas. Pode corresponder a um distrito sanitário, a uma zona de
  informação do IBGE ou a outro agrupamento relevante para o planejamento (definição do CNES).
- **Área:** conjunto de microáreas sob responsabilidade de uma equipe. Na Estratégia Saúde da Família, as microáreas
  são contíguas e a área é de uma eSF.
- **Microárea:** espaço geográfico delimitado de atuação de um ACS. A referência do CNES é de até 750 pessoas por
  microárea.
- **Imóvel, domicílio, núcleo familiar:** o e-SUS APS organiza o cadastro territorial nas dimensões imóvel,
  territorial, familiar e individual. Os moradores aparecem por microárea e logradouro, com cadastro, última visita e
  equipe.

```text
Município
   │
   └── Segmento Territorial
          │
          └── Área
                 │
                 ├── Microárea
                 │      ├── imóveis
                 │      ├── famílias
                 │      └── cidadãos
                 ├── Microárea
                 └── Microárea
```

## 3. A área é da equipe, não da UBS

A equipe de Saúde da Família assume responsabilidade sanitária sobre a população de um território. A UBS chega à
área **pela equipe**:

```text
UBS
 ├── eSF 01
 │     └── Área 01
 │           ├── Microárea 01
 │           ├── Microárea 02
 │           └── Microárea 03
 └── eSF 02
       └── Área 02
             ├── Microárea 04
             ├── Microárea 05
             └── Microárea 06
```

E não `UBS → Área → Microáreas`.

## 4. O ACS e a microárea

O RH continua dono do profissional; a atribuição territorial é deste domínio:

```text
Carlos
Cargo: Agente Comunitário de Saúde       ← RH
Lotação: UBS Jardim Esperança            ← RH
Equipe: eSF 03                           ← Organização (ADR-0103)
Microárea: 07                            ← Território (AtribuicaoMicroarea)
```

O ACS faz as visitas domiciliares, identifica problemas da comunidade, faz ações educativas e preventivas e liga a
equipe à população. **A microárea é a unidade operacional do ACS. A área é da equipe:**

```text
Equipe
   │
   └── Área
         ├── Microárea → ACS 1
         ├── Microárea → ACS 2
         └── Microárea → ACS 3
```

Médico e enfermeiro pertencem à equipe e alcançam a área inteira. `Médico → Microárea` não é regra estrutural.

## 5. População: imóvel, domicílio, família, cidadão

A microárea não é só um polígono. A população se liga a ela pelo imóvel:

```text
Microárea 07
├── Rua A, 100
│    └── Família Silva
│         ├── João
│         ├── Maria
│         └── Pedro
├── Rua A, 102
│    └── Família Santos
│         ├── Ana
│         └── Carlos
└── Rua A, 104
     └── Família Oliveira
```

Por isso `Paciente → Microárea` não basta como única relação: o caminho é microárea → imóvel → domicílio → núcleo
familiar → cidadão. **O cidadão é o `Paciente` que já existe** (ADR-0040); o domínio não cria outra pessoa.

## 6. Uso por tipo de unidade e de equipe

Nenhum tipo de equipe é codificado como "o único com território" (nada de `if (equipe é ESF)`). O CNES diferencia o
uso de segmento e área conforme o tipo de equipe, e os fluxos de cadastro territorial das outras equipes de APS podem
ser definidos localmente.

| Onde | Usa | Como |
|---|---|---|
| UBS com eSF | **sim, cenário principal** | equipe → área → microáreas → ACS → famílias e cidadãos |
| UBS com eAP e outras equipes de APS | pode usar | conforme a modalidade local |
| Consultório na Rua | território de atuação | **sem microárea formal** nem ACS por microárea |
| CAPS | território de referência | população e área de responsabilidade, **sem microárea de ACS** |
| Hospital | área ou população de referência | região de cobertura; **não é microárea da APS** |
| UPA | não | a geografia serve para origem dos pacientes, demanda, planejamento e indicadores |
| Vigilância (epidemiológica, sanitária, ambiental, zoonoses, saúde do trabalhador) | sim, com finalidade própria | município → distrito → área → microárea → imóveis → eventos; não é necessariamente a microárea do ACS. Desenhado na [ADR-0110](../adr/0110-vigilancia-em-saude.md): usa a finalidade vigilância e o imóvel, sem ficar preso à microárea |

Por isso a microárea **não fica dentro da equipe de Saúde da Família**: o território tem uma **finalidade**
(atenção primária, referência assistencial, vigilância) e a responsabilidade da equipe é uma associação separada.

## 7. Território geográfico ≠ adscrição assistencial

- **Território** (`Territorio`, `Area`, `Microarea`, `Imovel`): onde fica. É espaço.
- **Adscrição** (`ResponsabilidadeTerritorial`, `AtribuicaoMicroarea`, `Adscricao`): quem responde, por quem,
  desde quando. É responsabilidade, com histórico.

```text
                    TERRITÓRIO
                        │
              ┌─────────┴─────────┐
              │                   │
       SegmentoTerritorial    Territorio (finalidade)
                                  │
                                  └── Área
                                        │
                                  ┌─────┴─────┐
                              Microárea    Microárea
                                  │
                           ┌──────┴──────┐
                        Imóveis       Famílias
                                         │
                                      Cidadãos (Paciente)

Equipe ──── ResponsabilidadeTerritorial ──── Área          (com início e fim)
ACS ─────── AtribuicaoMicroarea ──────────── Microárea     (com início e fim)
Família ─── Adscricao ────────────────────── Equipe        (com início e fim)
```

## 8. Entidades previstas

Os atributos são uma previsão; a ADR de cada fatia fecha o desenho. As associações são **histórico com início e
fim**, não campo que se sobrescreve (mesma regra do RH).

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `SegmentoTerritorial` | município (unidade de gestão), código, nome, tipo (distrito sanitário, zona IBGE, outro), geometria | F1 |
| `Territorio` | segmento, nome, **finalidade** (`ATENCAO_PRIMARIA`, `REFERENCIA_ASSISTENCIAL`, `VIGILANCIA`), ativo | F1 |
| `Area` | território, código (com o do CNES, quando houver), nome, geometria | F1 |
| `Microarea` | área, código, nome, geometria, população de referência | F1 |
| `ResponsabilidadeTerritorial` | equipe, área, início, fim, motivo | F1 |
| `AtribuicaoMicroarea` | microárea, profissional (ACS) e participação na equipe, início, fim, motivo | F1 |
| `Imovel` | microárea, tipo (domicílio, comércio, escola, outro), endereço, ponto (lat/long) | F3 |
| `Domicilio` | imóvel, condições do domicílio (do cadastro do e-SUS) | F3 |
| `NucleoFamiliar` | domicílio, responsável (paciente), renda, membros (pacientes) | F3 |
| `Adscricao` | núcleo familiar ou paciente, equipe, início, fim; base das perguntas por microárea do acompanhamento programático ([ADR-0109](../adr/0109-programas-acoes-e-linhas-de-cuidado.md)) | F3 |

Coordenadas da unidade (latitude e longitude em `UnidadeDeSaude`) entram na F1, para os pinos do mapa e para o mapa
de Equipamentos, que hoje mostra "Em breve".

## 9. Escopo de acesso

O território acrescenta **mais uma dimensão de escopo** ao modelo da ADR-0066 (escopo por unidade):

| Quem | Escopo | Vê |
|---|---|---|
| ACS Carlos (papel ACS, UBS Central, eSF 03) | microárea 07 | as famílias da microárea 07, para as atividades autorizadas |
| Enfermeiro da eSF 03 | área 03 | a população sob responsabilidade da equipe, conforme as permissões clínicas |
| Gestor municipal | município | a visão agregada da população |

**Microárea ≠ autorização.** A decisão continua sendo usuário + papel + permissão + escopo + contexto. A microárea é
só uma dimensão possível do escopo na APS, nunca um controle de acesso universal. Entra na F4, atrás de toggle, como o
prontuário por vínculo (ADR-0076).

## 10. Geolocalização e mapa

- **Sem PostGIS por enquanto:** o banco é `postgres:16-alpine`. A geometria fica como **GeoJSON (WGS84)** em texto ou
  `jsonb`: polígonos na área e na microárea, ponto no imóvel e latitude e longitude na unidade.
- **Contas espaciais no Java**, com JTS, quando forem necessárias:
  - imóvel dentro de qual microárea;
  - microárea dentro da área;
  - sobreposição entre microáreas.

  O PostGIS só entra se o volume ou as consultas pedirem, e aí com outra ADR.
- **Mapa:** **Leaflet** com tiles do **OpenStreetMap** e a atribuição exigida pelo OSM, carregado só nas telas que
  usam mapa.
  - Destaques: a **área inteira** em contorno, as **microáreas coloridas**, cada uma com código e ACS, e os pinos das
    unidades.
  - Microárea sem ACS fica em âmbar, como em Equipes (ADR-0104).
- **Desenho:** polígonos desenhados na tela com `leaflet-draw`, ou importados de GeoJSON ou KML (exportados de outro
  SIG ou do Google My Maps).
- **Tela:** pelo modelo de `Equipamentos.html`, com lista e detalhe e o mapa no lugar do SVG esquemático do
  protótipo.

## 11. Fatias de implementação

| Fatia | Entrega | Depende de |
|---|---|---|
| **F1** · backend do território | segmento, território, área e microárea com GeoJSON; responsabilidade equipe ↔ área; atribuição ACS ↔ microárea; coordenadas da unidade; migração dos textos `Equipe.microareas` e `MembroEquipe.microarea`; regras de microárea dentro da área e sem sobreposição | ADR-0103 |
| **F2** · tela Território | lista e detalhe com mapa Leaflet; destaque da área e das microáreas; desenho e edição de polígonos; pinos das unidades; mapa real em Equipamentos | F1 |
| **F3** · cadastro territorial | imóvel, domicílio e núcleo familiar com o paciente; adscrição à equipe; geocodificação opcional do endereço; famílias, pacientes e cobertura em Equipes | F1, F2 |
| **F4** · escopo territorial | ACS restrito à microárea e equipe à área, atrás de toggle | F3, ADR-0066 |

**Transição do texto livre:**
- **F1:** os códigos de `Equipe.microareas` viram `Microarea` (sem geometria, a desenhar depois) numa área da equipe.
  `MembroEquipe.microarea` vira `AtribuicaoMicroarea`.
- **Depois da migração:** os dois campos passam a ser leitura derivada e saem numa migração seguinte.

**Fora do escopo agora:**
- visita domiciliar (ficha do ACS);
- integração com o e-SUS APS e o CNES;
- indicadores do Previne;
- vigilância.

## 12. Referências

- Portaria SAS/MS nº 750/2006: área e microárea no CNES —
  <https://bvsms.saude.gov.br/bvs/saudelegis/sas/2006/prt0750_10_10_2006.html>
- Portaria GM/MS nº 648/2006 (revogada; histórico da PNAB): atribuições e microárea do ACS —
  <https://bvs.saude.gov.br/bvs/saudelegis/gm/2006/prt0648_28_03_2006_revog.html>
- Wiki do CNES, Cadastro de Equipes: segmento territorial e área por tipo de equipe —
  <https://wiki.saude.gov.br/cnes/index.php/Cadastro_de_Equipes>
- e-SUS APS, manual do Território: cadastro por imóvel, território, família e indivíduo —
  <https://sisaps.saude.gov.br/sistemas/esusaps/docs/manual/TERRITORIO/territorio_03/>
- e-SUS APS, PEC, acompanhamento das condições de saúde: moradores por microárea e logradouro —
  <https://sisaps.saude.gov.br/sistemas/esusaps/docs/manual/PEC/PEC_10_acompanhamento_condicoes_saude/>
- SAPS, Equipe de Saúde da Família e o papel do ACS —
  <https://www.gov.br/saude/pt-br/composicao/saps/esf/equipe-saude-da-familia>
