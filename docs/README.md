# Documentação do Mais Saúde Pública

Índice de toda a documentação do projeto. **Comece por aqui.**

## Visão geral e estado

| Documento | Para quê |
|---|---|
| [`STATUS-DOS-DOMINIOS.md`](./STATUS-DOS-DOMINIOS.md) | **o que cada domínio já tem e o que falta**, com a ordem e as dependências das fatias; o registro vivo do andamento |
| [`MAPA-DE-DOMINIOS.md`](./MAPA-DE-DOMINIOS.md) | os 31 itens da plataforma (mais Documentos): responsabilidade, entidades e estado de cada um; mapa fechado pela [ADR-0116](./adr/0116-marco-mapa-de-dominios-fechado.md) |
| [`PENDENCIAS.md`](./PENDENCIAS.md) | estado dos ambientes (o que está ligado em `dev`, `qaa`, `homologacao` e `prod`), ações manuais, infraestrutura e roadmap |
| [`adr/README.md`](./adr/README.md) | índice de todas as decisões de arquitetura (ADRs) |
| [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](./MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md) | levantamento dos equipamentos de saúde (base CNES) e sua correspondência no sistema |

## Modelos de domínio

Cada domínio desenhado tem um documento vivo com conceitos, entidades, regras, fatias e referências. A decisão
correspondente está na ADR indicada.

| Domínio | Documento | ADR | Estado |
|---|---|---|---|
| Recursos Humanos | [`rh/MODELO-RH.md`](./rh/MODELO-RH.md), [`rh/ESCOPO-RH.md`](./rh/ESCOPO-RH.md) | várias (0018 a 0029, 0072 a 0086, 0103 a 0107) | funcionando |
| Administrativo | [`administrativo/ESCOPO-ADMINISTRATIVO.md`](./administrativo/ESCOPO-ADMINISTRATIVO.md) | 0030 a 0038 | funcionando |
| Assistência (Paciente, Atendimento, Agenda, Prontuário) | [`assistencia/ESCOPO-ASSISTENCIA.md`](./assistencia/ESCOPO-ASSISTENCIA.md) | 0040 a 0046, 0063, 0091, 0092 | funcionando |
| Enfermagem | [`enfermagem/ESCOPO-ENFERMAGEM.md`](./enfermagem/ESCOPO-ENFERMAGEM.md) | 0047, 0048, 0064 | funcionando |
| Farmácia | [`farmacia/ESCOPO-FARMACIA.md`](./farmacia/ESCOPO-FARMACIA.md) | 0049 a 0051, 0057 a 0061 | funcionando |
| Modelo Operacional dos Equipamentos | [`equipamentos/MODELO-OPERACIONAL.md`](./equipamentos/MODELO-OPERACIONAL.md) | [0115](./adr/0115-modelo-operacional-dos-equipamentos.md) | desenhado |
| Território e Adscrição | [`territorio/MODELO-TERRITORIO.md`](./territorio/MODELO-TERRITORIO.md) | [0108](./adr/0108-territorio-e-adscricao.md) | desenhado |
| Programas, Ações e Linhas de Cuidado | [`programas/MODELO-PROGRAMAS.md`](./programas/MODELO-PROGRAMAS.md) | [0109](./adr/0109-programas-acoes-e-linhas-de-cuidado.md) | desenhado |
| Vigilância em Saúde | [`vigilancia/MODELO-VIGILANCIA.md`](./vigilancia/MODELO-VIGILANCIA.md) | [0110](./adr/0110-vigilancia-em-saude.md) | desenhado |
| Saúde Bucal | [`saude-bucal/MODELO-SAUDE-BUCAL.md`](./saude-bucal/MODELO-SAUDE-BUCAL.md) | [0111](./adr/0111-saude-bucal.md) | desenhado |
| Saúde Mental | [`saude-mental/MODELO-SAUDE-MENTAL.md`](./saude-mental/MODELO-SAUDE-MENTAL.md) | [0112](./adr/0112-saude-mental.md) | desenhado |
| Gestão de Emergências e Desastres | [`emergencias/MODELO-EMERGENCIAS.md`](./emergencias/MODELO-EMERGENCIAS.md) | [0113](./adr/0113-emergencias-e-desastres.md) | desenhado |
| Comunicação e Educação em Saúde (futuro) | [`comunicacao/MODELO-COMUNICACAO.md`](./comunicacao/MODELO-COMUNICACAO.md) | [0114](./adr/0114-comunicacao-e-educacao-em-saude.md) | desenhado |

Laboratório, Regulação e Leitos e Internação estão documentados nas próprias ADRs (0087 a 0089, 0093 a 0095, 0098 a
0100).

## Acesso, segurança e frontend

| Documento | Para quê |
|---|---|
| [`acesso/GUIA-LIGAR-AUTORIZACAO.md`](./acesso/GUIA-LIGAR-AUTORIZACAO.md) | passo a passo para ligar login, permissões e prontuário por vínculo em cada ambiente |
| [`frontend/PADRAO-TELAS-INTERNAS.md`](./frontend/PADRAO-TELAS-INTERNAS.md) | padrão visual e de interação das telas internas (gavetas, abas, cartões, listas) |

## Material de origem

| Documento | Para quê |
|---|---|
| [`pm/sistema_de_saude_brasileiro.md`](./pm/sistema_de_saude_brasileiro.md) | levantamento dos equipamentos do SUS que originou a ADR-0053 |
| [`pm/sistema_de_acesso.md`](./pm/sistema_de_acesso.md) | material que originou o modelo de acesso |

## Como a documentação é mantida

- **Decisão nova** vira ADR, com uma linha no [índice](./adr/README.md).
- **Domínio desenhado** ganha um documento vivo na própria pasta e uma linha nesta tabela.
- **Toda entrega** atualiza o [`STATUS-DOS-DOMINIOS.md`](./STATUS-DOS-DOMINIOS.md) no mesmo PR: marca a caixa,
  move o item para "Temos" e ajusta o estado.
- **Domínio novo** só pelos cinco passos da [ADR-0116](./adr/0116-marco-mapa-de-dominios-fechado.md).
