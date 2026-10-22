# 0129 — Território: escopo territorial de acesso (F4)

## Status

Aceita e implementada (fatia F4 da [ADR-0108](./0108-territorio-e-adscricao.md); fecha o domínio #21 com as ADRs
[0125](./0125-territorio-fundacao.md) a [0128](./0128-adscricao-e-cobertura.md)).

## Contexto

- **Até a F3,** o cadastro territorial (imóvel, domicílio, família) valia na unidade da equipe da área. Um ACS da
  unidade via e cadastrava em todas as microáreas.
- **A ADR-0108 prevê o território como dimensão do escopo:** o ACS na sua microárea, a equipe na sua área. Mas
  **microárea não é autorização**: só estreita o que papel, permissão e unidade já dão, e entra atrás de interruptor,
  como o prontuário por vínculo (ADR-0076).
- **A adscrição (F3b) diz qual equipe responde por cada pessoa.** O prontuário por vínculo ainda não a usava.
- **Os campos de texto `Equipe.microareas` e `MembroEquipe.microarea` (ADR-0103)** só eram leitura derivada desde a
  F1 (ADR-0125), à espera da remoção.

## Decisão

1. **Interruptor `app.security.escopo-territorial.enabled`** (desligado por padrão). Só age com o login e a
   autorização ligados. Desligado, vale o escopo por unidade, como antes.
2. **Alcance de quem está logado** (componente `EscopoTerritorial`). Vem das **participações vigentes nas equipes**: o
   usuário se liga ao profissional pelo CPF.
   - **ACS:** as microáreas atribuídas a ele (F1).
   - **Outros membros da equipe:** a área da equipe.
   - **Quem gere o território (`TERRITORIO.GERENCIAR`: gestor, coordenação):** sem restrição territorial.
   - **Quem cadastra sem equipe vigente:** nada (403, "sem equipe no território").
3. **O que o escopo estreita:**
   - **Cadastro territorial:**
     - a lista de imóveis da área vem filtrada pelas microáreas alcançadas;
     - a ficha do imóvel e a família do paciente fora do alcance dão 403;
     - imóvel, domicílio, família, pessoa, saída, troca de responsável e mudança só no alcance (403). Na mudança, valem
       a origem e o destino.
   - **Adscrição manual:** só para a própria equipe.
   - **Elegíveis dos programas:** a própria equipe; o ACS vê só os adscritos da sua microárea.
4. **O que o escopo não estreita:**
   - **O cadastro e a busca de pacientes** (o ACS precisa achar a pessoa para pô-la na família, e quem não está
     cadastrado não tem microárea);
   - **a árvore e a cobertura**, que só têm contagens;
   - **a equipe de referência do paciente**, que é de quem consulta pacientes.
5. **A adscrição vira vínculo assistencial** (ADR-0076, regra 7): ser membro vigente da equipe a que o paciente está
   adscrito dá o vínculo para o prontuário.
   - **A eSF lê o prontuário da sua população sem atendimento prévio.**
   - **Vale com o interruptor do prontuário por vínculo,** porque é uma fonte de vínculo daquele mecanismo.
   - **O ACS não ganha prontuário:** o papel dele não tem `PRONTUARIO.CONSULTAR`.
6. **`GET /territorio/meu-escopo`** (para quem está logado) diz se o escopo está ligado, se restringe o usuário e o
   que ele alcança.
   - **Território:** abre na área do usuário, com uma faixa que diz o escopo, as microáreas do ACS no filtro e no
     cadastro, e sem "Novo imóvel" onde ele não alcança.
   - **Programas:** os elegíveis já vêm na equipe do usuário.
7. **Fim do texto livre das microáreas** (migração V22):
   - **Antes de remover,** o texto que ainda havia em equipes **sem** área (gravado depois da V19) vai para o
     Território do mesmo jeito da V19: área da equipe, microáreas e atribuição do ACS.
   - **Depois, as colunas saem:** `tb_equipe.microareas` e `tb_membro_equipe.microarea`.
   - **A API de Equipes deixa de receber** `microareas` e `microarea`. As microáreas da equipe e a do ACS vêm só do
     Território.
   - **A tela de Equipes** perde os dois campos e aponta para o Território.

## Consequências

- **O domínio #21 está completo:** recorte, mapa, cadastro, adscrição, cobertura e escopo.
- **Com o escopo ligado,** o ACS trabalha a sua microárea e a equipe a sua área, e o prontuário segue a população
  adscrita.
- **O escopo é opt-in:** a rede liga quando as equipes, as áreas e as atribuições estiverem cadastradas. Ligado cedo,
  quem não tem equipe no território perde o cadastro.
- **Fora do escopo:**
  - restringir a busca de pacientes por território;
  - o escopo territorial em outros domínios (vigilância, visitas), que fica para as fatias deles.

## Testes

- **JUnit:**
  - `EscopoTerritorialControllerTest`, com login, autorização, escopo e vínculo ligados:
    - o ACS lista só a sua microárea, recebe 403 no imóvel e no cadastro da outra e cadastra na sua;
    - o enfermeiro da equipe vê e cadastra na área inteira;
    - quem cadastra sem equipe recebe 403;
    - o gestor não tem restrição;
    - o `meu-escopo`;
    - o médico da equipe lê o prontuário do adscrito, e o de fora não.
  - **Com o escopo desligado,** as suítes de cadastro, adscrição e autorização seguem como antes.
- **Robot:**
  - **de API:** `Territorio.robot` CT-012 (`meu-escopo` com o escopo desligado). As suítes de Equipes deixaram de
    mandar os campos de texto.
  - **de interface:** o detalhe de Equipes mostra as microáreas vindas da área no Território.
  - **Limitação:** as suítes com login ligado são puladas no ambiente local e no CI da `developer`, então as regras
    do escopo ficam cobertas pelo JUnit.
