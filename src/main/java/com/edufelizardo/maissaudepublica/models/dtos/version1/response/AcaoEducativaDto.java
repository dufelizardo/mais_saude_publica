package com.edufelizardo.maissaudepublica.models.dtos.version1.response;

import com.edufelizardo.maissaudepublica.models.enuns.FormatoMaterial;
import com.edufelizardo.maissaudepublica.models.enuns.NivelParticipacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** O detalhe educativo da ação coletiva (ADR-0195); vazio (sem tema) enquanto não preenchido. */
public record AcaoEducativaDto(UUID acaoId, SituacaoAcaoProgramatica situacao, String tema, UUID campanhaComunicacaoId, String campanhaComunicacaoNome,
                               List<EducacaoPermanenteDto.Conteudo> conteudos, List<Material> materiais, List<Microarea> microareas,
                               Integer publicoAlcancado, NivelParticipacao participacao, String avaliacao, Instant avaliadoEm) implements Serializable {

    public record Material(UUID materialId, String titulo, FormatoMaterial formato, int quantidade, boolean retirado) implements Serializable {
    }

    public record Microarea(UUID microareaId, String codigo, String areaNome) implements Serializable {
    }

    /** A ação educativa como a campanha de comunicação a mostra. */
    public record DaCampanha(UUID acaoId, String descricao, String local, String unidadeNome, LocalDate dataPrevista, LocalDate realizadaEm,
                             SituacaoAcaoProgramatica situacao, String tema, Integer publicoAlcancado, int materiaisDistribuidos,
                             NivelParticipacao participacao) implements Serializable {
    }
}
