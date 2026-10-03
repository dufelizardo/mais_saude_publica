package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.EventoLeito;
import com.edufelizardo.maissaudepublica.models.Internacao;
import com.edufelizardo.maissaudepublica.models.Leito;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LeitoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.IndicadoresLeitosDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LeitoMapaDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.StatusInternacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoEventoLeito;
import com.edufelizardo.maissaudepublica.models.enuns.TipoSetor;
import com.edufelizardo.maissaudepublica.repositories.EventoLeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeitoRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Leitos (ADR-0098): cadastro num setor assistencial da unidade, bloqueio com motivo, liberação depois da higienização,
 * mapa e indicadores. A ocupação muda pela internação ({@link InternacaoService}).
 */
@Service
public class LeitoService {

    public static final String GERENCIAR = "LEITO.GERENCIAR";
    static final String[] VER = {"INTERNACAO.CONSULTAR", "INTERNACAO.GERENCIAR", "INTERNACAO.ALTA", GERENCIAR};

    @Autowired
    private LeitoRepository leitoRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private EventoLeitoRepository eventoRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Transactional
    public LeitoMapaDto criar(LeitoRequestDto dto) {
        UnidadeDeSaude unidade = unidadeDeSaudeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, unidade);
        Setor setor = setorRepository.findById(dto.getSetorId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um setor com o id " + dto.getSetorId() + " em nossos registros."));
        if (setor.getUnidade() == null || !setor.getUnidade().getUuid().equals(unidade.getUuid())) {
            throw new ResourceBadRequestException("O setor é de outra unidade.");
        }
        if (setor.getTipo() != TipoSetor.ASSISTENCIAL) {
            throw new ResourceUnprocessableEntityException("Leito fica em setor assistencial; o setor " + setor.getNome() + " é " + setor.getTipo() + ".");
        }
        String identificacao = dto.getIdentificacao().trim();
        if (leitoRepository.existsByUnidade_UuidAndIdentificacaoIgnoreCase(unidade.getUuid(), identificacao)) {
            throw new ResourceConflictException("Já existe o leito " + identificacao + " nesta unidade.");
        }
        Leito l = new Leito();
        l.setUnidade(unidade);
        l.setSetor(setor);
        l.setIdentificacao(identificacao);
        l.setTipo(dto.getTipo());
        l.setSexo(dto.getSexo());
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        l = leitoRepository.save(l);
        ContextoAuditoria.registro(l.getUuid());
        return LeitoMapaDto.fromLeito(l, null);
    }

    /** Edita identificação, tipo, sexo e uso. Leito ocupado não sai de uso. */
    @Transactional
    public LeitoMapaDto atualizar(UUID uuid, LeitoRequestDto dto) {
        Leito l = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, l.getUnidade());
        String identificacao = dto.getIdentificacao().trim();
        if (!identificacao.equalsIgnoreCase(l.getIdentificacao())
                && leitoRepository.existsByUnidade_UuidAndIdentificacaoIgnoreCase(l.getUnidade().getUuid(), identificacao)) {
            throw new ResourceConflictException("Já existe o leito " + identificacao + " nesta unidade.");
        }
        boolean ativo = dto.getAtivo() == null || dto.getAtivo();
        if (!ativo && l.getSituacao() == SituacaoLeito.OCUPADO) {
            throw new ResourceUnprocessableEntityException("O leito está ocupado; dê alta ou troque o paciente de leito antes de tirá-lo de uso.");
        }
        if (l.getSituacao() == SituacaoLeito.OCUPADO && dto.getSexo() != l.getSexo()) {
            throw new ResourceUnprocessableEntityException("O leito está ocupado; mude quem a enfermaria recebe depois da saída do paciente.");
        }
        l.setIdentificacao(identificacao);
        l.setTipo(dto.getTipo());
        l.setSexo(dto.getSexo());
        l.setAtivo(ativo);
        return LeitoMapaDto.fromLeito(leitoRepository.save(l), internacaoAtiva(l));
    }

    @Transactional
    public LeitoMapaDto bloquear(UUID uuid, String motivo) {
        Leito l = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, l.getUnidade());
        if (l.getSituacao() != SituacaoLeito.LIVRE && l.getSituacao() != SituacaoLeito.HIGIENIZACAO) {
            throw new ResourceUnprocessableEntityException("Só leito livre ou em higienização pode ser bloqueado; este está " + l.getSituacao() + ".");
        }
        l.setSituacao(SituacaoLeito.BLOQUEADO);
        l.setMotivoBloqueio(motivo.trim());
        registrar(l, null, TipoEventoLeito.BLOQUEIO, motivo.trim(), null);
        return LeitoMapaDto.fromLeito(leitoRepository.save(l), null);
    }

    @Transactional
    public LeitoMapaDto desbloquear(UUID uuid) {
        Leito l = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, l.getUnidade());
        if (l.getSituacao() != SituacaoLeito.BLOQUEADO) {
            throw new ResourceUnprocessableEntityException("O leito não está bloqueado.");
        }
        l.setSituacao(SituacaoLeito.LIVRE);
        l.setMotivoBloqueio(null);
        registrar(l, null, TipoEventoLeito.DESBLOQUEIO, null, null);
        return LeitoMapaDto.fromLeito(leitoRepository.save(l), null);
    }

    /** Depois da saída do paciente, a limpeza registrada devolve o leito ao mapa como livre. */
    @Transactional
    public LeitoMapaDto liberar(UUID uuid) {
        Leito l = travar(uuid);
        controleDeAcesso.exigir(GERENCIAR, l.getUnidade());
        if (l.getSituacao() != SituacaoLeito.HIGIENIZACAO) {
            throw new ResourceUnprocessableEntityException("Só leito em higienização é liberado; este está " + l.getSituacao() + ".");
        }
        l.setSituacao(SituacaoLeito.LIVRE);
        registrar(l, null, TipoEventoLeito.HIGIENIZACAO_CONCLUIDA, null, null);
        return LeitoMapaDto.fromLeito(leitoRepository.save(l), null);
    }

    /** Os leitos das unidades do escopo (ou de uma), com o paciente de cada ocupado. Fora de uso, só com {@code todos}. */
    @Transactional(readOnly = true)
    public List<LeitoMapaDto> mapa(UUID unidadeId, boolean todos) {
        List<Leito> leitos = visiveis(unidadeId).filter(l -> todos || l.isAtivo()).toList();
        Map<UUID, Internacao> ocupacao = internacaoRepository.findByStatusAndLeito_UuidIn(StatusInternacao.INTERNADO,
                        leitos.stream().map(Leito::getUuid).toList()).stream()
                .collect(Collectors.toMap(i -> i.getLeito().getUuid(), Function.identity(), (a, b) -> a));
        return leitos.stream().map(l -> LeitoMapaDto.fromLeito(l, ocupacao.get(l.getUuid()))).toList();
    }

    @Transactional(readOnly = true)
    public IndicadoresLeitosDto indicadores(UUID unidadeId) {
        List<Leito> leitos = visiveis(unidadeId).filter(Leito::isAtivo).toList();
        long livres = contar(leitos, SituacaoLeito.LIVRE);
        long ocupados = contar(leitos, SituacaoLeito.OCUPADO);
        long higienizacao = contar(leitos, SituacaoLeito.HIGIENIZACAO);
        long bloqueados = contar(leitos, SituacaoLeito.BLOQUEADO);
        long operacionais = leitos.size() - bloqueados;
        List<UUID> unidades = leitos.stream().map(l -> l.getUnidade().getUuid()).distinct().toList();
        List<Internacao> altas = internacaoRepository.findByStatusAndAltaEmAfter(StatusInternacao.ALTA,
                        Instant.now().minus(Duration.ofDays(30))).stream()
                .filter(i -> unidades.contains(i.getUnidade().getUuid())).toList();
        Double media = altas.isEmpty() ? null : Math.round(altas.stream()
                .mapToDouble(i -> Duration.between(i.getAdmitidaEm(), i.getAltaEm()).toHours() / 24.0).average().orElse(0) * 10) / 10.0;
        Double taxa = operacionais == 0 ? null : Math.round(ocupados * 1000.0 / operacionais) / 10.0;
        return new IndicadoresLeitosDto(leitos.size(), livres, ocupados, higienizacao, bloqueados, taxa, media, altas.size());
    }

    // ── Apoio, também usado pela internação ────────────────────────────────────────────────────────

    Leito travar(UUID uuid) {
        Leito l = leitoRepository.findByIdParaAtualizar(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um leito com o id " + uuid + " em nossos registros."));
        ContextoAuditoria.registro(l.getUuid());
        return l;
    }

    void registrar(Leito leito, Internacao internacao, TipoEventoLeito tipo, String texto, Profissional profissional) {
        EventoLeito e = new EventoLeito();
        e.setLeito(leito);
        e.setInternacao(internacao);
        e.setTipo(tipo);
        e.setTexto(texto);
        e.setProfissional(profissional);
        e.setOcorridoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        eventoRepository.save(e);
    }

    private Internacao internacaoAtiva(Leito l) {
        return internacaoRepository.findByStatusAndLeito_UuidIn(StatusInternacao.INTERNADO, List.of(l.getUuid())).stream().findFirst().orElse(null);
    }

    private Stream<Leito> visiveis(UUID unidadeId) {
        List<Leito> leitos = unidadeId != null ? leitoRepository.findByUnidade_UuidOrderByIdentificacaoAsc(unidadeId)
                : leitoRepository.findAllByOrderByIdentificacaoAsc();
        return controleDeAcesso.filtrar(leitos.stream(), l -> Stream.of(l.getUnidade()), VER);
    }

    private static long contar(List<Leito> leitos, SituacaoLeito situacao) {
        return leitos.stream().filter(l -> l.getSituacao() == situacao).count();
    }
}
