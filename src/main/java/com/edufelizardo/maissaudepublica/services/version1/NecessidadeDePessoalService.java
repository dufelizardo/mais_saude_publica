package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.models.Cargo;
import com.edufelizardo.maissaudepublica.models.NecessidadeDePessoal;
import com.edufelizardo.maissaudepublica.models.Setor;
import com.edufelizardo.maissaudepublica.models.UnidadeDeSaude;
import com.edufelizardo.maissaudepublica.models.Vaga;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.NecessidadeDePessoalRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.NecessidadeDePessoalResponseDto;
import com.edufelizardo.maissaudepublica.repositories.NecessidadeDePessoalRepository;
import com.edufelizardo.maissaudepublica.repositories.UnidadeDeSaudeRepository;
import com.edufelizardo.maissaudepublica.repositories.VagaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NecessidadeDePessoalService {

    @Autowired
    private NecessidadeDePessoalRepository necessidadeDePessoalRepository;

    @Autowired
    private UnidadeDeSaudeRepository unidadeDeSaudeRepository;

    @Autowired
    private SetorService setorService;

    @Autowired
    private CargoService cargoService;

    @Autowired
    private VagaRepository vagaRepository;

    public NecessidadeDePessoalResponseDto criar(NecessidadeDePessoalRequestDto dto) {
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Setor setor = buscarSetorSeInformado(dto.getSetorId());
        Cargo cargo = cargoService.buscarEntidadePorId(dto.getCargoId());

        NecessidadeDePessoal necessidade = new NecessidadeDePessoal(unidade, setor, cargo, dto.getQuantidade(),
                dto.getJornadaSemanalHoras(), dto.getCompetenciasNecessarias(), dto.getJustificativa(), LocalDate.now());
        necessidade = necessidadeDePessoalRepository.save(necessidade);
        return NecessidadeDePessoalResponseDto.fromNecessidadeDePessoal(necessidade);
    }

    public NecessidadeDePessoalResponseDto atualizar(UUID uuid, NecessidadeDePessoalRequestDto dto) {
        NecessidadeDePessoal necessidade = buscarEntidadePorId(uuid);
        UnidadeDeSaude unidade = buscarUnidadePorId(dto.getUnidadeId());
        Setor setor = buscarSetorSeInformado(dto.getSetorId());
        Cargo cargo = cargoService.buscarEntidadePorId(dto.getCargoId());

        necessidade.setUnidade(unidade);
        necessidade.setSetor(setor);
        necessidade.setCargo(cargo);
        necessidade.setQuantidade(dto.getQuantidade());
        necessidade.setJornadaSemanalHoras(dto.getJornadaSemanalHoras());
        necessidade.setCompetenciasNecessarias(dto.getCompetenciasNecessarias());
        necessidade.setJustificativa(dto.getJustificativa());
        necessidade = necessidadeDePessoalRepository.save(necessidade);
        return NecessidadeDePessoalResponseDto.fromNecessidadeDePessoal(necessidade);
    }

    /**
     * Vincula a {@link Vaga} que o RH abriu a partir desta necessidade -- link informativo, nunca um
     * gatilho automático (ver ADR-0036). Recusa vincular uma necessidade que já tem vaga associada
     * (409), mesmo espírito do "encerrar" de AdesaoBeneficio/ResponsabilidadeAdministrativa: uma ação
     * que só acontece uma vez.
     */
    public NecessidadeDePessoalResponseDto vincularVaga(UUID uuid, UUID vagaId) {
        NecessidadeDePessoal necessidade = buscarEntidadePorId(uuid);

        if (necessidade.getVagaAssociada() != null) {
            throw new ResourceConflictException(
                    "A necessidade de pessoal de id " + uuid + " já está vinculada à vaga " + necessidade.getVagaAssociada().getUuid() + ".");
        }

        Vaga vaga = vagaRepository.findById(vagaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma vaga com o id " + vagaId + " em nossos registros."));

        necessidade.setVagaAssociada(vaga);
        necessidade = necessidadeDePessoalRepository.save(necessidade);
        return NecessidadeDePessoalResponseDto.fromNecessidadeDePessoal(necessidade);
    }

    public List<NecessidadeDePessoalResponseDto> listar() {
        return necessidadeDePessoalRepository.findAll()
                .stream()
                .map(NecessidadeDePessoalResponseDto::fromNecessidadeDePessoal)
                .collect(Collectors.toList());
    }

    public NecessidadeDePessoalResponseDto buscarPorId(UUID uuid) {
        return NecessidadeDePessoalResponseDto.fromNecessidadeDePessoal(buscarEntidadePorId(uuid));
    }

    NecessidadeDePessoal buscarEntidadePorId(UUID uuid) {
        return necessidadeDePessoalRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma necessidade de pessoal com o id " + uuid + " em nossos registros."));
    }

    private UnidadeDeSaude buscarUnidadePorId(UUID uuid) {
        return unidadeDeSaudeRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Não foi possível encontrar uma unidade de saúde com o id " + uuid + " em nossos registros."));
    }

    private Setor buscarSetorSeInformado(UUID setorId) {
        if (setorId == null) {
            return null;
        }
        return setorService.buscarEntidadePorId(setorId);
    }
}
