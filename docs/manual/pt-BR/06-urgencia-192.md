# Urgência 192

> **Versão:** 1.15.0 · [English](../en/06-urgencia-192.md)

Menu: **Assistência › Urgência 192**. É a central do 192 (Regulação de urgência): os chamados, a regulação médica, o envio da
viatura, o destino (inclusive com vaga zero), a ficha do atendimento pré-hospitalar (APH) e os tempos de resposta.

**Quem usa:**

| Perfil | O que faz |
|--------|-----------|
| Atendente de regulação (TARM) | recebe o chamado e abre a ocorrência |
| Médico regulador de urgência | classifica a gravidade, decide a conduta e o destino |
| Equipe de intervenção do SAMU | preenche a ficha do APH na cena |
| Unidade de destino (recepção, médico, enfermagem) | recebe o paciente |
| Gestor | acompanha |

> Para a central funcionar, uma unidade precisa ter a capacidade **Regulação médica de urgência** no Modelo operacional
> (Administrativo › Modelo operacional). Sem ela, a tela avisa.

**Os cartões do alto:**

- **Aguardando regulação** (sem gravidade ainda);
- **Vermelho · amarelo** abertas;
- **Recurso em campo;**
- o tempo médio **Até a regulação**.

O filtro **Todas as centrais** escolhe a central.

## 1. Abrir uma ocorrência (TARM)

1. Clique em **Novo chamado**.
2. Escolha a **Central** e a **Origem**:
   - **Chamado 192;**
   - **Pedido de unidade:** escolha a **Unidade que pede**;
   - **Crise em saúde mental.**
3. Informe o telefone de retorno e descreva a **Queixa**: o que está acontecendo e desde quando.
4. **Paciente:**
   - informe o **CPF do paciente**, se souber;
   - se não, marque **Sem identificação** e descreva a pessoa (sexo e idade aproximada).
5. Escolha o **Tipo de local** (domicílio, via pública, local de evento, escola, ponto da unidade ou outro) e informe o
   **Endereço** e o **Ponto de referência**.
6. Salve (*Ocorrência aberta*). Ela entra em **Ocorrências abertas** como *Aguardando regulação*.

## 2. O painel de ocorrências

As **Ocorrências abertas** vêm primeiro as que aguardam regulação, depois por gravidade e pela hora do chamado. Cada uma mostra:

- a gravidade, o protocolo e a situação;
- a queixa e o local.

Quem não tem acesso ao dado clínico vê a linha com *sem acesso ao dado clínico*.

**As situações:** aguardando regulação, regulada, recurso em campo, concluída ou cancelada.

Ao abrir uma ocorrência, o painel mostra:

- a queixa, quem chamou, o local e a unidade que pediu;
- a decisão, a conduta, o destino e a vaga zero;
- o desfecho e a **Ficha do APH**;
- os tempos **Até o envio**, **Envio até o local** e **Total**;
- a **Linha do tempo**: chamado recebido, regulação, recurso enviado, chegada ao local, saída, chegada ao destino, recebido no
  destino, encerramento.

Ao lado ficam:

- as **Viaturas disponíveis** do SAMU, com plantão agora e sem ocorrência aberta;
- os **Destinos de urgência:** as unidades com o perfil Urgência, com os leitos livres e as ocorrências a caminho.

## 3. Regular (médico regulador)

1. Na ocorrência, clique em **Regular**.
2. Classifique a **Gravidade**: vermelho, amarelo, verde ou azul.
3. Escolha a **Decisão** e descreva a **Conduta**:

   | Decisão | O que pede |
   |---------|------------|
   | Orientação por telefone | só a conduta |
   | Envio de recurso | a viatura da frota ou o tipo de recurso (USA, USB, motolância, aeromédico ou outro); o destino pode vir depois |
   | Encaminhamento (meios próprios) | o destino |
   | Transferência entre unidades | o destino |

4. Em **Viatura da frota**, escolha uma das disponíveis com plantão. Sem viatura cadastrada, registre o tipo e um texto (por
   exemplo, *USA-01*); a viatura pode ser definida com a equipe no local.
5. Escolha o **Destino** (obrigatório no encaminhamento e na transferência). **Sem leito livre agora:** para mandar assim mesmo, marque **vaga zero** (encaminhar sem vaga
   confirmada) e escreva a **Justificativa da vaga zero**.
6. Informe a **Matrícula do médico regulador** e confirme: *Ocorrência regulada* ou *Orientação registrada*.

**Reclassificar:** escolha a **Nova gravidade** e o **Motivo** (*Gravidade reclassificada*).

**Definir destino** ou **Trocar destino:** depois de regulada, e até a chegada ao destino, o regulador escolhe ou troca a
unidade de destino, também com a vaga zero quando não há leito.

## 4. Atendimento na cena: a ficha do APH (equipe do SAMU)

A seção **Atendimento na cena** lista as ocorrências com recurso enviado.

1. Clique na ocorrência para abrir a ficha. Informe a **sua matrícula** no topo.
2. Preencha:
   - a **Cena:** a natureza (clínica, trauma, obstétrica, psiquiátrica, pediátrica…), o **Mecanismo do trauma** (queda,
     colisão, moto, atropelamento, arma de fogo, arma branca, agressão, queimadura, afogamento…) e a situação encontrada;
   - a **Avaliação primária (XABCDE):**
     - X, hemorragia exsanguinante;
     - A, via aérea;
     - B, respiração;
     - C, circulação;
     - D, o Glasgow ocular (1–4), verbal (1–5) e motor (1–6), com o total calculado, e as pupilas;
     - E, exposição;
   - os **Procedimentos:**
     - oxigênio, intubação, acesso venoso, colar cervical, prancha e imobilização, tala;
     - curativo ou compressão, torniquete, RCP, desfibrilação, monitorização, parto;
     - ou outro, descrito;
   - a **Impressão diagnóstica** e o **CID-10**, e o **Desfecho na cena** (com quem recusou, a hora do óbito ou quem recebeu no
     destino e a hora da entrega, conforme o caso).
3. Salve a ficha (*Ficha salva*). Depois de salva, registre:
   - os **Sinais vitais** (PA sistólica e diastólica, FC, SpO2, temperatura, glicemia e dor de 0 a 10), quantas vezes for
     preciso;
   - os **Medicamentos** (medicamento, dose e via).
4. Ao fim, clique em **Finalizar** (*Ficha finalizada*). **Finalizada, a ficha só aceita adendo:** use **Novo adendo**.

**PDF:** o botão **Ficha do APH em PDF** gera a ficha para imprimir ou anexar.

## 5. Receber no destino, identificar e encerrar

- **Receber no destino** (a unidade que recebe): registra a chegada do paciente (*Paciente recebido*).
- **Identificar paciente:** quando o paciente chegou sem identificação, informe o CPF com 11 dígitos para ligar a ocorrência ao
  cadastro (*Paciente identificado*).
- **Encerrar** (central):
  1. escolha o **Desfecho**: orientado por telefone, atendido no local, recebido no destino, óbito no local, recusa, sem
     ocorrência (engano ou trote) ou desistência;
  2. descreva o encerramento.

  O recebimento no destino é registrado pela unidade que recebe, e a orientação por telefone, na regulação.
- **Cancelar** (central): com o motivo (por exemplo, *chamado duplicado*).

## 6. Mensagens comuns

| Mensagem | O que quer dizer | O que fazer |
|----------|------------------|-------------|
| *Informe o telefone de retorno.* | o chamado está sem telefone | anote o número de quem ligou |
| *Descreva a queixa.* | o chamado está sem a queixa | descreva o que está acontecendo |
| *Nenhum paciente com esse CPF; deixe em branco e descreva a pessoa.* | o CPF não está cadastrado | marque sem identificação e descreva |
| *Classifique a gravidade.* / *Descreva a conduta.* | a regulação está incompleta | escolha a cor e descreva a conduta |
| *Escolha a viatura ou o tipo de recurso.* | o envio de recurso está sem recurso | escolha a viatura ou o tipo |
| *Escolha o destino.* | o encaminhamento ou a transferência está sem destino | escolha a unidade de destino |
| *Justifique a vaga zero.* | a vaga zero precisa do porquê | escreva a justificativa |
| *Informe a sua matrícula no topo da ficha.* | a ficha do APH está sem o profissional | informe a sua matrícula |
| *Descreva o outro procedimento.* | o procedimento *Outro* está sem descrição | descreva o que foi feito |
| *Escolha o desfecho.* / *Descreva o encerramento.* | o encerramento está incompleto | escolha o desfecho e descreva |
| *Informe o CPF com 11 dígitos.* | o CPF da identificação está incompleto | confira os números |
