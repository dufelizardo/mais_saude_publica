package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Consulta;
import com.edufelizardo.maissaudepublica.models.Dispensacao;
import com.edufelizardo.maissaudepublica.models.Lote;
import com.edufelizardo.maissaudepublica.models.Paciente;
import com.edufelizardo.maissaudepublica.models.Profissional;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.DispensacaoRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.DispensacaoResponseDto;
import com.edufelizardo.maissaudepublica.models.enuns.TipoMovimentacaoFarmacia;
import com.edufelizardo.maissaudepublica.repositories.ConsultaRepository;
import com.edufelizardo.maissaudepublica.repositories.DispensacaoRepository;
import com.edufelizardo.maissaudepublica.repositories.LoteRepository;
import com.edufelizardo.maissaudepublica.repositories.PacienteRepository;
import com.edufelizardo.maissaudepublica.repositories.ProfissionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CRUD (só criação e leitura, ver ADR-0051) da Dispensação (Farmácia — MAPA-DE-DOMINIOS.md #9).
 * Criar uma Dispensação lança uma saída no livro de movimentação ({@link MovimentacaoFarmaciaService},
 * ADR-0057), com o lote travado, validando estoque suficiente (422, não 400 — é uma regra de negócio,
 * não um erro de payload).
 */
@Service
public class DispensacaoService {

    @Autowired
    private DispensacaoRepository dispensacaoRepository;

    @Autowired
    private ControleDeAcesso controleDeAcesso;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimentacaoFarmaciaService movimentacaoFarmaciaService;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ProfissionalRepository profissionalRepository;

    @Autowired
    private ConsultaRepository consultaRepository;

    @Transactional
    public DispensacaoResponseDto criar(DispensacaoRequestDto dto) {
        Lote lote = buscarLotePorId(dto.getLoteId());
        controleDeAcesso.exigir("FARMACIA.DISPENSAR", lote.getUnidade());
        Paciente paciente = buscarPacientePorId(dto.getPacienteId());
        Profissional profissional = buscarProfissionalPorMatricula(dto.getProfissionalMatricula());
        Consulta consulta = buscarConsultaSeInformada(dto.getConsultaId());

        Dispensacao dispensacao = new Dispensacao(lote, paciente, profissional, consulta, dto.getQuantidade(),
                dto.getDataHora());
        dispensacao = dispensacaoRepository.save(dispensacao);
        // Estoque insuficiente lança 422 aqui e desfaz a transação inteira, dispensação incluída.
        movimentacaoFarmaciaService.lancar(lote, TipoMovimentacaoFarmacia.DISPENSACAO, -dto.getQuantidade(),
                profissional, null, null, dispensacao);
        return DispensacaoResponseDto.fromDispensacao(dispensacao);
    }

    public List<DispensacaoResponseDto> listar() {
        return controleDeAcesso.filtrar(dispensacaoRepository.findAll().stream(), d -> Stream.of(d.getLote().getUnidade()),
                        "FARMACIA.CONSULTAR", "FARMACIA.DISPENSAR")
                .map(DispensacaoResponseDto::fromDispensacao)
                .collect(Collectors.toList());
    }

    public DispensacaoResponseDto buscarPorId(UUID uuid) {
        Dispensacao dispensacao = buscarEntidadePorId(uuid);
        controleDeAcesso.exigirVisivel(dispensacao.getLote().getUnidade(), "FARMACIA.CONSULTAR", "FARMACIA.DISPENSAR");
        return DispensacaoResponseDto.fromDispensacao(dispensacao);
    }

    private Dispensacao buscarEntidadePorId(UUID uuid) {
        return dispensacaoRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma dispensação com o id " + uuid + " em nossos registros."));
    }

    private Lote buscarLotePorId(UUID uuid) {
        return loteRepository.findByIdParaMovimentar(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um lote com o id " + uuid + " em nossos registros."));
    }

    private Paciente buscarPacientePorId(UUID uuid) {
        return pacienteRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um paciente com o id " + uuid + " em nossos registros."));
    }

    private Profissional buscarProfissionalPorMatricula(String matricula) {
        return profissionalRepository.findByMatricula(matricula)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar um profissional com a matrícula " + matricula + " em nossos registros."));
    }

    private Consulta buscarConsultaSeInformada(UUID consultaId) {
        if (consultaId == null) {
            return null;
        }
        return consultaRepository.findById(consultaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma consulta com o id " + consultaId + " em nossos registros."));
    }
}
