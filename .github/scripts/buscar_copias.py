"""Busca de cópias pelos marcadores (ADR-0223).

Procura cada marcador na busca de código do GitHub e lista o que aparece fora deste repositório. Os marcadores vêm da variável
CANARIOS (uma linha por marcador, no formato "marcador=onde está"), que fica nas variáveis do repositório e não no código.

Uso: GH_TOKEN=... CANARIOS="..." GITHUB_REPOSITORY=dono/repo python .github/scripts/buscar_copias.py [--issue]
Com --issue, abre (ou comenta) a issue com o rótulo "copia-detectada" quando acha algo. Sai com 0 sempre que a busca rodou.
"""
import json
import os
import subprocess
import sys
import time

ROTULO = 'copia-detectada'


def gh(*args, entrada=None):
    r = subprocess.run(['gh', *args], capture_output=True, text=True, encoding='utf-8', input=entrada)
    if r.returncode != 0:
        raise RuntimeError(f'gh {" ".join(args[:3])}…: {r.stderr.strip()}')
    return r.stdout


def marcadores():
    linhas = [x.strip() for x in os.environ.get('CANARIOS', '').splitlines() if x.strip() and not x.strip().startswith('#')]
    pares = []
    for x in linhas:
        marcador, _, onde = x.partition('=')
        pares.append((marcador.strip(), onde.strip() or 'sem descrição'))
    return pares


def buscar(marcador):
    saida = gh('api', '-X', 'GET', 'search/code', '-f', f'q="{marcador}"', '-f', 'per_page=100')
    return json.loads(saida).get('items', [])


def main():
    repo = os.environ['GITHUB_REPOSITORY'].lower()
    pares = marcadores()
    if not pares:
        print('A variável CANARIOS está vazia: nada a buscar.')
        return 0
    achados = []
    for marcador, onde in pares:
        for item in buscar(marcador):
            nome = item['repository']['full_name']
            if nome.lower() != repo:
                achados.append((marcador, onde, nome, item['path'], item['html_url']))
        time.sleep(7)  # a busca de código aceita 10 consultas por minuto
    linhas = [f'- `{m}` ({o}) em **{n}**: [{p}]({u})' for m, o, n, p, u in achados]
    resumo = (f'Foram buscados {len(pares)} marcadores. ' +
              (f'Achados fora de `{repo}`:\n\n' + '\n'.join(linhas) if achados else 'Nenhum achado fora deste repositório.'))
    print(resumo)
    if os.environ.get('GITHUB_STEP_SUMMARY'):
        with open(os.environ['GITHUB_STEP_SUMMARY'], 'a', encoding='utf-8') as f:
            f.write('## Busca de cópias\n\n' + resumo + '\n')
    if achados and '--issue' in sys.argv:
        corpo = ('A busca semanal achou marcadores deste projeto fora dele (ADR-0223). Confira se é cópia, fork declarado ou uso '
                 'permitido pela licença.\n\n' + '\n'.join(linhas))
        abertas = json.loads(gh('issue', 'list', '--label', ROTULO, '--state', 'open', '--json', 'number'))
        if abertas:
            gh('issue', 'comment', str(abertas[0]['number']), '--body', corpo)
        else:
            gh('label', 'create', ROTULO, '--color', 'B60205', '--description', 'Marcador do projeto achado fora dele', '--force')
            gh('issue', 'create', '--title', 'Possível cópia do projeto detectada', '--label', ROTULO, '--body', corpo)
    return 0


if __name__ == '__main__':
    sys.exit(main())
