package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.Campanha;
import com.edufelizardo.maissaudepublica.models.LocalCampanha;
import com.edufelizardo.maissaudepublica.models.Programa;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LocalCampanhaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CampanhaPublicaDto;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoProgramatica;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanha;
import com.edufelizardo.maissaudepublica.repositories.AcaoProgramaticaRepository;
import com.edufelizardo.maissaudepublica.repositories.CampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.LocalCampanhaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProgramaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Campanhas de saúde, fatia P3 (ADR-0133, desenho na ADR-0109): cadastro com período, público, meta e locais (unidades
 * da rede ou locais temporários), publicação na consulta pública e cancelamento. A situação vem das datas; as ações da
 * campanha são ações programáticas (ADR-0132), e o atendido nelas dá o andamento da meta.
 */
@Service
public class CampanhaService {

    private static final String GERENCIAR = ProgramaService.GERENCIAR;

    @Autowired
    private CampanhaRepository campanhaRepository;

    @Autowired
    private LocalCampanhaRepository localRepository;

    @Autowired
    private AcaoProgramaticaRepository acaoRepository;

    @Autowired
    private ProgramaRepository programaRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    /** As campanhas (não é dado de paciente): em andamento primeiro, depois as planejadas, as encerradas e as canceladas. */
    @Transactional(readOnly = true)
    public List<CampanhaDto> listar(SituacaoCampanha situacao, UUID programaId) {
        LocalDate hoje = LocalDate.now();
        return campanhaRepository.findTodas().stream()
                .filter(c -> situacao == null || c.situacao(hoje) == situacao)
                .filter(c -> programaId == null || (c.getPrograma() != null && c.getPrograma().getUuid().equals(programaId)))
                .sorted(Comparator.comparing((Campanha c) -> c.situacao(hoje).ordinal() == 1 ? 0 : c.situacao(hoje).ordinal() == 0 ? 1 : 2 + c.situacao(hoje).ordinal())
                        .thenComparing(Campanha::getInicio))
                .map(c -> dto(c, hoje, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public CampanhaDto buscar(UUID uuid) {
        return dto(campanha(uuid), LocalDate.now(), true);
    }

    /** Consulta pública: as publicadas, em andamento ou planejadas, sem nenhum dado de paciente. */
    @Transactional(readOnly = true)
    public List<CampanhaPublicaDto> publico() {
        LocalDate hoje = LocalDate.now();
        return campanhaRepository.findTodas().stream()
                .filter(c -> c.isPublicada() && (c.situacao(hoje) == SituacaoCampanha.EM_ANDAMENTO || c.situacao(hoje) == SituacaoCampanha.PLANEJADA))
                .sorted(Comparator.comparing((Campanha c) -> c.situacao(hoje) != SituacaoCampanha.EM_ANDAMENTO).thenComparing(Campanha::getInicio))
                .map(c -> new CampanhaPublicaDto(c.getNome(), c.getTipo(), c.getDescricao(), publico(c), c.getInicio(), c.getFim(), c.situacao(hoje),
                        c.getMetaDescricao(), c.getMetaQuantidade(), realizado(c),
                        localRepository.findByCampanha_Uuid(c.getUuid()).stream()
                                .sorted(Comparator.comparing(this::nomeDoLocal, String.CASE_INSENSITIVE_ORDER))
                                .map(l -> new CampanhaPublicaDto.Local(nomeDoLocal(l), endereco(l), l.getFuncionamento(), l.getLatitude(), l.getLongitude()))
                                .toList()))
                .toList();
    }

    // ── Escrita ────────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public CampanhaDto criar(CampanhaRequestDto dto) {
        Campanha c = new Campanha();
        aplicar(c, dto);
        c.setCriadaEm(Instant.now());
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        c = campanhaRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Campanha " + c.getNome() + ".");
        return dto(c, LocalDate.now(), true);
    }

    @Transactional
    public CampanhaDto atualizar(UUID uuid, CampanhaRequestDto dto) {
        Campanha c = naoCancelada(uuid);
        aplicar(c, dto);
        campanhaRepository.save(c);
        return dto(c, LocalDate.now(), true);
    }

    /** Publica ou tira da consulta pública; a cancelada não se publica (422). */
    @Transactional
    public CampanhaDto publicar(UUID uuid, boolean publicada) {
        Campanha c = naoCancelada(uuid);
        c.setPublicada(publicada);
        campanhaRepository.save(c);
        ContextoAuditoria.detalhe(publicada ? "Publicada." : "Retirada da consulta pública.");
        return dto(c, LocalDate.now(), true);
    }

    /** Cancela com o motivo: sai da consulta pública, e as ações pendentes dela são canceladas junto. */
    @Transactional
    public CampanhaDto cancelar(UUID uuid, String motivo) {
        Campanha c = naoCancelada(uuid);
        if (motivo == null || motivo.isBlank()) {
            throw new ResourceBadRequestException("Informe o motivo do cancelamento.");
        }
        c.setCanceladaEm(Instant.now());
        c.setMotivoCancelamento(motivo.trim());
        c.setPublicada(false);
        campanhaRepository.save(c);
        for (AcaoProgramatica a : acaoRepository.findByCampanha_Uuid(c.getUuid())) {
            if (a.getSituacao() == SituacaoAcaoProgramatica.PENDENTE) {
                a.setSituacao(SituacaoAcaoProgramatica.CANCELADA);
                a.setMotivo("Campanha cancelada: " + c.getMotivoCancelamento());
                acaoRepository.save(a);
            }
        }
        ContextoAuditoria.detalhe("Cancelada: " + c.getMotivoCancelamento());
        return dto(c, LocalDate.now(), true);
    }

    /**
     * Inclui um local: a unidade da rede, ou um temporário (com nome) sob a unidade responsável. Quem gerencia programas
     * na unidade inclui; o período próprio fica dentro do da campanha (422).
     */
    @Transactional
    public CampanhaDto incluirLocal(UUID campanhaId, LocalCampanhaRequestDto dto) {
        Campanha c = naoCancelada(campanhaId);
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, unidade);
        if (!unidade.isAtivo()) {
            throw new ResourceUnprocessableEntityException(unidade.getNome() + " está inativa.");
        }
        boolean temporario = Boolean.TRUE.equals(dto.getTemporario());
        if (temporario && texto(dto.getNome()) == null) {
            throw new ResourceBadRequestException("O local temporário pede o nome (ex.: Escola Municipal, Praça Central).");
        }
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("Informe a latitude e a longitude juntas, ou nenhuma das duas.");
        }
        LocalDate inicio = dto.getInicio();
        LocalDate fim = dto.getFim();
        if ((inicio != null && (inicio.isBefore(c.getInicio()) || inicio.isAfter(c.getFim())))
                || (fim != null && (fim.isBefore(c.getInicio()) || fim.isAfter(c.getFim())))
                || (inicio != null && fim != null && fim.isBefore(inicio))) {
            throw new ResourceUnprocessableEntityException("O período do local precisa estar dentro do da campanha (" + c.getInicio() + " a " + c.getFim() + ").");
        }
        if (!temporario && localRepository.findByCampanha_Uuid(c.getUuid()).stream()
                .anyMatch(l -> !l.isTemporario() && l.getUnidade().getUuid().equals(unidade.getUuid()))) {
            throw new ResourceConflictException(unidade.getNome() + " já é local desta campanha.");
        }
        LocalCampanha l = new LocalCampanha(null, c, unidade, temporario, temporario ? texto(dto.getNome()) : null, texto(dto.getEndereco()),
                dto.getLatitude(), dto.getLongitude(), texto(dto.getFuncionamento()), inicio, fim);
        localRepository.save(l);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Local: " + nomeDoLocal(l) + ".");
        return dto(c, LocalDate.now(), true);
    }

    /** Retira o local; com ações registradas nele, não (409). */
    @Transactional
    public CampanhaDto removerLocal(UUID localId) {
        LocalCampanha l = localRepository.findById(localId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o local " + localId + " em nossos registros."));
        Campanha c = naoCancelada(l.getCampanha().getUuid());
        controleDeAcesso.exigir(GERENCIAR, l.getUnidade());
        if (acaoRepository.existsByLocalCampanha_Uuid(l.getUuid())) {
            throw new ResourceConflictException("O local tem ações registradas e não pode ser retirado.");
        }
        localRepository.delete(l);
        ContextoAuditoria.detalhe("Local retirado: " + nomeDoLocal(l) + ".");
        return dto(c, LocalDate.now(), true);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private void aplicar(Campanha c, CampanhaRequestDto dto) {
        if (dto.getFim().isBefore(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim da campanha não pode ser antes do início.");
        }
        if (dto.getIdadeMinima() != null && dto.getIdadeMaxima() != null && dto.getIdadeMaxima() < dto.getIdadeMinima()) {
            throw new ResourceBadRequestException("A idade máxima não pode ser menor que a mínima.");
        }
        if (dto.getSexo() == Sexo.IGNORADO) {
            throw new ResourceBadRequestException("O público-alvo é de um sexo (MASCULINO ou FEMININO) ou de qualquer um (vazio).");
        }
        Programa programa = null;
        if (dto.getProgramaId() != null) {
            programa = programaRepository.findById(dto.getProgramaId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o programa " + dto.getProgramaId() + " em nossos registros."));
        }
        // Locais com período próprio continuam dentro do período novo.
        if (c.getUuid() != null && localRepository.findByCampanha_Uuid(c.getUuid()).stream().anyMatch(l ->
                (l.getInicio() != null && l.getInicio().isBefore(dto.getInicio())) || (l.getFim() != null && l.getFim().isAfter(dto.getFim())))) {
            throw new ResourceUnprocessableEntityException("Há local com período fora do novo período da campanha; ajuste o local antes.");
        }
        c.setNome(dto.getNome().trim());
        c.setDescricao(texto(dto.getDescricao()));
        c.setTipo(dto.getTipo());
        c.setPrograma(programa);
        c.setInicio(dto.getInicio());
        c.setFim(dto.getFim());
        c.setIdadeMinima(dto.getIdadeMinima());
        c.setIdadeMaxima(dto.getIdadeMaxima());
        c.setPublicoSexo(dto.getSexo());
        c.setGestante(Boolean.TRUE.equals(dto.getGestante()));
        List<String> condicoes = ProgramaService.condicoes(dto.getCondicoes());
        c.getCondicoes().clear();
        c.getCondicoes().addAll(condicoes);
        c.setPublicoDescricao(texto(dto.getPublicoDescricao()));
        c.setMetaDescricao(texto(dto.getMetaDescricao()));
        c.setMetaQuantidade(dto.getMetaQuantidade());
    }

    private Campanha campanha(UUID uuid) {
        Campanha c = campanhaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a campanha " + uuid + " em nossos registros."));
        ContextoAuditoria.registro(c.getUuid());
        return c;
    }

    private Campanha naoCancelada(UUID uuid) {
        Campanha c = campanha(uuid);
        if (c.getCanceladaEm() != null) {
            throw new ResourceUnprocessableEntityException("A campanha foi cancelada e não muda mais.");
        }
        return c;
    }

    /** A descrição do público, quando houver; senão, o texto montado dos campos. */
    private static String publico(Campanha c) {
        return c.getPublicoDescricao() != null ? c.getPublicoDescricao()
                : ProgramaService.publico(c.getIdadeMinima(), c.getIdadeMaxima(), c.getPublicoSexo(), c.isGestante(), c.getCondicoes(), false);
    }

    /** O atendido nas ações realizadas da campanha. */
    private int realizado(Campanha c) {
        return acaoRepository.findByCampanha_Uuid(c.getUuid()).stream()
                .filter(a -> a.getSituacao() == SituacaoAcaoProgramatica.REALIZADA && a.getQuantidadeAtendida() != null)
                .mapToInt(AcaoProgramatica::getQuantidadeAtendida).sum();
    }

    private String nomeDoLocal(LocalCampanha l) {
        return l.isTemporario() ? l.getNome() : l.getUnidade().getNome();
    }

    private String endereco(LocalCampanha l) {
        if (l.getEndereco() != null || l.isTemporario()) {
            return l.getEndereco();
        }
        var e = l.getUnidade().getEndereco();
        if (e == null || texto(e.getLogradouro()) == null) {
            return null;
        }
        String rua = e.getLogradouro().trim() + (texto(e.getNumeroLogradouro()) != null ? ", " + e.getNumeroLogradouro().trim() : "");
        return texto(e.getBairro()) != null ? rua + " · " + e.getBairro().trim() : rua;
    }

    private CampanhaDto dto(Campanha c, LocalDate hoje, boolean completo) {
        List<AcaoProgramatica> acoes = acaoRepository.findByCampanha_Uuid(c.getUuid());
        List<CampanhaDto.Local> locais = !completo ? List.of() : localRepository.findByCampanha_Uuid(c.getUuid()).stream()
                .sorted(Comparator.comparing(this::nomeDoLocal, String.CASE_INSENSITIVE_ORDER))
                .map(l -> new CampanhaDto.Local(l.getUuid(), l.isTemporario(), l.getUnidade().getUuid(), l.getUnidade().getNome(), nomeDoLocal(l),
                        endereco(l), l.getLatitude(), l.getLongitude(), l.getFuncionamento(), l.getInicio(), l.getFim()))
                .toList();
        return new CampanhaDto(c.getUuid(), c.getNome(), c.getDescricao(), c.getTipo(), c.getPrograma() != null ? c.getPrograma().getUuid() : null,
                c.getPrograma() != null ? c.getPrograma().getNome() : null, c.getInicio(), c.getFim(), c.situacao(hoje), publico(c),
                c.getIdadeMinima(), c.getIdadeMaxima(), c.getPublicoSexo(), c.isGestante(), List.copyOf(c.getCondicoes()), c.getPublicoDescricao(),
                c.getMetaDescricao(), c.getMetaQuantidade(), realizado(c), c.isPublicada(), c.getCanceladaEm(), c.getMotivoCancelamento(),
                (int) acoes.stream().filter(a -> a.getSituacao() == SituacaoAcaoProgramatica.PENDENTE).count(),
                (int) acoes.stream().filter(a -> a.getSituacao() == SituacaoAcaoProgramatica.REALIZADA).count(), locais);
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
