package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.EsquemaVacinal;
import com.edufelizardo.maissaudepublica.models.Imunobiologico;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EsquemaVacinalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ImunobiologicoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EsquemaVacinalDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ImunobiologicoDto;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoVacinal;
import com.edufelizardo.maissaudepublica.repositories.EsquemaVacinalRepository;
import com.edufelizardo.maissaudepublica.repositories.ImunobiologicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Catálogo de imunobiológicos e calendário vacinal, fatia I1 da Imunização (ADR-0147): lidos por qualquer logado,
 * mantidos por quem gerencia a imunização. O calendário é dado: a mudança do Ministério é um esquema novo, não código.
 */
@Service
public class ImunizacaoCatalogoService {

    public static final String GERENCIAR = "IMUNIZACAO.GERENCIAR";

    @Autowired
    private ImunobiologicoRepository imunobiologicoRepository;

    @Autowired
    private EsquemaVacinalRepository esquemaRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public List<ImunobiologicoDto> imunobiologicos() {
        return imunobiologicoRepository.findAllByOrderByNomeAsc().stream().map(ImunizacaoCatalogoService::dto).toList();
    }

    @Transactional
    public ImunobiologicoDto criarImunobiologico(ImunobiologicoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        String codigo = codigo(dto.getCodigo());
        if (imunobiologicoRepository.findByCodigo(codigo).isPresent()) {
            throw new ResourceConflictException("Já existe o imunobiológico " + codigo + ".");
        }
        Imunobiologico i = new Imunobiologico();
        i.setCodigo(codigo);
        preencher(i, dto);
        i = imunobiologicoRepository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        return dto(i);
    }

    @Transactional
    public ImunobiologicoDto atualizarImunobiologico(UUID uuid, ImunobiologicoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        Imunobiologico i = imunobiologico(uuid);
        String codigo = codigo(dto.getCodigo());
        imunobiologicoRepository.findByCodigo(codigo).filter(o -> !o.getUuid().equals(uuid)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe o imunobiológico " + codigo + ".");
        });
        i.setCodigo(codigo);
        preencher(i, dto);
        ContextoAuditoria.registro(i.getUuid());
        return dto(imunobiologicoRepository.save(i));
    }

    @Transactional(readOnly = true)
    public List<EsquemaVacinalDto> calendario() {
        return esquemaRepository.findTodos().stream().map(ImunizacaoCatalogoService::dto).toList();
    }

    @Transactional
    public EsquemaVacinalDto criarEsquema(EsquemaVacinalRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        EsquemaVacinal e = new EsquemaVacinal();
        preencher(e, dto);
        e = esquemaRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e);
    }

    @Transactional
    public EsquemaVacinalDto atualizarEsquema(UUID uuid, EsquemaVacinalRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        EsquemaVacinal e = esquemaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a dose do calendário " + uuid + " em nossos registros."));
        preencher(e, dto);
        ContextoAuditoria.registro(e.getUuid());
        return dto(esquemaRepository.save(e));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────

    private void preencher(Imunobiologico i, ImunobiologicoRequestDto dto) {
        i.setNome(dto.getNome().trim());
        i.setSigla(dto.getSigla().trim());
        i.setTipo(dto.getTipo());
        i.setVia(dto.getVia());
        i.setVolumeDoseMl(dto.getVolumeDoseMl());
        i.setDosesPorFrasco(dto.getDosesPorFrasco());
        i.setValidadeAposAbertoHoras(dto.getValidadeAposAbertoHoras());
        i.setCodigoSipni(dto.getCodigoSipni() == null || dto.getCodigoSipni().isBlank() ? null : dto.getCodigoSipni().trim());
        i.setAtivo(dto.getAtivo() == null || dto.getAtivo());
    }

    /** As idades em ordem (mínima ≤ recomendada ≤ máxima) e a vigência com o fim depois do início (400). */
    private void preencher(EsquemaVacinal e, EsquemaVacinalRequestDto dto) {
        Integer min = dto.getIdadeMinimaMeses();
        Integer rec = dto.getIdadeRecomendadaMeses();
        Integer max = dto.getIdadeMaximaMeses();
        if ((min != null && rec != null && rec < min) || (rec != null && max != null && max < rec) || (min != null && max != null && max < min)) {
            throw new ResourceBadRequestException("As idades precisam estar em ordem: mínima, recomendada e máxima.");
        }
        if (dto.getVigenteAte() != null && dto.getVigenteAte().isBefore(dto.getVigenteDesde())) {
            throw new ResourceBadRequestException("O fim da vigência não pode ser antes do início.");
        }
        boolean conta = dto.getContaNaSituacao() == null || dto.getContaNaSituacao();
        if (conta && rec == null && dto.getRepeticaoAnos() == null) {
            throw new ResourceBadRequestException("A dose que entra na situação vacinal precisa da idade recomendada (ou da repetição).");
        }
        e.setImunobiologico(imunobiologico(dto.getImunobiologicoId()));
        e.setDose(dto.getDose());
        e.setPublico(dto.getPublico());
        e.setIdadeMinimaMeses(min);
        e.setIdadeRecomendadaMeses(rec);
        e.setIdadeMaximaMeses(max);
        e.setIntervaloMinimoDias(dto.getIntervaloMinimoDias());
        e.setRepeticaoAnos(dto.getRepeticaoAnos());
        e.setContaNaSituacao(conta);
        e.setObservacao(dto.getObservacao() == null || dto.getObservacao().isBlank() ? null : dto.getObservacao().trim());
        e.setVigenteDesde(dto.getVigenteDesde());
        e.setVigenteAte(dto.getVigenteAte());
        e.setOrdem(dto.getOrdem() == null ? 999 : dto.getOrdem());
        e.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        // A cobertura (ADR-0149): vazio mantém; a dose que mede pede a meta, a idade recomendada e o público criança.
        boolean mede = dto.getMedeCobertura() != null ? dto.getMedeCobertura() : e.isMedeCobertura();
        BigDecimal meta = dto.getMetaCobertura() != null ? dto.getMetaCobertura() : e.getMetaCobertura();
        if (mede && meta == null) {
            throw new ResourceBadRequestException("Informe a meta da cobertura (em %).");
        }
        if (mede && (rec == null || dto.getPublico() != PublicoVacinal.CRIANCA)) {
            throw new ResourceBadRequestException("Só a dose da criança com idade recomendada mede a cobertura (a coorte sai dessa idade).");
        }
        e.setMedeCobertura(mede);
        e.setMetaCobertura(meta);
    }

    private Imunobiologico imunobiologico(UUID uuid) {
        return imunobiologicoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o imunobiológico " + uuid + " em nossos registros."));
    }

    private static String codigo(String codigo) {
        return codigo.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    static ImunobiologicoDto dto(Imunobiologico i) {
        return new ImunobiologicoDto(i.getUuid(), i.getCodigo(), i.getNome(), i.getSigla(), i.getTipo(), i.getVia(), i.getVolumeDoseMl(),
                i.getDosesPorFrasco(), i.getValidadeAposAbertoHoras(), i.getCodigoSipni(), i.isAtivo());
    }

    static EsquemaVacinalDto dto(EsquemaVacinal e) {
        Imunobiologico i = e.getImunobiologico();
        return new EsquemaVacinalDto(e.getUuid(), i.getUuid(), i.getNome(), i.getSigla(), e.getDose(), e.getDose().rotulo(), e.getPublico(),
                e.getIdadeMinimaMeses(), e.getIdadeRecomendadaMeses(), e.getIdadeMaximaMeses(), e.getIntervaloMinimoDias(), e.getRepeticaoAnos(),
                e.isContaNaSituacao(), e.getObservacao(), e.getVigenteDesde(), e.getVigenteAte(), e.getOrdem(), e.isAtivo(),
                e.isMedeCobertura(), e.getMetaCobertura());
    }
}
