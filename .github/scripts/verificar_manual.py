"""Confere o manual do usuário (ADR-0222): as duas línguas, a versão e os links.

Uso: python .github/scripts/verificar_manual.py   (na raiz do repositório)
Sai com 1 e lista os problemas quando algo não confere.
"""
import os
import re
import sys

RAIZ = os.path.normpath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
MANUAL = os.path.join(RAIZ, 'docs', 'manual')
LINGUAS = {'pt-BR': r'^> \*\*Versão:\*\* (\S+)', 'en': r'^> \*\*Version:\*\* (\S+)'}
SEMVER = re.compile(r'^(\d+)\.(\d+)\.(\d+)$')
LINK = re.compile(r'\[[^\]]*\]\(([^)\s]+)\)')


def versao_do_pom():
    texto = open(os.path.join(RAIZ, 'pom.xml'), encoding='utf-8').read()
    # A primeira <version> depois do </parent> é a do projeto.
    depois = texto.split('</parent>', 1)[-1]
    return re.search(r'<version>([^<]+)</version>', depois).group(1).strip()


def tupla(v):
    m = SEMVER.match(v)
    return tuple(int(x) for x in m.groups()) if m else None


def arquivos(lingua):
    pasta = os.path.join(MANUAL, lingua)
    if not os.path.isdir(pasta):
        return set()
    return {os.path.relpath(os.path.join(d, f), pasta).replace('\\', '/')
            for d, _, fs in os.walk(pasta) for f in fs if f.endswith('.md')}


def main():
    problemas = []
    pom = versao_do_pom()
    por_lingua = {l: arquivos(l) for l in LINGUAS}
    for l, fs in por_lingua.items():
        if not fs:
            problemas.append(f'docs/manual/{l}/ não tem nenhum arquivo')
    pt, en = por_lingua['pt-BR'], por_lingua['en']
    for f in sorted(pt - en):
        problemas.append(f'docs/manual/pt-BR/{f} não tem o espelho em docs/manual/en/{f}')
    for f in sorted(en - pt):
        problemas.append(f'docs/manual/en/{f} não tem o original em docs/manual/pt-BR/{f}')

    versoes = {}
    for l, padrao in LINGUAS.items():
        for f in sorted(por_lingua[l]):
            caminho = os.path.join(MANUAL, l, f)
            rel = f'docs/manual/{l}/{f}'
            texto = open(caminho, encoding='utf-8').read()
            linhas = texto.split('\n')
            if not linhas or not linhas[0].startswith('# '):
                problemas.append(f'{rel}: a primeira linha precisa ser o título (# ...)')
            m = next((re.match(padrao, x) for x in linhas[1:4] if re.match(padrao, x)), None)
            if not m:
                problemas.append(f'{rel}: falta a linha da versão logo depois do título')
            else:
                v = m.group(1)
                if not tupla(v):
                    problemas.append(f'{rel}: a versão "{v}" não é X.Y.Z')
                elif tupla(pom) and tupla(v) > tupla(pom):
                    problemas.append(f'{rel}: a versão {v} passa da versão do pom.xml ({pom})')
                versoes[(l, f)] = v
            for alvo in LINK.findall(texto):
                if re.match(r'^(https?:|mailto:|#)', alvo):
                    continue
                destino = os.path.normpath(os.path.join(os.path.dirname(caminho), alvo.split('#', 1)[0]))
                if not os.path.exists(destino):
                    problemas.append(f'{rel}: o link "{alvo}" aponta para um arquivo que não existe')
    for f in sorted(pt & en):
        a, b = versoes.get(('pt-BR', f)), versoes.get(('en', f))
        if a and b and a != b:
            problemas.append(f'{f}: a versão em pt-BR ({a}) difere da versão em en ({b})')

    if problemas:
        print('Manual do usuário com problemas (ADR-0222):')
        for p in problemas:
            print('  -', p)
        return 1
    print(f'Manual do usuário em ordem: {len(pt)} arquivo(s) em cada língua.')
    return 0


if __name__ == '__main__':
    sys.exit(main())
