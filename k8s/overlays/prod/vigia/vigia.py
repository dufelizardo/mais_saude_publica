"""Vigia dos ambientes (ADR-0209): avisa quando a API ou o banco de um ambiente fica fora.

Roda de 5 em 5 minutos (CronJob). Para cada namespace em VIGIA_NAMESPACES, lê os Deployments em VIGIA_DEPLOYMENTS pela API do
Kubernetes (com a conta de serviço do próprio pod) e considera fora o que está com a condição Available=False há mais de
VIGIA_LIMITE_MINUTOS. Com GITHUB_TOKEN (secret opcional alerta-github), abre uma issue com a etiqueta "alerta" para cada um fora
e, quando volta, comenta e fecha a issue. Sem o token, só escreve no log. Só a biblioteca padrão: nada para instalar.
"""
import datetime
import json
import os
import ssl
import sys
import urllib.error
import urllib.request

API = "https://kubernetes.default.svc"
CONTA = "/var/run/secrets/kubernetes.io/serviceaccount"
GITHUB = "https://api.github.com"
ETIQUETA = "alerta"


def kubernetes(caminho):
    token = open(f"{CONTA}/token").read().strip()
    contexto = ssl.create_default_context(cafile=f"{CONTA}/ca.crt")
    pedido = urllib.request.Request(API + caminho, headers={"Authorization": f"Bearer {token}"})
    with urllib.request.urlopen(pedido, context=contexto, timeout=15) as r:
        return json.load(r)


def github(metodo, caminho, corpo=None):
    pedido = urllib.request.Request(GITHUB + caminho, method=metodo, data=json.dumps(corpo).encode() if corpo else None, headers={
        "Authorization": f"Bearer {os.environ['GITHUB_TOKEN']}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "mais-saude-vigia",
    })
    with urllib.request.urlopen(pedido, timeout=15) as r:
        return json.load(r)


def fora_ha(deployment, agora):
    """Minutos desde que o Deployment ficou indisponível; None se está disponível."""
    for c in deployment.get("status", {}).get("conditions", []):
        if c.get("type") == "Available":
            if c.get("status") == "True":
                return None
            desde = datetime.datetime.fromisoformat(c["lastTransitionTime"].replace("Z", "+00:00"))
            return (agora - desde).total_seconds() / 60
    return None


def titulo(ns, nome):
    return f"[alerta] {nome} fora em {ns}"


def main():
    namespaces = os.environ.get("VIGIA_NAMESPACES", "").split()
    nomes = os.environ.get("VIGIA_DEPLOYMENTS", "app postgres").split()
    limite = float(os.environ.get("VIGIA_LIMITE_MINUTOS", "10"))
    repositorio = os.environ.get("GITHUB_REPOSITORIO", "")
    com_github = bool(os.environ.get("GITHUB_TOKEN")) and bool(repositorio)
    agora = datetime.datetime.now(datetime.timezone.utc)

    abertas = {}
    if com_github:
        for i in github("GET", f"/repos/{repositorio}/issues?state=open&labels={ETIQUETA}&per_page=100"):
            abertas[i["title"]] = i["number"]

    for ns in namespaces:
        for nome in nomes:
            try:
                minutos = fora_ha(kubernetes(f"/apis/apps/v1/namespaces/{ns}/deployments/{nome}"), agora)
            except urllib.error.HTTPError as e:
                print(f"{ns}/{nome}: não consegui ler ({e.code}).", flush=True)
                continue
            t = titulo(ns, nome)
            if minutos is not None and minutos >= limite:
                print(f"{ns}/{nome}: FORA há {minutos:.0f} min.", flush=True)
                if com_github and t not in abertas:
                    corpo = (f"O Deployment `{nome}` do namespace `{ns}` está indisponível há {minutos:.0f} minutos "
                             f"(condição Available=False desde {agora - datetime.timedelta(minutes=minutos):%d/%m %H:%M} UTC).\n\n"
                             "Diagnóstico no servidor:\n\n"
                             f"```bash\nsudo kubectl -n {ns} get pods\nsudo kubectl -n {ns} describe deploy {nome}\n"
                             f"sudo kubectl -n {ns} logs deploy/{nome} --tail=80\n```\n\n"
                             "Aberta pelo vigia dos ambientes (ADR-0209); ela é fechada sozinha quando o serviço voltar.")
                    n = github("POST", f"/repos/{repositorio}/issues", {"title": t, "body": corpo, "labels": [ETIQUETA, "bug"]})["number"]
                    print(f"{ns}/{nome}: issue #{n} aberta.", flush=True)
            else:
                estado = "ok" if minutos is None else f"indisponível há {minutos:.0f} min (abaixo do limite)"
                print(f"{ns}/{nome}: {estado}.", flush=True)
                if com_github and minutos is None and t in abertas:
                    n = abertas[t]
                    github("POST", f"/repos/{repositorio}/issues/{n}/comments",
                           {"body": f"Voltou: `{nome}` disponível em `{ns}` ({agora:%d/%m %H:%M} UTC)."})
                    github("PATCH", f"/repos/{repositorio}/issues/{n}", {"state": "closed", "state_reason": "completed"})
                    print(f"{ns}/{nome}: issue #{n} fechada.", flush=True)
    if not com_github:
        print("Sem GITHUB_TOKEN (secret alerta-github): só o log.", flush=True)


if __name__ == "__main__":
    try:
        main()
    except Exception as e:  # o vigia nunca deve quebrar em ciclo; o próximo agendamento tenta de novo
        print(f"Vigia falhou: {e}", file=sys.stderr, flush=True)
        sys.exit(1)
