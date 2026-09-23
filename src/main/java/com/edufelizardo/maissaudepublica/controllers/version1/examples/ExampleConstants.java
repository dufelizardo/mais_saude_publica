package com.edufelizardo.maissaudepublica.controllers.version1.examples;

public class ExampleConstants {

    public static final String FEDERAL_RESPONSE_EXAMPLE = """
                [
                    {
                        "nome": "Ministério da Saúde",
                        "tipo": "FEDERAL",
                        "endereco": {
                            "cep": "70058-900",
                            "logradouro": "Esplanada dos Ministérios",
                            "numeroLogradouro": "Bloco G",
                            "complemento": "Edifício Sede",
                            "bairro": "",
                            "cidade": "Brasília",
                            "estado": "DF",
                            "ddd": ""
                        },
                        "telefones": [
                            "060-4567-9894"
                        ],
                        "email": "nao_informado@gov.br",
                        "horarioFuncionamento": {
                            "MONDAY": "8:00 AM - 9:00 PM",
                            "TUESDAY": "8:00 AM - 9:00 PM",
                            "WEDNESDAY": "8:00 AM - 9:00 PM",
                            "THURSDAY": "8:00 AM - 9:00 PM",
                            "FRIDAY": "8:00 AM - 9:00 PM"
                        },
                        "horarioAtendimento": {
                            "MONDAY": "8:00 AM - 6:00 PM",
                            "TUESDAY": "8:00 AM - 6:00 PM",
                            "WEDNESDAY": "8:00 AM - 6:00 PM",
                            "THURSDAY": "8:00 AM - 6:00 PM",
                            "FRIDAY": "8:00 AM - 6:00 PM"
                        }
                    }
                ]
            """;
    public static final String FEDERAL_RESPONSE_FIND_EXAMPLE = """
                {
                    "nome": "Ministério da Saúde",
                    "tipo": "FEDERAL",
                    "endereco": {
                        "cep": "05120-020",
                        "logradouro": "Esplanada dos Ministérios",
                        "numeroLogradouro": "Bloco G",
                        "complemento": "Edifício Sede",
                        "bairro": "Pirituba",
                        "cidade": "Brasília",
                        "estado": "DF",
                        "ddd": ""
                    },
                    "telefones": [
                        "060-4567-9894"
                    ],
                    "email": "nao_informado@gov.br",
                    "horarioFuncionamento": {
                        "MONDAY": "8:00 AM - 9:00 PM",
                        "TUESDAY": "8:00 AM - 9:00 PM",
                        "WEDNESDAY": "8:00 AM - 9:00 PM",
                        "THURSDAY": "8:00 AM - 9:00 PM",
                        "FRIDAY": "8:00 AM - 9:00 PM"
                    },
                    "horarioAtendimento": {
                        "MONDAY": "8:00 AM - 6:00 PM",
                        "TUESDAY": "8:00 AM - 6:00 PM",
                        "WEDNESDAY": "8:00 AM - 6:00 PM",
                        "THURSDAY": "8:00 AM - 6:00 PM",
                        "FRIDAY": "8:00 AM - 6:00 PM"
                    }
                }
            """;
    public static final String SUCCESS_RESPONSE_EXAMPLE = """
                {
                    "message": "Unidade de Saúde criada com sucesso!",
                    "details": "Nome: Gerencia de Saúde, Tipo: FEDERAL"
                }
            """;
    public static final String SUCCESS_RESPONSE_UPDATE_EXAMPLE = """
                {
                    "message": "Unidade de Saúde atualizada com sucesso!",
                    "details": "Nome: Gerencia de Saúde"
                }
            """;
    public static final String ERROR_EXAMPLE_400 = """
                {
                  "message": "A solicitação do cliente está malformada ou não pode ser processada pelo servidor.",
                  "details": "Bad Request"
                }
            """;
    public static final String ERROR_EXAMPLE_401 = """
                {
                  "message": "O cliente precisa autenticar-se para obter a resposta.",
                  "details": "Unauthorized"
                }
            """;
    public static final String ERROR_EXAMPLE_403 = """
                {
                  "message": "O cliente está autenticado, mas não tem permissão para criar o recurso.",
                  "details": "Forbidden"
                }
            """;
    public static final String ERROR_EXAMPLE_404 = """
                {
                  "message": "O servidor não encontrou o recurso solicitado.",
                  "details": "Not Found"
                }
            """;
    public static final String ERROR_EXAMPLE_409 = """
                {
                  "message": "Conflito com o estado atual do recurso.",
                  "details": "Conflict"
                }
            """;
    public static final String ERROR_EXAMPLE_422 = """
                {
                  "message": "Solicitação não permitida.",
                  "details": "Unprocessable Entity"
                }
            """;
    public static final String ERROR_EXAMPLE_500 = """
                {
                  "message": "O servidor encontrou uma condição inesperada que impediu-o de atender a solicitação.",
                  "details": "Internal Server Error"
                }
            """;
    public static final String ERROR_EXAMPLE_504 = """
                {
                  "message": "O servidor atuando como gateway não recebeu uma resposta a tempo do servidor upstream.",
                  "details": "Gateway Timeout"
                }
            """;
    public static final String ESTADUAL_RESPONSE_EXAMPLE = """
                [
                     {
                         "nome": "Ministério da Saúde",
                         "tipo": "ESTADUAL",
                         "endereco": {
                             "cep": "70058-900",
                             "logradouro": "Esplanada dos Ministérios",
                             "numeroLogradouro": "Bloco G",
                             "complemento": "Edifício Sede",
                             "bairro": "Aviao",
                             "cidade": "Brasília",
                             "estado": "DF",
                             "ddd": ""
                         },
                         "saudeTelefones": [],
                         "email": "nao_informado@gov.br",
                         "horarioFuncionamento": {
                             "TUESDAY": "8:00 AM - 9:00 PM",
                             "MONDAY": "8:00 AM - 9:00 PM",
                             "WEDNESDAY": "8:00 AM - 9:00 PM",
                             "THURSDAY": "8:00 AM - 9:00 PM",
                             "FRIDAY": "8:00 AM - 9:00 PM"
                         },
                         "horarioAtendimento": {
                             "TUESDAY": "8:00 AM - 6:00 PM",
                             "MONDAY": "8:00 AM - 6:00 PM",
                             "WEDNESDAY": "8:00 AM - 6:00 PM",
                             "THURSDAY": "8:00 AM - 6:00 PM",
                             "FRIDAY": "8:00 AM - 6:00 PM"
                         }
                     }
                 ]
            """;
    public static final String ESTADUAL_RESPONSE_FIND_EXAMPLE = """
                {
                    "nome": "SES-DRS I-Grande São Paulo",
                    "tipo": "ESTADUAL",
                    "estado": "SP",
                    "administracaoSuperior": "Ministério da Saúde",
                    "endereco": {
                        "cep": "01037-000",
                        "logradouro": "Rua Conselheiro Crispiniano",
                        "numeroLogradouro": "20",
                        "complemento": "1º andar",
                        "bairro": "Aviao",
                        "cidade": "Centro",
                        "estado": "SP",
                        "ddd": ""
                    },
                    "saudeTelefones": [],
                    "email": "drs1@saude.sp.gov.br",
                    "horarioFuncionamento": {
                        "TUESDAY": "8:00 AM - 9:00 PM",
                        "MONDAY": "8:00 AM - 9:00 PM",
                        "WEDNESDAY": "8:00 AM - 9:00 PM",
                        "THURSDAY": "8:00 AM - 9:00 PM",
                        "FRIDAY": "8:00 AM - 9:00 PM"
                    },
                    "horarioAtendimento": {
                        "TUESDAY": "8:00 AM - 6:00 PM",
                        "MONDAY": "8:00 AM - 6:00 PM",
                        "WEDNESDAY": "8:00 AM - 6:00 PM",
                        "THURSDAY": "8:00 AM - 6:00 PM",
                        "FRIDAY": "8:00 AM - 6:00 PM"
                    }
                }
            """;
    public static final String MUNICIPAL_RESPONSE_EXAMPLE = """
                [
                    {
                        "nome": "Coordenadoria Regional de Saúde Sudeste I",
                        "tipo": "MUNICIPAL",
                        "administracaoSuperior": "SES-DRS I-Grande São Paulo",
                        "municipio": "São Paulo",
                        "endereco": {
                            "cep": "02012-040",
                            "logradouro": "Rua Padre Marchetti",
                            "numeroLogradouro": "557",
                            "complemento": "",
                            "bairro": "Ipiranga",
                            "cidade": "São Paulo",
                            "estado": "SP",
                            "ddd": ""
                        },
                        "saudeTelefones": ["(11)2063-7185"],
                        "email": "crssudeste@prefeituras.sp.gov.br",
                        "horarioFuncionamento": {
                            "THURSDAY": "8:00 AM - 9:00 PM",
                            "TUESDAY": "8:00 AM - 9:00 PM",
                            "WEDNESDAY": "8:00 AM - 9:00 PM",
                            "FRIDAY": "8:00 AM - 9:00 PM",
                            "MONDAY": "8:00 AM - 9:00 PM"
                        },
                        "horarioAtendimento": {
                            "THURSDAY": "8:00 AM - 6:00 PM",
                            "TUESDAY": "8:00 AM - 6:00 PM",
                            "WEDNESDAY": "8:00 AM - 6:00 PM",
                            "FRIDAY": "8:00 AM - 6:00 PM",
                            "MONDAY": "8:00 AM - 6:00 PM"
                        }
                    }
                ]
            """;
    public static final String MUNICIPAL_RESPONSE_FIND_EXAMPLE = """
                {
                    "nome": "Coordenadoria Regional de Saúde Sudeste I",
                    "tipo": "MUNICIPAL",
                    "administracaoSuperior": "SES-DRS I-Grande São Paulo",
                    "municipio": "São Paulo",
                    "endereco": {
                        "cep": "02012-040",
                        "logradouro": "Rua Padre Marchetti",
                        "numeroLogradouro": "557",
                        "complemento": "",
                        "bairro": "Ipiranga",
                        "cidade": "São Paulo",
                        "estado": "SP",
                        "ddd": ""
                    },
                    "saudeTelefones": ["(11)2063-7185"],
                    "email": "crssudeste@prefeituras.sp.gov.br",
                    "horarioFuncionamento": {
                        "THURSDAY": "8:00 AM - 9:00 PM",
                        "TUESDAY": "8:00 AM - 9:00 PM",
                        "WEDNESDAY": "8:00 AM - 9:00 PM",
                        "FRIDAY": "8:00 AM - 9:00 PM",
                        "MONDAY": "8:00 AM - 9:00 PM"
                    },
                    "horarioAtendimento": {
                        "THURSDAY": "8:00 AM - 6:00 PM",
                        "TUESDAY": "8:00 AM - 6:00 PM",
                        "WEDNESDAY": "8:00 AM - 6:00 PM",
                        "FRIDAY": "8:00 AM - 6:00 PM",
                        "MONDAY": "8:00 AM - 6:00 PM"
                    }
                }
            """;
    public static final String REGIONAL_RESPONSE_EXAMPLE = """
                [
                  {
                    "nome": "string",
                    "tipo": "REGIONAL",
                    "administracaoSuperior": "string",
                    "regiao": "string",
                    "endereco": {
                      "cep": "string",
                      "logradouro": "string",
                      "numeroLogradouro": "string",
                      "complemento": "string",
                      "bairro": "string",
                      "cidade": "string",
                      "estado": "string",
                      "ddd": "string"
                    },
                    "saudeTelefones": [
                      "string"
                    ],
                    "email": "string",
                    "horarioFuncionamento": {
                      "additionalProp1": "string",
                      "additionalProp2": "string",
                      "additionalProp3": "string"
                    },
                    "horarioAtendimento": {
                      "additionalProp1": "string",
                      "additionalProp2": "string",
                      "additionalProp3": "string"
                    }
                  }
                ]
            """;
    public static final String REGIONAL_RESPONSE_FIND_EXAMPLE = """
                {
                  "nome": "string",
                  "tipo": "REGIONAL",
                  "administracaoSuperior": "string",
                  "regiao": "string",
                  "endereco": {
                    "cep": "string",
                    "logradouro": "string",
                    "numeroLogradouro": "string",
                    "complemento": "string",
                    "bairro": "string",
                    "cidade": "string",
                    "estado": "string",
                    "ddd": "string"
                  },
                  "saudeTelefones": [
                    "string"
                  ],
                  "email": "string",
                  "horarioFuncionamento": {
                    "additionalProp1": "string",
                    "additionalProp2": "string",
                    "additionalProp3": "string"
                  },
                  "horarioAtendimento": {
                    "additionalProp1": "string",
                    "additionalProp2": "string",
                    "additionalProp3": "string"
                  }
                }
            """;
    public static final String UNIDADE_SAUDE_RESPONSE_EXAMPLE = """
                [
                  {
                    "nome": "string",
                    "tipo": "UBS",
                    "administracaoSuperior": "string",
                    "supervisaoRegional": "string",
                    "responsavelCpf": "string",
                    "responsavelNome": "string",
                    "endereco": {
                      "cep": "string",
                      "logradouro": "string",
                      "numeroLogradouro": "string",
                      "complemento": "string",
                      "bairro": "string",
                      "cidade": "string",
                      "estado": "string",
                      "ddd": "string"
                    },
                    "saudeTelefones": [
                      "string"
                    ],
                    "email": "string",
                    "horarioFuncionamento": {
                      "additionalProp1": "string",
                      "additionalProp2": "string",
                      "additionalProp3": "string"
                    },
                    "horarioAtendimento": {
                      "additionalProp1": "string",
                      "additionalProp2": "string",
                      "additionalProp3": "string"
                    }
                  }
                ]
            """;
    public static final String UNIDADE_SAUDE_RESPONSE_FIND_EXAMPLE = """
                {
                  "nome": "string",
                  "tipo": "UBS",
                  "administracaoSuperior": "string",
                  "supervisaoRegional": "string",
                  "responsavelCpf": "string",
                  "responsavelNome": "string",
                  "endereco": {
                    "cep": "string",
                    "logradouro": "string",
                    "numeroLogradouro": "string",
                    "complemento": "string",
                    "bairro": "string",
                    "cidade": "string",
                    "estado": "string",
                    "ddd": "string"
                  },
                  "saudeTelefones": [
                    "string"
                  ],
                  "email": "string",
                  "horarioFuncionamento": {
                    "additionalProp1": "string",
                    "additionalProp2": "string",
                    "additionalProp3": "string"
                  },
                  "horarioAtendimento": {
                    "additionalProp1": "string",
                    "additionalProp2": "string",
                    "additionalProp3": "string"
                  }
                }
            """;
    public static final String PROFISSIONAL_RESPONSE_EXAMPLE = """
                [
                  {
                    "matricula": "11111111111111-11",
                    "cpf": "string",
                    "nome": "string",
                    "conselhoClasse": "string",
                    "numeroConselho": "string",
                    "endereco": {
                      "cep": "string",
                      "logradouro": "string",
                      "numeroLogradouro": "string",
                      "complemento": "string",
                      "bairro": "string",
                      "cidade": "string",
                      "estado": "string",
                      "ddd": "string"
                    },
                    "telefones": [
                      "string"
                    ],
                    "email": "string",
                    "dataAdmissao": "2024-01-01",
                    "dataDesligamento": null,
                    "ativo": true
                  }
                ]
            """;
    public static final String PROFISSIONAL_RESPONSE_FIND_EXAMPLE = """
                {
                  "matricula": "11111111111111-11",
                  "cpf": "string",
                  "nome": "string",
                  "conselhoClasse": "string",
                  "numeroConselho": "string",
                  "endereco": {
                    "cep": "string",
                    "logradouro": "string",
                    "numeroLogradouro": "string",
                    "complemento": "string",
                    "bairro": "string",
                    "cidade": "string",
                    "estado": "string",
                    "ddd": "string"
                  },
                  "telefones": [
                    "string"
                  ],
                  "email": "string",
                  "dataAdmissao": "2024-01-01",
                  "dataDesligamento": null,
                  "ativo": true
                }
            """;
    public static final String CATEGORIA_SALARIAL_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "nome": "Enfermagem",
                    "convencaoColetiva": "SINDSAUDE-SP 2026"
                  }
                ]
            """;
    public static final String CARGO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "nome": "Enfermeiro",
                    "categoriaUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "categoriaNome": "Enfermagem",
                    "descricao": "Atuação em plantões noturnos, 12x36."
                  }
                ]
            """;
    public static final String LOTACAO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "unidadeNome": "string",
                    "cargoNome": "Enfermeiro",
                    "categoriaNome": "Enfermagem",
                    "jornadaSemanalHoras": 40,
                    "dataInicio": "2024-01-01",
                    "dataFim": null,
                    "motivo": "Admissão"
                  }
                ]
            """;
    public static final String TABELA_SALARIAL_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "cargoNome": "Enfermeiro",
                    "categoriaNome": "Enfermagem",
                    "valorBase": 5000.00,
                    "dataVigencia": "2026-01-01",
                    "motivo": "DISSIDIO"
                  }
                ]
            """;
    public static final String REGRA_ANUENIO_RESPONSE_EXAMPLE = """
                {
                  "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "categoriaUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa7",
                  "categoriaNome": "Enfermagem",
                  "percentualPorAno": 1.00,
                  "tetoAnos": 25
                }
            """;
    public static final String AJUSTE_INDIVIDUAL_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "valor": 300.00,
                    "dataInicio": "2026-01-01",
                    "dataFim": null,
                    "motivo": "GRATIFICACAO_PESSOAL",
                    "referencia": null
                  }
                ]
            """;
    public static final String TIPO_BENEFICIO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "nome": "Vale Refeição",
                    "custeio": "EMPRESA"
                  }
                ]
            """;
    public static final String VALOR_BENEFICIO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "tipoBeneficioNome": "Vale Refeição",
                    "valor": 35.00,
                    "dataVigencia": "2026-01-01",
                    "motivo": "Reajuste"
                  }
                ]
            """;
    public static final String ADESAO_BENEFICIO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "tipoBeneficioNome": "Vale Refeição",
                    "dataInicio": "2026-01-01",
                    "dataFim": null,
                    "quantidadeDependentes": null
                  }
                ]
            """;
    public static final String AFASTAMENTO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "tipo": "FERIAS",
                    "dataInicio": "2026-01-01",
                    "dataFim": "2026-01-30",
                    "status": "APROVADO",
                    "observacao": null
                  }
                ]
            """;
    public static final String REGISTRO_PONTO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "dataHora": "2026-01-05T08:00:00",
                    "tipo": "ENTRADA",
                    "origem": "app"
                  }
                ]
            """;
    public static final String LICENCA_RESPONSE_EXAMPLE = """
                {
                  "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "afastamentoId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                  "profissionalMatricula": "11111111111111-11",
                  "profissionalNome": "string",
                  "tipoLegal": "MATERNIDADE",
                  "responsavelPagamento": "INSS",
                  "documentoUrl": null
                }
            """;
    public static final String CALCULO_RESCISAO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "tipoDesligamento": "SEM_JUSTA_CAUSA",
                    "avisoPrevio": 3000.00,
                    "feriasVencidas": 0.00,
                    "feriasProporcionais": 1500.00,
                    "decimoTerceiroProporcional": 1000.00,
                    "multaFgts": 1200.00,
                    "total": 6700.00,
                    "documentoTrctUrl": null
                  }
                ]
            """;
    public static final String FOLHA_PAGAMENTO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "competencia": "09/2026",
                    "proventos": 10964.00,
                    "descontos": 2221.32,
                    "encargos": 3069.92,
                    "total": 8742.68
                  }
                ]
            """;
    public static final String TREINAMENTO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "nome": "NR-32 - Segurança em Saúde",
                    "cargaHoraria": 8,
                    "validadeMeses": 12,
                    "obrigatorio": true
                  }
                ]
            """;
    public static final String PARTICIPACAO_TREINAMENTO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "treinamentoNome": "NR-32 - Segurança em Saúde",
                    "dataConclusao": "2026-01-15",
                    "dataValidade": "2027-01-15",
                    "certificadoUrl": null
                  }
                ]
            """;
    public static final String EXAME_OCUPACIONAL_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "tipo": "PERIODICO",
                    "dataRealizacao": "2026-01-15",
                    "dataValidade": "2027-01-15",
                    "resultado": "APTO",
                    "asoUrl": null
                  }
                ]
            """;
    public static final String ACIDENTE_TRABALHO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "dataHora": "2026-01-15T10:30:00",
                    "descricao": "string",
                    "catEmitida": true,
                    "catUrl": null,
                    "diasAfastamento": 5
                  }
                ]
            """;
    public static final String EPI_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "tipo": "LUVAS",
                    "numeroCA": "12345",
                    "dataEntrega": "2026-01-15",
                    "dataDevolucao": null
                  }
                ]
            """;
    public static final String VAGA_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "unidadeUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "unidadeNome": "string",
                    "cargoUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "cargoNome": "Enfermeiro",
                    "cargoDescricao": "Atuação em plantões noturnos na UBS Central.",
                    "quantidade": 2,
                    "status": "ABERTA"
                  }
                ]
            """;
    public static final String CANDIDATO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "vagaId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "nome": "string",
                    "cpf": "string",
                    "curriculoUrl": null,
                    "status": "INSCRITO"
                  }
                ]
            """;
    public static final String CICLO_AVALIACAO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "nome": "2027-S1",
                    "dataInicio": "2027-01-01",
                    "dataFim": "2027-06-30"
                  }
                ]
            """;
    public static final String AVALIACAO_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "profissionalMatricula": "11111111111111-11",
                    "profissionalNome": "string",
                    "cicloNome": "2027-S1",
                    "avaliador": "string",
                    "nota": 8.5,
                    "observacao": null
                  }
                ]
            """;
    public static final String COMPOSICAO_REMUNERATORIA_RESPONSE_EXAMPLE = """
                {
                  "profissionalMatricula": "11111111111111-11",
                  "profissionalNome": "string",
                  "cargoNome": "Enfermeiro",
                  "categoriaNome": "Enfermagem",
                  "valorBase": 4000.00,
                  "anosCompletos": 3,
                  "percentualAnuenio": 1.00,
                  "valorAnuenio": 120.00,
                  "ajustesIndividuaisVigentes": [],
                  "totalAjustesIndividuais": 0.00,
                  "total": 4120.00
                }
            """;
    public static final String SETOR_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "unidadeUuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "unidadeNome": "string",
                    "nome": "Administração",
                    "codigo": "ADM",
                    "tipo": "ADMINISTRATIVO",
                    "ativo": true
                  }
                ]
            """;
    public static final String CAPACIDADE_ADMINISTRATIVA_RESPONSE_EXAMPLE = """
                [
                  {
                    "uuid": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
                    "codigo": "PATRIMONIO",
                    "nome": "Patrimônio",
                    "descricao": "Cadastro, transferência, inventário, manutenção, baixa e descarte de bens.",
                    "ativo": true
                  }
                ]
            """;
}
