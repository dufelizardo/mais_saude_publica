# 0073 — Catálogos de RH agrupados em telas com o layout recente

## Status

Aceita e implementada. Segunda de três entregas da padronização das telas de RH. A primeira é a
[ADR-0072](./0072-tela-profissionais-a-partir-do-prototipo.md) (Profissionais). A última é o cabeçalho do
perfil do profissional.

## Contexto

Depois da ADR-0072, o menu de RH ainda tinha **9 telas** da primeira geração:
- Perfil do profissional;
- Categorias salariais, Cargos (com a Tabela salarial em rota própria) e Regras de anuênio;
- Folha de pagamento;
- Catálogo de treinamentos e Ciclos de avaliação;
- Vagas (com Candidatos em rota própria);
- Tipos de benefício (com Valores em rota própria).

Todas tinham a mesma forma antiga: cabeçalho simples, tabela solta, formulário em modal centralizado
(`form-group`, `form-banner`), sem indicadores e sem estado vazio orientado. Várias eram partes de um mesmo
assunto espalhadas pelo menu.

## Decisão

1. **Agrupar por assunto.** O menu de RH passa de 9 itens (além de Profissionais) para 5:

   | Tela nova | Junta | Forma |
   |---|---|---|
   | **Cargos & salários** (`/rh/cargos-e-salarios`) | Categorias salariais, Cargos, Tabela salarial, Regras de anuênio | Abas Cargos, Categorias e Anuênio; a **tabela salarial abre em gaveta** ao clicar no cargo |
   | **Benefícios** (`/rh/beneficios`) | Tipos de benefício, Valores do benefício | Lista; os **valores abrem em gaveta** ao clicar no benefício |
   | **Desenvolvimento** (`/rh/desenvolvimento`) | Catálogo de treinamentos, Ciclos de avaliação | Abas |
   | **Recrutamento** (`/rh/recrutamento`) | Vagas, Candidatos | Lista; os **candidatos abrem em gaveta** ao clicar na vaga |
   | **Folha de pagamento** (`/rh/folha-pagamento`) | (continua sozinha) | É a operação do mês, com outro uso |

   **"Perfil do profissional" sai do menu.** Ele só existe com um profissional escolhido, e o caminho é o
   cartão da tela Profissionais.
2. **Layout recente em todas**, o mesmo de Farmácia, Usuários & Perfis e Auditoria:
   - **Cabeçalho** com a ação principal.
   - **Quatro indicadores** tirados dos próprios dados:
     - categorias, convenções e cargos;
     - custeio dos benefícios;
     - treinamentos obrigatórios e ciclos em andamento;
     - vagas abertas e posições a preencher;
     - na Folha, proventos, descontos, encargos e total da competência.
   - **Abas** (`page-tabs`) com contagem e navegação por setas.
   - **Card** com título, subtítulo e filtros (`card__head` + `filters`), tabela em `tbl-wrap`, badges e
     estado vazio que diz o que fazer.
   - **Cadastro, edição e subpáginas em gaveta** (`app-drawer` + `dw-*`), no lugar do modal antigo. As
     gavetas de histórico (tabela salarial, valores, candidatos) mostram o valor vigente em destaque, o
     histórico e o formulário do próximo registro no mesmo painel.
   - Ações de cadastro e edição só com `RH.GERENCIAR` (ADR-0068).
3. **Folha de pagamento:**
   - abre no **mês corrente**, sem precisar digitar a competência;
   - navega entre meses com setas ou pelo seletor de mês;
   - tem busca e totais no rodapé da tabela;
   - sem folha no mês, o estado vazio leva a Profissionais, porque o registro é feito no perfil.
4. **Rotas antigas redirecionam** para a aba ou a gaveta certa:

   | Rota antiga | Vai para |
   |---|---|
   | `/rh/categorias-salariais` | `/rh/cargos-e-salarios?aba=categorias` |
   | `/rh/cargos` | `/rh/cargos-e-salarios` |
   | `/rh/cargos/:id/tabela-salarial` | `/rh/cargos-e-salarios?cargo=:id`, que abre a gaveta |
   | `/rh/regras-anuenio` | `/rh/cargos-e-salarios?aba=anuenio` |
   | `/rh/tipos-beneficio` | `/rh/beneficios` |
   | `/rh/tipos-beneficio/:id/valores` | `/rh/beneficios?tipo=:id` |
   | `/rh/treinamentos` | `/rh/desenvolvimento` |
   | `/rh/ciclos-avaliacao` | `/rh/desenvolvimento?aba=ciclos` |
   | `/rh/vagas` | `/rh/recrutamento` |
   | `/rh/vagas/:id/candidatos` | `/rh/recrutamento?vaga=:id` |

   Os 10 componentes antigos foram removidos.
5. **As 4 telas novas são carregadas sob demanda.** O bundle inicial voltou para 980 kB, abaixo do limite
   de 1 MB.
6. **Nenhuma regra de negócio mudou.** São os mesmos endpoints, os mesmos campos e as mesmas validações,
   agora também conferidas na gaveta antes de enviar. Valores em reais aceitam "4.850,00".

## Trade-offs considerados

**Gaveta (escolhida)** × **manter o modal nos catálogos**
- ✅ Uma forma só para todo o sistema. A gaveta mantém a lista visível e cabe o histórico junto ao
  formulário (tabela salarial, valores, candidatos), o que o modal não comportava.
- ❌ Para um cadastro de dois campos, a gaveta é maior que o necessário. A consistência pesou mais.

**Subpágina em gaveta (escolhida)** × **rota própria**
- ✅ Não se perde o contexto da lista, e a rota antiga continua funcionando por redirecionamento.

## Consequências

**Positivas:**
- O RH inteiro, exceto o perfil, tem a mesma cara das telas novas.
- O menu de RH fica com 6 itens, cada um com um assunto.

**Negativas / pendências:**
- **Perfil do profissional** (13 abas, modais antigos): a próxima entrega, só o cabeçalho.
- Treinamento, ciclo e tipo de benefício continuam sem edição, porque a API não tem esse endpoint.

## Referências

- [ADR-0072](./0072-tela-profissionais-a-partir-do-prototipo.md): Profissionais.
- [ADR-0058](./0058-tela-da-farmacia-abas-gaveta-lateral-e-livro-de-estoque.md): o padrão de gaveta.
- [ADR-0063](./0063-tela-atendimentos-do-agendamento-ao-prontuario.md): telas soltas virando abas e gavetas.
- ADRs [0019](./0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md) a [0028](./0028-fatia-9-beneficios.md): as regras de cada catálogo, inalteradas.
