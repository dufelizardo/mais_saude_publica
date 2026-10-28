package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncaminhamentoIntersetorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetornoEncaminhamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Saúde Mental, fatia SM6 (ADR-0155): o caso encaminhado a instituições de outros setores (CRAS, CREAS, escola, Conselho
 * Tutelar...), pelo encaminhamento intersetorial da IS1 (ADR-0154). Escrever é de quem escreve no caso: a permissão na
 * unidade, o caso vigente e o conteúdo liberado.
 */
@Service
public class IntersetorialSaudeMentalService {

    private static final DominioOrigemEncaminhamento ORIGEM = DominioOrigemEncaminhamento.SAUDE_MENTAL;

    @Autowired
    private SaudeMentalService saudeMentalService;

    @Autowired
    private EncaminhamentoIntersetorialService encaminhamentoService;

    @Transactional
    public AcompanhamentoSaudeMentalDto encaminhar(UUID casoId, EncaminhamentoIntersetorialRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        EncaminhamentoIntersetorial e = encaminhamentoService.encaminhar(ORIGEM, caso.getUuid(), caso.getPaciente(), caso.getUnidade(),
                saudeMentalService.profissionalDoUsuario(), dto);
        ContextoAuditoria.registro(caso.getUuid());
        ContextoAuditoria.detalhe("Encaminhamento intersetorial " + e.getUuid() + " a " + e.getInstituicao().getNome() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    @Transactional
    public AcompanhamentoSaudeMentalDto receber(UUID casoId, UUID encaminhamentoId, RetornoEncaminhamentoRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        encaminhamentoService.receber(encaminhamentoService.daOrigem(encaminhamentoId, ORIGEM, caso.getUuid()), dto);
        return saudeMentalService.detalheLiberado(caso);
    }

    @Transactional
    public AcompanhamentoSaudeMentalDto retorno(UUID casoId, UUID encaminhamentoId, RetornoEncaminhamentoRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        encaminhamentoService.registrarRetorno(encaminhamentoService.daOrigem(encaminhamentoId, ORIGEM, caso.getUuid()), dto);
        return saudeMentalService.detalheLiberado(caso);
    }

    @Transactional
    public AcompanhamentoSaudeMentalDto cancelar(UUID casoId, UUID encaminhamentoId, MotivoSaudeMentalRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        encaminhamentoService.cancelar(encaminhamentoService.daOrigem(encaminhamentoId, ORIGEM, caso.getUuid()), dto.getMotivo());
        return saudeMentalService.detalheLiberado(caso);
    }
}
