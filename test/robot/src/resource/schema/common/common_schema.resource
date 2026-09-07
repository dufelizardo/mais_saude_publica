*** Comments ***
    Mais Saúde Pública API - shared response schemas, reused across all 4 hierarchy domains.
    SuccessResponseDto and ErrorExceptionResponse happen to share the same {message, details} shape,
    but are kept as two variables for semantic clarity at the call site (success vs. error body).
*** Variables ***
${SUCCESS_RESPONSE_SCHEMA}    ${{{'required': ['message', 'details'], 'types': {'message': str, 'details': str}, 'enums': {}}}}
${ERROR_RESPONSE_SCHEMA}    ${{{'required': ['message', 'details'], 'types': {'message': str, 'details': str}, 'enums': {}}}}
