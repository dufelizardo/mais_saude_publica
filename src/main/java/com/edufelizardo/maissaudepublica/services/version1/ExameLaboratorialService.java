package com.edufelizardo.maissaudepublica.services.version1;

import com.edufelizardo.maissaudepublica.exceptions.ResourceBadRequestException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceConflictException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceNotFoundException;
import com.edufelizardo.maissaudepublica.exceptions.ResourceUnprocessableEntityException;
import com.edufelizardo.maissaudepublica.models.ExameLaboratorial;
import com.edufelizardo.maissaudepublica.models.FaixaReferenciaExame;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.ExameLaboratorialRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.request.FaixaReferenciaExameRequestDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.ExameLaboratorialResponseDto;
import com.edufelizardo.maissaudepublica.models.dtos.version1.response.FaixaReferenciaExameDto;
import com.edufelizardo.maissaudepublica.models.enuns.Sexo;
import com.edufelizardo.maissaudepublica.models.enuns.CategoriaExame;
import com.edufelizardo.maissaudepublica.models.enuns.TipoResultadoExame;
import com.edufelizardo.maissaudepublica.repositories.ExameLaboratorialRepository;
import com.edufelizardo.maissaudepublica.repositories.FaixaReferenciaExameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Catálogo de exames do laboratório assistencial (ADR-0093), com as faixas de referência por sexo e idade e os valores
 * críticos (ADR-0180).
 */
@Service
public class ExameLaboratorialService {

    /** Rótulo da faixa geral no resultado, quando nenhuma faixa por sexo e idade se aplica. */
    public static final String FAIXA_GERAL = "Faixa geral";

    @Autowired
    private ExameLaboratorialRepository repository;

    @Autowired
    private FaixaReferenciaExameRepository faixaRepository;

    @Autowired
    private com.edufelizardo.maissaudepublica.repositories.InsumoExameRepository insumoRepository;

    @Autowired
    private com.edufelizardo.maissaudepublica.repositories.MaterialEstoqueRepository materialEstoqueRepository;

    @Transactional
    public ExameLaboratorialResponseDto criar(ExameLaboratorialRequestDto dto) {
        ExameLaboratorial exame = new ExameLaboratorial();
        aplicar(exame, dto, null);
        exame.setAtivo(dto.getAtivo() == null || dto.getAtivo());
        return resposta(repository.save(exame));
    }

    @Transactional
    public ExameLaboratorialResponseDto atualizar(UUID uuid, ExameLaboratorialRequestDto dto) {
        ExameLaboratorial exame = buscar(uuid);
        aplicar(exame, dto, uuid);
        if (dto.getAtivo() != null) {
            exame.setAtivo(dto.getAtivo());
        }
        if (exame.getTipoResultado() != TipoResultadoExame.NUMERICO && !faixaRepository.findByExame_Uuid(uuid).isEmpty()) {
            throw new ResourceUnprocessableEntityException("O exame tem faixas de referência por sexo e idade: remova-as antes de "
                    + "mudar o resultado para texto.");
        }
        return resposta(repository.save(exame));
    }

    @Transactional(readOnly = true)
    public List<ExameLaboratorialResponseDto> listar(boolean apenasAtivos) {
        List<ExameLaboratorial> exames = repository.findAllByOrderByNomeAsc().stream().filter(e -> !apenasAtivos || e.isAtivo()).toList();
        Map<UUID, List<FaixaReferenciaExame>> faixas = exames.isEmpty() ? Map.of()
                : faixaRepository.findByExame_UuidIn(exames.stream().map(ExameLaboratorial::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(f -> f.getExame().getUuid()));
        Map<UUID, List<com.edufelizardo.maissaudepublica.models.InsumoExame>> insumos = exames.isEmpty() ? Map.of()
                : insumoRepository.findByExame_UuidIn(exames.stream().map(ExameLaboratorial::getUuid).toList()).stream()
                .collect(Collectors.groupingBy(i -> i.getExame().getUuid()));
        return exames.stream().map(e -> {
            ExameLaboratorialResponseDto d = ExameLaboratorialResponseDto.fromExame(e, faixas.getOrDefault(e.getUuid(), List.of()));
            d.setInsumos(insumos.getOrDefault(e.getUuid(), List.of()).stream()
                    .map(com.edufelizardo.maissaudepublica.models.dtos.version1.response.InsumoExameDto::fromInsumo).toList());
            return d;
        }).toList();
    }

    @Transactional(readOnly = true)
    public ExameLaboratorialResponseDto buscarPorId(UUID uuid) {
        return resposta(buscar(uuid));
    }

    ExameLaboratorial buscar(UUID uuid) {
        return repository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar um exame com o id " + uuid + " em nossos registros."));
    }

    // ── Faixas por sexo e idade (ADR-0180) ─────────────────────────────────────────────────────────

    @Transactional
    public ExameLaboratorialResponseDto adicionarFaixa(UUID exameId, FaixaReferenciaExameRequestDto dto) {
        ExameLaboratorial exame = buscar(exameId);
        if (exame.getTipoResultado() != TipoResultadoExame.NUMERICO) {
            throw new ResourceUnprocessableEntityException("Faixa por sexo e idade só vale para exame de resultado numérico.");
        }
        FaixaReferenciaExame faixa = new FaixaReferenciaExame();
        faixa.setExame(exame);
        aplicarFaixa(faixa, dto);
        faixaRepository.save(faixa);
        return resposta(exame);
    }

    @Transactional
    public ExameLaboratorialResponseDto atualizarFaixa(UUID faixaId, FaixaReferenciaExameRequestDto dto) {
        FaixaReferenciaExame faixa = buscarFaixa(faixaId);
        aplicarFaixa(faixa, dto);
        faixaRepository.save(faixa);
        return resposta(faixa.getExame());
    }

    /** Remove a faixa; os resultados já dados guardam a cópia da faixa usada. */
    @Transactional
    public ExameLaboratorialResponseDto removerFaixa(UUID faixaId) {
        FaixaReferenciaExame faixa = buscarFaixa(faixaId);
        ExameLaboratorial exame = faixa.getExame();
        faixaRepository.delete(faixa);
        faixaRepository.flush();
        return resposta(exame);
    }

    /**
     * A faixa que vale para o paciente na data da coleta: a do sexo dele que cubra a idade; senão, a sem sexo que cubra a
     * idade. Sexo ignorado: só as sem sexo. Sem nascimento: nenhuma. Vazio: vale a faixa geral do catálogo.
     */
    @Transactional(readOnly = true)
    public Optional<FaixaReferenciaExame> faixaPara(ExameLaboratorial exame, Sexo sexo, LocalDate nascimento, LocalDate data) {
        if (nascimento == null || data.isBefore(nascimento)) {
            return Optional.empty();
        }
        List<FaixaReferenciaExame> cobrem = faixaRepository.findByExame_Uuid(exame.getUuid()).stream()
                .filter(f -> f.cobre(nascimento, data)).toList();
        Optional<FaixaReferenciaExame> doSexo = sexo == Sexo.MASCULINO || sexo == Sexo.FEMININO
                ? cobrem.stream().filter(f -> f.getSexo() == sexo).findFirst() : Optional.empty();
        return doSexo.or(() -> cobrem.stream().filter(f -> f.getSexo() == null).findFirst());
    }

    private void aplicarFaixa(FaixaReferenciaExame faixa, FaixaReferenciaExameRequestDto dto) {
        if (dto.getSexo() == Sexo.IGNORADO) {
            throw new ResourceBadRequestException("A faixa é de um sexo (MASCULINO ou FEMININO) ou dos dois (sem sexo); IGNORADO não vale.");
        }
        if ((dto.getIdadeMaxima() == null) != (dto.getUnidadeIdadeMaxima() == null)) {
            throw new ResourceBadRequestException("Informe a idade máxima com a sua unidade, ou nenhuma das duas para faixa sem limite.");
        }
        faixa.setSexo(dto.getSexo());
        faixa.setIdadeMinima(dto.getIdadeMinima());
        faixa.setUnidadeIdadeMinima(dto.getUnidadeIdadeMinima());
        faixa.setIdadeMaxima(dto.getIdadeMaxima());
        faixa.setUnidadeIdadeMaxima(dto.getUnidadeIdadeMaxima());
        if (faixa.fimAproximado() <= faixa.inicioAproximado()) {
            throw new ResourceBadRequestException("A idade máxima precisa ser maior que a mínima.");
        }
        if (dto.getReferenciaMinima() == null && dto.getReferenciaMaxima() == null) {
            throw new ResourceBadRequestException("Informe ao menos um limite de referência (mínimo ou máximo).");
        }
        validarLimites(dto.getReferenciaMinima(), dto.getReferenciaMaxima(), dto.getCriticoMinimo(), dto.getCriticoMaximo());
        faixa.setReferenciaMinima(dto.getReferenciaMinima());
        faixa.setReferenciaMaxima(dto.getReferenciaMaxima());
        faixa.setCriticoMinimo(dto.getCriticoMinimo());
        faixa.setCriticoMaximo(dto.getCriticoMaximo());
        faixa.setDescricao(textoOuNulo(dto.getDescricao()));
        faixaRepository.findByExame_Uuid(faixa.getExame().getUuid()).stream()
                .filter(o -> !o.getUuid().equals(faixa.getUuid()) && o.getSexo() == faixa.getSexo() && o.sobrepoe(faixa))
                .findFirst().ifPresent(o -> {
                    throw new ResourceConflictException("A faixa sobrepõe as idades de outra do mesmo sexo: "
                            + FaixaReferenciaExameDto.rotulo(o) + ".");
                });
    }

    /** Mínima até máxima; crítico mínimo abaixo da mínima e crítico máximo acima da máxima. */
    private static void validarLimites(BigDecimal minima, BigDecimal maxima, BigDecimal criticoMinimo, BigDecimal criticoMaximo) {
        if (minima != null && maxima != null && minima.compareTo(maxima) > 0) {
            throw new ResourceBadRequestException("A referência mínima não pode ser maior que a máxima.");
        }
        if (criticoMinimo != null && minima != null && criticoMinimo.compareTo(minima) > 0) {
            throw new ResourceBadRequestException("O crítico mínimo precisa ficar abaixo (ou no limite) da referência mínima.");
        }
        if (criticoMaximo != null && maxima != null && criticoMaximo.compareTo(maxima) < 0) {
            throw new ResourceBadRequestException("O crítico máximo precisa ficar acima (ou no limite) da referência máxima.");
        }
        if (criticoMinimo != null && criticoMaximo != null && criticoMinimo.compareTo(criticoMaximo) >= 0) {
            throw new ResourceBadRequestException("O crítico mínimo precisa ser menor que o crítico máximo.");
        }
    }

    private void aplicar(ExameLaboratorial exame, ExameLaboratorialRequestDto dto, UUID proprio) {
        String nome = dto.getNome().trim();
        repository.findByNomeIgnoreCase(nome).filter(e -> !e.getUuid().equals(proprio)).ifPresent(e -> {
            throw new ResourceConflictException("Já existe um exame com o nome " + nome + ".");
        });
        // A categoria decide o fluxo (coleta e liberação técnica ou realização e laudo) e não muda depois (ADR-0182).
        CategoriaExame categoria = dto.getCategoria() != null ? dto.getCategoria() : proprio != null ? exame.getCategoria() : CategoriaExame.LABORATORIAL;
        if (proprio != null && categoria != exame.getCategoria()) {
            throw new ResourceUnprocessableEntityException("A categoria do exame não muda depois de criado: cadastre outro exame.");
        }
        if (categoria == CategoriaExame.IMAGEM) {
            if (dto.getModalidade() == null) {
                throw new ResourceBadRequestException("Informe a modalidade do exame de imagem.");
            }
            exame.setNome(nome);
            exame.setCategoria(categoria);
            exame.setModalidade(dto.getModalidade());
            exame.setMaterial(null);
            exame.setTipoResultado(TipoResultadoExame.TEXTO);
            exame.setUnidadeMedida(null);
            exame.setReferenciaMinima(null);
            exame.setReferenciaMaxima(null);
            exame.setCriticoMinimo(null);
            exame.setCriticoMaximo(null);
            exame.setReferenciaTexto(null);
            exame.setPreparo(textoOuNulo(dto.getPreparo()));
            exame.setPrazoDias(dto.getPrazoDias());
            return;
        }
        if (dto.getMaterial() == null || dto.getTipoResultado() == null) {
            throw new ResourceBadRequestException("Informe o material e o tipo de resultado do exame laboratorial.");
        }
        exame.setCategoria(categoria);
        exame.setModalidade(null);
        boolean numerico = dto.getTipoResultado() == TipoResultadoExame.NUMERICO;
        if (numerico) {
            validarLimites(dto.getReferenciaMinima(), dto.getReferenciaMaxima(), dto.getCriticoMinimo(), dto.getCriticoMaximo());
        }
        exame.setNome(nome);
        exame.setMaterial(dto.getMaterial());
        exame.setTipoResultado(dto.getTipoResultado());
        exame.setUnidadeMedida(numerico ? textoOuNulo(dto.getUnidadeMedida()) : null);
        exame.setReferenciaMinima(numerico ? dto.getReferenciaMinima() : null);
        exame.setReferenciaMaxima(numerico ? dto.getReferenciaMaxima() : null);
        exame.setCriticoMinimo(numerico ? dto.getCriticoMinimo() : null);
        exame.setCriticoMaximo(numerico ? dto.getCriticoMaximo() : null);
        exame.setReferenciaTexto(numerico ? null : textoOuNulo(dto.getReferenciaTexto()));
        exame.setPreparo(textoOuNulo(dto.getPreparo()));
        exame.setPrazoDias(dto.getPrazoDias());
    }

    private ExameLaboratorialResponseDto resposta(ExameLaboratorial exame) {
        ExameLaboratorialResponseDto d = ExameLaboratorialResponseDto.fromExame(exame, faixaRepository.findByExame_Uuid(exame.getUuid()));
        d.setInsumos(insumoRepository.findByExame_Uuid(exame.getUuid()).stream()
                .map(com.edufelizardo.maissaudepublica.models.dtos.version1.response.InsumoExameDto::fromInsumo).toList());
        return d;
    }

    // ── Insumos (ADR-0183) ─────────────────────────────────────────────────────────────────────────

    /**
     * Substitui os insumos do exame. Material repetido: 400; material fora de uso: 422; "só com contraste" fora do exame de imagem:
     * 422. Lista vazia remove todos.
     */
    @Transactional
    public ExameLaboratorialResponseDto definirInsumos(UUID exameId,
            List<com.edufelizardo.maissaudepublica.models.dtos.version1.request.InsumoExameRequestDto> lista) {
        ExameLaboratorial exame = buscar(exameId);
        if (lista.stream().map(com.edufelizardo.maissaudepublica.models.dtos.version1.request.InsumoExameRequestDto::getMaterialId).distinct().count() != lista.size()) {
            throw new ResourceBadRequestException("O mesmo material aparece mais de uma vez nos insumos.");
        }
        List<com.edufelizardo.maissaudepublica.models.InsumoExame> novos = new java.util.ArrayList<>();
        for (var dto : lista) {
            if (dto.getMaterialId() == null || dto.getQuantidade() == null || dto.getQuantidade() < 1 || dto.getQuantidade() > 100) {
                throw new ResourceBadRequestException("Cada insumo precisa do material e de uma quantidade de 1 a 100.");
            }
            var material = materialEstoqueRepository.findById(dto.getMaterialId()).orElseThrow(() -> new ResourceNotFoundException(
                    "Não foi possível encontrar o material " + dto.getMaterialId() + " em nossos registros."));
            if (!material.isAtivo()) {
                throw new ResourceUnprocessableEntityException("O material " + material.getNome() + " está fora de uso no Estoque.");
            }
            boolean soComContraste = Boolean.TRUE.equals(dto.getSoComContraste());
            if (soComContraste && exame.getCategoria() != CategoriaExame.IMAGEM) {
                throw new ResourceUnprocessableEntityException("\"Só com contraste\" vale para exame de imagem.");
            }
            var i = new com.edufelizardo.maissaudepublica.models.InsumoExame();
            i.setExame(exame);
            i.setMaterial(material);
            i.setQuantidade(dto.getQuantidade());
            i.setSoComContraste(soComContraste);
            novos.add(i);
        }
        insumoRepository.deleteAll(insumoRepository.findByExame_Uuid(exameId));
        insumoRepository.flush();
        insumoRepository.saveAll(novos);
        return resposta(exame);
    }

    private FaixaReferenciaExame buscarFaixa(UUID uuid) {
        return faixaRepository.findById(uuid).orElseThrow(() -> new ResourceNotFoundException(
                "Não foi possível encontrar uma faixa de referência com o id " + uuid + " em nossos registros."));
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
