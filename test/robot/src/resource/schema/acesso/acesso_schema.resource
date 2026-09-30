*** Comments ***
    Mais Saúde Pública API - Acesso response schemas (PermissaoResponseDto, PapelResponseDto,
    AtribuicaoAcessoResponseDto, UsuarioResponseDto). Papéis e escopo por unidade (ver ADR-0066).
*** Variables ***
${PERMISSAO_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'descricao', 'dimensao'], 'types': {'uuid': str, 'codigo': str, 'descricao': str, 'dimensao': str}, 'enums': {'dimensao': ['ADMINISTRACAO_DO_SISTEMA', 'OPERACAO', 'ACESSO_AO_DADO_DE_SAUDE']}}}}
${PAPEL_SCHEMA}    ${{{'required': ['uuid', 'codigo', 'nome', 'ativo', 'padrao', 'permissoes'], 'types': {'uuid': str, 'codigo': str, 'nome': str, 'descricao': str, 'ativo': bool, 'padrao': bool, 'permissoes': list}, 'enums': {}}}}
${ATRIBUICAO_ACESSO_SCHEMA}    ${{{'required': ['uuid', 'usuarioUuid', 'papelUuid', 'papelCodigo', 'concedidoEm', 'vigente'], 'types': {'uuid': str, 'usuarioUuid': str, 'usuarioNome': str, 'usuarioCpf': str, 'papelUuid': str, 'papelCodigo': str, 'papelNome': str, 'unidadeUuid': str, 'unidadeNome': str, 'inicio': str, 'fim': str, 'concedidoEm': str, 'concedidoPorCpf': str, 'revogadoEm': str, 'revogadoPorCpf': str, 'motivoRevogacao': str, 'vigente': bool}, 'enums': {}}}}
${USUARIO_SCHEMA}    ${{{'required': ['uuid', 'cpf', 'nome', 'ativo'], 'types': {'uuid': str, 'cpf': str, 'nome': str, 'ativo': bool, 'bloqueadoAte': str, 'ultimoAcessoEm': str}, 'enums': {}}}}
