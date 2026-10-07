# 0115 — Modelo Operacional dos Equipamentos e reconciliação do mapa de domínios

## Status

Aceita. **Aceita e completa a [ADR-0053](./0053-criterio-de-governanca-para-equipamentos-de-saude.md).** Desenho,
sem implementação: as fatias MO1 a MO4 vêm depois, cada uma com ADR própria. O detalhe, com a matriz dos equipamentos,
está em [`equipamentos/MODELO-OPERACIONAL.md`](../equipamentos/MODELO-OPERACIONAL.md).

## Contexto

O usuário trouxe dois textos:
- **uma análise de 17 equipamentos de saúde;**
- **um plano arquitetural de 31 itens que renumera a plataforma.**

Ele pediu para comparar com o que já existe e dizer se vale uma adequação.

**Da análise:**
- **seis padrões operacionais;**
- o tipo de equipamento não determina sozinho o comportamento;
- o local da organização é diferente do local onde o cuidado acontece (SAMU, unidade móvel);
- o objeto central varia: cidadão, amostra, estabelecimento, animal;
- metamodelo com perfil operacional, capacidades, serviços, equipes e pontos operacionais;
- uma matriz de equipamentos × capacidades antes do backlog.

**O que já tínhamos:**
- **A ADR-0053, ainda "Proposta",** já decide o essencial:
  - diferença de dado vira capacidade, e não enum;
  - comportamento novo vira especialização de domínio;
  - `TipoUnidadeDeSaude` só cresce por mudança estrutural;
  - nenhuma condição por tipo dentro do núcleo.
- **O Mapa de Equipamentos (seção 4)** já tem equipamento → serviços → capacidades → profissionais → setores. Lá, SAMU
  e ambulância não são `UnidadeDeSaude`, e a unidade móvel é modalidade.
- **O padrão de catálogo como dado** já existe no Administrativo (`CapacidadeAdministrativa`, `PerfilAdministrativo`).

**O que faltava:**
- **o perfil operacional** como conceito;
- **o ponto operacional:** hoje o `Atendimento` só tem a unidade;
- **um lugar único** para pendências espalhadas que são o mesmo conceito: CAPS (ADR-0112, SM5), CEO (ADR-0111, SB4),
  capacidade emergencial (ADR-0113, E3);
- **dois domínios ausentes do mapa:** Gestão da Rede de Atenção e Intersetorialidade.

## Decisão

1. **A ADR-0053 passa a "Aceita",** complementada por esta. Os cinco critérios dela continuam valendo.
2. **Camada transversal: #29 Modelo Operacional dos Equipamentos.**
   - Não é domínio de negócio. Fica entre Organização (#1) e os domínios especializados.
   - **Regra:** a unidade define onde e em que contexto a operação existe. Perfil e capacidades definem o que ela pode
     fazer. Serviços e equipes definem como ela se organiza. Os domínios definem os processos.
3. **Metamodelo, com entidades só quando um domínio puxar:**
   - a unidade, com o tipo estrutural;
   - **perfis operacionais e capacidades assistenciais**, como catálogo, vários por unidade;
   - **serviços**;
   - setores e equipes, que já existem;
   - **pontos operacionais**.

   Os catálogos seguem o padrão do Administrativo, no contexto assistencial próprio, sem reaproveitar a entidade
   administrativa.
4. **Seis padrões operacionais** como referência de desenho, não como tipos:
   - territorial longitudinal;
   - episódico;
   - longitudinal especializado;
   - complexo hospitalar;
   - serviço operacional com fluxo próprio;
   - vigilância e controle.
5. **Os domínios consultam capacidades, não tipos.**
   - Leitos, Agenda, Saúde Mental (CAPS), Saúde Bucal (CEO) e Emergências (capacidade emergencial) perguntam "a unidade
     tem a capacidade X?".
   - Sem herança de unidade nem `HospitalService` ou `UBSService`.
6. **O local do cuidado é diferente do endereço da unidade.**
   - O `Atendimento` ganha um **local de realização** opcional (domicílio, via pública, local do evento, escola, ponto
     da unidade móvel).
   - A unidade continua sendo o vínculo organizacional.
   - A unidade móvel tem base, localização e rota.
   - SAMU e ambulância seguem o Mapa de Equipamentos.
7. **O núcleo não impõe "Paciente → Atendimento".** Vigilância e serviços operacionais operam sobre os objetos dos
   próprios domínios (estabelecimento, amostra, dispensação, foco).
8. **Equipamento de saúde ≠ equipamento patrimonial:** a UPA é #1/#29; o respirador é #15.
9. **Reconciliação sem renumerar.**
   - O mapa mantém os números, citados pelas ADRs e pelo STATUS. A correspondência com o plano de 31 itens fica no
     documento do modelo.
   - **Entram:**
     - o **#29**;
     - o **#30 Gestão da Rede de Atenção:** oferta, pactuação e PPI, redes temáticas, participação na rede,
       referência estrutural. A Regulação processa a necessidade individual. Reservado, a desenhar;
     - o **#31 Intersetorialidade e Proteção Social:** catálogo único de instituições externas e encaminhamento
       intersetorial, usados por Saúde Mental, Vigilância, Emergências e Comunicação. Reservado, a desenhar.
   - **Ficam como estão:** o #24 Imunização e o domínio Documentos, que o plano omitia.
10. **A matriz dos 17 equipamentos × capacidades** é o artefato de validação antes do backlog. O documento-fonte
    analisa 16; a Policlínica completa a lista.
11. **Fatias:**
    - **MO1:** perfis e capacidades, com a aba na tela Equipamentos.
    - **MO2:** serviços da unidade.
    - **MO3:** ponto operacional.
    - **MO4:** domínios consultando capacidades.

    A **MO1 é pré-requisito** de SB4, SM5 e E3.

## Consequências

- **O critério da ADR-0053 vira modelo,** com lugar para perfil, capacidade, serviço e ponto operacional.
- **As pendências espalhadas** (CAPS, CEO, capacidade emergencial) passam a depender de uma fatia só (MO1).
- **O mapa fica com 31 itens sem perder a rastreabilidade.**
- **Saúde Mental (SM6) passa a usar o #31** para as instituições externas, e a Regulação passa as cotas PPI ao #30.
- **Fora do escopo:**
  - redesenhar `TipoUnidadeDeSaude`;
  - migrar o perfil administrativo;
  - motor de fluxo configurável (BPM);
  - herança de unidade.
