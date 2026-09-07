*** Settings ***
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Comments ***
    Shared, reusable response-schema validation keywords. Same generic contract used across the LKDF reference
    examples (see https://github.com/dufelizardo/Layered-Keyword-Driven-Framework-LKDF) — domain-agnostic on
    purpose, so every *_schema.resource just supplies a {required, types, enums} dict.
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
*** Keywords ***
Validate Object Schema
    [Documentation]    Validates that ${data} is a JSON object satisfying ${schema}: every key listed in
    ...    schema['required'] is present, every key present in schema['types'] has the expected Python type,
    ...    and every key present in schema['enums'] has a value inside its allowed list.
    [Arguments]    ${data}    ${schema}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Should Be True    isinstance($data, dict)    msg=Response body is not a JSON object: ${data}

    ${errors}=    Evaluate
    ...    [m for m in (['missing required field: ' + f for f in $schema['required'] if f not in $data] + ['field ' + f + ' expected type ' + t.__name__ + ' but got ' + type($data.get(f)).__name__ for f, t in $schema['types'].items() if f in $data and $data.get(f) is not None and not isinstance($data[f], t)] + ['field ' + f + ' value ' + repr($data[f]) + ' not in allowed values ' + repr(v) for f, v in $schema['enums'].items() if f in $data and $data[f] not in v])]

    Should Be Empty    ${errors}    msg=Schema validation failed: ${errors}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════

Validate Array Schema
    [Documentation]    Validates that ${data} is a JSON array and, when non-empty, that every item satisfies
    ...    ${item_schema}.
    [Arguments]    ${data}    ${item_schema}
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
    Should Be True    isinstance($data, list)    msg=Response body is not a JSON array: ${data}

    FOR    ${item}    IN    @{data}
        Validate Object Schema    ${item}    ${item_schema}
    END
    # ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════
# ══════════════════════════════════════════════════════════════════════════════════════════════════════════════════════
