package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.ComunicacaoMinisterioPublico;
import com.edufelizardo.maissaudepublica.models.EncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.models.EventoCrise;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.ProjetoTerapeuticoSingular;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.enuns.DominioOrigemEncaminhamento;
import com.edufelizardo.maissaudepublica.models.enuns.EtapaCriseSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.MomentoComunicacaoMp;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEncaminhamentoIntersetorial;
import com.edufelizardo.maissaudepublica.repositories.AcompanhamentoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.ComunicacaoMinisterioPublicoRepository;
import com.edufelizardo.maissaudepublica.repositories.EncaminhamentoIntersetorialRepository;
import com.edufelizardo.maissaudepublica.repositories.EtapaCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoCriseRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProjetoTerapeuticoSingularRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A apuração dos indicadores do programa Saúde Mental (ADR-0155), dentro do conjunto fixo da P6 (ADR-0141): cada método
 * devolve {numerador, denominador}, na unidade (nula = a rede). Só números; quem chama confere o programa sensível.
 */
@Component
class IndicadoresSaudeMental {

    private static final ZoneId FUSO = ZoneId.systemDefault();

    @Autowired private AcompanhamentoSaudeMentalRepository casoRepository;
    @Autowired private ProjetoTerapeuticoSingularRepository ptsRepository;
    @Autowired private AvaliacaoSaudeMentalRepository avaliacaoRepository;
    @Autowired private EventoCriseRepository criseRepository;
    @Autowired private EtapaCriseRepository etapaRepository;
    @Autowired private InternacaoRepository internacaoRepository;
    @Autowired private ComunicacaoMinisterioPublicoRepository comunicacaoRepository;
    @Autowired private EncaminhamentoIntersetorialRepository encaminhamentoRepository;

    /** Casos abertos no fim do período com o PTS que valia naquele dia ainda dentro da data de revisão. */
    long[] ptsEmDia(UnidadeDeSaude u, LocalDate ate) {
        List<AcompanhamentoSaudeMental> casos = casoRepository.findAbertosEm(ate).stream().filter(c -> daUnidade(c.getUnidade(), u)).toList();
        if (casos.isEmpty()) {
            return new long[]{0, 0};
        }
        Instant limite = inicioDo(ate.plusDays(1));
        Map<UUID, ProjetoTerapeuticoSingular> vigente = new HashMap<>();
        for (ProjetoTerapeuticoSingular p : ptsRepository.findDosAcompanhamentos(casos.stream().map(AcompanhamentoSaudeMental::getUuid).toList())) {
            if (p.getElaboradoEm().isBefore(limite) && (p.getSubstituidoEm() == null || !p.getSubstituidoEm().isBefore(limite))) {
                vigente.put(p.getAcompanhamento().getUuid(), p);
            }
        }
        long emDia = casos.stream().map(c -> vigente.get(c.getUuid())).filter(p -> p != null && !p.getRevisarEm().isBefore(ate)).count();
        return new long[]{emDia, casos.size()};
    }

    /**
     * Casos abertos no período com a primeira avaliação em até 30 dias do início. Os de menos de 30 dias ainda sem
     * avaliação não entram: o prazo deles não venceu.
     */
    long[] avaliacaoEm30Dias(UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        List<AcompanhamentoSaudeMental> casos = casoRepository.findIniciadosNoIntervalo(de, ate).stream().filter(c -> daUnidade(c.getUnidade(), u)).toList();
        if (casos.isEmpty()) {
            return new long[]{0, 0};
        }
        Map<UUID, Instant> primeira = new HashMap<>();
        for (Object[] l : avaliacaoRepository.findPrimeiras(casos.stream().map(AcompanhamentoSaudeMental::getUuid).toList())) {
            primeira.put((UUID) l[0], (Instant) l[1]);
        }
        LocalDate hoje = LocalDate.now();
        long num = 0;
        long den = 0;
        for (AcompanhamentoSaudeMental c : casos) {
            Instant prazo = inicioDo(c.getInicio().plusDays(31));
            Instant p = primeira.get(c.getUuid());
            boolean noPrazo = p != null && p.isBefore(prazo);
            if (noPrazo || !c.getInicio().plusDays(30).isAfter(hoje)) {
                den++;
                num += noPrazo ? 1 : 0;
            }
        }
        return new long[]{num, den};
    }

    /**
     * Crises identificadas no período com o acompanhamento pós-crise registrado em até 7 dias. As de menos de 7 dias
     * ainda sem pós-crise não entram.
     */
    long[] posCriseEm7Dias(UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        List<EventoCrise> crises = criseRepository.findIdentificadasNoIntervalo(inicioDo(de), inicioDo(ate.plusDays(1))).stream()
                .filter(c -> daUnidade(c.getUnidade(), u)).toList();
        if (crises.isEmpty()) {
            return new long[]{0, 0};
        }
        Map<UUID, Instant> posCrise = new HashMap<>();
        for (Object[] l : etapaRepository.findPrimeirasDaEtapa(crises.stream().map(EventoCrise::getUuid).toList(), EtapaCriseSaudeMental.POS_CRISE)) {
            posCrise.put((UUID) l[0], (Instant) l[1]);
        }
        Instant agora = Instant.now();
        long num = 0;
        long den = 0;
        for (EventoCrise c : crises) {
            Instant prazo = c.getIdentificadaEm().plus(Duration.ofDays(7));
            Instant p = posCrise.get(c.getUuid());
            boolean noPrazo = p != null && !p.isAfter(prazo);
            if (noPrazo || !prazo.isAfter(agora)) {
                den++;
                num += noPrazo ? 1 : 0;
            }
        }
        return new long[]{num, den};
    }

    /**
     * Internações involuntárias admitidas no período comunicadas ao Ministério Público em até 72 horas (Lei 10.216/2001,
     * art. 8º). As de menos de 72 horas ainda sem comunicação não entram.
     */
    long[] comunicacaoMpNoPrazo(UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        List<Internacao> involuntarias = internacaoRepository.findDaModalidadeAdmitidasNoIntervalo(ModalidadeInternacao.INVOLUNTARIA, inicioDo(de),
                inicioDo(ate.plusDays(1))).stream().filter(i -> daUnidade(i.getUnidade(), u)).toList();
        if (involuntarias.isEmpty()) {
            return new long[]{0, 0};
        }
        Map<UUID, Instant> admissao = new HashMap<>();
        for (ComunicacaoMinisterioPublico c : comunicacaoRepository.findDasInternacoes(involuntarias.stream().map(Internacao::getUuid).toList())) {
            if (c.getMomento() == MomentoComunicacaoMp.ADMISSAO) {
                admissao.put(c.getInternacao().getUuid(), c.getComunicadaEm());
            }
        }
        Instant agora = Instant.now();
        long num = 0;
        long den = 0;
        for (Internacao i : involuntarias) {
            Instant prazo = i.getAdmitidaEm().plus(Duration.ofHours(72));
            Instant c = admissao.get(i.getUuid());
            boolean noPrazo = c != null && !c.isAfter(prazo);
            if (noPrazo || !prazo.isAfter(agora)) {
                den++;
                num += noPrazo ? 1 : 0;
            }
        }
        return new long[]{num, den};
    }

    /** Encaminhamentos intersetoriais enviados no período (sem os cancelados) que já têm o retorno da instituição. */
    long[] retornoIntersetorial(UnidadeDeSaude u, LocalDate de, LocalDate ate) {
        List<EncaminhamentoIntersetorial> enviados = encaminhamentoRepository.findEnviadosNoIntervalo(DominioOrigemEncaminhamento.SAUDE_MENTAL, de, ate)
                .stream()
                .filter(e -> e.getSituacao() != SituacaoEncaminhamentoIntersetorial.CANCELADO)
                .filter(e -> daUnidade(e.getUnidade(), u))
                .toList();
        return new long[]{enviados.stream().filter(e -> e.getRetornoEm() != null).count(), enviados.size()};
    }

    private static boolean daUnidade(UnidadeDeSaude x, UnidadeDeSaude u) {
        return u == null || x.getUuid().equals(u.getUuid());
    }

    private static Instant inicioDo(LocalDate dia) {
        return dia.atStartOfDay(FUSO).toInstant();
    }
}
