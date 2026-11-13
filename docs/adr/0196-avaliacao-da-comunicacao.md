# 0196 — Comunicação e Educação em Saúde: avaliação da comunicação e da educação (C6)

## Status

Aceita e implementada. Fatia **C6** do domínio 28 ([ADR-0114](./0114-comunicacao-e-educacao-em-saude.md); detalhe em
[`comunicacao/MODELO-COMUNICACAO.md`](../comunicacao/MODELO-COMUNICACAO.md), seção 13). Com ela, o domínio fica completo; a C5 (multicanal)
depende de Integrações e não impede fechar o domínio.

## Contexto

- **A ADR-0114 decidiu:** o domínio produz os dados de alcance e participação, e o #19 Indicadores e BI consolida.
- **O modelo pede que a avaliação responda:**
  - quantas pessoas a campanha alcançou e quantas participaram;
  - que território teve menor alcance;
  - que canal funcionou melhor;
  - se o público-alvo foi atingido.
- **Os dados já existem:**
  - as publicações dos comunicados da campanha por canal, com enviados e falhas (C2, [ADR-0174](./0174-campanhas-de-comunicacao.md));
  - as ações educativas da campanha, com o público, as microáreas, os materiais e a participação (C3, [ADR-0195](./0195-educacao-em-saude.md));
  - os moradores por microárea, pelos membros de núcleo vigentes (Território, [ADR-0127](./0127-cadastro-territorial.md));
  - o andamento da meta da campanha operacional (P3, [ADR-0133](./0133-programas-campanhas.md)) e as inscrições no programa (P2).

## Decisão

1. **Metas da campanha de comunicação** (opcionais; só se editam na planejada, como o resto da campanha):
   - `metaPessoas`, as pessoas a alcançar nas ações educativas;
   - `metaAcoes`, as ações educativas a realizar;
   - zero ou negativo, 400.
2. **Apuração ao vivo** (em qualquer situação, sem tabela):
   - **Canais:** os planejados e os que tiveram publicação, com publicações, enviados, falhas e taxa de entrega. O canal que mais entregou fica
     em destaque. O Portal mostra só as publicações, porque não conta pessoas.
   - **Ações educativas:**
     - quantas previstas, realizadas, não realizadas e canceladas;
     - nas realizadas: as pessoas alcançadas (a regra da C3), a participação e os materiais distribuídos.
   - **Cobertura por território:**
     - entram os territórios alvo da campanha; sem alvo, os das microáreas das ações previstas ou realizadas;
     - cada território mostra as microáreas, as microáreas com ação realizada, a **cobertura** (a razão entre as duas), os moradores e as
       pessoas alcançadas;
     - a ação que atende dois territórios conta nos dois, e o total da campanha a conta uma vez só;
     - o território de menor cobertura vem primeiro, em destaque quando há mais de um;
     - a ação realizada sem microárea aparece à parte, como "Sem território".
   - **Metas:** atingida; em andamento; abaixo (só com a campanha encerrada ou cancelada); ou sem meta.
   - **Efetividade:** são indicadores de resultado associados à campanha, e a tela avisa que não provam causa.
     - Com campanha operacional ligada: o andamento da meta dela (atendidos sobre a meta).
     - Com programa ligado: as inscrições novas no período da campanha (até hoje), comparadas ao período anterior de mesma duração, na
       unidade da campanha ou na rede.
3. **Avaliação registrada** (`TB_AVALIACAO_CAMPANHA`, uma por campanha):
   - só na campanha encerrada (422);
   - o público-alvo atingido (sim, parcial ou não; 400 sem ele), os resultados, as conclusões (400 sem elas) e as recomendações;
   - **congela os números da apuração** no registro: pessoas, ações realizadas, enviados, cobertura e a situação das duas metas. Mexer numa
     ação depois muda a apuração ao vivo, mas não o julgamento registrado;
   - registrar de novo substitui o julgamento, congela os números outra vez e guarda quem atualizou e quando.
4. **Indicadores e BI (#19):** dois relatórios novos no catálogo da BI4 ([ADR-0167](./0167-bi-relatorios.md)), na categoria "Comunicação e
   educação". Os números vêm do serviço da avaliação; o `RelatorioService` só organiza.
   - **`CAMPANHAS_COMUNICACAO`:** as campanhas cujo período cruza o filtro, com a situação, as ações realizadas, as pessoas, os enviados, a
     cobertura, as metas e o público atingido. Os números são os da campanha inteira.
   - **`EDUCACAO_EM_SAUDE`:** as ações educativas realizadas no período, por tema (com a participação), por unidade e os materiais.
   - **Escopo** (`INDICADOR.CONSULTAR`, pelo `EscopoDosIndicadores`):
     - com unidade no filtro, entram as campanhas e as ações daquela unidade e das abaixo dela;
     - a campanha da rede, sem unidade, só entra sem o filtro.
   - O CSV fica na trilha de auditoria, como os demais.
5. **Acesso:** sem permissão nova. Apurar e registrar pedem `COMUNICACAO.PUBLICAR`, na unidade da campanha; a campanha da rede, a permissão
   em qualquer unidade.
6. **Telas:**
   - **Comunicação, aba Campanhas:**
     - a gaveta da campanha ganha as duas metas;
     - o detalhe ganha a seção **Avaliação**: pessoas, ações, enviados, cobertura e metas; a participação e os materiais; as tabelas de
       canais e de territórios, com o de menor cobertura em destaque; a efetividade com o aviso; e a avaliação registrada;
     - na campanha encerrada, o botão Avaliar campanha (Revisar avaliação, depois de registrada) abre o formulário.
   - **Relatórios:** a categoria "Comunicação e educação", com os dois relatórios.

### API

| Método | Caminho | Permissão |
|---|---|---|
| GET | `comunicacao/campanha/{id}/avaliacao` | `COMUNICACAO.PUBLICAR` (unidade da campanha) |
| POST | `comunicacao/campanha/{id}/avaliacao` (`publicoAtingido`, `resultados`, `conclusoes`, `recomendacoes`) | idem |
| GET | `relatorios/CAMPANHAS_COMUNICACAO` e `relatorios/EDUCACAO_EM_SAUDE` (`de`, `ate`, `unidadeId`; e `/exportacao`) | `INDICADOR.CONSULTAR` (escopo) |

Mudam também `POST` e `PATCH comunicacao/campanha`, que aceitam `metaPessoas` e `metaAcoes`, e `GET comunicacao/campanha`, que as devolve.

### Migração

`V80__avaliacao_da_comunicacao.sql`: as colunas `meta_pessoas` e `meta_acoes` em `tb_campanha_comunicacao` e a tabela
`tb_avaliacao_campanha`, com os CHECKs do público atingido e da situação das metas.

## Alternativas descartadas

- **Guardar a apuração em tabela atualizada a cada evento:** duplicaria o que as publicações e as ações já registram. A apuração é
  calculada; só o julgamento e os números do momento do registro ficam guardados.
- **Pesquisa de satisfação ou questionário de conhecimento:** pede instrumento validado e plataforma.
- **Análise estatística de causa:** a efetividade mostra associação no tempo, com o aviso.
- **Contagem de acessos ao Portal:** é analítica web. Fica com a C5, junto dos indicadores dos canais novos.
- **Repartir as pessoas de uma ação entre os territórios:** daria números fracionários sem base. A ação conta em cada território que atende,
  e o total da campanha a conta uma vez.

## Consequências

- **STATUS-DOS-DOMINIOS:** o domínio 28 passa a ✅; a C5 continua na lista do #20.
- **Implantação:** cadastrar as microáreas dos territórios (Território) para a cobertura ter denominador; informar as metas no planejamento.
- **Fora do escopo:** os indicadores de canais da C5 (SMS, notificação, aplicativo); as respostas do público.

## Testes

- **JUnit `AvaliacaoCampanhaControllerTest`:**
  - a meta zero (400) e a campanha com as metas;
  - a apuração com o e-mail em destaque e a taxa de entrega, o Portal sem contagem, as ações, a participação, o material, o território
    sem ação em destaque, a cobertura, a ação sem microárea, as metas em andamento e a efetividade;
  - a avaliação antes de encerrar (422), sem público atingido ou sem conclusões (400), e os números congelados depois de mexer na ação;
  - os dois relatórios, com a unidade e a exportação.
- **JUnit `AvaliacaoCampanhaAutorizacaoControllerTest`,** com a segurança ligada: sem login (401); o gestor na sua unidade e não na outra;
  o médico não apura nem avalia.
- **`RelatorioControllerTest` e `RelatorioAutorizacaoControllerTest`** passam a cobrir os dois relatórios.
- **Robot:** `AvaliacaoCampanha.robot` (API, CT-001); `Relatorios.robot` com os sete relatórios; CT-005 de `UI_comunicacao.robot`; CT-005 de
  `UI_relatorios.robot`.
