# Public Health Plus API Documentation
## ![visitors](https://visitor-badge.laobi.icu/badge?page_id=dufelizardo.visitor-mais_saude_publica) ![GitHub followers](https://img.shields.io/github/followers/dufelizardo.visitor-mais_saude_publica?style=social) <img src="https://img.shields.io/badge/Completed-0%25-red"/>  <img src="https://img.shields.io/badge/public-Yes-green"/>
The Public Health Plus API is a platform designed to provide quick and easy access to critical public health data. Offering a variety of endpoints, the API allows for the retrieval of up-to-date information on health statistics, medical services, vaccination campaigns, and other essential data to promote well-being and awareness in the public health sector. Developed to facilitate integration into applications, information systems, and online services, the Public Health Plus API aims to enhance the availability of relevant information, aiding in informed decision-making in public health management.

## Overview

Welcome to the official documentation for the Public Health Plus API. This API has been developed to provide reliable access to public health data. Below are the main features, authentication instructions, and guidelines for effectively using the API.

## Main Features

1. **Health Statistics**
   - **Endpoint:** `/health-statistics`
   - **Description:** Retrieve up-to-date statistics on various public health indicators.

2. **Medical Services**
   - **Endpoint:** `/medical-services`
   - **Description:** Access information about available medical services, including location and specialties.

3. **Vaccination Campaigns**
   - **Endpoint:** `/vaccination-campaigns`
   - **Description:** Obtain details about ongoing and planned vaccination campaigns.

4. **Organization Registration**
   - **Primeiro Grupo Medical**
     - **Endpoint:** `/api/primeiro-grupo-medical`
       - **Methods:** GET, POST
       - **Description:** Access details about the Ministry of Health.
       - **Example Body and Response:**
         ```json
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
                     "MONDAY": "08:00 - 21:00",
                     "TUESDAY": "08:00 - 21:00",
                     "WEDNESDAY": "08:00 - 21:00",
                     "THURSDAY": "08:00 - 21:00",
                     "FRIDAY": "08:00 - 21:00",
                     "SATURDAY": "Closed",
                     "SUNDAY": "Closed"
                 },
                 "horarioAtendimento": {
                     "MONDAY": "08:00 - 18:00",
                     "TUESDAY": "08:00 - 18:00",
                     "WEDNESDAY": "08:00 - 18:00",
                     "THURSDAY": "08:00 - 18:00",
                     "FRIDAY": "08:00 - 18:00",
                     "SATURDAY": "Closed",
                     "SUNDAY": "Closed"
                 }
             }
         ]
         ```
     - **Endpoint:** `/api/primeiro-grupo-medical/dados/{nomeInstitucional}`
       - **Method:** PUT
       - **Description:** Update Address, Operating Hours, or Service Hours information.
     - **Endpoint:** `/api/primeiro-grupo-medical/{nomeInstitucional}`
       - **Method:** PUT
       - **Description:** Update the Institutional Name if it was registered incorrectly.

## Authentication Instructions

To access the API's resources, authentication is required. Follow the instructions below to obtain the necessary credentials:

- **Authentication Endpoint:** `/auth`
- **Method:** POST
- **Parameters:**
  - `username`: Your username
  - `password`: Your password

## Using the API

To make a request to the API, use the appropriate HTTP method for the desired resource. Be sure to include the authentication credentials in the request header.

**Example request using cURL:**

```bash
curl -X GET -H "Authorization: Bearer YOUR_TOKEN" https://api.mais-saude-publica.com/health-statistics 
```

## Examples

### Retrieve Health Statistics

```bash
curl -X GET -H "Authorization: Bearer YOUR_TOKEN" https://api.mais-saude-publica.com/health-statistics 
```

### Explore Medical Services

```bash
curl -X GET -H "Authorization: Bearer YOUR_TOKEN" https://api.mais-saude-publica.com/medical-services 
```

## Final Considerations

Thank you for using the Public Health Plus API. If you have any questions or issues, please contact us at [meugit.edufelizardo@gmail.com](mailto:meugit.edufelizardo@gmail.com).

## Note

This is just the initial documentation. We plan to update and expand it as new features and improvements are implemented.

---

This translation should ensure the documentation is clear and accessible to English-speaking users. Let me know if you need further adjustments!
