# 0074 — Telas do Administrativo agrupadas com o layout recente

## Status

Aceita e implementada. Aplica ao Administrativo o que a [ADR-0073](./0073-catalogos-de-rh-em-telas-com-abas.md)
fez nos catálogos de RH.

## Contexto

O menu Administrativo tinha **7 telas** da primeira geração ([ADR-0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md),
fases F1 a F6):
- Setores;
- Capacidades administrativas;
- Perfis administrativos;
- Perfil por tipo de unidade;
- Processos administrativos;
- Responsabilidades administrativas;
- Necessidades de pessoal.

Todas tinham a forma antiga: tabela solta, modal centralizado, nenhum indicador e nenhum estado vazio
orientado. Quatro delas descrevem o mesmo assunto, o **modelo** do que um tipo de unidade faz
administrativamente, e são configuração rara. As responsabilidades são dadas **dentro de um setor**,
mas ficavam numa tela separada.

## Decisão

1. **Agrupar por assunto.** O menu cai de 7 para 3 itens:

   | Tela nova | Junta | Forma |
   |---|---|---|
   | **Setores** (`/administrativo/setores`) | Setores, Responsabilidades administrativas | Abas Setores e Responsabilidades. Na segunda, escolhe-se o profissional (busca por nome ou matrícula) e vê-se o histórico dele: vigentes e encerradas |
   | **Modelo administrativo** (`/administrativo/modelo`) | Capacidades, Processos, Perfis administrativos, Perfil por tipo de unidade | Quatro abas. "Processos" na linha da capacidade abre a aba Processos já filtrada. Perfil por tipo lista os 8 tipos de unidade de saúde, com "Sem perfil" nos que faltam |
   | **Necessidades de pessoal** (`/administrativo/necessidades-de-pessoal`) | (continua sozinha) | É o pedido operacional da unidade ao RH, com outro uso |

2. **Layout recente em todas**, o mesmo de RH, Farmácia e Auditoria: cabeçalho com a ação principal,
   quatro indicadores, abas com contagem e setas, card com filtros, tabela em `tbl-wrap`, badges, estado
   vazio e **cadastro, edição, atribuição, encerramento e vínculo em gaveta** (`app-drawer` + `dw-*`).
   Os indicadores são:
   - **Setores:** ativos, unidades com setor, divisão por tipo, setores sem responsável.
   - **Modelo:** capacidades ativas, processos (e capacidades sem processo), perfis ativos, tipos de
     unidade com perfil de 8.
   - **Necessidades:** registradas, posições e horas semanais pedidas, sem vaga vinculada (o que espera o
     RH) e com vaga.
3. **Ajudas de preenchimento que não mudam regra:**
   - na necessidade de pessoal, o setor só oferece os da unidade escolhida;
   - ao vincular vaga, as vagas da mesma unidade e cargo aparecem primeiro;
   - necessidades sem vaga aparecem no topo da lista.
4. **Permissões na interface (ADR-0068):**
   - Setores é aberta a todo usuário logado. Criar e editar setor exige `ORGANIZACAO.GERENCIAR`;
     atribuir e encerrar responsabilidade exige `ADMINISTRATIVO.GERENCIAR`.
   - Modelo administrativo aparece com `ADMINISTRATIVO.*`, e cadastro e edição exigem
     `ADMINISTRATIVO.GERENCIAR`.
   - Necessidades aparecem com `ADMINISTRATIVO.*` ou `RH.*`, e as ações exigem
     `ADMINISTRATIVO.GERENCIAR` ou `RH.GERENCIAR`.
5. **Rotas antigas redirecionam:**

   | Rota antiga | Vai para |
   |---|---|
   | `/administrativo/responsabilidades` | `/administrativo/setores?aba=responsabilidades` (mantém `?matricula=`) |
   | `/administrativo/capacidades` | `/administrativo/modelo?aba=capacidades` |
   | `/administrativo/processos` | `/administrativo/modelo?aba=processos` |
   | `/administrativo/perfis` | `/administrativo/modelo?aba=perfis` |
   | `/administrativo/perfis-por-tipo-unidade` | `/administrativo/modelo?aba=tipos` |

   Os 5 componentes antigos foram removidos.
6. **Modelo administrativo e Necessidades de pessoal são carregadas sob demanda.** O bundle inicial caiu de
   980 kB para 954 kB.
7. **Nenhuma regra de negócio mudou.** Os endpoints, os campos e as validações são os mesmos. As validações
   agora também são conferidas na gaveta antes do envio.

## Trade-offs considerados

**Responsabilidades como aba de Setores (escolhida)** × **tela própria**
- ✅ A responsabilidade é dada num setor, e quem mantém setores é quem atribui responsáveis.
- ❌ A consulta continua sendo por profissional, e não por setor, porque a API só lista o histórico de uma
  matrícula (`GET .../profissional/{matricula}`). Uma visão "responsáveis
  deste setor" precisa de endpoint novo.

**Quatro catálogos numa tela (escolhida)** × **uma tela por catálogo**
- ✅ Tudo é o mesmo modelo e é configuração rara. Juntos, dá para ver o que falta: capacidade sem processo
  e tipo de unidade sem perfil.

## Consequências

**Positivas:**
- O Administrativo inteiro tem a mesma cara das telas novas.
- O menu tem 3 itens.

**Negativas / pendências:**
- Não há lista de responsáveis por setor (ver trade-off acima).
- A tela não foi conferida visualmente no navegador por quem implementou. A conferência fica com o revisor.

## Referências

- [ADR-0073](./0073-catalogos-de-rh-em-telas-com-abas.md): o mesmo agrupamento no RH.
- [ADR-0038](./0038-app-shell-dinamico-e-telas-de-frontend-do-setor-administrativo.md): as telas originais.
- ADRs [0030](./0030-setor-administrativo-e-relacao-com-unidade-de-saude.md) a [0036](./0036-necessidade-de-pessoal-encaminhada-ao-rh.md): as regras, inalteradas.
- [ADR-0068](./0068-tela-usuarios-e-perfis.md): menu por permissão.
