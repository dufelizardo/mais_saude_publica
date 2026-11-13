# 0199 — Vigilância Sanitária: estabelecimento, licença, denúncia e inspeção (V3a)

## Status

Aceita e implementada. Primeira parte da fatia **V3** do domínio 23 ([ADR-0110](./0110-vigilancia-em-saude.md), seção 4; detalhe em
[`vigilancia/MODELO-VIGILANCIA.md`](../vigilancia/MODELO-VIGILANCIA.md)). A V3 foi dividida em três PRs:

- **V3a (esta):** estabelecimento regulado, licença, denúncia, roteiro e inspeção com as irregularidades;
- **V3b:** auto de infração, processo administrativo sanitário, medidas cautelares, termos assinados com verificação pública e o
  relatório no #19;
- **V3c:** a notificação de violência levada à rede intersetorial (ECA e Estatuto da Pessoa Idosa).

## Contexto

- **A VISA era a única parte da Vigilância sem código.** A ADR-0110 já separava o estabelecimento regulado da `UnidadeDeSaude` e
  previa o rito da Lei 6.437/1977.
- **O STATUS dizia que a V3 dependia da D2 de Documentos:** autos, fotos e plantas precisam de dono que não seja o paciente.
- **O licenciamento depende do risco da atividade:** a RDC 153/2017 classifica as atividades (CNAE) em risco I, II e III, e a Lei
  13.874/2019 (liberdade econômica) dispensa o licenciamento prévio do risco baixo.
- **A inspeção é ato de autoridade sanitária:** precisa dizer quem fiscalizou e ser assinada, como o laudo da
  [ADR-0183](./0183-laudo-pdf-assinado-e-insumos.md).

## Decisão

1. **Estabelecimento regulado** (`TB_ESTABELECIMENTO_REGULADO`):
   - CPF ou CNPJ só com os dígitos, com o dígito verificador conferido (400) e único (409);
   - razão social, nome fantasia, endereço, microárea e coordenadas opcionais, telefone, responsável legal e responsável técnico com
     o registro no conselho;
   - a unidade pública inspecionada liga-se opcionalmente (`unidade_id`);
   - **atividades** (`TB_ATIVIDADE_ESTABELECIMENTO`): CNAE de sete dígitos, descrição e grau de risco (BAIXO, MEDIO, ALTO). O risco
     do estabelecimento é o maior entre elas; CNAE repetido, 400. Na alteração, o CNAE que fica mantém o registro;
   - inativar tira o estabelecimento das inspeções e das licenças novas (422).
2. **Licença sanitária** (`TB_LICENCA_SANITARIA`), guardada como histórico (memória: dado temporal vira histórico):
   - número do ano (`2026/000001`), emissão (não futura), validade e observação; a situação gravada é VIGENTE, SUSPENSA ou CASSADA;
     **vencida** e **dispensada** são calculadas;
   - **risco I:** registra a dispensa, sem validade (400 se informada) e sem repetir com o risco ainda baixo (409);
   - **risco II e III:** validade de um ano por padrão, sempre depois da emissão (400); a renovação vale a partir de 60 dias do
     vencimento (409);
   - **risco III** só com inspeção **satisfatória** nos 12 meses anteriores (422), que fica ligada à licença;
   - irregularidade **gravíssima aberta** impede a licença (422); a suspensa só se libera pelo processo (422, V3b).
3. **Denúncia** (`TB_DENUNCIA_SANITARIA`):
   - protocolo do ano, canal (telefone, presencial, ouvidoria, internet, outro), descrição, e o estabelecimento cadastrado ou só
     descrito (400 sem nenhum);
   - **anônima** sem os dados do denunciante (400); identificada, com o nome (400). O nome e o contato só aparecem para quem tem
     `VISA.GERENCIAR`;
   - RECEBIDA → EM_APURACAO (na primeira inspeção ligada, que também preenche o estabelecimento) → PROCEDENTE (só com inspeção, 422)
     ou IMPROCEDENTE; concluída não reabre (422).
4. **Roteiro de inspeção** (`TB_ROTEIRO_INSPECAO` e `TB_VERSAO_ROTEIRO_INSPECAO`):
   - os itens (código, texto e gravidade sugerida) ficam na versão, como dado, no padrão da ficha do agravo da V1;
   - nova versão não muda as inspeções antigas, que guardam a versão que responderam;
   - carga: serviços de alimentação (RDC 216/2004) e farmácias e drogarias (RDC 44/2009).
5. **Inspeção** (`TB_INSPECAO_SANITARIA`, fiscais em `TB_INSPECAO_SANITARIA_FISCAL`):
   - motivo: licenciamento, programada, denúncia, retorno, surto ou outro. **O motivo pede o vínculo e o vínculo pede o motivo**
     (400): a denúncia aberta (422) do mesmo estabelecimento (422); o surto da V2b; a inspeção de origem do mesmo estabelecimento
     (422) no retorno;
   - o roteiro é respondido **por inteiro** (400), e cada item não conforme pede a irregularidade (400);
   - resultado SATISFATORIA, COM_RESSALVAS ou INSATISFATORIA; satisfatória não tem irregularidade (400); relatório obrigatório;
   - **fiscais pela matrícula** (404); com login, quem registra está entre eles (403) e **assina**: o CPF e o SHA-256 do texto
     canônico (inspeção, estabelecimento, data, motivo, roteiro e respostas, resultado, relatório, fiscais e irregularidades), como na
     ADR-0183. Sem login (segurança desligada nos testes), o hash é gravado sem assinante;
   - o **termo de inspeção em PDF** (OpenPDF) traz o estabelecimento, o roteiro respondido, as irregularidades com o prazo (a
     intimação), o relatório, a assinatura e o campo da ciência do responsável.
6. **Irregularidade** (`TB_IRREGULARIDADE_SANITARIA`):
   - descrição, enquadramento legal em texto, gravidade (leve, grave, gravíssima, como a infração do art. 4º da Lei 6.437/1977), o
     item do roteiro quando houver e o **prazo para corrigir**, nunca antes da inspeção (400);
   - ABERTA → CORRIGIDA só na **inspeção de retorno**, que informa as corrigidas (só abertas e do estabelecimento, 422; só no retorno,
     400); AUTUADA fica para a V3b;
   - o prazo vencido não é guardado: é a aberta com o prazo antes de hoje.
7. **Pendências** (`GET vigilancia-sanitaria/pendencias`): o estabelecimento ativo de risco II ou III sem licença (ou só com a
   dispensa), com a licença vencida, vencendo em 30 dias, suspensa ou cassada; o prazo de correção vencido; a denúncia sem conclusão há
   mais de 30 dias.
8. **Documentos (D2 parcial, [ADR-0172](./0172-documentos-anexos.md)):**
   - `DonoDocumento` ganha ESTABELECIMENTO e INSPECAO_SANITARIA; fora do paciente, a unidade do documento é opcional (o CHECK
     `tb_documento_unidade_paciente_check` mantém a obrigação para o paciente);
   - mesmas regras do arquivo (PDF, JPG ou PNG, até 10 MB, tipo pelo conteúdo, hash, retirada com motivo);
   - a leitura segue as permissões da VISA, não o prontuário; os caminhos do paciente e da VISA não abrem o documento um do outro
     (404).
9. **Acesso:**
   - permissões novas: `VISA.INSPECIONAR` (cadastrar estabelecimentos, registrar inspeções) e `VISA.GERENCIAR` (licenças, denúncias,
     roteiros);
   - papéis novos: **Fiscal sanitário** (`VISA.INSPECIONAR`) e **Coordenação da vigilância sanitária** (as duas, mais
     `EMERGENCIA.REGISTRAR`); o **Gestor** ganha `VISA.GERENCIAR`;
   - o cadastro do estabelecimento aceita as duas permissões: o fiscal encontra o estabelecimento em campo. A licença é só da
     coordenação.
10. **Tela** `/vigilancia/sanitaria`, no grupo Vigilância do menu: os cartões das pendências e as abas Estabelecimentos (busca, risco,
    licença atual, irregularidades abertas; o detalhe com o histórico e os documentos), Inspeções (com o termo), Denúncias, Licenças
    e Pendências. Na inspeção, o item não conforme do roteiro abre a irregularidade com o texto e a gravidade do item.

### API

| Método | Caminho (`/api/v1/vigilancia-sanitaria/`) | Permissão |
|---|---|---|
| GET | `estabelecimentos` (`busca`, `risco`), `estabelecimento/{id}` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST, PUT | `estabelecimento`, `estabelecimento/{id}` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| GET | `licencas` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST | `estabelecimento/{id}/licenca` | `VISA.GERENCIAR` |
| GET | `roteiros` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST | `roteiro`, `roteiro/{id}/versao` | `VISA.GERENCIAR` |
| GET | `denuncias` (`situacao`) | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST | `denuncia`, `denuncia/{id}/conclusao` | `VISA.GERENCIAR` |
| GET | `inspecoes` (`estabelecimentoId`), `inspecao/{id}`, `inspecao/{id}/termo.pdf`, `pendencias` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST | `inspecao` | `VISA.INSPECIONAR` |
| GET, POST | `estabelecimento/{id}/documentos`, `estabelecimento/{id}/documento`, `inspecao/{id}/documentos`, `inspecao/{id}/documento` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |
| POST, GET | `documento/{id}/retirada`, `documento/versao/{id}/arquivo` | `VISA.INSPECIONAR` ou `VISA.GERENCIAR` |

### Migração

`V82__vigilancia_sanitaria.sql`: as nove tabelas, a carga dos dois roteiros, o CHECK do dono do documento com os dois valores novos e a
unidade do documento opcional fora do paciente.

## Alternativas descartadas

- **Estabelecimento como `UnidadeDeSaude`:** misturaria a rede com o setor regulado; a ADR-0110 já tinha descartado.
- **Licença como campo do estabelecimento (número e validade):** perderia a renovação, a suspensão e a cassação; vira histórico.
- **Roteiro como tabela de itens:** a versão como dado (JSON), igual à ficha do agravo, deixa a inspeção antiga intacta sem copiar
  linhas.
- **Corrigir a irregularidade por um botão:** a correção é constatada pela autoridade sanitária, na inspeção de retorno.
- **Esperar a D2 inteira:** só os donos da VISA entram agora; os materiais da C2 seguem o mesmo caminho quando vierem.

## Consequências

- **STATUS-DOS-DOMINIOS:** a V3a fica marcada no domínio 23 e a D2 passa a parcial (donos da VISA); o domínio segue 🟡 até a V3b,
  a V3c e a V5.
- **A inspeção sanitária** sai da lista do que falta no domínio 1: a unidade pública pode ser inspecionada como estabelecimento ligado.
- **Fora do escopo:** auto de infração, processo, penalidades, medidas cautelares, verificação pública e o relatório no #19 (V3b);
  cadastro de produtos (ANVISA); tabela de multas (código sanitário municipal); integrações com SINAVISA, REDESIM e Junta Comercial
  (domínio 20).

## Testes

- **JUnit `VigilanciaSanitariaControllerTest`:**
  - CNPJ inválido e CNAE repetido (400), documento repetido (409), o risco pelo maior da atividade;
  - risco III sem inspeção (422), a inspeção satisfatória com o roteiro e o hash, a licença ligada a ela, a renovação cedo (409);
  - a dispensa do risco I (400 com validade, 409 repetida); as pendências sem licença e vencida; a alteração das atividades e o
    inativo sem licença (422);
  - a denúncia anônima e identificada (400), procedente sem inspeção (422); as regras da inspeção (vínculos, roteiro incompleto, item
    sem irregularidade, satisfatória com irregularidade, prazo antes da data, fiscal inexistente); a gravíssima que impede a licença;
    o retorno com as corrigidas; o termo em PDF;
  - os documentos do estabelecimento: anexar, abrir, o caminho do paciente que não abre (404), arquivo inválido (400), retirada;
  - o roteiro com o código único (409), item repetido (400) e a segunda versão.
- **JUnit `VigilanciaSanitariaAutorizacaoControllerTest`,** com a segurança ligada: sem login (401); a recepção (403); o fiscal
  cadastra e inspeciona assinando com a própria matrícula (403 sem ela), não licencia nem registra denúncia; a coordenação licencia, vê
  o denunciante (o fiscal não); o gestor lê sem inspecionar.
- **Robot API** `VigilanciaSanitaria.robot` (CT-001 a CT-005) e **Robot UI** `UI_vigilancia_sanitaria.robot` (menu, cadastro do
  estabelecimento e inspeção com roteiro).
