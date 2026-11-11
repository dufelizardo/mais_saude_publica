package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.LocalDeslocamento;
import com.edufelizardo.maissaudepublica.models.enuns.MarcadorVulnerabilidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDeslocamento;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** As pessoas na emergência (E4a, ADR-0193): os vulneráveis da área afetada, os deslocados e a ocupação dos destinos. */
public final class PessoasEmergenciaDtos {

    private PessoasEmergenciaDtos() {
    }

    public record Vulneravel(UUID pacienteId, String nome, Integer idade, String telefone, String endereco, UUID microareaId, String microarea,
                             String acsNome, List<MarcadorVulnerabilidade> marcadores, List<String> programas, boolean deslocado,
                             String deslocadoPara) implements Serializable {
    }

    public record Vulneraveis(int pessoasNaArea, int vulneraveis, List<Vulneravel> lista) implements Serializable {
    }

    public record Deslocado(UUID uuid, UUID pacienteId, String pacienteNome, LocalDeslocamento origemTipo, String origem,
                            LocalDeslocamento destinoTipo, UUID destinoUnidadeId, UUID destinoInstituicaoId, String destino, Instant saidaEm,
                            String necessidades, String responsavel, SituacaoDeslocamento situacao, Instant fimEm, String motivoFim,
                            UUID anteriorId, UUID solicitacaoTransporteId, String solicitacaoTransporteSituacao,
                            List<EncaminhamentoIntersetorialDto> encaminhamentos) implements Serializable {
    }

    /** A ocupação de um destino: quantos deslocados estão lá agora. */
    public record Abrigo(LocalDeslocamento tipo, String destino, int pessoas) implements Serializable {
    }

    /** Para o prontuário: o paciente fora de casa por uma emergência. */
    public record DeslocamentoAberto(UUID uuid, UUID emergenciaId, String emergenciaNome, LocalDeslocamento destinoTipo, String destino,
                                     Instant saidaEm) implements Serializable {
    }
}
