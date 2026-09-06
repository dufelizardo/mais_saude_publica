# Contribuindo para Mais Saúde Pública

Obrigado por considerar contribuir para o projeto **Mais Saúde Pública**! Este documento descreve o processo de contribuição e as melhores práticas para garantir que todas as colaborações sejam feitas de forma eficiente e harmoniosa.

## Sumário
1. [Como Posso Contribuir?](#como-posso-contribuir)
2. [Requisitos](#requisitos)
3. [Fluxo de Trabalho](#fluxo-de-trabalho)
4. [Estilo de Código](#estilo-de-código)
5. [Testes](#testes)
6. [Envio de Pull Requests](#envio-de-pull-requests)
7. [Reporte de Issues](#reporte-de-issues)
8. [Código de Conduta](#código-de-conduta)

## Como Posso Contribuir?

Existem várias maneiras de contribuir para este projeto:
- **Reportando Bugs:** Se você encontrar algum problema, abra uma [Issue](#reporte-de-issues) com uma descrição detalhada.
- **Sugerindo Funcionalidades:** Se tiver uma ideia para uma nova funcionalidade, compartilhe abrindo uma Issue.
- **Corrigindo Bugs:** Veja as Issues abertas e envie um Pull Request (PR) com uma correção.
- **Melhorando a Documentação:** Qualquer ajuda na documentação é bem-vinda! Revise, corrija ou amplie a documentação.
- **Desenvolvendo Novas Funcionalidades:** Siga o processo descrito abaixo para implementar novas funcionalidades.

## Requisitos

Antes de começar, certifique-se de que você tem:
- [Git](https://git-scm.com/) instalado.
- O ambiente de desenvolvimento configurado conforme descrito no [README.md](./README.md).
- Familiaridade com a linguagem e tecnologias utilizadas no projeto.

## Fluxo de Trabalho

1. **Fork o Repositório:**
   - Clique em "Fork" no GitHub para criar uma cópia do repositório na sua conta.

2. **Clone o Repositório:**
   - Clone o fork para sua máquina local.
   ```bash
   git clone https://github.com/seu-usuario/mais-saude-publica.git
   cd mais-saude-publica
   ```

3. **Crie uma Branch:**
   - Crie uma nova branch para a sua contribuição.
   ```bash
   git checkout -b minha-contribuicao
   ```

4. **Faça Suas Alterações:**
   - Realize as mudanças desejadas no código ou na documentação.

5. **Commit:**
   - Escreva uma mensagem de commit clara e descritiva.
   ```bash
   git add .
   git commit -m "Descrição clara da contribuição"
   ```

6. **Envie para o GitHub:**
   - Envie as alterações para o seu fork no GitHub.
   ```bash
   git push origin minha-contribuicao
   ```

7. **Abra um Pull Request:**
   - No GitHub, vá até a página do repositório original e clique em "Compare & pull request".
   - Descreva suas alterações e abra o PR.

## Estilo de Código

Para manter a consistência no código, siga as seguintes diretrizes:
- **Indentação:** Use 4 espaços para indentação.
- **Nomeação:** Use nomes de variáveis e funções descritivos e em inglês.
- **Comentários:** Comente partes do código que não são autoexplicativas.

Recomendamos o uso de linters e formatadores para garantir a qualidade do código.

## Testes

Todos os novos recursos ou correções devem incluir testes automatizados. 
- **Executando Testes Locais:**
   - Execute os testes localmente antes de enviar o PR.
   ```bash
   pytest
   ```
- **Cobertura de Testes:** Assegure-se de que a cobertura de código não diminua.

## Envio de Pull Requests

Ao enviar um Pull Request:
- Garanta que o PR está associado a uma Issue, se aplicável.
- Descreva claramente a motivação e o que foi alterado.
- Mantenha o PR pequeno e focado em um único objetivo.

## Reporte de Issues

Encontrou um bug? Abra uma Issue!
- **Título:** Use um título descritivo.
- **Descrição:** Inclua detalhes como o que aconteceu, o esperado, e como reproduzir o problema.
- **Capturas de Tela/Logs:** Inclua capturas de tela ou logs, se aplicável.

## Código de Conduta

Por favor, siga nosso [Código de Conduta](./CODE_OF_CONDUCT.md) em todas as interações. Respeito e inclusão são fundamentais.

---

Agradecemos por contribuir para o **Mais Saúde Pública**!

### Dicas:
- **Seja específico e claro**: Quanto mais claro for o `CONTRIBUTING.md`, mais fácil será para outros entenderem como contribuir.
- **Mantenha atualizado**: Se o projeto evoluir, não se esqueça de atualizar o `CONTRIBUTING.md` para refletir as novas práticas ou ferramentas.
- **Incentive feedback**: Deixe claro que feedbacks sobre o processo de contribuição também são bem-vindos, isso ajuda a melhorar a colaboração.

Se precisar de mais detalhes ou ajustes, é só avisar!
