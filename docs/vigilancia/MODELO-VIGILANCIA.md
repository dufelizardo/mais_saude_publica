# Modelo do domínio Vigilância em Saúde

**Data:** 2026-10-07 (criado)
**Status:** desenho aprovado na [ADR-0110](../adr/0110-vigilancia-em-saude.md). **Nada implementado ainda.** A
implementação vem nas fatias V1 a V5 (seção 14), cada uma com ADR própria.

A vigilância **não é mais um módulo assistencial**: é uma camada de inteligência e intervenção sobre riscos,
doenças, agravos e eventos de saúde pública, ligada a território, população e rede. Este documento detalha os
subdomínios, a base compartilhada, as entidades, o território e as integrações. A ADR-0110 registra as decisões.

## 1. Objetivo e ciclo

O domínio deve permitir:
- identificar riscos e agravos, registrar notificações, investigar casos e acompanhar doenças e eventos;
- identificar surtos e fazer ações de controle;
- fiscalizar estabelecimentos e atividades sujeitas à vigilância;
- acompanhar riscos ambientais, cuidar da saúde do trabalhador e controlar zoonoses e vetores;
- relacionar as ocorrências ao território e produzir indicadores para a gestão municipal.

```text
Evento de saúde → Detecção → Notificação → Investigação → Classificação → Ação de vigilância → Encerramento → Indicadores
```

## 2. Subdomínios sobre uma base compartilhada

Não existe uma entidade única `Vigilancia`. São subdomínios com uma base comum:

```text
Vigilância em Saúde
├── Epidemiológica       notificação, investigação, casos, contatos, surtos
├── Sanitária            estabelecimentos, inspeções, irregularidades, licenciamento
├── Ambiental            riscos e monitoramento (água, ar, solo, contaminantes, desastres)
│   └── Zoonoses e Vetores   vetores, animais, focos, controle (autonomia operacional)
├── Saúde do Trabalhador eventos, riscos ocupacionais, investigação
└── compartilhado        EventoVigilancia, local e TerritorioReferencia, AcaoVigilancia
```

**Imunização** não está na lista: é um domínio próprio, o #24 (seção 10).

## 3. Vigilância Epidemiológica

É a primeira a implementar. Trabalha com doenças, agravos e eventos de interesse à saúde pública:

```text
Pessoa (Paciente) → Notificação → Caso epidemiológico → Investigação → Classificação e encerramento
```

### 3.1 Agravo e notificação genérica

**Não há tabela por doença** (`NotificacaoDengue`, `NotificacaoTuberculose`...). O **agravo** é um catálogo e define
o que a notificação precisa ter.

| `Agravo` | Para quê |
|---|---|
| nome e CID-10 | identificar o agravo e ligar com o CID registrado na rede |
| notificação compulsória | se está na Lista Nacional (Portaria de Consolidação GM/MS nº 4/2017, anexo V, com as atualizações) |
| periodicidade | **imediata** (até 24 horas) ou **semanal** |
| prazo de encerramento | quantos dias a investigação tem para fechar |
| critérios de classificação | laboratorial, clínico-epidemiológico, clínico |
| sensível | visibilidade restrita (seção 11) |
| ficha específica | os campos extras do agravo, **como dado e versionados** |

A ficha específica é uma definição de campos guardada com o agravo. Exemplo para dengue:

```json
{
  "versao": 1,
  "campos": [
    { "chave": "febre", "rotulo": "Febre", "tipo": "sim_nao", "obrigatorio": true },
    { "chave": "sinaisAlarme", "rotulo": "Sinais de alarme", "tipo": "multipla",
      "opcoes": ["dor abdominal", "vômitos persistentes", "sangramento de mucosa", "letargia"] },
    { "chave": "dataInicioSintomas", "rotulo": "Início dos sintomas", "tipo": "data", "obrigatorio": true }
  ]
}
```

A notificação guarda as respostas junto com a versão da ficha. Mudar a ficha não altera as notificações antigas.
A carga inicial vem da lista nacional, e o município acrescenta agravos de interesse local.

| `Notificacao` | |
|---|---|
| cidadão | o `Paciente` |
| agravo | do catálogo |
| unidade e profissional notificantes | da rede |
| datas | dos primeiros sintomas e da notificação |
| situação | notificada, em investigação, encerrada |
| origem | atendimento, laboratório, busca ativa, comunidade |
| local provável | endereço e ponto opcional (seção 9) |
| respostas da ficha | com a versão |

Prevê também a **notificação negativa** semanal da unidade: a declaração de que não houve casos na semana.

### 3.2 Investigação e classificação

**Notificação não é caso confirmado.** O processo é:

```text
Notificação → Investigação → Classificação: SUSPEITO · PROVÁVEL · CONFIRMADO · DESCARTADO · INCONCLUSIVO
```

A classificação vem com o critério (laboratorial ou clínico-epidemiológico). A investigação reúne:
- entrevista e histórico;
- exposição, deslocamentos e local provável de infecção;
- contatos;
- exames, ligados ao Laboratório (ADR-0093);
- vínculo territorial e medidas adotadas.

### 3.3 Contato

Pessoa exposta a um caso. **Pode ainda não ter cadastro** (nome, telefone, endereço) e vira `Paciente` quando é
atendida.

### 3.4 Surto

**Surto não é simplesmente "vários casos".** É um evento epidemiológico que relaciona casos:

```text
Surto
 ├── agravo, território, período e situação
 ├── casos relacionados
 ├── investigação
 └── medidas de controle

Surto de dengue → microáreas 04, 05 e 06 → 37 casos → investigação territorial → controle do vetor
```

É onde Território, Vigilância, Programas e Atendimento se encontram.

### 3.5 Detecção a partir da rede

A rede detecta, mas **não notifica sozinha**. Um CID de agravo notificável registrado no atendimento, na internação
(que já guarda CID) ou no resultado de exame gera uma **sugestão de notificação** para o profissional. A notificação
é ato profissional, o mesmo princípio do público-alvo em Programas (ADR-0109).

## 4. Vigilância Sanitária

É outro mundo, que não se mistura com a epidemiologia. Cuida de estabelecimentos, produtos, serviços, ambientes e
condições sanitárias, e exerce **poder de polícia administrativa**:

```text
Estabelecimento regulado → Inspeção → Achados → Irregularidade → Medida (auto, interdição, apreensão) → Regularização
```

- **Entidades:** `EstabelecimentoRegulado`, `InspecaoSanitaria`, `IrregularidadeSanitaria`, `MedidaSanitaria` e
  `LicencaSanitaria`.
- **O estabelecimento regulado não é a `UnidadeDeSaude`.** Uma farmácia privada, um restaurante e uma clínica
  particular são regulados sem ser unidades da rede. A unidade pública também pode ser inspecionada e, nesse caso,
  liga-se opcionalmente.
- **As medidas seguem um rito:** auto de infração, processo administrativo sanitário e prazos de defesa (Lei
  6.437/1977). Os autos são documentos formais, e por isso a sanitária depende do domínio Documentos.

## 5. Vigilância Ambiental

Riscos ligados ao ambiente: água, ar, solo, contaminantes, desastres, fatores ambientais, vetores e animais. Liga-se
fortemente com Território, Zoonoses e Epidemiologia. Entidades previstas: `EventoAmbiental`, `RiscoAmbiental` e
`MonitoramentoAmbiental` (por exemplo, a qualidade da água, que hoje é do SISAGUA).

## 6. Zoonoses e Controle de Vetores

Fica dentro da Ambiental, mas com autonomia operacional. Cuida de dengue, chikungunya, zika, raiva e leishmaniose,
de animais, focos, imóveis, criadouros e do controle vetorial:

```text
Território → Imóvel → Vistoria → Foco → Ação de controle
```

Usa o **imóvel** do domínio Território (ADR-0108, fatia F3), ligado à microárea, mas **não fica preso a ela**: o
agente de endemias percorre quarteirões e áreas que não coincidem com a microárea do ACS.

## 7. Saúde do Trabalhador

Acidente de trabalho, doença relacionada ao trabalho, exposição e risco ocupacional, investigação, notificação e
acompanhamento:

```text
Trabalhador → Evento relacionado ao trabalho → Notificação → Investigação → Medidas
```

- **Não depende do RH:** o trabalhador pode ser de fora da rede, de qualquer empresa do município.
- **Servidor da rede:** quando o trabalhador é servidor, o evento se liga opcionalmente ao `AcidenteTrabalho` do RH,
  que já existe.
- **Sem duplicar:** o RH cuida da gestão do servidor (afastamento, CAT); a vigilância cuida da notificação e da
  investigação.
- **CEREST:** a referência técnica em saúde do trabalhador já está no Mapa de Equipamentos.

## 8. Território

A pergunta central da vigilância é: **onde estão acontecendo os eventos?**

```text
Evento → Local → Território → Área → Microárea → Equipe responsável
```

**Nem todo evento precisa ter microárea.** Todo evento tem **local** (endereço e ponto opcional em GeoJSON, como na
ADR-0108); o vínculo com território e microárea é opcional:

| Evento | Território |
|---|---|
| Notificação epidemiológica | pode ter (local provável e residência) |
| Inspeção sanitária | tem o endereço do estabelecimento |
| Surto | tem território (o recorte afetado) |
| Acidente de trabalho | tem o local do evento |
| Microárea | só quando fizer sentido |

O território da vigilância usa a **finalidade vigilância** já prevista no domínio Território, porque os recortes da
vigilância não são os da APS.

## 9. Integração

```text
                 TERRITÓRIO
                     │
        ┌────────────┼────────────┐
        ↓            ↓            ↓
   POPULAÇÃO      EVENTOS      IMÓVEIS
        │            │            │
        └────────────┼────────────┘
                     ↓
                VIGILÂNCIA
                     │
          ┌──────────┼──────────┐
          ↓          ↓          ↓
      PROGRAMAS   ASSISTÊNCIA  REDE
```

| Domínio | Como se liga |
|---|---|
| Paciente | cidadão da notificação e do contato |
| Atendimento, Internação, Laboratório | origem da sugestão de notificação; exames da investigação |
| Território (#21) | local, imóvel, recorte do surto, finalidade vigilância |
| Programas (#22) | caso confirmado pode gerar acompanhamento (por exemplo, tuberculose); campanha de controle |
| RH | `AcidenteTrabalho` do servidor; profissionais notificantes e investigadores |
| Organização | unidade notificadora; equipamentos de vigilância (LACEN, CEREST, Unidade de Vigilância de Zoonoses, Serviço de Verificação de Óbito) |
| Documentos | autos e processos da sanitária |
| Indicadores (#19) | incidência, surtos, encerramento oportuno, inspeções |
| Integrações (#20) | adaptadores SINAN, SIVEP, SISAGUA, ANVISA |

## 10. Imunização é outro domínio (#24)

A imunização **não é só vigilância**. Ela atravessa três áreas:

| Área | O que faz |
|---|---|
| Vigilância | calendário, campanhas, cobertura, monitoramento, eventos adversos |
| Assistência | aplicação, registro, atendimento |
| Estoque | vacinas, lotes, cadeia de frio |

Por isso vira o domínio **#24 Imunização**, a desenhar em ADR própria, integrado à vigilância. A **campanha de
vacinação** continua sendo uma `Campanha` de Programas (ADR-0109); a Imunização registra as doses aplicadas. A aba
Vacinação de Pacientes depende dele.

## 11. Dado sensível e permissões

- **Notificação identificada é dado de saúde** (LGPD, art. 11). A leitura é auditada (ADR-0070).
- **Agravos sensíveis** têm visibilidade restrita e sigilo: HIV/aids, sífilis, violência interpessoal e
  autoprovocada. Só a equipe de vigilância responsável vê o detalhe.
- **Boletins e indicadores mostram só agregados.**

| Permissão | Quem |
|---|---|
| `VIGILANCIA.NOTIFICAR` | qualquer profissional assistencial |
| `VIGILANCIA.INVESTIGAR` | equipe de vigilância epidemiológica |
| `VIGILANCIA.GERENCIAR` | catálogo de agravos, surtos |
| `VISA.FISCALIZAR` | autoridade sanitária |
| as dos demais subdomínios | definidas nas fatias V4 e V5 |

## 12. Entidades previstas

| Entidade | Atributos previstos | Fatia |
|---|---|---|
| `Agravo` | nome, CID-10, compulsório, periodicidade, prazo de encerramento, critérios, sensível, ficha (versionada) | V1 |
| `Notificacao` | paciente, agravo, unidade, profissional, datas, situação, origem, local, respostas, versão da ficha | V1 |
| `NotificacaoNegativa` | unidade, semana epidemiológica, responsável | V1 |
| `InvestigacaoEpidemiologica` | notificação, investigador, exposição, local provável, exames, medidas | V1 |
| `ClassificacaoCaso` | investigação, classificação, critério, data, responsável (histórico) | V1 |
| `ContatoEpidemiologico` | caso, pessoa ou paciente, tipo de exposição, acompanhamento | V2 |
| `Surto` | agravo, território, período, situação, casos, medidas | V2 |
| `MedidaControle` | surto ou caso, tipo, responsável, data, situação | V2 |
| `EstabelecimentoRegulado` | razão social, CNPJ ou CPF, atividade, endereço, unidade (opcional) | V3 |
| `InspecaoSanitaria`, `IrregularidadeSanitaria`, `MedidaSanitaria`, `LicencaSanitaria` | estabelecimento, data, fiscal, achados, auto, prazos, validade | V3 |
| `EventoAmbiental`, `RiscoAmbiental`, `MonitoramentoAmbiental` | tipo, local, território, medições | V4 |
| `Vistoria`, `Foco`, `Animal`, `AcaoControle` | imóvel, agente, data, achado, espécie, ação | V4 |
| `EventoTrabalho`, `RiscoOcupacional`, `InvestigacaoTrabalho` | trabalhador, empresa, local, evento, acidente do RH (opcional) | V5 |

Arquitetura de pacotes prevista:

```text
vigilancia/
├── epidemiologica/  Agravo, Notificacao, InvestigacaoEpidemiologica, ClassificacaoCaso, ContatoEpidemiologico, Surto, MedidaControle
├── sanitaria/       EstabelecimentoRegulado, InspecaoSanitaria, IrregularidadeSanitaria, MedidaSanitaria, LicencaSanitaria
├── ambiental/       EventoAmbiental, RiscoAmbiental, MonitoramentoAmbiental
├── zoonoses/        Vistoria, Foco, Animal, AcaoControle
├── trabalhador/     EventoTrabalho, RiscoOcupacional, InvestigacaoTrabalho
└── compartilhado/   EventoVigilancia, LocalEvento, AcaoVigilancia
```

## 13. Domínio interno primeiro, adaptadores depois

O Mais Saúde Pública não tenta reproduzir SINAN, SIVEP, SISAGUA, e-SUS, CNES ou os sistemas da ANVISA. Primeiro vem
o modelo próprio; depois, os **adaptadores** do domínio 20 (Integrações):

```text
Mais Saúde Pública ── SINAN · SIVEP · SI-PNI · SISAGUA · CNES · SISAB/e-SUS APS · outros
```

Assim o modelo dos sistemas externos não contamina o domínio interno.

## 14. Fatias de implementação

| Fatia | Entrega | Depende de |
|---|---|---|
| **V1** · fundação | agravo (catálogo e ficha como dado), notificação e notificação negativa, investigação, classificação; sugestão de notificação a partir de atendimento, internação e exame | Paciente, Atendimento; Território opcional |
| **V2** · epidemiologia avançada | contato, surto, medida de controle, mapa e linha do tempo dos casos | V1; Território F1 e F2 para o mapa |
| **V3** · sanitária | estabelecimento regulado, inspeção, irregularidade, medida, licença | Documentos (autos) |
| **V4** · ambiental e zoonoses | evento e risco ambiental, monitoramento, vistoria, foco, animal, ação de controle | Território F3 (imóvel) |
| **V5** · trabalhador e integrações | evento do trabalho, risco ocupacional, investigação; adaptadores (SINAN primeiro) | V1; Integrações (#20) |

**Fora do escopo agora:**
- reproduzir o SINAN ou o SIVEP;
- vigilância de óbito (Serviço de Verificação de Óbito);
- laboratório de saúde pública (LACEN);
- a Imunização em si (#24);
- alertas automáticos de surto por regra estatística.

## 15. Referências

- Lei nº 6.259/1975: organização das ações de vigilância epidemiológica e notificação compulsória.
- Lei nº 6.437/1977: infrações à legislação sanitária federal e sanções.
- Portaria de Consolidação GM/MS nº 4/2017, anexo V: Lista Nacional de Notificação Compulsória, com as atualizações
  posteriores.
- [`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`](../MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md): LACEN, CEREST, Unidade de Vigilância de
  Zoonoses, Serviço de Verificação de Óbito, Centro de Imunização.
