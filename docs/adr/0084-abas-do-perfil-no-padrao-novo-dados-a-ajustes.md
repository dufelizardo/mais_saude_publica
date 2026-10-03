# 0084 — Abas do perfil do profissional no padrão novo (Dados a Ajustes)

## Status

Aceita e implementada (parte 1 de 3). Continua a [ADR-0075](./0075-cabecalho-do-perfil-do-profissional.md),
que refez só o cabeçalho do perfil e deixou as 13 abas na primeira geração.

## Contexto

- As abas do perfil usam o visual antigo:
  - formulários soltos abaixo das tabelas (`form-group`, `form-banner`);
  - estilos inline;
  - tabelas sem `tbl-wrap`, sem badge de situação e sem estado vazio.
- O "Editar contato" abria um `app-modal`, enquanto as telas refeitas usam gaveta.
- Depois de registrar, nada avisava que deu certo: o formulário só esvaziava.
- São 13 abas e cerca de 1.500 linhas de template. Refazer tudo num PR só dificulta a revisão.

## Decisão

1. **Três entregas:**
   - **A, esta:** Dados, Lotação, Composição remuneratória e Ajustes individuais.
   - **B:** Afastamentos e Licenças, Ponto, Folha de pagamento e SST.
   - **C:** Treinamentos, Avaliações, Benefícios, Desligamento/Rescisão e Histórico funcional.
2. **Cada aba segue o mesmo desenho:**
   - **Cabeçalho da seção** (`card__head`): título, subtítulo com a regra de negócio em uma linha e a
     ação principal, como "+ Registrar transferência" ou "+ Registrar ajuste".
   - **A ação só aparece com `RH.GERENCIAR`** (ADR-0079).
   - **Tabela** em `tbl-wrap`, com situação em badge (Vigente ou Encerrada), período numa coluna só
     ("dd/mm/aaaa a dd/mm/aaaa" ou "em diante") e estado vazio (`empty`).
   - **Destaque** (`perfil-destaque`) para o valor central da aba, como a lotação vigente ou o total
     da composição.
3. **Formulários em gaveta (`app-drawer`):**
   - Os inline viram gaveta: transferência e ajuste.
   - O modal de contato também vira gaveta.
   - Campos `f-<campo>`, erros `e-<campo>` ligados por `aria-describedby`, rodapé com Cancelar e
     Salvar.
   - A data de início já vem com hoje.
   - A gaveta de transferência avisa qual lotação será encerrada.
4. **Aviso de sucesso:** depois de salvar, a gaveta fecha e um toast (`role=status`) confirma
   "Contato atualizado", "Transferência registrada" ou "Ajuste registrado".
5. **Nenhuma regra muda:**
   - mesmas validações e os mesmos serviços;
   - as mesmas recargas de lotação, composição e ajustes.
   - Os dois modais que restam (correção de ponto e encerrar adesão) saem nas partes B e C.

## Consequências

- As quatro primeiras abas ficam iguais às telas refeitas do RH e do Administrativo.
- Até as partes B e C, o perfil mistura os dois visuais nas abas seguintes.
- `Modal` continua importado no componente até a parte C.

## Testes

- **Robot de interface** (`test/ui/rh/perfil/UI_perfil_abas_dados_a_ajustes.robot`):
  - registrar ajuste pela gaveta (aviso e linha nova);
  - editar o contato pela gaveta (aviso e e-mail novo);
  - gaveta de ajuste recusa envio sem valor (mensagem do campo, gaveta aberta).
- Sem backend novo, portanto sem JUnit nem Robot de API.
