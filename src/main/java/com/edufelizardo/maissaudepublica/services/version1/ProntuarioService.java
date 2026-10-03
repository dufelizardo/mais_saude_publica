package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.repositories.AdministracaoMedicamentoRepository;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AdministracaoMedicamentoResponseDto;
import com.edufelizardo.maissaudepublica.models.AdministracaoMedicamento;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import com.edufelizardo.maissaudepublica.models.Procedimento;
import com.edufelizardo.maissaudepublica.models.EvolucaoEnfermagem;
import com.edufelizardo.maissaudepublica.models.Triagem;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Atendimento;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AtendimentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ConsultaResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProcedimentoResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioAtendimentoDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.EvolucaoEnfermagemResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioConsultaDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ProntuarioResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.AcessoProntuarioDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.TriagemResponseDto;
import com.edufelizardo.maissaudepublica.repositories.AtendimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.EvolucaoEnfermagemRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProcedimentoRepository;
import com.edufelizardo.maissaudepublica.repositories.TriagemRepository;
import com.edufelizardo.maissaudepublica.repositories.ItemPedidoExameRepository;
import com.edufelizardo.maissaudepublica.repositories.PedidoExameRepository;
import com.edufelizardo.maissaudepublica.models.PedidoExame;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameProntuarioDto;
import java.util.Comparator;
import com.edufelizardo.maissaudepublica.repositories.InternacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.EventoLeitoRepository;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.InternacaoResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Monta o Prontuário de um Paciente sob demanda, agregando Atendimento/Consulta/Procedimento (ver
 * ADR-0039 decisão 6, ADR-0045) — leitura pura, nenhum dado é persistido aqui.
 */
@Service
public class ProntuarioService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private AtendimentoRepository atendimentoRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Autowired
    private ProcedimentoRepository procedimentoRepository;

    @Autowired
    private TriagemRepository triagemRepository;

    @Autowired
    private EvolucaoEnfermagemRepository evolucaoEnfermagemRepository;

    @Autowired
    private AdministracaoMedicamentoRepository administracaoMedicamentoRepository;

    @Autowired
    private VinculoAssistencialService vinculoAssistencialService;

    @Autowired
    private PedidoExameRepository pedidoExameRepository;

    @Autowired
    private ItemPedidoExameRepository itemPedidoExameRepository;

    @Autowired
    private InternacaoRepository internacaoRepository;

    @Autowired
    private EventoLeitoRepository eventoLeitoRepository;

    public ProntuarioResponseDto buscarPorPacienteId(UUID pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + pacienteId + " em nossos registros."));
        // Histórico da rede inteira, mas só com vínculo assistencial (ADR-0076).
        AcessoProntuarioDto acesso = vinculoAssistencialService.exigirVinculo(pacienteId);

        List<ProntuarioAtendimentoDto> atendimentos = atendimentoRepository.findByPacienteUuid(pacienteId)
                .stream()
                .map(this::montarAtendimento)
                .collect(Collectors.toList());

        ProntuarioResponseDto response = new ProntuarioResponseDto();
        response.setPacienteUuid(paciente.getUuid());
        response.setPacienteNome(paciente.getNome());
        response.setAtendimentos(atendimentos);
        response.setAcesso(acesso);
        response.setExames(exames(pacienteId));
        response.setInternacoes(internacaoRepository.findByPaciente_UuidOrderByAdmitidaEmDesc(pacienteId).stream()
                .map(i -> InternacaoResponseDto.fromInternacao(i, eventoLeitoRepository.findByInternacao_UuidOrderByOcorridoEmAsc(i.getUuid()), true))
                .collect(Collectors.toList()));
        return response;
    }

    /** Os exames laboratoriais do paciente, pedido mais recente primeiro (ADR-0095). */
    private List<ExameProntuarioDto> exames(UUID pacienteId) {
        List<PedidoExame> pedidos = pedidoExameRepository.findByPaciente_UuidOrderBySolicitadoEmDesc(pacienteId);
        if (pedidos.isEmpty()) {
            return List.of();
        }
        return itemPedidoExameRepository.findByPedido_UuidIn(pedidos.stream().map(PedidoExame::getUuid).toList()).stream()
                .sorted(Comparator.comparing((com.edufelizardo.maissaudepublica.models.ItemPedidoExame i) -> i.getPedido().getSolicitadoEm())
                        .reversed().thenComparing(i -> i.getExame().getNome()))
                .map(ExameProntuarioDto::fromItem)
                .collect(Collectors.toList());
    }

    /**
     * Todas as versões de cada registro clínico vêm no prontuário (ADR-0062): as retificadas marcadas com
     * {@code retificado} e o id da versão que as corrige. Como a retificação mantém o atendimento, basta
     * olhar os registros do próprio atendimento para saber quem corrige quem.
     */
    private ProntuarioAtendimentoDto montarAtendimento(Atendimento atendimento) {
        AtendimentoResponseDto atendimentoDto = AtendimentoResponseDto.fromAtendimento(atendimento);

        List<Triagem> triagensDoAtendimento = triagemRepository.findByAtendimentoUuid(atendimento.getUuid());
        Map<UUID, UUID> sucessorTriagem = sucessores(triagensDoAtendimento, Triagem::getUuid, Triagem::getRetificacaoDe);
        List<TriagemResponseDto> triagens = triagensDoAtendimento.stream()
                .map(t -> TriagemResponseDto.fromTriagem(t, sucessorTriagem.get(t.getUuid())))
                .collect(Collectors.toList());

        List<EvolucaoEnfermagem> evolucoesDoAtendimento = evolucaoEnfermagemRepository.findByAtendimentoUuid(atendimento.getUuid());
        Map<UUID, UUID> sucessorEvolucao = sucessores(evolucoesDoAtendimento, EvolucaoEnfermagem::getUuid,
                EvolucaoEnfermagem::getRetificacaoDe);
        List<EvolucaoEnfermagemResponseDto> evolucoes = evolucoesDoAtendimento.stream()
                .map(e -> EvolucaoEnfermagemResponseDto.fromEvolucaoEnfermagem(e, sucessorEvolucao.get(e.getUuid())))
                .collect(Collectors.toList());

        List<Consulta> consultasDoAtendimento = consultaRepository.findByAtendimentoUuid(atendimento.getUuid());
        Map<UUID, UUID> sucessorConsulta = sucessores(consultasDoAtendimento, Consulta::getUuid, Consulta::getRetificacaoDe);
        // Procedimentos de uma consulta retificada aparecem na versão vigente dela.
        Map<UUID, List<Procedimento>> procedimentosPorConsultaVigente = new HashMap<>();
        for (Consulta consulta : consultasDoAtendimento) {
            UUID vigente = vigente(consulta.getUuid(), sucessorConsulta);
            procedimentosPorConsultaVigente.computeIfAbsent(vigente, k -> new ArrayList<>())
                    .addAll(procedimentoRepository.findByConsultaUuid(consulta.getUuid()));
        }
        List<ProntuarioConsultaDto> consultas = consultasDoAtendimento.stream()
                .map(c -> montarConsulta(c, sucessorConsulta.get(c.getUuid()),
                        procedimentosPorConsultaVigente.getOrDefault(c.getUuid(), List.of())))
                .collect(Collectors.toList());

        List<AdministracaoMedicamento> administracoesDoAtendimento =
                administracaoMedicamentoRepository.findByAtendimentoUuid(atendimento.getUuid());
        Map<UUID, UUID> sucessorAdministracao = sucessores(administracoesDoAtendimento, AdministracaoMedicamento::getUuid,
                AdministracaoMedicamento::getRetificacaoDe);
        List<AdministracaoMedicamentoResponseDto> administracoes = administracoesDoAtendimento.stream()
                .map(a -> AdministracaoMedicamentoResponseDto.fromAdministracao(a, sucessorAdministracao.get(a.getUuid())))
                .collect(Collectors.toList());

        ProntuarioAtendimentoDto dto = new ProntuarioAtendimentoDto();
        dto.setAtendimento(atendimentoDto);
        dto.setAdministracoes(administracoes);
        dto.setTriagens(triagens);
        dto.setEvolucoes(evolucoes);
        dto.setConsultas(consultas);
        return dto;
    }

    private ProntuarioConsultaDto montarConsulta(Consulta consulta, UUID sucessor, List<Procedimento> procedimentosDaConsulta) {
        Map<UUID, UUID> sucessorProcedimento = sucessores(procedimentosDaConsulta, Procedimento::getUuid,
                Procedimento::getRetificacaoDe);
        List<ProcedimentoResponseDto> procedimentos = procedimentosDaConsulta.stream()
                .map(p -> ProcedimentoResponseDto.fromProcedimento(p, sucessorProcedimento.get(p.getUuid())))
                .collect(Collectors.toList());

        ProntuarioConsultaDto dto = new ProntuarioConsultaDto();
        dto.setConsulta(ConsultaResponseDto.fromConsulta(consulta, sucessor));
        dto.setProcedimentos(procedimentos);
        return dto;
    }

    /** registro corrigido → versão que o corrige, entre os registros informados. */
    private static <T> Map<UUID, UUID> sucessores(List<T> registros, Function<T, UUID> id, Function<T, T> retificacaoDe) {
        Map<UUID, UUID> mapa = new HashMap<>();
        for (T registro : registros) {
            T anterior = retificacaoDe.apply(registro);
            if (anterior != null) {
                mapa.put(id.apply(anterior), id.apply(registro));
            }
        }
        return mapa;
    }

    private static UUID vigente(UUID id, Map<UUID, UUID> sucessores) {
        UUID atual = id;
        while (sucessores.containsKey(atual)) {
            atual = sucessores.get(atual);
        }
        return atual;
    }
}
