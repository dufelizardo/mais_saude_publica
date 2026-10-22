package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.CompartilhamentoCuidado;
import com.edufelizardo.maissaudepublica.models.ReferenciaSaudeMental;
import com.edufelizardo.maissaudepublica.models.SolicitacaoRegulacao;
import com.edufelizardo.maissaudepublica.models.TransferenciaCuidado;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CompartilhamentoCuidadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EncaminhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TransferenciaCuidadoDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaCuidado;
import com.edufelizardo.maissaudepublica.repositories.CompartilhamentoCuidadoRepository;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroEquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ReferenciaSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.TransferenciaCuidadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Leitura do que a SM4 pendura no caso (ADR-0122): encaminhamentos pela Regulação, cuidado compartilhado e
 * transferências. Só repositórios, para o {@link SaudeMentalService} montar o detalhe e o sigilo sem depender das
 * escritas, que ficam no {@link RedeSaudeMentalService}.
 */
@Component
public class RedeLeitura {

    @Autowired
    private EncaminhamentoSaudeMentalRepository encaminhamentoRepository;

    @Autowired
    private CompartilhamentoCuidadoRepository compartilhamentoRepository;

    @Autowired
    private TransferenciaCuidadoRepository transferenciaRepository;

    @Autowired
    private ReferenciaSaudeMentalRepository referenciaRepository;

    @Autowired
    private MembroEquipeRepository membroRepository;

    public List<EncaminhamentoSaudeMentalDto> encaminhamentos(UUID caso) {
        return encaminhamentoRepository.findDoAcompanhamento(caso).stream().map(e -> {
            SolicitacaoRegulacao s = e.getSolicitacao();
            return new EncaminhamentoSaudeMentalDto(e.getUuid(), s.getUuid(), s.getProcedimento().getNome(), s.getStatus(), s.getPrioridade(),
                    s.getSolicitadoEm(), s.getUnidadeExecutante() != null ? s.getUnidadeExecutante().getNome() : null, s.getDataHoraPrevista(),
                    s.getContrarreferencia());
        }).toList();
    }

    public List<CompartilhamentoCuidadoDto> compartilhamentos(UUID caso) {
        return compartilhamentoRepository.findDoAcompanhamento(caso).stream().map(c -> new CompartilhamentoCuidadoDto(c.getUuid(),
                c.getEquipe().getUuid(), c.getEquipe().getNome(), c.getEquipe().getUnidade().getNome(), c.getObjetivo(), c.getInicio(),
                c.getFim(), c.getMotivoFim())).toList();
    }

    public List<TransferenciaCuidadoDto> transferencias(UUID caso) {
        return transferenciaRepository.findDoAcompanhamento(caso).stream().map(RedeLeitura::dto).toList();
    }

    public TransferenciaCuidado transferenciaPendente(UUID caso) {
        return transferenciaRepository.findDoAcompanhamento(caso).stream()
                .filter(t -> t.getSituacao() == SituacaoTransferenciaCuidado.PROPOSTA).findFirst().orElse(null);
    }

    /** O usuário é membro de uma equipe com compartilhamento vigente no caso. */
    public boolean parceiro(UUID caso, String cpf) {
        List<UUID> equipes = compartilhamentoRepository.findDoAcompanhamento(caso).stream().filter(c -> c.getFim() == null)
                .map(c -> c.getEquipe().getUuid()).toList();
        return !equipes.isEmpty() && membroRepository.findByEquipe_UuidInAndFimIsNull(equipes).stream()
                .anyMatch(m -> cpf.equals(m.getProfissional().getCpf()));
    }

    /** O usuário é a referência do caso aberto por transferência a partir deste: o caso anterior fica legível para ela. */
    public boolean referenciaDoCasoSeguinte(UUID caso, String cpf) {
        return transferenciaRepository.findDoAcompanhamento(caso).stream()
                .filter(t -> t.getSituacao() == SituacaoTransferenciaCuidado.ACEITA && t.getCasoDestino() != null)
                .anyMatch(t -> {
                    AcompanhamentoSaudeMental destino = t.getCasoDestino();
                    return referenciaRepository.findDosAcompanhamentos(List.of(destino.getUuid())).stream()
                            .filter(ReferenciaSaudeMental::vigente)
                            .anyMatch(r -> cpf.equals(r.getTecnico().getCpf())
                                    || membroRepository.findByEquipe_UuidInAndFimIsNull(List.of(r.getEquipe().getUuid())).stream()
                                    .anyMatch(m -> cpf.equals(m.getProfissional().getCpf())));
                });
    }

    /** Compartilhamento vigente com a equipe no caso. */
    public CompartilhamentoCuidado compartilhamentoVigente(UUID caso, UUID equipe) {
        return compartilhamentoRepository.findDoAcompanhamento(caso).stream()
                .filter(c -> c.getFim() == null && c.getEquipe().getUuid().equals(equipe)).findFirst().orElse(null);
    }

    static TransferenciaCuidadoDto dto(TransferenciaCuidado t) {
        AcompanhamentoSaudeMental c = t.getAcompanhamento();
        return new TransferenciaCuidadoDto(t.getUuid(), c.getUuid(), c.getPaciente().getUuid(), c.getPaciente().getNome(),
                c.getUnidade().getUuid(), c.getUnidade().getNome(), t.getUnidadeDestino().getUuid(), t.getUnidadeDestino().getNome(),
                t.getResumo(), t.getSituacao(), t.getPropostaEm(), t.getDecididaEm(), t.getMotivoDecisao(),
                t.getCasoDestino() != null ? t.getCasoDestino().getUuid() : null);
    }
}
