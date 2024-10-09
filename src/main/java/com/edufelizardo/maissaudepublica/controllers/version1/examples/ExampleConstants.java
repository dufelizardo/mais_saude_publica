package com.edufelizardo.maissaudepublica.controllers.version1.examples;

public class ExampleConstants {

    public static final String HIERARQUICO_ZERO_RESPONSE_EXAMPLE = """
                [
                    {
                        "nome": "Ministério da Saúde",
                        "tipo": "0",
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
    public static final String HIERARQUICO_ZERO_RESPONSE_FIND_EXAMPLE = """
                {
                    "nome": "Ministério da Saúde",
                    "tipo": "0",
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
                    "details": "Nome: Gerencia de Saúde, Tipo: 0"
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
}
