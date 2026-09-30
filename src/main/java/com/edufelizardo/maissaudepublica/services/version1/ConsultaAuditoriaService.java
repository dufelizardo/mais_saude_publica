package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.models.EventoAuditoria;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EventoAuditoriaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PaginaAuditoriaResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.AcaoAuditoria;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoAuditoria;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Consulta da trilha de auditoria (ADR-0071): filtros opcionais, do mais recente ao mais antigo, paginada.
 * Respeita o escopo: quem tem {@code AUDITORIA.CONSULTAR} numa unidade vê os eventos dessa unidade e das
 * de baixo; eventos sem unidade (login, cadastros de rede) só para quem audita a rede inteira.
 */
@Service
public class ConsultaAuditoriaService {

    public static final int TAMANHO_PADRAO = 20;
    public static final int TAMANHO_MAXIMO = 100;
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");

    /** Filtros da consulta; todos opcionais. {@code desde}/{@code ate} são dias inteiros no fuso de Brasília. */
    public record Filtro(String usuarioCpf, UUID pacienteId, UUID registroId, UUID unidadeId, AcaoAuditoria acao,
                         ResultadoAuditoria resultado, LocalDate desde, LocalDate ate) {
    }

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Transactional(readOnly = true)
    public PaginaAuditoriaResponseDto consultar(Filtro f, int pagina, int tamanho) {
        if (f.desde() != null && f.ate() != null && f.ate().isBefore(f.desde())) {
            throw new ResourceBadRequestException("O fim do período não pode ser antes do início.");
        }
        if (pagina < 0 || tamanho < 1 || tamanho > TAMANHO_MAXIMO) {
            throw new ResourceBadRequestException("Página a partir de 0 e tamanho de 1 a " + TAMANHO_MAXIMO + ".");
        }
        Optional<Set<UUID>> escopo = controleDeAcesso.unidadesVisiveis("AUDITORIA.CONSULTAR");
        if (escopo.isPresent() && escopo.get().isEmpty()) {
            return new PaginaAuditoriaResponseDto(List.of(), 0, pagina, tamanho, new PaginaAuditoriaResponseDto.Resumo());
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<EventoAuditoria> q = cb.createQuery(EventoAuditoria.class);
        Root<EventoAuditoria> r = q.from(EventoAuditoria.class);
        q.where(predicados(cb, r, f, escopo)).orderBy(cb.desc(r.get("ocorridoEm")));
        List<EventoAuditoria> eventos = em.createQuery(q).setFirstResult(pagina * tamanho).setMaxResults(tamanho).getResultList();

        CriteriaQuery<Tuple> g = cb.createTupleQuery();
        Root<EventoAuditoria> rg = g.from(EventoAuditoria.class);
        g.multiselect(rg.get("acao"), rg.get("resultado"), cb.count(rg))
                .where(predicados(cb, rg, f, escopo))
                .groupBy(rg.get("acao"), rg.get("resultado"));
        long total = 0;
        PaginaAuditoriaResponseDto.Resumo resumo = new PaginaAuditoriaResponseDto.Resumo();
        for (Tuple t : em.createQuery(g).getResultList()) {
            AcaoAuditoria acao = t.get(0, AcaoAuditoria.class);
            boolean negado = t.get(1, ResultadoAuditoria.class) == ResultadoAuditoria.NEGADO;
            long n = t.get(2, Long.class);
            total += n;
            if (negado) resumo.setNegados(resumo.getNegados() + n);
            switch (acao) {
                case LEITURA -> resumo.setLeituras(resumo.getLeituras() + n);
                case LOGIN -> {
                    resumo.setLogins(resumo.getLogins() + n);
                    if (negado) resumo.setLoginsRecusados(resumo.getLoginsRecusados() + n);
                }
                case TROCA_DE_SENHA -> { }
                default -> resumo.setAlteracoes(resumo.getAlteracoes() + n);
            }
        }
        return new PaginaAuditoriaResponseDto(comNomes(eventos), total, pagina, tamanho, resumo);
    }

    private Predicate[] predicados(CriteriaBuilder cb, Root<EventoAuditoria> r, Filtro f, Optional<Set<UUID>> escopo) {
        List<Predicate> p = new ArrayList<>();
        if (f.usuarioCpf() != null && !f.usuarioCpf().isBlank()) p.add(cb.equal(r.get("usuarioCpf"), f.usuarioCpf().replaceAll("\\D", "")));
        if (f.pacienteId() != null) p.add(cb.equal(r.get("pacienteId"), f.pacienteId()));
        if (f.registroId() != null) p.add(cb.equal(r.get("registroId"), f.registroId()));
        if (f.unidadeId() != null) p.add(cb.equal(r.get("unidadeId"), f.unidadeId()));
        if (f.acao() != null) p.add(cb.equal(r.get("acao"), f.acao()));
        if (f.resultado() != null) p.add(cb.equal(r.get("resultado"), f.resultado()));
        if (f.desde() != null) p.add(cb.greaterThanOrEqualTo(r.get("ocorridoEm"), f.desde().atStartOfDay(FUSO).toInstant()));
        if (f.ate() != null) p.add(cb.lessThan(r.get("ocorridoEm"), f.ate().plusDays(1).atStartOfDay(FUSO).toInstant()));
        escopo.ifPresent(ids -> p.add(r.get("unidadeId").in(ids)));
        return p.toArray(new Predicate[0]);
    }

    /** Nomes de quem, do paciente e da unidade, buscados de uma vez para a página. */
    private List<EventoAuditoriaResponseDto> comNomes(List<EventoAuditoria> eventos) {
        Set<String> cpfs = eventos.stream().map(EventoAuditoria::getUsuarioCpf).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> pacientes = eventos.stream().map(EventoAuditoria::getPacienteId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<UUID> unidades = eventos.stream().map(EventoAuditoria::getUnidadeId).filter(Objects::nonNull).collect(Collectors.toSet());
        // HashMap: eventos sem usuário, paciente ou unidade consultam com chave nula (Map.of() recusaria).
        Map<String, String> nomeUsuario = new HashMap<>();
        if (!cpfs.isEmpty()) usuarioRepository.findByCpfIn(cpfs).forEach(u -> nomeUsuario.putIfAbsent(u.getCpf(), u.getNome()));
        Map<UUID, String> nomePaciente = new HashMap<>();
        if (!pacientes.isEmpty()) pacienteRepository.findAllById(pacientes).forEach(p -> nomePaciente.put(p.getUuid(), p.getNome()));
        Map<UUID, String> nomeUnidade = new HashMap<>();
        if (!unidades.isEmpty()) unidadeDeSaudeRepository.findAllById(unidades).forEach(u -> nomeUnidade.put(u.getUuid(), u.getNome()));
        Function<EventoAuditoria, EventoAuditoriaResponseDto> dto = e -> EventoAuditoriaResponseDto.fromEvento(e,
                nomeUsuario.get(e.getUsuarioCpf()), nomePaciente.get(e.getPacienteId()), nomeUnidade.get(e.getUnidadeId()));
        return eventos.stream().map(dto).toList();
    }
}
