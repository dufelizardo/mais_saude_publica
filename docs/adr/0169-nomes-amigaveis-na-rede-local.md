# 0169 — Nomes amigáveis na rede local (mDNS)

## Status

Aceita e implementada. Complementa a [ADR-0012](./0012-infraestrutura-local-k3s-homelab.md) e a
[ADR-0016](./0016-deploy-do-frontend-angular-no-ambiente-dev.md).

## Contexto

- **Os nomes `*.mais-saude.local` só funcionam onde há entrada no arquivo `hosts`.** Celular e outros computadores da casa não
  abrem o sistema, e pelo IP o Ingress responde 404.
- **O modem não ajuda:** a senha de administração não está disponível, e ele é da operadora.
- **Nome público com o IP (nip.io)** funcionaria, mas é ruim de digitar e depende de serviço externo.
- **Não há SSH com chave nem `kubectl` a partir da máquina de desenvolvimento.** A mudança precisa chegar pelo GitOps.

## Decisão

1. **Um nome simples por ambiente, anunciado por mDNS** (RFC 6762), que Windows, iPhone, Mac e Android recentes resolvem sozinhos:

   | Ambiente | Endereço |
   |---|---|
   | Desenvolvimento | `http://maissaudepublica-dev.local` |
   | QA | `http://maissaudepublica-qaa.local` |
   | Homologação | `http://maissaudepublica-cert.local` |
   | Produção | `http://maissaudepublica.local` |

   O mDNS só aceita um nome antes do `.local`, por isso os nomes antigos, com dois níveis, não servem. O nome da homologação usa
   `cert`, por escolha do dono do produto; o ambiente continua sendo `homologacao` no restante do projeto.
2. **Anunciador dentro do cluster:** o Deployment `mdns` (`k8s/base/deployment-mdns.yaml`), um por ambiente, responde pelo nome do
   `mdns-config` do overlay com o IP do Ingress (`192.168.0.200`).
   - Usa `hostNetwork`, porque o mDNS só circula na rede do servidor.
   - O script `k8s/base/mdns/anunciador.py` usa só a biblioteca padrão do Python: sem Avahi nem instalação no servidor, e vários
     ambientes dividem a porta 5353 com `SO_REUSEPORT`.
   - Sem privilégio: usuário sem permissão, sistema de arquivos só de leitura, sem capacidades e sem token do Kubernetes.
3. **Os nomes antigos continuam valendo.** Cada `frontend-ingress.yaml` ganha uma regra a mais.
4. **Nenhuma mudança na aplicação:** o front chama a API pelo mesmo endereço, pelo proxy do nginx, e não há restrição de CORS.

## Consequências

- **Qualquer aparelho no Wi-Fi abre o sistema pelo nome,** sem mexer no aparelho nem no modem.
- **Só na rede de casa:** o IP é interno. Acesso de fora pede túnel ou domínio próprio.
- **Abrir o nome não é o mesmo que ter acesso:** `qaa`, `homologacao` e `prod` seguem sem login (`PENDENCIAS.md`, seção 1), então quem
  está no Wi-Fi entra e faz tudo. Aceitável enquanto os dados forem fictícios; com dado real, o login vem antes.
- **Limites conhecidos:** Android antigo pode não resolver `.local`, e alguns roteadores bloqueiam multicast entre aparelhos. O `hosts`
  segue como alternativa.
- **Só responde IPv4:** a pergunta por IPv6 fica sem resposta, e o aparelho usa o IPv4.

## Testes

- `kubectl kustomize` dos quatro overlays, com o nome de cada ambiente no Ingress e no `mdns-config`.
- O script testado fora do repositório: pergunta pelo nome, por outro nome e por IPv6; nome em maiúsculas e comprimido; pacotes
  malformados; resposta de outro aparelho ignorada.
- **Na rede real:** com o script rodando na máquina de desenvolvimento, o Windows resolveu um nome de teste para `192.168.0.200` pela
  própria consulta do sistema, sem entrada no `hosts`.
- Sem teste Robot: não há endpoint nem tela novos. O funcionamento no cluster se confere abrindo o endereço em outro aparelho.
