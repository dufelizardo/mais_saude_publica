"""Gera CPF com dígito verificador válido, para criar usuários nos testes (a API valida o CPF do usuário)."""
import random


def _digito(numeros):
    soma = sum(n * p for n, p in zip(numeros, range(len(numeros) + 1, 1, -1)))
    resto = soma % 11
    return 0 if resto < 2 else 11 - resto


def gerar_cpf_valido():
    """Devolve um CPF de 11 dígitos, sem pontuação, com os dígitos verificadores corretos."""
    while True:
        base = [random.randint(0, 9) for _ in range(9)]
        if len(set(base)) > 1:
            break
    base.append(_digito(base))
    base.append(_digito(base))
    return ''.join(map(str, base))
