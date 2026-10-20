# 0128 — Território: adscrição e cobertura (F3b)

## Status

Aceita e implementada. É a segunda parte da fatia F3 da [ADR-0108](./0108-territorio-e-adscricao.md), sobre a
[ADR-0127](./0127-cadastro-territorial.md); detalhe em [`territorio/MODELO-TERRITORIO.md`](../territorio/MODELO-TERRITORIO.md).

## Contexto

- **A F3a (ADR-0127) pôs as pessoas no território:** imóvel, domicílio, família e membros.
- **Faltava dizer quem responde por elas:** a adscrição à equipe, com histórico, que a ADR-0108 separa do território
  ("onde fica" contra "quem responde, por quem, desde quando").
- **A P2 (ADR-0119) deixou a lista de elegíveis para depois da adscrição:** sem território, a lista seria a base
  inteira da rede.
- **Equipes mostrava "Famílias e pacientes por microárea: em breve"**, à espera do cadastro.

## Decisão

1. **`Adscricao`** (migração V21):
   - **Campos:** paciente, equipe, microárea (quando vem do território), **origem** (`TERRITORIO · MANUAL`), motivo
     (da manual), início, fim e motivo do fim.
   - **Uma vigente por paciente,** com índice único parcial.
   - **A carga** adscreve quem já mora num domicílio de área com equipe.
2. **Adscrição pelo território, calculada:**
   - **Regra:** morar num domicílio de área com equipe responsável adscreve o paciente a essa equipe, na microárea do
     domicílio.
   - **O que recalcula, fechando e reabrindo com histórico:**
     - entrar numa família;
     - mudar de domicílio (de microárea ou de área);
     - sair da família (sem domicílio, sem adscrição);
     - mudar o imóvel de microárea;
     - **trocar a equipe da área** (F1), que leva todos os moradores à nova equipe.
   - **Área sem equipe:** não adscreve, e quando a equipe chega, adscreve os moradores.
3. **Adscrição manual:**
   - **Para quem:** quem não tem domicílio no território (a população em situação de rua do Consultório na Rua) ou
     escolheu outra equipe.
   - **Motivo obrigatório** (400), e equipe ativa (422).
   - **Ao registrar,** a vigente, de qualquer origem, fecha na data.
   - **A calculada não substitui a manual:** entrar numa família não a muda.
   - **Encerrar a manual** devolve o paciente à adscrição do território, se ele mora em área com equipe. A do
     território não se encerra por aqui (422): ela segue a família e a equipe da área.
4. **Cobertura** (só números, aberta a quem está logado):
   - **por equipe:** pessoas adscritas (e quantas manuais), famílias, imóveis e microáreas sem cadastro;
   - **por microárea:** pessoas, famílias, imóveis, ACS e a cobertura em % (pessoas cadastradas sobre a população de
     referência).
5. **Elegíveis da P2:**
   - **O que lista:** `programa/{id}/elegiveis?equipeId&microareaId` traz os adscritos vigentes da equipe (ou da
     microárea) no público do programa que ainda não estão inscritos.
   - **Critérios:** avalia **idade e sexo**. **Gestação e condição clínica** vêm como critérios não avaliados (não há
     registro estruturado delas) e a tela pede confirmação. Quem não tem data de nascimento fica de fora e é contado.
   - **Acesso:** `PROGRAMA.ACOMPANHAR` na unidade da equipe, com leitura auditada.
6. **Acesso:**
   - **Ver a equipe de referência** (`paciente/{id}/adscricao`) é de quem consulta pacientes (`PACIENTE.CONSULTAR`) ou
     cadastra ou gere o território, com leitura auditada.
   - **A adscrição manual** pede `TERRITORIO.CADASTRAR` na unidade da equipe.
7. **API** (`/api/v1/territorio/`):
   - `paciente/{id}/adscricao`: GET para ler, POST para a manual, e `/encerrar`;
   - `equipe/{id}/cobertura`;
   - e `/api/v1/programa/{id}/elegiveis`.
8. **Telas:**
   - **Pacientes:** o resumo ganha "Equipe de referência":
     - a equipe, se pelo domicílio ou manual, a microárea, o ACS e onde mora (com o link para o Território);
     - o histórico;
     - "Adscrever manualmente" e "Encerrar adscrição manual".
   - **Equipes:** cada microárea mostra as pessoas cadastradas, e o rodapé mostra pessoas adscritas, famílias, imóveis
     e microáreas sem cadastro, no lugar do "em breve".
   - **Programas:** o detalhe ganha "Elegíveis", com a escolha de equipe e microárea, os critérios avaliados e não
     avaliados e "Inscrever", que abre a inscrição já com o paciente, a unidade e a equipe.
   - **Território:** cada microárea mostra pessoas e famílias cadastradas.

## Consequências

- **Cada pessoa cadastrada no território tem equipe de referência,** com o histórico de quem respondeu por ela.
- **A cobertura sai do cadastro, não de estimativa.**
- **Os programas ganham busca ativa de quem cabe no público.**
- **Destrava** a F4 (escopo territorial do ACS e da equipe), a V4 (zoonoses, pelo imóvel) e a E4 (vulneráveis por
  território).
- **Fora do escopo:**
  - critérios clínicos dos elegíveis (gestação, condição), que pedem registro estruturado;
  - o escopo de acesso por microárea (F4);
  - indicadores de cobertura no tempo (P6).

## Testes

- **JUnit:**
  - **`AdscricaoControllerTest`:**
    - a adscrição na entrada, na mudança de microárea, na troca de equipe da área e o fim na saída;
    - a manual que a calculada não troca, a volta à do território e a do território que não se encerra;
    - a cobertura (pessoas, famílias, microárea sem cadastro, %);
    - os elegíveis (idade, condição não avaliada, inscrito sai da lista, filtro de microárea).
  - **`CadastroTerritorialAutorizacaoControllerTest`:** o enfermeiro de outra unidade lê a equipe de referência e não
    adscreve à mão; o ACS adscreve.
- **Robot:**
  - **de API:** `test/territorio/Adscricao.robot`;
  - **de interface:** `UI_territorio.robot`, CT-012 a CT-014 (equipe de referência na ficha, adscrição manual,
    elegíveis e inscrição).
