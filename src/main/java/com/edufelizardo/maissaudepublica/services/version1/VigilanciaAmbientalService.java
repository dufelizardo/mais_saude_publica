package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AmostraAgua;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.PontoColetaAgua;
import com.edufelizardo.maissaudepublica.models.RiscoAmbiental;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AmostraAguaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ControleRiscoAmbientalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MedidaAmostraAguaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PontoColetaAguaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RiscoAmbientalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AmostraAguaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PontoColetaAguaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RiscoAmbientalDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRiscoAmbiental;
import com.edufelizardo.maissaudepublica.models.enuns.TipoAbastecimentoAgua;
import com.edufelizardo.maissaudepublica.repositories.AmostraAguaRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.PontoColetaAguaRepository;
import com.edufelizardo.maissaudepublica.repositories.RiscoAmbientalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Vigilância ambiental, fatia V4c da Vigilância (ADR-0164, desenho na ADR-0110): a qualidade da água (pontos de coleta e
 * amostras com a conformidade da Portaria GM/MS 888/2021) e o risco ambiental permanente do território. A exposição de um
 * desastre fica nas Emergências (ADR-0161).
 */
@Service
public class VigilanciaAmbientalService {

    private static final String VISTORIAR = ControleVetorialService.VISTORIAR;
    private static final String GERENCIAR = ControleVetorialService.GERENCIAR;
    private static final String[] LEITURA = {VISTORIAR, GERENCIAR, "VIGILANCIA.INVESTIGAR", "INDICADOR.CONSULTAR"};
    /** Os limites da Portaria GM/MS 888/2021 para a água distribuída. */
    static final double CLORO_MINIMO = 0.2;
    static final double CLORO_MAXIMO = 5.0;
    static final double TURBIDEZ_MAXIMA = 5.0;
    static final double FLUOR_MAXIMO = 1.5;

    @Autowired private PontoColetaAguaRepository pontoRepository;
    @Autowired private AmostraAguaRepository amostraRepository;
    @Autowired private RiscoAmbientalRepository riscoRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Água ───────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<PontoColetaAguaDto> pontos() {
        controleDeAcesso.exigirAlguma(LEITURA);
        List<AmostraAgua> amostras = amostraRepository.findTodas();
        return pontoRepository.findTodos().stream().map(p -> {
            List<AmostraAgua> doPonto = amostras.stream().filter(a -> a.getPonto().getUuid().equals(p.getUuid())).toList();
            AmostraAgua ultima = doPonto.isEmpty() ? null : doPonto.get(0);
            Microarea m = p.getMicroarea();
            return new PontoColetaAguaDto(p.getUuid(), p.getNome(), p.getTipo(), p.getLocal(), p.getLatitude(), p.getLongitude(),
                    m != null ? m.getCodigo() : null, m != null ? m.getArea().getNome() : null, p.isAtivo(), ultima != null ? ultima.getDataColeta() : null,
                    ultima != null ? ultima.isConforme() : null, doPonto.size());
        }).toList();
    }

    /** O ponto de coleta: a latitude e a longitude juntas (400); a microárea existente (404). */
    @Transactional
    public PontoColetaAguaDto criarPonto(PontoColetaAguaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        localizacao(dto.getLatitude(), dto.getLongitude());
        PontoColetaAgua p = new PontoColetaAgua(null, dto.getNome().trim(), dto.getTipo(), dto.getLocal().trim(), dto.getLatitude(), dto.getLongitude(),
                microarea(dto.getMicroareaId()), true, Instant.now(), UsuarioAutenticado.cpf());
        pontoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return pontos().stream().filter(x -> x.getUuid().equals(p.getUuid())).findFirst().orElseThrow();
    }

    /** As amostras; pontoId, naoConformes e desde filtram. */
    @Transactional(readOnly = true)
    public List<AmostraAguaDto> amostras(UUID pontoId, Boolean naoConformes, LocalDate desde) {
        controleDeAcesso.exigirAlguma(LEITURA);
        return amostraRepository.findTodas().stream()
                .filter(a -> pontoId == null || a.getPonto().getUuid().equals(pontoId))
                .filter(a -> naoConformes == null || naoConformes != a.isConforme())
                .filter(a -> desde == null || !a.getDataColeta().isBefore(desde))
                .map(VigilanciaAmbientalService::dto).toList();
    }

    /**
     * Registra a amostra do ponto ativo (422): ao menos um parâmetro (400), data não futura (400). A conformidade sai da
     * Portaria GM/MS 888/2021: E. coli ausente; coliformes totais ausentes na água tratada; cloro residual livre de 0,2 a
     * 5 mg/L na água tratada (rede e solução coletiva); turbidez até 5 uT; fluoreto até 1,5 mg/L.
     */
    @Transactional
    public AmostraAguaDto registrarAmostra(AmostraAguaRequestDto dto) {
        controleDeAcesso.exigirAlguma(VISTORIAR);
        PontoColetaAgua p = pontoRepository.findById(dto.getPontoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o ponto de coleta " + dto.getPontoId() + " em nossos registros."));
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O ponto de coleta está inativo.");
        }
        if (dto.getColiformesTotais() == null && dto.getEcoli() == null && dto.getCloroResidual() == null && dto.getTurbidez() == null
                && dto.getFluor() == null) {
            throw new ResourceBadRequestException("Informe ao menos um parâmetro da amostra.");
        }
        LocalDate data = dto.getDataColeta() != null ? dto.getDataColeta() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A coleta não pode ser de data futura.");
        }
        List<String> falhas = naoConformidades(p.getTipo(), dto);
        AmostraAgua a = new AmostraAgua(null, p, data, dto.getColiformesTotais(), dto.getEcoli(), dto.getCloroResidual(), dto.getTurbidez(), dto.getFluor(),
                falhas.isEmpty(), falhas.isEmpty() ? null : String.join("; ", falhas), null, null, Instant.now(), UsuarioAutenticado.cpf());
        amostraRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        ContextoAuditoria.detalhe(falhas.isEmpty() ? "Amostra de água conforme." : "Amostra de água fora do padrão: " + a.getNaoConformidades() + ".");
        return dto(a);
    }

    static List<String> naoConformidades(TipoAbastecimentoAgua tipo, AmostraAguaRequestDto dto) {
        boolean tratada = tipo != TipoAbastecimentoAgua.SOLUCAO_ALTERNATIVA_INDIVIDUAL;
        List<String> falhas = new ArrayList<>();
        if (Boolean.TRUE.equals(dto.getEcoli())) {
            falhas.add("Escherichia coli presente");
        }
        if (tratada && Boolean.TRUE.equals(dto.getColiformesTotais())) {
            falhas.add("coliformes totais presentes (recoleta)");
        }
        if (tratada && dto.getCloroResidual() != null && dto.getCloroResidual() < CLORO_MINIMO) {
            falhas.add("cloro residual livre abaixo de 0,2 mg/L");
        }
        if (dto.getCloroResidual() != null && dto.getCloroResidual() > CLORO_MAXIMO) {
            falhas.add("cloro residual livre acima de 5 mg/L");
        }
        if (dto.getTurbidez() != null && dto.getTurbidez() > TURBIDEZ_MAXIMA) {
            falhas.add("turbidez acima de 5 uT");
        }
        if (dto.getFluor() != null && dto.getFluor() > FLUOR_MAXIMO) {
            falhas.add("fluoreto acima de 1,5 mg/L");
        }
        return falhas;
    }

    /** A medida diante da amostra fora do padrão (a conforme, 422). */
    @Transactional
    public AmostraAguaDto registrarMedida(UUID id, MedidaAmostraAguaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        AmostraAgua a = amostraRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a amostra " + id + " em nossos registros."));
        if (a.isConforme()) {
            throw new ResourceUnprocessableEntityException("A amostra está dentro do padrão.");
        }
        a.setMedida(dto.getMedida().trim());
        a.setMedidaEm(Instant.now());
        amostraRepository.save(a);
        ContextoAuditoria.registro(a.getUuid());
        return dto(a);
    }

    // ── Risco ambiental ────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<RiscoAmbientalDto> riscos(SituacaoRiscoAmbiental situacao) {
        controleDeAcesso.exigirAlguma(LEITURA);
        return riscoRepository.findTodos().stream().filter(r -> situacao == null || r.getSituacao() == situacao).map(VigilanciaAmbientalService::dto).toList();
    }

    /** O risco: a latitude e a longitude juntas (400); a microárea existente (404). Nasce ativo. */
    @Transactional
    public RiscoAmbientalDto registrarRisco(RiscoAmbientalRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        localizacao(dto.getLatitude(), dto.getLongitude());
        RiscoAmbiental r = new RiscoAmbiental(null, dto.getTipo(), dto.getDescricao().trim(), dto.getLocal().trim(), dto.getLatitude(), dto.getLongitude(),
                microarea(dto.getMicroareaId()), dto.getPopulacaoExposta(), SituacaoRiscoAmbiental.ATIVO, null, null, Instant.now(), UsuarioAutenticado.cpf());
        riscoRepository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    /** Controlado, do ativo (422), com o motivo. */
    @Transactional
    public RiscoAmbientalDto controlar(UUID id, ControleRiscoAmbientalRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        RiscoAmbiental r = riscoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o risco ambiental " + id + " em nossos registros."));
        if (r.getSituacao() != SituacaoRiscoAmbiental.ATIVO) {
            throw new ResourceUnprocessableEntityException("O risco já foi controlado.");
        }
        r.setSituacao(SituacaoRiscoAmbiental.CONTROLADO);
        r.setControladoEm(Instant.now());
        r.setMotivoControle(dto.getMotivo().trim());
        riscoRepository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    private static void localizacao(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResourceBadRequestException("A localização pede a latitude e a longitude.");
        }
    }

    private Microarea microarea(UUID id) {
        if (id == null) {
            return null;
        }
        return microareaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a microárea " + id + " em nossos registros."));
    }

    private static AmostraAguaDto dto(AmostraAgua a) {
        return new AmostraAguaDto(a.getUuid(), a.getPonto().getUuid(), a.getPonto().getNome(), a.getPonto().getTipo(), a.getDataColeta(),
                a.getColiformesTotais(), a.getEcoli(), a.getCloroResidual(), a.getTurbidez(), a.getFluor(), a.isConforme(),
                a.getNaoConformidades() == null ? List.of() : Arrays.asList(a.getNaoConformidades().split("; ")), a.getMedida(), a.getMedidaEm());
    }

    private static RiscoAmbientalDto dto(RiscoAmbiental r) {
        Microarea m = r.getMicroarea();
        return new RiscoAmbientalDto(r.getUuid(), r.getTipo(), r.getDescricao(), r.getLocal(), r.getLatitude(), r.getLongitude(),
                m != null ? m.getCodigo() : null, m != null ? m.getArea().getNome() : null, r.getPopulacaoExposta(), r.getSituacao(), r.getControladoEm(),
                r.getMotivoControle(), r.getRegistradoEm());
    }
}
