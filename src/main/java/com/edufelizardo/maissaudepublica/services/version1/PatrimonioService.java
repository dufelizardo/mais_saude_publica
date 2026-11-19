package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.ContextoAuditoria;
import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.*;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.PatrimonioDtos;
import com.edufelizardo.maissaudepublica.models.enuns.ClasseBem;
import com.edufelizardo.maissaudepublica.models.enuns.OrigemBem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoBem;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoTransferenciaBem;
import com.edufelizardo.maissaudepublica.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Patrimônio, fatia PM1 (ADR-0215): o bem patrimonial tombado, o termo de responsabilidade com histórico, a transferência entre unidades
 * em duas etapas e o bem permanente comprado, que nasce a tombar no recebimento definitivo de Compras (ADR-0214).
 */
@Service
public class PatrimonioService {

    public static final String CONSULTAR = "PATRIMONIO.CONSULTAR";
    public static final String GERENCIAR = "PATRIMONIO.GERENCIAR";
    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final Set<SituacaoBem> ATIVOS = EnumSet.of(SituacaoBem.EM_USO, SituacaoBem.OCIOSO);

    @Autowired private BemPatrimonialRepository bemRepository;
    @Autowired private ResponsavelBemRepository responsavelRepository;
    @Autowired private TransferenciaBemRepository transferenciaRepository;
    @Autowired private UnidadeDeSaudeRepository unidadeRepository;
    @Autowired private SetorRepository setorRepository;
    @Autowired private ProfissionalRepository profissionalRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ControleDeAcesso controleDeAcesso;

    // ── Consulta ───────────────────────────────────────────────────────────────────────────────────

    /** Os bens das unidades visíveis; os filtros são opcionais e a busca olha o tombamento, a descrição e o número de série. */
    @Transactional(readOnly = true)
    public List<PatrimonioDtos.Bem> bens(UUID unidadeId, ClasseBem classe, SituacaoBem situacao, String busca) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        String termo = limpo(busca) == null ? null : busca.trim().toLowerCase(Locale.ROOT);
        List<BemPatrimonial> lista = controleDeAcesso.filtrar(bemRepository.findTodos().stream(), b -> java.util.stream.Stream.of(b.getUnidade()), CONSULTAR, GERENCIAR)
                .filter(b -> unidadeId == null || b.getUnidade().getUuid().equals(unidadeId))
                .filter(b -> classe == null || b.getClasse() == classe)
                .filter(b -> situacao == null || b.getSituacao() == situacao)
                .filter(b -> termo == null || contem(b.getNumeroTombamento(), termo) || contem(b.getDescricao(), termo) || contem(b.getNumeroSerie(), termo))
                .toList();
        return dtos(lista);
    }

    /** Os bens sob a responsabilidade de quem está logado ("Meus bens"); só pede o login. */
    @Transactional(readOnly = true)
    public List<PatrimonioDtos.Bem> meus() {
        String cpf = UsuarioAutenticado.cpf();
        if (cpf == null) {
            return List.of();
        }
        List<UUID> ids = responsavelRepository.findBensAtuaisDoCpf(cpf);
        return ids.isEmpty() ? List.of() : dtos(bemRepository.findComRelacoes(ids));
    }

    /** A ficha do bem, com o histórico dos responsáveis e das transferências: para quem vê a unidade ou para o responsável atual (403 aos outros). */
    @Transactional(readOnly = true)
    public PatrimonioDtos.Detalhe bem(UUID id) {
        BemPatrimonial b = bemOu404(id);
        if (!responsavelAtual(b)) {
            controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
            controleDeAcesso.exigirVisivel(b.getUnidade(), CONSULTAR, GERENCIAR);
        }
        return detalhe(b);
    }

    /** As transferências das unidades visíveis (origem ou destino); {@code pendentes} deixa só as que esperam o aceite. */
    @Transactional(readOnly = true)
    public List<PatrimonioDtos.Transferencia> transferencias(Boolean pendentes) {
        controleDeAcesso.exigirAlguma(CONSULTAR, GERENCIAR);
        return controleDeAcesso.filtrar(transferenciaRepository.findTodas().stream(), t -> java.util.stream.Stream.of(t.getOrigem(), t.getDestino()), CONSULTAR, GERENCIAR)
                .filter(t -> !Boolean.TRUE.equals(pendentes) || t.getSituacao() == SituacaoTransferenciaBem.PENDENTE)
                .map(PatrimonioService::dto).toList();
    }

    // ── Cadastro e tombamento ──────────────────────────────────────────────────────────────────────

    /**
     * O bem que não veio de uma compra pelo sistema (doação, cessão, transferência de outro ente ou o acervo antigo): entra tombado, em
     * uso, com o termo de responsabilidade. Aquisição não futura (400); setor da unidade (422); responsável ativo (404/422).
     */
    @Transactional
    public PatrimonioDtos.Detalhe cadastrar(BemPatrimonialRequestDto dto) {
        UnidadeDeSaude unidade = unidadeRepository.findById(dto.getUnidadeId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getUnidadeId() + " em nossos registros."));
        controleDeAcesso.exigir(GERENCIAR, unidade);
        LocalDate hoje = hoje();
        if (dto.getDataAquisicao().isAfter(hoje)) {
            throw new ResourceBadRequestException("A data de aquisição não pode ser futura.");
        }
        Profissional responsavel = profissional(dto.getResponsavelMatricula());
        BemPatrimonial b = new BemPatrimonial();
        b.setUnidade(unidade);
        b.setDescricao(dto.getDescricao().trim());
        b.setClasse(dto.getClasse());
        identificar(b, dto.getMarca(), dto.getModelo(), dto.getNumeroSerie());
        b.setSetor(setor(dto.getSetorId(), unidade));
        b.setOrigem(dto.getOrigem());
        b.setValorAquisicao(dto.getValorAquisicao());
        b.setDataAquisicao(dto.getDataAquisicao());
        b.setDocumento(limpo(dto.getDocumento()));
        b.setObservacao(limpo(dto.getObservacao()));
        b.setRegistradoEm(Instant.now());
        b.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        tombar(b, hoje);
        bemRepository.save(b);
        ContextoAuditoria.registro(b.getUuid());
        novoResponsavel(b, responsavel, hoje);
        ContextoAuditoria.detalhe("Bem " + b.getNumeroTombamento() + " cadastrado");
        return detalhe(b);
    }

    /** Corrige a descrição, a identificação, o setor e a observação; o bem baixado não muda (422). */
    @Transactional
    public PatrimonioDtos.Detalhe editar(UUID id, EdicaoBemRequestDto dto) {
        BemPatrimonial b = bemOu404(id);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (b.getSituacao() == SituacaoBem.BAIXADO) {
            throw new ResourceUnprocessableEntityException("O bem está baixado.");
        }
        b.setDescricao(dto.getDescricao().trim());
        identificar(b, dto.getMarca(), dto.getModelo(), dto.getNumeroSerie());
        b.setSetor(setor(dto.getSetorId(), b.getUnidade()));
        b.setObservacao(limpo(dto.getObservacao()));
        bemRepository.save(b);
        return detalhe(b);
    }

    /** O tombamento do bem comprado, só a tombar (422): o número, a classe, a identificação, o setor e o responsável; fica em uso. */
    @Transactional
    public PatrimonioDtos.Detalhe tombar(UUID id, TombamentoBemRequestDto dto) {
        BemPatrimonial b = bemOu404(id);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (b.getSituacao() != SituacaoBem.A_TOMBAR) {
            throw new ResourceUnprocessableEntityException("O bem já foi tombado (" + b.getNumeroTombamento() + ").");
        }
        Profissional responsavel = profissional(dto.getResponsavelMatricula());
        b.setClasse(dto.getClasse());
        identificar(b, dto.getMarca(), dto.getModelo(), dto.getNumeroSerie());
        b.setSetor(setor(dto.getSetorId(), b.getUnidade()));
        if (limpo(dto.getObservacao()) != null) {
            b.setObservacao(dto.getObservacao().trim());
        }
        LocalDate hoje = hoje();
        tombar(b, hoje);
        bemRepository.save(b);
        novoResponsavel(b, responsavel, hoje);
        ContextoAuditoria.detalhe("Bem tombado com o número " + b.getNumeroTombamento());
        return detalhe(b);
    }

    /**
     * O bem permanente aceito no recebimento definitivo (ADR-0214): uma unidade, um bem a tombar, na unidade da ordem, com o fornecedor,
     * o preço do contrato e a nota. Chamado pelo recebimento, que já conferiu quem atesta.
     */
    void incorporarPeloRecebimento(OrdemFornecimento o, RecebimentoCompra r, ItemRecebimentoCompra item, String descricao, BigDecimal preco,
                                   Fornecedor fornecedor, int quantidade) {
        LocalDate hoje = hoje();
        List<BemPatrimonial> novos = new ArrayList<>();
        for (int n = 0; n < quantidade; n++) {
            BemPatrimonial b = new BemPatrimonial();
            b.setUnidade(o.getUnidade());
            b.setDescricao(descricao);
            b.setSituacao(SituacaoBem.A_TOMBAR);
            b.setOrigem(OrigemBem.COMPRA);
            b.setValorAquisicao(preco);
            b.setDataAquisicao(hoje);
            b.setDocumento(r.getNotaFiscal());
            b.setFornecedor(fornecedor);
            b.setItemRecebimento(item);
            b.setRegistradoEm(Instant.now());
            b.setRegistradoPorCpf(UsuarioAutenticado.cpf());
            novos.add(b);
        }
        bemRepository.saveAll(novos);
    }

    // ── Responsável e uso ──────────────────────────────────────────────────────────────────────────

    /** Troca o responsável do bem tombado e não baixado (422); a mesma pessoa não troca (422). O termo anterior se encerra hoje. */
    @Transactional
    public PatrimonioDtos.Detalhe trocarResponsavel(UUID id, ResponsavelBemRequestDto dto) {
        BemPatrimonial b = bemOu404(id);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (b.getSituacao() == SituacaoBem.A_TOMBAR || b.getSituacao() == SituacaoBem.BAIXADO) {
            throw new ResourceUnprocessableEntityException(b.getSituacao() == SituacaoBem.A_TOMBAR ? "O bem ainda não foi tombado." : "O bem está baixado.");
        }
        Profissional p = profissional(dto.getResponsavelMatricula());
        Optional<ResponsavelBem> atual = responsavelRepository.findAtuais(List.of(b.getUuid())).stream().findFirst();
        if (atual.isPresent() && atual.get().getProfissional().getUuid().equals(p.getUuid())) {
            throw new ResourceUnprocessableEntityException(p.getNome() + " já é o responsável pelo bem.");
        }
        novoResponsavel(b, p, hoje());
        ContextoAuditoria.detalhe("Responsável pelo bem " + b.getNumeroTombamento() + ": " + p.getNome());
        return detalhe(b);
    }

    /** Marca o bem como em uso ou ocioso (400 nas outras); só do bem em uso ou ocioso (422). */
    @Transactional
    public PatrimonioDtos.Detalhe mudarSituacao(UUID id, SituacaoBemRequestDto dto) {
        BemPatrimonial b = bemOu404(id);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (!ATIVOS.contains(dto.getSituacao())) {
            throw new ResourceBadRequestException("Aqui o bem fica em uso ou ocioso; a manutenção e a baixa têm o próprio registro.");
        }
        if (!ATIVOS.contains(b.getSituacao())) {
            throw new ResourceUnprocessableEntityException("O bem está " + rotulo(b.getSituacao()) + ".");
        }
        b.setSituacao(dto.getSituacao());
        bemRepository.save(b);
        return detalhe(b);
    }

    /**
     * Liga o bem da classe veículo (422) ao veículo da frota (404), que não pode ser de outro bem (409) nem de outra unidade (422).
     */
    @Transactional
    public PatrimonioDtos.Detalhe vincularVeiculo(UUID id, VeiculoDoBemRequestDto dto) {
        BemPatrimonial b = bemOu404(id);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (b.getClasse() != ClasseBem.VEICULO) {
            throw new ResourceUnprocessableEntityException("Só o bem da classe veículo se liga a um veículo da frota.");
        }
        if (b.getSituacao() == SituacaoBem.BAIXADO) {
            throw new ResourceUnprocessableEntityException("O bem está baixado.");
        }
        Veiculo v = veiculoRepository.findById(dto.getVeiculoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o veículo " + dto.getVeiculoId() + " em nossos registros."));
        Optional<BemPatrimonial> outro = bemRepository.findByVeiculoUuid(v.getUuid());
        if (outro.isPresent() && !outro.get().getUuid().equals(b.getUuid())) {
            throw new ResourceConflictException("O veículo " + v.getPlaca() + " já é o bem " + outro.get().getNumeroTombamento() + ".");
        }
        if (v.getUnidade() != null && !v.getUnidade().getUuid().equals(b.getUnidade().getUuid())) {
            throw new ResourceUnprocessableEntityException("O veículo " + v.getPlaca() + " é de outra unidade.");
        }
        b.setVeiculo(v);
        bemRepository.save(b);
        return detalhe(b);
    }

    // ── Transferência ──────────────────────────────────────────────────────────────────────────────

    /** Envia o bem em uso ou ocioso (422) para outra unidade (400), uma transferência por vez (409). Quem envia gerencia a origem. */
    @Transactional
    public PatrimonioDtos.Detalhe transferir(UUID id, TransferenciaBemRequestDto dto) {
        BemPatrimonial b = bemOu404(id);
        controleDeAcesso.exigir(GERENCIAR, b.getUnidade());
        ContextoAuditoria.registro(b.getUuid());
        if (!ATIVOS.contains(b.getSituacao())) {
            throw new ResourceUnprocessableEntityException("O bem está " + rotulo(b.getSituacao()) + ".");
        }
        UnidadeDeSaude destino = unidadeRepository.findById(dto.getDestinoId()).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma unidade de saúde com o id " + dto.getDestinoId() + " em nossos registros."));
        if (destino.getUuid().equals(b.getUnidade().getUuid())) {
            throw new ResourceBadRequestException("O destino é a própria unidade do bem.");
        }
        if (pendente(b).isPresent()) {
            throw new ResourceConflictException("O bem já tem uma transferência à espera do aceite.");
        }
        TransferenciaBem t = new TransferenciaBem();
        t.setBem(b);
        t.setOrigem(b.getUnidade());
        t.setDestino(destino);
        t.setMotivo(dto.getMotivo().trim());
        t.setEnviadaEm(hoje());
        t.setEnviadaPorCpf(UsuarioAutenticado.cpf());
        t.setSituacao(SituacaoTransferenciaBem.PENDENTE);
        t.setRegistradoEm(Instant.now());
        transferenciaRepository.save(t);
        ContextoAuditoria.detalhe("Bem " + b.getNumeroTombamento() + " enviado para " + destino.getNome());
        return detalhe(b);
    }

    /** O destino aceita (quem gerencia o destino): o bem muda de unidade, de setor (do destino, 422) e de responsável. Só a pendente (422). */
    @Transactional
    public PatrimonioDtos.Detalhe aceitar(UUID transferenciaId, AceiteTransferenciaBemRequestDto dto) {
        TransferenciaBem t = pendenteOu422(transferenciaId);
        controleDeAcesso.exigir(GERENCIAR, t.getDestino());
        BemPatrimonial b = t.getBem();
        ContextoAuditoria.registro(b.getUuid());
        Setor setor = setor(dto.getSetorId(), t.getDestino());
        Profissional p = profissional(dto.getResponsavelMatricula());
        LocalDate hoje = hoje();
        b.setUnidade(t.getDestino());
        b.setSetor(setor);
        if (b.getVeiculo() != null && b.getVeiculo().getUnidade() != null && !b.getVeiculo().getUnidade().getUuid().equals(t.getDestino().getUuid())) {
            b.setVeiculo(null);
        }
        bemRepository.save(b);
        resolver(t, SituacaoTransferenciaBem.ACEITA, null, hoje);
        Optional<ResponsavelBem> atual = responsavelRepository.findAtuais(List.of(b.getUuid())).stream().findFirst();
        if (atual.isEmpty() || !atual.get().getProfissional().getUuid().equals(p.getUuid())) {
            novoResponsavel(b, p, hoje);
        }
        ContextoAuditoria.detalhe("Bem " + b.getNumeroTombamento() + " recebido de " + t.getOrigem().getNome());
        return detalhe(b);
    }

    /** O destino recusa, com o motivo; o bem fica na origem. Só a pendente (422). */
    @Transactional
    public PatrimonioDtos.Detalhe recusar(UUID transferenciaId, MotivoRequestDto dto) {
        TransferenciaBem t = pendenteOu422(transferenciaId);
        controleDeAcesso.exigir(GERENCIAR, t.getDestino());
        ContextoAuditoria.registro(t.getBem().getUuid());
        resolver(t, SituacaoTransferenciaBem.RECUSADA, dto.getMotivo().trim(), hoje());
        return detalhe(t.getBem());
    }

    /** A origem desiste do envio. Só a pendente (422). */
    @Transactional
    public PatrimonioDtos.Detalhe cancelar(UUID transferenciaId) {
        TransferenciaBem t = pendenteOu422(transferenciaId);
        controleDeAcesso.exigir(GERENCIAR, t.getOrigem());
        ContextoAuditoria.registro(t.getBem().getUuid());
        resolver(t, SituacaoTransferenciaBem.CANCELADA, null, hoje());
        return detalhe(t.getBem());
    }

    // ── Auxiliares ─────────────────────────────────────────────────────────────────────────────────

    private void tombar(BemPatrimonial b, LocalDate hoje) {
        String prefixo = hoje.getYear() + "/";
        String maior = bemRepository.findMaiorNumero(prefixo);
        long n = maior == null ? 1 : Long.parseLong(maior.substring(prefixo.length())) + 1;
        b.setNumeroTombamento(prefixo + String.format("%06d", n));
        b.setTombadoEm(hoje);
        b.setSituacao(SituacaoBem.EM_USO);
    }

    private void novoResponsavel(BemPatrimonial b, Profissional p, LocalDate hoje) {
        for (ResponsavelBem r : responsavelRepository.findAtuais(List.of(b.getUuid()))) {
            r.setAte(hoje);
            responsavelRepository.save(r);
        }
        ResponsavelBem r = new ResponsavelBem();
        r.setBem(b);
        r.setProfissional(p);
        r.setDesde(hoje);
        r.setRegistradoEm(Instant.now());
        r.setRegistradoPorCpf(UsuarioAutenticado.cpf());
        responsavelRepository.save(r);
    }

    private void resolver(TransferenciaBem t, SituacaoTransferenciaBem situacao, String motivoRecusa, LocalDate hoje) {
        t.setSituacao(situacao);
        t.setResolvidaEm(hoje);
        t.setResolvidaPorCpf(UsuarioAutenticado.cpf());
        t.setMotivoRecusa(motivoRecusa);
        transferenciaRepository.save(t);
    }

    private static void identificar(BemPatrimonial b, String marca, String modelo, String serie) {
        b.setMarca(limpo(marca));
        b.setModelo(limpo(modelo));
        b.setNumeroSerie(limpo(serie));
    }

    private Setor setor(UUID setorId, UnidadeDeSaude unidade) {
        if (setorId == null) {
            return null;
        }
        Setor s = setorRepository.findById(setorId).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o setor " + setorId + " em nossos registros."));
        if (s.getUnidade() == null || !s.getUnidade().getUuid().equals(unidade.getUuid())) {
            throw new ResourceUnprocessableEntityException("O setor " + s.getNome() + " não é da unidade " + unidade.getNome() + ".");
        }
        return s;
    }

    private Profissional profissional(String matricula) {
        String m = matricula.trim();
        Profissional p = profissionalRepository.findByMatricula(m).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o profissional de matrícula " + m + " em nossos registros."));
        if (!p.isAtivo()) {
            throw new ResourceUnprocessableEntityException("O profissional " + p.getNome() + " está inativo.");
        }
        return p;
    }

    private boolean responsavelAtual(BemPatrimonial b) {
        String cpf = UsuarioAutenticado.cpf();
        return cpf != null && responsavelRepository.findAtuais(List.of(b.getUuid())).stream().anyMatch(r -> cpf.equals(r.getProfissional().getCpf()));
    }

    private Optional<TransferenciaBem> pendente(BemPatrimonial b) {
        return transferenciaRepository.findDosBens(List.of(b.getUuid())).stream().filter(t -> t.getSituacao() == SituacaoTransferenciaBem.PENDENTE).findFirst();
    }

    private TransferenciaBem pendenteOu422(UUID id) {
        TransferenciaBem t = transferenciaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar a transferência " + id + " em nossos registros."));
        if (t.getSituacao() != SituacaoTransferenciaBem.PENDENTE) {
            throw new ResourceUnprocessableEntityException("A transferência já foi " + switch (t.getSituacao()) {
                case ACEITA -> "aceita.";
                case RECUSADA -> "recusada.";
                default -> "cancelada.";
            });
        }
        return t;
    }

    private BemPatrimonial bemOu404(UUID id) {
        return bemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar o bem " + id + " em nossos registros."));
    }

    private PatrimonioDtos.Detalhe detalhe(BemPatrimonial b) {
        BemPatrimonial completo = bemRepository.findComRelacoes(List.of(b.getUuid())).get(0);
        List<PatrimonioDtos.Responsavel> responsaveis = responsavelRepository.findDosBens(List.of(b.getUuid())).stream()
                .map(r -> new PatrimonioDtos.Responsavel(r.getUuid(), r.getProfissional().getMatricula(), r.getProfissional().getNome(), r.getDesde(), r.getAte()))
                .toList();
        List<PatrimonioDtos.Transferencia> transferencias = transferenciaRepository.findDosBens(List.of(b.getUuid())).stream().map(PatrimonioService::dto).toList();
        return new PatrimonioDtos.Detalhe(dtos(List.of(completo)).get(0), responsaveis, transferencias);
    }

    private List<PatrimonioDtos.Bem> dtos(List<BemPatrimonial> bens) {
        if (bens.isEmpty()) {
            return List.of();
        }
        Set<UUID> ids = bens.stream().map(BemPatrimonial::getUuid).collect(Collectors.toSet());
        Map<UUID, ResponsavelBem> atuais = responsavelRepository.findAtuais(ids).stream()
                .collect(Collectors.toMap(r -> r.getBem().getUuid(), Function.identity(), (a, c) -> a));
        Map<UUID, TransferenciaBem> pendentes = transferenciaRepository.findDosBens(ids).stream()
                .filter(t -> t.getSituacao() == SituacaoTransferenciaBem.PENDENTE)
                .collect(Collectors.toMap(t -> t.getBem().getUuid(), Function.identity(), (a, c) -> a));
        return bens.stream().map(b -> {
            ResponsavelBem r = atuais.get(b.getUuid());
            TransferenciaBem t = pendentes.get(b.getUuid());
            return new PatrimonioDtos.Bem(b.getUuid(), b.getNumeroTombamento(), b.getDescricao(), b.getClasse(), b.getMarca(), b.getModelo(),
                    b.getNumeroSerie(), b.getUnidade().getUuid(), b.getUnidade().getNome(),
                    b.getSetor() != null ? b.getSetor().getUuid() : null, b.getSetor() != null ? b.getSetor().getNome() : null,
                    b.getSituacao(), b.getOrigem(), b.getValorAquisicao(), b.getDataAquisicao(), b.getDocumento(),
                    b.getFornecedor() != null ? b.getFornecedor().getRazaoSocial() : null, b.getTombadoEm(),
                    r != null ? r.getProfissional().getMatricula() : null, r != null ? r.getProfissional().getNome() : null, r != null ? r.getDesde() : null,
                    b.getVeiculo() != null ? b.getVeiculo().getUuid() : null, b.getVeiculo() != null ? b.getVeiculo().getPlaca() : null,
                    t != null ? dto(t) : null, b.getObservacao());
        }).toList();
    }

    private static PatrimonioDtos.Transferencia dto(TransferenciaBem t) {
        BemPatrimonial b = t.getBem();
        return new PatrimonioDtos.Transferencia(t.getUuid(), b.getUuid(), b.getNumeroTombamento(), b.getDescricao(), t.getOrigem().getUuid(),
                t.getOrigem().getNome(), t.getDestino().getUuid(), t.getDestino().getNome(), t.getMotivo(), t.getEnviadaEm(), t.getSituacao(),
                t.getResolvidaEm(), t.getMotivoRecusa());
    }

    private static String rotulo(SituacaoBem s) {
        return switch (s) {
            case A_TOMBAR -> "a tombar";
            case EM_USO -> "em uso";
            case OCIOSO -> "ocioso";
            case EM_MANUTENCAO -> "em manutenção";
            case BAIXADO -> "baixado";
        };
    }

    private static boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    private static String limpo(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static LocalDate hoje() {
        return LocalDate.now(ZONA);
    }
}
