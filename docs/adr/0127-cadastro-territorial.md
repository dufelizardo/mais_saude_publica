# 0127 — Território: imóvel, domicílio e família (F3a)

## Status

Aceita e implementada. É a primeira parte da fatia F3 da [ADR-0108](./0108-territorio-e-adscricao.md), sobre as ADRs
[0125](./0125-territorio-fundacao.md) e [0126](./0126-territorio-mapa.md); detalhe em
[`territorio/MODELO-TERRITORIO.md`](../territorio/MODELO-TERRITORIO.md). A adscrição e a cobertura (F3b) vêm na
ADR-0128.

## Contexto

- **As fatias F1 e F2 deram o recorte e o mapa:** área, microárea, equipe, ACS e desenho.
- **Mas o território ainda não tinha ninguém dentro.** A ADR-0108 prevê o caminho
  microárea → imóvel → domicílio → família → cidadão, e o cidadão é o `Paciente` que já existe (ADR-0040).
- **O e-SUS APS organiza o cadastro territorial** em imóvel, domicílio (com as condições de moradia), família e
  indivíduo.
- **Não existia papel de acesso para o ACS,** embora a função ACS exista nas equipes (ADR-0103).
- **A F3 foi dividida em duas entregas:** esta (cadastro) e a F3b (adscrição e cobertura).

## Decisão

1. **Entidades** (migração V20):
   - **`Imovel`:**
     - microárea, tipo (`DOMICILIO · COMERCIO · ESCOLA · INSTITUICAO · TERRENO_BALDIO · PONTO_ESTRATEGICO · OUTRO`) e
       endereço (o mesmo `Endereco` do paciente);
     - ponto (latitude e longitude, opcional) e ativo.
     - **É o lote ou o prédio.** Um prédio tem vários domicílios.
   - **`Domicilio`:** é a moradia dentro do imóvel.
     - **Identificação opcional** ("apto 12", "casa dos fundos"); repetida no imóvel, 409.
     - **Condições da ficha domiciliar:** situação (próprio, financiado, alugado, arrendado, cedido, ocupação, outra),
       localização (urbana ou rural), cômodos, abastecimento de água, escoamento sanitário, destino do lixo e energia
       elétrica.
     - **Só existe em imóvel de moradia** (422). Um imóvel com domicílio não deixa de ser de moradia (422).
     - **Inativar com família morando** dá 422.
   - **`NucleoFamiliar`:** a família morando num domicílio, **com início e fim**.
     - **Responsável:** um paciente que é membro vigente.
     - **Renda familiar:** em **faixas de salário mínimo**, não em valor.
     - **Mudança:** encerra o núcleo no domicílio de origem e abre outro no destino com as mesmas pessoas, parentescos
       e renda. Fica o histórico de onde a família morou. A mudança é da família inteira; para uma pessoa só, saída e
       entrada em outra família.
   - **`MembroNucleo`:**
     - **dados do vínculo:** paciente, parentesco com o responsável, início, fim e motivo;
     - **unicidade:** **uma família vigente por paciente** (409, com índice único parcial no banco);
     - **o responsável sai por último:** antes, troca-se o responsável, informando o parentesco do anterior com o
       novo (422);
     - **saindo o último, a família se encerra.**
2. **Território:**
   - **O ponto do imóvel cai dentro da microárea** quando ela tem desenho (JTS, com a folga de borda da ADR-0125;
     422).
   - **Sem microárea informada, o ponto a encontra** entre as microáreas desenhadas de território ativo de atenção
     primária. Se não achar, ou sem ponto nem microárea, 400.
   - **O imóvel não muda de área pela edição** (422), só de microárea dentro da área.
   - **O cadastro é da equipe:** área sem equipe responsável não recebe cadastro domiciliar (422).
3. **Endereço do paciente:**
   - **O cadastro do paciente não é sobrescrito.** A família marca a pessoa cujo endereço diverge do imóvel (CEP,
     logradouro ou número).
   - **"Usar o endereço do imóvel" é uma ação explícita,** que pede também `PACIENTE.CADASTRAR`. O complemento leva a
     identificação do domicílio.
4. **Geocodificação: não entra.** O ponto é marcado no mapa da F2.
   - **Motivo:** mandar endereço de moradia a um serviço externo (Nominatim) é tratamento de dado pessoal por
     terceiro (LGPD), e a política do Nominatim proíbe uso em massa.
   - **Um geocodificador contratado entra pelas Integrações (#20).**
5. **Acesso:**
   - **Permissão nova `TERRITORIO.CADASTRAR`**, na dimensão de acesso a dado de saúde e pessoal: composição familiar e
     condições de moradia identificam pessoas.
   - **Escopo:** a unidade da equipe responsável pela área. A restrição do ACS à própria microárea é a F4.
   - **Papel novo "Agente comunitário de saúde":** `PACIENTE.CONSULTAR`, `PACIENTE.CADASTRAR` e `TERRITORIO.CADASTRAR`.
     **Enfermeiro e Coordenador de enfermagem** recebem `TERRITORIO.CADASTRAR`.
   - **Leitura:**
     - a lista de imóveis, o imóvel e a família do paciente pedem `TERRITORIO.CADASTRAR` ou `TERRITORIO.GERENCIAR` no
       escopo;
     - **o imóvel e a família do paciente entram na auditoria** (`@AuditarLeitura`);
     - **a árvore, aberta a quem está logado,** traz só contagens por microárea (imóveis, famílias, pessoas).
6. **API** (`/api/v1/territorio/`):
   - **Leitura:** `area/{id}/imoveis` (filtro `microareaId`), `imovel/{id}` e `paciente/{id}/familia`;
   - **Imóvel e domicílio:** `imovel` (POST/PATCH) e `domicilio` (POST/PATCH);
   - **Família:**
     - `familia` (POST);
     - `familia/{id}/renda`, `/membro`, `/responsavel` e `/mudanca`;
     - `familia/membro/{id}/saida`;
     - `familia/membro/{id}/endereco-do-imovel`.
7. **Tela:**
   - **Bloco "Imóveis e famílias" no detalhe da área** (componente `ImoveisDaArea`): lista por microárea e logradouro,
     com famílias e pessoas.
   - **Ficha do imóvel:** os domicílios, com as condições, as famílias e as pessoas (parentesco, idade, aviso de
     endereço diferente) e quem já morou ali.
   - **Gavetas:**
     - imóvel, com minimapa para marcar o ponto;
     - domicílio;
     - família;
     - incluir pessoa (busca do paciente existente, com o link para cadastrar);
     - saída, troca de responsável, renda e mudança (área → imóvel → domicílio de destino).
   - **Os imóveis com ponto aparecem no mapa da área** como pinos menores, e o clique no pino abre a ficha.

## Consequências

- **O território passa a ter imóveis, famílias e pessoas,** com histórico de moradia.
- **Destrava a F3b** (adscrição e cobertura) e, por ela, a lista de elegíveis da P2, a V4 (zoonoses), a E4
  (vulneráveis) e a F4.
- **Fora do escopo:**
  - adscrição e cobertura (F3b);
  - escopo por microárea (F4);
  - visita domiciliar e fichas do ACS;
  - geocodificação;
  - integração com o e-SUS.

## Testes

- **JUnit:**
  - `CadastroTerritorialControllerTest`:
    - o cadastro sem equipe;
    - o ponto fora da microárea e o ponto que encontra a microárea;
    - domicílio só em imóvel de moradia, identificação repetida e imóvel com domicílio que vira comércio;
    - família com responsável e uma família por pessoa;
    - troca de responsável e saída do responsável por último;
    - mudança com histórico;
    - endereço do imóvel no cadastro;
    - contagens da árvore;
    - saída do último membro encerrando a família.
  - `CadastroTerritorialAutorizacaoControllerTest`: o ACS cadastra na unidade da equipe; o enfermeiro de outra
    unidade e o gestor não; o gestor lê; a leitura do imóvel é auditada.
- **Robot:**
  - **de API:** `test/territorio/CadastroTerritorial.robot`;
  - **de interface:** `UI_territorio.robot`, CT-009 a CT-011.
