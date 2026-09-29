package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.config.UsuarioAutenticado;
import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.AdministracaoMedicamento;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Medicamento;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.AdministracaoMedicamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.RetificacaoAdministracaoMedicamentoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdministracaoMedicamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.MotivoNaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.SituacaoAdministracao;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.AdministracaoMedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.MedicamentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Checagem de enfermagem de medicamento prescrito (ADR-0064). Administrado: baixa o lote da unidade
 * do atendimento pelo livro da Farmácia (ADR-0057). Não administrado: só o registro, com o motivo.
 * Retificação (ADR-0062) estorna a baixa da versão anterior antes de lançar a nova.
 */
@Service
public class AdministracaoMedicamentoService {

    @Autowired
    private AdministracaoMedicamentoRepository administracaoRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private MedicamentoRepository medicamentoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    @Transactional
    public AdministracaoMedicamentoResponseDto registrar(AdministracaoMedicamentoRequestDto dto) {
        Atendimento atendimento = atendimentoRepository.findById(dto.getAtendimentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um atendimento com o id " + dto.getAtendimentoId() + " em nossos registros."));
        Consulta consulta = consultaRepository.findById(dto.getConsultaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + dto.getConsultaId() + " em nossos registros."));
        if (!consulta.getAtendimento().getUuid().equals(atendimento.getUuid())) {
            throw new ResourceBadRequestException("A prescrição precisa ser de uma consulta deste atendimento.");
        }
        if (consultaRepository.existsByRetificacaoDe_Uuid(consulta.getUuid())) {
            throw new ResourceUnprocessableEntityException(
                    "Esta consulta foi retificada: registre a administração a partir da versão vigente da prescrição.");
        }
        Map<UUID, Lote> lotes = travarLotes(dto.getLoteId());
        AdministracaoMedicamento administracao = montar(dto, atendimento, consulta, lotes);
        administracao = administracaoRepository.save(administracao);
        baixar(administracao);
        return AdministracaoMedicamentoResponseDto.fromAdministracao(administracao, null);
    }

    @Transactional
    public AdministracaoMedicamentoResponseDto retificar(UUID uuid, RetificacaoAdministracaoMedicamentoRequestDto dto) {
        AdministracaoMedicamento original = administracaoRepository.findByIdParaRetificar(uuid)
                .orElseThrow(() -> naoEncontrada(uuid));
        UUID vigente = sucessores().get(uuid);
        if (vigente != null) {
            throw new ResourceUnprocessableEntityException("Esta administração já foi retificada pela versão " + vigente
                    + ": retifique a versão vigente.");
        }
        if (!original.getAtendimento().getUuid().equals(dto.getAtendimentoId())
                || !original.getConsulta().getUuid().equals(dto.getConsultaId())) {
            throw new ResourceBadRequestException(
                    "A retificação precisa manter o mesmo atendimento e a mesma prescrição do registro original.");
        }

        UUID loteAnterior = original.getSituacao() == SituacaoAdministracao.ADMINISTRADO ? original.getLote().getUuid() : null;
        Map<UUID, Lote> lotes = travarLotes(loteAnterior, dto.getLoteId());

        AdministracaoMedicamento nova = montar(dto, original.getAtendimento(), original.getConsulta(), lotes);
        nova.setRetificacaoDe(original);
        nova.setMotivoRetificacao(dto.getMotivoRetificacao().trim());
        nova = administracaoRepository.save(nova);

        // A baixa anterior volta ao lote antes da nova — o livro mostra as duas operações.
        if (loteAnterior != null) {
            movimentacaoFarmaciaService.lancarAdministracao(lotes.get(loteAnterior), TipoMovimentacaoFarmacia.ADMINISTRACAO_ESTORNO,
                    original.getQuantidade(), nova.getProfissional(),
                    "Administração retificada: " + nova.getMotivoRetificacao(), nova);
        }
        baixar(nova);
        return AdministracaoMedicamentoResponseDto.fromAdministracao(nova, null);
    }

    /** Da mais recente para a mais antiga. */
    @Transactional(readOnly = true)
    public List<AdministracaoMedicamentoResponseDto> listar() {
        Map<UUID, UUID> sucessores = sucessores();
        return administracaoRepository.findAll().stream()
                .sorted((a, b) -> b.getDataHora().compareTo(a.getDataHora()))
                .map(a -> AdministracaoMedicamentoResponseDto.fromAdministracao(a, sucessores.get(a.getUuid())))
                .collect(Collectors.toList());
    }

    /** Responde 404 quando o atendimento não tem administrações (convenção das listagens por entidade relacionada). */
    @Transactional(readOnly = true)
    public List<AdministracaoMedicamentoResponseDto> listarPorAtendimento(UUID atendimentoId) {
        Map<UUID, UUID> sucessores = sucessores();
        List<AdministracaoMedicamentoResponseDto> lista = administracaoRepository.findByAtendimentoUuid(atendimentoId).stream()
                .sorted((a, b) -> a.getDataHora().compareTo(b.getDataHora()))
                .map(a -> AdministracaoMedicamentoResponseDto.fromAdministracao(a, sucessores.get(a.getUuid())))
                .collect(Collectors.toList());
        if (lista.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Não foi possível encontrar administrações para o atendimento de id " + atendimentoId + " em nossos registros.");
        }
        return lista;
    }

    @Transactional(readOnly = true)
    public AdministracaoMedicamentoResponseDto buscarPorId(UUID uuid) {
        AdministracaoMedicamento a = administracaoRepository.findById(uuid).orElseThrow(() -> naoEncontrada(uuid));
        return AdministracaoMedicamentoResponseDto.fromAdministracao(a, sucessores().get(uuid));
    }

    /**
     * Valida a checagem e monta a entidade. Administrado: lote do mesmo medicamento, da unidade do
     * atendimento e dentro da validade, com dose, via e quantidade. Não administrado: motivo (OUTRO
     * exige observação) e nada de estoque.
     */
    private AdministracaoMedicamento montar(AdministracaoMedicamentoRequestDto dto, Atendimento atendimento, Consulta consulta,
                                            Map<UUID, Lote> lotes) {
        Medicamento medicamento = medicamentoRepository.findById(dto.getMedicamentoId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um medicamento com o id " + dto.getMedicamentoId() + " em nossos registros."));
        Profissional profissional = profissionalRepository.findByMatricula(dto.getProfissionalMatricula())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + dto.getProfissionalMatricula()
                                + " em nossos registros."));
        String observacao = textoOuNulo(dto.getObservacao());

        AdministracaoMedicamento a = new AdministracaoMedicamento();
        a.setAtendimento(atendimento);
        a.setConsulta(consulta);
        a.setMedicamento(medicamento);
        a.setSituacao(dto.getSituacao());
        a.setObservacao(observacao);
        a.setDataHora(dto.getDataHora());
        a.setProfissional(profissional);
        a.setRegistradoEm(Instant.now());
        a.setRegistradoPorCpf(UsuarioAutenticado.cpf());

        if (dto.getSituacao() == SituacaoAdministracao.ADMINISTRADO) {
            String dose = textoOuNulo(dto.getDose());
            if (dto.getLoteId() == null || dose == null || dto.getVia() == null || dto.getQuantidade() == null) {
                throw new ResourceBadRequestException("Medicamento administrado exige loteId, dose, via e quantidade.");
            }
            Lote lote = lotes.get(dto.getLoteId());
            if (!lote.getMedicamento().getUuid().equals(medicamento.getUuid())) {
                throw new ResourceBadRequestException("O lote informado não é do medicamento administrado.");
            }
            if (!lote.getUnidade().getUuid().equals(atendimento.getUnidade().getUuid())) {
                throw new ResourceUnprocessableEntityException(
                        "O lote " + lote.getNumeroLote() + " é de outra unidade: use o estoque da unidade do atendimento.");
            }
            if (lote.getValidade().isBefore(dto.getDataHora().toLocalDate())) {
                throw new ResourceUnprocessableEntityException("O lote " + lote.getNumeroLote() + " venceu em "
                        + lote.getValidade() + ": medicamento vencido não é administrado.");
            }
            a.setLote(lote);
            a.setDose(dose);
            a.setVia(dto.getVia());
            a.setQuantidade(dto.getQuantidade());
        } else {
            if (dto.getMotivoNaoAdministracao() == null) {
                throw new ResourceBadRequestException("Medicamento não administrado exige motivoNaoAdministracao.");
            }
            if (dto.getMotivoNaoAdministracao() == MotivoNaoAdministracao.OUTRO && observacao == null) {
                throw new ResourceBadRequestException("Motivo OUTRO exige observação.");
            }
            a.setMotivoNaoAdministracao(dto.getMotivoNaoAdministracao());
        }
        return a;
    }

    /** Administrado → saída do lote no livro; saldo insuficiente responde 422 e desfaz tudo. */
    private void baixar(AdministracaoMedicamento a) {
        if (a.getSituacao() != SituacaoAdministracao.ADMINISTRADO) {
            return;
        }
        movimentacaoFarmaciaService.lancarAdministracao(a.getLote(), TipoMovimentacaoFarmacia.ADMINISTRACAO, -a.getQuantidade(),
                a.getProfissional(), "Administrado a " + a.getAtendimento().getPaciente().getNome() + " · " + a.getDose()
                        + " · " + a.getVia(), a);
    }

    /** Trava os lotes envolvidos sempre em ordem crescente de id, como as demais movimentações (ADR-0059). */
    private Map<UUID, Lote> travarLotes(UUID... ids) {
        Map<UUID, Lote> lotes = new HashMap<>();
        Stream.of(ids).filter(Objects::nonNull).distinct().sorted().forEach(id -> lotes.put(id,
                loteRepository.findByIdParaMovimentar(id).orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + id + " em nossos registros."))));
        return lotes;
    }

    private Map<UUID, UUID> sucessores() {
        return administracaoRepository.findParesDeRetificacao().stream()
                .collect(Collectors.toMap(par -> (UUID) par[0], par -> (UUID) par[1]));
    }

    private ResourceNotFoundException naoEncontrada(UUID uuid) {
        return new ResourceNotFoundException(
                "Não foi possível encontrar uma administração de medicamento com o id " + uuid + " em nossos registros.");
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
