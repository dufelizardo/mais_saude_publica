package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcompanhamentoSaudeMental;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.ReferenciaSaudeMental;
import com.edufelizardo.maissaudepublica.models.IntervencaoPrevista;
import com.edufelizardo.maissaudepublica.models.IntervencaoSaudeMental;
import com.edufelizardo.maissaudepublica.models.ObjetivoTerapeutico;
import com.edufelizardo.maissaudepublica.models.PessoaRedeApoio;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.ProjetoTerapeuticoSingular;
import com.edufelizardo.maissaudepublica.models.TipoIntervencaoSaudeMental;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRedeApoioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.IntervencaoPrevistaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.IntervencaoSaudeMentalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ObjetivoTerapeuticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PessoaRedeApoioRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ProjetoTerapeuticoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcompanhamentoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProjetoTerapeuticoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TipoIntervencaoSaudeMentalDto;
import com.edufelizardo.maissaudepublica.models.enuns.PeriodicidadeAcompanhamento;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIntervencaoSaudeMental;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoObjetivoTerapeutico;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.IntervencaoPrevistaRepository;
import com.edufelizardo.maissaudepublica.repositories.IntervencaoSaudeMentalRepository;
import com.edufelizardo.maissaudepublica.repositories.ObjetivoTerapeuticoRepository;
import com.edufelizardo.maissaudepublica.repositories.PessoaRedeApoioRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.ProjetoTerapeuticoSingularRepository;
import com.edufelizardo.maissaudepublica.repositories.TipoIntervencaoSaudeMentalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Saúde Mental, fatia SM2 (ADR-0121): o Projeto Terapêutico Singular versionado, as intervenções realizadas e a rede
 * de apoio. Tudo se pendura no caso da SM1 e segue o mesmo sigilo: só escreve quem tem o conteúdo do caso liberado
 * (equipe de referência, autores, acesso justificado), com {@value SaudeMentalService#ACOMPANHAR} na unidade.
 */
@Service
public class ProjetoTerapeuticoService {

    /** Sem periodicidade informada, a revisão do PTS é trimestral. */
    static final PeriodicidadeAcompanhamento REVISAO_PADRAO = PeriodicidadeAcompanhamento.TRIMESTRAL;

    @Autowired
    private SaudeMentalService saudeMentalService;

    @Autowired
    private TerapeuticoLeitura leitura;

    @Autowired
    private ProjetoTerapeuticoSingularRepository ptsRepository;

    @Autowired
    private ObjetivoTerapeuticoRepository objetivoRepository;

    @Autowired
    private IntervencaoPrevistaRepository previstaRepository;

    @Autowired
    private IntervencaoSaudeMentalRepository intervencaoRepository;

    @Autowired
    private PessoaRedeApoioRepository redeRepository;

    @Autowired
    private TipoIntervencaoSaudeMentalRepository tipoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    /** Tipo de intervenção do apoio matricial (ADR-0122): pede a equipe matriciada. */
    static final String MATRICIAMENTO = "MATRICIAMENTO";

    @Transactional(readOnly = true)
    public List<TipoIntervencaoSaudeMentalDto> tipos() {
        return tipoRepository.findAllByOrderByOrdemAsc().stream()
                .map(t -> new TipoIntervencaoSaudeMentalDto(t.getUuid(), t.getCodigo(), t.getNome(), t.isAtivo())).toList();
    }

    /** As versões do PTS do caso, com o mesmo acesso de escrita (só quem tem o conteúdo liberado lê o PTS). */
    @Transactional(readOnly = true)
    public List<ProjetoTerapeuticoDto> versoes(UUID casoId) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaLer(casoId);
        return leitura.versoes(caso.getUuid());
    }

    /**
     * Elabora (sem {@code versaoBase}) ou revisa o PTS. A revisão parte da versão vigente: se outra revisão entrou antes,
     * 409, para uma não apagar a outra. A versão anterior fica, marcada como substituída.
     */
    @Transactional
    public AcompanhamentoSaudeMentalDto salvarPts(UUID casoId, ProjetoTerapeuticoRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        List<ProjetoTerapeuticoSingular> versoes = ptsRepository.findDoAcompanhamento(caso.getUuid());
        ProjetoTerapeuticoSingular vigente = versoes.stream().filter(ProjetoTerapeuticoSingular::vigente).findFirst().orElse(null);
        Integer atual = vigente != null ? vigente.getVersao() : null;
        if (!Objects.equals(atual, dto.getVersaoBase())) {
            throw new ResourceConflictException(atual == null
                    ? "O caso ainda não tem PTS; elabore a primeira versão."
                    : "O PTS já está na versão " + atual + "; recarregue o caso e revise a partir dela.");
        }
        String sintese = texto(dto.getSinteseRevisao());
        if (vigente != null && sintese == null) {
            throw new ResourceBadRequestException("Na revisão, escreva a síntese do que mudou.");
        }
        PeriodicidadeAcompanhamento periodicidade = dto.getPeriodicidadeRevisao() != null ? dto.getPeriodicidadeRevisao() : REVISAO_PADRAO;
        Integer dias = periodicidade.dias() != null ? periodicidade.dias() : REVISAO_PADRAO.dias();
        LocalDate revisarEm = dto.getRevisarEm() != null ? dto.getRevisarEm() : LocalDate.now().plusDays(dias);
        if (revisarEm.isBefore(LocalDate.now())) {
            throw new ResourceBadRequestException("A data de revisão não pode ser no passado.");
        }
        Instant agora = Instant.now();
        if (vigente != null) {
            vigente.setSubstituidoEm(agora);
            ptsRepository.saveAndFlush(vigente);
        }
        ProjetoTerapeuticoSingular pts = new ProjetoTerapeuticoSingular();
        pts.setAcompanhamento(caso);
        pts.setVersao(atual == null ? 1 : atual + 1);
        pts.setNecessidades(dto.getNecessidades().trim());
        pts.setPeriodicidadeRevisao(periodicidade);
        pts.setRevisarEm(revisarEm);
        pts.setPactuadoComPessoa(Boolean.TRUE.equals(dto.getPactuadoComPessoa()));
        pts.setPactuacaoObservacao(texto(dto.getPactuacaoObservacao()));
        pts.setSinteseRevisao(vigente != null ? sintese : null);
        pts.setElaboradoEm(agora);
        pts.setProfissional(saudeMentalService.profissionalDoUsuario());
        pts.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        pts = ptsRepository.save(pts);
        int ordem = 0;
        for (ObjetivoTerapeuticoRequestDto o : dto.getObjetivos()) {
            ObjetivoTerapeutico obj = new ObjetivoTerapeutico();
            obj.setPts(pts);
            obj.setOrdem(++ordem);
            obj.setDescricao(o.getDescricao().trim());
            obj.setMeta(texto(o.getMeta()));
            obj.setPrazo(o.getPrazo());
            obj.setResponsavel(responsavel(caso, o.getResponsavelMatricula()));
            obj.setSituacao(o.getSituacao() != null ? o.getSituacao() : SituacaoObjetivoTerapeutico.EM_ANDAMENTO);
            objetivoRepository.save(obj);
        }
        ordem = 0;
        for (IntervencaoPrevistaRequestDto i : dto.getIntervencoesPrevistas() == null ? List.<IntervencaoPrevistaRequestDto>of() : dto.getIntervencoesPrevistas()) {
            IntervencaoPrevista prev = new IntervencaoPrevista();
            prev.setPts(pts);
            prev.setOrdem(++ordem);
            prev.setTipo(tipoAtivo(i.getTipoId()));
            prev.setFrequencia(texto(i.getFrequencia()));
            prev.setResponsavel(responsavel(caso, i.getResponsavelMatricula()));
            prev.setServico(texto(i.getServico()));
            previstaRepository.save(prev);
        }
        ContextoAuditoria.detalhe("PTS versão " + pts.getVersao() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    /** Registra a intervenção realizada (ou a falta). Só inclusão. */
    @Transactional
    public AcompanhamentoSaudeMentalDto registrarIntervencao(UUID casoId, IntervencaoSaudeMentalRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        Instant quando = dto.getRealizadaEm() != null ? dto.getRealizadaEm() : Instant.now();
        if (quando.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A intervenção não pode ser no futuro.");
        }
        if (quando.atZone(ZoneId.systemDefault()).toLocalDate().isBefore(caso.getInicio())) {
            throw new ResourceBadRequestException("A intervenção não pode ser antes do início do caso (" + caso.getInicio() + ").");
        }
        IntervencaoPrevista prevista = null;
        if (dto.getPrevistaId() != null) {
            prevista = previstaRepository.findById(dto.getPrevistaId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a intervenção prevista " + dto.getPrevistaId() + " em nossos registros."));
            if (!prevista.getPts().getAcompanhamento().getUuid().equals(caso.getUuid())) {
                throw new ResourceUnprocessableEntityException("A intervenção prevista é do PTS de outro caso.");
            }
        }
        Profissional compartilhado = null;
        String matricula = texto(dto.getCompartilhadoComMatricula());
        if (matricula != null) {
            compartilhado = profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
            if (!compartilhado.isAtivo()) {
                throw new ResourceUnprocessableEntityException(compartilhado.getNome() + " está inativo.");
            }
        }
        TipoIntervencaoSaudeMental tipo = tipoAtivo(dto.getTipoId());
        Equipe matriciada = equipeMatriciada(caso, tipo, dto.getEquipeMatriciadaId());
        IntervencaoSaudeMental i = new IntervencaoSaudeMental();
        i.setAcompanhamento(caso);
        i.setTipo(tipo);
        i.setEquipeMatriciada(matriciada);
        i.setRealizadaEm(quando);
        i.setProfissional(saudeMentalService.profissionalDoUsuario());
        i.setCompartilhadoCom(compartilhado);
        i.setPrevista(prevista);
        i.setSituacao(dto.getSituacao() != null ? dto.getSituacao() : SituacaoIntervencaoSaudeMental.REALIZADA);
        i.setResumo(dto.getResumo().trim());
        i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        i = intervencaoRepository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        ContextoAuditoria.detalhe("Intervenção " + i.getTipo().getCodigo() + " (" + i.getSituacao() + ").");
        return saudeMentalService.detalheLiberado(caso);
    }

    @Transactional
    public AcompanhamentoSaudeMentalDto incluirNaRede(UUID casoId, PessoaRedeApoioRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        PessoaRedeApoio p = new PessoaRedeApoio();
        p.setAcompanhamento(caso);
        p.setNome(dto.getNome().trim());
        p.setPapel(dto.getPapel());
        p.setVinculo(texto(dto.getVinculo()));
        p.setTelefone(texto(dto.getTelefone()));
        p.setObservacao(texto(dto.getObservacao()));
        p.setInicio(LocalDate.now());
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        p = redeRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Rede de apoio: " + dto.getPapel() + ".");
        return saudeMentalService.detalheLiberado(caso);
    }

    /** Sai da rede de apoio com motivo; o registro fica no histórico do caso. */
    @Transactional
    public AcompanhamentoSaudeMentalDto encerrarNaRede(UUID casoId, UUID pessoaId, EncerramentoRedeApoioRequestDto dto) {
        AcompanhamentoSaudeMental caso = saudeMentalService.casoParaEscrever(casoId);
        PessoaRedeApoio p = leitura.pessoa(pessoaId);
        if (p == null || !p.getAcompanhamento().getUuid().equals(caso.getUuid())) {
            throw new ResourceNotFoundException("Não foi possível encontrar a pessoa " + pessoaId + " na rede de apoio deste caso.");
        }
        if (!p.vigente()) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " já saiu da rede de apoio.");
        }
        p.setFim(LocalDate.now());
        p.setMotivoFim(dto.getMotivo().trim());
        redeRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return saudeMentalService.detalheLiberado(caso);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Responsável no PTS: opcional; se informado, técnico ou membro vigente da equipe de referência do caso. */
    private Profissional responsavel(AcompanhamentoSaudeMental caso, String matricula) {
        String m = texto(matricula);
        if (m == null) {
            return null;
        }
        Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + m + " em nossos registros."));
        if (!p.isAtivo() || !saudeMentalService.daReferencia(caso, p)) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " não é da equipe de referência do caso e não pode ser "
                    + "responsável no PTS.");
        }
        return p;
    }

    /**
     * No matriciamento, a equipe com quem o caso foi discutido é obrigatória, e o apoio matricial da ADR-0103 precisa
     * existir entre ela e a equipe de referência, em qualquer sentido. Nos outros tipos, não se informa.
     */
    private Equipe equipeMatriciada(AcompanhamentoSaudeMental caso, TipoIntervencaoSaudeMental tipo, UUID equipeId) {
        if (!MATRICIAMENTO.equals(tipo.getCodigo())) {
            if (equipeId != null) {
                throw new ResourceBadRequestException("A equipe matriciada só vale na discussão de caso em apoio matricial.");
            }
            return null;
        }
        if (equipeId == null) {
            throw new ResourceBadRequestException("No matriciamento, informe a equipe com quem o caso foi discutido.");
        }
        Equipe matriciada = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        ReferenciaSaudeMental r = saudeMentalService.referenciaVigente(caso);
        Equipe referencia = r != null ? r.getEquipe() : null;
        boolean apoio = referencia != null && (referencia.getApoiadas().stream().anyMatch(e -> e.getUuid().equals(matriciada.getUuid()))
                || matriciada.getApoiadas().stream().anyMatch(e -> e.getUuid().equals(referencia.getUuid())));
        if (!apoio) {
            throw new ResourceUnprocessableEntityException("Não há apoio matricial entre a equipe " + matriciada.getNome()
                    + " e a equipe de referência do caso (cadastre o apoio na equipe, ADR-0103).");
        }
        return matriciada;
    }

    private TipoIntervencaoSaudeMental tipoAtivo(UUID tipoId) {
        TipoIntervencaoSaudeMental t = tipoRepository.findById(tipoId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o tipo de intervenção " + tipoId + " em nossos registros."));
        if (!t.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O tipo de intervenção " + t.getNome() + " está inativo.");
        }
        return t;
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
