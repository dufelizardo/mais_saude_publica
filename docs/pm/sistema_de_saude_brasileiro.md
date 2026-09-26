
Sim. Fiz o levantamento tomando como referência **a estrutura atual do SUS/CNES**, e há uma conclusão importante para o nosso projeto:

> **“Equipamento de saúde” não é sinônimo de “tipo de UnidadeDeSaude”.**

O CNES hoje contempla desde UBS e hospitais até farmácias, centrais de regulação, laboratórios de saúde pública, unidades móveis, CAPS, CEREST, telessaúde, vigilância de zoonoses etc. A própria classificação do CNES distingue tipos e classificações de estabelecimentos. 

Para o **Mais Saúde Pública**, eu faria o levantamento em camadas.

---

# 1. Atenção Primária

## 1.1 UBS / Centro de Saúde

É a principal porta de entrada da população no SUS.

### Função

* consultas médicas
* enfermagem
* acolhimento
* vacinação
* curativos
* procedimentos
* acompanhamento de doenças crônicas
* pré-natal
* puericultura
* saúde da mulher
* saúde da criança
* saúde do idoso
* saúde bucal
* acompanhamento territorial
* visitas domiciliares
* prevenção
* promoção da saúde
* vigilância em saúde

As equipes de Saúde da Família atuam sobre uma população e território definidos e realizam promoção, prevenção, diagnóstico, tratamento, reabilitação, redução de danos, cuidados paliativos e vigilância. ([Serviços e Informações do Brasil][1])

### No sistema

```text
UBS
 ├── Atendimento
 ├── Enfermagem
 ├── Vacinação
 ├── Saúde Bucal
 ├── Farmácia
 ├── Procedimentos
 ├── Visita Domiciliar
 └── Acompanhamento territorial
```

---

# 2. UBS Fluvial

É uma UBS adaptada para atendimento de populações ribeirinhas.

A diferença principal **não é clínica**, mas territorial e logística.

### Função

Executar atenção primária em áreas onde o acesso terrestre é limitado.

Pode envolver:

* consultas
* enfermagem
* vacinação
* procedimentos
* saúde bucal
* coleta
* ações preventivas
* acompanhamento de famílias

A UBS fluvial aparece inclusive entre as equipes de APS que podem ser vinculadas às eMulti. ([Serviços e Informações do Brasil][2])

### Para nosso domínio

Eu **não criaria `UnidadeBasicaFluvial`**.

Seria:

```text
TipoUnidadeDeSaude = UBS
caracteristicas:
    FLUVIAL
```

---

# 3. Consultório na Rua

É uma modalidade da Atenção Primária voltada a pessoas em situação de rua.

### Função

Levar o cuidado até o território onde a população está.

Pode envolver:

* atendimento clínico
* enfermagem
* saúde mental
* redução de danos
* vacinação
* curativos
* acompanhamento
* encaminhamento
* articulação social

É uma equipe de APS que pode trabalhar integrada às eMulti. ([Serviços e Informações do Brasil][3])

### Arquiteturalmente

Não necessariamente precisa ser um novo `TipoUnidadeDeSaude`.

Pode ser uma:

```text
Equipe/Servico
    CONSULTORIO_NA_RUA
```

---

# 4. Academia da Saúde

Não é uma UBS.

É um ponto de promoção da saúde integrado à APS.

### Função

* atividade física
* práticas corporais
* promoção da saúde
* educação em saúde
* ações comunitárias
* prevenção

O Ministério da Saúde define os polos como espaços públicos de promoção da saúde e produção do cuidado, integrados à APS. ([Serviços e Informações do Brasil][4])

### No sistema

```text
PoloAcademiaSaude
```

poderia ser um tipo próprio de equipamento.

---

# 5. Atenção Especializada Ambulatorial

Aqui entram:

* ambulatórios
* clínicas especializadas
* policlínicas
* centros de especialidades

O CNES contempla `Policlínica`, `Clínica/Centro de Especialidade` e `SADT`. 

## Função

Receber pacientes que precisam de atendimento além da capacidade da APS.

Exemplos:

```text
Cardiologia
Ortopedia
Neurologia
Endocrinologia
Dermatologia
Oftalmologia
Ginecologia
Otorrinolaringologia
```

### Fluxo

```text
UBS
 ↓
Encaminhamento
 ↓
Regulação
 ↓
Especialista
 ↓
Retorno/contrarreferência
```

Isso é muito importante para o nosso domínio **Regulação**.

---

# 6. CEO — Centro de Especialidades Odontológicas

É o ponto especializado da Rede de Saúde Bucal.

### Função

Receber casos encaminhados pela atenção primária para procedimentos odontológicos especializados.

O Ministério da Saúde caracteriza os CEOs como serviços de atenção especializada em saúde bucal que dão continuidade ao cuidado iniciado na APS. ([Serviços e Informações do Brasil][5])

### Exemplos

* cirurgia oral
* periodontia
* endodontia
* atendimento especializado
* diagnóstico de lesões

### Sistema

```text
UBS / Saúde Bucal
       ↓
      CEO
       ↓
Tratamento especializado
```

---

# 7. CAPS

Centro de Atenção Psicossocial.

É um equipamento especializado em **saúde mental**.

### Função

* acolhimento
* acompanhamento contínuo
* atendimento individual
* grupos
* terapias
* Projeto Terapêutico Singular
* atendimento de crises
* apoio familiar
* reinserção social
* articulação com a rede

Existem modalidades como:

```text
CAPS I
CAPS II
CAPS III
CAPS i
CAPS AD
CAPS AD III
```

com diferenças de público, porte e funcionamento. ([Serviços e Informações do Brasil][6])

### Particularidade

CAPS não é simplesmente "ambulatório de psicologia".

Ele tem um **modelo assistencial próprio**.

---

# 8. UPA 24h

É o equipamento intermediário da rede de urgência.

### Função

* atendimento de urgência
* avaliação inicial
* estabilização
* diagnóstico inicial
* observação
* atendimento clínico
* atendimento traumático/cirúrgico inicial
* encaminhamento hospitalar

A UPA integra a rede pré-hospitalar fixa e funciona 24 horas. ([Serviços e Informações do Brasil][7])

### Fluxo

```text
Paciente
   ↓
UPA
   ├── resolve
   ├── observação
   └── encaminha
          ↓
       Hospital
```

---

# 9. Pronto-Socorro / Porta Hospitalar

Diferente da UPA, está ligado à estrutura hospitalar.

### Função

* urgência
* emergência
* estabilização
* diagnóstico
* tratamento
* encaminhamento
* internação

A Rede de Atenção às Urgências articula APS, SAMU, UPA, hospitais e outros componentes. ([Serviços e Informações do Brasil][8])

---

# 10. Hospital Geral

É um dos equipamentos mais complexos.

### Pode possuir

```text
Emergência
Internação
UTI
Centro Cirúrgico
Centro Obstétrico
Pediatria
Clínica Médica
Clínica Cirúrgica
Diagnóstico
Laboratório
Farmácia
Hemoterapia
```

### Função

Prestar atenção hospitalar, incluindo internação, procedimentos cirúrgicos e cuidados de maior complexidade, conforme seu perfil e habilitações.

### Para nosso sistema

Hospital **não deve ser modelado como uma classe gigante**.

Deve ser:

```text
Hospital
 ├── Setores
 ├── Serviços
 ├── Leitos
 ├── Centro Cirúrgico
 ├── Emergência
 ├── Farmácia
 ├── Laboratório
 └── outros serviços
```

Isso reforça bastante a decisão que já tomamos para o Administrativo.

---

# 11. Hospital Especializado

É voltado para determinado conjunto de condições/especialidades.

Exemplos:

* oncologia
* cardiologia
* ortopedia
* pediatria
* saúde mental
* maternidade
* doenças infecciosas

### Conceito

A diferença não precisa criar outra arquitetura.

```text
Hospital
 └── especialidades/perfis
```

Ou seja:

```text
tipo = HOSPITAL
perfil = ESPECIALIZADO
especialidades = [...]
```

---

# 12. Maternidade / Centro de Parto Normal

Equipamento especializado na atenção obstétrica e neonatal.

### Funções

* pré-parto
* parto
* pós-parto
* assistência obstétrica
* cuidados ao recém-nascido
* acompanhamento materno
* encaminhamento de risco

O CNES contempla, entre outras classificações, centros de assistência obstétrica e neonatal normal. 

---

# 13. Serviço de Atenção Domiciliar

O cuidado acontece **na residência do paciente**.

### Função

* tratamento
* acompanhamento
* reabilitação
* cuidados contínuos
* cuidados paliativos
* desospitalização

A Atenção Domiciliar integra a Rede de Atenção à Saúde e pode envolver equipes de APS ou equipes especializadas como EMAD e EMAP. ([Serviços e Informações do Brasil][9])

### Modelo

```text
Hospital
   ↓
Necessidade de cuidado domiciliar
   ↓
SAD
   ↓
EMAD / EMAP
   ↓
Paciente em casa
```

---

# 14. SAMU 192

É o atendimento **pré-hospitalar móvel de urgência**.

### Função

* receber chamado
* fazer regulação médica
* orientar
* enviar equipe
* atendimento no local
* estabilização
* transporte quando necessário

O SAMU funciona 24/7 e é acionado pelo 192, com despacho regulado pela Central de Regulação das Urgências. ([Serviços e Informações do Brasil][10])

### Equipamentos móveis

```text
Ambulância USB
Ambulância USA
Motolância
Ambulancha
Aeromédico
```

### Importante

Aqui temos uma distinção arquitetural:

```text
SAMU = serviço/rede

Central de Regulação = estrutura operacional

Ambulância = recurso móvel
```

Não devemos transformar cada ambulância em `UnidadeDeSaude`.

---

# 15. Central de Regulação

É um dos equipamentos mais importantes para nosso futuro domínio de Regulação.

### Função

Gerenciar acesso aos serviços conforme:

* necessidade
* prioridade
* disponibilidade
* critérios clínicos
* referência
* capacidade da rede

Existem diferentes centrais, inclusive:

```text
Central de Regulação Médica das Urgências
Central de Regulação do Acesso
```

O CNES registra essas estruturas separadamente. 

---

# 16. Farmácia

É o equipamento de assistência farmacêutica.

### Função

* dispensação
* orientação
* armazenamento
* controle de medicamentos
* acompanhamento farmacoterapêutico
* gestão de estoque

No CNES, Farmácia aparece como tipo de estabelecimento próprio. 

### Importante para nossa arquitetura

A Farmácia é diferente de:

```text
Estoque
```

porque:

```text
Estoque = infraestrutura logística

Farmácia = serviço de assistência farmacêutica
```

Podem compartilhar o domínio de estoque, mas possuem responsabilidades diferentes.

---

# 17. Laboratório / Unidade de Apoio Diagnóstico

### Função

Executar exames necessários ao diagnóstico e acompanhamento.

Pode envolver:

* coleta
* processamento
* análise
* resultado
* controle de qualidade
* laudo

O CNES possui tanto unidades de apoio diagnóstico/terapia quanto laboratórios de saúde pública. 

---

# 18. LACEN / Laboratório de Saúde Pública

Aqui a função muda bastante.

O LACEN não é simplesmente um "laboratório municipal que faz exames".

### Função

* exames de maior complexidade
* vigilância laboratorial
* investigação de surtos
* controle de qualidade
* apoio epidemiológico
* análises de água/alimentos
* doenças de interesse em saúde pública
* suporte técnico à rede

O Ministério da Saúde destaca exatamente esse papel estratégico dos LACEN. ([Serviços e Informações do Brasil][11])

### Portanto

```text
Laboratório Assistencial
        ≠
Laboratório de Saúde Pública
```

Isso precisa aparecer no modelo.

---

# 19. Hemoterapia / Hemocentro

Equipamentos voltados para sangue e componentes.

### Funções

* coleta
* processamento
* armazenamento
* distribuição
* transfusão
* testes
* atendimento a doadores
* suporte à rede hospitalar

No CNES aparece a categoria de atenção hemoterápica/hematológica. 

---

# 20. Unidade de Reabilitação

Atende pessoas que precisam recuperar ou desenvolver capacidades funcionais.

### Exemplos

* fisioterapia
* terapia ocupacional
* fonoaudiologia
* reabilitação física
* reabilitação neurológica
* reabilitação intelectual
* reabilitação auditiva
* reabilitação visual

Pode envolver também fornecimento/adaptação de dispositivos.

---

# 21. Oficina Ortopédica

É especializada em:

* órteses
* próteses
* meios auxiliares de locomoção
* confecção
* adaptação
* manutenção

O Ministério da Saúde define a Oficina Ortopédica justamente como serviço de dispensação, confecção, adaptação e manutenção desses dispositivos. ([Serviços e Informações do Brasil][12])

---

# 22. CEREST

Centro de Referência em Saúde do Trabalhador.

### Função

* assistência especializada ao trabalhador
* investigação de doenças relacionadas ao trabalho
* vigilância
* promoção
* prevenção
* apoio técnico à rede
* educação permanente

Não é simplesmente uma clínica.

Ele também funciona como **referência técnica e articuladora da política de saúde do trabalhador**. ([Serviços e Informações do Brasil][13])

---

# 23. Unidade de Vigilância de Zoonoses

Aqui saímos da assistência clínica tradicional.

### Função

* vigilância de zoonoses
* investigação
* controle de vetores
* monitoramento de animais
* coleta de amostras
* prevenção
* ações contra surtos

O Ministério da Saúde deixa claro que UVZ **não é clínica veterinária**; sua atuação está relacionada às zoonoses de relevância para a saúde pública. ([Serviços e Informações do Brasil][14])

---

# 24. Centro de Imunização

Especializado em vacinação.

### Função

* vacinação
* conservação de imunobiológicos
* controle de cadeia de frio
* registro
* acompanhamento vacinal
* campanhas
* vacinação especial

Pode existir dentro de uma UBS, mas também pode ser uma estrutura própria.

Isso é importante para nosso modelo:

> **serviço não precisa necessariamente ser uma UnidadeDeSaude independente.**

---

# 25. Saúde Indígena

Aqui temos uma estrutura própria do **SasiSUS**, integrada ao SUS.

Hoje a estrutura inclui:

```text
DSEI
 ├── Polo Base
 ├── UBSI
 └── CASAI
```

Em setembro de 2026, o Ministério da Saúde informa 34 DSEI, 70 CASAI, 1.211 UBSI e 388 Polos Base. ([Serviços e Informações do Brasil][15])

### UBSI

Atendimento de saúde indígena no território.

### Polo Base

Coordenação/apoio das equipes e, conforme o tipo, execução de ações assistenciais.

### CASAI

Apoio e acolhimento ao indígena referenciado para atendimento fora do território. ([BVS MS][16])

Esse é outro caso onde **não devemos tentar encaixar tudo artificialmente em UBS/UPA/Hospital**.

---

# 26. Telessaúde

É um serviço/estrutura que utiliza tecnologia para ampliar acesso e apoiar profissionais.

### Funções

* teleconsultoria
* teleatendimento
* apoio diagnóstico
* segunda opinião
* educação permanente
* discussão de casos

O Ministério da Saúde está inclusive estruturando pontos de telessaúde nas UBS para ampliar o acesso à atenção especializada. ([Serviços e Informações do Brasil][17])

---

# 27. Unidades móveis

O CNES contempla diferentes unidades móveis.

Exemplos:

```text
Unidade móvel terrestre
Unidade móvel fluvial
Unidade móvel pré-hospitalar
```

Elas não devem necessariamente ser tratadas como unidades físicas convencionais.

### Arquiteturalmente

Eu pensaria em:

```text
UnidadeDeSaude
 └── modalidade = MOVEL

RecursoMovel
 └── veículo
```

---

# 28. Central de Abastecimento

É uma estrutura logística da rede.

### Função

* receber insumos
* armazenar
* controlar
* distribuir
* abastecer unidades

Isso se relaciona diretamente com o futuro domínio:

```text
Estoque
Compras
Logística
Farmácia
Administrativo
```

---

# 29. Central de Transplantes

É uma estrutura de coordenação do sistema de transplantes.

### Funções

* gerenciamento de potenciais doadores
* listas de espera
* compatibilidade
* coordenação da captação
* articulação com hospitais
* distribuição dos órgãos

O Ministério da Saúde descreve a Central como responsável, entre outras atividades, por coordenar confirmação, compatibilidade, lista de receptores e logística da retirada. ([Serviços e Informações do Brasil][18])

---

# 30. Serviço de Verificação de Óbito

É outro equipamento que não é assistencial no sentido tradicional.

### Função

Investigar mortes naturais sem causa esclarecida, conforme a organização do serviço.

Produz informação importante para:

```text
Mortalidade
Vigilância
Epidemiologia
Estatísticas de saúde
```

---

# O que isso muda no Mais Saúde Pública?

Aqui está a parte **mais importante para nossa arquitetura**.

Depois desse levantamento, eu **não usaria uma lista simples como**:

```text
UBS
UPA
Hospital
Laboratório
Farmácia
CAPS
...
```

como se todos fossem equivalentes.

Temos pelo menos **quatro naturezas diferentes**.

### 1. Pontos assistenciais

```text
UBS
UPA
Hospital
CAPS
Policlínica
CEO
Reabilitação
Laboratório
Farmácia
```

### 2. Serviços/estruturas de suporte à rede

```text
Central de Regulação
Central de Abastecimento
Central de Transplantes
LACEN
Telessaúde
CEREST
```

### 3. Estruturas territoriais/especializadas

```text
DSEI
Polo Base
UBSI
CASAI
Academia da Saúde
UVZ
```

### 4. Recursos/serviços móveis

```text
SAMU
Ambulância
Unidade Móvel
UBS Fluvial
```

---

# E isso é excelente para o nosso modelo

Eu mudaria ligeiramente o conceito que estávamos usando.

Em vez de pensar somente:

```text
TipoUnidadeDeSaude
```

precisamos separar:

```text
TipoEstabelecimento
TipoServico
PerfilAssistencial
Capacidade
Modalidade
```

Por exemplo:

```text
Hospital
│
├── Perfil: GERAL
│
├── Capacidades
│   ├── INTERNACAO
│   ├── CIRURGIA
│   ├── URGENCIA
│   ├── UTI
│   ├── LABORATORIO
│   └── FARMACIA
│
└── Serviços
    ├── Pronto Socorro
    ├── Centro Cirúrgico
    ├── UTI
    └── Ambulatório
```

Enquanto:

```text
UBS
│
├── Perfil: ATENCAO_PRIMARIA
│
├── Serviços
│   ├── Medicina
│   ├── Enfermagem
│   ├── Vacinação
│   └── Saúde Bucal
│
└── Capacidades
    ├── CONSULTA
    ├── PROCEDIMENTO
    ├── VACINACAO
    └── VISITA_DOMICILIAR
```

E:

```text
SAMU
│
├── Serviço: URGENCIA_MOVEL
├── Central de Regulação
└── Recursos
    ├── USB
    ├── USA
    ├── Motolância
    └── Ambulância
```

## Minha recomendação para o próximo passo

Agora que fizemos esse levantamento, **eu não partiria ainda para implementar #9 Farmácia**.

O próximo artefato deveria ser justamente:

**`MAPA-DE-EQUIPAMENTOS-DE-SAUDE.md`**

com uma tabela para cada equipamento contendo:

| Equipamento | Natureza            | Função                  | Público                | Serviços                        | Capacidades         | Depende de  | Pode conter         |
| ----------- | ------------------- | ----------------------- | ---------------------- | ------------------------------- | ------------------- | ----------- | ------------------- |
| UBS         | Assistencial        | APS                     | População adscrita     | Consulta, enfermagem, vacinação | APS                 | RH, Agenda  | Farmácia, vacinação |
| UPA         | Assistencial        | Urgência                | Demanda espontânea     | Urgência, observação            | Urgência            | Regulação   | Laboratório, RX     |
| Hospital    | Assistencial        | Alta/média complexidade | Referenciados/urgência | Internação, cirurgia            | Leitos, cirurgia    | Regulação   | UTI, farmácia       |
| CAPS        | Assistencial        | Saúde mental            | Usuários RAPS          | Acolhimento, PTS                | Saúde mental        | Rede        | —                   |
| LACEN       | Apoio especializado | Vigilância laboratorial | Rede/SUS               | Análises                        | Diagnóstico público | Laboratório | —                   |
| CEREST      | Referência          | Saúde do trabalhador    | Trabalhadores          | Assistência/vigilância          | Saúde trabalhador   | Vigilância  | —                   |
| SAMU        | Móvel               | Urgência pré-hospitalar | População              | Atendimento móvel               | APH                 | Regulação   | Ambulâncias         |

E aí conseguimos fazer algo **muito mais poderoso para o seu domínio `UnidadeDeSaude`**:

> definir quais equipamentos são realmente `TipoUnidadeDeSaude`, quais são **serviços**, quais são **estruturas de apoio**, quais são **recursos móveis** e quais são apenas **capacidades que uma unidade pode possuir**.

Isso evita que o Mais Saúde Pública acabe com 30 subclasses de `UnidadeDeSaude` e deixa o modelo compatível com a realidade do SUS. O próprio CNES mostra que a rede brasileira é muito mais rica que apenas UBS/UPA/Hospital. 

[1]: https://www.gov.br/saude/pt-br/composicao/saps/esf/equipe-saude-da-familia/faq/quais-sao-as-atribuicoes-comuns-dos-profissionais-de-uma-equipe-de-saude-da-familia?utm_source=chatgpt.com "Quais são as atribuições comuns dos profissionais de uma equipe de Saúde da Família? — Ministério da Saúde"
[2]: https://www.gov.br/saude/pt-br/composicao/saps/acoes-interprofissionais/emulti/faq/orientacoes-sobre-o-cadastro-de-emulti-no-scnes/quais-equipes-de-aps-podem?utm_source=chatgpt.com "Quais equipes de APS podem estar vinculadas a uma eMulti? — Ministério da Saúde"
[3]: https://www.gov.br/saude/pt-br/composicao/saps/acoes-interprofissionais/emulti?utm_source=chatgpt.com "eMulti — Ministério da Saúde"
[4]: https://www.gov.br/saude/pt-br/composicao/saps/academia-da-saude?utm_source=chatgpt.com "Programa Academia da Saúde — Ministério da Saúde"
[5]: https://www.gov.br/saude/pt-br/composicao/saps/brasil-sorridente/ceo?utm_source=chatgpt.com "Centro de Especialidades Odontológicas — Ministério da Saúde"
[6]: https://www.gov.br/saude/pt-br/composicao/saes/desmad/raps/caps?utm_source=chatgpt.com "Centros de Atenção Psicossocial — Ministério da Saúde"
[7]: https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/u/upa-24h?utm_source=chatgpt.com "UPA 24h — Ministério da Saúde"
[8]: https://www.gov.br/saude/pt-br/composicao/saes/samu-192/rau?utm_source=chatgpt.com "Rede de Atenção às Urgências e Emergências — Ministério da Saúde"
[9]: https://www.gov.br/saude/pt-br/composicao/saes/dahu/atencao-domiciliar?trk=article-ssr-frontend-pulse_little-text-block&utm_source=chatgpt.com "Atenção Domiciliar — Ministério da Saúde"
[10]: https://www.gov.br/saude/pt-br/composicao/saes/samu-192?utm_source=chatgpt.com "SAMU 192 — Ministério da Saúde"
[11]: https://www.gov.br/saude/pt-br/assuntos/novo-pac-saude/preparacao-para-emergencias-sanitarias?utm_source=chatgpt.com "Preparação para Emergências Sanitárias — Ministério da Saúde"
[12]: https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/s/saude-da-pessoa-com-deficiencia/faq/o-que-e-uma-oficina?utm_source=chatgpt.com "O que é uma Oficina Ortopédica? — Ministério da Saúde"
[13]: https://www.gov.br/saude/pt-br/composicao/svsa/cerest?utm_source=chatgpt.com "Centro de Referência em Saúde do Trabalhador — Ministério da Saúde"
[14]: https://www.gov.br/saude/pt-br/assuntos/saude-de-a-a-z/z/zoonoses/faq?utm_source=chatgpt.com "Perguntas Frequentes (FAQ) — Ministério da Saúde"
[15]: https://www.gov.br/saude/pt-br/assuntos/noticias-ms/2026/setembro/sasisus-completa-27-anos-e-representa-marco-a-atencao-a-saude-dos-povos-indigenas/?utm_source=chatgpt.com "SasiSUS completa 27 anos e representa marco à atenção à saúde dos povos indígenas — Ministério da Saúde"
[16]: https://bvsms.saude.gov.br/bvs/publicacoes/manual_atendimento_indigenas_expostos_mercurio.pdf?utm_source=chatgpt.com "e política, de modo a favorecer a superação dos fatores que tornam essa população mais vulnerável aos agravos à saúde de maior magnitude e transcendência entre os brasileiros, reconhecendo a eficácia de sua medicina e o direito desses povos à sua cultura."
[17]: https://www.gov.br/saude/pt-br/assuntos/novo-pac-saude/selecao-2025/kits-de-telessaude?utm_source=chatgpt.com "Kits de Telessaúde — Ministério da Saúde"
[18]: https://www.gov.br/saude/pt-br/composicao/saes/snt/faq/transplantes/como-funciona-o-sistema-de?utm_source=chatgpt.com "Como funciona o sistema de captação de órgãos? — Ministério da Saúde"
