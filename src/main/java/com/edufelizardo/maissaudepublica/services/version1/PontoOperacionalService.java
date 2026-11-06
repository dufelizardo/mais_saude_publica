package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Domicilio;
import com.edufelizardo.maissaudepublica.models.Endereco;
import com.edufelizardo.maissaudepublica.models.PontoOperacional;
import com.edufelizardo.maissaudepublica.models.PosicaoUnidadeMovel;
import com.edufelizardo.maissaudepublica.models.RotaProgramada;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.UnidadePerfilOperacional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AtendimentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EncerramentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PontoOperacionalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PosicaoUnidadeMovelRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RotaProgramadaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.LocaisDeRealizacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PontosDaUnidadeDto;
import com.edufelizardo.maissaudepublica.models.enuns.LocalRealizacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoPontoOperacional;
import com.edufelizardo.maissaudepublica.repositories.DomicilioRepository;
import com.edufelizardo.maissaudepublica.repositories.MembroNucleoRepository;
import com.edufelizardo.maissaudepublica.repositories.PontoOperacionalRepository;
import com.edufelizardo.maissaudepublica.repositories.PosicaoUnidadeMovelRepository;
import com.edufelizardo.maissaudepublica.repositories.RotaProgramadaRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadePerfilOperacionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Ponto operacional, fatia MO3 do Modelo Operacional (ADR-0166, desenho na ADR-0115): os pontos onde a unidade atua, a base,
 * as rotas e a localização da unidade móvel, e o local de realização do Atendimento. A unidade continua sendo o vínculo
 * organizacional. Unidade móvel é a unidade com o perfil Atenção móvel vigente, não um tipo.
 */
@Service
public class PontoOperacionalService {

    static final String PERFIL_MOVEL = "ATENCAO_MOVEL";
    private static final String[] VER_LOCAIS = {"ATENDIMENTO.GERENCIAR", RedeUnidadesService.GERENCIAR, "ADMINISTRATIVO.CONSULTAR"};
    private static final int POSICOES_NA_FICHA = 20;

    @Autowired private PontoOperacionalRepository pontoRepository;
    @Autowired private RotaProgramadaRepository rotaRepository;
    @Autowired private PosicaoUnidadeMovelRepository posicaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private UnidadePerfilOperacionalRepository unidadePerfilRepository;
    @Autowired private DomicilioRepository domicilioRepository;
    @Autowired private MembroNucleoRepository membroRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    @Transactional(readOnly = true)
    public PontosDaUnidadeDto daUnidade(UUID unidadeId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, RedeUnidadesService.VER);
        return montar(u);
    }

    /** Os pontos ativos da unidade e o domicílio atual do paciente, para o local de realização do atendimento. */
    @Transactional(readOnly = true)
    public LocaisDeRealizacaoDto locais(UUID unidadeId, UUID pacienteId) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigirVisivel(u, VER_LOCAIS);
        List<LocaisDeRealizacaoDto.Ponto> pontos = pontoRepository.findByUnidade_UuidOrderByNomeAsc(u.getUuid()).stream()
                .filter(PontoOperacional::vigente)
                .map(p -> new LocaisDeRealizacaoDto.Ponto(p.getUuid(), p.getNome(), p.getTipo())).toList();
        Domicilio d = pacienteId == null ? null : membroRepository.findByPaciente_UuidAndFimIsNull(pacienteId)
                .map(m -> m.getNucleo().getDomicilio()).orElse(null);
        return new LocaisDeRealizacaoDto(pontos, d != null ? d.getUuid() : null, d != null ? descricao(d) : null);
    }

    // ── Pontos ─────────────────────────────────────────────────────────────────────────────────────

    /** Inclui o ponto: base só em unidade móvel (422) e uma vigente por unidade (409); nome único entre os ativos (409). */
    @Transactional
    public PontosDaUnidadeDto incluirPonto(UUID unidadeId, PontoOperacionalRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        if (dto.getTipo() == null) {
            throw new ResourceBadRequestException("Informe o tipo do ponto.");
        }
        coordenadas(dto.getLatitude(), dto.getLongitude());
        List<PontoOperacional> vigentes = pontoRepository.findByUnidade_UuidOrderByNomeAsc(u.getUuid()).stream().filter(PontoOperacional::vigente).toList();
        if (dto.getTipo() == TipoPontoOperacional.BASE) {
            exigirMovel(u);
            if (vigentes.stream().anyMatch(p -> p.getTipo() == TipoPontoOperacional.BASE)) {
                throw new ResourceConflictException(u.getNome() + " já tem uma base vigente.");
            }
        }
        String nome = nomeLivre(dto.getNome(), vigentes.stream().map(PontoOperacional::getNome));
        LocalDate inicio = dto.getInicio() != null ? dto.getInicio() : LocalDate.now();
        if (inicio.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O início não pode ser no futuro.");
        }
        PontoOperacional p = new PontoOperacional();
        p.setUnidade(u);
        p.setTipo(dto.getTipo());
        p.setNome(nome);
        p.setReferencia(texto(dto.getReferencia()));
        p.setLatitude(dto.getLatitude());
        p.setLongitude(dto.getLongitude());
        p.setInicio(inicio);
        p.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        pontoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Ponto " + nome + " incluído.");
        return montar(u);
    }

    /** Ajusta o nome, a referência e a posição; encerrado não muda (422). */
    @Transactional
    public PontosDaUnidadeDto atualizarPonto(UUID unidadeId, UUID pontoId, PontoOperacionalRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        PontoOperacional p = ponto(u, pontoId);
        if (!p.vigente()) {
            throw new ResourceUnprocessableEntityException("Este ponto já foi encerrado.");
        }
        coordenadas(dto.getLatitude(), dto.getLongitude());
        p.setNome(nomeLivre(dto.getNome(), pontoRepository.findByUnidade_UuidOrderByNomeAsc(u.getUuid()).stream()
                .filter(o -> o.vigente() && !o.getUuid().equals(pontoId)).map(PontoOperacional::getNome)));
        p.setReferencia(texto(dto.getReferencia()));
        p.setLatitude(dto.getLatitude());
        p.setLongitude(dto.getLongitude());
        pontoRepository.save(p);
        ContextoAuditoria.registro(p.getUuid());
        return montar(u);
    }

    /** Encerra com o motivo; não encerra ponto de rota vigente (422). */
    @Transactional
    public PontosDaUnidadeDto encerrarPonto(UUID unidadeId, UUID pontoId, EncerramentoRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        PontoOperacional p = ponto(u, pontoId);
        if (!p.vigente()) {
            throw new ResourceUnprocessableEntityException("Este ponto já foi encerrado.");
        }
        List<String> rotas = rotaRepository.findByUnidade_UuidOrderByNomeAsc(u.getUuid()).stream()
                .filter(r -> r.vigente() && r.getPontos().stream().anyMatch(x -> x.getUuid().equals(pontoId)))
                .map(RotaProgramada::getNome).toList();
        if (!rotas.isEmpty()) {
            throw new ResourceUnprocessableEntityException("Encerre antes as rotas que passam por " + p.getNome() + ": " + String.join(", ", rotas) + ".");
        }
        p.setFim(fim(dto, p.getInicio()));
        p.setMotivoFim(dto.getMotivo().trim());
        pontoRepository.save(p);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(p.getUuid());
        ContextoAuditoria.detalhe("Ponto " + p.getNome() + " encerrado.");
        return montar(u);
    }

    // ── Rotas ──────────────────────────────────────────────────────────────────────────────────────

    /** Inclui a rota da unidade móvel (422 sem o perfil); nome único entre as vigentes (409). */
    @Transactional
    public PontosDaUnidadeDto incluirRota(UUID unidadeId, RotaProgramadaRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        exigirMovel(u);
        RotaProgramada r = new RotaProgramada();
        r.setUnidade(u);
        r.setInicio(LocalDate.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(r, dto);
        rotaRepository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Rota " + r.getNome() + " incluída.");
        return montar(u);
    }

    /** Ajusta a rota; encerrada não muda (422). */
    @Transactional
    public PontosDaUnidadeDto atualizarRota(UUID unidadeId, UUID rotaId, RotaProgramadaRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        RotaProgramada r = rota(u, rotaId);
        if (!r.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta rota já foi encerrada.");
        }
        preencher(r, dto);
        rotaRepository.save(r);
        ContextoAuditoria.registro(r.getUuid());
        return montar(u);
    }

    @Transactional
    public PontosDaUnidadeDto encerrarRota(UUID unidadeId, UUID rotaId, EncerramentoRequestDto dto) {
        UnidadeDeSaude u = unidade(unidadeId);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        RotaProgramada r = rota(u, rotaId);
        if (!r.vigente()) {
            throw new ResourceUnprocessableEntityException("Esta rota já foi encerrada.");
        }
        r.setFim(fim(dto, r.getInicio()));
        r.setMotivoFim(dto.getMotivo().trim());
        rotaRepository.save(r);
        ContextoAuditoria.unidade(u);
        ContextoAuditoria.registro(r.getUuid());
        ContextoAuditoria.detalhe("Rota " + r.getNome() + " encerrada.");
        return montar(u);
    }

    // ── Localização ────────────────────────────────────────────────────────────────────────────────

    /** Registra onde a unidade móvel está: ponto ativo da própria unidade (422), diferente do atual (409), depois da passagem anterior (400). */
    @Transactional
    public PontosDaUnidadeDto registrarPosicao(UUID unidadeId, PosicaoUnidadeMovelRequestDto dto) {
        UnidadeDeSaude u = unidadeDeAtendimento(unidadeId);
        exigirMovel(u);
        PontoOperacional p = pontoDaUnidade(u, dto.getPontoId());
        LocalDateTime desde = dto.getDesde() != null ? dto.getDesde() : LocalDateTime.now();
        if (desde.isAfter(LocalDateTime.now())) {
            throw new ResourceBadRequestException("A data da passagem não pode ser no futuro.");
        }
        PosicaoUnidadeMovel atual = posicaoRepository.findDaUnidade(u.getUuid()).stream().findFirst().orElse(null);
        if (atual != null) {
            if (atual.getPonto().getUuid().equals(p.getUuid())) {
                throw new ResourceConflictException(u.getNome() + " já está em " + p.getNome() + ".");
            }
            if (desde.isBefore(atual.getDesde())) {
                throw new ResourceBadRequestException("A passagem vem depois da anterior.");
            }
        }
        PosicaoUnidadeMovel nova = new PosicaoUnidadeMovel(null, u, p, desde, texto(dto.getObservacao()), UsuarioAutenticado.cpf());
        posicaoRepository.save(nova);
        ContextoAuditoria.registro(nova.getUuid());
        ContextoAuditoria.detalhe("Unidade móvel em " + p.getNome() + ".");
        return montar(u);
    }

    // ── Local de realização do atendimento ─────────────────────────────────────────────────────────

    /**
     * Grava o local (ADR-0166). Sem local, foi na unidade. Ponto da unidade: ponto ativo da unidade do atendimento (400, 422);
     * o que já estava continua valendo depois de encerrado. Domicílio: o cadastrado ou a descrição (400). Demais: descrição
     * (400). Latitude e longitude juntas (400).
     */
    public void aplicarLocal(Atendimento at, AtendimentoRequestDto dto) {
        LocalRealizacao local = dto.getLocalRealizacao();
        at.setLocalRealizacao(local);
        if (local == null) {
            at.setLocalDescricao(null);
            at.setLocalLatitude(null);
            at.setLocalLongitude(null);
            at.setDomicilio(null);
            at.setPontoOperacional(null);
            return;
        }
        coordenadas(dto.getLocalLatitude(), dto.getLocalLongitude());
        String descricao = texto(dto.getLocalDescricao());
        Domicilio domicilio = null;
        PontoOperacional ponto = null;
        if (local == LocalRealizacao.PONTO_DA_UNIDADE) {
            if (dto.getPontoOperacionalId() == null) {
                throw new ResourceBadRequestException("Informe o ponto da unidade.");
            }
            ponto = at.getPontoOperacional() != null && at.getPontoOperacional().getUuid().equals(dto.getPontoOperacionalId())
                    ? at.getPontoOperacional() : pontoDaUnidade(at.getUnidade(), dto.getPontoOperacionalId());
            if (!ponto.getUnidade().getUuid().equals(at.getUnidade().getUuid())) {
                throw new ResourceUnprocessableEntityException("O ponto " + ponto.getNome() + " é de outra unidade.");
            }
        } else if (local == LocalRealizacao.DOMICILIO && dto.getDomicilioId() != null) {
            domicilio = domicilioRepository.findById(dto.getDomicilioId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o domicílio " + dto.getDomicilioId() + " em nossos registros."));
        } else if (descricao == null) {
            throw new ResourceBadRequestException("Descreva o local de realização.");
        }
        at.setLocalDescricao(descricao);
        at.setLocalLatitude(dto.getLocalLatitude());
        at.setLocalLongitude(dto.getLocalLongitude());
        at.setDomicilio(domicilio);
        at.setPontoOperacional(ponto);
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Dias e pontos válidos: horário de início antes do fim (400), pontos ativos da unidade (422), sem repetir (409). */
    private void preencher(RotaProgramada r, RotaProgramadaRequestDto dto) {
        if (dto.getHoraInicio() != null && dto.getHoraFim() != null && !dto.getHoraInicio().isBefore(dto.getHoraFim())) {
            throw new ResourceBadRequestException("O horário de início vem antes do fim.");
        }
        r.setNome(nomeLivre(dto.getNome(), rotaRepository.findByUnidade_UuidOrderByNomeAsc(r.getUnidade().getUuid()).stream()
                .filter(o -> o.vigente() && !o.getUuid().equals(r.getUuid())).map(RotaProgramada::getNome)));
        if (new HashSet<>(dto.getPontoIds()).size() != dto.getPontoIds().size()) {
            throw new ResourceConflictException("O mesmo ponto não se repete na rota.");
        }
        List<PontoOperacional> pontos = new ArrayList<>();
        for (UUID id : dto.getPontoIds()) {
            pontos.add(pontoDaUnidade(r.getUnidade(), id));
        }
        r.getPontos().clear();
        r.getPontos().addAll(pontos);
        r.getDias().clear();
        r.getDias().addAll(dto.getDias());
        r.setHoraInicio(dto.getHoraInicio());
        r.setHoraFim(dto.getHoraFim());
    }

    private PontosDaUnidadeDto montar(UnidadeDeSaude u) {
        List<PontoOperacional> pontos = pontoRepository.findByUnidade_UuidOrderByNomeAsc(u.getUuid());
        List<RotaProgramada> rotas = rotaRepository.findByUnidade_UuidOrderByNomeAsc(u.getUuid());
        return new PontosDaUnidadeDto(u.getUuid(), movel(u),
                pontos.stream().filter(PontoOperacional::vigente).map(PontoOperacionalService::dto).toList(),
                pontos.stream().filter(p -> !p.vigente()).sorted(Comparator.comparing(PontoOperacional::getFim).reversed()).map(PontoOperacionalService::dto).toList(),
                rotas.stream().filter(RotaProgramada::vigente).map(PontoOperacionalService::dto).toList(),
                rotas.stream().filter(r -> !r.vigente()).sorted(Comparator.comparing(RotaProgramada::getFim).reversed()).map(PontoOperacionalService::dto).toList(),
                posicaoRepository.findDaUnidade(u.getUuid()).stream().limit(POSICOES_NA_FICHA)
                        .map(p -> new PontosDaUnidadeDto.Posicao(p.getUuid(), p.getPonto().getUuid(), p.getPonto().getNome(), p.getDesde(), p.getObservacao())).toList());
    }

    private static PontosDaUnidadeDto.Ponto dto(PontoOperacional p) {
        return new PontosDaUnidadeDto.Ponto(p.getUuid(), p.getTipo(), p.getNome(), p.getReferencia(), p.getLatitude(), p.getLongitude(), p.getInicio(), p.getFim(), p.getMotivoFim());
    }

    private static PontosDaUnidadeDto.Rota dto(RotaProgramada r) {
        return new PontosDaUnidadeDto.Rota(r.getUuid(), r.getNome(), r.getDias().stream().sorted(Comparator.comparing(DayOfWeek::getValue)).toList(),
                r.getHoraInicio(), r.getHoraFim(), r.getPontos().stream().map(p -> new PontosDaUnidadeDto.PontoRef(p.getUuid(), p.getNome())).toList(),
                r.getInicio(), r.getFim(), r.getMotivoFim());
    }

    private static String descricao(Domicilio d) {
        Endereco e = d.getImovel().getEndereco();
        String rua = e == null ? null : Stream.of(e.getLogradouro(), e.getNumeroLogradouro()).filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(", "));
        return Stream.of(rua, d.getIdentificacao()).filter(Objects::nonNull).filter(s -> !s.isBlank()).collect(Collectors.joining(" · "));
    }

    private boolean movel(UnidadeDeSaude u) {
        return unidadePerfilRepository.findDaUnidade(u.getUuid()).stream()
                .filter(UnidadePerfilOperacional::vigente).anyMatch(a -> PERFIL_MOVEL.equals(a.getPerfil().getCodigo()));
    }

    private void exigirMovel(UnidadeDeSaude u) {
        if (!movel(u)) {
            throw new ResourceUnprocessableEntityException("Base e rota são da unidade móvel; " + u.getNome() + " não tem o perfil Atenção móvel.");
        }
    }

    /** Ponto ativo da unidade: existe (404), é dela (422), está ativo (422). */
    private PontoOperacional pontoDaUnidade(UnidadeDeSaude u, UUID id) {
        PontoOperacional p = pontoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o ponto " + id + " em nossos registros."));
        if (!p.getUnidade().getUuid().equals(u.getUuid())) {
            throw new ResourceUnprocessableEntityException("O ponto " + p.getNome() + " é de outra unidade.");
        }
        if (!p.vigente()) {
            throw new ResourceUnprocessableEntityException("O ponto " + p.getNome() + " foi encerrado.");
        }
        return p;
    }

    private PontoOperacional ponto(UnidadeDeSaude u, UUID id) {
        return pontoRepository.findById(id).filter(p -> p.getUnidade().getUuid().equals(u.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar o ponto " + id + " nesta unidade."));
    }

    private RotaProgramada rota(UnidadeDeSaude u, UUID id) {
        return rotaRepository.findById(id).filter(r -> r.getUnidade().getUuid().equals(u.getUuid()))
                .orElseThrow(() -> new ResourceNotFoundException("Não foi possível encontrar a rota " + id + " nesta unidade."));
    }

    private UnidadeDeSaude unidade(UUID id) {
        return unidadeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + id + " em nossos registros."));
    }

    private UnidadeDeSaude unidadeDeAtendimento(UUID id) {
        UnidadeDeSaude u = unidade(id);
        controleDeAcesso.exigir(RedeUnidadesService.GERENCIAR, u);
        if (RedeUnidadesService.NIVEIS_DE_GESTAO.contains(u.getTipo())) {
            throw new ResourceUnprocessableEntityException(u.getNome() + " é um nível de gestão (" + u.getTipo() + "); pontos são das unidades de atendimento.");
        }
        ContextoAuditoria.unidade(u);
        return u;
    }

    private static String nomeLivre(String nome, Stream<String> usados) {
        String n = nome.trim();
        if (usados.anyMatch(n::equalsIgnoreCase)) {
            throw new ResourceConflictException("Já existe " + n + " nesta unidade.");
        }
        return n;
    }

    private static LocalDate fim(EncerramentoRequestDto dto, LocalDate inicio) {
        LocalDate fim = dto.getFim() != null ? dto.getFim() : LocalDate.now();
        if (fim.isBefore(inicio) || fim.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("O fim fica entre o início e hoje.");
        }
        return fim;
    }

    private static void coordenadas(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResourceBadRequestException("Informe a latitude e a longitude juntas.");
        }
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
