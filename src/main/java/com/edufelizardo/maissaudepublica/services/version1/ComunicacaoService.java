package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.CanalComunicacao;
import com.edufelizardo.maissaudepublica.models.Comunicado;
import com.edufelizardo.maissaudepublica.models.ConteudoEducativo;
import com.edufelizardo.maissaudepublica.models.Lotacao;
import com.edufelizardo.maissaudepublica.models.Publicacao;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.CanalComunicacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ComunicadoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConteudoEducativoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.MotivoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.PublicarConteudoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.CanalComunicacaoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicadoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ComunicadoPublicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConteudoEducativoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConteudoPublicoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PublicacaoDto;
import com.edufelizardo.maissaudepublica.models.enuns.PrioridadeComunicado;
import com.edufelizardo.maissaudepublica.models.enuns.PublicoComunicacao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoComunicado;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoConteudo;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoPublicacao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoCanalComunicacao;
import com.edufelizardo.maissaudepublica.repositories.CanalComunicacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.ComunicadoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConteudoEducativoRepository;
import com.edufelizardo.maissaudepublica.repositories.LotacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.PublicacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Comunicação básica, fatia C1 do domínio 28 (ADR-0168, desenho na ADR-0114): comunicado, conteúdo educativo com revisão técnica,
 * canais e publicação. O portal é a landing page; o e-mail usa o envio que já existe e só vai a profissionais, porque não há
 * registro de consentimento nem de descadastro para a população. A falta de SMTP não derruba a publicação: fica no resultado.
 */
@Service
public class ComunicacaoService {

    static final String PUBLICAR = "COMUNICACAO.PUBLICAR";
    static final String EDITAR = "CONTEUDO.EDITAR";
    static final String REVISAR = "CONTEUDO.REVISAR";
    static final String COORDENAR_EMERGENCIA = "EMERGENCIA.COORDENAR";
    private static final String[] LER = {PUBLICAR, EDITAR, REVISAR};
    private static final Set<SituacaoConteudo> EM_ANDAMENTO = EnumSet.of(SituacaoConteudo.RASCUNHO, SituacaoConteudo.EM_REVISAO, SituacaoConteudo.APROVADO);

    @Autowired private CanalComunicacaoRepository canalRepository;
    @Autowired private ComunicadoRepository comunicadoRepository;
    @Autowired private com.edufelizardo.maissaudepublica.repositories.CampanhaComunicacaoRepository campanhaComunicacaoRepository;
    @Autowired private ConteudoEducativoRepository conteudoRepository;
    @Autowired private PublicacaoRepository publicacaoRepository;
    @Autowired private LotacaoRepository lotacaoRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;
    @Autowired private EnvioDeEmail envioDeEmail;

    // ── Canais ─────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CanalComunicacaoDto> canais() {
        return canalRepository.findAllByOrderByNomeAsc().stream().map(ComunicacaoService::dto).toList();
    }

    /** Nome repetido, 409. */
    @Transactional
    public CanalComunicacaoDto criarCanal(CanalComunicacaoRequestDto dto) {
        String nome = dto.getNome().trim();
        if (canalRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResourceConflictException("Já existe o canal " + nome + ".");
        }
        CanalComunicacao c = new CanalComunicacao(null, dto.getTipo(), nome, texto(dto.getDescricao()), dto.getAtivo() == null || dto.getAtivo());
        canalRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /** O tipo decide como o canal envia, então não muda (422). */
    @Transactional
    public CanalComunicacaoDto atualizarCanal(UUID uuid, CanalComunicacaoRequestDto dto) {
        CanalComunicacao c = canalRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o canal " + uuid + " em nossos registros."));
        if (c.getTipo() != dto.getTipo()) {
            throw new ResourceUnprocessableEntityException("O tipo do canal não muda.");
        }
        String nome = dto.getNome().trim();
        if (!c.getNome().equalsIgnoreCase(nome) && canalRepository.existsByNomeIgnoreCase(nome)) {
            throw new ResourceConflictException("Já existe o canal " + nome + ".");
        }
        c.setNome(nome);
        c.setDescricao(texto(dto.getDescricao()));
        c.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        canalRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    // ── Comunicados ────────────────────────────────────────────────────────────────────────────────

    /** Os comunicados da rede e os das unidades do acesso. */
    @Transactional(readOnly = true)
    public List<ComunicadoDto> comunicados() {
        Optional<Set<UUID>> visiveis = controleDeAcesso.unidadesVisiveis(LER);
        return comunicadoRepository.findAllByOrderByInicioDesc().stream()
                .filter(c -> c.getUnidade() == null || visiveis.isEmpty() || visiveis.get().contains(c.getUnidade().getUuid()))
                .map(this::dto).toList();
    }

    @Transactional
    public ComunicadoDto criarComunicado(ComunicadoRequestDto dto) {
        Comunicado c = new Comunicado();
        c.setSituacao(SituacaoComunicado.RASCUNHO);
        c.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(c, dto);
        comunicadoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Comunicado " + c.getTitulo() + ".");
        return dto(c);
    }

    /** Só o rascunho muda (422). */
    @Transactional
    public ComunicadoDto atualizarComunicado(UUID uuid, ComunicadoRequestDto dto) {
        Comunicado c = comunicado(uuid);
        if (c.getSituacao() != SituacaoComunicado.RASCUNHO) {
            throw new ResourceUnprocessableEntityException("Comunicado publicado não se edita; encerre e crie outro.");
        }
        exigirPublicar(c.getUnidade(), PUBLICAR);
        preencher(c, dto);
        comunicadoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /**
     * Publica o rascunho nos canais dele: pelo menos um (400), período ainda aberto (422). O emergencial pede também
     * EMERGENCIA.COORDENAR (403).
     */
    @Transactional
    public ComunicadoDto publicarComunicado(UUID uuid) {
        Comunicado c = comunicado(uuid);
        exigirPublicar(c.getUnidade(), PUBLICAR);
        if (c.getPrioridade() == PrioridadeComunicado.EMERGENCIAL) {
            exigirPublicar(c.getUnidade(), COORDENAR_EMERGENCIA);
        }
        if (c.getSituacao() != SituacaoComunicado.RASCUNHO) {
            throw new ResourceUnprocessableEntityException("Este comunicado já foi publicado.");
        }
        if (c.getFim().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("O período do comunicado já terminou.");
        }
        if (c.getCanais().isEmpty()) {
            throw new ResourceBadRequestException("Escolha ao menos um canal.");
        }
        validarCanais(c.getPublico(), c.getCanais());
        c.setSituacao(SituacaoComunicado.PUBLICADO);
        c.setPublicadoEm(LocalDateTime.now());
        c.setPublicadoPorCpf(UsuarioAutenticado.cpf());
        comunicadoRepository.save(c);
        for (CanalComunicacao k : c.getCanais()) {
            publicar(c, null, k, c.getUnidade(), c.getTitulo(), c.getTitulo() + "\n\n" + c.getMensagem());
        }
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Comunicado " + c.getTitulo() + " publicado.");
        return dto(c);
    }

    @Transactional
    public ComunicadoDto encerrarComunicado(UUID uuid, MotivoRequestDto dto) {
        Comunicado c = comunicado(uuid);
        exigirPublicar(c.getUnidade(), PUBLICAR);
        if (c.getSituacao() != SituacaoComunicado.PUBLICADO) {
            throw new ResourceUnprocessableEntityException("Só o comunicado publicado se encerra.");
        }
        c.setSituacao(SituacaoComunicado.ENCERRADO);
        c.setEncerradoEm(LocalDate.now());
        c.setMotivoEncerramento(dto.getMotivo().trim());
        comunicadoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Comunicado " + c.getTitulo() + " encerrado.");
        return dto(c);
    }

    // ── Conteúdo educativo ─────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ConteudoEducativoDto> conteudos() {
        return conteudoRepository.findAllByOrderByTituloAscVersaoDesc().stream().map(this::dto).toList();
    }

    @Transactional
    public ConteudoEducativoDto criarConteudo(ConteudoEducativoRequestDto dto) {
        ConteudoEducativo c = new ConteudoEducativo();
        c.setGrupoId(UUID.randomUUID());
        c.setVersao(1);
        c.setSituacao(SituacaoConteudo.RASCUNHO);
        c.setAutorCpf(autor());
        preencher(c, dto);
        conteudoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Conteúdo " + c.getTitulo() + ".");
        return dto(c);
    }

    /** Só o rascunho muda (422). */
    @Transactional
    public ConteudoEducativoDto atualizarConteudo(UUID uuid, ConteudoEducativoRequestDto dto) {
        ConteudoEducativo c = conteudo(uuid);
        exigirSituacao(c, SituacaoConteudo.RASCUNHO);
        preencher(c, dto);
        conteudoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    @Transactional
    public ConteudoEducativoDto enviarParaRevisao(UUID uuid) {
        ConteudoEducativo c = conteudo(uuid);
        exigirSituacao(c, SituacaoConteudo.RASCUNHO);
        c.setSituacao(SituacaoConteudo.EM_REVISAO);
        c.setMotivoDevolucao(null);
        conteudoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /** A revisão técnica: quem escreveu não aprova o próprio conteúdo (422). */
    @Transactional
    public ConteudoEducativoDto aprovar(UUID uuid) {
        ConteudoEducativo c = conteudo(uuid);
        exigirSituacao(c, SituacaoConteudo.EM_REVISAO);
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && cpf.equals(c.getAutorCpf())) {
            throw new ResourceUnprocessableEntityException("Quem escreveu não aprova o próprio conteúdo.");
        }
        c.setSituacao(SituacaoConteudo.APROVADO);
        c.setRevisorCpf(cpf);
        c.setRevisadoEm(LocalDateTime.now());
        conteudoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Conteúdo " + c.getTitulo() + " aprovado.");
        return dto(c);
    }

    /** Devolve ao autor com o motivo. */
    @Transactional
    public ConteudoEducativoDto devolver(UUID uuid, MotivoRequestDto dto) {
        ConteudoEducativo c = conteudo(uuid);
        exigirSituacao(c, SituacaoConteudo.EM_REVISAO);
        c.setSituacao(SituacaoConteudo.RASCUNHO);
        c.setMotivoDevolucao(dto.getMotivo().trim());
        c.setRevisorCpf(UsuarioAutenticado.cpf());
        c.setRevisadoEm(LocalDateTime.now());
        conteudoRepository.save(c);
        ContextoAuditoria.registro(c.getUuid());
        return dto(c);
    }

    /** Publica o aprovado e não vencido (422). A versão anterior publicada fica substituída. */
    @Transactional
    public ConteudoEducativoDto publicarConteudo(UUID uuid, PublicarConteudoRequestDto dto) {
        ConteudoEducativo c = conteudo(uuid);
        exigirSituacao(c, SituacaoConteudo.APROVADO);
        if (c.getValidade().isBefore(LocalDate.now())) {
            throw new ResourceUnprocessableEntityException("A validade deste conteúdo já venceu.");
        }
        Set<CanalComunicacao> canais = canais(dto.getCanalIds());
        validarCanais(c.getPublico(), canais);
        conteudoRepository.findByGrupoId(c.getGrupoId()).stream().filter(o -> o.getSituacao() == SituacaoConteudo.PUBLICADO).forEach(o -> {
            o.setSituacao(SituacaoConteudo.SUBSTITUIDO);
            conteudoRepository.save(o);
        });
        c.setSituacao(SituacaoConteudo.PUBLICADO);
        c.setPublicadoEm(LocalDateTime.now());
        c.getCanais().clear();
        c.getCanais().addAll(canais);
        conteudoRepository.save(c);
        for (CanalComunicacao k : canais) {
            publicar(null, c, k, null, c.getTitulo(), c.getTitulo() + "\n\n" + c.getResumo() + "\n\n" + c.getTexto());
        }
        ContextoAuditoria.registro(c.getUuid());
        ContextoAuditoria.detalhe("Conteúdo " + c.getTitulo() + " publicado (versão " + c.getVersao() + ").");
        return dto(c);
    }

    /** Cria o rascunho da versão seguinte; só uma em andamento por conteúdo (409). */
    @Transactional
    public ConteudoEducativoDto novaVersao(UUID uuid) {
        ConteudoEducativo c = conteudo(uuid);
        exigirSituacao(c, SituacaoConteudo.PUBLICADO);
        List<ConteudoEducativo> grupo = conteudoRepository.findByGrupoId(c.getGrupoId());
        if (grupo.stream().anyMatch(o -> EM_ANDAMENTO.contains(o.getSituacao()))) {
            throw new ResourceConflictException("Este conteúdo já tem uma nova versão em andamento.");
        }
        ConteudoEducativo n = new ConteudoEducativo();
        n.setGrupoId(c.getGrupoId());
        n.setVersao(grupo.stream().mapToInt(ConteudoEducativo::getVersao).max().orElse(c.getVersao()) + 1);
        n.setTitulo(c.getTitulo());
        n.setResumo(c.getResumo());
        n.setTexto(c.getTexto());
        n.setTema(c.getTema());
        n.setPublico(c.getPublico());
        n.setFaixaEtaria(c.getFaixaEtaria());
        n.setValidade(c.getValidade());
        n.setSituacao(SituacaoConteudo.RASCUNHO);
        n.setAutorCpf(autor());
        conteudoRepository.save(n);
        ContextoAuditoria.registro(n.getUuid());
        return dto(n);
    }

    // ── Portal ─────────────────────────────────────────────────────────────────────────────────────

    /** Os avisos vigentes no portal, com o emergencial primeiro. Nenhum dado de pessoa. */
    @Transactional(readOnly = true)
    public List<ComunicadoPublicoDto> comunicadosPublicos() {
        return comunicadoRepository.findNoPortal(LocalDate.now()).stream()
                .sorted(Comparator.comparing(Comunicado::getPrioridade).reversed())
                .map(c -> new ComunicadoPublicoDto(c.getTitulo(), c.getMensagem(), c.getPrioridade(), c.getUnidade() != null ? c.getUnidade().getNome() : null, c.getInicio(), c.getFim()))
                .toList();
    }

    /** As orientações publicadas e dentro da validade. */
    @Transactional(readOnly = true)
    public List<ConteudoPublicoDto> conteudosPublicos() {
        return conteudoRepository.findNoPortal(LocalDate.now()).stream()
                .map(c -> new ConteudoPublicoDto(c.getTitulo(), c.getResumo(), c.getTexto(), c.getTema(), c.getFaixaEtaria(), c.getVersao(), c.getValidade())).toList();
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** Fim antes do início (400); canais ativos e coerentes com o público (422); unidade do acesso (403). */
    private void preencher(Comunicado c, ComunicadoRequestDto dto) {
        if (dto.getFim().isBefore(dto.getInicio())) {
            throw new ResourceBadRequestException("O fim vem depois do início.");
        }
        UnidadeDeSaude unidade = null;
        if (dto.getUnidadeId() != null) {
            unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        }
        exigirPublicar(unidade, PUBLICAR);
        Set<CanalComunicacao> canais = canais(dto.getCanalIds());
        validarCanais(dto.getPublico(), canais);
        c.setTitulo(dto.getTitulo().trim());
        c.setMensagem(dto.getMensagem().trim());
        c.setPrioridade(dto.getPrioridade());
        c.setPublico(dto.getPublico());
        c.setInicio(dto.getInicio());
        c.setFim(dto.getFim());
        c.setUnidade(unidade);
        c.getCanais().clear();
        c.getCanais().addAll(canais);
        c.setCampanha(campanhaDoComunicado(dto));
    }

    /** A campanha precisa estar planejada ou em andamento, e o período do comunicado caber no dela (422). */
    private com.edufelizardo.maissaudepublica.models.CampanhaComunicacao campanhaDoComunicado(ComunicadoRequestDto dto) {
        if (dto.getCampanhaId() == null) {
            return null;
        }
        var campanha = campanhaComunicacaoRepository.findById(dto.getCampanhaId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a campanha " + dto.getCampanhaId() + " em nossos registros."));
        var s = campanha.getSituacao();
        if (s == com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao.ENCERRADA
                || s == com.edufelizardo.maissaudepublica.models.enuns.SituacaoCampanhaComunicacao.CANCELADA) {
            throw new ResourceUnprocessableEntityException("A campanha " + campanha.getNome() + " já terminou.");
        }
        if (dto.getInicio().isBefore(campanha.getInicio()) || dto.getFim().isAfter(campanha.getFim())) {
            throw new ResourceUnprocessableEntityException("O período do comunicado precisa caber no da campanha.");
        }
        return campanha;
    }

    private static void preencher(ConteudoEducativo c, ConteudoEducativoRequestDto dto) {
        c.setTitulo(dto.getTitulo().trim());
        c.setResumo(dto.getResumo().trim());
        c.setTexto(dto.getTexto().trim());
        c.setTema(dto.getTema().trim());
        c.setPublico(dto.getPublico());
        c.setFaixaEtaria(texto(dto.getFaixaEtaria()));
        c.setValidade(dto.getValidade());
    }

    private void exigirPublicar(UnidadeDeSaude unidade, String permissao) {
        if (unidade != null) {
            controleDeAcesso.exigir(permissao, unidade);
        } else {
            controleDeAcesso.exigirAlguma(permissao);
        }
    }

    private Set<CanalComunicacao> canais(List<UUID> ids) {
        Set<CanalComunicacao> lista = new HashSet<>();
        if (ids == null) {
            return lista;
        }
        for (UUID id : new HashSet<>(ids)) {
            CanalComunicacao k = canalRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o canal " + id + " em nossos registros."));
            lista.add(k);
        }
        return lista;
    }

    /** A população só pelo portal, porque não há consentimento para e-mail; os profissionais, fora do portal, que é público. */
    private static void validarCanais(PublicoComunicacao publico, Set<CanalComunicacao> canais) {
        for (CanalComunicacao k : canais) {
            if (!k.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O canal " + k.getNome() + " está inativo.");
            }
            boolean portal = k.getTipo() == TipoCanalComunicacao.PORTAL;
            if (publico == PublicoComunicacao.POPULACAO && !portal) {
                throw new ResourceUnprocessableEntityException("Para a população, o canal é o portal: envio a pessoa identificada pede consentimento.");
            }
            if (publico == PublicoComunicacao.PROFISSIONAIS && portal) {
                throw new ResourceUnprocessableEntityException("O portal é público; para os profissionais, use o e-mail.");
            }
        }
    }

    private void publicar(Comunicado comunicado, ConteudoEducativo conteudo, CanalComunicacao canal, UnidadeDeSaude unidade, String assunto, String corpo) {
        SituacaoPublicacao situacao;
        int enviados = 0;
        int falhas = 0;
        String detalhe = null;
        if (canal.getTipo() == TipoCanalComunicacao.PORTAL) {
            situacao = SituacaoPublicacao.PUBLICADA;
        } else if (canal.getTipo() != TipoCanalComunicacao.EMAIL) {
            situacao = SituacaoPublicacao.NAO_ENVIADA;
            detalhe = "Este canal ainda não tem envio automático.";
        } else if (!envioDeEmail.disponivel()) {
            situacao = SituacaoPublicacao.NAO_ENVIADA;
            detalhe = "O envio de e-mail não está configurado.";
        } else {
            for (String email : destinatarios(unidade)) {
                if (envioDeEmail.enviar(email, assunto, corpo)) {
                    enviados++;
                } else {
                    falhas++;
                }
            }
            situacao = enviados + falhas == 0 ? SituacaoPublicacao.NAO_ENVIADA : falhas == 0 ? SituacaoPublicacao.ENVIADA
                    : enviados == 0 ? SituacaoPublicacao.NAO_ENVIADA : SituacaoPublicacao.PARCIAL;
            detalhe = enviados + falhas == 0 ? "Nenhum profissional com e-mail no alcance." : null;
        }
        publicacaoRepository.save(new Publicacao(null, comunicado, conteudo, canal, LocalDateTime.now(), situacao, enviados, falhas, detalhe, UsuarioAutenticado.cpf()));
    }

    /** Os profissionais lotados na unidade; sem unidade, os das unidades do acesso de quem publica. */
    private List<String> destinatarios(UnidadeDeSaude unidade) {
        Optional<Set<UUID>> visiveis = unidade != null ? Optional.of(Set.of(unidade.getUuid())) : controleDeAcesso.unidadesVisiveis(PUBLICAR);
        return lotacaoRepository.findVigentesComProfissional().stream()
                .filter(l -> visiveis.isEmpty() || visiveis.get().contains(l.getUnidade().getUuid()))
                .map(Lotacao::getProfissional)
                .filter(p -> p.isAtivo() && p.getEmail() != null && !p.getEmail().isBlank())
                .map(p -> p.getEmail().trim().toLowerCase()).distinct().toList();
    }

    private ComunicadoDto dto(Comunicado c) {
        LocalDate hoje = LocalDate.now();
        boolean vigente = c.getSituacao() == SituacaoComunicado.PUBLICADO && !c.getInicio().isAfter(hoje) && !c.getFim().isBefore(hoje);
        return new ComunicadoDto(c.getUuid(), c.getTitulo(), c.getMensagem(), c.getPrioridade(), c.getPublico(), c.getInicio(), c.getFim(),
                c.getUnidade() != null ? c.getUnidade().getUuid() : null, c.getUnidade() != null ? c.getUnidade().getNome() : null,
                new ArrayList<>(c.getCanais().stream().map(CanalComunicacao::getUuid).toList()), c.getSituacao(), vigente, c.getPublicadoEm(), c.getEncerradoEm(),
                c.getMotivoEncerramento(), publicacaoRepository.findDoComunicado(c.getUuid()).stream().map(ComunicacaoService::dto).toList(),
                c.getCampanha() != null ? c.getCampanha().getUuid() : null, c.getCampanha() != null ? c.getCampanha().getNome() : null);
    }

    private ConteudoEducativoDto dto(ConteudoEducativo c) {
        SituacaoConteudo situacao = c.getSituacao() == SituacaoConteudo.PUBLICADO && c.getValidade().isBefore(LocalDate.now()) ? SituacaoConteudo.EXPIRADO : c.getSituacao();
        return new ConteudoEducativoDto(c.getUuid(), c.getGrupoId(), c.getVersao(), c.getTitulo(), c.getResumo(), c.getTexto(), c.getTema(), c.getPublico(), c.getFaixaEtaria(),
                c.getValidade(), situacao, c.getMotivoDevolucao(), c.getPublicadoEm(), publicacaoRepository.findDoConteudo(c.getUuid()).stream().map(ComunicacaoService::dto).toList());
    }

    private static CanalComunicacaoDto dto(CanalComunicacao c) {
        return new CanalComunicacaoDto(c.getUuid(), c.getTipo(), c.getNome(), c.getDescricao(), c.isAtivo());
    }

    private static PublicacaoDto dto(Publicacao p) {
        return new PublicacaoDto(p.getUuid(), p.getCanal().getNome(), p.getPublicadaEm(), p.getSituacao(), p.getEnviados(), p.getFalhas(), p.getDetalhe());
    }

    private Comunicado comunicado(UUID uuid) {
        return comunicadoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o comunicado " + uuid + " em nossos registros."));
    }

    private ConteudoEducativo conteudo(UUID uuid) {
        return conteudoRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o conteúdo " + uuid + " em nossos registros."));
    }

    private static void exigirSituacao(ConteudoEducativo c, SituacaoConteudo esperada) {
        if (c.getSituacao() != esperada) {
            throw new ResourceUnprocessableEntityException("Esta ação não vale para o conteúdo nesta situação.");
        }
    }

    /** O CPF do autor; com a autorização desligada, não há usuário. */
    private static String autor() {
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null ? cpf : "SISTEMA";
    }

    private static String texto(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
