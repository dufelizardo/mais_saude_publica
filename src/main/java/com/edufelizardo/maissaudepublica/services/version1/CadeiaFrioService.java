package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CamaraRefrigeracao;
import com.edufelizardo.maissaudepublica.models.FrascoAberto;
import com.edufelizardo.maissaudepublica.models.LeituraTemperatura;
import com.edufelizardo.maissaudepublica.models.LoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AvaliacaoLoteRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CamaraRefrigeracaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LeituraTemperaturaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CadeiaFrioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CamaraRefrigeracaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LeituraTemperaturaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LoteImunobiologicoDto;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoEncerramentoFrasco;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoPerdaImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLoteImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoImunobiologico;
import com.edufelizardo.maissaudepublica.models.enuns.TurnoLeituraTemperatura;
import com.edufelizardo.maissaudepublica.repositories.CamaraRefrigeracaoRepository;
import com.edufelizardo.maissaudepublica.repositories.FrascoAbertoRepository;
import com.edufelizardo.maissaudepublica.repositories.LeituraTemperaturaRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteImunobiologicoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Cadeia de frio da sala de vacina, fatia I2 da Imunização (ADR-0148): a câmara de refrigeração com a faixa (padrão de
 * 2 a 8 °C), duas leituras por dia (atual, mínima e máxima) e a excursão: a leitura fora da faixa põe sob suspeita os
 * lotes da câmara, que não se aplicam até a coordenação avaliar (liberar com o parecer, ou inutilizar como perda física).
 */
@Service
public class CadeiaFrioService {

    static final BigDecimal MINIMA_PADRAO = new BigDecimal("2.0");
    static final BigDecimal MAXIMA_PADRAO = new BigDecimal("8.0");
    /** Depois destas horas, o turno sem leitura vira pendência. */
    static final LocalTime FIM_MANHA = LocalTime.NOON;
    static final LocalTime FIM_TARDE = LocalTime.of(18, 0);

    @Autowired private CamaraRefrigeracaoRepository camaraRepository;
    @Autowired private LeituraTemperaturaRepository leituraRepository;
    @Autowired private LoteImunobiologicoRepository loteRepository;
    @Autowired private FrascoAbertoRepository frascoRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private EstoqueVacinaService estoqueService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Painel ─────────────────────────────────────────────────────────────────────────────────

    /** As câmaras com a última leitura, as leituras do dia (padrão: hoje), os turnos sem leitura e os lotes sob suspeita. */
    @Transactional(readOnly = true)
    public CadeiaFrioDto painel(UUID unidadeId, LocalDate data) {
        UnidadeDeSaude u = unidade(unidadeId);
        estoqueService.exigirLeitura(u);
        LocalDate dia = data == null ? LocalDate.now() : data;
        LocalDateTime agora = LocalDateTime.now();
        List<CamaraRefrigeracao> camaras = camaraRepository.findByUnidade_UuidOrderByNomeAsc(unidadeId);
        List<LeituraTemperatura> leituras = leituraRepository.findDaUnidadeEntre(unidadeId, dia.atStartOfDay(), dia.plusDays(1).atStartOfDay());
        List<String> pendencias = new ArrayList<>();
        for (CamaraRefrigeracao c : camaras) {
            if (!c.isAtiva()) {
                continue;
            }
            for (TurnoLeituraTemperatura t : TurnoLeituraTemperatura.values()) {
                LocalDateTime limite = dia.atTime(t == TurnoLeituraTemperatura.MANHA ? FIM_MANHA : FIM_TARDE);
                boolean lida = leituras.stream().anyMatch(l -> l.getCamara().getUuid().equals(c.getUuid()) && l.getTurno() == t);
                if (!lida && agora.isAfter(limite)) {
                    pendencias.add(c.getNome() + ": falta a leitura da " + (t == TurnoLeituraTemperatura.MANHA ? "manhã" : "tarde") + ".");
                }
            }
        }
        LocalDate hoje = agora.toLocalDate();
        List<LoteImunobiologicoDto> sobSuspeita = loteRepository.findDaUnidade(unidadeId).stream()
                .filter(l -> l.getSituacao() == SituacaoLoteImunobiologico.SOB_SUSPEITA).map(l -> EstoqueVacinaService.dto(l, hoje)).toList();
        return new CadeiaFrioDto(u.getUuid(), dia, camaras.stream().map(this::dto).toList(), leituras.stream().map(l -> dto(l, 0)).toList(),
                pendencias, sobSuspeita);
    }

    // ── Câmaras ────────────────────────────────────────────────────────────────────────────────

    @Transactional
    public CamaraRefrigeracaoDto criarCamara(CamaraRefrigeracaoRequestDto dto) {
        UnidadeDeSaude u = unidade(dto.getUnidadeId());
        controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, u);
        estoqueService.exigirSala(u);
        String nome = dto.getNome().trim();
        if (camaraRepository.existsByUnidade_UuidAndNomeIgnoreCase(u.getUuid(), nome)) {
            throw new ResourceConflictException("A unidade já tem a câmara " + nome + ".");
        }
        CamaraRefrigeracao c = new CamaraRefrigeracao();
        c.setUnidade(u);
        c.setNome(nome);
        faixa(c, dto);
        c.setAtiva(true);
        c = camaraRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /** Ajusta o nome e a faixa; a câmara desativada sai das pendências de leitura. */
    @Transactional
    public CamaraRefrigeracaoDto atualizarCamara(UUID uuid, CamaraRefrigeracaoRequestDto dto) {
        CamaraRefrigeracao c = camara(uuid);
        controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, c.getUnidade());
        String nome = dto.getNome().trim();
        if (!c.getNome().equalsIgnoreCase(nome) && camaraRepository.existsByUnidade_UuidAndNomeIgnoreCase(c.getUnidade().getUuid(), nome)) {
            throw new ResourceConflictException("A unidade já tem a câmara " + nome + ".");
        }
        c.setNome(nome);
        faixa(c, dto);
        if (dto.getAtiva() != null) {
            c.setAtiva(dto.getAtiva());
        }
        ContextoAuditoria.registro(c.getUuid());
        return dto(camaraRepository.save(c));
    }

    // ── Leituras ───────────────────────────────────────────────────────────────────────────────

    /**
     * Leitura da câmara: a mínima não passa da atual, nem a atual da máxima (400). Fora da faixa é excursão: os lotes
     * disponíveis da câmara ficam sob suspeita.
     */
    @Transactional
    public LeituraTemperaturaDto registrarLeitura(UUID camaraId, LeituraTemperaturaRequestDto dto) {
        CamaraRefrigeracao c = camara(camaraId);
        controleDeAcesso.exigir(VacinacaoService.REGISTRAR, c.getUnidade());
        if (!c.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A câmara " + c.getNome() + " está desativada.");
        }
        if (dto.getTemperaturaMinima().compareTo(dto.getTemperaturaAtual()) > 0 || dto.getTemperaturaAtual().compareTo(dto.getTemperaturaMaxima()) > 0) {
            throw new ResourceBadRequestException("A temperatura atual precisa estar entre a mínima e a máxima do período.");
        }
        LocalDateTime quando = dto.getLidaEm() != null ? dto.getLidaEm() : LocalDateTime.now();
        if (quando.isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new ResourceBadRequestException("A leitura não pode ser no futuro.");
        }
        Profissional profissional = null;
        if (dto.getProfissionalMatricula() != null && !dto.getProfissionalMatricula().isBlank()) {
            profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula().trim()).orElseThrow(() ->
                    new ResourceNotFoundException("Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula()
                            + " em nossos registros."));
        }
        LeituraTemperatura l = new LeituraTemperatura();
        l.setCamara(c);
        l.setLidaEm(quando);
        l.setTurno(dto.getTurno() != null ? dto.getTurno() : quando.toLocalTime().isBefore(FIM_MANHA) ? TurnoLeituraTemperatura.MANHA : TurnoLeituraTemperatura.TARDE);
        l.setTemperaturaAtual(dto.getTemperaturaAtual());
        l.setTemperaturaMinima(dto.getTemperaturaMinima());
        l.setTemperaturaMaxima(dto.getTemperaturaMaxima());
        l.setExcursao(dto.getTemperaturaMinima().compareTo(c.getTemperaturaMinima()) < 0 || dto.getTemperaturaMaxima().compareTo(c.getTemperaturaMaxima()) > 0);
        l.setProfissional(profissional);
        l.setObservacao(dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim());
        l.setRegistradoEm(Instant.now());
        l.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        l = leituraRepository.save(l);
        int suspeitos = 0;
        if (l.isExcursao()) {
            for (LoteImunobiologico lote : loteRepository.findByCamara_UuidAndSituacao(c.getUuid(), SituacaoLoteImunobiologico.DISPONIVEL)) {
                if (lote.getDoses() <= 0) {
                    continue;
                }
                lote.setSituacao(SituacaoLoteImunobiologico.SOB_SUSPEITA);
                lote.setSobSuspeitaDesde(Instant.now());
                lote.setParecer(null);
                lote.setAvaliadoEm(null);
                lote.setAvaliadoPorCpf(null);
                loteRepository.save(lote);
                suspeitos++;
            }
        }
        ContextoAuditoria.registro(l.getUuid());
        return dto(l, suspeitos);
    }

    // ── Avaliação do lote sob suspeita ─────────────────────────────────────────────────────────

    /**
     * A coordenação avalia o lote sob suspeita (com a consulta ao fabricante ou à rede de frio): libera, com o parecer,
     * ou inutiliza, e o saldo inteiro sai como perda física por excursão de temperatura.
     */
    @Transactional
    public LoteImunobiologicoDto avaliar(UUID loteId, AvaliacaoLoteRequestDto dto) {
        LoteImunobiologico l = estoqueService.loteComTrava(loteId);
        controleDeAcesso.exigir(ImunizacaoCatalogoService.GERENCIAR, l.getUnidade());
        String decisao = dto.getDecisao().trim().toUpperCase(Locale.ROOT);
        if (!decisao.equals("LIBERAR") && !decisao.equals("INUTILIZAR")) {
            throw new ResourceBadRequestException("A decisão é LIBERAR ou INUTILIZAR.");
        }
        if (l.getSituacao() != SituacaoLoteImunobiologico.SOB_SUSPEITA) {
            throw new ResourceUnprocessableEntityException("O lote " + l.getNumeroLote() + " não está sob suspeita.");
        }
        l.setParecer(dto.getParecer().trim());
        l.setAvaliadoEm(Instant.now());
        l.setAvaliadoPorCpf(UsuarioAutenticado.cpf());
        if (decisao.equals("LIBERAR")) {
            l.setSituacao(SituacaoLoteImunobiologico.DISPONIVEL);
        } else {
            LocalDateTime agora = LocalDateTime.now();
            for (FrascoAberto f : frascoRepository.findByLote_UuidAndEncerradoEmIsNullOrderByAbertoEmAsc(l.getUuid())) {
                f.setEncerradoEm(agora);
                f.setMotivoEncerramento(MotivoEncerramentoFrasco.VENCIDO);
                frascoRepository.save(f);
            }
            l.setSituacao(SituacaoLoteImunobiologico.INUTILIZADO);
            int doses = l.getDoses();
            l.setDoses(0);
            if (doses > 0) {
                estoqueService.lancar(l, TipoMovimentacaoImunobiologico.PERDA_FISICA, -doses, MotivoPerdaImunobiologico.EXCURSAO_TEMPERATURA,
                        l.getParecer(), null, null);
            }
        }
        ContextoAuditoria.registro(l.getUuid());
        return EstoqueVacinaService.dto(loteRepository.save(l), LocalDate.now());
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private static void faixa(CamaraRefrigeracao c, CamaraRefrigeracaoRequestDto dto) {
        BigDecimal min = dto.getTemperaturaMinima() != null ? dto.getTemperaturaMinima() : MINIMA_PADRAO;
        BigDecimal max = dto.getTemperaturaMaxima() != null ? dto.getTemperaturaMaxima() : MAXIMA_PADRAO;
        if (min.compareTo(max) >= 0) {
            throw new ResourceBadRequestException("A temperatura mínima da faixa precisa ser menor que a máxima.");
        }
        c.setTemperaturaMinima(min);
        c.setTemperaturaMaxima(max);
    }

    private CamaraRefrigeracao camara(UUID uuid) {
        return camaraRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a câmara " + uuid + " em nossos registros."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private CamaraRefrigeracaoDto dto(CamaraRefrigeracao c) {
        LeituraTemperaturaDto ultima = leituraRepository.findFirstByCamara_UuidOrderByLidaEmDesc(c.getUuid()).map(l -> dto(l, 0)).orElse(null);
        return new CamaraRefrigeracaoDto(c.getUuid(), c.getUnidade().getUuid(), c.getNome(), c.getTemperaturaMinima(), c.getTemperaturaMaxima(),
                c.isAtiva(), ultima, loteRepository.countByCamara_UuidAndSituacao(c.getUuid(), SituacaoLoteImunobiologico.SOB_SUSPEITA));
    }

    private static LeituraTemperaturaDto dto(LeituraTemperatura l, int suspeitos) {
        CamaraRefrigeracao c = l.getCamara();
        return new LeituraTemperaturaDto(l.getUuid(), c.getUuid(), c.getNome(), l.getLidaEm(), l.getTurno(), l.getTemperaturaAtual(),
                l.getTemperaturaMinima(), l.getTemperaturaMaxima(), l.isExcursao(), l.getProfissional() != null ? l.getProfissional().getNome() : null,
                l.getObservacao(), suspeitos);
    }
}
