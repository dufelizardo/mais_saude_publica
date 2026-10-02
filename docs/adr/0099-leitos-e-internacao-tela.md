# 0099 — Leitos e internação: tela

## Status

Aceita e implementada (parte 2 de 3). Usa a API da [ADR-0098](./0098-leitos-e-internacao-backend.md). As
internações no prontuário e o vínculo assistencial de quem cuida do internado vêm na ADR-0100.

## Contexto

- A ADR-0098 criou leitos, internação, troca de leito, alta, higienização, bloqueio, mapa e indicadores.
- Não há protótipo de leitos. A tela segue o padrão das telas da Assistência: indicadores, abas, `card` e
  gavetas `dw-*`.
- O mapa usa cartões, como o protótipo de Profissionais (ADR-0072): cada leito é uma unidade visual com
  estado e ação, e uma tabela esconderia a disposição por setor.

## Decisão

1. **Tela Leitos** em `/assistencia/leitos`, no grupo Assistência, depois do Laboratório.
   - Aparece com qualquer permissão `INTERNACAO.*` ou com `LEITO.GERENCIAR`.
   - Carrega sob demanda.
2. **Indicadores:**
   - taxa de ocupação, com ocupados sobre leitos em operação;
   - leitos livres;
   - em higienização e bloqueados;
   - média de permanência e altas dos últimos 30 dias.
   - Os indicadores seguem o filtro de unidade.
3. **Filtro de unidade** (só as unidades que têm leito) e uma legenda das cores.
4. **Aba Mapa de leitos:**
   - um bloco por setor ("x de y ocupados") e um cartão por leito, com a cor da situação na borda;
   - o cartão mostra a identificação, o tipo e o sexo da enfermaria;
   - **ocupado:** o paciente, os dias de internação (D+n), a alta prevista e o médico;
   - **bloqueado:** o motivo.
   - A ação depende da situação:

     | Situação | Ações |
     |---|---|
     | Livre | **Internar** e **Bloquear** |
     | Ocupado | **Abrir** a internação |
     | Em higienização | **Liberar** (registra a limpeza) e **Bloquear** |
     | Bloqueado | **Desbloquear** |
5. **Aba Internações:**
   - lista sem o motivo nem o sumário;
   - busca por paciente, leito, setor ou CID;
   - filtro de situação, que abre em **Internados**.
6. **Aba Cadastro de leitos**, só com `LEITO.GERENCIAR`: tabela com **+ Leito** e **Editar**.
7. **Gavetas:**
   - **Internar:**
     - paciente com busca;
     - o leito já escolhido no mapa, ou uma lista de leitos **livres e compatíveis com o sexo do paciente**,
       a mesma regra do backend;
     - CID-10 principal, médico responsável, motivo, caráter e previsão de alta.
   - **Internação** (larga):
     - dados, motivo, sumário de alta e movimentos;
     - os botões **Trocar de leito** (`INTERNACAO.GERENCIAR`) e **Dar alta** (`INTERNACAO.ALTA`);
     - leitura auditada.
   - **Trocar de leito:** leitos livres e compatíveis da mesma unidade, motivo e matrícula. Avisa que o
     leito atual vai para higienização.
   - **Dar alta:**
     - tipo, data e hora (vazio é agora; futuro é recusado), sumário e matrícula do médico;
     - avisa que a alta encerra a internação.
   - **Bloquear**, com motivo.
   - **Leito:**
     - unidade e setor assistencial, que só podem ser escolhidos no cadastro;
     - identificação, tipo, enfermaria e "Em uso".
8. **Internar no atendimento:** o detalhe do atendimento ganha **Internar** (com `INTERNACAO.GERENCIAR`). Ele abre
   `/assistencia/leitos?acao=internar` com o paciente, a unidade e o atendimento, e a internação fica ligada
   ao atendimento.
9. As matrículas das gavetas vêm preenchidas com a de quem está logado, quando é profissional.

## Consequências

- O mapa de leitos passa a ser o ponto de trabalho do NIR e da enfermagem: internar, trocar, liberar depois
  da limpeza e bloquear, sem sair da tela.
- Até a ADR-0100, a internação ainda não aparece no prontuário.

## Testes

- **Robot de interface** (`test/ui/assistencia/leitos/UI_leitos.robot`, 7 casos):
  - internação pelo leito livre do mapa;
  - internação vazia recusada;
  - alta e liberação depois da higienização;
  - troca de leito;
  - bloqueio e desbloqueio;
  - cadastro de leito;
  - internação a partir do atendimento.
