# 0072 — Tela Profissionais a partir do protótipo

## Status

Aceita e implementada. Primeira de três entregas da padronização das telas de Recursos Humanos. As
próximas são os catálogos de RH agrupados em telas com abas e o cabeçalho do perfil do profissional.

## Contexto

As telas de RH são as mais antigas do frontend (ADRs 0018 a 0029). Elas vieram antes do padrão que as
telas mais recentes seguem: Farmácia, Atendimentos, Usuários & Perfis e Auditoria.
- A lista de profissionais era uma tabela simples: sem lotação, sem situação e com o CPF sem máscara.
- Cadastrar e Desligar eram páginas inteiras e soltas no menu.

O usuário trouxe o protótipo `Profissionais.html`. Ele tem:
- indicadores;
- abas de categoria com contagem;
- filtros em "pílulas";
- alternância entre **cartões** e lista;
- cada cartão com avatar e ponto de situação, etiquetas de categoria e situação, conselho, unidade,
  telefone, carga semanal e ações;
- paginação.

## Decisão

1. **A grade de cartões é a visão padrão**, e a lista é a alternativa. A escolha fica guardada no
   navegador. O cartão é o elemento principal do protótipo e não foi trocado por uma tabela.
2. **Só aparece o que o sistema tem. O resto fica "Em breve".**

   | No protótipo | Na tela |
   |---|---|
   | Categoria (Médicos, Enfermagem…) | `conselhoClasse`: CRM → Médicos, COREN → Enfermagem, CRO → Odontologia, CRF → Farmácia, outro conselho → Outros conselhos, sem conselho → Apoio e administrativo. Só aparecem as abas das categorias que existem no quadro |
   | Conselho | `conselhoClasse` + `numeroConselho`; sem conselho, a matrícula |
   | Unidade e função | lotação vigente: unidade e cargo |
   | Carga semanal | **jornada contratual** da lotação (`jornadaSemanalHoras`) e "na lotação desde". A barra de ocupação e a mini-semana dependem de Escalas, que ainda não existe, e não entram |
   | Situação (ponto e etiqueta) | Ativo; Em férias até dd/mm; Afastado até dd/mm; Sem lotação vigente; Desligado em dd/mm/aaaa |
   | Indicadores | ativos (e admissões no mês), médicos e enfermagem (com % do quadro), e em férias ou afastados agora |
   | Exportar, Importar CSV, Especialidade, Ver escala, Atribuir equipe | "Em breve". Equipe e Escala são domínios futuros |

   **Licença médica não é nomeada no cartão**, só "Afastado até". Motivo de saúde é dado sensível. O tipo
   do afastamento continua no perfil, para quem tem acesso ao RH.
3. **`GET /api/v1/profissional/quadro`**:
   - devolve cada profissional com a **lotação vigente** (unidade, cargo, jornada, início) e o
     **afastamento em curso hoje** (aprovado ou em andamento, com o período cobrindo a data);
   - faz três consultas no total, sem uma por profissional;
   - exige `RH.CONSULTAR` ou `RH.GERENCIAR` (ADR-0067). O `GET /profissional/` continua aberto a quem
     está logado, porque as telas clínicas o usam para achar a matrícula;
   - lista vazia → 404, como nas outras listagens.
4. **Cadastro e desligamento viram gavetas desta tela**, como a tela de Atendimentos absorveu outras
   (ADR-0063).
   - "Novo profissional" abre o cadastro na gaveta larga. Os dados são os mesmos de antes, incluindo a
     lotação de admissão (ADR-0019) e o preenchimento automático pelo CEP.
   - "Desligar" fica no menu "⋯" do cartão.
   - As rotas antigas `/profissionais/novo` e `/profissionais/desligar` redirecionam para
     `/profissionais?acao=…`, que abre a gaveta.
   - Os componentes antigos foram removidos, e o menu de RH perdeu esses dois itens.
   - As ações de cadastro e desligamento só aparecem com `RH.GERENCIAR` (ADR-0068).
5. O CSS foi portado do protótipo para `styles.css` (bloco "Profissionais"). Foram reaproveitados o
   `.toolbar` e o `.seg`, que já existiam, e o padrão de paginação e de gaveta das telas novas.

## Trade-offs considerados

**Categoria pelo conselho (escolhida)** × **pelo cargo**
- ✅ O conselho de classe é o que define a profissão regulamentada (CRM, COREN…) e não depende de como
  cada município nomeia cargos.
- ❌ Técnico e auxiliar de enfermagem também têm COREN e ficam em "Enfermagem". A função aparece no
  cartão (o cargo) e no filtro de Função.

**Jornada contratual (escolhida)** × **carga realizada com barra**
- ✅ Só mostra o que existe. A carga realizada e a semana dependem de Escalas e voltam quando esse
  domínio existir.

## Consequências

**Positivas:**
- A tela mais usada do RH fica no padrão das telas novas.
- A lotação e o afastamento aparecem sem abrir o perfil.
- O menu de RH perde dois itens.

**Negativas / pendências:**
- Equipes e Escalas (itens do protótipo) são domínios novos, sem data.
- Exportar e importar ficam "Em breve".

## Referências

- [ADR-0018](./0018-app-shell-e-decisoes-de-frontend-do-modulo-rh.md) e [ADR-0029](./0029-listagem-e-edicao-de-contato-do-profissional.md): a lista anterior.
- [ADR-0019](./0019-vincular-cargo-e-lotacao-no-cadastro-de-profissional.md): lotação na admissão.
- [ADR-0063](./0063-tela-atendimentos-do-agendamento-ao-prontuario.md): o mesmo movimento de telas soltas virando gavetas.
- [ADR-0067](./0067-autorizacao-aplicada-nas-rotas-e-no-escopo.md) e [ADR-0068](./0068-tela-usuarios-e-perfis.md): permissão da rota e ações escondidas.
