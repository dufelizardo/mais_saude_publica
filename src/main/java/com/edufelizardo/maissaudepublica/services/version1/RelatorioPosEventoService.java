package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.AtivacaoCapacidade;
import com.edufelizardo.maissaudepublica.models.AtivacaoPlano;
import com.edufelizardo.maissaudepublica.models.AvaliacaoImpacto;
import com.edufelizardo.maissaudepublica.models.Emergencia;
import com.edufelizardo.maissaudepublica.models.Mobilizacao;
import com.edufelizardo.maissaudepublica.models.MudancaNivelEmergencia;
import com.edufelizardo.maissaudepublica.models.PlanoContingencia;
import com.edufelizardo.maissaudepublica.models.RecomendacaoRelatorio;
import com.edufelizardo.maissaudepublica.models.RelatorioPosEvento;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RelatorioPosEventoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PlanoContingenciaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.RelatorioPosEventoDto;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAcaoEmergencial;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoEmergencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPlanoContingencia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoRelatorioPosEvento;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMobilizacao;
import com.edufelizardo.maissaudepublica.repositories.AcaoEmergencialRepository;
import com.edufelizardo.maissaudepublica.repositories.AtivacaoCapacidadeRepository;
import com.edufelizardo.maissaudepublica.repositories.AtivacaoPlanoRepository;
import com.edufelizardo.maissaudepublica.repositories.AvaliacaoImpactoRepository;
import com.edufelizardo.maissaudepublica.repositories.EmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.MobilizacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.MudancaNivelEmergenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.PlanoContingenciaRepository;
import com.edufelizardo.maissaudepublica.repositories.RelatorioPosEventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * O relatório pós-evento, fatia E5 das Emergências (ADR-0159): o texto da equipe (resumo, problemas, lições,
 * recomendações) e os números calculados do que foi registrado. Começa na desmobilização ou depois de encerrada;
 * finalizado não muda. A recomendação que aponta um plano usado gera a revisão dele (E2, ADR-0157).
 */
@Service
public class RelatorioPosEventoService {

    @Autowired private RelatorioPosEventoRepository repository;
    @Autowired private EmergenciaRepository emergenciaRepository;
    @Autowired private MudancaNivelEmergenciaRepository mudancaRepository;
    @Autowired private AcaoEmergencialRepository acaoRepository;
    @Autowired private MobilizacaoRepository mobilizacaoRepository;
    @Autowired private AtivacaoCapacidadeRepository capacidadeRepository;
    @Autowired private AvaliacaoImpactoRepository avaliacaoRepository;
    @Autowired private AtivacaoPlanoRepository ativacaoPlanoRepository;
    @Autowired private PlanoContingenciaRepository planoRepository;
    @Autowired private PlanoContingenciaService planoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    /** O relatório (sem rascunho, só os números). */
    @Transactional(readOnly = true)
    public RelatorioPosEventoDto buscar(UUID emergenciaId) {
        Emergencia em = emergencia(emergenciaId);
        return dto(em, repository.findByEmergencia_Uuid(emergenciaId).orElse(null));
    }

    /**
     * Cria ou ajusta o rascunho: a emergência em desmobilização ou encerrada (422), o relatório não finalizado (422); o plano
     * de uma recomendação precisa ter sido usado na emergência (422). As recomendações substituem as anteriores; a que já
     * gerou revisão do plano é mantida pela descrição e pelo plano iguais.
     */
    @Transactional
    public RelatorioPosEventoDto salvar(UUID emergenciaId, RelatorioPosEventoRequestDto dto) {
        controleDeAcesso.exigirAlguma("EMERGENCIA.COORDENAR", EmergenciaService.ATIVAR);
        Emergencia em = emergencia(emergenciaId);
        if (em.getSituacao() == SituacaoEmergencia.ATIVA) {
            throw new ResourceUnprocessableEntityException("O relatório começa na desmobilização.");
        }
        RelatorioPosEvento r = repository.findByEmergencia_Uuid(emergenciaId).orElse(null);
        Instant agora = Instant.now();
        if (r == null) {
            r = new RelatorioPosEvento();
            r.setEmergencia(em);
            r.setSituacao(SituacaoRelatorioPosEvento.RASCUNHO);
            r.setCriadoEm(agora);
            r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        } else if (r.getSituacao() == SituacaoRelatorioPosEvento.FINALIZADO) {
            throw new ResourceUnprocessableEntityException("O relatório já foi finalizado.");
        }
        Set<String> usados = codigosUsados(em);
        r.setResumo(texto(dto.getResumo()));
        r.setProblemas(texto(dto.getProblemas()));
        r.setLicoes(texto(dto.getLicoes()));
        List<RecomendacaoRelatorio> anteriores = new ArrayList<>(r.getRecomendacoes());
        r.getRecomendacoes().clear();
        int i = 0;
        for (RelatorioPosEventoRequestDto.Recomendacao rec : dto.getRecomendacoes() == null ? List.<RelatorioPosEventoRequestDto.Recomendacao>of()
                : dto.getRecomendacoes()) {
            String codigo = texto(rec.getPlanoCodigo()) != null ? rec.getPlanoCodigo().trim().toUpperCase() : null;
            if (codigo != null && !usados.contains(codigo)) {
                throw new ResourceUnprocessableEntityException("O plano " + codigo + " não foi usado nesta emergência.");
            }
            String descricao = rec.getDescricao().trim();
            PlanoContingencia revisao = anteriores.stream().filter(a -> a.getPlanoRevisao() != null && a.getDescricao().equals(descricao)
                    && java.util.Objects.equals(a.getPlanoCodigo(), codigo)).map(RecomendacaoRelatorio::getPlanoRevisao).findFirst().orElse(null);
            r.getRecomendacoes().add(new RecomendacaoRelatorio(null, r, i++, descricao, codigo, revisao));
        }
        r.setAtualizadoEm(agora);
        repository.save(r);
        ContextoAuditoria.registro(em.getUuid());
        return dto(em, r);
    }

    /** Finaliza: a emergência encerrada (422), com o resumo (422). Finalizado, não muda. */
    @Transactional
    public RelatorioPosEventoDto finalizar(UUID emergenciaId) {
        controleDeAcesso.exigirAlguma(EmergenciaService.ATIVAR);
        Emergencia em = emergencia(emergenciaId);
        RelatorioPosEvento r = repository.findByEmergencia_Uuid(emergenciaId).orElseThrow(() -> new ResourceUnprocessableEntityException(
                "Escreva o relatório antes de finalizar."));
        if (r.getSituacao() == SituacaoRelatorioPosEvento.FINALIZADO) {
            throw new ResourceUnprocessableEntityException("O relatório já foi finalizado.");
        }
        if (em.getSituacao() != SituacaoEmergencia.ENCERRADA) {
            throw new ResourceUnprocessableEntityException("Encerre a emergência antes de finalizar o relatório.");
        }
        if (r.getResumo() == null) {
            throw new ResourceUnprocessableEntityException("O relatório finalizado pede o resumo.");
        }
        r.setSituacao(SituacaoRelatorioPosEvento.FINALIZADO);
        r.setFinalizadoEm(Instant.now());
        r.setFinalizadoPorCpf(UsuarioAutenticado.cpf());
        repository.save(r);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Relatório pós-evento finalizado.");
        return dto(em, r);
    }

    /**
     * A recomendação que aponta um plano gera a revisão dele (a versão vigente vira rascunho novo, com as regras da E2 e
     * CONTINGENCIA.GERENCIAR); sem plano, 422; já gerada, 409.
     */
    @Transactional
    public RelatorioPosEventoDto revisarPlano(UUID emergenciaId, UUID recomendacaoId) {
        Emergencia em = emergencia(emergenciaId);
        RelatorioPosEvento r = repository.findByEmergencia_Uuid(emergenciaId).orElseThrow(() -> new ResourceNotFoundException(
                "Não há relatório nesta emergência."));
        RecomendacaoRelatorio rec = r.getRecomendacoes().stream().filter(x -> x.getUuid().equals(recomendacaoId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a recomendação " + recomendacaoId + " neste relatório."));
        if (rec.getPlanoCodigo() == null) {
            throw new ResourceUnprocessableEntityException("A recomendação não aponta um plano.");
        }
        if (rec.getPlanoRevisao() != null) {
            throw new ResourceConflictException("Esta recomendação já gerou a revisão do plano (versão " + rec.getPlanoRevisao().getVersao() + ").");
        }
        PlanoContingencia vigente = planoRepository.findVersoes(rec.getPlanoCodigo()).stream()
                .filter(p -> p.getSituacao() == SituacaoPlanoContingencia.VIGENTE).findFirst()
                .orElseThrow(() -> new ResourceUnprocessableEntityException("O plano " + rec.getPlanoCodigo() + " não tem versão vigente para revisar."));
        PlanoContingenciaDto nova = planoService.revisar(vigente.getUuid());
        rec.setPlanoRevisao(planoRepository.findById(nova.getUuid()).orElseThrow());
        repository.save(r);
        ContextoAuditoria.registro(em.getUuid());
        ContextoAuditoria.detalhe("Revisão do plano " + rec.getPlanoCodigo() + " a partir do relatório pós-evento.");
        return dto(em, r);
    }

    /** Os números do relatório em CSV (separador ;), para a planilha da gestão. */
    @Transactional(readOnly = true)
    public String csv(UUID emergenciaId) {
        Emergencia em = emergencia(emergenciaId);
        RelatorioPosEventoDto.Indicadores i = indicadores(em);
        StringBuilder sb = new StringBuilder("Indicador;Valor\n");
        linha(sb, "Emergência", em.getNome());
        linha(sb, "Tipo", em.getTipo().getNome());
        linha(sb, "Duração (horas)", i.getDuracaoHoras());
        linha(sb, "Do evento à ativação (horas)", i.getHorasAteAtivacao());
        linha(sb, "Nível máximo", i.getNivelMaximo());
        linha(sb, "Mudanças de nível", i.getMudancasDeNivel());
        linha(sb, "Ações", i.getAcoesTotal());
        linha(sb, "Ações concluídas", i.getAcoesConcluidas());
        linha(sb, "Ações canceladas", i.getAcoesCanceladas());
        linha(sb, "Ações concluídas no prazo (%)", i.getPercentualNoPrazo());
        linha(sb, "Mobilizações", i.getMobilizacoes());
        linha(sb, "Profissionais mobilizados", i.getProfissionaisMobilizados());
        i.getMobilizacoesPorTipo().forEach(c -> linha(sb, "Mobilizações · " + c.getChave(), c.getQuantidade()));
        linha(sb, "Capacidade emergencial ativada", i.getCapacidadeAtivada());
        linha(sb, "População afetada", i.getPopulacaoAfetada());
        linha(sb, "Unidades afetadas", i.getUnidadesAfetadas());
        linha(sb, "Leitos indisponíveis", i.getLeitosIndisponiveis());
        return sb.toString();
    }

    private static void linha(StringBuilder sb, String nome, Object valor) {
        sb.append(nome.replace(';', ',')).append(';').append(valor == null ? "" : String.valueOf(valor).replace(';', ',')).append('\n');
    }

    private RelatorioPosEventoDto.Indicadores indicadores(Emergencia em) {
        Instant fim = em.getFim() != null ? em.getFim() : Instant.now();
        List<MudancaNivelEmergencia> niveis = mudancaRepository.findDaEmergencia(em.getUuid());
        String maximo = niveis.stream().max(Comparator.comparingInt(m -> m.getNivelNovo().getOrdem())).map(m -> m.getNivelNovo().getNome())
                .orElse(em.getNivel().getNome());
        List<AcaoEmergencial> acoes = acaoRepository.findDaEmergencia(em.getUuid());
        int concluidas = (int) acoes.stream().filter(a -> a.getSituacao() == SituacaoAcaoEmergencial.CONCLUIDA).count();
        int canceladas = (int) acoes.stream().filter(a -> a.getSituacao() == SituacaoAcaoEmergencial.CANCELADA).count();
        List<AcaoEmergencial> comPrazo = acoes.stream().filter(a -> a.getSituacao() == SituacaoAcaoEmergencial.CONCLUIDA && a.getPrazo() != null).toList();
        int noPrazo = (int) comPrazo.stream().filter(a -> !a.getConcluidaEm().isAfter(a.getPrazo())).count();
        List<Mobilizacao> mobs = mobilizacaoRepository.findDaEmergencia(em.getUuid());
        Map<TipoMobilizacao, Integer> porTipo = new LinkedHashMap<>();
        mobs.forEach(m -> porTipo.merge(m.getTipo(), 1, Integer::sum));
        int capacidade = capacidadeRepository.findDaEmergencia(em.getUuid()).stream().mapToInt(AtivacaoCapacidade::getQuantidade).sum();
        AvaliacaoImpacto ultima = avaliacaoRepository.findDaEmergencia(em.getUuid()).stream().findFirst().orElse(null);
        return new RelatorioPosEventoDto.Indicadores(horas(em.getInicio(), fim), horas(em.getEvento().getOcorridoEm(), em.getInicio()), maximo,
                Math.max(0, niveis.size() - 1), acoes.size(), concluidas, canceladas, comPrazo.size(), noPrazo,
                comPrazo.isEmpty() ? null : BigDecimal.valueOf(noPrazo * 100.0 / comPrazo.size()).setScale(1, RoundingMode.HALF_UP).doubleValue(),
                mobs.size(), (int) mobs.stream().filter(m -> m.getTipo() == TipoMobilizacao.PROFISSIONAL).count(),
                porTipo.entrySet().stream().map(e -> new RelatorioPosEventoDto.Contagem(e.getKey().name(), e.getValue())).toList(), capacidade,
                ultima != null ? ultima.getPopulacaoAfetada() : null, ultima != null ? ultima.getUnidadesAfetadas().size() : 0,
                ultima != null ? ultima.getLeitosIndisponiveis() : null);
    }

    private static double horas(Instant de, Instant ate) {
        double h = Math.max(0, Duration.between(de, ate).toMinutes()) / 60.0;
        return BigDecimal.valueOf(h).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    private Set<String> codigosUsados(Emergencia em) {
        Set<String> s = new LinkedHashSet<>();
        ativacaoPlanoRepository.findDaEmergencia(em.getUuid()).forEach(a -> s.add(a.getPlano().getCodigo().toUpperCase()));
        return s;
    }

    private RelatorioPosEventoDto dto(Emergencia em, RelatorioPosEvento r) {
        Map<String, AtivacaoPlano> usados = new LinkedHashMap<>();
        ativacaoPlanoRepository.findDaEmergencia(em.getUuid()).forEach(a -> usados.putIfAbsent(a.getPlano().getCodigo(), a));
        List<RelatorioPosEventoDto.PlanoUsado> planos = usados.values().stream()
                .map(a -> new RelatorioPosEventoDto.PlanoUsado(a.getPlano().getCodigo(), a.getPlano().getNome(), a.getPlano().getVersao())).toList();
        if (r == null) {
            return new RelatorioPosEventoDto(null, em.getUuid(), null, null, null, null, List.of(), planos, indicadores(em), null, null, null);
        }
        return new RelatorioPosEventoDto(r.getUuid(), em.getUuid(), r.getSituacao(), r.getResumo(), r.getProblemas(), r.getLicoes(),
                r.getRecomendacoes().stream().map(x -> new RelatorioPosEventoDto.Recomendacao(x.getUuid(), x.getDescricao(), x.getPlanoCodigo(),
                        x.getPlanoRevisao() != null ? x.getPlanoRevisao().getUuid() : null,
                        x.getPlanoRevisao() != null ? x.getPlanoRevisao().getVersao() : null)).toList(),
                planos, indicadores(em), r.getCriadoEm(), r.getAtualizadoEm(), r.getFinalizadoEm());
    }

    private Emergencia emergencia(UUID uuid) {
        return emergenciaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a emergência " + uuid + " em nossos registros."));
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
