# 0083 — Edição de catálogos de RH e responsáveis por setor

## Status

Aceita e implementada. Fecha três pendências pequenas de API das ADRs
[0073](./0073-catalogos-de-rh-em-telas-com-abas.md) e [0074](./0074-telas-do-administrativo-agrupadas.md).

## Contexto

- **Treinamento, ciclo de avaliação e tipo de benefício só podiam ser criados.** Um erro de digitação no
  nome, ou um ciclo com a data errada, só se corrigia direto no banco (ADR-0073).
- **Não dava para ver quem é responsável por um setor.** A API só listava o histórico de um profissional,
  e a tela Setores levava apenas ao responsável principal (ADR-0074).
- **A criação de ciclo aceitava fim antes do início.**

## Decisão

1. **Edição dos três catálogos:** `PATCH /treinamento/{uuid}`, `/ciclo-avaliacao/{uuid}` e
   `/tipo-beneficio/{uuid}`.
   - Mesmos campos e regras da criação; exige `RH.GERENCIAR`.
   - Inexistente → 404. Nome já usado por outro item → 409, pela restrição única que já existia.
   - Os vínculos não mudam: participações, avaliações e valores continuam apontando para o mesmo item,
     agora com o nome corrigido.
2. **O ciclo passa a recusar fim antes do início (400),** tanto na criação quanto na edição. A tela já
   validava isso; agora a API também valida.
3. **Responsáveis de um setor:** `GET /responsabilidade-administrativa/setor/{setorId}`.
   - Traz as vigentes primeiro e depois as encerradas, cada grupo da mais recente para a mais antiga.
   - Exige `ADMINISTRATIVO.CONSULTAR` ou `ADMINISTRATIVO.GERENCIAR`.
   - Setor inexistente ou sem responsabilidade → 404, como a consulta por profissional.
4. **Telas:**
   - **Desenvolvimento:** botão "Editar" nas linhas de treinamento e de ciclo, na mesma gaveta do
     cadastro.
   - **Benefícios:** botão "Editar" na linha do tipo, para nome e custeio. Os valores seguem no histórico
     próprio.
   - **Setores:** botão "Responsáveis" em todo setor, que abre uma gaveta com as vigentes e as encerradas.
     O "Histórico" de cada pessoa leva à aba Responsabilidades.

## Consequências

- Os catálogos de RH podem ser corrigidos pela tela, sem perder histórico.
- O setor mostra quem responde por ele, inclusive quem respondia antes.

## Testes

- **JUnit:**
  - edição com sucesso, 404 e 409 (treinamento);
  - ciclo invertido dá 400 na criação e na edição;
  - edição de tipo de benefício;
  - ordem das responsabilidades do setor (vigente antes de encerrada) e 404.
- **Robot de API:**
  - edição dos três catálogos (200 e 404; 400 no ciclo);
  - responsáveis do setor (200 e 404).
- **Robot de interface:**
  - editar treinamento e tipo de benefício pela gaveta;
  - gaveta de responsáveis do setor.
