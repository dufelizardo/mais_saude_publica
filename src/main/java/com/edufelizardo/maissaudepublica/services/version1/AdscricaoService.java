package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Adscricao;
import com.edufelizardo.maissaudepublica.models.Area;
import com.edufelizardo.maissaudepublica.models.Equipe;
import com.edufelizardo.maissaudepublica.models.Imovel;
import com.edufelizardo.maissaudepublica.models.MembroNucleo;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdscricaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoAtribuicaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdscricaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemAdscricao;
import com.edufelizardo.maissaudepublica.repositories.AdscricaoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtribuicaoMicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.EquipeRepository;
import com.edufelizardo.maissaudepublica.repositories.ImovelRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroNucleoRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.NucleoFamiliarRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ResponsabilidadeTerritorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adscrição e cobertura, fatia F3b (ADR-0128). A adscrição pelo território é calculada: quem mora num domicílio de área
 * com equipe responsável fica adscrito a ela, na microárea do domicílio; entrar, sair, mudar ou trocar a equipe da área
 * fecha e reabre, com histórico. A manual (sem domicílio no território, ou por escolha) tem motivo e não é trocada pela
 * calculada. Ver a equipe de referência é de quem consulta pacientes; registrar a manual pede o cadastro do território
 * na unidade da equipe.
 */
@Service
public class AdscricaoService {

    static final String[] LER = {"PACIENTE.CONSULTAR", CadastroTerritorialService.CADASTRAR, TerritorioService.GERENCIAR};

    @Autowired
    private AdscricaoRepository adscricaoRepository;

    @Autowired
    private MembroNucleoRepository membroRepository;

    @Autowired
    private ResponsabilidadeTerritorialRepository responsabilidadeRepository;

    @Autowired
    private AtribuicaoMicroareaRepository atribuicaoRepository;

    @Autowired
    private MicroareaRepository microareaRepository;

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private NucleoFamiliarRepository nucleoRepository;

    @Autowired
    private EquipeRepository equipeRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private EscopoTerritorial escopo;

    // ── Cálculo pelo território ────────────────────────────────────────────────────────────────────

    /**
     * Acerta a adscrição territorial do paciente: a equipe e a microárea do domicílio onde ele mora hoje (ou nenhuma). A
     * manual vigente fica como está.
     */
    void recalcular(Paciente p, LocalDate data, String motivo) {
        Adscricao atual = adscricaoRepository.findByPaciente_UuidAndFimIsNull(p.getUuid()).orElse(null);
        if (atual != null && atual.getOrigem() == OrigemAdscricao.MANUAL) {
            return;
        }
        Optional<MembroNucleo> mora = membroRepository.findByPaciente_UuidAndFimIsNull(p.getUuid());
        Microarea microarea = mora.map(m -> m.getNucleo().getDomicilio().getImovel().getMicroarea()).orElse(null);
        Equipe equipe = microarea == null ? null : responsabilidadeRepository.findByArea_UuidAndFimIsNull(microarea.getArea().getUuid())
                .map(r -> r.getEquipe()).orElse(null);
        if (atual != null && equipe != null && atual.getEquipe().getUuid().equals(equipe.getUuid())
                && atual.getMicroarea() != null && atual.getMicroarea().getUuid().equals(microarea.getUuid())) {
            return;
        }
        if (atual != null) {
            atual.setFim(data.isBefore(atual.getInicio()) ? atual.getInicio() : data);
            atual.setMotivoFim(limitar(motivo));
            adscricaoRepository.saveAndFlush(atual);
        }
        if (equipe != null) {
            adscricaoRepository.save(new Adscricao(null, p, equipe, microarea, OrigemAdscricao.TERRITORIO, null, data, null, null,
                    UsuarioAutenticado.cpf()));
        }
    }

    /** Todos os moradores da área (a equipe dela mudou). */
    void recalcularArea(Area a, LocalDate data, String motivo) {
        for (MembroNucleo m : membroRepository.findVigentesDaArea(a.getUuid())) {
            recalcular(m.getPaciente(), data, motivo);
        }
    }

    /** Os moradores do imóvel (ele mudou de microárea). */
    void recalcularImovel(Imovel i, LocalDate data, String motivo) {
        for (MembroNucleo m : membroRepository.findVigentesDoImovel(i.getUuid())) {
            recalcular(m.getPaciente(), data, motivo);
        }
    }

    // ── Leitura ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AdscricaoDto.DoPaciente doPaciente(UUID pacienteId) {
        controleDeAcesso.exigirAlguma(LER);
        Paciente p = paciente(pacienteId);
        ContextoAuditoria.paciente(p.getUuid());
        return montar(p);
    }

    /**
     * A cobertura da equipe: pessoas adscritas (pelo território e manuais), famílias e imóveis da área, e por microárea as
     * pessoas cadastradas contra a população de referência. Só números.
     */
    @Transactional(readOnly = true)
    public AdscricaoDto.Cobertura cobertura(UUID equipeId) {
        Equipe e = equipeRepository.findById(equipeId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + equipeId + " em nossos registros."));
        List<Adscricao> adscritos = adscricaoRepository.findByEquipe_UuidAndFimIsNull(e.getUuid());
        int manuais = (int) adscritos.stream().filter(a -> a.getOrigem() == OrigemAdscricao.MANUAL).count();
        Area area = responsabilidadeRepository.findByEquipe_UuidAndFimIsNull(e.getUuid()).map(r -> r.getArea()).orElse(null);
        if (area == null) {
            return new AdscricaoDto.Cobertura(e.getUuid(), e.getNome(), null, null, adscritos.size(), manuais, 0, 0, 0, List.of());
        }
        Map<UUID, Integer> imoveis = contagem(imovelRepository.contarPorMicroarea());
        Map<UUID, Integer> familias = contagem(nucleoRepository.contarVigentesPorMicroarea());
        Map<UUID, Integer> pessoas = contagem(membroRepository.contarVigentesPorMicroarea());
        List<AdscricaoDto.MicroareaCobertura> micro = microareaRepository.findByArea_UuidOrderByCodigoAsc(area.getUuid()).stream().map(m -> {
            int n = pessoas.getOrDefault(m.getUuid(), 0);
            Integer cobertura = m.getPopulacaoReferencia() == null || m.getPopulacaoReferencia() == 0 ? null
                    : (int) Math.round(100.0 * n / m.getPopulacaoReferencia());
            return new AdscricaoDto.MicroareaCobertura(m.getUuid(), m.getCodigo(), m.getPopulacaoReferencia(), n,
                    familias.getOrDefault(m.getUuid(), 0), imoveis.getOrDefault(m.getUuid(), 0),
                    atribuicaoRepository.findByMicroarea_UuidAndFimIsNull(m.getUuid()).map(x -> x.getMembro().getProfissional().getNome()).orElse(null),
                    cobertura);
        }).toList();
        return new AdscricaoDto.Cobertura(e.getUuid(), e.getNome(), area.getUuid(), area.getCodigo(), adscritos.size(), manuais,
                micro.stream().mapToInt(AdscricaoDto.MicroareaCobertura::getFamilias).sum(),
                micro.stream().mapToInt(AdscricaoDto.MicroareaCobertura::getImoveis).sum(),
                (int) micro.stream().filter(m -> m.getImoveis() == 0).count(), micro);
    }

    // ── Adscrição manual ───────────────────────────────────────────────────────────────────────────

    /** Adscreve o paciente à equipe à mão, com motivo; a vigente (de qualquer origem) se encerra na data. */
    @Transactional
    public AdscricaoDto.DoPaciente adscrever(UUID pacienteId, AdscricaoRequestDto dto) {
        Paciente p = paciente(pacienteId);
        Equipe e = equipeRepository.findById(dto.getEquipeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma equipe com o id " + dto.getEquipeId() + " em nossos registros."));
        controleDeAcesso.exigir(CadastroTerritorialService.CADASTRAR, e.getUnidade());
        escopo.exigir(e);
        if (!e.isAtiva()) {
            throw new ResourceUnprocessableEntityException("A equipe " + e.getNome() + " está inativa.");
        }
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        String motivo = dto.getMotivo().trim();
        adscricaoRepository.findByPaciente_UuidAndFimIsNull(p.getUuid()).ifPresent(atual -> {
            if (inicio.isBefore(atual.getInicio())) {
                throw new ResourceBadRequestException("A nova adscrição não pode começar antes da atual (" + atual.getInicio() + ").");
            }
            atual.setFim(inicio);
            atual.setMotivoFim(limitar("Adscrição manual à equipe " + e.getNome() + ": " + motivo));
            adscricaoRepository.saveAndFlush(atual);
        });
        adscricaoRepository.save(new Adscricao(null, p, e, null, OrigemAdscricao.MANUAL, motivo, inicio, null, null, UsuarioAutenticado.cpf()));
        ContextoAuditoria.paciente(p.getUuid());
        ContextoAuditoria.detalhe("Adscrição manual à equipe " + e.getNome() + ".");
        return montar(p);
    }

    /** Encerra a adscrição manual; o paciente volta à do território, se mora em domicílio de área com equipe. */
    @Transactional
    public AdscricaoDto.DoPaciente encerrar(UUID pacienteId, EncerramentoAtribuicaoRequestDto dto) {
        Paciente p = paciente(pacienteId);
        Adscricao atual = adscricaoRepository.findByPaciente_UuidAndFimIsNull(p.getUuid())
                .orElseThrow(() -> new ResourceUnprocessableEntityException(p.getNome() + " não tem adscrição vigente."));
        controleDeAcesso.exigir(CadastroTerritorialService.CADASTRAR, atual.getEquipe().getUnidade());
        escopo.exigir(atual.getEquipe());
        if (atual.getOrigem() != OrigemAdscricao.MANUAL) {
            throw new ResourceUnprocessableEntityException("A adscrição vem do território: ela muda com a família (saída ou mudança) ou com a equipe da área.");
        }
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(atual.getInicio()) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O fim precisa ser entre o início (" + atual.getInicio() + ") e hoje.");
        }
        atual.setFim(fim);
        atual.setMotivoFim(limitar(dto.getMotivo().trim()));
        adscricaoRepository.saveAndFlush(atual);
        recalcular(p, fim, "Fim da adscrição manual");
        ContextoAuditoria.paciente(p.getUuid());
        return montar(p);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** As adscrições vigentes da equipe (para os elegíveis dos programas). */
    List<Adscricao> vigentesDaEquipe(UUID equipeId) {
        return adscricaoRepository.findByEquipe_UuidAndFimIsNull(equipeId);
    }

    String acsDaMicroarea(Microarea m) {
        return m == null ? null : atribuicaoRepository.findByMicroarea_UuidAndFimIsNull(m.getUuid())
                .map(x -> x.getMembro().getProfissional().getNome()).orElse(null);
    }

    private AdscricaoDto.DoPaciente montar(Paciente p) {
        List<Adscricao> todas = adscricaoRepository.findByPaciente_UuidOrderByInicioDesc(p.getUuid());
        Adscricao atual = todas.stream().filter(Adscricao::vigente).findFirst().orElse(null);
        Optional<MembroNucleo> mora = membroRepository.findByPaciente_UuidAndFimIsNull(p.getUuid());
        String domicilio = mora.map(m -> CadastroTerritorialService.textoEndereco(m.getNucleo().getDomicilio().getImovel().getEndereco(),
                m.getNucleo().getDomicilio().getIdentificacao())).orElse(null);
        Microarea microarea = atual != null && atual.getMicroarea() != null ? atual.getMicroarea()
                : mora.map(m -> m.getNucleo().getDomicilio().getImovel().getMicroarea()).orElse(null);
        return new AdscricaoDto.DoPaciente(p.getUuid(), atual == null ? null : item(atual),
                todas.stream().filter(a -> !a.vigente()).map(AdscricaoService::item).toList(), domicilio,
                microarea == null ? null : microarea.getArea().getUuid(), microarea == null ? null : microarea.getArea().getCodigo(),
                microarea == null ? null : microarea.getCodigo(), acsDaMicroarea(microarea));
    }

    private static AdscricaoDto.Item item(Adscricao a) {
        Equipe e = a.getEquipe();
        return new AdscricaoDto.Item(a.getUuid(), e.getUuid(), e.getNome(), e.getTipo(), e.getUnidade().getUuid(), e.getUnidade().getNome(),
                a.getMicroarea() != null ? a.getMicroarea().getCodigo() : null, a.getOrigem(), a.getMotivo(), a.getInicio(), a.getFim(), a.getMotivoFim());
    }

    private Paciente paciente(UUID uuid) {
        return pacienteRepository.findById(Objects.requireNonNull(uuid)).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }

    private static Map<UUID, Integer> contagem(List<Object[]> linhas) {
        return linhas.stream().collect(Collectors.toMap(l -> (UUID) l[0], l -> ((Number) l[1]).intValue()));
    }

    private static String limitar(String texto) {
        return texto == null ? null : texto.length() > 500 ? texto.substring(0, 500) : texto;
    }
}
