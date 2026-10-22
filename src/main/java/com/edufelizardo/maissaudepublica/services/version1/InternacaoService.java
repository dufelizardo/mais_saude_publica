package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.models.ComunicacaoMinisterioPublico;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicacaoMpRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ModalidadeInternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicacaoMpDto;
import com.edufelizardo.maissaudepublica.models.enuns.ModalidadeInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.MomentoComunicacaoMp;
import com.edufelizardo.maissaudepublica.repositories.ComunicacaoMinisterioPublicoRepository;
import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AltaInternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InternacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.TrocaLeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InternacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SexoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLeito;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoLeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * Internação (ADR-0098): admissão num leito livre, troca de leito na mesma unidade e alta, que é ato médico. Uma
 * internação ativa por paciente; o leito é travado na admissão e na troca. Na saída do paciente, o leito vai para
 * higienização.
 */
@Service
public class InternacaoService {

    public static final String GERENCIAR = "INTERNACAO.GERENCIAR";
    public static final String ALTA = "INTERNACAO.ALTA";

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private EventoLeitoRepository eventoRepository;

    @Autowired
    private LeitoService leitoService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private ComunicacaoMinisterioPublicoRepository comunicacaoMpRepository;

    @Transactional
    public InternacaoResponseDto internar(InternacaoRequestDto dto) {
        Leito leito = leitoService.travar(dto.getLeitoId());
        controleDeAcesso.exigir(GERENCIAR, leito.getUnidade());
        Paciente paciente = pacienteRepository.findById(dto.getPacienteId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + dto.getPacienteId() + " em nossos registros."));
        ContextoAuditoria.paciente(paciente.getUuid());
        if (!paciente.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O paciente está inativo; reative o cadastro antes de internar.");
        }
        if (internacaoRepository.existsByPaciente_UuidAndStatus(paciente.getUuid(), StatusInternacao.INTERNADO)) {
            throw new ResourceConflictException("O paciente já está internado; para mudar de leito, use a troca de leito.");
        }
        Atendimento atendimento = null;
        if (dto.getAtendimentoId() != null) {
            atendimento = atendimentoRepository.findById(dto.getAtendimentoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
            if (!atendimento.getPaciente().getUuid().equals(paciente.getUuid())) {
                throw new ResourceBadRequestException("O atendimento informado é de outro paciente.");
            }
        }
        exigirLivre(leito);
        exigirSexo(leito, paciente);
        if (dto.getPrevisaoAlta() != null && dto.getPrevisaoAlta().isBefore(java.time.LocalDate.now())) {
            throw new ResourceBadRequestException("A previsão de alta não pode ser no passado.");
        }
        Profissional medico = buscarProfissional(dto.getMedicoMatricula());

        Internacao i = new Internacao();
        i.setPaciente(paciente);
        i.setAtendimento(atendimento);
        i.setUnidade(leito.getUnidade());
        i.setLeito(leito);
        i.setMedicoResponsavel(medico);
        i.setCid(dto.getCid().trim().toUpperCase(Locale.ROOT).replace(".", ""));
        i.setMotivo(dto.getMotivo().trim());
        i.setCarater(dto.getCarater());
        i.setAdmitidaEm(Instant.now());
        i.setPrevisaoAlta(dto.getPrevisaoAlta());
        i.setStatus(StatusInternacao.INTERNADO);
        i.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        i = internacaoRepository.save(i);
        leito.setSituacao(SituacaoLeito.OCUPADO);
        leitoService.registrar(leito, i, TipoEventoLeito.ADMISSAO, dto.getCarater() + " · CID " + i.getCid(), medico);
        return detalhe(i);
    }

    @Transactional
    public InternacaoResponseDto trocarLeito(UUID uuid, TrocaLeitoRequestDto dto) {
        Internacao i = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, i.getUnidade());
        exigirInternado(i, "trocar de leito");
        if (i.getLeito().getUuid().equals(dto.getLeitoId())) {
            throw new ResourceBadRequestException("O paciente já está neste leito.");
        }
        Leito destino = leitoService.travar(dto.getLeitoId());
        if (!destino.getUnidade().getUuid().equals(i.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("A troca é na mesma unidade; para outra unidade, dê alta por transferência.");
        }
        exigirLivre(destino);
        exigirSexo(destino, i.getPaciente());
        Profissional profissional = buscarProfissional(dto.getProfissionalMatricula());
        Leito origem = leitoService.travar(i.getLeito().getUuid());
        origem.setSituacao(SituacaoLeito.HIGIENIZACAO);
        destino.setSituacao(SituacaoLeito.OCUPADO);
        i.setLeito(destino);
        internacaoRepository.save(i);
        String texto = "De " + origem.getIdentificacao() + " para " + destino.getIdentificacao() + ": " + dto.getMotivo().trim();
        leitoService.registrar(origem, i, TipoEventoLeito.TROCA_DE_LEITO, texto, profissional);
        leitoService.registrar(destino, i, TipoEventoLeito.TROCA_DE_LEITO, texto, profissional);
        return detalhe(i);
    }

    /** Alta (ato médico): tipo, sumário e data; a internação fecha e o leito vai para higienização. */
    @Transactional
    public InternacaoResponseDto darAlta(UUID uuid, AltaInternacaoRequestDto dto) {
        Internacao i = travar(uuid);
        controleDeAcesso.exigir(ALTA, i.getUnidade());
        exigirInternado(i, "dar alta a");
        Instant altaEm = dto.getAltaEm() != null ? dto.getAltaEm() : Instant.now();
        if (altaEm.isBefore(i.getAdmitidaEm())) {
            throw new ResourceBadRequestException("A alta não pode ser antes da admissão.");
        }
        if (altaEm.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A alta não pode ser no futuro.");
        }
        Profissional medico = buscarProfissional(dto.getMedicoMatricula());
        Leito leito = leitoService.travar(i.getLeito().getUuid());
        i.setStatus(StatusInternacao.ALTA);
        i.setTipoAlta(dto.getTipoAlta());
        i.setAltaEm(altaEm);
        i.setSumarioAlta(dto.getSumario().trim());
        i.setAltaPor(medico);
        internacaoRepository.save(i);
        leito.setSituacao(SituacaoLeito.HIGIENIZACAO);
        leitoService.registrar(leito, i, TipoEventoLeito.ALTA, dto.getTipoAlta().toString(), medico);
        return detalhe(i);
    }

    /** Internações das unidades do escopo, da mais recente à mais antiga; sem motivo, sumário e eventos. */
    @Transactional(readOnly = true)
    public List<InternacaoResponseDto> listar(UUID unidadeId, StatusInternacao status, UUID pacienteId) {
        return listar(unidadeId, status, pacienteId, false);
    }

    /** Com {@code comunicacaoMpPendente}, só as involuntárias com comunicação ao Ministério Público pendente (ADR-0123). */
    @Transactional(readOnly = true)
    public List<InternacaoResponseDto> listar(UUID unidadeId, StatusInternacao status, UUID pacienteId, boolean comunicacaoMpPendente) {
        List<Internacao> todas = pacienteId != null ? internacaoRepository.findByPaciente_UuidOrderByAdmitidaEmDesc(pacienteId)
                : internacaoRepository.findAllByOrderByAdmitidaEmDesc();
        List<Internacao> visiveis = controleDeAcesso.filtrar(todas.stream(), i -> Stream.of(i.getUnidade()), LeitoService.VER)
                .filter(i -> unidadeId == null || i.getUnidade().getUuid().equals(unidadeId))
                .filter(i -> status == null || i.getStatus() == status)
                .toList();
        List<UUID> involuntarias = visiveis.stream().filter(i -> i.getModalidade() == ModalidadeInternacao.INVOLUNTARIA).map(Internacao::getUuid).toList();
        java.util.Map<UUID, List<ComunicacaoMinisterioPublico>> comunicacoes = involuntarias.isEmpty() ? java.util.Map.of()
                : comunicacaoMpRepository.findDasInternacoes(involuntarias).stream()
                .collect(java.util.stream.Collectors.groupingBy(c -> c.getInternacao().getUuid()));
        return visiveis.stream()
                .map(i -> comMp(InternacaoResponseDto.fromInternacao(i, List.of(), false), i, comunicacoes.getOrDefault(i.getUuid(), List.of())))
                .filter(d -> !comunicacaoMpPendente || d.getComunicacaoMpPendente() != null)
                .toList();
    }

    /** Detalhe com motivo, sumário de alta e movimentos: leitura auditada, no escopo da unidade. */
    @Transactional(readOnly = true)
    public InternacaoResponseDto buscarPorId(UUID uuid) {
        Internacao i = internacaoRepository.findById(uuid).orElseThrow(() -> naoEncontrada(uuid));
        ContextoAuditoria.unidade(i.getUnidade());
        controleDeAcesso.filtrar(Stream.of(i), x -> Stream.of(x.getUnidade()), LeitoService.VER).findAny()
                .orElseThrow(() -> new ResourceForbiddenException("Esta internação é de uma unidade fora do seu acesso."));
        return detalhe(i);
    }

    // ── Internação psiquiátrica (ADR-0123) ─────────────────────────────────────────────────────────

    /** Prazo da comunicação ao Ministério Público: 72 horas da admissão e da alta (Lei 10.216/2001, art. 8º). */
    static final java.time.Duration PRAZO_MP = java.time.Duration.ofHours(72);

    /** Informa ou corrige a modalidade. Na compulsória, a ordem judicial é obrigatória. */
    @Transactional
    public InternacaoResponseDto definirModalidade(UUID uuid, ModalidadeInternacaoRequestDto dto) {
        Internacao i = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, i.getUnidade());
        String ordem = dto.getOrdemJudicial() == null || dto.getOrdemJudicial().isBlank() ? null : dto.getOrdemJudicial().trim();
        if (dto.getModalidade() == ModalidadeInternacao.COMPULSORIA && ordem == null) {
            throw new ResourceBadRequestException("Na internação compulsória, informe o número do processo ou da ordem judicial.");
        }
        i.setModalidade(dto.getModalidade());
        i.setOrdemJudicial(dto.getModalidade() == ModalidadeInternacao.COMPULSORIA ? ordem : null);
        internacaoRepository.save(i);
        ContextoAuditoria.detalhe("Modalidade: " + dto.getModalidade() + ".");
        return detalhe(i);
    }

    /** Registra a comunicação da involuntária ao Ministério Público, na admissão ou na alta. */
    @Transactional
    public InternacaoResponseDto comunicarMp(UUID uuid, ComunicacaoMpRequestDto dto) {
        Internacao i = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, i.getUnidade());
        if (i.getModalidade() != ModalidadeInternacao.INVOLUNTARIA) {
            throw new ResourceUnprocessableEntityException("Só a internação involuntária é comunicada ao Ministério Público.");
        }
        if (dto.getMomento() == MomentoComunicacaoMp.ALTA && i.getAltaEm() == null) {
            throw new ResourceUnprocessableEntityException("A comunicação da alta vem depois da alta.");
        }
        if (comunicacaoMpRepository.existsByInternacao_UuidAndMomento(i.getUuid(), dto.getMomento())) {
            throw new ResourceUnprocessableEntityException("A comunicação da " + (dto.getMomento() == MomentoComunicacaoMp.ALTA ? "alta" : "admissão")
                    + " já foi registrada.");
        }
        Instant em = dto.getComunicadaEm() != null ? dto.getComunicadaEm() : Instant.now();
        Instant inicio = dto.getMomento() == MomentoComunicacaoMp.ALTA ? i.getAltaEm() : i.getAdmitidaEm();
        if (em.isBefore(inicio) || em.isAfter(Instant.now().plusSeconds(60))) {
            throw new ResourceBadRequestException("A comunicação precisa ser entre a " + (dto.getMomento() == MomentoComunicacaoMp.ALTA ? "alta" : "admissão")
                    + " e agora.");
        }
        ComunicacaoMinisterioPublico c = new ComunicacaoMinisterioPublico();
        c.setInternacao(i);
        c.setMomento(dto.getMomento());
        c.setComunicadaEm(em);
        String matricula = dto.getResponsavelMatricula() == null || dto.getResponsavelMatricula().isBlank() ? null : dto.getResponsavelMatricula().trim();
        c.setResponsavel(matricula != null ? buscarProfissional(matricula)
                : (UsuarioAutenticado.cpf() == null ? null : profissionalRepository.findByCpfAndAtivoTrue(UsuarioAutenticado.cpf()).orElse(null)));
        c.setProtocolo(dto.getProtocolo() == null || dto.getProtocolo().isBlank() ? null : dto.getProtocolo().trim());
        c.setObservacao(dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        comunicacaoMpRepository.save(c);
        ContextoAuditoria.detalhe("Comunicação ao Ministério Público: " + dto.getMomento() + ".");
        return detalhe(i);
    }

    /** Acrescenta ao DTO as comunicações ao MP, a pendente e o prazo (só na involuntária). */
    private InternacaoResponseDto comMp(InternacaoResponseDto d, Internacao i, List<ComunicacaoMinisterioPublico> comunicacoes) {
        d.setComunicacoesMp(comunicacoes.stream().map(c -> new ComunicacaoMpDto(c.getUuid(), c.getMomento(), c.getComunicadaEm(),
                c.getResponsavel() != null ? c.getResponsavel().getNome() : null, c.getProtocolo(), c.getObservacao())).toList());
        if (i.getModalidade() != ModalidadeInternacao.INVOLUNTARIA) {
            return d;
        }
        boolean admissao = comunicacoes.stream().anyMatch(c -> c.getMomento() == MomentoComunicacaoMp.ADMISSAO);
        boolean alta = comunicacoes.stream().anyMatch(c -> c.getMomento() == MomentoComunicacaoMp.ALTA);
        MomentoComunicacaoMp pendente = !admissao ? MomentoComunicacaoMp.ADMISSAO : (i.getAltaEm() != null && !alta ? MomentoComunicacaoMp.ALTA : null);
        if (pendente != null) {
            Instant prazo = (pendente == MomentoComunicacaoMp.ALTA ? i.getAltaEm() : i.getAdmitidaEm()).plus(PRAZO_MP);
            d.setComunicacaoMpPendente(pendente);
            d.setPrazoComunicacaoMp(prazo);
            d.setComunicacaoMpVencida(Instant.now().isAfter(prazo));
        }
        return d;
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private InternacaoResponseDto detalhe(Internacao i) {
        ContextoAuditoria.paciente(i.getPaciente().getUuid());
        ContextoAuditoria.registro(i.getUuid());
        return comMp(InternacaoResponseDto.fromInternacao(i, eventoRepository.findByInternacao_UuidOrderByOcorridoEmAsc(i.getUuid()), true), i,
                comunicacaoMpRepository.findDasInternacoes(List.of(i.getUuid())));
    }

    private Internacao travar(UUID uuid) {
        Internacao i = internacaoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> naoEncontrada(uuid));
        ContextoAuditoria.paciente(i.getPaciente().getUuid());
        return i;
    }

    private static void exigirInternado(Internacao i, String acao) {
        if (i.getStatus() != StatusInternacao.INTERNADO) {
            throw new ResourceUnprocessableEntityException("Não é possível " + acao + " uma internação encerrada.");
        }
    }

    private static void exigirLivre(Leito l) {
        if (!l.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O leito " + l.getIdentificacao() + " está fora de uso.");
        }
        if (l.getSituacao() != SituacaoLeito.LIVRE) {
            throw new ResourceUnprocessableEntityException("O leito " + l.getIdentificacao() + " não está livre: está " + l.getSituacao() + ".");
        }
    }

    /** Enfermaria masculina ou feminina só recebe paciente do mesmo sexo; sexo ignorado vai para leito misto. */
    private static void exigirSexo(Leito l, Paciente p) {
        if (l.getSexo() == SexoLeito.MISTO) {
            return;
        }
        Sexo esperado = l.getSexo() == SexoLeito.MASCULINO ? Sexo.MASCULINO : Sexo.FEMININO;
        if (p.getSexo() != esperado) {
            throw new ResourceUnprocessableEntityException("O leito " + l.getIdentificacao() + " é de enfermaria "
                    + l.getSexo().toString().toLowerCase(Locale.ROOT) + "; escolha um leito compatível com o paciente.");
        }
    }

    private Profissional buscarProfissional(String matricula) {
        return profissionalRepository.findByMatricula(matricula).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private static ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException("Não foi possível encontrar uma internação com o id " + uuid + " em nossos registros.");
    }
}
