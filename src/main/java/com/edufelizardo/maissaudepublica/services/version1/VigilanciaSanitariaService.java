package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceForbiddenException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AtividadeEstabelecimento;
import com.edufelizardo.maissaudepublica.models.DenunciaSanitaria;
import com.edufelizardo.maissaudepublica.models.EstabelecimentoRegulado;
import com.edufelizardo.maissaudepublica.models.InspecaoSanitaria;
import com.edufelizardo.maissaudepublica.models.IrregularidadeSanitaria;
import com.edufelizardo.maissaudepublica.models.LicencaSanitaria;
import com.edufelizardo.maissaudepublica.models.Microarea;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.RoteiroInspecao;
import com.edufelizardo.maissaudepublica.models.Surto;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.VersaoRoteiroInspecao;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ConclusaoDenunciaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DenunciaSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.EstabelecimentoReguladoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.InspecaoSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.LicencaSanitariaRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RoteiroInspecaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DocumentosDoPacienteDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.VigilanciaSanitariaDtos;
import com.edufelizardo.maissaudepublica.models.enuns.DonoDocumento;
import com.edufelizardo.maissaudepublica.models.enuns.GrauRiscoSanitario;
import com.edufelizardo.maissaudepublica.models.enuns.GravidadeIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoInspecao;
import com.edufelizardo.maissaudepublica.models.enuns.RespostaRoteiro;
import com.edufelizardo.maissaudepublica.models.enuns.ResultadoInspecao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoDenuncia;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoIrregularidade;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoLicencaSanitaria;
import com.edufelizardo.maissaudepublica.repositories.DenunciaSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.EstabelecimentoReguladoRepository;
import com.edufelizardo.maissaudepublica.repositories.InspecaoSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.IrregularidadeSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.LicencaSanitariaRepository;
import com.edufelizardo.maissaudepublica.repositories.MicroareaRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import com.edufelizardo.maissaudepublica.repositories.RoteiroInspecaoRepository;
import com.edufelizardo.maissaudepublica.repositories.SurtoRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VersaoRoteiroInspecaoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vigilância Sanitária, fatia V3a da Vigilância (ADR-0199, desenho na ADR-0110): o estabelecimento regulado com as atividades e o
 * grau de risco (RDC 153/2017), a licença sanitária guardada como histórico, a denúncia, o roteiro de inspeção versionado, a
 * inspeção assinada pelo fiscal logado e as irregularidades com o prazo de correção. O auto de infração, o processo administrativo
 * sanitário e as medidas cautelares ficam na V3b.
 */
@Service
public class VigilanciaSanitariaService {

    public static final String INSPECIONAR = "VISA.INSPECIONAR";
    public static final String GERENCIAR = "VISA.GERENCIAR";
    static final String[] LEITURA = {INSPECIONAR, GERENCIAR};
    /** Renovação aceita a partir de 60 dias do vencimento. */
    static final int DIAS_RENOVACAO = 60;
    /** Licença vencendo em até 30 dias entra nas pendências. */
    static final int DIAS_VENCENDO = 30;
    /** Denúncia sem conclusão há mais de 30 dias entra nas pendências. */
    static final int DIAS_DENUNCIA_PARADA = 30;
    static final Set<DonoDocumento> DONOS = Set.of(DonoDocumento.ESTABELECIMENTO, DonoDocumento.INSPECAO_SANITARIA);
    private static final ObjectMapper JSON = new ObjectMapper();

    @Autowired private EstabelecimentoReguladoRepository estabelecimentoRepository;
    @Autowired private LicencaSanitariaRepository licencaRepository;
    @Autowired private RoteiroInspecaoRepository roteiroRepository;
    @Autowired private VersaoRoteiroInspecaoRepository versaoRoteiroRepository;
    @Autowired private DenunciaSanitariaRepository denunciaRepository;
    @Autowired private InspecaoSanitariaRepository inspecaoRepository;
    @Autowired private IrregularidadeSanitariaRepository irregularidadeRepository;
    @Autowired private MicroareaRepository microareaRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private SurtoRepository surtoRepository;
    @Autowired private DocumentoService documentoService;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Estabelecimentos ───────────────────────────────────────────────────────────────────────────

    /** Os estabelecimentos; busca (nome, fantasia ou documento) e risco filtram. */
    @Transactional(readOnly = true)
    public List<VigilanciaSanitariaDtos.Estabelecimento> estabelecimentos(String busca, GrauRiscoSanitario risco) {
        controleDeAcesso.exigirAlguma(LEITURA);
        String b = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT);
        String bDigitos = b.replaceAll("\\D", "");
        Contexto c = contexto();
        return estabelecimentoRepository.findTodos().stream()
                .filter(e -> b.isEmpty() || e.getRazaoSocial().toLowerCase(Locale.ROOT).contains(b)
                        || (e.getNomeFantasia() != null && e.getNomeFantasia().toLowerCase(Locale.ROOT).contains(b))
                        || (!bDigitos.isEmpty() && e.getDocumento().contains(bDigitos)))
                .filter(e -> risco == null || risco(e) == risco)
                .map(e -> dto(e, c)).toList();
    }

    @Transactional(readOnly = true)
    public VigilanciaSanitariaDtos.EstabelecimentoDetalhe estabelecimento(UUID id) {
        controleDeAcesso.exigirAlguma(LEITURA);
        EstabelecimentoRegulado e = estabelecimentoOu404(id);
        ContextoAuditoria.registro(e.getUuid());
        Contexto c = contexto();
        LocalDate hoje = LocalDate.now();
        return new VigilanciaSanitariaDtos.EstabelecimentoDetalhe(dto(e, c),
                c.licencas.getOrDefault(e.getUuid(), List.of()).stream().map(l -> dto(l, hoje)).toList(),
                inspecaoRepository.findByEstabelecimento_UuidOrderByDataInspecaoDescRegistradoEmDesc(e.getUuid()).stream().map(this::dto).toList(),
                irregularidadeRepository.findByEstabelecimento_UuidOrderByRegistradoEmAsc(e.getUuid()).stream().map(r -> dto(r, hoje)).toList(),
                documentoService.doDono(DonoDocumento.ESTABELECIMENTO, e.getUuid()));
    }

    /** O cadastro: documento válido (400) e único (409), atividades sem CNAE repetido (400), microárea e unidade existentes (404). */
    @Transactional
    public VigilanciaSanitariaDtos.Estabelecimento criarEstabelecimento(EstabelecimentoReguladoRequestDto dto) {
        controleDeAcesso.exigirAlguma(LEITURA);
        String documento = documento(dto.getDocumento());
        if (estabelecimentoRepository.findByDocumento(documento).isPresent()) {
            throw new ResourceConflictException("Já existe um estabelecimento com este CPF ou CNPJ.");
        }
        EstabelecimentoRegulado e = new EstabelecimentoRegulado();
        e.setDocumento(documento);
        e.setAtivo(true);
        e.setRegistradoEm(Instant.now());
        e.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        preencher(e, dto);
        estabelecimentoRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e, contexto());
    }

    /** Altera o cadastro e as atividades (as que saem são removidas; o CNAE que fica mantém o registro). */
    @Transactional
    public VigilanciaSanitariaDtos.Estabelecimento atualizarEstabelecimento(UUID id, EstabelecimentoReguladoRequestDto dto) {
        controleDeAcesso.exigirAlguma(LEITURA);
        EstabelecimentoRegulado e = estabelecimentoOu404(id);
        String documento = documento(dto.getDocumento());
        estabelecimentoRepository.findByDocumento(documento).filter(o -> !o.getUuid().equals(id)).ifPresent(o -> {
            throw new ResourceConflictException("Já existe um estabelecimento com este CPF ou CNPJ.");
        });
        e.setDocumento(documento);
        if (dto.getAtivo() != null) {
            e.setAtivo(dto.getAtivo());
        }
        preencher(e, dto);
        estabelecimentoRepository.save(e);
        ContextoAuditoria.registro(e.getUuid());
        return dto(e, contexto());
    }

    private void preencher(EstabelecimentoRegulado e, EstabelecimentoReguladoRequestDto dto) {
        if ((dto.getLatitude() == null) != (dto.getLongitude() == null)) {
            throw new ResourceBadRequestException("A localização pede a latitude e a longitude.");
        }
        e.setRazaoSocial(dto.getRazaoSocial().trim());
        e.setNomeFantasia(limpo(dto.getNomeFantasia()));
        e.setEndereco(dto.getEndereco().trim());
        e.setLatitude(dto.getLatitude());
        e.setLongitude(dto.getLongitude());
        e.setMicroarea(dto.getMicroareaId() == null ? null : microareaRepository.findById(dto.getMicroareaId()).orElseThrow(() ->
                new ResourceNotFoundException("Não foi possível encontrar a microárea " + dto.getMicroareaId() + " em nossos registros.")));
        e.setUnidade(dto.getUnidadeId() == null ? null : unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() ->
                new ResourceNotFoundException("Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros.")));
        e.setTelefone(limpo(dto.getTelefone()));
        e.setResponsavelLegal(limpo(dto.getResponsavelLegal()));
        e.setResponsavelTecnico(limpo(dto.getResponsavelTecnico()));
        e.setRegistroConselhoRt(limpo(dto.getRegistroConselhoRt()));
        Map<String, EstabelecimentoReguladoRequestDto.Atividade> novas = new LinkedHashMap<>();
        for (EstabelecimentoReguladoRequestDto.Atividade a : dto.getAtividades()) {
            String cnae = a.getCnae().replaceAll("\\D", "");
            if (novas.put(cnae, a) != null) {
                throw new ResourceBadRequestException("A atividade " + cnae + " está repetida.");
            }
        }
        e.getAtividades().removeIf(a -> !novas.containsKey(a.getCnae()));
        Map<String, AtividadeEstabelecimento> atuais = e.getAtividades().stream().collect(Collectors.toMap(AtividadeEstabelecimento::getCnae, Function.identity()));
        novas.forEach((cnae, a) -> {
            AtividadeEstabelecimento x = atuais.get(cnae);
            if (x == null) {
                x = new AtividadeEstabelecimento(null, e, cnae, a.getDescricao().trim(), a.getRisco());
                e.getAtividades().add(x);
            } else {
                x.setDescricao(a.getDescricao().trim());
                x.setRisco(a.getRisco());
            }
        });
    }

    // ── Licença ────────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VigilanciaSanitariaDtos.Licenca> licencas() {
        controleDeAcesso.exigirAlguma(LEITURA);
        LocalDate hoje = LocalDate.now();
        return licencaRepository.findTodas().stream().map(l -> dto(l, hoje)).toList();
    }

    /**
     * Concede a licença (risco II e III) ou a dispensa (risco I, sem validade, 400 se informada). Estabelecimento ativo (422); emissão
     * não futura (400); validade depois da emissão (400), um ano por padrão. Risco III só com inspeção satisfatória nos 12 meses
     * anteriores (422). Irregularidade gravíssima aberta impede a licença (422); a suspensa só se libera pelo processo (422). A
     * renovação vale a partir de 60 dias do vencimento (409), e a dispensa não se repete com o risco ainda baixo (409).
     */
    @Transactional
    public VigilanciaSanitariaDtos.Licenca concederLicenca(UUID estabelecimentoId, LicencaSanitariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        EstabelecimentoRegulado e = estabelecimentoOu404(estabelecimentoId);
        if (!e.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O estabelecimento está inativo.");
        }
        LocalDate emitida = dto.getEmitidaEm() != null ? dto.getEmitidaEm() : LocalDate.now();
        if (emitida.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A emissão não pode ser no futuro.");
        }
        GrauRiscoSanitario risco = risco(e);
        boolean dispensa = risco == GrauRiscoSanitario.BAIXO;
        List<LicencaSanitaria> historico = licencaRepository.findByEstabelecimento_UuidOrderByEmitidaEmDescRegistradoEmDesc(e.getUuid());
        LicencaSanitaria atual = historico.isEmpty() ? null : historico.get(0);
        if (atual != null && atual.getSituacao() == SituacaoLicencaSanitaria.SUSPENSA) {
            throw new ResourceUnprocessableEntityException("A licença está suspensa: só a decisão do processo sanitário a libera.");
        }
        LocalDate validade = null;
        InspecaoSanitaria base = ultimaSatisfatoria(e, emitida);
        if (dispensa) {
            if (dto.getValidade() != null) {
                throw new ResourceBadRequestException("A dispensa de licenciamento (risco I) não tem validade.");
            }
            if (atual != null && atual.isDispensa() && atual.getSituacao() == SituacaoLicencaSanitaria.VIGENTE) {
                throw new ResourceConflictException("O estabelecimento já está dispensado do licenciamento.");
            }
        } else {
            validade = dto.getValidade() != null ? dto.getValidade() : emitida.plusYears(1);
            if (!validade.isAfter(emitida)) {
                throw new ResourceBadRequestException("A validade precisa ser depois da emissão.");
            }
            if (atual != null && !atual.isDispensa() && atual.getSituacao() == SituacaoLicencaSanitaria.VIGENTE
                    && atual.getValidade().isAfter(emitida.plusDays(DIAS_RENOVACAO))) {
                throw new ResourceConflictException("A licença " + atual.getNumero() + " vale até " + atual.getValidade()
                        + ": a renovação é a partir de " + DIAS_RENOVACAO + " dias do vencimento.");
            }
            boolean gravissima = irregularidadeRepository.findByEstabelecimento_UuidOrderByRegistradoEmAsc(e.getUuid()).stream()
                    .anyMatch(r -> r.getSituacao() == SituacaoIrregularidade.ABERTA && r.getGravidade() == GravidadeIrregularidade.GRAVISSIMA);
            if (gravissima) {
                throw new ResourceUnprocessableEntityException("Há irregularidade gravíssima aberta: corrija antes de licenciar.");
            }
            if (risco == GrauRiscoSanitario.ALTO && base == null) {
                throw new ResourceUnprocessableEntityException("Atividade de risco III: a licença pede uma inspeção satisfatória nos últimos 12 meses.");
            }
        }
        LicencaSanitaria l = new LicencaSanitaria();
        l.setEstabelecimento(e);
        l.setNumero(numero(emitida.getYear(), licencaRepository::countByNumeroStartingWith));
        l.setDispensa(dispensa);
        l.setEmitidaEm(emitida);
        l.setValidade(validade);
        l.setSituacao(SituacaoLicencaSanitaria.VIGENTE);
        l.setObservacao(limpo(dto.getObservacao()));
        l.setInspecao(base);
        l.setRegistradoEm(Instant.now());
        l.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        licencaRepository.save(l);
        ContextoAuditoria.registro(l.getUuid());
        ContextoAuditoria.detalhe((dispensa ? "Dispensa de licenciamento " : "Licença sanitária ") + l.getNumero() + ".");
        return dto(l, LocalDate.now());
    }

    private InspecaoSanitaria ultimaSatisfatoria(EstabelecimentoRegulado e, LocalDate ate) {
        return inspecaoRepository.findByEstabelecimento_UuidOrderByDataInspecaoDescRegistradoEmDesc(e.getUuid()).stream()
                .filter(i -> i.getResultado() == ResultadoInspecao.SATISFATORIA && !i.getDataInspecao().isAfter(ate)
                        && i.getDataInspecao().isAfter(ate.minusMonths(12)))
                .findFirst().orElse(null);
    }

    // ── Roteiros ───────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VigilanciaSanitariaDtos.Roteiro> roteiros() {
        controleDeAcesso.exigirAlguma(LEITURA);
        return roteiroRepository.findAllByOrderByNomeAsc().stream().map(this::dto).toList();
    }

    /** O roteiro com a primeira versão: código e nome (400), código único (409), itens sem código repetido (400). */
    @Transactional
    public VigilanciaSanitariaDtos.Roteiro criarRoteiro(RoteiroInspecaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        String codigo = limpo(dto.getCodigo());
        String nome = limpo(dto.getNome());
        if (codigo == null || nome == null) {
            throw new ResourceBadRequestException("Informe o código e o nome do roteiro.");
        }
        String cod = codigo.toUpperCase(Locale.ROOT);
        if (roteiroRepository.findByCodigo(cod).isPresent()) {
            throw new ResourceConflictException("Já existe o roteiro " + cod + ".");
        }
        RoteiroInspecao r = roteiroRepository.save(new RoteiroInspecao(null, cod, nome, true, Instant.now()));
        versao(r, 1, dto.getItens());
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    /** Nova versão dos itens; as inspeções antigas seguem com a versão que responderam. */
    @Transactional
    public VigilanciaSanitariaDtos.Roteiro novaVersaoRoteiro(UUID id, RoteiroInspecaoRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        RoteiroInspecao r = roteiroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o roteiro " + id + " em nossos registros."));
        int ultima = versaoRoteiroRepository.findFirstByRoteiro_UuidOrderByVersaoDesc(id).map(VersaoRoteiroInspecao::getVersao).orElse(0);
        if (limpo(dto.getNome()) != null) {
            r.setNome(dto.getNome().trim());
        }
        versao(r, ultima + 1, dto.getItens());
        ContextoAuditoria.registro(r.getUuid());
        return dto(r);
    }

    private void versao(RoteiroInspecao r, int numero, List<RoteiroInspecaoRequestDto.Item> itens) {
        Set<String> codigos = new HashSet<>();
        List<VigilanciaSanitariaDtos.ItemRoteiro> lista = new ArrayList<>();
        for (RoteiroInspecaoRequestDto.Item i : itens) {
            String cod = i.getCodigo().trim().toUpperCase(Locale.ROOT);
            if (!codigos.add(cod)) {
                throw new ResourceBadRequestException("O item " + cod + " está repetido.");
            }
            lista.add(new VigilanciaSanitariaDtos.ItemRoteiro(cod, i.getTexto().trim(), i.getGravidade()));
        }
        String definicao;
        try {
            definicao = JSON.writeValueAsString(Map.of("itens", lista));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        versaoRoteiroRepository.save(new VersaoRoteiroInspecao(null, r, numero, definicao, Instant.now(), UsuarioAutenticado.cpf()));
    }

    static List<VigilanciaSanitariaDtos.ItemRoteiro> itens(VersaoRoteiroInspecao v) {
        try {
            Map<String, List<VigilanciaSanitariaDtos.ItemRoteiro>> m = JSON.readValue(v.getDefinicao(), new TypeReference<>() {
            });
            return m.getOrDefault("itens", List.of());
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Roteiro com definição inválida: " + v.getUuid(), ex);
        }
    }

    // ── Denúncias ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VigilanciaSanitariaDtos.Denuncia> denuncias(SituacaoDenuncia situacao) {
        controleDeAcesso.exigirAlguma(LEITURA);
        Map<UUID, Integer> inspecoes = inspecoesPorDenuncia();
        boolean gerencia = controleDeAcesso.temAlguma(GERENCIAR);
        return denunciaRepository.findTodas().stream().filter(d -> situacao == null || d.getSituacao() == situacao)
                .map(d -> dto(d, inspecoes, gerencia)).toList();
    }

    /** Registra a denúncia com o protocolo do ano; nasce recebida. */
    @Transactional
    public VigilanciaSanitariaDtos.Denuncia registrarDenuncia(DenunciaSanitariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        String descricaoEstab = limpo(dto.getEstabelecimentoDescricao());
        if (dto.getEstabelecimentoId() == null && descricaoEstab == null) {
            throw new ResourceBadRequestException("Informe o estabelecimento denunciado: escolha um cadastrado ou descreva.");
        }
        String nome = limpo(dto.getDenuncianteNome());
        String contato = limpo(dto.getDenuncianteContato());
        boolean anonima = Boolean.TRUE.equals(dto.getAnonima());
        if (anonima && (nome != null || contato != null)) {
            throw new ResourceBadRequestException("A denúncia anônima não leva os dados do denunciante.");
        }
        if (!anonima && nome == null) {
            throw new ResourceBadRequestException("Informe o nome do denunciante ou marque a denúncia como anônima.");
        }
        DenunciaSanitaria d = new DenunciaSanitaria();
        d.setProtocolo(numero(LocalDate.now().getYear(), denunciaRepository::countByProtocoloStartingWith));
        d.setCanal(dto.getCanal());
        d.setDescricao(dto.getDescricao().trim());
        d.setEstabelecimento(dto.getEstabelecimentoId() == null ? null : estabelecimentoOu404(dto.getEstabelecimentoId()));
        d.setEstabelecimentoDescricao(descricaoEstab);
        d.setAnonima(anonima);
        d.setDenuncianteNome(nome);
        d.setDenuncianteContato(contato);
        d.setSituacao(SituacaoDenuncia.RECEBIDA);
        d.setRecebidaEm(Instant.now());
        d.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        denunciaRepository.save(d);
        ContextoAuditoria.registro(d.getUuid());
        ContextoAuditoria.detalhe("Denúncia sanitária " + d.getProtocolo() + ".");
        return dto(d, Map.of(), true);
    }

    /** Conclui a denúncia aberta (422): procedente só com inspeção ligada (422); a situação final é procedente ou improcedente (400). */
    @Transactional
    public VigilanciaSanitariaDtos.Denuncia concluirDenuncia(UUID id, ConclusaoDenunciaRequestDto dto) {
        controleDeAcesso.exigirAlguma(GERENCIAR);
        DenunciaSanitaria d = denunciaOu404(id);
        if (d.getSituacao() != SituacaoDenuncia.RECEBIDA && d.getSituacao() != SituacaoDenuncia.EM_APURACAO) {
            throw new ResourceUnprocessableEntityException("A denúncia já foi concluída.");
        }
        if (dto.getSituacao() != SituacaoDenuncia.PROCEDENTE && dto.getSituacao() != SituacaoDenuncia.IMPROCEDENTE) {
            throw new ResourceBadRequestException("A conclusão é procedente ou improcedente.");
        }
        if (dto.getSituacao() == SituacaoDenuncia.PROCEDENTE && inspecaoRepository.findByDenuncia_Uuid(id).isEmpty()) {
            throw new ResourceUnprocessableEntityException("A denúncia procedente pede a inspeção que a apurou.");
        }
        d.setSituacao(dto.getSituacao());
        d.setConclusao(dto.getConclusao().trim());
        d.setConcluidaEm(Instant.now());
        d.setConcluidaPorCpf(UsuarioAutenticado.cpf());
        denunciaRepository.save(d);
        ContextoAuditoria.registro(d.getUuid());
        return dto(d, inspecoesPorDenuncia(), true);
    }

    private Map<UUID, Integer> inspecoesPorDenuncia() {
        Map<UUID, Integer> m = new HashMap<>();
        inspecaoRepository.findTodas().stream().filter(i -> i.getDenuncia() != null).forEach(i -> m.merge(i.getDenuncia().getUuid(), 1, Integer::sum));
        return m;
    }

    // ── Inspeções ──────────────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VigilanciaSanitariaDtos.Inspecao> inspecoes(UUID estabelecimentoId) {
        controleDeAcesso.exigirAlguma(LEITURA);
        return inspecaoRepository.findTodas().stream()
                .filter(i -> estabelecimentoId == null || i.getEstabelecimento().getUuid().equals(estabelecimentoId)).map(this::dto).toList();
    }

    @Transactional(readOnly = true)
    public VigilanciaSanitariaDtos.Inspecao inspecao(UUID id) {
        controleDeAcesso.exigirAlguma(LEITURA);
        InspecaoSanitaria i = inspecaoOu404(id);
        ContextoAuditoria.registro(i.getUuid());
        return dto(i);
    }

    /**
     * Registra a inspeção. Estabelecimento ativo (422); data não futura (400). O motivo pede o vínculo e o vínculo pede o motivo
     * (400): denúncia aberta (422) do mesmo estabelecimento (422), que passa a em apuração; surto; inspeção de origem do mesmo
     * estabelecimento (422) no retorno, que também confirma as correções (só no retorno, 400; abertas e do estabelecimento, 422). O
     * roteiro é respondido por inteiro (400), e cada item não conforme pede a irregularidade (400). Inspeção satisfatória não tem
     * irregularidade (400); o prazo de correção não é antes da inspeção (400). Os fiscais pela matrícula (404); com login, quem
     * registra está entre eles (403) e assina.
     */
    @Transactional
    public VigilanciaSanitariaDtos.Inspecao registrarInspecao(InspecaoSanitariaRequestDto dto) {
        controleDeAcesso.exigirAlguma(INSPECIONAR);
        EstabelecimentoRegulado e = estabelecimentoOu404(dto.getEstabelecimentoId());
        if (!e.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O estabelecimento está inativo.");
        }
        LocalDate data = dto.getDataInspecao() != null ? dto.getDataInspecao() : LocalDate.now();
        if (data.isAfter(LocalDate.now())) {
            throw new ResourceBadRequestException("A inspeção não pode ser de data futura.");
        }
        MotivoInspecao motivo = dto.getMotivo();
        vinculo(motivo == MotivoInspecao.DENUNCIA, dto.getDenunciaId() != null, "A inspeção por denúncia pede a denúncia, e só ela.");
        vinculo(motivo == MotivoInspecao.SURTO, dto.getSurtoId() != null, "A inspeção por surto pede o surto, e só ela.");
        vinculo(motivo == MotivoInspecao.RETORNO, dto.getInspecaoOrigemId() != null, "A inspeção de retorno pede a inspeção de origem, e só ela.");
        List<UUID> corrigidasIds = dto.getCorrigidas() == null ? List.of() : dto.getCorrigidas().stream().distinct().toList();
        if (!corrigidasIds.isEmpty() && motivo != MotivoInspecao.RETORNO) {
            throw new ResourceBadRequestException("A correção das irregularidades é confirmada na inspeção de retorno.");
        }

        DenunciaSanitaria denuncia = null;
        if (dto.getDenunciaId() != null) {
            denuncia = denunciaOu404(dto.getDenunciaId());
            if (denuncia.getSituacao() != SituacaoDenuncia.RECEBIDA && denuncia.getSituacao() != SituacaoDenuncia.EM_APURACAO) {
                throw new ResourceUnprocessableEntityException("A denúncia " + denuncia.getProtocolo() + " já foi concluída.");
            }
            if (denuncia.getEstabelecimento() != null && !denuncia.getEstabelecimento().getUuid().equals(e.getUuid())) {
                throw new ResourceUnprocessableEntityException("A denúncia " + denuncia.getProtocolo() + " é de outro estabelecimento.");
            }
        }
        Surto surto = dto.getSurtoId() == null ? null : surtoRepository.findById(dto.getSurtoId()).orElseThrow(() ->
                new ResourceNotFoundException("Não foi possível encontrar o surto " + dto.getSurtoId() + " em nossos registros."));
        InspecaoSanitaria origem = null;
        if (dto.getInspecaoOrigemId() != null) {
            origem = inspecaoOu404(dto.getInspecaoOrigemId());
            if (!origem.getEstabelecimento().getUuid().equals(e.getUuid())) {
                throw new ResourceUnprocessableEntityException("A inspeção de origem é de outro estabelecimento.");
            }
        }
        List<IrregularidadeSanitaria> corrigidas = new ArrayList<>();
        for (UUID rid : corrigidasIds) {
            IrregularidadeSanitaria r = irregularidadeRepository.findById(rid).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a irregularidade " + rid + " em nossos registros."));
            if (!r.getEstabelecimento().getUuid().equals(e.getUuid()) || r.getSituacao() != SituacaoIrregularidade.ABERTA) {
                throw new ResourceUnprocessableEntityException("Só se corrige irregularidade aberta deste estabelecimento.");
            }
            corrigidas.add(r);
        }

        // O roteiro respondido por inteiro, e os itens não conformes com a irregularidade.
        VersaoRoteiroInspecao versao = null;
        Map<String, RespostaRoteiro> respostas = dto.getRespostas() == null ? Map.of() : dto.getRespostas();
        Map<String, RespostaRoteiro> normalizadas = new TreeMap<>();
        respostas.forEach((k, v) -> normalizadas.put(k.trim().toUpperCase(Locale.ROOT), v));
        List<InspecaoSanitariaRequestDto.Irregularidade> irregs = dto.getIrregularidades() == null ? List.of() : dto.getIrregularidades();
        if (dto.getRoteiroVersaoId() != null) {
            versao = versaoRoteiroRepository.findById(dto.getRoteiroVersaoId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar a versão de roteiro " + dto.getRoteiroVersaoId() + " em nossos registros."));
            Set<String> codigos = itens(versao).stream().map(VigilanciaSanitariaDtos.ItemRoteiro::codigo).collect(Collectors.toCollection(LinkedHashSet::new));
            for (String c : codigos) {
                if (normalizadas.get(c) == null) {
                    throw new ResourceBadRequestException("Responda o item " + c + " do roteiro.");
                }
            }
            for (String c : normalizadas.keySet()) {
                if (!codigos.contains(c)) {
                    throw new ResourceBadRequestException("O item " + c + " não é do roteiro.");
                }
            }
            Set<String> comIrregularidade = irregs.stream().map(i -> limpo(i.getItemCodigo())).filter(Objects::nonNull)
                    .map(x -> x.toUpperCase(Locale.ROOT)).collect(Collectors.toSet());
            for (Map.Entry<String, RespostaRoteiro> r : normalizadas.entrySet()) {
                if (r.getValue() == RespostaRoteiro.NAO_CONFORME && !comIrregularidade.contains(r.getKey())) {
                    throw new ResourceBadRequestException("O item " + r.getKey() + " não conforme pede a irregularidade.");
                }
            }
            for (String c : comIrregularidade) {
                if (normalizadas.get(c) != RespostaRoteiro.NAO_CONFORME) {
                    throw new ResourceBadRequestException("A irregularidade do item " + c + " pede o item não conforme.");
                }
            }
        } else if (!normalizadas.isEmpty()) {
            throw new ResourceBadRequestException("As respostas pedem o roteiro.");
        } else if (irregs.stream().anyMatch(i -> limpo(i.getItemCodigo()) != null)) {
            throw new ResourceBadRequestException("O item da irregularidade pede o roteiro.");
        }
        if (dto.getResultado() == ResultadoInspecao.SATISFATORIA && !irregs.isEmpty()) {
            throw new ResourceBadRequestException("A inspeção satisfatória não tem irregularidade.");
        }
        for (InspecaoSanitariaRequestDto.Irregularidade i : irregs) {
            if (i.getPrazoCorrecao() != null && i.getPrazoCorrecao().isBefore(data)) {
                throw new ResourceBadRequestException("O prazo de correção não pode ser antes da inspeção.");
            }
        }

        // Os fiscais; com login, quem registra é um deles e assina.
        Set<Profissional> fiscais = new LinkedHashSet<>();
        for (String m : dto.getFiscais()) {
            String mat = m.trim();
            fiscais.add(profissionalRepository.findByMatricula(mat).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o profissional de matrícula " + mat + " em nossos registros.")));
        }
        String cpf = UsuarioAutenticado.cpf();
        if (cpf != null && fiscais.stream().noneMatch(p -> cpf.equals(p.getCpf()))) {
            throw new ResourceForbiddenException("A inspeção é assinada por quem está logado: inclua a sua própria matrícula entre os fiscais.");
        }

        InspecaoSanitaria i = new InspecaoSanitaria();
        i.setEstabelecimento(e);
        i.setMotivo(motivo);
        i.setDataInspecao(data);
        i.setDenuncia(denuncia);
        i.setSurto(surto);
        i.setInspecaoOrigem(origem);
        i.setRoteiroVersao(versao);
        try {
            i.setRespostas(versao == null ? null : JSON.writeValueAsString(normalizadas));
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
        i.setResultado(dto.getResultado());
        i.setRelatorio(dto.getRelatorio().trim());
        i.setFiscais(fiscais);
        i.setRegistradoEm(Instant.now());
        i.setRegistradoPorCpf(cpf);
        inspecaoRepository.save(i);

        List<IrregularidadeSanitaria> novas = new ArrayList<>();
        Instant agora = Instant.now();
        for (InspecaoSanitariaRequestDto.Irregularidade x : irregs) {
            String item = limpo(x.getItemCodigo());
            IrregularidadeSanitaria r = new IrregularidadeSanitaria(null, i, e, item == null ? null : item.toUpperCase(Locale.ROOT), x.getDescricao().trim(),
                    limpo(x.getEnquadramento()), x.getGravidade(), x.getPrazoCorrecao(), SituacaoIrregularidade.ABERTA, null, null, agora);
            novas.add(irregularidadeRepository.save(r));
        }
        for (IrregularidadeSanitaria r : corrigidas) {
            r.setSituacao(SituacaoIrregularidade.CORRIGIDA);
            r.setCorrigidaEm(data);
            r.setInspecaoCorrecao(i);
            irregularidadeRepository.save(r);
        }
        if (denuncia != null) {
            if (denuncia.getEstabelecimento() == null) {
                denuncia.setEstabelecimento(e);
            }
            denuncia.setSituacao(SituacaoDenuncia.EM_APURACAO);
            denunciaRepository.save(denuncia);
        }
        i.setAssinadoPorCpf(cpf);
        i.setHashConteudo(AssinaturaResultado.sha256(conteudo(i, novas, corrigidas)));
        inspecaoRepository.save(i);
        ContextoAuditoria.registro(i.getUuid());
        ContextoAuditoria.detalhe("Inspeção sanitária " + rotulo(motivo.name()).toLowerCase(Locale.ROOT) + ": " + rotulo(dto.getResultado().name()).toLowerCase(Locale.ROOT)
                + (novas.isEmpty() ? "." : ", " + novas.size() + " irregularidade(s)."));
        return dto(i, novas, corrigidas.stream().map(IrregularidadeSanitaria::getUuid).toList());
    }

    private static void vinculo(boolean doMotivo, boolean informado, String mensagem) {
        if (doMotivo != informado) {
            throw new ResourceBadRequestException(mensagem);
        }
    }

    /** O texto canônico assinado: a inspeção, o estabelecimento, o roteiro e as respostas, o resultado, o relatório, os fiscais e as irregularidades. */
    static String conteudo(InspecaoSanitaria i, List<IrregularidadeSanitaria> novas, List<IrregularidadeSanitaria> corrigidas) {
        List<String> linhas = new ArrayList<>(List.of(
                "inspecao=" + i.getUuid(),
                "estabelecimento=" + i.getEstabelecimento().getDocumento(),
                "data=" + i.getDataInspecao(),
                "motivo=" + i.getMotivo(),
                "roteiro=" + (i.getRoteiroVersao() != null ? i.getRoteiroVersao().getUuid() : ""),
                "respostas=" + Objects.toString(i.getRespostas(), ""),
                "resultado=" + i.getResultado(),
                "relatorio=" + i.getRelatorio(),
                "fiscais=" + i.getFiscais().stream().map(Profissional::getMatricula).sorted().collect(Collectors.joining(","))));
        novas.forEach(r -> linhas.add("irregularidade=" + Objects.toString(r.getItemCodigo(), "") + "|" + r.getDescricao() + "|" + r.getGravidade() + "|"
                + Objects.toString(r.getPrazoCorrecao(), "")));
        corrigidas.forEach(r -> linhas.add("corrigida=" + r.getUuid()));
        return String.join("\n", linhas);
    }

    // ── Pendências ─────────────────────────────────────────────────────────────────────────────────

    /** Licença faltando, vencida, vencendo em 30 dias, suspensa ou cassada (risco II e III); prazo de correção vencido; denúncia parada. */
    @Transactional(readOnly = true)
    public VigilanciaSanitariaDtos.Pendencias pendencias() {
        controleDeAcesso.exigirAlguma(LEITURA);
        LocalDate hoje = LocalDate.now();
        Contexto c = contexto();
        List<VigilanciaSanitariaDtos.PendenciaLicenca> licencas = new ArrayList<>();
        for (EstabelecimentoRegulado e : estabelecimentoRepository.findTodos()) {
            GrauRiscoSanitario risco = risco(e);
            if (!e.isAtivo() || risco == GrauRiscoSanitario.BAIXO) {
                continue;
            }
            List<LicencaSanitaria> h = c.licencas.getOrDefault(e.getUuid(), List.of());
            LicencaSanitaria atual = h.isEmpty() ? null : h.get(0);
            String motivo = null;
            if (atual == null || atual.isDispensa()) {
                motivo = "SEM_LICENCA";
            } else {
                String s = situacao(atual, hoje);
                if (!"VIGENTE".equals(s)) {
                    motivo = s;
                } else if (!atual.getValidade().isAfter(hoje.plusDays(DIAS_VENCENDO))) {
                    motivo = "VENCENDO";
                }
            }
            if (motivo != null) {
                licencas.add(new VigilanciaSanitariaDtos.PendenciaLicenca(e.getUuid(), nome(e), risco, motivo,
                        atual != null && !atual.isDispensa() ? atual.getValidade() : null));
            }
        }
        List<VigilanciaSanitariaDtos.Irregularidade> prazos = irregularidadeRepository.findTodas().stream()
                .filter(r -> vencida(r, hoje)).sorted(Comparator.comparing(IrregularidadeSanitaria::getPrazoCorrecao))
                .map(r -> dto(r, hoje)).toList();
        Instant limite = Instant.now().minus(Duration.ofDays(DIAS_DENUNCIA_PARADA));
        Map<UUID, Integer> inspecoes = inspecoesPorDenuncia();
        boolean gerencia = controleDeAcesso.temAlguma(GERENCIAR);
        List<VigilanciaSanitariaDtos.Denuncia> paradas = denunciaRepository.findTodas().stream()
                .filter(d -> (d.getSituacao() == SituacaoDenuncia.RECEBIDA || d.getSituacao() == SituacaoDenuncia.EM_APURACAO) && d.getRecebidaEm().isBefore(limite))
                .map(d -> dto(d, inspecoes, gerencia)).toList();
        return new VigilanciaSanitariaDtos.Pendencias(licencas, prazos, paradas);
    }

    // ── Documentos (D2 parcial) ────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<DocumentosDoPacienteDto.Item> documentos(DonoDocumento dono, UUID donoId) {
        controleDeAcesso.exigirAlguma(LEITURA);
        exigirDono(dono, donoId);
        return documentoService.doDono(dono, donoId);
    }

    @Transactional
    public List<DocumentosDoPacienteDto.Item> anexar(DonoDocumento dono, UUID donoId, UUID tipoId, String titulo, LocalDate data, MultipartFile arquivo) {
        controleDeAcesso.exigirAlguma(LEITURA);
        exigirDono(dono, donoId);
        documentoService.anexarAoDono(dono, donoId, tipoId, titulo, data, arquivo);
        return documentoService.doDono(dono, donoId);
    }

    @Transactional
    public void retirarDocumento(UUID documentoId, String motivo) {
        controleDeAcesso.exigirAlguma(LEITURA);
        documentoService.retirarDoDono(documentoId, DONOS, motivo);
    }

    @Transactional(readOnly = true)
    public DocumentoService.ArquivoDoDocumento abrirDocumento(UUID versaoId) {
        controleDeAcesso.exigirAlguma(LEITURA);
        return documentoService.abrirDoDono(versaoId, DONOS);
    }

    private void exigirDono(DonoDocumento dono, UUID id) {
        if (dono == DonoDocumento.ESTABELECIMENTO) {
            estabelecimentoOu404(id);
        } else {
            inspecaoOu404(id);
        }
    }

    // ── Termo de inspeção ──────────────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public byte[] termo(UUID id) {
        controleDeAcesso.exigirAlguma(LEITURA);
        InspecaoSanitaria i = inspecaoOu404(id);
        ContextoAuditoria.registro(i.getUuid());
        return TermoInspecaoPdf.gerar(i, dto(i));
    }

    // ── Apoio ──────────────────────────────────────────────────────────────────────────────────────

    /** O que os DTOs dos estabelecimentos precisam, lido uma vez. */
    private record Contexto(Map<UUID, List<LicencaSanitaria>> licencas, Map<UUID, Long> abertas, Map<UUID, LocalDate> ultimaInspecao) {
    }

    private Contexto contexto() {
        Map<UUID, List<LicencaSanitaria>> licencas = licencaRepository.findTodas().stream()
                .collect(Collectors.groupingBy(l -> l.getEstabelecimento().getUuid(), LinkedHashMap::new, Collectors.toList()));
        Map<UUID, Long> abertas = irregularidadeRepository.findTodas().stream().filter(r -> r.getSituacao() == SituacaoIrregularidade.ABERTA)
                .collect(Collectors.groupingBy(r -> r.getEstabelecimento().getUuid(), Collectors.counting()));
        Map<UUID, LocalDate> ultima = new HashMap<>();
        inspecaoRepository.findTodas().forEach(i -> ultima.merge(i.getEstabelecimento().getUuid(), i.getDataInspecao(), (a, b) -> a.isAfter(b) ? a : b));
        return new Contexto(licencas, abertas, ultima);
    }

    /** O risco do estabelecimento é o maior entre as atividades. */
    static GrauRiscoSanitario risco(EstabelecimentoRegulado e) {
        return e.getAtividades().stream().map(AtividadeEstabelecimento::getRisco).max(Comparator.naturalOrder()).orElse(GrauRiscoSanitario.BAIXO);
    }

    static String situacao(LicencaSanitaria l, LocalDate hoje) {
        if (l.getSituacao() != SituacaoLicencaSanitaria.VIGENTE) {
            return l.getSituacao().name();
        }
        if (l.isDispensa()) {
            return "DISPENSADA";
        }
        return l.getValidade().isBefore(hoje) ? "VENCIDA" : "VIGENTE";
    }

    private static boolean vencida(IrregularidadeSanitaria r, LocalDate hoje) {
        return r.getSituacao() == SituacaoIrregularidade.ABERTA && r.getPrazoCorrecao() != null && r.getPrazoCorrecao().isBefore(hoje);
    }

    /** O número do ano: 2026/000001. A licença e a denúncia têm séries próprias. */
    private static String numero(int ano, Function<String, Long> contar) {
        String prefixo = ano + "/";
        return prefixo + String.format(Locale.ROOT, "%06d", contar.apply(prefixo) + 1);
    }

    /** CPF (11 dígitos) ou CNPJ (14) com o dígito verificador; aceita a pontuação (400). */
    static String documento(String informado) {
        String d = informado == null ? "" : informado.replaceAll("\\D", "");
        boolean valido = d.length() == 11 ? cpfValido(d) : d.length() == 14 && cnpjValido(d);
        if (!valido) {
            throw new ResourceBadRequestException("Informe um CPF ou CNPJ válido.");
        }
        return d;
    }

    static boolean cpfValido(String d) {
        if (d.chars().distinct().count() == 1) {
            return false;
        }
        for (int n = 9; n <= 10; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += (d.charAt(i) - '0') * (n + 1 - i);
            }
            int dv = (soma * 10) % 11 % 10;
            if (dv != d.charAt(n) - '0') {
                return false;
            }
        }
        return true;
    }

    static boolean cnpjValido(String d) {
        if (d.chars().distinct().count() == 1) {
            return false;
        }
        int[] pesos = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        for (int n = 12; n <= 13; n++) {
            int soma = 0;
            for (int i = 0; i < n; i++) {
                soma += (d.charAt(i) - '0') * pesos[i + 13 - n];
            }
            int resto = soma % 11;
            int dv = resto < 2 ? 0 : 11 - resto;
            if (dv != d.charAt(n) - '0') {
                return false;
            }
        }
        return true;
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    static String nome(EstabelecimentoRegulado e) {
        return e.getNomeFantasia() != null ? e.getNomeFantasia() : e.getRazaoSocial();
    }

    static String rotulo(String nome) {
        String s = nome.replace('_', ' ').toLowerCase(Locale.ROOT);
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private EstabelecimentoRegulado estabelecimentoOu404(UUID id) {
        return estabelecimentoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o estabelecimento " + id + " em nossos registros."));
    }

    private DenunciaSanitaria denunciaOu404(UUID id) {
        return denunciaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a denúncia " + id + " em nossos registros."));
    }

    private InspecaoSanitaria inspecaoOu404(UUID id) {
        return inspecaoRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a inspeção " + id + " em nossos registros."));
    }

    private VigilanciaSanitariaDtos.Estabelecimento dto(EstabelecimentoRegulado e, Contexto c) {
        Microarea m = e.getMicroarea();
        UnidadeDeSaude u = e.getUnidade();
        List<LicencaSanitaria> h = c.licencas.getOrDefault(e.getUuid(), List.of());
        return new VigilanciaSanitariaDtos.Estabelecimento(e.getUuid(), e.getDocumento(), e.getRazaoSocial(), e.getNomeFantasia(), e.getEndereco(),
                e.getLatitude(), e.getLongitude(), m != null ? m.getUuid() : null, m != null ? m.getCodigo() : null, m != null ? m.getArea().getNome() : null,
                u != null ? u.getUuid() : null, u != null ? u.getNome() : null, e.getTelefone(), e.getResponsavelLegal(), e.getResponsavelTecnico(),
                e.getRegistroConselhoRt(), e.isAtivo(), risco(e),
                e.getAtividades().stream().map(a -> new VigilanciaSanitariaDtos.Atividade(a.getCnae(), a.getDescricao(), a.getRisco())).toList(),
                h.isEmpty() ? null : dto(h.get(0), LocalDate.now()), c.abertas.getOrDefault(e.getUuid(), 0L), c.ultimaInspecao.get(e.getUuid()));
    }

    private static VigilanciaSanitariaDtos.Licenca dto(LicencaSanitaria l, LocalDate hoje) {
        return new VigilanciaSanitariaDtos.Licenca(l.getUuid(), l.getEstabelecimento().getUuid(), nome(l.getEstabelecimento()), l.getNumero(), l.isDispensa(),
                l.getEmitidaEm(), l.getValidade(), situacao(l, hoje), l.getObservacao(), l.getInspecao() != null ? l.getInspecao().getUuid() : null);
    }

    private VigilanciaSanitariaDtos.Roteiro dto(RoteiroInspecao r) {
        VersaoRoteiroInspecao v = versaoRoteiroRepository.findFirstByRoteiro_UuidOrderByVersaoDesc(r.getUuid()).orElse(null);
        return new VigilanciaSanitariaDtos.Roteiro(r.getUuid(), r.getCodigo(), r.getNome(), r.isAtivo(), v != null ? v.getUuid() : null,
                v != null ? v.getVersao() : 0, v != null ? itens(v) : List.of());
    }

    private static VigilanciaSanitariaDtos.Denuncia dto(DenunciaSanitaria d, Map<UUID, Integer> inspecoes, boolean gerencia) {
        EstabelecimentoRegulado e = d.getEstabelecimento();
        Instant fim = d.getConcluidaEm() != null ? d.getConcluidaEm() : Instant.now();
        return new VigilanciaSanitariaDtos.Denuncia(d.getUuid(), d.getProtocolo(), d.getCanal(), d.getDescricao(), e != null ? e.getUuid() : null,
                e != null ? nome(e) : null, d.getEstabelecimentoDescricao(), d.isAnonima(), gerencia ? d.getDenuncianteNome() : null,
                gerencia ? d.getDenuncianteContato() : null, d.getSituacao(), d.getRecebidaEm(), d.getConclusao(), d.getConcluidaEm(),
                Duration.between(d.getRecebidaEm(), fim).toDays(), inspecoes.getOrDefault(d.getUuid(), 0));
    }

    private static VigilanciaSanitariaDtos.Irregularidade dto(IrregularidadeSanitaria r, LocalDate hoje) {
        return new VigilanciaSanitariaDtos.Irregularidade(r.getUuid(), r.getInspecao().getUuid(), r.getInspecao().getDataInspecao(), r.getEstabelecimento().getUuid(),
                nome(r.getEstabelecimento()), r.getItemCodigo(), r.getDescricao(), r.getEnquadramento(), r.getGravidade(), r.getPrazoCorrecao(), r.getSituacao(),
                vencida(r, hoje), r.getCorrigidaEm(), r.getInspecaoCorrecao() != null ? r.getInspecaoCorrecao().getUuid() : null);
    }

    private VigilanciaSanitariaDtos.Inspecao dto(InspecaoSanitaria i) {
        List<UUID> corrigidas = irregularidadeRepository.findTodas().stream()
                .filter(r -> r.getInspecaoCorrecao() != null && r.getInspecaoCorrecao().getUuid().equals(i.getUuid())).map(IrregularidadeSanitaria::getUuid).toList();
        return dto(i, irregularidadeRepository.findByInspecao_UuidOrderByRegistradoEmAsc(i.getUuid()), corrigidas);
    }

    private VigilanciaSanitariaDtos.Inspecao dto(InspecaoSanitaria i, List<IrregularidadeSanitaria> irregularidades, List<UUID> corrigidas) {
        VersaoRoteiroInspecao v = i.getRoteiroVersao();
        Map<String, RespostaRoteiro> respostas = Map.of();
        if (i.getRespostas() != null) {
            try {
                respostas = JSON.readValue(i.getRespostas(), new TypeReference<TreeMap<String, RespostaRoteiro>>() {
                });
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        LocalDate hoje = LocalDate.now();
        EstabelecimentoRegulado e = i.getEstabelecimento();
        return new VigilanciaSanitariaDtos.Inspecao(i.getUuid(), e.getUuid(), nome(e), e.getDocumento(), i.getMotivo(), i.getDataInspecao(),
                i.getDenuncia() != null ? i.getDenuncia().getUuid() : null, i.getDenuncia() != null ? i.getDenuncia().getProtocolo() : null,
                i.getSurto() != null ? i.getSurto().getUuid() : null, i.getInspecaoOrigem() != null ? i.getInspecaoOrigem().getUuid() : null,
                v != null ? v.getUuid() : null, v != null ? v.getRoteiro().getNome() : null, v != null ? v.getVersao() : null, v != null ? itens(v) : List.of(),
                respostas, i.getResultado(), i.getRelatorio(),
                i.getFiscais().stream().map(p -> new VigilanciaSanitariaDtos.Fiscal(p.getMatricula(), p.getNome())).toList(),
                irregularidades.stream().map(r -> dto(r, hoje)).toList(), corrigidas, i.getAssinadoPorCpf() != null, i.getHashConteudo(), i.getRegistradoEm());
    }
}
