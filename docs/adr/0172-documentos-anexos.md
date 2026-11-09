# 0172 — Documentos: anexos do paciente (D1)

## Status

Aceita e implementada. Primeira fatia do domínio transversal Documentos, previsto no mapa desde a ADR-0039 e mantido no conjunto fechado pela
[ADR-0116](./0116-marco-mapa-de-dominios-fechado.md).

## Contexto

- **Nenhuma parte do sistema recebia arquivo.** A aba Anexos do paciente estava como "Em breve".
- **Vários domínios esperam por isto:** Vigilância Sanitária (V3), materiais de comunicação (C2) e guias para imprimir.
- **Arquivo de paciente é dado de saúde:** quem abre segue as regras do prontuário.

## Decisão

1. **Documento reaproveitável** (`TB_DOCUMENTO`): dono (`dono_tipo` e `dono_id`, hoje só Paciente), tipo, título, data e unidade. Outro domínio entra sem tabela nova.
2. **Tipos em catálogo editável** (`TB_TIPO_DOCUMENTO`, `ADMINISTRATIVO.GERENCIAR`). Carga: exame externo, laudo, receita, termo de consentimento, documento pessoal e outro.
3. **Versões** (`TB_VERSAO_DOCUMENTO`, só inclusão): nome, tipo, tamanho, autor, data e SHA-256. Hash diferente ao abrir responde 409: o arquivo foi alterado fora do sistema.
4. **Nada se apaga:** o documento é retirado com o motivo. Retirado sai da lista normal, não abre (422) e não recebe versão (422).
5. **Armazenamento fora do banco:** o volume `documentos-data`, de 5 GB por ambiente, atrás de `ArmazenamentoDeArquivos`, para poder trocar por armazenamento externo. A chave é
   gerada pelo sistema; o nome enviado nunca vira caminho.
6. **Regras de envio:** PDF, JPG e PNG, até 10 MB (400). O tipo é conferido pelo conteúdo, não pela extensão nem pelo que o navegador declara.
7. **Abrir:** sempre como anexo, com `nosniff`, nunca como página.
8. **Permissões:**
   - `DOCUMENTO.ANEXAR` (nova), na unidade: Médico, Enfermeiro, Recepção, Assistente social, Psicólogo e Gestor.
   - **Listar e abrir tudo** segue `PRONTUARIO.CONSULTAR`, com o vínculo assistencial e a leitura na trilha.
   - **Quem só anexa,** como a recepção, vê apenas o que anexou e não abre o arquivo: título de documento também pode revelar dado de saúde.

### API

- `GET, POST /api/v1/tipo-documento`, `PATCH /api/v1/tipo-documento/{uuid}`;
- `GET /api/v1/paciente/{id}/documentos`, `POST /api/v1/paciente/{id}/documento` (multipart);
- `POST /api/v1/documento/{id}/versao` (multipart), `POST /api/v1/documento/{id}/retirada`;
- `GET /api/v1/documento/versao/{id}/arquivo`.

### Telas

- **Pacientes:** aba Anexos, com lista, envio, versões, download e retirada.
- **Modelo administrativo:** aba Tipos de documento.

### Infraestrutura

`documentos-pvc.yaml`, o volume montado em `/dados/documentos`, `APP_DOCUMENTOS_DIRETORIO` e `client_max_body_size 12m` no nginx do front.

### Migração

`V57__documentos.sql`: as tabelas e a carga dos tipos.

## Consequências

- **Backup passa a ser necessário antes de dado real:** perder o disco é perder os anexos. O banco sozinho não recupera os arquivos.
- **Espaço:** cada ambiente reserva 5 GB no servidor.
- **Sem antivírus:** o arquivo não é executado nem aberto como página, mas a verificação fica para depois.
- **Ficam para depois:** D2, outros donos (materiais da C2, autos da Vigilância); D3, guias para imprimir; D4, aprovação e assinatura digital; visualização embutida e OCR.

## Testes

- **JUnit:** `DocumentoControllerTest`: anexar e abrir, conteúdo contra extensão, caminho no nome, validações, tipo inativo, catálogo, versões, retirada, arquivo alterado e leitura
  na trilha; `DocumentoAutorizacaoControllerTest`, com a segurança ligada: quem não tem a permissão, outra unidade, a recepção sem abrir e o médico vendo tudo.
- **Robot:** `Documentos.robot` (API, cinco casos) e o caso de anexos em `UI_pacientes_prontuario.robot`.
