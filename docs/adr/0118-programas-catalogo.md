# 0118 — Programas de saúde: catálogo (P1)

## Status

Aceita e implementada (fatia P1 da [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md)). As fatias P2
(acompanhamento) a P6 (indicadores) vêm depois.

## Contexto

- A [ADR-0109](./0109-programas-acoes-e-linhas-de-cuidado.md) desenhou o domínio #22 Programas, Ações e Linhas de
  Cuidado. Detalhe em [`programas/MODELO-PROGRAMAS.md`](../programas/MODELO-PROGRAMAS.md).
- Programas aparecem como "Em breve" em Pacientes, Agenda e Equipes.
- O protótipo `Programas.html` mostra cartões por categoria e o detalhe do programa.
- A landing page tinha um cartão "Programas de saúde" que apontava para `#programas`, uma seção que não existia.
- Tudo o que vem depois depende do catálogo:
  - o acompanhamento do paciente (P2), que leva à Saúde Mental (SM1);
  - as campanhas (P3);
  - as ações (P4);
  - os indicadores (P6).

## Decisão

1. **Catálogo cadastrável, sem enum de programa:**
   - **`CategoriaPrograma`:** nome único, ordem e ativa.
   - **`Programa`:**
     - código único (maiúsculas), nome, descrição e categoria;
     - abrangência (nacional, estadual, municipal) e situação (ativo, suspenso, encerrado);
     - início e fim;
     - coordenação: profissional do RH, pela matrícula;
     - periodicidade de acompanhamento;
     - marca de **sensível**.
   - **Público-alvo, dentro do programa:**
     - idade mínima e máxima, sexo (o enum `Sexo` do Paciente, sem `IGNORADO`), gestante;
     - condições por código CID-10 ou CIAP-2.
     - **Só descreve quem o programa atende; não inscreve ninguém.** Sugerir elegíveis é da P2.
   - **`ParticipacaoPrograma`:**
     - campos: unidade, equipe opcional (que precisa ser da unidade), início, fim e motivo;
     - **histórico**, como na MO1: encerrar não apaga.
2. **Regras:**

   | Situação | Resposta |
   |---|---|
   | Código de programa ou nome de categoria repetido | 409 |
   | Encerrado sem fim; fim antes do início; idade máxima menor que a mínima; condição fora do formato CID/CIAP; sexo `IGNORADO` | 400 |
   | Categoria inativa para programa novo, ou coordenação inativa | 422 |
   | Coordenação com matrícula que não existe | 404 |
   | Participação de nível de gestão, de equipe de outra unidade, ou em programa encerrado | 422 |
   | A mesma unidade e equipe vigente duas vezes | 409 |
   | Início de participação no futuro; fim fora do intervalo entre o início e hoje; encerrar sem motivo | 400 |

3. **Acesso** (ADR-0109, decisão 7):
   - **Ler o catálogo:** qualquer usuário logado (`@LiberadoParaAutenticados`). O catálogo não tem dado de paciente.
   - **Cadastrar e mexer nas participações:** permissão nova **`PROGRAMA.GERENCIAR`**.
     - Ela entra no papel Gestor pela rotina do catálogo de acesso (ADR-0087).
     - A participação exige a permissão no escopo da unidade.
   - **Auditoria:** as alterações entram nos recursos `PROGRAMA` e `CATEGORIA_PROGRAMA`.
4. **Consulta pública sem login:**
   - `GET /api/v1/publico/programa` devolve os programas ativos de categorias ativas, com nome, categoria,
     descrição, público em texto e as unidades onde cada um é oferecido.
   - **Ficam de fora:** código, coordenação e os códigos de condição. Nunca há dado de paciente.
   - **Anotação nova `@RotaPublica`:**
     - o `SecurityConfig` libera `/api/v1/publico/**`, e o `AutorizacaoInterceptor` não exige permissão;
     - o teste de rotas (`AutorizacaoControllerTest`) aceita `@RotaPublica` **só sob `/api/v1/publico/`**, para
       que uma rota comum não fique aberta por engano.
5. **API:**
   - `/api/v1/categoria-programa/`: GET, POST e PATCH.
   - `/api/v1/programa/`:
     - GET, com os filtros `categoriaId` e `unidadeId`;
     - GET por id, com as participações vigentes e as encerradas;
     - POST e PATCH;
     - `POST {id}/participacao` e `POST {id}/participacao/{participacaoId}/encerramento`.
6. **Migração V12** (roda uma vez; depois, tudo é editável):
   - **4 categorias:** Atenção Básica, Crônicos, Ciclos de vida e Saúde Mental.
   - **13 programas nacionais:**

     | Categoria | Programas |
     |---|---|
     | Atenção Básica | Saúde da Família, Saúde Bucal, Imunização, Planejamento Reprodutivo |
     | Crônicos | HiperDia, Tuberculose, Hanseníase, Tabagismo |
     | Ciclos de vida | Pré-natal, Saúde da Mulher, Saúde da Criança, Saúde do Idoso |
     | Saúde Mental | Saúde Mental (sensível) |

   - **Público-alvo onde é claro:**
     - Criança: 0 a 9 anos; Idoso: 60 anos ou mais; Planejamento Reprodutivo: 10 a 49 anos;
     - Pré-natal: gestantes; Saúde da Mulher: sexo feminino;
     - HiperDia: I10, E10, E11, K86, K87, T89, T90;
     - Tuberculose: A15 a A19; Hanseníase: A30; Tabagismo: F17, P17.
   - **Participações não entram:** não dá para adivinhar quais unidades oferecem o quê.
7. **Telas:**
   - **Programas de Saúde** em `/assistencia/programas`, no grupo Assistência, aberta a quem está logado, pelo
     protótipo:
     - indicadores: programas ativos e programas com unidades; pacientes e cobertura ficam "Em breve";
     - busca, pílulas por categoria e filtro por unidade;
     - grade de cartões com unidades, coordenação e a marca de sensível;
     - detalhe com público-alvo, condições, período, unidades e equipes participantes (incluir, encerrar, mostrar
       encerradas) e os indicadores "Em breve";
     - "Inscrever paciente" e "Ver pacientes" ficam desabilitados até a P2;
     - gavetas de programa, participação, encerramento e categorias.
   - **Landing page:** a seção "Programas de saúde" (`#programas`), por categoria, com o público e onde procurar.

## Consequências

- **O município mantém o catálogo sem mudar código.** A P2 (acompanhamento) e a P3 (campanhas) ficam prontas para
  começar.
- **O público-alvo já está estruturado** para a P2 sugerir elegíveis (gestantes fora do pré-natal, idosos sem
  acompanhamento).
- **A consulta pública abre um caminho novo sem login (`/api/v1/publico/**`).** Ele é reservado a dado agregado ou
  institucional, e o teste de rotas garante que só rotas `@RotaPublica` fiquem ali.
- **Fora do escopo:**
  - acompanhamento de pacientes e sugestão de elegíveis (P2);
  - campanhas (P3);
  - ações programáticas e o programa na Agenda (P4);
  - linhas de cuidado (P5);
  - metas e indicadores (P6).

## Testes

- **JUnit:**
  - `ProgramaControllerTest`:
    - carga inicial;
    - cadastro com público-alvo e as validações (código, condição, idades, encerrado sem fim, sexo, coordenação);
    - categoria única e inativa;
    - participação de unidade e equipe como histórico, com nível de gestão e equipe de outra unidade recusados;
    - consulta pública só com ativos e sem códigos.
  - `ProgramaAutorizacaoControllerTest`: sem login, só a consulta pública responde; o enfermeiro lê e não
    cadastra; o gestor cadastra.
  - `AutorizacaoControllerTest`: o teste de rotas aceita `@RotaPublica` só sob `/api/v1/publico/`.
  - A suíte inteira passou localmente. O único erro (no `SetorControllerTest`) era resto de dado no banco local e
    não se repetiu num banco limpo.
- **Robot de API** (`test/programas/programa`): 8 casos.
- **Robot de interface** (`test/ui/assistencia/programas`): 6 casos (detalhe, cadastro, validação, incluir e
  encerrar participação, landing page).
