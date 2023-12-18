# Documentação da API Mais Saúde Pública
## ![visitors](https://visitor-badge.laobi.icu/badge?page_id=dufelizardo.visitor-mais_saude_publica) ![GitHub followers](https://img.shields.io/github/followers/edufelizardo1.visitor-mais_saude_publica?style=social) <img src="https://img.shields.io/badge/Completed-0%25-red"/>  <img src="https://img.shields.io/badge/public-Yes-green"/>
Mais Saúde Pública API é uma plataforma dedicada a fornecer acesso fácil e rápido a dados essenciais relacionados à saúde pública. Oferecendo uma variedade de endpoints, nossa API permite a recuperação de informações atualizadas sobre estatísticas de saúde, serviços médicos, campanhas de vacinação, e outros dados cruciais para promover o bem-estar e a conscientização na esfera da saúde pública. Desenvolvida para facilitar integrações em aplicativos, sistemas de informação e serviços online, a API Mais Saúde Pública visa fortalecer a disponibilidade de informações relevantes e contribuir para a tomada de decisões informadas na gestão da saúde pública.

## Visão Geral
Bem-vindo à documentação oficial da API Mais Saúde Pública. Esta API foi desenvolvida para fornecer acesso fácil e confiável a dados relacionados à saúde pública. Abaixo estão os principais recursos, instruções de autenticação e diretrizes para usar efetivamente a API.

## Recursos Principais
1. **Estatísticas de Saúde:**
   - Endpoint: `/health-statistics`
   - Descrição: Recupere estatísticas atualizadas sobre diversos indicadores de saúde pública.

2. **Serviços Médicos:**
   - Endpoint: `/medical-services`
   - Descrição: Explore informações sobre serviços médicos disponíveis, incluindo localização e especialidades.

3. **Campanhas de Vacinação:**
   - Endpoint: `/vaccination-campaigns`
   - Descrição: Acesse detalhes sobre campanhas de vacinação em andamento e planejadas.

4. **Cadastro de organizações:**
   - **MINISTÉRIO DA SAÚDE**
      - Endpoint: `/api/primeiro-grupo-medical`
         - Verbos: GET e POST
         - Descrição: Acesse detalhes sobre o Ministério da Saúde.
         - **Exemplo de Body e Response**
            ```bash
            [
                {
                    "nomeInstitucional": "Ministério da Saúde",
                    "endereco": {
                        "cep": "70058-900",
                        "logradouro": "Esplanada dos Ministérios Bloco G",
                        "numeroLogradouro": "S/N",
                        "complemento": "",
                        "bairro": "Zona Cívico-Administrativa",
                        "cidade": "Brasília",
                        "estado": "DF",
                        "ddd": "61"
                    },
                    "horarioFuncionamento": {
                        "WEDNESDAY": "08:00 - 21:00",
                        "SATURDAY": "Closed",
                        "MONDAY": "08:00 - 21:00",
                        "TUESDAY": "08:00 - 21:00",
                        "THURSDAY": "08:00 - 21:00",
                        "SUNDAY": "Closed",
                        "FRIDAY": "08:00 - 21:00"
                    },
                    "horarioAtendimento": {
                        "WEDNESDAY": "08:00 - 18:00",
                        "SATURDAY": "Closed",
                        "MONDAY": "08:00 - 18:00",
                        "TUESDAY": "08:00 - 18:00",
                        "THURSDAY": "08:00 - 18:00",
                        "SUNDAY": "Closed",
                        "FRIDAY": "08:00 - 18:00"
                    }
                }
            ]
            ```
      - Endpoint: `/api/primeiro-grupo-medical/dados/{nomeInstitucional}`
         - Verbos: PUT
         - Descrição: Altere as informações de Endereço e ou Horario de Funcionamento e ou Atendimento.
      - Endpoint: `/api/primeiro-grupo-medical/{nomeInstitucional}`
         - Verbos: PUT
         - Descrição: Altere o Nome Institucional caso o tenha cadastrado erroneamente.

## Instruções de Autenticação

Para acessar os recursos da API, é necessário autenticação. Siga as instruções abaixo para obter as credenciais necessárias:

- Endpoint de Autenticação: `/auth`
- Método: `POST`
- Parâmetros:
  - `username`: Seu nome de usuário
  - `password`: Sua senha

## Uso da API

Para fazer uma solicitação à API, utilize o método HTTP apropriado para o recurso desejado. Certifique-se de incluir as credenciais de autenticação no cabeçalho da solicitação.

Exemplo de solicitação usando cURL:

```bash
curl -X GET -H "Authorization: Bearer SEU_TOKEN" https://api.mais-saude-publica.com/health-statistics 
```
## Exemplos

### Recuperar Estatísticas de Saúde
```bash
curl -X GET -H "Authorization: Bearer SEU_TOKEN" https://api.mais-saude-publica.com/health-statistics 
```

## Explorar Serviços Médicos
```bash
curl -X GET -H "Authorization: Bearer SEU_TOKEN" https://api.mais-saude-publica.com/medical-services 
```
## Considerações Finais
Agradecemos por escolher a API Mais Saúde Pública. Em caso de dúvidas ou problemas, entre em contato conosco em meugit.edufelizardo@gmail.com.

Esperamos que essa estrutura ajude na criação da documentação para a sua API "Mais Saúde Pública". Personalize conforme necessário para atender às especificidades da sua API.

## OBSERVAÇÂO: Está é apenas a documentação inicial
