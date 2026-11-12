package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoQualidade;
import com.edufelizardo.maissaudepublica.models.IncidenteSeguranca;
import com.edufelizardo.maissaudepublica.models.IrregularidadeSanitaria;
import com.edufelizardo.maissaudepublica.models.NaoConformidade;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.VerificacaoEficacia;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AcaoQualidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AnaliseCausaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AndamentoAcaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NaoConformidadeRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.VerificacaoEficaciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NaoConformidadeDtos;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaCausa;
import com.edufelizardo.maissaudepublica.models.enuns.GrauDano;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemNaoConformidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoQualidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIncidente;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoNaoConformidade;
import com.edufelizardo.maissaudepublica.repositories.AcaoQualidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.IncidenteSegurancaRepository;
import com.edufelizardo.maissaudepublica.repositories.IrregularidadeSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.NaoConformidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.SetorRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VerificacaoEficaciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Não conformidades e planos de ação, fatia Q2 da Qualidade (ADR-0204, desenho na ADR-0202): o ciclo PDCA. A não conformidade nasce de
 * uma origem (incidente da Q1, irregularidade da VISA numa unidade pública, auditoria interna, reclamação), recebe a análise de causa (6M)
 * e o plano 5W2H por ciclo; com as ações terminadas, vai à verificação de eficácia, que encerra ou abre o ciclo seguinte.
 */
@Service
public class NaoConformidadeService {

    public static final String GERIR = "QUALIDADE.GERIR";
    static final int DIAS_PARA_VERIFICAR = 30;
    private static final Set<SituacaoNaoConformidade> FECHADAS = EnumSet.of(SituacaoNaoConformidade.ENCERRADA, SituacaoNaoConformidade.CANCELADA);
    private static final Set<SituacaoAcaoQualidade> TERMINADAS = EnumSet.of(SituacaoAcaoQualidade.CONCLUIDA, SituacaoAcaoQualidade.CANCELADA);
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");

    @Autowired private NaoConformidadeRepository repository;
    @Autowired private AcaoQualidadeRepository acaoRepository;
    @Autowired private VerificacaoEficaciaRepository verificacaoRepository;
    @Autowired private IncidenteSegurancaRepository incidenteRepository;
    @Autowired private IrregularidadeSanitariaRepository irregularidadeRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Não conformidade ───────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<NaoConformidadeDtos.NaoConformidade> listar(UUID unidadeId, SituacaoNaoConformidade situacao, OrigemNaoConformidade origem) {
        controleDeAcesso.exigirAlguma(GERIR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(GERIR);
        Map<UUID, List<AcaoQualidade>> acoes = acaoRepository.findTodas().stream().collect(Collectors.groupingBy(a -> a.getNaoConformidade().getUuid()));
        return repository.findTodas().stream()
                .filter(n -> visiveis.isEmpty() || visiveis.get().contains(n.getUnidade().getUuid()))
                .filter(n -> unidadeId == null || n.getUnidade().getUuid().equals(unidadeId))
                .filter(n -> situacao == null || n.getSituacao() == situacao)
                .filter(n -> origem == null || n.getOrigem() == origem)
                .map(n -> dto(n, acoes.getOrDefault(n.getUuid(), List.of()), List.of())).toList();
    }

    @Transactional(readOnly = true)
    public NaoConformidadeDtos.NaoConformidade detalhe(UUID id) {
        NaoConformidade n = paraGerir(id);
        return dto(n);
    }

    /**
     * Registra: a unidade do acesso (403); o setor da unidade (422); o responsável pela matrícula (404); identificada não futura (400). A
     * origem pede o vínculo e o vínculo pede a origem (400): o incidente e a irregularidade são da mesma unidade (422; a irregularidade, de
     * estabelecimento ligado à unidade) e não têm não conformidade em aberto (409). O indicador chega com a Q3 (400).
     */
    @Transactional
    public NaoConformidadeDtos.NaoConformidade registrar(NaoConformidadeRequestDto dto) {
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERIR, unidade);
        if (dto.getOrigem() == OrigemNaoConformidade.INDICADOR) {
            throw new ResourceBadRequestException("A origem indicador chega com os indicadores de qualidade (Q3).");
        }
        boolean pedeIncidente = dto.getOrigem() == OrigemNaoConformidade.INCIDENTE;
        boolean pedeIrregularidade = dto.getOrigem() == OrigemNaoConformidade.INSPECAO_SANITARIA;
        if (pedeIncidente != (dto.getIncidenteId() != null) || pedeIrregularidade != (dto.getIrregularidadeId() != null)) {
            throw new ResourceBadRequestException("A origem incidente pede o incidente, a inspeção sanitária pede a irregularidade, e só elas.");
        }
        List<NaoConformidade> todas = repository.findTodas();
        IncidenteSeguranca incidente = null;
        if (pedeIncidente) {
            incidente = incidenteRepository.findById(dto.getIncidenteId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o incidente " + dto.getIncidenteId() + " em nossos registros."));
            if (!incidente.getUnidade().getUuid().equals(unidade.getUuid())) {
                throw new ResourceUnprocessableEntityException("O incidente é de outra unidade.");
            }
            UUID id = incidente.getUuid();
            if (todas.stream().anyMatch(x -> !FECHADAS.contains(x.getSituacao()) && x.getIncidente() != null && x.getIncidente().getUuid().equals(id))) {
                throw new ResourceConflictException("Este incidente já tem não conformidade em aberto.");
            }
        }
        IrregularidadeSanitaria irregularidade = null;
        if (pedeIrregularidade) {
            irregularidade = irregularidadeRepository.findById(dto.getIrregularidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a irregularidade " + dto.getIrregularidadeId() + " em nossos registros."));
            UnidadeDeSaude ligada = irregularidade.getEstabelecimento().getUnidade();
            if (ligada == null || !ligada.getUuid().equals(unidade.getUuid())) {
                throw new ResourceUnprocessableEntityException("A irregularidade é de estabelecimento que não é esta unidade da rede.");
            }
            UUID id = irregularidade.getUuid();
            if (todas.stream().anyMatch(x -> !FECHADAS.contains(x.getSituacao()) && x.getIrregularidade() != null && x.getIrregularidade().getUuid().equals(id))) {
                throw new ResourceConflictException("Esta irregularidade já tem não conformidade em aberto.");
            }
        }
        Setor setor = null;
        if (dto.getSetorId() != null) {
            setor = setorRepository.findById(dto.getSetorId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o setor " + dto.getSetorId() + " em nossos registros."));
            if (setor.getUnidade() == null || !setor.getUnidade().getUuid().equals(unidade.getUuid())) {
                throw new ResourceUnprocessableEntityException("O setor é de outra unidade.");
            }
        }
        LocalDate identificada = dto.getIdentificadaEm() != null ? dto.getIdentificadaEm() : hoje();
        if (identificada.isAfter(hoje())) {
            throw new ResourceBadRequestException("A identificação não pode ser no futuro.");
        }
        NaoConformidade n = new NaoConformidade();
        n.setNumero(numero(identificada.getYear()));
        n.setUnidade(unidade);
        n.setSetor(setor);
        n.setOrigem(dto.getOrigem());
        n.setIncidente(incidente);
        n.setIrregularidade(irregularidade);
        n.setDescricao(dto.getDescricao().trim());
        n.setCorrecaoImediata(limpo(dto.getCorrecaoImediata()));
        n.setGravidade(dto.getGravidade());
        n.setIdentificadaEm(identificada);
        n.setResponsavel(profissional(dto.getResponsavelMatricula()));
        n.setSituacao(SituacaoNaoConformidade.ABERTA);
        n.setCiclo(1);
        n.setRegistradoEm(Instant.now());
        n.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        repository.save(n);
        ContextoAuditoria.unidade(unidade);
        ContextoAuditoria.registro(n.getUuid());
        ContextoAuditoria.detalhe("Não conformidade " + n.getNumero() + " (" + dto.getOrigem() + ", " + dto.getGravidade() + ").");
        return dto(n);
    }

    /** A análise de causa (6M), enquanto a não conformidade não fechou (422). */
    @Transactional
    public NaoConformidadeDtos.NaoConformidade analisar(UUID id, AnaliseCausaRequestDto dto) {
        NaoConformidade n = paraGerir(id);
        aberta(n);
        n.setAnaliseCausa(dto.getAnaliseCausa().trim());
        n.setCategoriasCausa(dto.getCategorias() == null || dto.getCategorias().isEmpty() ? null
                : dto.getCategorias().stream().distinct().map(Enum::name).collect(Collectors.joining(";")));
        return salvar(n, "Análise de causa registrada.");
    }

    /** Uma ação no ciclo corrente: aberta ou em tratamento (422); o prazo não antes da identificação (400). Põe em tratamento. */
    @Transactional
    public NaoConformidadeDtos.NaoConformidade adicionarAcao(UUID id, AcaoQualidadeRequestDto dto) {
        NaoConformidade n = paraGerir(id);
        if (n.getSituacao() != SituacaoNaoConformidade.ABERTA && n.getSituacao() != SituacaoNaoConformidade.EM_TRATAMENTO) {
            throw new ResourceUnprocessableEntityException("As ações entram com a não conformidade aberta ou em tratamento.");
        }
        if (dto.getPrazo().isBefore(n.getIdentificadaEm())) {
            throw new ResourceBadRequestException("O prazo não pode ser antes da identificação.");
        }
        AcaoQualidade a = new AcaoQualidade();
        a.setNaoConformidade(n);
        a.setCiclo(n.getCiclo());
        a.setOQue(dto.getDescricao().trim());
        a.setPorQue(dto.getJustificativa().trim());
        a.setResponsavel(profissional(dto.getResponsavelMatricula()));
        a.setOnde(limpo(dto.getOnde()));
        a.setPrazo(dto.getPrazo());
        a.setComo(limpo(dto.getComo()));
        a.setCusto(dto.getCusto());
        a.setSituacao(SituacaoAcaoQualidade.PENDENTE);
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        acaoRepository.save(a);
        n.setSituacao(SituacaoNaoConformidade.EM_TRATAMENTO);
        n.setVerificacaoPrevista(null);
        return salvar(n, "Ação do plano: " + a.getOQue() + ".");
    }

    /**
     * O andamento da ação: quem gere na unidade ou o próprio responsável (403 aos outros). Só em andamento ou concluída (400), da ação
     * aberta (422); concluir pede a evidência (400) e a data não futura (400). Terminadas as ações do ciclo com ao menos uma concluída, a não
     * conformidade vai à verificação, prevista para 30 dias.
     */
    @Transactional
    public NaoConformidadeDtos.Acao andamento(UUID acaoId, AndamentoAcaoRequestDto dto) {
        AcaoQualidade a = acaoOu404(acaoId);
        NaoConformidade n = a.getNaoConformidade();
        String cpf = UsuarioAutenticado.cpf();
        boolean responsavel = cpf != null && cpf.equals(a.getResponsavel().getCpf());
        if (!responsavel) {
            controleDeAcesso.exigir(GERIR, n.getUnidade());
        }
        if (dto.getSituacao() != SituacaoAcaoQualidade.EM_ANDAMENTO && dto.getSituacao() != SituacaoAcaoQualidade.CONCLUIDA) {
            throw new ResourceBadRequestException("O andamento é em andamento ou concluída; o cancelamento é da gestão.");
        }
        if (TERMINADAS.contains(a.getSituacao()) || a.getCiclo() != n.getCiclo() || FECHADAS.contains(n.getSituacao())) {
            throw new ResourceUnprocessableEntityException("Esta ação já terminou.");
        }
        if (dto.getSituacao() == SituacaoAcaoQualidade.CONCLUIDA) {
            if (limpo(dto.getEvidencia()) == null) {
                throw new ResourceBadRequestException("Concluir a ação pede a evidência do que foi feito.");
            }
            LocalDate em = dto.getConcluidaEm() != null ? dto.getConcluidaEm() : hoje();
            if (em.isAfter(hoje())) {
                throw new ResourceBadRequestException("A conclusão não pode ser no futuro.");
            }
            a.setEvidencia(dto.getEvidencia().trim());
            a.setConcluidaEm(em);
        }
        a.setSituacao(dto.getSituacao());
        acaoRepository.save(a);
        talvezVerificar(n);
        ContextoAuditoria.registro(n.getUuid());
        ContextoAuditoria.detalhe("Ação " + dto.getSituacao() + ": " + a.getOQue() + ".");
        return dto(a, hoje());
    }

    /** A gestão cancela a ação aberta (422), com o motivo. */
    @Transactional
    public NaoConformidadeDtos.NaoConformidade cancelarAcao(UUID acaoId, String motivo) {
        AcaoQualidade a = acaoOu404(acaoId);
        NaoConformidade n = a.getNaoConformidade();
        controleDeAcesso.exigir(GERIR, n.getUnidade());
        if (TERMINADAS.contains(a.getSituacao()) || a.getCiclo() != n.getCiclo() || FECHADAS.contains(n.getSituacao())) {
            throw new ResourceUnprocessableEntityException("Esta ação já terminou.");
        }
        a.setSituacao(SituacaoAcaoQualidade.CANCELADA);
        a.setMotivoCancelamento(motivo.trim());
        acaoRepository.save(a);
        talvezVerificar(n);
        return salvar(n, "Ação cancelada.");
    }

    /**
     * A verificação de eficácia, só em verificação (422), com a data não futura (400): eficaz encerra; não eficaz pede a observação (400)
     * e abre o ciclo seguinte, em tratamento.
     */
    @Transactional
    public NaoConformidadeDtos.NaoConformidade verificar(UUID id, VerificacaoEficaciaRequestDto dto) {
        NaoConformidade n = paraGerir(id);
        if (n.getSituacao() != SituacaoNaoConformidade.EM_VERIFICACAO) {
            throw new ResourceUnprocessableEntityException("A verificação vem depois de terminadas as ações do ciclo.");
        }
        boolean eficaz = dto.getEficaz();
        if (!eficaz && limpo(dto.getObservacao()) == null) {
            throw new ResourceBadRequestException("A verificação não eficaz pede a observação: o que ainda não se resolveu.");
        }
        LocalDate em = dto.getVerificadaEm() != null ? dto.getVerificadaEm() : hoje();
        if (em.isAfter(hoje())) {
            throw new ResourceBadRequestException("A verificação não pode ser no futuro.");
        }
        verificacaoRepository.save(new VerificacaoEficacia(null, n, n.getCiclo(), eficaz, em, limpo(dto.getObservacao()), UsuarioAutenticado.cpf(), Instant.now()));
        if (eficaz) {
            n.setSituacao(SituacaoNaoConformidade.ENCERRADA);
            n.setEncerradaEm(em);
        } else {
            n.setCiclo(n.getCiclo() + 1);
            n.setSituacao(SituacaoNaoConformidade.EM_TRATAMENTO);
        }
        n.setVerificacaoPrevista(null);
        return salvar(n, eficaz ? "Verificação eficaz: não conformidade encerrada." : "Verificação não eficaz: ciclo " + n.getCiclo() + ".");
    }

    /** Cancela a não conformidade aberta (422), com o motivo; as ações abertas também. */
    @Transactional
    public NaoConformidadeDtos.NaoConformidade cancelar(UUID id, String motivo) {
        NaoConformidade n = paraGerir(id);
        aberta(n);
        for (AcaoQualidade a : acaoRepository.findDaNaoConformidade(n.getUuid())) {
            if (!TERMINADAS.contains(a.getSituacao())) {
                a.setSituacao(SituacaoAcaoQualidade.CANCELADA);
                a.setMotivoCancelamento("Não conformidade cancelada.");
                acaoRepository.save(a);
            }
        }
        n.setSituacao(SituacaoNaoConformidade.CANCELADA);
        n.setMotivoCancelamento(motivo.trim());
        n.setVerificacaoPrevista(null);
        return salvar(n, "Não conformidade cancelada.");
    }

    // ── Minhas ações, sugestões e painel ───────────────────────────────────────────────────────────

    /** As ações abertas de quem está logado, pelo CPF do profissional responsável; sem login, nenhuma. */
    @Transactional(readOnly = true)
    public List<NaoConformidadeDtos.MinhaAcao> minhasAcoes() {
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return List.of();
        }
        LocalDate hoje = hoje();
        return acaoRepository.findTodas().stream()
                .filter(a -> cpf.equals(a.getResponsavel().getCpf()) && !TERMINADAS.contains(a.getSituacao())
                        && a.getCiclo() == a.getNaoConformidade().getCiclo() && !FECHADAS.contains(a.getNaoConformidade().getSituacao()))
                .sorted((x, y) -> x.getPrazo().compareTo(y.getPrazo()))
                .map(a -> new NaoConformidadeDtos.MinhaAcao(a.getNaoConformidade().getUuid(), a.getNaoConformidade().getNumero(),
                        a.getNaoConformidade().getUnidade().getNome(), a.getNaoConformidade().getDescricao(), dto(a, hoje))).toList();
    }

    /**
     * As sugestões, sem abrir nada: o incidente concluído com dano moderado ou maior, ou never event, e a irregularidade aberta da VISA em
     * estabelecimento ligado à unidade, sem não conformidade (fora as canceladas), nas unidades em que se gere.
     */
    @Transactional(readOnly = true)
    public List<NaoConformidadeDtos.Sugestao> sugestoes() {
        controleDeAcesso.exigirAlguma(GERIR);
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(GERIR);
        List<NaoConformidade> todas = repository.findTodas().stream().filter(n -> n.getSituacao() != SituacaoNaoConformidade.CANCELADA).toList();
        Set<UUID> incidentes = todas.stream().filter(n -> n.getIncidente() != null).map(n -> n.getIncidente().getUuid()).collect(Collectors.toSet());
        Set<UUID> irregularidades = todas.stream().filter(n -> n.getIrregularidade() != null).map(n -> n.getIrregularidade().getUuid()).collect(Collectors.toSet());
        Set<GrauDano> danos = EnumSet.of(GrauDano.MODERADO, GrauDano.GRAVE, GrauDano.OBITO);
        List<NaoConformidadeDtos.Sugestao> lista = new ArrayList<>();
        for (IncidenteSeguranca i : incidenteRepository.findTodos()) {
            if (i.getSituacao() == SituacaoIncidente.CONCLUIDA && (i.isNeverEvent() || danos.contains(i.getDano())) && !incidentes.contains(i.getUuid())
                    && (visiveis.isEmpty() || visiveis.get().contains(i.getUnidade().getUuid()))) {
                lista.add(new NaoConformidadeDtos.Sugestao(OrigemNaoConformidade.INCIDENTE, i.getUuid(), "Incidente " + i.getNumero(), i.getUnidade().getUuid(),
                        i.getUnidade().getNome(), i.getTipo().name() + (i.getDano() != null ? ", dano " + i.getDano().name() : ""),
                        LocalDate.ofInstant(i.getOcorridoEm(), ZONA)));
            }
        }
        for (IrregularidadeSanitaria r : irregularidadeRepository.findTodas()) {
            UnidadeDeSaude u = r.getEstabelecimento().getUnidade();
            if (u != null && r.getSituacao() == SituacaoIrregularidade.ABERTA && !irregularidades.contains(r.getUuid())
                    && (visiveis.isEmpty() || visiveis.get().contains(u.getUuid()))) {
                lista.add(new NaoConformidadeDtos.Sugestao(OrigemNaoConformidade.INSPECAO_SANITARIA, r.getUuid(), "Irregularidade sanitária", u.getUuid(),
                        u.getNome(), r.getDescricao(), r.getInspecao().getDataInspecao()));
            }
        }
        return lista;
    }

    /** O painel, só com totais: nas unidades em que se gere ou se consultam indicadores. */
    @Transactional(readOnly = true)
    public NaoConformidadeDtos.Painel painel(UUID unidadeId) {
        controleDeAcesso.exigirAlguma(GERIR, "INDICADOR.CONSULTAR");
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(GERIR, "INDICADOR.CONSULTAR");
        List<NaoConformidade> lista = repository.findTodas().stream()
                .filter(n -> visiveis.isEmpty() || visiveis.get().contains(n.getUnidade().getUuid()))
                .filter(n -> unidadeId == null || n.getUnidade().getUuid().equals(unidadeId))
                .filter(n -> n.getSituacao() != SituacaoNaoConformidade.CANCELADA).toList();
        Set<UUID> ids = lista.stream().map(NaoConformidade::getUuid).collect(Collectors.toSet());
        LocalDate hoje = hoje();
        long atrasadas = acaoRepository.findTodas().stream().filter(a -> ids.contains(a.getNaoConformidade().getUuid()) && atrasada(a, hoje)).count();
        Map<UUID, List<VerificacaoEficacia>> verificacoes = verificacaoRepository.findTodas().stream().filter(v -> ids.contains(v.getNaoConformidade().getUuid()))
                .collect(Collectors.groupingBy(v -> v.getNaoConformidade().getUuid()));
        long naPrimeira = verificacoes.values().stream().filter(l -> l.stream().anyMatch(v -> v.getCiclo() == 1 && v.isEficaz())).count();
        return new NaoConformidadeDtos.Painel(lista.size(), lista.stream().filter(n -> n.getSituacao() != SituacaoNaoConformidade.ENCERRADA).count(), atrasadas,
                verificacoes.size(), verificacoes.isEmpty() ? null : Math.round(naPrimeira * 1000.0 / verificacoes.size()) / 10.0,
                contar(lista, n -> n.getOrigem().name()), contar(lista, n -> n.getGravidade().name()), contar(lista, n -> n.getSituacao().name()));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Terminadas as ações do ciclo, com ao menos uma concluída, vai à verificação prevista para 30 dias. */
    private void talvezVerificar(NaoConformidade n) {
        List<AcaoQualidade> doCiclo = acaoRepository.findDaNaoConformidade(n.getUuid()).stream().filter(a -> a.getCiclo() == n.getCiclo()).toList();
        boolean terminadas = doCiclo.stream().allMatch(a -> TERMINADAS.contains(a.getSituacao()));
        boolean algumaConcluida = doCiclo.stream().anyMatch(a -> a.getSituacao() == SituacaoAcaoQualidade.CONCLUIDA);
        if (n.getSituacao() == SituacaoNaoConformidade.EM_TRATAMENTO && terminadas && algumaConcluida) {
            n.setSituacao(SituacaoNaoConformidade.EM_VERIFICACAO);
            n.setVerificacaoPrevista(hoje().plusDays(DIAS_PARA_VERIFICAR));
            repository.save(n);
        }
    }

    static boolean atrasada(AcaoQualidade a, LocalDate hoje) {
        return !TERMINADAS.contains(a.getSituacao()) && a.getCiclo() == a.getNaoConformidade().getCiclo()
                && !FECHADAS.contains(a.getNaoConformidade().getSituacao()) && a.getPrazo().isBefore(hoje);
    }

    private NaoConformidade paraGerir(UUID id) {
        NaoConformidade n = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a não conformidade " + id + " em nossos registros."));
        controleDeAcesso.exigir(GERIR, n.getUnidade());
        ContextoAuditoria.registro(n.getUuid());
        return n;
    }

    private static void aberta(NaoConformidade n) {
        if (FECHADAS.contains(n.getSituacao())) {
            throw new ResourceUnprocessableEntityException("A não conformidade já foi " + (n.getSituacao() == SituacaoNaoConformidade.ENCERRADA ? "encerrada." : "cancelada."));
        }
    }

    private AcaoQualidade acaoOu404(UUID id) {
        return acaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a ação " + id + " em nossos registros."));
    }

    private Profissional profissional(String matricula) {
        String m = matricula.trim();
        return profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o profissional de matrícula " + m + " em nossos registros."));
    }

    private NaoConformidadeDtos.NaoConformidade salvar(NaoConformidade n, String detalhe) {
        repository.save(n);
        ContextoAuditoria.detalhe(detalhe);
        return dto(n);
    }

    private String numero(int ano) {
        String prefixo = ano + "/";
        return prefixo + String.format(Locale.ROOT, "%06d", repository.countByNumeroStartingWith(prefixo) + 1);
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static List<NaoConformidadeDtos.Contagem> contar(List<NaoConformidade> lista, Function<NaoConformidade, String> chave) {
        Map<String, Long> m = new TreeMap<>(lista.stream().collect(Collectors.groupingBy(chave, Collectors.counting())));
        return m.entrySet().stream().map(e -> new NaoConformidadeDtos.Contagem(e.getKey(), e.getValue())).toList();
    }

    private NaoConformidadeDtos.NaoConformidade dto(NaoConformidade n) {
        return dto(n, acaoRepository.findDaNaoConformidade(n.getUuid()), verificacaoRepository.findByNaoConformidade_UuidOrderByCicloAsc(n.getUuid()));
    }

    private static NaoConformidadeDtos.Acao dto(AcaoQualidade a, LocalDate hoje) {
        Profissional r = a.getResponsavel();
        return new NaoConformidadeDtos.Acao(a.getUuid(), a.getCiclo(), a.getOQue(), a.getPorQue(), r.getMatricula(), r.getNome(), a.getOnde(), a.getPrazo(),
                a.getComo(), a.getCusto(), a.getSituacao(), atrasada(a, hoje), a.getEvidencia(), a.getConcluidaEm(), a.getMotivoCancelamento());
    }

    private static NaoConformidadeDtos.NaoConformidade dto(NaoConformidade n, List<AcaoQualidade> acoes, List<VerificacaoEficacia> verificacoes) {
        LocalDate hoje = hoje();
        Setor s = n.getSetor();
        IncidenteSeguranca i = n.getIncidente();
        IrregularidadeSanitaria r = n.getIrregularidade();
        List<CategoriaCausa> categorias = n.getCategoriasCausa() == null ? List.of()
                : Arrays.stream(n.getCategoriasCausa().split(";")).map(CategoriaCausa::valueOf).toList();
        return new NaoConformidadeDtos.NaoConformidade(n.getUuid(), n.getNumero(), n.getUnidade().getUuid(), n.getUnidade().getNome(),
                s != null ? s.getUuid() : null, s != null ? s.getNome() : null, n.getOrigem(), i != null ? i.getUuid() : null, i != null ? i.getNumero() : null,
                r != null ? r.getUuid() : null, r != null ? r.getDescricao() : null, n.getDescricao(), n.getCorrecaoImediata(), n.getGravidade(),
                n.getIdentificadaEm(), n.getResponsavel().getMatricula(), n.getResponsavel().getNome(), n.getAnaliseCausa(), categorias, n.getSituacao(),
                n.getCiclo(), n.getVerificacaoPrevista(), n.getEncerradaEm(), n.getMotivoCancelamento(), acoes.stream().filter(a -> atrasada(a, hoje)).count(),
                acoes.stream().map(a -> dto(a, hoje)).toList(),
                verificacoes.stream().map(v -> new NaoConformidadeDtos.Verificacao(v.getCiclo(), v.isEficaz(), v.getVerificadaEm(), v.getObservacao())).toList(),
                n.getRegistradoEm());
    }
}
